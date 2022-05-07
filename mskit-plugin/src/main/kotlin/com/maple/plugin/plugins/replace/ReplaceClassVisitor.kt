package com.maple.plugin.plugins.replace

import com.maple.plugin.utils.Log
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 代码行替换
 */
class ReplaceClassVisitor(
    cv: ClassVisitor,
    val configs: List<ReplaceBean>?
) : ClassVisitor(Opcodes.ASM7, cv), Opcodes {
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
//            LogMethodVisitor(vm)
            ReplaceMethodVisitor(vm, configs, mClassName)
            // ReplaceMethodVisitor2(vm, configs, mClassName)
        }
    }

}