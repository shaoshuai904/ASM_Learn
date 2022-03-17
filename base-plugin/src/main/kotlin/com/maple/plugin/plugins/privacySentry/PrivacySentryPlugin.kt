package com.maple.plugin.plugins.privacySentry

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @Author: leavesCZY
 * @Date: 2021/12/21 22:57
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
class PrivacySentryPlugin : Plugin<Project> {

    companion object {
        private const val EXT_NAME = "PrivacySentry"
    }

    override fun apply(project: Project) {
        project.extensions.create<PrivacySentryGradleConfig>(EXT_NAME, PrivacySentryGradleConfig::class.java)
        project.afterEvaluate {
            val config = (it.extensions.findByName(EXT_NAME) as? PrivacySentryGradleConfig) ?: PrivacySentryGradleConfig()
            config.transform()
        }
        val appExtension: AppExtension = project.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(
            PrivacySentryTransform(project, PrivacySentryConfig())
        )
    }

}