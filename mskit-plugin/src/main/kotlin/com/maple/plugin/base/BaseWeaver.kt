package com.maple.plugin.base

import com.maple.plugin.utils.Log
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.*
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Created by quinn on 07/09/2018
 *
 */
open class BaseWeaver : IWeaver {
    protected lateinit var mClassLoader: ClassLoader

    open fun setClassLoader(classLoader: ClassLoader) {
        this.mClassLoader = classLoader
    }

    open fun setExtension(extension: Any?) {}

    companion object {
        val ZERO = FileTime.fromMillis(0)
    }

    open fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
        return classWriter
    }

    @Throws(IOException::class)
    override fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter: ClassWriter = ExtendClassWriter(mClassLoader, ClassWriter.COMPUTE_MAXS)
        val classWriterWrapper = wrapClassWriter(classWriter)
        classReader.accept(classWriterWrapper, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    /**
     * 是否需要处理文件
     */
    override fun isWeavableClass(filePath: String): Boolean {
        return (filePath.endsWith(".class")
                && !filePath.contains("R$")
                && !filePath.contains("R.class")
                && !filePath.contains("BuildConfig.class"))
    }

    @Throws(IOException::class)
    fun weaveJar(inputJar: File?, outputJar: File) {
        val inputZip = ZipFile(inputJar)
        val outputZip = ZipOutputStream(BufferedOutputStream(Files.newOutputStream(outputJar.toPath())))
        val inEntries = inputZip.entries()
        while (inEntries.hasMoreElements()) {
            val entry = inEntries.nextElement()
            val originalFile: InputStream = BufferedInputStream(inputZip.getInputStream(entry))
            val outEntry = ZipEntry(entry.name)
            // separator of entry name is always '/', even in windows
            val newEntryContent: ByteArray =
                if (isWeavableClass(outEntry.name.replace("/", "."))) {
                    weaveSingleClassToByteArray(originalFile)
                } else {
                    IOUtils.toByteArray(originalFile)
                }
            val crc32 = CRC32()
            crc32.update(newEntryContent)
            outEntry.crc = crc32.value
            outEntry.method = ZipEntry.STORED
            outEntry.size = newEntryContent.size.toLong()
            outEntry.compressedSize = newEntryContent.size.toLong()
            outEntry.lastAccessTime = ZERO
            outEntry.lastModifiedTime = ZERO
            outEntry.creationTime = ZERO
            outputZip.putNextEntry(outEntry)
            outputZip.write(newEntryContent)
            outputZip.closeEntry()
        }
        outputZip.flush()
        outputZip.close()
        inputZip.close()
    }

    @Throws(IOException::class)
    fun weaveSingleClassToFile(inputFile: File, outputFile: File?, inputDir: String) {
        val inputBaseDir = if (!inputDir.endsWith(File.separator)) {
            inputDir + File.separator
        } else {
            inputDir
        }
        val fileName = inputFile.absolutePath
            .replace(inputBaseDir, "")
            .replace(File.separator, ".")
        if (isWeavableClass(fileName)) {
            Log.log("检查: $fileName")
            FileUtils.touch(outputFile)
            val inputStream: InputStream = FileInputStream(inputFile)
            val bytes = weaveSingleClassToByteArray(inputStream)
            val fos = FileOutputStream(outputFile)
            fos.write(bytes)
            fos.close()
            inputStream.close()
        } else {
            Log.log("跳过: $fileName")
            if (inputFile.isFile) {
                FileUtils.touch(outputFile)
                FileUtils.copyFile(inputFile, outputFile)
            }
        }
    }

}