package com.maple.plugin.task

import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.CodeShrinker
import com.android.utils.appendCapitalized
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

interface ICreationAction<TaskT> {
    val name: String
    val type: Class<TaskT>
}

abstract class BaseCreationAction<TaskT>(
    private val creationConfig: CreationConfig
) : ICreationAction<TaskT> {

    companion object {
        @JvmField
        val computeTaskName = { prefix: String, name: String, suffix: String ->
            prefix.appendCapitalized(name, suffix)
        }

        fun findNamedTask(taskContainer: TaskContainer, name: String): TaskProvider<Task>? {
            try {
                return taskContainer.named(name)
            } catch (t: Throwable) {
            } finally {
            }
            return null
        }
    }

    fun computeTaskName(prefix: String, suffix: String): String =
        computeTaskName(prefix, creationConfig.variant.name, suffix)
}

class CreationConfig(
    val variant: BaseVariant,
    val project: Project
) {
    companion object {

        fun getCodeShrinker(project: Project): CodeShrinker {

            var enableR8: Boolean = when (val property = project.properties["android.enableR8"]) {
                null -> true
                else -> (property as String).toBoolean()
            }

            return when {
                enableR8 -> CodeShrinker.R8
                else -> CodeShrinker.PROGUARD
            }
        }
    }
}