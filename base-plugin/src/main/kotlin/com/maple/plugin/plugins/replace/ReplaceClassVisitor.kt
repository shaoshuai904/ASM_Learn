package com.maple.plugin.plugins.replace

import com.android.ddmlib.Log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 代码行替换
 */
class ReplaceClassVisitor(
    cv: ClassVisitor,
    val configs: List<ReplaceBean>
) : ClassVisitor(Opcodes.ASM5, cv), Opcodes {
    private var mClassName: String = ""

    override fun visit(
        version: Int, // 51
        access: Int,  // 33
        name: String, // androidx/fragment/app/FragmentActivity
        signature: String?,
        superName: String, // androidx/activity/ComponentActivity
        interfaces: Array<String>?
    ) {
        mClassName = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,  // 4
        name: String, // onCreate
        desc: String, // (Landroid/os/Bundle;)V
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        // 4 - onCreate - (Landroid/os/Bundle;)V - null
        // Log.e("MS_ASM", "visitMethod:$access - $name - $desc - $signature")
        val replace = configs.find {
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
            val bean = configs.find {
                it.isSameOldConfig(opcode, owner, name, descriptor, isInterface)
            }
            if (bean != null) {
                // ctx.sendBroadcast(intent);
                // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V - false

                // BroadcastUtils.sendAppInsideBroadcast(ctx, intent);
                // 184 - com/gavin/asmdemo/BroadcastUtils - sendAppInsideBroadcast - (Landroid/content/Context;Landroid/content/Intent;)V - false
                Log.e("MS_ASM", "执行替换: $opcode - $owner - $name - $descriptor - $isInterface")
                super.visitMethodInsn(bean.getNewOpcodeInt(), bean.newOwner, bean.newName, bean.newDescriptor, bean.newIsInterface)
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }

}