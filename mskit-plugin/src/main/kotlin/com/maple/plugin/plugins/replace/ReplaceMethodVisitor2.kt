package com.maple.plugin.plugins.replace

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 通过操作栈，使栈内平衡，有残留
 */
class ReplaceMethodVisitor2(
    mv: MethodVisitor?,
    private val configs: List<ReplaceBean>?,
    private val mClassName: String
) : MethodVisitor(Opcodes.ASM7, mv) {

    private var lineNumber = 0
    override fun visitLineNumber(line: Int, start: Label?) {
        lineNumber = line
        super.visitLineNumber(line, start)
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
        val bean = configs?.find {
            it.isSameOldConfig(opcode, owner, name, descriptor, isInterface)
        }
        if (bean != null) {
            LogFileUtils.addLog("$opcode - $owner - $name - $descriptor", "$mClassName : $lineNumber")
            super.visitMethodInsn(bean.getNewOpcodeInt(), bean.newOwner, bean.newName, bean.newDescriptor, bean.newIsInterface)
            //methodVisitor.visitTypeInsn(NEW, "java/io/File");  进栈一个
            //methodVisitor.visitInsn(DUP); 复制栈顶
            //methodVisitor.visitVarInsn(ALOAD, 2); 进栈一个
            //methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
            //methodVisitor.visitVarInsn(ASTORE, 3); 出栈一个

            //methodVisitor.visitVarInsn(ALOAD, 2);
            //methodVisitor.visitMethodInsn(INVOKESTATIC, "com/maple/asm_learn/BroadcastUtils", "createFile", "(Ljava/lang/String;)Ljava/io/File;", false);
            //methodVisitor.visitVarInsn(ASTORE, 3);
            if (
                bean.getOldOpcodeInt() == Opcodes.INVOKESPECIAL
                && "<init>" == bean.oldName
                && bean.getNewOpcodeInt() == Opcodes.INVOKESTATIC
            ) {
                // todo： class 有残留
                mv.visitInsn(Opcodes.SWAP)
                mv.visitInsn(Opcodes.POP)
                mv.visitInsn(Opcodes.SWAP)
                mv.visitInsn(Opcodes.POP)

//                    mv.visitInsn(Opcodes.DUP_X1) // 复制顶部操作数堆栈值并向下插入两个值
//                    mv.visitInsn(Opcodes.POP) // 出栈两个
//                    mv.visitInsn(Opcodes.POP) // 出栈两个
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }

}