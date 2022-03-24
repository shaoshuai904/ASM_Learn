package com.maple.plugin.plugins.linelog

import com.android.build.gradle.internal.LoggerWrapper
import com.maple.plugin.plugins.linelog.LineLogMethodAdapter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by Quinn on 15/09/2018.
 */
class LineLogMethodAdapter(mv: MethodVisitor?) : MethodVisitor(Opcodes.ASM7, mv), Opcodes {
    private var lineNumber = 0

    companion object {
        private val logger = LoggerWrapper.getLogger(LineLogMethodAdapter::class.java)
        private const val mOwner = "com/maple/asm_learn/LineNumberLog"
    }

    override fun visitLineNumber(line: Int, start: Label?) {
        lineNumber = line
        super.visitLineNumber(line, start)
    }


    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        if ("android/util/Log" == owner) {
            val linenumberConst = lineNumber.toString()
            when (name) {
                "v", "d", "i", "w", "e" -> {
                    when (desc) {
                        "(Ljava/lang/String;Ljava/lang/String;)I" -> {
                            mv.visitLdcInsn(linenumberConst)
                            mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC, mOwner, name,
                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", false
                            )
                        }
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I" -> {
                            mv.visitLdcInsn(linenumberConst)
                            mv.visitMethodInsn(
                                Opcodes.INVOKESTATIC, mOwner, name,
                                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;Ljava/lang/String;)I", false
                            )
                        }
                        else -> {
                            super.visitMethodInsn(opcode, owner, name, desc, itf)
                        }
                    }
                }
                "println" -> {
                    if ("(ILjava/lang/String;Ljava/lang/String;)I" == desc) {
                        mv.visitLdcInsn(linenumberConst)
                        mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC, mOwner, name,
                            "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", false
                        )
                    } else {
                        super.visitMethodInsn(opcode, owner, name, desc, itf)
                    }
                }
                else -> {
                    super.visitMethodInsn(opcode, owner, name, desc, itf)
                }
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }
    }

}