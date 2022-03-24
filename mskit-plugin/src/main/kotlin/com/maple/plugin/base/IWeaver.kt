package com.maple.plugin.base

import java.io.IOException
import java.io.InputStream

/**
 * Created by quinn on 06/09/2018
 */
interface IWeaver {
    /**
     * Check a certain file is weavable
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

// public interface IWeaver {
//
//    /**
//     * Check a certain file is weavable
//     * @param filePath class路径
//     * @return 是否需要修改字节码
//     */
//    public boolean isWeavableClass(String filePath) throws IOException;
//
//    /**
//     * Weave single class to byte array
//     */
//    public byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException;
//
//
//}