package com.maple.plugin.task

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.DexArchiveBuilderTask
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.maple.plugin.extension.ReplaceExtension
import com.maple.plugin.plugins.replace.ReplaceTransform
import com.maple.plugin.plugins.replace.TestData
import com.maple.plugin.utils.Log
import com.maple.plugin.utils.getAndroid
import org.gradle.api.Project
import org.gradle.api.Task

class ReplaceInjection {
    lateinit var mTransform: ReplaceTransform

    fun inject(project: Project, extension: ReplaceExtension) {
        val appExtension = project.getAndroid<AppExtension>()
        val testData = TestData.getReplaceBeans()
        mTransform = ReplaceTransform(project)
        // transparentTransform = ReplaceTransform(project, extension)
        appExtension.registerTransform(mTransform)

        project.afterEvaluate {
            if (extension.isEnable) {
//                mTransform.enable = true
                // doInjection(appExtension, project, extension)
            }
        }
    }

    private fun doInjection(appExtension: AppExtension, project: Project, extension: ReplaceExtension) {
        appExtension.applicationVariants.all { variant ->
            val isTransform = (!variant.buildType.isMinifyEnabled
//                    || extension.isTransformInjectionForced
                    || getCodeShrinkerR8(project))
            if (isTransform) {
                // Inject transform
                Log.log("Using trace transform mode.")
//                mTransform.enable = true
            } else {
                // Inject task
                Log.log("Using trace task mode.")
//                taskInjection(project, extension, variant)
            }
        }
    }

    private fun getCodeShrinkerR8(project: Project): Boolean {
        return when (val property = project.properties["android.enableR8"]) {
            null -> true
            else -> (property as String).toBoolean()
        }
    }

    private fun taskInjection(project: Project, extension: ReplaceExtension, variant: BaseVariant) {
        project.afterEvaluate {
            val creationConfig = CreationConfig(variant, project)
            val action = ReplaceTask.CreationAction(creationConfig, extension)
            val traceTaskProvider = project.tasks.register(action.name, action.type, action)
            val variantName = variant.name
            val minifyTasks = arrayOf(
                BaseCreationAction.computeTaskName("minify", variantName, "WithProguard")
            )

            var minify = false
            for (taskName in minifyTasks) {
                val taskProvider = BaseCreationAction.findNamedTask(project.tasks, taskName)
                if (taskProvider != null) {
                    minify = true
                    traceTaskProvider.dependsOn(taskProvider)
                }
            }

            if (minify) {
                val dexBuilderTaskName = BaseCreationAction.computeTaskName("dexBuilder", variantName, "")
                val taskProvider = BaseCreationAction.findNamedTask(project.tasks, dexBuilderTaskName)
                taskProvider?.configure { task: Task ->
                    traceTaskProvider.get().wired(creationConfig, task as DexArchiveBuilderTask)
                }
                if (taskProvider == null) {
                    Log.log("Do not find '$dexBuilderTaskName' task. Inject matrix trace task failed.")
                }
            }
        }
    }


}
