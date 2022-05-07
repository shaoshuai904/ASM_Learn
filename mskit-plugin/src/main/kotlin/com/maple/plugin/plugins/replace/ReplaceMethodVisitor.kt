package com.maple.plugin.plugins.replace

import com.maple.plugin.utils.Log
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 通过临时存储、再写入的方式，移除指定初始化指令
 */
class ReplaceMethodVisitor(
    mv: MethodVisitor?,
    private val configs: List<ReplaceBean>?,
    private val mClassName: String
) : MethodVisitor(Opcodes.ASM7, mv) {
    private var lineNumber = 0

    // 是否查找到初始化方法
    private var findInitMethod = false
    private val steps = arrayListOf<List<Any?>>()

    /**
     * 临时存储指令
     */
    private fun addStep(method: String, vararg arguments: Any?) {
        val list: ArrayList<Any?> = arrayListOf(method)
        list.addAll(arguments.toList())
        //Log.log("存：$list")
        steps.add(list)
    }


    enum class NewDupType {
        NEW_DUP,
        NEW_DUP_STORE,
        NEW_DUP_DUP_STORE,
        NEW_DUP_LOOP
    }

    /**
     * 类型一：
     * mv.visitTypeInsn(NEW, "java/io/File");
     * mv.visitInsn(DUP);
     * mv.visitVarInsn(ALOAD, 2);
     * mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
     * mv.visitVarInsn(ASTORE, 3);
     *
     * 类型二：
     * visitTypeInsn: NEW java/io/File
     * visitInsn: DUP
     * visitVarInsn: ASTORE, 2
     * ...
     * visitMethodInsn: INVOKESPECIAL, java/io/File, <init>, (Ljava/lang/String;)V, false
     *
     * 类型三：
     * visitTypeInsn: NEW java/io/File
     * visitInsn: DUP
     * visitInsn: DUP
     * visitVarInsn: ASTORE, 3
     * visitVarInsn: ALOAD, 2
     * visitMethodInsn: 183, java/io/File, <init>, (Ljava/lang/String;)V, false
     *
     * 类型四：
     * methodVisitor.visitTypeInsn(NEW, "java/io/File");
     * methodVisitor.visitInsn(DUP);
     * methodVisitor.visitTypeInsn(NEW, "java/io/File");
     * methodVisitor.visitInsn(DUP);
     * methodVisitor.visitTypeInsn(NEW, "java/io/File");
     * methodVisitor.visitInsn(DUP);
     * methodVisitor.visitLdcInsn("fasdffadf");
     * methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
     * methodVisitor.visitLdcInsn("abcdef");
     * methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/io/File;Ljava/lang/String;)V", false);
     * methodVisitor.visitLdcInsn("fasd");
     * methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/io/File;Ljava/lang/String;)V", false);
     * methodVisitor.visitVarInsn(ASTORE, 3);
     */
    private fun getNewDupStore(): List<List<Any?>>? {
        if (steps.size < 2) {
            return null
        }
        val className = "java/io/File"
        val loopSize = steps.filter { step ->
            // visitTypeInsn: NEW java/io/File
            step.size == 3
                    && "visitTypeInsn" == (step[0] as String)
                    && Opcodes.NEW == (step[1] as Int)
                    && className == (step[2] as String)
        }.size

        if (loopSize > 1) {
            // todo : 取最后一个 new dup，第一个 init , 做 list 内消除
            // 套娃模式
            return null
        } else {
            when {
                isNewDupStore(steps) -> {
                    steps.forEachIndexed { index, step ->
                        if (index < 3) {
                            Log.log("跳过：$index : $step")
                        } else {
                            writeSingleStep(step)
                        }
                    }
                    writeSingleStep(steps[2])
                    steps.clear()
                }
                isNewDupDupStore(steps) -> {
                    steps.forEachIndexed { index, step ->
                        if (index < 4) {
                            Log.log("跳过：$index : $step")
                        } else {
                            writeSingleStep(step)
                        }
                    }
                    writeSingleStep(steps[2])
                    writeSingleStep(steps[3])
                    steps.clear()
                }
                else -> {
                    steps.forEachIndexed { index, step ->
                        if (index < 2) {
                            Log.log("跳过：$index : $step")
                        } else {
                            writeSingleStep(step)
                        }
                    }
                    steps.clear()
                }
            }
            return null
        }
    }

    /**
     * 类型二：
     * visitTypeInsn: NEW java/io/File
     * visitInsn: DUP
     * visitVarInsn: ASTORE, 2
     * ...
     * visitMethodInsn: INVOKESPECIAL, java/io/File, <init>, (Ljava/lang/String;)V, false
     */
    private fun isNewDupStore(steps: List<List<Any?>>): Boolean {
        return steps.size >= 3
                && "visitTypeInsn" == (steps[0][0] as String)
                && Opcodes.NEW == (steps[0][1] as Int)
                && "visitInsn" == (steps[1][0] as String)
                && Opcodes.DUP == (steps[1][1] as Int)
                && "visitVarInsn" == (steps[2][0] as String)
                && Opcodes.ASTORE == (steps[2][1] as Int)
    }

    /**
     * 类型三
     * <p>visitTypeInsn: NEW java/io/File
     * <p>visitInsn: DUP
     * <p>visitInsn: DUP
     * <p>visitVarInsn: ASTORE, 3
     * <p>...
     * <p>visitMethodInsn: 183, java/io/File, <init>, (Ljava/lang/String;)V, false
     */
    private fun isNewDupDupStore(steps: List<List<Any?>>): Boolean {
        return steps.size >= 4
                && "visitTypeInsn" == (steps[0][0] as String)
                && Opcodes.NEW == (steps[0][1] as Int)
                && "visitInsn" == (steps[1][0] as String)
                && Opcodes.DUP == (steps[1][1] as Int)
                && "visitInsn" == (steps[2][0] as String)
                && Opcodes.DUP == (steps[2][1] as Int)
                && "visitVarInsn" == (steps[3][0] as String)
                && Opcodes.ASTORE == (steps[3][1] as Int)
    }

    /**
     * 写入单条指令
     */
    private fun writeSingleStep(step: List<Any?>) {
        val method: String = step[0] as String
        when (method) {
            "visitTypeInsn" -> mv.visitTypeInsn(step[1] as Int, step[2] as String?)
            "visitInsn" -> mv.visitInsn(step[1] as Int)
            "visitLdcInsn" -> mv.visitLdcInsn(step[1])
            "visitVarInsn" -> mv.visitVarInsn(step[1] as Int, step[2] as Int)
            "visitIntInsn" -> mv.visitIntInsn(step[1] as Int, step[2] as Int)
            "visitIincInsn" -> mv.visitIincInsn(step[1] as Int, step[2] as Int)
            "visitFieldInsn" -> mv.visitFieldInsn(step[1] as Int, step[2] as String?, step[3] as String?, step[4] as String?)
            "visitMethodInsn" -> mv.visitMethodInsn(
                step[1] as Int, step[2] as String?, step[3] as String?, step[4] as String?, step[5] as Boolean
            )
            //--
            "visitJumpInsn" -> mv.visitJumpInsn(step[1] as Int, step[2] as Label?)
            "visitLabel" -> mv.visitLabel(step[1] as Label?)
            "visitLineNumber" -> mv.visitLineNumber(step[1] as Int, step[2] as Label?)
            "visitFrame" -> mv.visitFrame(
                step[1] as Int, step[2] as Int, step[3] as Array<out Any>?,
                step[4] as Int, step[5] as Array<out Any>?
            )
            "visitInvokeDynamicInsn" -> mv.visitInvokeDynamicInsn(
                step[1] as String?, step[2] as String?, step[3] as Handle?, step[4]
            )
            "visitMaxs" -> mv.visitMaxs(step[1] as Int, step[2] as Int)
            else -> {}
        }
    }


    // 查找存储的所有指令里，是需要替换的New方法的个数
    private fun findStepsNeedReplaceNewNumber(): Int {
        // 查找 同类型、同类、同方法名 的所有指令 (重载方法)
        val beans: List<ReplaceBean>? = configs?.filter {
            "INVOKESPECIAL" == it.oldOpcode// "INVOKESPECIAL"
                    && "<init>" == it.oldName // "<init>"
            // && "java/io/File" == it.oldOwner // "java/io/File"
            // && descriptor == it.oldDescriptor // "(Ljava/io/File;Ljava/lang/String;)V",
            // && isInterface == it.oldIsInterface // false
        }
        if (beans == null || beans.isEmpty()) {
            return 0
        }

        // 配置文件中所有需要替换 构造方法 的类名
        val classArr = arrayListOf<String>()
        beans.forEach {
            classArr.add(it.oldOwner ?: "")
        }
        //methodVisitor.visitTypeInsn(NEW, "java/io/File");
        //methodVisitor.visitInsn(DUP);
        //methodVisitor.visitTypeInsn(NEW, "java/io/File");
        //methodVisitor.visitInsn(DUP);
        //methodVisitor.visitTypeInsn(NEW, "java/io/File");
        //methodVisitor.visitInsn(DUP);
        val size = steps.filter { step ->
            step.size == 3 && "visitTypeInsn" == step[0] && Opcodes.NEW == step[1]
                    && classArr.contains(step[2])
        }.size
        return size
    }

    // methodVisitor.visitTypeInsn(NEW, "java/io/File");
    // methodVisitor.visitTypeInsn(NEW, "com/maple/asm_learn/MsBean");
    override fun visitTypeInsn(opcode: Int, type: String?) {
        if (
            Opcodes.NEW == opcode
            && configs?.find {
                // 所有构造方法 全屏蔽了
                "<init>" == it.oldName && it.oldOwner == type
            } != null
        ) {
            findInitMethod = true
        }
        if (findInitMethod) {
            addStep("visitTypeInsn", opcode, type)
            // addStep("visitTypeInsn $opcode $type")
        } else {
            super.visitTypeInsn(opcode, type)
        }
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean) {
        // 查找 同类型、同类、同方法名 的所有指令 (重载方法)
        val beans: List<ReplaceBean>? = configs?.filter {
            if (owner == null || name == null || descriptor == null) {
                false
            } else {
                opcode == it.convertOpcode(it.oldOpcode)// INVOKESPECIAL
                        && owner == it.oldOwner // "java/io/File"
                        && name == it.oldName // "<init>"
                        // && descriptor == it.oldDescriptor 方法参数可以不一致
                        && isInterface == it.oldIsInterface
            }
        }
        if (beans != null && beans.isNotEmpty()) {
            // 可能需要替换，（参数不一致的也放进来了）
            // (INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false)
            val bean = beans.find { descriptor == it.oldDescriptor }
            if (bean == null) {
                // 参数不一致，重载方法，不做替换
                addStep("visitMethodInsn", opcode, owner, name, descriptor, isInterface)
                checkWriteSteps(false)
            } else {
                // 参数一致，就是目标方法
                LogFileUtils.addLog("$opcode - $owner - $name - $descriptor", "$mClassName : $lineNumber")
                // 非构造方法的替换
                // [old] ctx.sendBroadcast(intent);
                // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V - false
                // [new] BroadcastUtils.sendAppInsideBroadcast(ctx, intent);
                // 184 - com/gavin/asmdemo/BroadcastUtils - sendAppInsideBroadcast - (Landroid/content/Context;Landroid/content/Intent;)V - false
                addStep("visitMethodInsn", bean.getNewOpcodeInt(), bean.newOwner, bean.newName, bean.newDescriptor, bean.newIsInterface)
                // 目标new方法
                checkWriteSteps("<init>" == name)
            }
        } else {
            if (findInitMethod) {
                addStep("visitMethodInsn", opcode, owner, name, descriptor, isInterface)
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }

    /**
     * 将存储的指令 重新写入。
     */
    private fun checkWriteSteps(isTargetNew: Boolean) {
        val newSize = findStepsNeedReplaceNewNumber()
        Log.log("检查栈内New方法个数： $newSize ")
        when {
            // 内含多个需要替换的构造方法
            newSize > 1 -> {
                findInitMethod = false
                steps.forEach { step -> writeSingleStep(step) }
                steps.clear()
            }
            // 只有一个需要替换的构造方法，简单处理
            newSize == 1 -> {
                if (!isTargetNew) {
                    // 参数不一致的，构造方法
                    // (INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false)
                    val curStep = steps.last()
                    if (
                        "visitMethodInsn" == curStep[0]
                        && Opcodes.INVOKESPECIAL == curStep[1]
                        && steps.first()[2] == curStep[2]
                        && "<init>" == curStep[3]
                    ) {
                        // 检查存储集合 最后一条，如果是 误伤new, 此时的new dup 原封不动，保存 回写
                        findInitMethod = false
                        steps.forEach { step ->
                            Log.log("误伤回写：$step")
                            writeSingleStep(step)
                        }
                        steps.clear()
                    }
                } else {
                    // val addSteps = arrayListOf<List<Any?>>()
                    var doubleDupStore = -1
                    var jumpNum = 2
                    if (isNewDupStore(steps)) {
                        //addSteps.add(steps[2])
                        jumpNum = 3
                    }
                    if (isNewDupDupStore(steps)) {
                        //addSteps.add(steps[2])
                        //addSteps.add(steps[3])
                        jumpNum = 4
                        doubleDupStore = steps[3][2] as Int
                    }
                    findInitMethod = false
                    steps.forEachIndexed { index, step ->
                        if (index < jumpNum) {
                            // 去除目标操作
                            Log.log("去除：$index : $step")
                        } else {
                            Log.log("写入：$step")
                            writeSingleStep(step)
                        }
                    }
                    steps.clear()
                    if (doubleDupStore != -1) {
                        // visitInsn: DUP
                        // visitVarInsn: ASTORE, 3
                        writeSingleStep(arrayListOf("visitInsn", Opcodes.DUP))
                        writeSingleStep(arrayListOf("visitVarInsn", Opcodes.ASTORE, doubleDupStore))
                    }
                }
            }
            // 没有需要构造方法
            else -> {
                findInitMethod = false
                steps.forEach { step ->
                    Log.log("普通回写：$step")
                    writeSingleStep(step)
                }
                steps.clear()
            }
        }
    }


    // -----------------------------------------------------------------------------------------------
    // Parameters, annotations and non standard attributes
    // -----------------------------------------------------------------------------------------------

//  public void visitParameter(final String name, final int access) {}

//  public AnnotationVisitor visitAnnotationDefault() {}

//  public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {}

//  public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {}

//  public void visitAnnotableParameterCount(final int parameterCount, final boolean visible) { }

//  public AnnotationVisitor visitParameterAnnotation( final int parameter, final String descriptor, final boolean visible) {}

//  public void visitAttribute(final Attribute attribute) {}

//  /** Starts the visit of the method's code, if any (i.e. non abstract method). */
//  public void visitCode() {}

    override fun visitFrame(type: Int, numLocal: Int, local: Array<out Any>?, numStack: Int, stack: Array<out Any>?) {
        if (findInitMethod) {
            addStep("visitFrame", type, numLocal, local, numStack, stack)
        } else {
            super.visitFrame(type, numLocal, local, numStack, stack)
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Normal instructions
    // -----------------------------------------------------------------------------------------------

    override fun visitInsn(opcode: Int) {
        if (findInitMethod) {
            addStep("visitInsn", opcode)
        } else {
            super.visitInsn(opcode)
        }
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        if (findInitMethod) {
            addStep("visitIntInsn", opcode, operand)
        } else {
            super.visitIntInsn(opcode, operand)
        }
    }

    override fun visitVarInsn(opcode: Int, va: Int) {
        if (findInitMethod) {
            addStep("visitVarInsn", opcode, va)
        } else {
            super.visitVarInsn(opcode, va)
        }
    }

    // visitTypeInsn

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        if (findInitMethod) {
            addStep("visitFieldInsn", opcode, owner, name, descriptor)
        } else {
            super.visitFieldInsn(opcode, owner, name, descriptor)
        }
    }

    // visitMethodInsn

    override fun visitInvokeDynamicInsn(name: String?, descriptor: String?, bootstrapMethodHandle: Handle?, vararg bootstrapMethodArguments: Any?) {
        if (findInitMethod) {
            addStep("visitInvokeDynamicInsn", name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments)
        } else {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
        }
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
        if (findInitMethod) {
            addStep("visitJumpInsn", opcode, label)
        } else {
            super.visitJumpInsn(opcode, label)
        }
    }

    override fun visitLabel(label: Label?) {
        if (findInitMethod) {
            addStep("visitLabel", label)
        } else {
            super.visitLabel(label)
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Special instructions
    // -----------------------------------------------------------------------------------------------

    override fun visitLdcInsn(value: Any?) {
        if (findInitMethod) {
            addStep("visitLdcInsn", value)
        } else {
            super.visitLdcInsn(value)
        }
    }

    override fun visitIincInsn(index: Int, increment: Int) {
        if (findInitMethod) {
            addStep("visitIincInsn", index, increment)
        } else {
            super.visitIincInsn(index, increment)
        }
    }

//  public void visitTableSwitchInsn( final int min, final int max, final Label dflt, final Label... labels) { }

//  public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) { }

//  public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) { }

//  public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {}

    // -----------------------------------------------------------------------------------------------
    // Exceptions table entries, debug information, max stack and max locals
    // -----------------------------------------------------------------------------------------------

//  public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) { }

//  public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {}

//  public void visitLocalVariable( final String name, final String descriptor,
//      final String signature, final Label start, final Label end, final int index) {}

//  public AnnotationVisitor visitLocalVariableAnnotation(
//      final int typeRef, final TypePath typePath, final Label[] start, final Label[] end,
//      final int[] index, final String descriptor, final boolean visible) { }

    override fun visitLineNumber(line: Int, start: Label?) {
        if (findInitMethod) {
            addStep("visitLineNumber", line, start)
        } else {
            lineNumber = line
            super.visitLineNumber(line, start)
        }
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        if (findInitMethod) {
            addStep("visitMaxs", maxStack, maxLocals)
        } else {
            super.visitMaxs(maxStack, maxLocals)
        }
    }

//  public void visitEnd() { }

}