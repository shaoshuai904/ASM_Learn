package com.maple.plugin.extension

import com.maple.plugin.plugins.replace.ReplaceExtension
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

enum class RunVariant {
    DEBUG,  // debug 运行
    RELEASE,  // release 运行
    ALWAYS,  // 始终运行
    NEVER // 永不运行
}

open class LineLogExtension(
    var runVariant: RunVariant = RunVariant.ALWAYS,
    var duplcatedClassSafeMode: Boolean = false
) {

    override fun toString(): String {
        return "LineLogExtension{" +
                "runVariant=" + runVariant +
                ", duplcatedClassSafeMode=" + duplcatedClassSafeMode +
                '}'
    }
}

