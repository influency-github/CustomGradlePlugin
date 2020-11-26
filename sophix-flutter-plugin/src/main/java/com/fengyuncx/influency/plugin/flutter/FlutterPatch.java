package com.fengyuncx.influency.plugin.flutter;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;


/**
 * reflect FlutterLoader.aotSharedLibraryName method in android  releaseMode build
 * warning: debugMode is not supported
 */
public class FlutterPatch {

    private static final String TAG = "FlutterPatch";
    private static String libPathFromSophix = "";

    private static boolean isUseSophix = false;
//    private static boolean isUseTinker = false;


    private FlutterPatch() {
    }

//    public static String getLibPath(Context context) {
//        String libPath = findLibraryFromTinker(context, "lib" + File.separator + getCpuABI(), "libapp.so");
//        if (!TextUtils.isEmpty(libPath) && libPath.equals("libapp.so")) {
//            return null;
//        }
//        return libPath;
//    }


    public static void reflect(String libPath) {
        try {
            io.flutter.embedding.engine.loader.FlutterLoader flutterLoader = io.flutter.embedding.engine.loader.FlutterLoader.getInstance();

            Field field = io.flutter.embedding.engine.loader.FlutterLoader.class.getDeclaredField("aotSharedLibraryName");
            field.setAccessible(true);
            field.set(flutterLoader, libPath);
            Log.e(TAG, "flutter patch is loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * #Do not correct this function
     * @param obj
     */
    public static void hook(Object obj) {
        if (obj instanceof Context) {

            Context context = (Context) obj;
            Log.e(TAG, "FlutterMain ");

//            if (isUseTinker) {
//
//                String libPathFromTinker = getLibPath(context);
//                if (!TextUtils.isEmpty(libPathFromTinker)) {
//                    reflect(libPathFromTinker);
//                }
//            } else
            if (isUseSophix) {
                if (!TextUtils.isEmpty(libPathFromSophix)) {
                    reflect(libPathFromSophix);
                }else{
                    Log.e(TAG, "lib path is null");
                }
            }

        } else {

            Log.e(TAG, "Object: " + obj.getClass().getName());
        }

    }

    /**
     * #Do not correct this function
     * get libsapp.so absolute path
     * @param obj
     */
    public static void hookSophix(Object obj) {
        if (null != obj) {
            Log.e(TAG,"obj path = "+obj.toString());
            File sophixDir = new File(obj.toString());
            if(sophixDir.exists()){
                printFileName(sophixDir.getAbsolutePath(),0);
            }
            File file = new File(obj.toString() + "/libs/libapp.so");
            if (file.exists() && !file.isDirectory()) {
                libPathFromSophix = file.getAbsolutePath();
                Log.e(TAG, "path is " + libPathFromSophix);
            } else {
                Log.e(TAG, "path file is not exist");
            }
        }
    }
    public static void printFileName(String filePath,int level){
        File rootFile = new File(filePath);
        if(rootFile.exists()){
            logFileName(level, rootFile);
            if(rootFile.isDirectory()){
                level++;
                File[] files = rootFile.listFiles();
                if(files == null){
                    return;
                }
                for (File childF:files) {
                    if(childF!=null){
                        printFileName(childF.getPath(),level);
                    }
                }
            }
        }
    }

    private static void logFileName(int level, File rootFile) {
        StringBuffer spaceBuffer = new StringBuffer("");
        while (level > 0) {
            spaceBuffer.append("  ");
            level--;
        }
        Log.e(TAG,String.format("%1$s%2$s %3$s", spaceBuffer.toString(), rootFile.getName(),rootFile.isDirectory()?"d":"f"));
    }

    /**
     * now not in use
     */
    public static void hookIsUseSophix() {
        isUseSophix = true;
        Log.e(TAG, "is use sophix");
    }

    /**
     * use in tinker
     * <p>
     */
//    public static void hookIsUseTinker() {
//        isUseTinker = true;
//        Log.i(TAG, "is use tinker");
//    }


//    public static String findLibraryFromTinker(Context context, String relativePath, String libName) throws UnsatisfiedLinkError {
//        final Tinker tinker = Tinker.with(context);
//
//        libName = libName.startsWith("lib") ? libName : "lib" + libName;
//        libName = libName.endsWith(".so") ? libName : libName + ".so";
//        String relativeLibPath = relativePath + File.separator + libName;
//
//        TinkerLog.i(TAG, "flutterPatchInit() called   " + tinker.isTinkerLoaded() + " " + tinker.isEnabledForNativeLib());
//
//        if (tinker.isEnabledForNativeLib() && tinker.isTinkerLoaded()) {
//            TinkerLoadResult loadResult = tinker.getTinkerLoadResultIfPresent();
//            if (loadResult.libs == null) {
//                return libName;
//            }
//            for (String name : loadResult.libs.keySet()) {
//                if (!name.equals(relativeLibPath)) {
//                    continue;
//                }
//                String patchLibraryPath = loadResult.libraryDirectory + "/" + name;
//                File library = new File(patchLibraryPath);
//                if (!library.exists()) {
//                    continue;
//                }
//
//                boolean verifyMd5 = tinker.isTinkerLoadVerify();
//                if (verifyMd5 && !SharePatchFileUtil.verifyFileMd5(library, loadResult.libs.get(name))) {
//                    tinker.getLoadReporter().onLoadFileMd5Mismatch(library, ShareConstants.TYPE_LIBRARY);
//                } else {
//                    TinkerLog.i(TAG, "findLibraryFromTinker success:" + patchLibraryPath);
//                    return patchLibraryPath;
//                }
//            }
//        }
//
//        return libName;
//    }

    /**
     * @return
     */
    public static String getCpuABI() {

        if (Build.VERSION.SDK_INT >= 21) {
            for (String cpu : Build.SUPPORTED_ABIS) {
                if (!TextUtils.isEmpty(cpu)) {
                    Log.e(TAG, "cpu abi is:" + cpu);
                    return cpu;
                }
            }
        } else {
            Log.e(TAG, "cpu abi is:" + Build.CPU_ABI);
            return Build.CPU_ABI;
        }

        return "";
    }
}