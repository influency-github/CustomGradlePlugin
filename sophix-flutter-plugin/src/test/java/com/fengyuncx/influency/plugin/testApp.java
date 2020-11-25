package com.fengyuncx.influency.plugin;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.fengyuncx.influency.plugin.flutter.FlutterPatch;

import org.junit.Test;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.engine.loader.FlutterLoader;


public class testApp {
    static boolean isRunningInRobolectricTest;
    public static void startInitialization(@NonNull Context applicationContext) {
        if (isRunningInRobolectricTest) {
            FlutterPatch.hook(applicationContext);
        } else {
            FlutterLoader.getInstance().startInitialization(applicationContext);
            FlutterPatch.hook(applicationContext);
        }
    }

}
