package com.maple.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.maple.plugin.utils.ClassUtils
import com.maple.plugin.utils.DigestUtils
import com.maple.plugin.utils.Log
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.Type
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream


abstract class BaseTransform protected constructor(val project: Project) : Transform() {
    //    internal open val transformers = listOf<Transformer>()
//    private val executorService: ExecutorService = ForkJoinPool.commonPool()
    private val taskList = mutableListOf<Callable<Unit>>()
    private fun submitTask(task: () -> Unit) {
//        taskList.add(Callable<Unit> {
        task()
//        })
    }

    override fun transform(invocation: TransformInvocation) {
        val startTime = System.currentTimeMillis()
        Log.log("transform start--------------->")
        onTransformStart()
        val context = invocation.context
        // 管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        val outputProvider = invocation.outputProvider
        // 是否增量编译
        val isIncremental = invocation.isIncremental && this.isIncremental
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        // 引用型输入，无需输出。
        // invocation.referencedInputs.forEach {  }
        // 消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
        invocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                submitTask {
                    forEachJar(jarInput, outputProvider, context, isIncremental)
                }
            }
            input.directoryInputs.forEach { dirInput ->
                submitTask {
                    forEachDirectory(dirInput, outputProvider, context, isIncremental)
                }
            }
        }
        val taskListFeature = ForkJoinPool.commonPool().invokeAll(taskList)
        taskListFeature.forEach {
            it.get()
        }
        onTransformEnd()
        Log.log("transform end---------------> 耗时: " + (System.currentTimeMillis() - startTime) + " ms")
    }

    protected open fun onTransformStart() {}

    protected open fun onTransformEnd() {}

    private fun forEachJar(
        jarInput: JarInput,
        outputProvider: TransformOutputProvider,
        context: Context,
        isIncremental: Boolean
    ) {
        // Log.log("jarInput: " + jarInput.file.name)
        val destFile = outputProvider.getContentLocation(
            DigestUtils.generateJarFileName(jarInput.file),
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )

        // 增量编译
        if (isIncremental) {
            when (jarInput.status) {
                Status.NOTCHANGED -> {}
                Status.REMOVED -> {
                    Log.log("增量处理 jar： " + jarInput.file.name + " remove~")
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                    return
                }
                Status.ADDED, Status.CHANGED -> {
                    Log.log("增量处理 jar： " + jarInput.file.name + " add or change~")
                }
                else -> {
                    return
                }
            }
        }

        if (destFile.exists()) {
            FileUtils.forceDelete(destFile)
        }
        val modifiedJar = if (ClassUtils.isLegalJar(jarInput.file)) {
            modifyJar(jarInput.file, context.temporaryDir)
        } else {
            Log.log("不处理 jar： " + jarInput.file.name)
            jarInput.file
        }
        FileUtils.copyFile(modifiedJar, destFile)
    }

    private fun modifyJar(jarFile: File, temporaryDir: File): File {
        Log.log("处理 jar： " + jarFile.name)
        val tempOutputJarFile = File(temporaryDir, DigestUtils.generateJarFileName(jarFile))
        if (tempOutputJarFile.exists()) {
            tempOutputJarFile.delete()
        }
        val jarOutputStream = JarOutputStream(FileOutputStream(tempOutputJarFile))
        val inputJarFile = JarFile(jarFile, false)
        try {
            val enumeration = inputJarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val jarEntryName = jarEntry.name
                if (jarEntryName.endsWith(".DSA") || jarEntryName.endsWith(".SF")) {
                    //ignore
                } else {
                    val inputStream = inputJarFile.getInputStream(jarEntry)
                    try {
                        val sourceClassBytes = IOUtils.toByteArray(inputStream)
                        val modifiedClassBytes = if (jarEntry.isDirectory || !ClassUtils.isLegalClass(jarEntryName)) {
                            null
                        } else {
                            modifyClass(sourceClassBytes)
                        }
                        jarOutputStream.putNextEntry(JarEntry(jarEntryName))
                        jarOutputStream.write(modifiedClassBytes ?: sourceClassBytes)
                        jarOutputStream.closeEntry()
                    } finally {
                        IOUtils.closeQuietly(inputStream)
                    }
                }
            }
        } finally {
            jarOutputStream.flush()
            IOUtils.closeQuietly(jarOutputStream)
            IOUtils.closeQuietly(inputJarFile)
        }
        return tempOutputJarFile
    }

    /**
     * 处理文件目录下的class文件
     */
    private fun forEachDirectory(
        directoryInput: DirectoryInput,
        outputProvider: TransformOutputProvider,
        context: Context,
        isIncremental: Boolean
    ) {
        val dir = directoryInput.file
        Log.log("处理dir： " + dir.absolutePath)
        val dest = outputProvider.getContentLocation(
            directoryInput.name,
            directoryInput.contentTypes,
            directoryInput.scopes,
            Format.DIRECTORY
        )
        val srcDirPath = dir.absolutePath
        val destDirPath = dest.absolutePath
        val temporaryDir = context.temporaryDir
        FileUtils.forceMkdir(dest)
        // 增量编译
        if (isIncremental) {
            directoryInput.changedFiles.forEach { map ->
                val classFile = map.key
                when (map.value) {
                    Status.NOTCHANGED -> {}
                    Status.ADDED, Status.CHANGED -> {
                        Log.log("增量处理 class： " + classFile.absoluteFile + " 添加 or 改变～")
                        modifyClassFile(classFile, srcDirPath, destDirPath, temporaryDir)
                    }
                    Status.REMOVED -> {
                        Log.log("增量处理 class： " + classFile.absoluteFile + " 删除～")
                        //最终文件应该存放的路径
                        val destFilePath = classFile.absolutePath.replace(srcDirPath, destDirPath)
                        val destFile = File(destFilePath)
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                    }
                    else -> {}
                }
            }
        } else {
            directoryInput.file.walkTopDown().filter { it.isFile }.forEach { classFile ->
                modifyClassFile(classFile, srcDirPath, destDirPath, temporaryDir)
            }
        }
    }

    private fun modifyClassFile(
        classFile: File,
        srcDirPath: String,
        destDirPath: String,
        temporaryDir: File
    ) {
        // Log.log("处理 class： " + classFile.absoluteFile)
        //最终文件应该存放的路径
        val destFilePath = classFile.absolutePath.replace(srcDirPath, destDirPath)
        val destFile = File(destFilePath)
        if (destFile.exists()) {
            destFile.delete()
        }
        //拿到修改后的临时文件
        val modifyClassFile = if (ClassUtils.isLegalClass(classFile)) {
            modifyClass(classFile, temporaryDir)
        } else {
            null
        }
        // 将修改结果保存到目标路径
        FileUtils.copyFile(modifyClassFile ?: classFile, destFile)
        modifyClassFile?.delete()
    }

    private fun modifyClass(classFile: File, temporaryDir: File): File {
        val byteArray = IOUtils.toByteArray(FileInputStream(classFile))
        val modifiedByteArray = modifyClass(byteArray)
        val modifiedFile = File(temporaryDir, DigestUtils.generateClassFileName(classFile))
        if (modifiedFile.exists()) {
            modifiedFile.delete()
        }
        modifiedFile.createNewFile()
        val fos = FileOutputStream(modifiedFile)
        fos.write(modifiedByteArray)
        fos.close()
        return modifiedFile
    }

    protected fun getVisitPosition(
        argumentTypes: Array<Type>,
        parameterIndex: Int,
        isStaticMethod: Boolean
    ): Int {
        if (parameterIndex < 0 || parameterIndex >= argumentTypes.size) {
            throw Error("getVisitPosition error")
        }
        return if (parameterIndex == 0) {
            if (isStaticMethod) {
                0
            } else {
                1
            }
        } else {
            getVisitPosition(
                argumentTypes,
                parameterIndex - 1,
                isStaticMethod
            ) + argumentTypes[parameterIndex - 1].size
        }
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = when {
        // transformers.isEmpty() -> mutableSetOf()
        project.plugins.hasPlugin("com.android.application") -> TransformManager.SCOPE_FULL_PROJECT
        project.plugins.hasPlugin("com.android.dynamic-feature") -> TransformManager.SCOPE_FULL_WITH_FEATURES
        project.plugins.hasPlugin("com.android.library") -> TransformManager.PROJECT_ONLY
        else -> TODO("Not an Android project")
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> = when {
        project.plugins.hasPlugin("com.android.application") -> TransformManager.SCOPE_FULL_PROJECT
        project.plugins.hasPlugin("com.android.dynamic-feature") -> TransformManager.SCOPE_FULL_WITH_FEATURES
        project.plugins.hasPlugin("com.android.library") -> TransformManager.PROJECT_ONLY
        else -> super.getReferencedScopes()
    }

    override fun getName(): String = javaClass.simpleName

    override fun isIncremental(): Boolean = true

    protected abstract fun modifyClass(byteArray: ByteArray): ByteArray

}