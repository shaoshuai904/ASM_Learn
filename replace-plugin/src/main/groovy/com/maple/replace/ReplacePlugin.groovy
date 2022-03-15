package com.maple.replace

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.maple.replace.replace.ReplaceClassVisitor
import com.maple.replace.replace.TestClassVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

class ReplacePlugin extends Transform implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println '-------------hello gradle plugin!---------- '
        // registerTransform
        // AppExtension appExtension = project.extensions.findByType(AppExtension.class)
        def appExtension = project.extensions.getByType(AppExtension)
        appExtension.registerTransform(this)
    }

    @Override
    String getName() {
        return "LifecyclePlugin"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        // 需要处理的数据类型，有两种枚举类型
        // CLASSES 代表处理的编译后的class文件，
        // RESOURCES 代表要处理的java资源
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        // 值Transform 的作用范围，有一下7种类型：
        // 1.EXTERNAL_LIBRARIES        只有外部库
        // 2.PROJECT                   只有项目内容
        // 3.PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
        // 4.PROVIDED_ONLY             只提供本地或远程依赖项
        // 5.SUB_PROJECTS              只有子项目
        // 6.SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)
        // 7.TESTED_CODE               由当前变量(包括依赖项)测试的代码
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        // 是否支持增量编译
        return false
    }

    // 这个方法用来进行具体的输入输出处理，这里可以获取输入的目录文件以及jar包文件
    @Override
    void transform(@NonNull TransformInvocation transformInvocation) {
        println '--------------- LifecyclePlugin visit start --------------- '
        def startTime = System.currentTimeMillis()
        Collection<TransformInput> inputs = transformInvocation.inputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        //删除之前的输出
        if (outputProvider != null)
            outputProvider.deleteAll()
        //遍历inputs
        inputs.each { TransformInput input ->
            // 遍历 directoryInputs
            println '-------------处理dir: ' + input.directoryInputs.size()
            input.directoryInputs.each { DirectoryInput directoryInput ->
                handleDirectoryInput(directoryInput, outputProvider)
            }
            println '-------------处理jar: ' + input.jarInputs.size()
            // 遍历 jarInputs
            input.jarInputs.each { JarInput jarInput ->
                handleJarInputs(jarInput, outputProvider)
            }
        }
        def cost = (System.currentTimeMillis() - startTime) / 1000
        println '--------------- LifecyclePlugin visit end --------------- '
        println "LifecyclePlugin cost ： $cost s"
    }

    /**
     * 处理文件目录下的class文件
     */
    static void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        def rootPath = directoryInput.file.absolutePath
        println '---开始处理：' + rootPath
        //是否是目录
        if (directoryInput.file.isDirectory()) {
            // 列出 </app/build/intermediates/javac/debug/classes> 目录所有文件（包含子文件夹，子文件夹内文件）
            directoryInput.file.eachFileRecurse { File file ->
                def className = file.absolutePath.substring(rootPath.length() + 1)
                println '----debug/class: ' + className
                if (checkClassFile(className)) {
                    // println '----需要处理class中的类：' + className
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new TestClassVisitor(classWriter)
                    classReader.accept(cv, EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    FileOutputStream fos = new FileOutputStream(file.absolutePath)
                    fos.write(code)
                    fos.close()
                }
            }
        }
        //处理完输入文件之后，要把输出给下一个任务
        def dest = outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes,
                Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    /**
     * 处理Jar中的class文件
     */
    static void handleJarInputs(JarInput jarInput, TransformOutputProvider outputProvider) {
        // println '---开始处理：<' + jarInput.file.getAbsolutePath() + '>'
        println '---处理Jar：' + jarInput.file.getName()
        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            //重名名输出文件,因为可能同名,会覆盖
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            //避免上次的缓存被重复插入
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))
            //用于保存
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                //插桩class
                println '----jar/class: ' + entryName
                if (checkClassFile(entryName)) {
                    //class文件处理
                    //println '----需要处理Jar中的类：' + entryName
                    jarOutputStream.putNextEntry(zipEntry)
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new ReplaceClassVisitor(classWriter)// 开始处理
                    classReader.accept(cv, EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    jarOutputStream.write(code)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }
            //结束
            jarOutputStream.close()
            jarFile.close()
            def dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(tmpFile, dest)
            tmpFile.delete()
        }
    }

    /**
     * 简单过滤 debug/class 和 jar 中的类，检查class文件是否需要处理
     * e.g:
     * com/gavin/asmdemo/SecondActivity.class
     * androidx/fragment/app/FragmentActivity.class
     */
    static boolean checkClassFile(String name) {
//        ---处理Jar：R.jar
//        ----需要处理Jar中的类：androidx/activity/R$layout.class
        if (!name.endsWith(".class"))
            return false
        return (
                true
//                "com/gavin/asmdemo/SecondActivity.class" == name
//                        || "androidx/fragment/app/FragmentActivity.class" == name
        )
    }

}