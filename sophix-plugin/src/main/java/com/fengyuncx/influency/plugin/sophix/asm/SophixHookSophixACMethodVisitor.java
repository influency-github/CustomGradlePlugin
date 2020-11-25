//package com.fengyuncx.influency.plugin.sophix.asm;
//
//import org.objectweb.asm.Label;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;
//
//import static org.objectweb.asm.Opcodes.ALOAD;
//import static org.objectweb.asm.Opcodes.GETFIELD;
//import static org.objectweb.asm.Opcodes.INVOKESTATIC;
//
//public class SophixHookSophixACMethodVisitor extends MethodVisitor {
//    public SophixHookSophixACMethodVisitor(MethodVisitor methodVisitor) { super(Opcodes.ASM4, methodVisitor); }
//
//    @Override
//    public void visitCode() {
//        super.visitCode();
//
//    }
//
//    @Override
//    public void visitInsn(int opcode) {
//        //execute after method called.
//        Label label0 = new Label();
//        mv.visitLabel(label0);
//        mv.visitLineNumber(475, label0);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, "com/taobao/sophix/a/c", "i", "Landroid/app/Application;");
//        mv.visitMethodInsn(INVOKESTATIC, "com/fengyuncx/influency/plugin/flutter/FlutterPatch", "hook", "(Ljava/lang/Object;)V", false);
//        super.visitInsn(opcode);
//    }
//}
