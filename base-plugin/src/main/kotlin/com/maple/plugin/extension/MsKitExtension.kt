package com.maple.plugin.extension

import org.gradle.api.Action

/**
 * 映射 build.gradle 中的配置信息
 */
open class MsKitExtension(
    var uuid: String = "",
    var replace: ReplaceExtension = ReplaceExtension(),
    var logLevel: String = "I"
) {

    /**
     * 让 replace 支持 DSL 语法
     */
    fun replace(action: Action<ReplaceExtension>) {
        action.execute(replace)
    }

    override fun toString(): String {
        return " uuid:$uuid  replace:$replace logLevel:$logLevel"
    }
}

open class ReplaceExtension(
    var isEnable: Boolean = false,
    var configFile: String? = null
) {

    override fun toString(): String {
        return "--- isEnable:$isEnable  configFile:$configFile"
    }
}