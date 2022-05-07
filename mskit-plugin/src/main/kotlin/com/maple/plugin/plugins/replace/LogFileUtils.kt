package com.maple.plugin.plugins.replace

import java.lang.StringBuilder

object LogFileUtils {
    private val logMaps = mutableMapOf<String, ArrayList<String>>()

    // [ms_plugin]===>: 执行替换: com/maple/asm_learn/MainActivity : 42
    // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V
    // [ms_plugin]===>: 执行替换: com/maple/asm_learn/MainActivity : 44
    // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V
    // [ms_plugin]===>: 执行替换: com/maple/asm_learn/MainActivity : 45
    // 182 - android/content/Context - sendBroadcast - (Landroid/content/Intent;)V
    fun addLog(type: String, log: String) {
        val list = logMaps[type] ?: arrayListOf()
        list.add(log)
        logMaps[type] = list
    }

    fun toLogString(): String {
        val sb = StringBuilder()
        logMaps.forEach { (key, list) ->
            sb.append("\n替换 $key ：")
            sb.append("\n共计 ${list.size} 条：")
            list.forEach { item ->
                sb.append("\n---> $item")
            }
        }
        return sb.toString()
    }


}