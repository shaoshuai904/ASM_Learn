package com.maple.plugin.plugins.replace

import com.android.build.api.transform.TransformInvocation
import com.google.gson.Gson
import com.maple.plugin.BaseTransform
import com.maple.plugin.extension.ReplaceExtension
import com.maple.plugin.utils.Log
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File

class ReplaceTransform(project: Project) : BaseTransform(project) {
    private var replaceBeans: List<ReplaceBean>? = null

    companion object {
        var config: ReplaceExtension = ReplaceExtension()
    }

    override fun transform(invocation: TransformInvocation) {
        replaceBeans = getReplaceConfig(project, config.configFile)
        Log.log("transform :::::  ${replaceBeans?.size} ~")
        if (config.isEnable && !replaceBeans.isNullOrEmpty()) {
            super.transform(invocation)
        }
    }

    override fun modifyClass(byteArray: ByteArray): ByteArray {
        val classReader = ClassReader(byteArray)
        val className = classReader.className
        if ("com/maple/asm_learn/MainActivity" == className) {
            val cw = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val visitor = ReplaceClassVisitor(cw, replaceBeans!!)
            classReader.accept(visitor, ClassReader.EXPAND_FRAMES)
            return cw.toByteArray()
        }
        return byteArray
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