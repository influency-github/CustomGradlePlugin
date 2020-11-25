package com.fengyuncx.influency.plugin.sophix.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class SophixHookPathMethodVisitor extends MethodVisitor {
    private boolean isDebug;

    public SophixHookPathMethodVisitor(MethodVisitor methodVisitor) {
        this(methodVisitor, false);
    }

    public SophixHookPathMethodVisitor(MethodVisitor methodVisitor, boolean isDebug) {
        super(Opcodes.ASM4, methodVisitor);
        this.isDebug = isDebug;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        //insert code before method call.
//        Label label0 = new Label();
//        mv.visitLabel(label0);
//        mv.visitLineNumber(99, label0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "com/fengyuncx/influency/plugin/flutter/FlutterPatch", "hookSophix", "(Ljava/lang/Object;)V", false);

    }
}
