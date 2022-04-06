package com.maple.plugin.plugins.linelog

import com.maple.plugin.base.BaseWeaver
import com.maple.plugin.utils.Log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter


class LineLogWeaver : BaseWeaver() {

    override fun isWeavableClass(filePath: String): Boolean {
        Log.log("isWeavableClass: $filePath")
        return super.isWeavableClass(filePath)
                && ("com.mape.asm_learn.LineNumberLog" != filePath)
    }

    override fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
        return LineLogClassAdapter(classWriter)
    }
}