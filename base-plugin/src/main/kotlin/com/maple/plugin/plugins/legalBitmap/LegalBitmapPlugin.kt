package com.maple.plugin.plugins.legalBitmap

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Author: leavesCZY
 * @Desc:
 */
class LegalBitmapPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val config = LegalBitmapConfig()
        val appExtension: AppExtension = project.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(LegalBitmapTransform(project, config))
    }

}