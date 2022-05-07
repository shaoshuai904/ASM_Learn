package com.maple.asm_learn

import android.app.Activity
import android.content.Intent
import java.io.File

/**
 * 度量衡换算工具类
 *
 * @author : shaoshuai27
 * @date ：2020/4/3
 */
object ConversionUtils {
    private val unitList = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")

    fun aaa() {
        val file: File = File("fadb")
    }


    fun convertB(B: Long?) = convertSize(B?.toDouble(), 0)
    fun convertB(B: Double?) = convertSize(B, 0)
    fun convertKB(KB: Double?) = convertSize(KB, 1)
    fun convertMB(MB: Double?) = convertSize(MB, 2)


    /**
     * 内存大小单位换算
     *
     * @param size  大小
     * @param unit  单位
     */
    private fun convertSize(size: Double?, unit: Int): String {
        if (size == null)
            return "--"
        var curUnit = unit
        var curSize = size
        while (curSize > 1024) {
            curUnit++
            curSize /= 1024
        }
        return "${String.format("%.2f", curSize)} ${unitList[curUnit]}"
    }

    fun toSecondPage(activity: Activity) {
        val intent = Intent(activity, SecondActivity::class.java)
        activity.startActivity(intent)
    }
}
