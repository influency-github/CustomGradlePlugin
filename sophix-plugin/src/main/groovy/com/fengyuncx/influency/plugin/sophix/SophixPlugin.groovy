package com.fengyuncx.influency.plugin.sophix

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.fengyuncx.influency.plugin.sophix.asm.SophixClassVisitor
import com.fengyuncx.influency.plugin.sophix.extension.InfluencySophixExtension
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class SophixPlugin extends Transform implements Plugin<Project> {
    boolean isDebug = true
    String hotfixPluginName = "sophix"

    @Override
    String getName() {
        return getClass().getName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    Project mProject

    @Override
    void apply(Project project) {
        mProject = project;
        // create gradle config first:
        project.extensions.create("influencySophix", InfluencySophixExtension)

        com.android.build.gradle.AppExtension android = project.extensions.getByType(com.android.build.gradle.AppExtension)
        android.registerTransform(this)

        project.afterEvaluate {
            //load gradle config second:
            println('#############load influencySophix success#########')
            isDebug = project.extensions.influencySophix.isDebug
            hotfixPluginName = project.extensions.influencySophix.hotfixPluginName
            if (isDebug) {
                println("influencySophix debug ${isDebug ? "on" : "off"}. hotfixPluginName: ${hotfixPluginName}")
            }
        }
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if (isDebug) {
            println("---------SophixPlugin visit start------------------")
        }
        def startTime = System.currentTimeMillis()
        Collection<TransformInput> inputs = transformInvocation.inputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        //delete last output code third:
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }
        inputs.each { TransformInput transformInput ->
            //handle class file in dir fourth:
            transformInput.directoryInputs.each { DirectoryInput dirInput ->
                handleDirInput(dirInput, outputProvider)
            }
            //handle class file in jar fifth:
            transformInput.jarInputs.each { JarInput jarInput ->
                handleJarInput(jarInput, outputProvider)
            }
        }
        if (isDebug) {
            println("SophixPlugin cost: ${(System.currentTimeMillis() - startTime) / 1000}s")
            println("---------SophixPlugin visit end---------------------")
        }
    }

    void handleDirInput(DirectoryInput dirInput, TransformOutputProvider outputProvider) {

        if (dirInput.file.isDirectory()) {
            dirInput.file.eachFileRecurse { File file ->
                if (checkFileNamePass(file.getCanonicalPath())) {
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor classVisitor = new SophixClassVisitor(classWriter, isDebug)
                    classReader.accept(classVisitor, ClassWriter.COMPUTE_FRAMES)

                    byte[] code = classWriter.toByteArray()
                    FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + file.name)
                    fos.write(code)
                    fos.close()
                }
            }
        }

        def dest = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
        FileUtils.copyDirectory(dirInput.file, dest)

    }

    public void handleJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        //##to resolve the hotfix classes.jar file from group:articleId = com.aliyun.ams:alicloud-android-hotfix
        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tempFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            if (tempFile.exists()) {
                tempFile.delete()
            }
            if (!tempFile.exists()) {
                tempFile.createNewFile()
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempFile))
            enumeration.each { JarEntry jarEntry ->
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(zipEntry)
                if (checkFileNamePass(jarEntry.getName())) {
                    jarOutputStream.putNextEntry(zipEntry)
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new SophixClassVisitor(classWriter, isDebug)
                    classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    jarOutputStream.write(code)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
            }
            jarOutputStream.close()
            jarFile.close()

            def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(tempFile, dest)
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete()
            }

        }

    }

    boolean checkFileNamePass(String fileName) {
        if (!fileName.startsWith("R\$")) {
            if (fileName.endsWith(".class") && fileName.startsWith("com/taobao/sophix/a/c") && fileName.equals("com/taobao/sophix/a/c.class")) {
                if (isDebug) {
                    println("SophixPlugin: deal with jar class file path: $fileName")
                }
                return true
            } else if ((fileName.endsWith("FlutterMain.java") || fileName.endsWith("FlutterMain.class")) && fileName.startsWith("io/flutter/view/FlutterMain")) {
                if (isDebug) {
                    println("SophixPlugin: deal with java file path: $fileName")
                }
                return true
            }
        }
        return false
    }


}