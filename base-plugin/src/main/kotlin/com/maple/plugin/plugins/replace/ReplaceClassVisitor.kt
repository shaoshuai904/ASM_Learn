package com.maple.plugin.plugins.replace

import com.android.ddmlib.Log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 代码行替换
 */
class ReplaceClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM5, cv), Opcodes {
    private var mClassName: String = ""
    private val replaceBeans = ReplaceConfig.getReplaceBeans()

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>?) {
        // 51 - 33 - androidx/fragment/app/FragmentActivity - null - androidx/activity/ComponentActivity
        mClassName = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
        // 4 - onCreate - (Landroid/os/Bundle;)V - null
        // Log.e("MS_ASM", "visitMethod:$access - $name - $desc - $signature")
        val replace = replaceBeans.find {
            mClassName == it.newOwner && name == it.newName
        }
        val vm = super.visitMethod(access, name, desc, signature, exceptions)
        return if (replace != null) {
            Log.e("MS_ASM", "跳过原方法: ${replace.newOwner} - ${replace.newName}")
            vm
        } else {
            getReplaceMethodVisitor(vm)
        }
    }

    /**
     * 替换方法体中某一行代码
     */
    private fun getReplaceMethodVisitor(vm: MethodVisitor) = object : MethodVisitor(Opcodes.ASM5, vm) {
        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
            val bean = replaceBeans.find {
                it.isSameOldConfig(opcode, owner, name, descriptor, isInterface)
            }
            if (bean != null) {
                // ctx.sendBroadcast(intent);
                // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V - false

                // BroadcastUtils.sendAppInsideBroadcast(ctx, intent);
                // 184 - com/gavin/asmdemo/BroadcastUtils - sendAppInsideBroadcast - (Landroid/content/Context;Landroid/content/Intent;)V - false
                Log.e("MS_ASM", "执行替换: $opcode - $owner - $name - $descriptor - $isInterface")
                super.visitMethodInsn(bean.newOpcode, bean.newOwner, bean.newName, bean.newDescriptor, bean.isNewIsInterface)
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }

}