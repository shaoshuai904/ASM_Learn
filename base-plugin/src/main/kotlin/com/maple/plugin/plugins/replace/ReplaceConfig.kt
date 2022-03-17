package com.maple.plugin.plugins.replace

import org.objectweb.asm.Opcodes

object ReplaceConfig {

    @JvmStatic
    fun getReplaceBeans(): List<ReplaceBean> {
        return listOf(
            ReplaceBean(
                Opcodes.INVOKEVIRTUAL,
                "android/content/Context",
                "sendBroadcast",
                "(Landroid/content/Intent;)V",
                false
            ).setNewCodeConfig(
                Opcodes.INVOKESTATIC,
                "com/maple/asm_learn/BroadcastUtils",
                "sendAppInsideBroadcast",
                "(Landroid/content/Context;Landroid/content/Intent;)V",
                false
            )
        )
    }
}