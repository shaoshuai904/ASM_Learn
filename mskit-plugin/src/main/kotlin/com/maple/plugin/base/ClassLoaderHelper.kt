package com.maple.plugin.base

import com.android.build.api.transform.TransformInput
import com.android.build.gradle.AppExtension
import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterables
import org.gradle.api.Project
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader

/**
 * Created by quinn on 31/08/2018
 */
object ClassLoaderHelper {

    @Throws(MalformedURLException::class)
    fun getClassLoader(
        inputs: Collection<TransformInput>?,
        referencedInputs: Collection<TransformInput>?,
        project: Project
    ): URLClassLoader {
        val urls = ImmutableList.Builder<URL>()
        val file = File(getAndroidJarPath(project))
        val androidJarURL = file.toURI().toURL()
        urls.add(androidJarURL)
        for (totalInputs in Iterables.concat(inputs, referencedInputs)) {
            for (directoryInput in totalInputs.directoryInputs) {
                if (directoryInput.file.isDirectory) {
                    urls.add(directoryInput.file.toURI().toURL())
                }
            }
            for (jarInput in totalInputs.jarInputs) {
                if (jarInput.file.isFile) {
                    urls.add(jarInput.file.toURI().toURL())
                }
            }
        }
        val classLoaderUrls = urls.build().toTypedArray()
        return URLClassLoader(classLoaderUrls)
    }

    /**
     * /Users/quinn/Documents/Android/SDK/platforms/android-28/android.jar
     */
    private fun getAndroidJarPath(project: Project): String {
        val appExtension = project.properties["android"] as AppExtension
        val sdkDirectory = appExtension.sdkDirectory.absolutePath + File.separator + "platforms" + File.separator
        return sdkDirectory + appExtension.compileSdkVersion + File.separator + "android.jar"
    }
}