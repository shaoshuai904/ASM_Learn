package com.maple.asm_learn;


import android.content.Context;

import java.io.File;


/**
 * Created by qingnuan on 16/9/7.
 */
public class GifEmotionUtils {

    private static volatile GifEmotionUtils instance;

    private Context mContext;


    private GifEmotionUtils(Context context) {
        this.mContext = MsApplication.getWBApplicationContext();
    }

    public static GifEmotionUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (GifEmotionUtils.class) {
                if (instance == null) {
                    instance = new GifEmotionUtils(context);
                }
            }
        }
        return instance;
    }

    /**
     * 获取动图包文件夹路径(注册用户)
     * 780修改 开关判断存储路径
     *
     * @param pkgId 格式（../uid/business_name/pkgId）
     */
    public String getGifEmotionPkgPath(String pkgId) {
        return getGifEmotionPkgPathNewCache(pkgId);
    }

    /**
     * @param pkgId
     * @return
     */
    public String getGifEmotionPkgPathNewCache(String pkgId) {
        String filePath = "";
        return filePath;
    }

    public static boolean isAdaptationRFileEnable(){
        return true;
    }

    public static File checkAndCreateNameSpace(Context context){
        return new File("");
    }
    public static File getSDPath(){
        return new File("");
    }

}
