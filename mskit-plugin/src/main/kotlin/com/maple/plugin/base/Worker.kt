package com.maple.plugin.base

import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.function.Consumer

class Worker(var executor: ExecutorService) {

    private val futures: LinkedList<Future<*>> = LinkedList()

    fun execute(runnable: Runnable) {
        futures.add(executor.submit(runnable))
    }

    fun <T> submit(callable: Callable<T>): Future<T> {
        val future = executor.submit(callable)
        futures.add(future)
        return future
    }

    @Throws(Exception::class)
    fun await() {
        var future: Future<*>? = null
        while (futures.pollFirst()?.also { future = it } != null) {
            try {
                future?.get()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    @Throws(IOException::class)
    fun <I> submitAndAwait(coll: Collection<I>, consumer: Consumer<I>) {
        coll.stream().map { f: I ->
            Runnable { consumer.accept(f) }
        }.forEach { runnable: Runnable ->
            execute(runnable)
        }
        await()
    }
}