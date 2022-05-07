package com.maple.plugin

import com.android.build.gradle.AppExtension
import com.maple.plugin.extension.MsKitExtension
import com.maple.plugin.plugins.replace.ReplaceTransform
import com.maple.plugin.utils.Log
import com.maple.plugin.utils.getAndroid
import com.maple.plugin.utils.isReleaseTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin 入口
 */
class MsKitPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        Log.log("MsKitPlugin apply~")
        if (project.isReleaseTask()) {
            return
        }

        // 获取 gradle.properties 文件中的配置信息
        // val asmOpen = project.getProperty("enableASMPlugin", false)
        // val replaceConfigFile = project.getProperty("asmReplaceConfigFile", "")
        // Log.log("getProperty: $asmOpen  $replaceConfigFile ")

        // 获取 build.gradle 文件中的配置信息
        val extension = project.extensions.create("msKitExt", MsKitExtension::class.java)
        project.afterEvaluate {
            Log.log("调用 afterEvaluate: $extension")
            ReplaceTransform.config = extension.replace
        }

        registerTransform(project)
    }

    private fun registerTransform(project: Project) {
        if (project.plugins.hasPlugin("com.android.application")
            || project.plugins.hasPlugin("com.android.dynamic-feature")
        ) {
            project.getAndroid<AppExtension>().let { androidExt ->
                // registerTransform
                androidExt.registerTransform(ReplaceTransform(project))
                // androidExt.registerTransform(LineLogTransform(project))
            }
        } else if (project.plugins.hasPlugin("com.android.library")) {
            Log.log("当前是 library")
        } else {
            Log.log("未知~")
        }
    }


}