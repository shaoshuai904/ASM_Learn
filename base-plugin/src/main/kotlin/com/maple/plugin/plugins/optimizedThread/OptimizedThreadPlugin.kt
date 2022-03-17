package com.maple.plugin.plugins.optimizedThread

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Author: leavesCZY
 * @Date: 2021/12/16 14:32
 * @Desc:
 */
class OptimizedThreadPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val appExtension: AppExtension = project.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(OptimizedThreadTransform(project, OptimizedThreadConfig()))
    }

}