package com.fengyuncx.influency.plugin.sophix.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SophixClassVisitor extends ClassVisitor implements Opcodes {
    private static final String TAG = SophixClassVisitor.class.getSimpleName();
    boolean isDebug = false;
    private String mClassName;

    public SophixClassVisitor(ClassVisitor classVisitor) {
        this(classVisitor, false);
    }

    public SophixClassVisitor(ClassVisitor classVisitor, boolean isDebug) {
        super(Opcodes.ASM5, classVisitor);
        this.isDebug = isDebug;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        if ("com/taobao/sophix/a/c".equals(mClassName)) {
            if ("a".equals(name) && "(Ljava/lang/String;)V".equals(descriptor)) {
                if (isDebug) {
                    System.out.println(String.format("SophixClassVisitor.visitMethod: name:%s descriptor:%s signature:%s", name, descriptor, signature));
                }

                return new SophixHookPathMethodVisitor(methodVisitor, isDebug);
            }
//            else if("(Ljava/io/File;Lcom/taobao/sophix/listener/PatchLoadStatusListener;Lcom/taobao/sophix/c/c;)V".equals(descriptor)){
//                if (isDebug) {
//                    System.out.println(String.format("SophixClassVisitor.visitMethod: name:%s descriptor:%s signature:%s", name, descriptor, signature));
//                }
//                return new SophixHookSophixACMethodVisitor(methodVisitor,isDebug);
//            }
        } else if ("io/flutter/view/FlutterMain".equals(mClassName)) {
            if ("startInitialization".equals(name) && "(Landroid/content/Context;)V".equals(descriptor)) {
                if (isDebug) {
                    System.out.println(String.format("SophixClassVisitor.visitMethod: name:%s descriptor:%s signature:%s", name, descriptor, signature));
                }
                return new SophixHookFlutterMainMethodVisitor(methodVisitor, isDebug);
            }
        }

        return methodVisitor;

    }
}
