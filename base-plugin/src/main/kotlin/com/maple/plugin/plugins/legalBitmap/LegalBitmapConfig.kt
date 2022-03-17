package com.maple.plugin.plugins.legalBitmap

/**
 * @Author: leavesCZY
 * @Desc:
 */
data class LegalBitmapConfig(
    private val monitorImageViewClass: String = "github.leavesczy.asm.legalBitmap.MonitorImageView"
) {

    val formatMonitorImageViewClass: String
        get() = monitorImageViewClass.replace(".", "/")

}