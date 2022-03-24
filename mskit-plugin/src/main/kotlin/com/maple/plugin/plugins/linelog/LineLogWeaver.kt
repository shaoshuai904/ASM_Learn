package com.maple.plugin.plugins.linelog

import com.android.build.gradle.internal.LoggerWrapper
import com.maple.plugin.base.BaseWeaver
import com.maple.plugin.utils.Log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

/**
 * Created by Quinn on 09/07/2017.
 */
class LineLogWeaver : BaseWeaver() {
    companion object {
        private val logger = LoggerWrapper.getLogger(LineLogWeaver::class.java)
    }

    override fun isWeavableClass(filePath: String): Boolean {
        val superResult = super.isWeavableClass(filePath)
        Log.log("isWeavableClass: $filePath")
        val isByteCodePlugin = "com.mape.asm_learn.LineNumberLog" != filePath
//        className.startsWith("com.mape.asm_learn.linelog")
        return superResult && isByteCodePlugin
    }

    override fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
        return LineLogClassAdapter(classWriter)
    }
}