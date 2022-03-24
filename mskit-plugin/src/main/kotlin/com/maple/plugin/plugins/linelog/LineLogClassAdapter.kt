package com.maple.plugin.plugins.linelog

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class LineLogClassAdapter(
    cv: ClassVisitor?
) : ClassVisitor(Opcodes.ASM7, cv) {

    override fun visitMethod(
        access: Int, name: String?, descriptor: String?,
        signature: String?, exceptions: Array<out String>?
    ): MethodVisitor {
        // return super.visitMethod(access, name, descriptor, signature, exceptions)
        val mv = cv.visitMethod(access, name, descriptor, signature, exceptions)
        return if (mv != null) {
            LineLogMethodAdapter(mv)
        } else {
            mv
        }
    }
}