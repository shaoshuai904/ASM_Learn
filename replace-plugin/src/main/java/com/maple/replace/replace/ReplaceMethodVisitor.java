package com.maple.replace.replace;

import com.android.ddmlib.Log;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * 替换方法体中某一行代码
 */
public class ReplaceMethodVisitor extends MethodVisitor {
    List<ReplaceBean> replaceBeans;

    public ReplaceMethodVisitor(MethodVisitor mv, List<ReplaceBean> reps) {
        super(Opcodes.ASM5, mv);
        this.replaceBeans = reps;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        ReplaceBean targetBean = null;
        if (replaceBeans != null && replaceBeans.size() > 0) {
            for (ReplaceBean bean : replaceBeans) {
                if (bean.isSameOldConfig(opcode, owner, name, descriptor, isInterface)) {
                    targetBean = bean;
                }
            }
        }
        if (targetBean != null) {
            Log.e("MS_ASM", "- 执行替换:" + opcode
                    + " - " + owner + " - " + name + " - " + descriptor
                    + " - " + isInterface
            );
            super.visitMethodInsn(targetBean.getNewOpcode(), targetBean.getNewOwner(), targetBean.getNewName(),
                    targetBean.getNewDescriptor(), targetBean.isNewIsInterface());
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
        // ctx.sendBroadcast(intent);
        // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V - false

        // BroadcastUtils.sendAppInsideBroadcast(ctx, intent);
        // 184 - com/gavin/asmdemo/BroadcastUtils - sendAppInsideBroadcast - (Landroid/content/Context;Landroid/content/Intent;)V - false
    }

//    @Override
//    public void visitLineNumber(int line, Label start) {
//        Log.e("MS_ASM", "- 当前代码行数:" + line);
//        super.visitLineNumber(line, start);
//    }
//
//    @Override
//    public void visitMaxs(int maxStack, int maxLocals) {
//        Log.e("MS_ASM", "- visitMaxs:" + maxStack + " - " + maxLocals);
//        super.visitMaxs(maxStack, maxLocals);
//    }
//
//    @Override
//    public void visitEnd() {
//        Log.e("MS_ASM", "- visitEnd ~");
//        super.visitEnd();
//    }

}
