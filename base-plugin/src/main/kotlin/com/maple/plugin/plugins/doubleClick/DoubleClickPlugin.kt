package com.maple.plugin.plugins.doubleClick

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * ASM 字节码插桩：实现双击防抖
 * https://mp.weixin.qq.com/s/9YsVhLShIIHnjKK3PQH4Eg
 */
class DoubleClickPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val config = DoubleClickConfig()
        val appExtension: AppExtension = target.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(DoubleClickTransform(target, config))
    }

}