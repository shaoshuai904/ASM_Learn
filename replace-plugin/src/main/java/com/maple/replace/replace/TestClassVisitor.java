package com.maple.replace.replace;

import com.android.ddmlib.Log;
import com.maple.replace.OnCreateMethodVisitor;
import com.maple.replace.SendBroadcastMethodVisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * 代码行替换
 */
public class TestClassVisitor extends ClassVisitor implements Opcodes {
    private String mClassName;
    private final List<ReplaceBean> replaceBeans = ReplaceConfig.getReplaceBeans();

    public TestClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // 11:56:27 E/MS_ASM: visitMethod:4 - onCreate - (Landroid/os/Bundle;)V - null
        // Log.e("MS_ASM", "visitMethod:" + access + " - " + name + " - " + desc + " - " + signature);
        Log.e("MS_ASM", "visitMethod:" + access + " - "
                + name + " - " + desc + " - " + signature);
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        String targetName = "com/gavin/asmdemo/SecondActivity";
        if (targetName.equals(this.mClassName)) {
            switch (name) {
                case "onCreate":
                    System.out.println("LifecycleClassVisitor : change method ----> " + name);
                    return new OnCreateMethodVisitor(mv);
                case "testSendBC":
                    return new SendBroadcastMethodVisitor(mv);
            }
        }
        return mv;
    }

}
