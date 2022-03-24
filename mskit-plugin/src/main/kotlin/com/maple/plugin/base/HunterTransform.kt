package com.maple.plugin.base

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.maple.plugin.extension.RunVariant
import com.maple.plugin.utils.Log
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.io.IOException
import java.util.concurrent.Callable

/**
 * Transform to modify bytecode
 */
open class HunterTransform(
    val project: Project
) : Transform() {
    private val worker: Worker = Schedulers.IO()
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

    open fun getRunVariant(): RunVariant {
        return RunVariant.ALWAYS
    }

    open fun inDuplicatedClassSafeMode(): Boolean {
        return false
    }

    @Throws(IOException::class, TransformException::class, InterruptedException::class)
    override fun transform(invocation: TransformInvocation) {
        Log.log("HunterTransform: transform start~")
        val startTime = System.currentTimeMillis()
        val outputProvider = invocation.outputProvider
        val isIncremental = invocation.isIncremental
        val runVariant = getRunVariant()
        if ("debug" == invocation.context.variantName) {
            emptyRun = runVariant == RunVariant.RELEASE || runVariant == RunVariant.NEVER
        } else if ("release" == invocation.context.variantName) {
            emptyRun = runVariant == RunVariant.DEBUG || runVariant == RunVariant.NEVER
        }
        Log.log("$name isIncremental = $isIncremental , runVariant = $runVariant , emptyRun = $emptyRun , inDuplicatedClassSafeMode = ${inDuplicatedClassSafeMode()} ")
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        val urlClassLoader = ClassLoaderHelper.getClassLoader(invocation.inputs, invocation.referencedInputs, project)
        bytecodeWeaver.setClassLoader(urlClassLoader)
        var flagForCleanDexBuilderFolder = false
        invocation.inputs.forEach { input ->
//            input.jarInputs.forEach { jarInput ->
//                val dest = outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                if (isIncremental && !emptyRun) {
//                    when (jarInput.status) {
//                        Status.NOTCHANGED -> {}
//                        Status.ADDED, Status.CHANGED -> transformJar(jarInput.file, dest)
//                        Status.REMOVED -> if (dest.exists()) {
//                            FileUtils.forceDelete(dest)
//                        }
//                        else -> {}
//                    }
//                } else {
//                    //Forgive me!, Some project will store 3rd-party aar for several copies in dexbuilder folder,unknown issue.
//                    if (inDuplicatedClassSafeMode() && !isIncremental && !flagForCleanDexBuilderFolder) {
//                        cleanDexBuilderFolder(dest)
//                        flagForCleanDexBuilderFolder = true
//                    }
//                    transformJar(jarInput.file, dest)
//                }
//            }
            input.directoryInputs.forEach { dirInput ->
                forEachDir(outputProvider, isIncremental, dirInput)
            }
        }
        worker.await()
        val costTime = System.currentTimeMillis() - startTime
        Log.log("$name costed: $costTime ms")
    }


    private fun transformJar(srcJar: File, destJar: File) {
        worker.submit(Callable<Any?> {
            if (emptyRun) {
                FileUtils.copyFile(srcJar, destJar)
            } else {
                bytecodeWeaver.weaveJar(srcJar, destJar)
            }
            null
        })
    }

    @Throws(IOException::class)
    private fun forEachDir(outputProvider: TransformOutputProvider, isIncremental: Boolean, directoryInput: DirectoryInput) {
        val inputDir = directoryInput.file
        val outputDir = outputProvider.getContentLocation(
            directoryInput.name, directoryInput.contentTypes,
            directoryInput.scopes, Format.DIRECTORY
        )
        FileUtils.forceMkdir(outputDir)
        if (isIncremental && !emptyRun) {
            val fileStatusMap: Map<File, Status> = directoryInput.changedFiles
            for ((inputFile, status) in fileStatusMap) {
                val destFilePath = inputFile.absolutePath.replace(inputDir.absolutePath, outputDir.absolutePath)
                val destFile = File(destFilePath)
                when (status) {
                    Status.NOTCHANGED -> {}
                    Status.REMOVED -> if (destFile.exists()) {
                        destFile.delete()
                    }
                    Status.ADDED, Status.CHANGED -> {
                        try {
                            FileUtils.touch(destFile)
                        } catch (e: IOException) {
                            //maybe mkdirs fail for some strange reason, try again.
                            FileUtils.forceMkdirParent(destFile)
                        }
                        worker.submit(Callable<Any?> {
                            bytecodeWeaver.weaveSingleClassToFile(inputFile, destFile, inputDir.absolutePath)
                            null
                        })
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
                for (file in com.android.utils.FileUtils.getAllFiles(inputDir)) {
                    worker.submit(Callable<Any?> {
                        val outputFile = File(file.absolutePath.replace(inputDir.absolutePath, outputDir.absolutePath))
                        bytecodeWeaver.weaveSingleClassToFile(file, outputFile, inputDir.absolutePath)
                        null
                    })
                }
            }
        }
    }

    private fun cleanDexBuilderFolder(dest: File) {
        worker.submit(Callable<Any?> {
            try {
                val dexBuilderDir = replaceLastPart(dest.absolutePath, name, "dexBuilder")
                //intermediates/transforms/dexBuilder/debug
                val file = File(dexBuilderDir).parentFile
                project.logger.warn("clean dexBuilder folder = " + file.absolutePath)
                if (file.exists() && file.isDirectory) {
                    com.android.utils.FileUtils.deleteDirectoryContents(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        })
    }

    private fun replaceLastPart(originString: String, replacement: String, toreplace: String): String {
        val start = originString.lastIndexOf(replacement)
        val sb = StringBuilder()
        sb.append(originString, 0, start)
        sb.append(toreplace)
        sb.append(originString.substring(start + replacement.length))
        return sb.toString()
    }

}