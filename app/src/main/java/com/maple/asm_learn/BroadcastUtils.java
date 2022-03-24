package com.maple.asm_learn;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

}
