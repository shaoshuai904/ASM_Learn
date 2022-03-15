package com.maple.replace.replace;

import com.android.ddmlib.Log;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * 代码行替换
 */
public class ReplaceClassVisitor extends ClassVisitor implements Opcodes {
    private String mClassName;
    private final List<ReplaceBean> replaceBeans = ReplaceConfig.getReplaceBeans();

    public ReplaceClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // 11:12:25 E/MS_ASM: visit:51 - 33
        // - androidx/fragment/app/FragmentActivity
        // - null
        // - androidx/activity/ComponentActivity
//        Log.e("MS_ASM", "visit:" + version + " - " + access
//                + " - " + name
//                + " - " + signature
//                + " - " + superName
//        );
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // 11:56:27 E/MS_ASM: visitMethod:4 - onCreate - (Landroid/os/Bundle;)V - null
        // Log.e("MS_ASM", "visitMethod:" + access + " - " + name + " - " + desc + " - " + signature);
        ReplaceBean targetBean = null;
        if (replaceBeans.size() > 0) {
            for (ReplaceBean bean : replaceBeans) {
                if (mClassName.equals(bean.getNewOwner()) && name.equals(bean.getNewName())) {
                    targetBean = bean;
                }
            }
        }

        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (targetBean != null) {
            Log.e("MS_ASM", "- 跳过原方法:" + targetBean.getNewOwner() + " - " + targetBean.getNewName());
            return mv;
        } else {
            return new ReplaceMethodVisitor(mv, replaceBeans);
        }
    }

}
