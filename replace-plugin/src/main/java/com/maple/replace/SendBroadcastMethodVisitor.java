package com.maple.replace;

import com.android.ddmlib.Log;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gavin
 * @date 2019/2/19
 */
public class SendBroadcastMethodVisitor extends MethodVisitor {

    public SendBroadcastMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return super.visitAnnotationDefault();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Log.e("MS_ASM", "- visitAnnotation:" + descriptor
                + " - " + visible
        );
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        Log.e("MS_ASM", "- visitParameterAnnotation:" + parameter
                + " - " + descriptor
                + " - " + visible
        );
        return super.visitParameterAnnotation(parameter, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
    }

    @Override
    public void visitCode() {
        Log.e("MS_ASM", "- visitCode:~");
        super.visitCode();
        //方法执行前插入
        // Log.e("MS_ASM", "-------> onCreate : " + this.getClass().getSimpleName());
        mv.visitLdcInsn("MS_ASM");
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn("-------> onCreate : ");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(Opcodes.POP);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitInsn(int opcode) {
        Log.e("MS_ASM", "- visitInsn:" + opcode);
        //方法执行后插入
        /*if (opcode == Opcodes.RETURN) {
            mv.visitLdcInsn("TAG");
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitLdcInsn("-------> onCreate : end ：");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            mv.visitInsn(Opcodes.POP);
        }*/
        super.visitInsn(opcode);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        Log.e("MS_ASM", "- visitFieldInsn:" + opcode
                + " - " + owner
                + " - " + name
                + " - " + descriptor
        );
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    final int oldOpcode = Opcodes.INVOKEVIRTUAL;// 182;
    final String oldOwner = "android/content/Context";
    final String oldName = "sendBroadcast";
    final String oldDescriptor = "(Landroid/content/Intent;)V";

    final int newOpcode = Opcodes.INVOKESTATIC;//184;
    final String newOwner = "com/gavin/asmdemo/BroadcastUtils";
    final String newName = "sendAppInsideBroadcast";
    final String newDescriptor = "(Landroid/content/Context;Landroid/content/Intent;)V";

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        Log.e("MS_ASM", "- visitMethodInsn:" + opcode
                + " - " + owner
                + " - " + name
                + " - " + descriptor
                + " - " + isInterface
        );
        if (oldOpcode == opcode && oldOwner.equals(owner) && oldName.equals(name) && oldDescriptor.equals(descriptor)) {
            super.visitMethodInsn(newOpcode, newOwner, newName, newDescriptor, isInterface);
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
        // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V - false
        // 184 - com/gavin/asmdemo/BroadcastUtils - sendAppInsideBroadcast - (Landroid/content/Context;Landroid/content/Intent;)V - false
        // super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        Log.e("MS_ASM", "- visitTypeInsn:" + opcode + " - " + type);
        // E/MS_ASM: - visitTypeInsn:187 - android/content/Intent
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        Log.e("MS_ASM", "- visitVarInsn:" + opcode + " - " + var);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        Log.e("MS_ASM", "- visitLocalVariable:" + name
                + " - " + descriptor
                + " - " + signature
                + " - " + index
        );
        //08:06:41 E/MS_ASM: - visitLocalVariable:this - Lcom/gavin/asmdemo/SecondActivity; - null - 0
        //08:06:41 E/MS_ASM: - visitLocalVariable:ctx - Landroid/content/Context; - null - 1
        //08:06:41 E/MS_ASM: - visitLocalVariable:intent - Landroid/content/Intent; - null - 2
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        Log.e("MS_ASM", "- 当前代码行数:" + line);
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        Log.e("MS_ASM", "- visitMaxs:" + maxStack + " - " + maxLocals);
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        Log.e("MS_ASM", "- visitEnd ~");
        super.visitEnd();
    }


}
