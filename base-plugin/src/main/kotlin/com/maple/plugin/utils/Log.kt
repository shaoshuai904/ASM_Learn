package com.maple.plugin.utils

import java.util.concurrent.Executors


object Log {

    private val logThreadExecutor = Executors.newSingleThreadExecutor()

    fun log(log: Any?) {
//        logThreadExecutor.submit {
            println("[ms_plugin]===>: $log")
//        }
    }

}