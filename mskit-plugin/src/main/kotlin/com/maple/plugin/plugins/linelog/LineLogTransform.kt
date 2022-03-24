package com.maple.plugin.plugins.linelog

import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.maple.plugin.base.BaseWeaver
import com.maple.plugin.base.HunterTransform
import com.maple.plugin.extension.LineLogExtension
import com.maple.plugin.extension.RunVariant
import com.maple.plugin.utils.Log
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.IOException

/**
 * Created by Quinn on 15/09/2018.
 */
class LineLogTransform(project: Project) : HunterTransform(project) {
    private val configTag: String = "linelogHunterExt"
    private var config: LineLogExtension = LineLogExtension()

    init {
        project.extensions.create(configTag, LineLogExtension::class.java)
        bytecodeWeaver = object : BaseWeaver() {
            override fun isWeavableClass(filePath: String): Boolean {
                val superResult = super.isWeavableClass(filePath)
                Log.log("isWeavableClass: $filePath")
                val isByteCodePlugin = "com.maple.asm_learn.LineNumberLog.class" != filePath
                return superResult && isByteCodePlugin
            }

            override fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
                return LineLogClassAdapter(classWriter)
            }
        }
    }

    @Throws(IOException::class, TransformException::class, InterruptedException::class)
    override fun transform(invocation: TransformInvocation) {
        config = project.extensions.getByName(configTag) as LineLogExtension
        super.transform(invocation)
    }

    override fun getRunVariant(): RunVariant {
        return config.runVariant
    }

}