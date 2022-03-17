package com.maple.plugin.plugins.legalBitmap

import com.maple.plugin.BaseTransform
import com.maple.plugin.utils.Log
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * ASM 字节码插桩：监控大图加载
 * https://mp.weixin.qq.com/s/-38MdII-8e3932Q_kbxOqg
 */
class LegalBitmapTransform(
    project: Project,
    private val config: LegalBitmapConfig
) : BaseTransform(project) {

    private companion object {

        private const val ImageViewClass = "android/widget/ImageView"

        private const val AppCompatImageViewClass = "androidx/appcompat/widget/AppCompatImageView"

    }

    override fun modifyClass(byteArray: ByteArray): ByteArray {
        val classReader = ClassReader(byteArray)
        val className = classReader.className
        val superName = classReader.superName
        Log.log("className: $className superName: $superName")
        return if ((superName == ImageViewClass && className != config.formatMonitorImageViewClass) || className == AppCompatImageViewClass) {
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val classVisitor = object : ClassVisitor(Opcodes.ASM6, classWriter) {
                override fun visit(
                    version: Int, access: Int, name: String?, signature: String?,
                    superName: String?, interfaces: Array<out String>?
                ) {
                    super.visit(
                        version, access, name, signature,
                        config.formatMonitorImageViewClass, interfaces
                    )
                }
            }
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            classWriter.toByteArray()
        } else {
            byteArray
        }
    }

}