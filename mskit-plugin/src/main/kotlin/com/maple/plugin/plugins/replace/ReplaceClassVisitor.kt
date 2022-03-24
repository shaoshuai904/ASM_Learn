package com.maple.plugin.plugins.replace

import com.maple.plugin.utils.Log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 代码行替换
 */
class ReplaceClassVisitor(
    cv: ClassVisitor,
    val configs: List<ReplaceBean>?
) : ClassVisitor(Opcodes.ASM5, cv), Opcodes {
    private var mClassName: String = ""

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>?) {
        // 51 - 33 - androidx/fragment/app/FragmentActivity - null - androidx/activity/ComponentActivity - null
        mClassName = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
        // 4 - onCreate - (Landroid/os/Bundle;)V - null
        // 判断当前方法是否是 需要替换的新方法。避免递归
        val newMethod = configs?.find {
            mClassName == it.newOwner && name == it.newName && desc == it.newDescriptor
        }
        val vm = cv.visitMethod(access, name, desc, signature, exceptions)
        return if (newMethod != null) {
            Log.log("跳过原方法: ${newMethod.newOwner} - ${newMethod.newName} - ${newMethod.newDescriptor}")
            vm
        } else {
            getReplaceMethodVisitor(vm)
        }
    }

    /**
     * 替换方法体中某一行代码
     */
    private fun getReplaceMethodVisitor(vm: MethodVisitor) = object : MethodVisitor(Opcodes.ASM5, vm) {
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
                // ctx.sendBroadcast(intent);
                // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V - false

                // BroadcastUtils.sendAppInsideBroadcast(ctx, intent);
                // 184 - com/gavin/asmdemo/BroadcastUtils - sendAppInsideBroadcast - (Landroid/content/Context;Landroid/content/Intent;)V - false
                Log.log("执行替换: $mClassName : $lineNumber \n $opcode - $owner - $name - $descriptor")
                super.visitMethodInsn(bean.getNewOpcodeInt(), bean.newOwner, bean.newName, bean.newDescriptor, bean.newIsInterface)
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }

}