package com.maple.plugin.base

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.maple.plugin.extension.RunVariant
import com.maple.plugin.utils.Log
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.util.concurrent.Callable

/**
 * Transform to modify bytecode
 */
open class HunterTransform(
    val project: Project
) : Transform() {
    private var emptyRun = false
    protected var bytecodeWeaver: BaseWeaver = BaseWeaver()

    override fun getName(): String {
        return this.javaClass.simpleName
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun isCacheable(): Boolean {
        return true
    }

    /**
     * 获取运行模式
     */
    open fun getRunVariant(): RunVariant {
        return RunVariant.ALWAYS
    }

    open fun isEmptyRun(variantName: String): Boolean {
        val runVariant = getRunVariant()
        // Log.log("runVariant: $runVariant  variantName: $variantName")
        return when (variantName) {
            "debug" -> runVariant == RunVariant.RELEASE || runVariant == RunVariant.NEVER
            "release" -> runVariant == RunVariant.DEBUG || runVariant == RunVariant.NEVER
            else -> false
        }
    }

    open fun onTransformStart() {}

    open fun onTransformEnd() {}

    @Throws(IOException::class, TransformException::class, InterruptedException::class)
    override fun transform(invocation: TransformInvocation) {
        val startTime = System.currentTimeMillis()
        Log.log("HunterTransform: transform start~")
        onTransformStart()
        val outputProvider = invocation.outputProvider
        val isIncremental = invocation.isIncremental
        emptyRun = isEmptyRun(invocation.context.variantName)
        // Log.log("$name isIncremental = $isIncremental , emptyRun = $emptyRun , inDuplicatedClassSafeMode = ${inDuplicatedClassSafeMode()} ")
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        val urlClassLoader = ClassLoaderHelper.getClassLoader(invocation.inputs, invocation.referencedInputs, project)
        bytecodeWeaver.setClassLoader(urlClassLoader)
        flagForCleanDexBuilderFolder = false
        invocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                submitTask {
                    forEachJar(outputProvider, jarInput, isIncremental)
                }
            }
            input.directoryInputs.forEach { dirInput ->
                submitTask {
                    forEachDir(outputProvider, dirInput, isIncremental)
                }
            }
        }
        worker.await()
        onTransformEnd()
        val costTime = System.currentTimeMillis() - startTime
        Log.log("$name costed: $costTime ms")
    }

    private var worker: Worker = Schedulers.IO()
    private fun submitTask(task: () -> Unit) {
        worker.submit(Callable<Any?> {
            task()
            null
        })
    }

    @Throws(IOException::class)
    private fun forEachDir(outputProvider: TransformOutputProvider, directoryInput: DirectoryInput, isIncremental: Boolean) {
        val inputDir = directoryInput.file
        // Log.log("处理dir： " + inputDir.absolutePath)
        val outputDir = outputProvider.getContentLocation(
            directoryInput.name, directoryInput.contentTypes,
            directoryInput.scopes, Format.DIRECTORY
        )
        FileUtils.forceMkdir(outputDir)
        if (isIncremental) {
            directoryInput.changedFiles.forEach { (inputFile, status) ->
                when (status) {
                    Status.NOTCHANGED -> {}
                    Status.REMOVED -> {
                        val outputFile = File(inputFile.absolutePath.replace(inputDir.absolutePath, outputDir.absolutePath))
                        if (outputFile.exists()) {
                            outputFile.delete()
                        }
                    }
                    Status.ADDED, Status.CHANGED -> {
                        val outputFile = File(inputFile.absolutePath.replace(inputDir.absolutePath, outputDir.absolutePath))
                        if (emptyRun) {
                            FileUtils.touch(outputFile)
                            FileUtils.copyFile(inputFile, outputFile)
                        } else {
                            bytecodeWeaver.weaveSingleClassToFile(inputFile, outputFile, inputDir.absolutePath)
                        }
                    }
                    else -> {}
                }
            }
        } else {
            if (emptyRun) {
                FileUtils.copyDirectory(inputDir, outputDir)
                return
            }
            if (inputDir.isDirectory) {
                inputDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    val outputFile = File(file.absolutePath.replace(inputDir.absolutePath, outputDir.absolutePath))
                    bytecodeWeaver.weaveSingleClassToFile(file, outputFile, inputDir.absolutePath)
                }
            }
        }
    }

    private fun forEachJar(outputProvider: TransformOutputProvider, jarInput: JarInput, isIncremental: Boolean) {
        // Log.log("jarInput: " + jarInput.file.name)
        val dest = outputProvider.getContentLocation(
            jarInput.file.absolutePath, jarInput.contentTypes,
            jarInput.scopes, Format.JAR
        )
        if (isIncremental) {
            when (jarInput.status) {
                Status.NOTCHANGED -> {}
                Status.REMOVED -> if (dest.exists()) {
                    FileUtils.forceDelete(dest)
                }
                Status.ADDED, Status.CHANGED -> {
                    if (emptyRun) {
                        FileUtils.copyFile(jarInput.file, dest)
                    } else {
                        bytecodeWeaver.weaveJar(jarInput.file, dest)
                    }
                }
                else -> {}
            }
        } else {
            // 原谅我！，一些项目会将第三方aar存储在dexbuilder文件夹中的多个副本中，未知问题。
            // Forgive me!, Some project will store 3rd-party aar for several copies in dexbuilder folder,unknown issue.
            if (inDuplicatedClassSafeMode() && !flagForCleanDexBuilderFolder) {
                // cleanDexBuilderFolder(dest)
                try {
                    val dexBuilderDir = replaceLastPart(dest.absolutePath, name, "dexBuilder")
                    // intermediates/transforms/dexBuilder/debug
                    val file = File(dexBuilderDir).parentFile
                    Log.log("clean dexBuilder folder = " + file.absolutePath)
                    if (file.exists() && file.isDirectory) {
                        com.android.utils.FileUtils.deleteDirectoryContents(file)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                flagForCleanDexBuilderFolder = true
            }
            if (emptyRun) {
                FileUtils.copyFile(jarInput.file, dest)
            } else {
                bytecodeWeaver.weaveJar(jarInput.file, dest)
            }
        }
    }

    /**
     * 安全模式下的复制类
     */
    open fun inDuplicatedClassSafeMode(): Boolean {
        return false
    }

    var flagForCleanDexBuilderFolder = false

    private fun replaceLastPart(originString: String, replacement: String, toreplace: String): String {
        val start = originString.lastIndexOf(replacement)
        return StringBuilder()
            .append(originString, 0, start)
            .append(toreplace)
            .append(originString.substring(start + replacement.length))
            .toString()
    }

}