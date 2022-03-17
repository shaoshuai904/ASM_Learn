package com.maple.plugin.plugins.replace

import com.maple.plugin.BaseTransform
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class ReplaceTransform(project: Project) : BaseTransform(project) {

    override fun modifyClass(byteArray: ByteArray): ByteArray {
        val classReader = ClassReader(byteArray)
        val className = classReader.className
        if ("com/maple/asm_learn/MainActivity" == className) {
//            val superName = classReader.superName
//            println("找到: $className superName: $superName")

            val cw = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val visitor = ReplaceClassVisitor(cw)
            classReader.accept(visitor, ClassReader.EXPAND_FRAMES)
            return cw.toByteArray()
        }
        return byteArray
    }
}