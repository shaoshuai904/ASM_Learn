package com.maple.plugin.plugins.addLog

import com.android.ddmlib.Log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AddLogClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM5, cv), Opcodes {
    private var mClassName: String? = null

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        mClassName = name
        super.visit(version, access, name, signature, superName, interfaces)
        aaa()
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        // 11:56:27 E/MS_ASM: visitMethod:4 - onCreate - (Landroid/os/Bundle;)V - null
        Log.e("MS_ASM", "visitMethod: $access - $name - $descriptor - $signature")
        val vm = super.visitMethod(access, name, descriptor, signature, exceptions)

        // String targetName = "androidx/fragment/app/FragmentActivity";
        val targetName = "com/maple/asm_learn/MainActivity"
        if (targetName == mClassName) {
            when (name) {
                "onCreate" -> {
                    println("LifecycleClassVisitor : change method ----> $name")
                    return AddLogMethodVisitor(vm, "[onCreate]")
                }
                "testSendBC" -> {
                    return RunTimeMethodVisitor(access, descriptor, vm)
                }
//                "<init>" -> {
//                    super.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(I)V", signature, exceptions)
//                }
            }
        }
        return vm
    }

    fun aaa() {
        val methodVisitor = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(I)V", null, null)
        methodVisitor.visitCode()
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/maple/asm_learn/BaseActivity", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("ms_app")
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("fas")
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 1)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(Opcodes.POP)
        methodVisitor.visitInsn(Opcodes.RETURN)
//        val label3 = Label()
//        methodVisitor.visitLabel(label3)
//        methodVisitor.visitLocalVariable("this", "Lcom/maple/asm_learn/MainActivity;", null, label0, label3, 0)
//        methodVisitor.visitLocalVariable("a", "I", null, label0, label3, 1)
        methodVisitor.visitMaxs(3, 2)
        methodVisitor.visitEnd()
    }

    override fun visitEnd() {
        Log.e("MS_ASM", "visitEnd ~")
        super.visitEnd()
    }
}