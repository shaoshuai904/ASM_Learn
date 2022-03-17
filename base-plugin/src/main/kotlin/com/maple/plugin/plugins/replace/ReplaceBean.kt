package com.maple.plugin.plugins.replace

/**
 * 行代码替换
 */
class ReplaceBean {
    var oldOpcode = 0
    var oldOwner: String? = null
    var oldName: String? = null
    var oldDescriptor: String? = null
    var isOldIsInterface = false

    var newOpcode = 0
    var newOwner: String? = null
    var newName: String? = null
    var newDescriptor: String? = null
    var isNewIsInterface = false

    constructor() {}

    constructor(oldOpcode: Int, oldOwner: String?, oldName: String?, oldDescriptor: String?, oldIsInterface: Boolean) {
        this.oldOpcode = oldOpcode
        this.oldOwner = oldOwner
        this.oldName = oldName
        this.oldDescriptor = oldDescriptor
        isOldIsInterface = oldIsInterface
    }

    fun setNewCodeConfig(newOpcode: Int, newOwner: String?, newName: String?, newDescriptor: String?, newIsInterface: Boolean): ReplaceBean {
        this.newOpcode = newOpcode
        this.newOwner = newOwner
        this.newName = newName
        this.newDescriptor = newDescriptor
        isNewIsInterface = newIsInterface
        return this
    }

    fun isSameOldConfig(opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean): Boolean {
        return if (owner == null || name == null || descriptor == null) {
            false
        } else {
            opcode == oldOpcode
                    && owner == oldOwner
                    && name == oldName
                    && descriptor == oldDescriptor
                    && isInterface == isOldIsInterface
        }
    }

    //------------------------
    override fun toString(): String {
        return "ReplaceBean{" +
                "oldOpcode=" + oldOpcode +
                ", oldOwner='" + oldOwner + '\'' +
                ", oldName='" + oldName + '\'' +
                ", oldDescriptor='" + oldDescriptor + '\'' +
                ", oldIsInterface=" + isOldIsInterface +
                ", newOpcode=" + newOpcode +
                ", newOwner='" + newOwner + '\'' +
                ", newName='" + newName + '\'' +
                ", newDescriptor='" + newDescriptor + '\'' +
                ", newIsInterface=" + isNewIsInterface +
                '}'
    }
}