package com.maple.plugin.plugins.replace

import com.maple.plugin.utils.Log
import org.objectweb.asm.*

class LogMethodVisitor(
    mv: MethodVisitor?
) : MethodVisitor(Opcodes.ASM7, mv) {


    // -----------------------------------------------------------------------------------------------
    // Parameters, annotations and non standard attributes
    // -----------------------------------------------------------------------------------------------


    override fun visitParameter(name: String?, access: Int) {
        Log.log("visitParameter: $name, $access")
        super.visitParameter(name, access)
    }

    override fun visitAnnotationDefault(): AnnotationVisitor {
        Log.log("visitAnnotationDefault~")
        return super.visitAnnotationDefault()
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        Log.log("visitAnnotation: $descriptor, $visible")
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
        Log.log("visitTypeAnnotation: $typeRef, $typePath, $descriptor, $visible")
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitAnnotableParameterCount(parameterCount: Int, visible: Boolean) {
        Log.log("visitAnnotableParameterCount: $parameterCount, $visible")
        super.visitAnnotableParameterCount(parameterCount, visible)
    }

    override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor {
        Log.log("visitParameterAnnotation: $parameter, $descriptor, $visible")
        return super.visitParameterAnnotation(parameter, descriptor, visible)
    }

    override fun visitAttribute(attribute: Attribute?) {
        Log.log("visitAttribute: $attribute")
        super.visitAttribute(attribute)
    }

    override fun visitCode() {
        Log.log("visitCode~")
        super.visitCode()
    }

    override fun visitFrame(type: Int, numLocal: Int, local: Array<out Any>?, numStack: Int, stack: Array<out Any>?) {
        Log.log("visitFrame: $type, $numLocal, $local, $numStack, $stack")
        super.visitFrame(type, numLocal, local, numStack, stack)
    }

    // -----------------------------------------------------------------------------------------------
    // Normal instructions
    // -----------------------------------------------------------------------------------------------

    override fun visitInsn(opcode: Int) {
        Log.log("visitInsn: $opcode")
        super.visitInsn(opcode)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        Log.log("visitIntInsn: $opcode, $operand")
        super.visitIntInsn(opcode, operand)
    }

    override fun visitVarInsn(opcode: Int, va: Int) {
        Log.log("visitVarInsn: $opcode, $va")
        super.visitVarInsn(opcode, va)
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        Log.log("visitTypeInsn: $opcode $type")
        super.visitTypeInsn(opcode, type)
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        Log.log("visitFieldInsn: $opcode, $owner, $name, $descriptor")
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
        Log.log("visitMethodInsn: $opcode, $owner, $name, $descriptor, $isInterface")
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun visitInvokeDynamicInsn(name: String?, descriptor: String?, bootstrapMethodHandle: Handle?, vararg bootstrapMethodArguments: Any?) {
        Log.log("visitInvokeDynamicInsn: $name, $descriptor, $bootstrapMethodHandle, $bootstrapMethodArguments")
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
        Log.log("visitJumpInsn: $opcode, $label")
        super.visitJumpInsn(opcode, label)
    }

    override fun visitLabel(label: Label?) {
        Log.log("visitLabel: $label")
        super.visitLabel(label)
    }

    // -----------------------------------------------------------------------------------------------
    // Special instructions
    // -----------------------------------------------------------------------------------------------

    override fun visitLdcInsn(value: Any?) {
        Log.log("visitLdcInsn: $value")
        super.visitLdcInsn(value)
    }

    override fun visitIincInsn(index: Int, increment: Int) {
        Log.log("visitIincInsn: $index, $increment")
        super.visitIincInsn(index, increment)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        Log.log("visitTableSwitchInsn: $min, $max, $dflt, $labels")
        super.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
        Log.log("visitLookupSwitchInsn: $dflt, $keys, $labels")
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) {
        Log.log("visitMultiANewArrayInsn: $descriptor, $numDimensions")
        super.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
        Log.log("visitInsnAnnotation: $typeRef, $typePath, $descriptor, $visible")
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible)
    }
    // -----------------------------------------------------------------------------------------------
    // Exceptions table entries, debug information, max stack and max locals
    // -----------------------------------------------------------------------------------------------

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
        Log.log("visitTryCatchBlock: $start, $end, $handler, $type")
        super.visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor {
        Log.log("visitTryCatchAnnotation: $typeRef, $typePath, $descriptor, $visible")
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitLocalVariable(name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?, index: Int) {
        Log.log("visitLocalVariable: $name, $descriptor, $signature, $start, $end, $index")
        super.visitLocalVariable(name, descriptor, signature, start, end, index)
    }

    override fun visitLocalVariableAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        start: Array<out Label>?,
        end: Array<out Label>?,
        index: IntArray?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor {
        Log.log("visitLocalVariableAnnotation: $typeRef, $typePath, $start, $end, $index, $descriptor, $visible")
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible)
    }

    override fun visitLineNumber(line: Int, start: Label?) {
        Log.log("visitLineNumber: $line, $start")
        super.visitLineNumber(line, start)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        Log.log("visitMaxs: $maxStack, $maxLocals")
        super.visitMaxs(maxStack, maxLocals)
    }

    override fun visitEnd() {
        Log.log("visitEnd!")
        super.visitEnd()
    }

}