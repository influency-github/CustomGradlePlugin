# Flutter热更新插件(flutter纯净版)

​	近期做的纯flutter项目需要集成热更新，边学边做，就当练手了。经过反复尝试之后，终于调试成功做成插件，现分享一些反复试错的经验总结，以及该插件的集成和使用方法。

---

#### 实现方案

- 我们都知道，原生安卓的热更新技术自提出([nvwa]())到各大厂纷纷实现([andfix,taobao,qq,携程...]())再到如今,形成体系完备且集成简单的方案可供选择的有阿里系的([sophix]())和腾讯系的([tinker]())。这两者，前者文档更新及时易于使用，后者完全开源，都是非常优秀的产品，都支持冷启动，即时下发patch,热更新resource和.so文件。但遗憾的是他们都不支持flutter项目的热更新！

- 所幸，flutter本身是支持热更新的，虽然官方并不打算开放这一功能，但我们仔细阅读flutter的启动过程，不难发现，flutter在debugBuild模式下仅支持JIT(我称之为即时编译)启动，releaseBuild模式下支持AOT(预先编译)启动(感兴趣的可以看看这篇文章[深入理解Flutter的编译原理与优化](https://www.jianshu.com/p/325766398021))。而在AOT模式下，我们的dart代码(lib部分)会被预先编译成.so文件，在flutterEngine初始化时加载：

  ```java
    public void ensureInitializationComplete(
        @NonNull Context applicationContext, @Nullable String[] args) {
  			if (BuildConfig.DEBUG || BuildConfig.JIT_RELEASE) {
          ...
        } else {
          shellArgs.add("--" + AOT_SHARED_LIBRARY_NAME + "=" + aotSharedLibraryName);
    			...
  			}
    }
  ```

  

- 知道了原理，我们就可以利用反射调用，在flutterEngine初始化加载.so时传入我们热更新的.so路径，达到热更新flutter模块的目的。

- 因为我前期项目已经集成sophix，这里就直接选择ali的sophix作为flutter热更新媒介

- 去哪拿我们生成好的libsapp.so文件呢？解压sophix的sophix_patch.jar可以发现libsapp.so文件早就生成好了，sophix还是贴心，点赞！

- 最后一步，获取sophix_patch.jar下发到终端并解压后的存储路径！找到hotfix的本地classes.jar,找到SophixManager.class,找到queryAndCheckPatch(),查看实现类，最终找到补丁加载成功的日志"add patch ...."，反向查找，找到var4 = new ZipFile(var1);很明显了，形参var1这就是sophix_patch.jar文件路径，继续反向查找，找到其初始化的地方，hook文件路径，传给上一步flutterEngine初始化的方法。完工。

  ``` java
  private synchronized void a(File var1, PatchLoadStatusListener var2, com.taobao.sophix.c.c var3) {
          com.taobao.sophix.e.d.b("PatchManager", "loadPatch", new Object[]{"patchFile", var1.getName()});
          ZipFile var4 = null;
          long var5 = System.currentTimeMillis();
  
          try {
              if (!this.h) {
                  if (!com.taobao.sophix.e.h.a(var1)) {
                      throw new com.taobao.sophix.a.b(75, "patch signInfo not match to apk");
                  }
              } else {
                  com.taobao.sophix.e.d.d("PatchManager", "loadPatch", new Object[]{"skip verifyPatchLegal in debug mode"});
              }
  
              try {
                  var4 = new ZipFile(var1);
              } catch (IOException var24) {
                  throw new com.taobao.sophix.a.b(77, var24);
              }
              ...
          }
    ...
  }
  ```

  

---

#### 插件集成和使用

​	插件已上传到binary ，4步集成：

1. 在rootProj 的build.gradle文件中添加

   ```groovy
   buildscript {
     repositories {
   			maven { url 'https://dl.bintray.com/influency/maven'}
       }
     dependencies {
         classpath 'com.fengyuncx.influency.plugin:sophix-plugin:1.0.4'
       }
   }
   allprojects {
     repositories {
         maven { url 'https://dl.bintray.com/influency/maven'}
     }
   }
   ```

2. 在module 的build.gradle文件中添加

   ```
   apply plugin: 'sophix-plugin'
   dependencies {
   		implementation 'com.fengyuncx.influency.plugin.flutter:sophix-flutter-plugin:1.0.1'
   }
   ```

3. 在项目Application.onCreate()中调用

   ```
   FlutterMain.startInitialization(Application.this);
   ```

4. 集成Ali sophix (集成文档看这里:[稳健接入](https://help.aliyun.com/document_detail/61082.html?spm=a2c4g.11186623.6.575.4ff71cdfKzdUwL))  



5. 我的Android端flutter sdk版本：Flutter 1.17.3 Stable



​		

​	

---



## 鸣谢

##### 感谢【[带你不到80行代码搞定Flutter热更新](https://codingnote.cc/p/20258/)】提供的宝贵思路

#####  感谢 【[magicbaby810](https://github.com/magicbaby810)/**[HotfixFlutter](https://github.com/magicbaby810/HotfixFlutter)**】大神带来的逆向经验









