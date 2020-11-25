package com.fengyuncx.influency.plugin.sophix.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class SophixHookFlutterMainMethodVisitor extends MethodVisitor {
    private boolean isDebug = false;

    public SophixHookFlutterMainMethodVisitor(MethodVisitor methodVisitor) {
        this(methodVisitor, false);
    }

    public SophixHookFlutterMainMethodVisitor(MethodVisitor methodVisitor, boolean isDebug) {
        super(Opcodes.ASM4, methodVisitor);
        this.isDebug = isDebug;
    }

    @Override
    public void visitCode() {
        super.visitCode();


    }

    @Override
    public void visitInsn(int opcode) {
        //execute after method called.

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, "com/fengyuncx/influency/plugin/flutter/FlutterPatch", "hook", "(Ljava/lang/Object;)V", false);
        super.visitInsn(opcode);
    }

//    @Override
//    public void visitJumpInsn(int opcode, Label label) {
//        super.visitJumpInsn(opcode, label);
//        if(opcode == Opcodes.IFEQ){
//            mv.visitVarInsn(ALOAD, 0);
//            mv.visitMethodInsn(INVOKESTATIC, "com/fengyuncx/influency/plugin/flutter/FlutterPatch", "hook", "(Ljava/lang/Object;)V", false);
//        }
//    }
}
