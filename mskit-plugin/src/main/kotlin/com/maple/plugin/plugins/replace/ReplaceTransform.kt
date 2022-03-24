package com.maple.plugin.plugins.replace

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.google.gson.Gson
import com.maple.plugin.base.BaseWeaver
import com.maple.plugin.base.HunterTransform
import com.maple.plugin.extension.RunVariant
import com.maple.plugin.utils.Log
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.IOException

class ReplaceTransform(project: Project) : HunterTransform(project) {
    private var replaceBeans: List<ReplaceBean>? = null

    companion object {
        var config: ReplaceExtension = ReplaceExtension()
    }

    init {
        // project.extensions.create(configTag, LineLogExtension::class.java)
        Log.log("调用 init: $config ")
        bytecodeWeaver = object : BaseWeaver() {
            override fun isWeavableClass(filePath: String): Boolean {
                return super.isWeavableClass(filePath)
                        && ("com.maple.asm_learn.LineNumberLog.class" != filePath)
            }

            override fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
                return ReplaceClassVisitor(classWriter, replaceBeans)
            }
        }
    }

    @Throws(IOException::class, TransformException::class, InterruptedException::class)
    override fun transform(invocation: TransformInvocation) {
        Log.log("调用 transform: $config")
        // config = project.extensions.getByName(configTag) as LineLogExtension
        replaceBeans = getReplaceConfig(project, config.configFile)
        bytecodeWeaver.setExtension(replaceBeans)
        super.transform(invocation)
    }

    override fun getRunVariant(): RunVariant {
        Log.log("调用 getRunVariant : ${replaceBeans?.size} ~")
        if (replaceBeans.isNullOrEmpty()) {
            return RunVariant.NEVER
        }
        return config.runVariant
    }

    private fun getReplaceConfig(project: Project, subPath: String?): List<ReplaceBean>? {
        if (subPath.isNullOrEmpty())
            return null
        // project.rootDir.absolutePath       /Users/work/GitHub/ASM_Learn
        // project.projectDir.absoluteFile    /Users/work/GitHub/ASM_Learn/app
        // project.buildDir.absolutePath      /Users/work/GitHub/ASM_Learn/app/build
        val configFile = File(project.rootDir.absolutePath, subPath)
        Log.log("configFile: ${configFile.absolutePath}")
        val jsonStr = if (configFile.exists()) {
            configFile.readText()
        } else ""
        // println("[ms_plugin]===>: configJson: $jsonStr")
        if (jsonStr.isNotEmpty()) {
            val rc: ReplaceConfigs = Gson().fromJson(jsonStr, ReplaceConfigs::class.java)
            Log.log("configs: $rc")
            return rc.configs
        }
        return null
    }

}