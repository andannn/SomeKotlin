package jvm

import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class ThreadLocalTest {
    @Test
    fun `thread local test`() {
        val latch = CountDownLatch(1)
        thread {
            val current = TestThreadLocal.get()
            assertEquals(current, TestThreadLocal.get())
            assertEquals(current, TestThreadLocal.get())
            assertEquals(current, TestThreadLocal.get())
            latch.countDown()
        }
        latch.await()
    }
}

private object TestThreadLocal : ThreadLocal<String>() {
    override fun initialValue(): String =
        kotlin.random
            .Random(256)
            .nextInt()
            .toString()
}
