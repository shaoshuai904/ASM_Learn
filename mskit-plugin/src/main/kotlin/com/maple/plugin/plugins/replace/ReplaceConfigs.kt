package com.maple.plugin.plugins.replace

import com.maple.plugin.extension.RunVariant
import org.objectweb.asm.Opcodes
import java.io.Serializable

/**
 * 映射 build.gradle 中的配置信息
 */
open class ReplaceExtension(
    var runVariant: RunVariant = RunVariant.ALWAYS,
    var isEnable: Boolean = false,
    var configFile: String? = null
) {
    override fun toString(): String {
        return "--- runVariant: $runVariant  isEnable:$isEnable  configFile:$configFile"
    }
}

data class ReplaceConfigs(
    var configs: List<ReplaceBean>
) : Serializable {
    var version: Int = 1

    fun getEnableConfigs() = configs.filter { it.enable }
}

/**
 * 行代码替换
 */
data class ReplaceBean(
    var oldOpcode: String?,
    var oldOwner: String?,
    var oldName: String?,
    var oldDescriptor: String?,
    var oldIsInterface: Boolean,
    var newOpcode: String?,
    var newOwner: String?,
    var newName: String?,
    var newDescriptor: String?,
    var newIsInterface: Boolean,
    var enable: Boolean
) : Serializable {

    fun isSameOldConfig(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean): Boolean {
        return if (owner == null || name == null || descriptor == null) {
            false
        } else {
            opcode == convertOpcode(oldOpcode)
                    && owner == oldOwner
                    && name == oldName
                    && descriptor == oldDescriptor
                    && isInterface == oldIsInterface
        }
    }

    fun getOldOpcodeInt() = convertOpcode(oldOpcode)
    fun getNewOpcodeInt() = convertOpcode(newOpcode)

    fun convertOpcode(code: String?): Int = when (code) {
        "INVOKEVIRTUAL", "virtual" -> Opcodes.INVOKEVIRTUAL // visitMethodInsn
        "INVOKESPECIAL", "special" -> Opcodes.INVOKESPECIAL
        "INVOKESTATIC", "static" -> Opcodes.INVOKESTATIC
        "INVOKEINTERFACE", "interface" -> Opcodes.INVOKEINTERFACE
        "INVOKEDYNAMIC", "dynamic" -> Opcodes.INVOKEDYNAMIC // visitInvokeDynamicInsn
        else -> -1
    }

    override fun toString(): String {
        return "ReplaceBean {" +
                "\n[Old]: $oldOpcode $oldOwner $oldName $oldDescriptor $oldIsInterface" +
                "\n[New]: $newOpcode $newOwner $newName $newDescriptor $newIsInterface" +
                "\n}"
    }


}