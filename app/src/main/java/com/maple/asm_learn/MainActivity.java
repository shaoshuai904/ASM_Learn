package com.maple.asm_learn;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.maple.asm_learn.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;

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

    public void testSendBC(Context ctx) {
//        long startTime = System.currentTimeMillis();
        Intent intent = new Intent();
        ctx.sendBroadcast(intent);
        Log.e("ms_app", "testSendBC");
        ctx.sendBroadcast(new Intent("fadf"));
        ctx.sendBroadcast(new Intent("com.sina.weibo.intent.action.ACTION_HOME_LIST_UPDATE"));
//        long countTime = System.currentTimeMillis() - startTime;
//        Log.e("MS_ASM","方法耗时："+countTime);
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