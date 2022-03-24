package com.maple.plugin.base

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.IOException

/**
 * Created by quinn on 30/08/2018
 */
class ExtendClassWriter(
    private val urlClassLoader: ClassLoader,
    flags: Int
) : ClassWriter(flags) {

    companion object {
        const val TAG = "ExtendClassWriter"
        private const val OBJECT = "java/lang/Object"
    }


    /**
     * https://github.com/Moniter123/pinpoint/blob/40106ffe6cc4d6aea9d59b4fb7324bcc009483ee/profiler/src/main/java/com/navercorp/pinpoint/profiler/instrument/ASMClassWriter.java
     */
    override fun getCommonSuperClass(type1: String?, type2: String?): String {

        if (type1 == null || type1 == OBJECT || type2 == null || type2 == OBJECT) {
            return OBJECT
        }
        if (type1 == type2) {
            return type1
        }
        val type1ClassReader = getClassReader(type1)
        val type2ClassReader = getClassReader(type2)
        if (type1ClassReader == null || type2ClassReader == null) {
            return OBJECT
        }
        //        if (isInterface(type1ClassReader)) {
        //            String interfaceName = type1;
        //            if (isImplements(interfaceName, type2ClassReader)) {
        //                return interfaceName;
        //            }
        //            if (isInterface(type2ClassReader)) {
        //                interfaceName = type2;
        //                if (isImplements(interfaceName, type1ClassReader)) {
        //                    return interfaceName;
        //                }
        //            }
        //            return OBJECT;
        //        }
        if (isInterface(type1ClassReader)) {
            var interfaceName = type1
            if (isImplements(interfaceName, type2ClassReader)) {
                return interfaceName
            }
            if (isInterface(type2ClassReader)) {
                interfaceName = type2
                if (isImplements(interfaceName, type1ClassReader)) {
                    return interfaceName
                }
            }
            return OBJECT
        }


        if (isInterface(type2ClassReader)) {
            return if (isImplements(type2, type1ClassReader)) {
                type2
            } else OBJECT
        }

        val superClassNames: MutableSet<String> = HashSet()
        superClassNames.add(type1)
        superClassNames.add(type2)
        var type1SuperClassName = type1ClassReader.superName
        if (!superClassNames.add(type1SuperClassName)) {
            return type1SuperClassName
        }
        var type2SuperClassName = type2ClassReader.superName
        if (!superClassNames.add(type2SuperClassName)) {
            return type2SuperClassName
        }

        //        while (type1SuperClassName != null || type2SuperClassName != null) {
        //            if (type1SuperClassName != null) {
        //                type1SuperClassName = getSuperClassName(type1SuperClassName);
        //                if (type1SuperClassName != null) {
        //                    if (!superClassNames.add(type1SuperClassName)) {
        //                        return type1SuperClassName;
        //                    }
        //                }
        //            }
        //
        //            if (type2SuperClassName != null) {
        //                type2SuperClassName = getSuperClassName(type2SuperClassName);
        //                if (type2SuperClassName != null) {
        //                    if (!superClassNames.add(type2SuperClassName)) {
        //                        return type2SuperClassName;
        //                    }
        //                }
        //            }
        //        }
        while (type1SuperClassName != null || type2SuperClassName != null) {
            if (type1SuperClassName != null) {
                type1SuperClassName = getSuperClassName(type1SuperClassName)!!
                if (type1SuperClassName != null) {
                    if (!superClassNames.add(type1SuperClassName)) {
                        return type1SuperClassName
                    }
                }
            }
            if (type2SuperClassName != null) {
                type2SuperClassName = getSuperClassName(type2SuperClassName)!!
                if (type2SuperClassName != null) {
                    if (!superClassNames.add(type2SuperClassName)) {
                        return type2SuperClassName
                    }
                }
            }
        }
        return OBJECT
    }

    //    private boolean isImplements(final String interfaceName, final ClassReader classReader) {
    //        ClassReader classInfo = classReader;
    //
    //        while (classInfo != null) {
    //            final String[] interfaceNames = classInfo.getInterfaces();
    //            for (String name : interfaceNames) {
    //                if (name != null && name.equals(interfaceName)) {
    //                    return true;
    //                }
    //            }
    //
    //            for (String name : interfaceNames) {
    //                if (name != null) {
    //                    final ClassReader interfaceInfo = getClassReader(name);
    //                    if (interfaceInfo != null) {
    //                        if (isImplements(interfaceName, interfaceInfo)) {
    //                            return true;
    //                        }
    //                    }
    //                }
    //            }
    //
    //            final String superClassName = classInfo.getSuperName();
    //            if (superClassName == null || superClassName.equals(OBJECT)) {
    //                break;
    //            }
    //            classInfo = getClassReader(superClassName);
    //        }
    //
    //        return false;
    //    }
    private fun isImplements(interfaceName: String, classReader: ClassReader): Boolean {
        var classInfo: ClassReader? = classReader
        while (classInfo != null) {
            val interfaceNames = classInfo.interfaces
            for (name in interfaceNames) {
                if (name != null && name == interfaceName) {
                    return true
                }
            }
            for (name in interfaceNames) {
                if (name != null) {
                    val interfaceInfo = getClassReader(name)
                    if (interfaceInfo != null) {
                        if (isImplements(interfaceName, interfaceInfo)) {
                            return true
                        }
                    }
                }
            }
            val superClassName = classInfo.superName
            if (superClassName == null || superClassName == OBJECT) {
                break
            }
            classInfo = getClassReader(superClassName)
        }
        return false
    }

    private fun isInterface(classReader: ClassReader): Boolean {
        // return (classReader.getAccess() & Opcodes.ACC_INTERFACE) != 0;
        return classReader.access and Opcodes.ACC_INTERFACE != 0
    }

    private fun getSuperClassName(className: String): String? {
        //        final ClassReader classReader = getClassReader(className);
        //        if (classReader == null) {
        //            return null;
        //        }
        //        return classReader.getSuperName();
        val classReader = getClassReader(className) ?: return null
        return classReader.superName
    }


    private fun getClassReader(className: String): ClassReader? {
        val inputStream = urlClassLoader.getResourceAsStream("$className.class")
        try {
            if (inputStream != null) {
                return ClassReader(inputStream)
            }
        } catch (ignored: IOException) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ignored: IOException) {
                }
            }
        }
        return null
    }


}