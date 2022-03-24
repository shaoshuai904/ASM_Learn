package com.maple.plugin.base

import java.util.concurrent.*

object Schedulers {

    private val cpuCount = Runtime.getRuntime().availableProcessors()
    private val IO: ExecutorService = ThreadPoolExecutor(
        0, cpuCount * 3,
        30L, TimeUnit.SECONDS, LinkedBlockingQueue()
    )
    private val COMPUTATION = Executors.newWorkStealingPool(cpuCount)

    fun IO(): Worker = Worker(IO)

    fun COMPUTATION(): Worker = Worker(COMPUTATION)

    fun FORKJOINPOOL(): ForkJoinPool = COMPUTATION as ForkJoinPool

}