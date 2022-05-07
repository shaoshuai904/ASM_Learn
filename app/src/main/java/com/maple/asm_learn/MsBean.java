package com.maple.asm_learn;

public class MsBean {
    private String mName = "";
    private int mAge = 0;
    private int mOld = 0;

    public MsBean(String name) {
        this.mName = name;
    }

    public MsBean(String name, int age) {
        this.mName = name;
        this.mAge = age;
    }

    public MsBean(String name, int age, int old) {
        this.mName = name;
        this.mAge = age;
        this.mOld = old;
    }

    public String toString() {
        return "name: " + mName + ", age: " + mAge;
    }
}
