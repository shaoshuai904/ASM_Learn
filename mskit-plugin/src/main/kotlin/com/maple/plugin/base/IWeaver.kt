package com.maple.plugin.base

import java.io.IOException
import java.io.InputStream

/**
 * Created by quinn on 06/09/2018
 */
interface IWeaver {
    /**
     * 检查一个文件是否需要修改字节码 Check a certain file is weavable
     * @param filePath class路径
     * @return 是否需要修改字节码
     */
    @Throws(IOException::class)
    fun isWeavableClass(filePath: String): Boolean

    /**
     * Weave single class to byte array
     */
    @Throws(IOException::class)
    fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray?
}