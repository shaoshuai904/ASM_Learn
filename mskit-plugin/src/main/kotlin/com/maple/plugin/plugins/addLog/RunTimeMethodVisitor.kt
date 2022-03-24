package com.maple.plugin.plugins.addLog

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter

/**
 * 统计方法耗时：
 *
 * public void testSendBC(Context ctx) {
 *  long var2 = System.currentTimeMillis();
 *  ...原代码
 *  var2 = System.currentTimeMillis() - var2;
 *  Log.e("MS_ASM", "方法耗时：" + var2);
 * }
 */
class RunTimeMethodVisitor(
    access: Int,
    descriptor: String?,
    mv: MethodVisitor?
) : LocalVariablesSorter(Opcodes.ASM7, access, descriptor, mv) {
    var timeLocalIndex: Int = 0

    override fun visitCode() {
        super.visitCode()
        //方法执行前插入
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        // 根据指定的类型创建一个新的本地变量，并直接分配一个本地变量的引用 index，其优势在于可以尽量复用以前的局部变量，而不需要我们考虑本地变量的分配和覆盖问题
        timeLocalIndex = newLocal(Type.LONG_TYPE)
        mv.visitVarInsn(Opcodes.LSTORE, timeLocalIndex)
    }

    override fun visitInsn(opcode: Int) {
        //方法执行后插入
        if (opcode == Opcodes.RETURN) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
            mv.visitVarInsn(Opcodes.LLOAD, timeLocalIndex)
            mv.visitInsn(Opcodes.LSUB)
            mv.visitVarInsn(Opcodes.LSTORE, timeLocalIndex)
            mv.visitLdcInsn("MS_ASM")
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
            mv.visitLdcInsn("\u65b9\u6cd5\u8017\u65f6\uff1a")
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
            mv.visitVarInsn(Opcodes.LLOAD, timeLocalIndex)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false)
            mv.visitInsn(Opcodes.POP)
        }
        super.visitInsn(opcode)
    }
}