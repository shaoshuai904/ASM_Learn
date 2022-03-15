package com.maple.replace.replace;

/**
 * 行代码替换
 */
public class ReplaceBean {
    private int oldOpcode;
    private String oldOwner;
    private String oldName;
    private String oldDescriptor;
    private boolean oldIsInterface;

    private int newOpcode;
    private String newOwner;
    private String newName;
    private String newDescriptor;
    private boolean newIsInterface;

    public ReplaceBean() {
    }

    public ReplaceBean(int oldOpcode, String oldOwner, String oldName, String oldDescriptor, boolean oldIsInterface) {
        this.oldOpcode = oldOpcode;
        this.oldOwner = oldOwner;
        this.oldName = oldName;
        this.oldDescriptor = oldDescriptor;
        this.oldIsInterface = oldIsInterface;
    }

    public ReplaceBean setNewCodeConfig(int newOpcode, String newOwner, String newName, String newDescriptor, boolean newIsInterface) {
        this.newOpcode = newOpcode;
        this.newOwner = newOwner;
        this.newName = newName;
        this.newDescriptor = newDescriptor;
        this.newIsInterface = newIsInterface;
        return this;
    }

    public boolean isSameOldConfig(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (owner == null || name == null || descriptor == null)
            return false;
        return (opcode == getOldOpcode()
                && owner.equals(getOldOwner())
                && name.equals(getOldName())
                && descriptor.equals(getOldDescriptor())
                && isInterface == isNewIsInterface()
        );
    }


    //------------------------

    @Override
    public String toString() {
        return "ReplaceBean{" +
                "oldOpcode=" + oldOpcode +
                ", oldOwner='" + oldOwner + '\'' +
                ", oldName='" + oldName + '\'' +
                ", oldDescriptor='" + oldDescriptor + '\'' +
                ", oldIsInterface=" + oldIsInterface +
                ", newOpcode=" + newOpcode +
                ", newOwner='" + newOwner + '\'' +
                ", newName='" + newName + '\'' +
                ", newDescriptor='" + newDescriptor + '\'' +
                ", newIsInterface=" + newIsInterface +
                '}';
    }

    public int getOldOpcode() {
        return oldOpcode;
    }

    public void setOldOpcode(int oldOpcode) {
        this.oldOpcode = oldOpcode;
    }

    public String getOldOwner() {
        return oldOwner;
    }

    public void setOldOwner(String oldOwner) {
        this.oldOwner = oldOwner;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getOldDescriptor() {
        return oldDescriptor;
    }

    public void setOldDescriptor(String oldDescriptor) {
        this.oldDescriptor = oldDescriptor;
    }

    public boolean isOldIsInterface() {
        return oldIsInterface;
    }

    public void setOldIsInterface(boolean oldIsInterface) {
        this.oldIsInterface = oldIsInterface;
    }

    public int getNewOpcode() {
        return newOpcode;
    }

    public void setNewOpcode(int newOpcode) {
        this.newOpcode = newOpcode;
    }

    public String getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(String newOwner) {
        this.newOwner = newOwner;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getNewDescriptor() {
        return newDescriptor;
    }

    public void setNewDescriptor(String newDescriptor) {
        this.newDescriptor = newDescriptor;
    }

    public boolean isNewIsInterface() {
        return newIsInterface;
    }

    public void setNewIsInterface(boolean newIsInterface) {
        this.newIsInterface = newIsInterface;
    }

}
