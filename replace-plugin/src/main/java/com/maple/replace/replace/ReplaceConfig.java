package com.maple.replace.replace;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class ReplaceConfig {

    public static List<ReplaceBean> getReplaceBeans() {
        List<ReplaceBean> beans = new ArrayList<>();
        beans.add(new ReplaceBean(Opcodes.INVOKEVIRTUAL,
                "android/content/Context",
                "sendBroadcast",
                "(Landroid/content/Intent;)V",
                false)
                .setNewCodeConfig(Opcodes.INVOKESTATIC,
                        "com/gavin/asmdemo/BroadcastUtils",
                        "sendAppInsideBroadcast",
                        "(Landroid/content/Context;Landroid/content/Intent;)V",
                        false)
        );
        return beans;
    }

}
