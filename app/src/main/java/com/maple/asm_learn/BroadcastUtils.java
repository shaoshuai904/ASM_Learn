package com.maple.asm_learn;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;

public class BroadcastUtils {

    public static void sendAppInsideBroadcast(Context ctx, Intent intent) {
        Log.e("ms_app", "BroadcastUtils-sendAppInsideBroadcast");
//        intent.setPackage("com.sina.weibo");
//        ctx.sendBroadcast(intent);
    }


    @SuppressLint("WrongConstant")
    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        Log.e("ms_app", "BroadcastUtils-sendAppInsideBroadcast");
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            final int FLAG_MUTABLE = 1 << 25;// PendingIntent.FLAG_MUTABLE
            flags = flags | FLAG_MUTABLE;
        }
        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }

    public static File createFile(String name) {
        File file = new File("new_" + name);
        Log.e("ms_app", "createFile " + file.getName());
        return file;
    }

    public static File createFile(File f, String name) {
        File file = new File(f, "new_" + name);
        Log.e("ms_app", "createFile " + file.getName());
        return file;
    }

    public static MsBean createMsBean(String name, int age) {
        MsBean msa = new MsBean("lisi" + name, 2333);
        Log.e("ms_app", "createMsBean " + msa.toString());
        return msa;
    }

}
