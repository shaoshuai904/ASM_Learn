package com.maple.replace;

import com.android.ddmlib.Log;
import com.maple.replace.replace.ReplaceConfig;
import com.maple.replace.replace.ReplaceMethodVisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gavin
 * @date 2019/2/18
 * lifecycle class visitor
 */
public class LifecycleClassVisitor extends ClassVisitor implements Opcodes {
    private String mClassName;

    public LifecycleClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // 11:12:25 E/MS_ASM: visit:51 - 33
        // - androidx/fragment/app/FragmentActivity
        // - null
        // - androidx/activity/ComponentActivity
        Log.e("MS_ASM", "visit:" + version + " - " + access
                + " - " + name
                + " - " + signature
                + " - " + superName
        );
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        Log.e("MS_ASM", "visitSource: " + source + " - " + debug);
        super.visitSource(source, debug);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        Log.e("MS_ASM", "visitOuterClass: " + owner + " - " + name + " - " + descriptor);
        super.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Log.e("MS_ASM", "visitAnnotation: " + descriptor + " - " + visible);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        Log.e("MS_ASM", "visitAttribute: " + attribute.type);
        super.visitAttribute(attribute);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        Log.e("MS_ASM", "visitInnerClass: " + name + " - " + outerName + " - " + innerName + " - " + access);
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        Log.e("MS_ASM", "visitField: " + access + " - " + name + " - " + descriptor + " - " + signature + " - " + value);
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // 11:56:27 E/MS_ASM: visitMethod:4 - onCreate - (Landroid/os/Bundle;)V - null
        Log.e("MS_ASM", "visitMethod:" + access + " - "
                + name + " - " + desc + " - " + signature);
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        // String targetName = "androidx/fragment/app/FragmentActivity";
        String targetName = "com/gavin/asmdemo/SecondActivity";
        if (targetName.equals(this.mClassName)) {
            switch (name) {
                case "onCreate":
                    System.out.println("LifecycleClassVisitor : change method ----> " + name);
                    return new OnCreateMethodVisitor(mv);
                case "onDestroy":
                    System.out.println("LifecycleClassVisitor : change method ----> " + name);
                    return new OnDestroyMethodVisitor(mv);
//                case "testSendBC":
//                    return new SendBroadcastMethodVisitor(mv);
//                    return new ReplaceMethodVisitor(mv, ReplaceConfig.getReplaceBeans());
            }
        }
        return new ReplaceMethodVisitor(mv, ReplaceConfig.getReplaceBeans());
//        return mv;
    }

    @Override
    public void visitEnd() {
        Log.e("MS_ASM", "visitEnd ~");
        super.visitEnd();
    }


}
