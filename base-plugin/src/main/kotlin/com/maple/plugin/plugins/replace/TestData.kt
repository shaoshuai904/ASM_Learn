package com.maple.plugin.plugins.replace

object TestData {

    @JvmStatic
    fun getReplaceBeans(): List<ReplaceBean> {
        return listOf(
            ReplaceBean(
                "INVOKEVIRTUAL",
                "android/content/Context",
                "sendBroadcast",
                "(Landroid/content/Intent;)V",
                false,
                "INVOKESTATIC",
                "com/maple/asm_learn/BroadcastUtils",
                "sendAppInsideBroadcast",
                "(Landroid/content/Context;Landroid/content/Intent;)V",
                false
            )
        )
    }
}