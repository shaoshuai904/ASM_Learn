package com.maple.asm_learn;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SecondActivity extends BaseActivity {
    public static final String TAG = "Second";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Log.e("MS_ASM", "SecondActivity中的onCreate");
        testMe();

        Throwable throwable = new Throwable();

        Log.i(TAG, "onCreate");
        Log.i(TAG, "onCreate", throwable);

        Log.d(TAG, "onCreate");
        Log.d(TAG, "onCreate", throwable);

        Log.v(TAG, "onCreate");
        Log.v(TAG, "onCreate", throwable);

        Log.e(TAG, "onCreate");
        Log.e(TAG, "onCreate", throwable);

        Log.w(TAG, "onCreate");
        Log.w(TAG, "onCreate", throwable);
        Log.w(TAG, throwable);
    }

    public int testMe() {
        int a = 10;
        int b = 1000 + a;
        Log.e("MS_ASM", "testMe:" + b);

        return b;
    }

    public void testSendBC(Context ctx) {
        Intent intent = new Intent();
        ctx.sendBroadcast(intent);
        Log.e("ms_app", "testSendBC");
        ctx.sendBroadcast(new Intent("com.sina.weibo.intent.action.ACTION_HOME_LIST_UPDATE"));
    }

//        BroadcastUtils.sendAppInsideBroadcast(this, intent);

    //08:18:58 E/MS_ASM: - 当前代码行数:27
    //08:18:58 E/MS_ASM: - visitTypeInsn:NEW - android/content/Intent
    //08:18:58 E/MS_ASM: - visitInsn:DUP
    //08:18:58 E/MS_ASM: - visitMethodInsn:INVOKESPECIAL - android/content/Intent - <init> - ()V - false
    //08:18:58 E/MS_ASM: - visitVarInsn:ASTORE - 2

    //08:18:58 E/MS_ASM: - 当前代码行数:28
    //08:18:58 E/MS_ASM: - visitVarInsn:ALOAD - 1
    //08:18:58 E/MS_ASM: - visitVarInsn:ALOAD - 2
    //08:18:58 E/MS_ASM: - visitMethodInsn:INVOKEVIRTUAL - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V - false

    //08:18:58 E/MS_ASM: - 当前代码行数:29
    //08:18:58 E/MS_ASM: - visitInsn:RETURN
    //08:18:58 E/MS_ASM: - visitLocalVariable:this - Lcom/gavin/asmdemo/SecondActivity; - null - 0
    //08:18:58 E/MS_ASM: - visitLocalVariable:ctx - Landroid/content/Context; - null - 1
    //08:18:58 E/MS_ASM: - visitLocalVariable:intent - Landroid/content/Intent; - null - 2
    //08:18:58 E/MS_ASM: - visitMaxs:2 - 3
    //08:18:58 E/MS_ASM: - visitEnd ~
}
