package com.maple.asm_learn;

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

}