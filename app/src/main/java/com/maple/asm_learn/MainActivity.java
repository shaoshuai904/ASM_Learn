package com.maple.asm_learn;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.maple.asm_learn.databinding.ActivityMainBinding;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;

//    private static final String SAVE_REAL_PATH = new File(GifEmotionUtils.isAdaptationRFileEnable() ?
//            GifEmotionUtils.checkAndCreateNameSpace(MsApplication.instance) :
//            GifEmotionUtils.getSDPath(), "LONG_PIC_SHARE_SUB_DIR").getAbsolutePath();


//    public MainActivity() {
//    }
//
//    public MainActivity(int a) {
//        Log.e("ms_app", "fdsfasas" + a);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testSendBC(getBaseContext());
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

//    public File getTempDirForSDcard(Context context) {
//        File dirtyFile = new File(String.format(Locale.US, "%s/%s_%020d%s", "logDir", "placeholderPrefix", new Date().getTime() * 1000 + getNextUnique(), "placeholderDirtySuffix"));
//        Log.e("ms_app", "testFile" + dirtyFile.getName());
//
//
//        // -----------------
//        File var10000;
//        File var4;
//        var10000 = var4 = new File("/shared_prefs/push_notify_flag.xml");
//        if (var10000.exists()) {
//            Log.e("ms_app", "testFile" + var4.getName());
//        }
//        return new File("fasd");
//    }
//
//    private int getNextUnique() {
//        return 99;
//    }

    public File renameTo(File file, File file2) {
        File fff =
                new File(
                        new File(
                                new File(getBasePath() + "fasdffadf"),
                                getBasePath() + "abcdef" + 5),
                        getBasePath() + "fasd"
                );
//        Log.e("ms_app", "testFile" + fff.getName());
//        return fff;
        return new File("fasd");
    }

    public String getBasePath() {
        return "base_";
    }

    public void testFile(File f) {
//        renameTo(new File(GifEmotionUtils.getInstance(MsApplication.instance).getGifEmotionPkgPath("temp_file")),
//                new File(GifEmotionUtils.getInstance(MsApplication.instance).getGifEmotionPkgPath("pkgid")));
//
//        renameTo(new File("fasdfa"),
//                new File("fadsfadsgadfdasfsdaf"));
//
//
//        File parent = new File(getTempDirForSDcard(MsApplication.getWBApplicationContext()) + "/" + Environment
//                .DIRECTORY_DCIM + "/weibo");
//
//        BroadcastUtils.createFile("fa");
//        MsBean aa = BroadcastUtils.createMsBean("", 423);

        new MsBean("zhangsan");
        MsBean msa = new MsBean(
                "lisi",
                2333
        );
        new MsBean(
                "wangwu",
                654,
                312
        );

        String test = "fabdefasdfasd.html";
        String name = "cuxtomgmedage123.xml";
        File file = new File(name);
        Log.e("ms_app", "testFile" + file.getName());
    }

    String var8 = "/system/framework/";
    String var1 = "hwpush.jar";

    public void testSendBC(Context ctx) {
        File var2;
        try {
            var2 = new File(var8 + var1);
            Log.e("ms_app", "var2 " + var2.getName());
        } catch (Exception e) {

        }

        testFile(new File("abddef"));

//        long startTime = System.currentTimeMillis();
        Intent intent = new Intent("abcdef");
        BroadcastUtils.sendAppInsideBroadcast(ctx, intent);
        Log.e("ms_app", "testSendBC");
        ctx.sendBroadcast(new Intent("fadf"));
        ctx.sendBroadcast(new Intent("com.sina.weibo.intent.action.ACTION_HOME_LIST_UPDATE"));
//        long countTime = System.currentTimeMillis() - startTime;
//        Log.e("MS_ASM","方法耗时："+countTime);
    }

    public static boolean createDirs(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    public void testPadding(Context ctx) {
        Intent intent = new Intent();
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
//        if (android.os.Build.VERSION.SDK_INT >= 31) {
//            final int FLAG_MUTABLE = 1 << 25;// PendingIntent.FLAG_MUTABLE
//            flags = flags | FLAG_MUTABLE;
//        }

        PendingIntent appIntent = PendingIntent.getActivity(ctx, 0, intent, flags);

        PendingIntent pendintent = PendingIntent.getService(ctx, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, flags);
    }

}