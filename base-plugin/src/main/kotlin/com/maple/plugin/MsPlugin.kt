package com.maple.plugin

import com.android.build.gradle.AppExtension
import com.maple.plugin.plugins.replace.ReplaceTransform
import com.maple.plugin.utils.getAndroid
import com.maple.plugin.utils.getProperty
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin 入口
 */
class MsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        println("[ms_plugin]===>: MsPlugin apply~")
        if (isReleaseTask(project)) {
            return
        }
        if (project.plugins.hasPlugin("com.android.application")
            || project.plugins.hasPlugin("com.android.dynamic-feature")
        ) {
            project.getAndroid<AppExtension>().let { androidExt ->
                val pluginSwitch = project.getProperty("enablePlugin", false)
                println("[ms_plugin]===>: pluginSwitch: $pluginSwitch")
                androidExt.registerTransform(ReplaceTransform(project))
            }
        } else if (project.plugins.hasPlugin("com.android.library")) {
            println("[ms_plugin]===>: 当前是 library")
        } else {
            println("[ms_plugin]===>: 未知~")
        }
    }

    // :app:assembleDebug
    private fun isReleaseTask(project: Project): Boolean {
        return project.gradle.startParameter.taskNames.any {
            it.contains("release") || it.contains("Release")
        }
    }

}