package com.maple.asm_learn;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastUtils {

    public static void sendAppInsideBroadcast(Context ctx, Intent intent) {
        Log.e("ms_app","BroadcastUtils-sendAppInsideBroadcast");
//        intent.setPackage("com.sina.weibo");
//        ctx.sendBroadcast(intent);
    }

}
