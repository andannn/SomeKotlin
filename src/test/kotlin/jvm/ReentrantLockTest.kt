package jvm

import kotlinx.coroutines.delay
import java.lang.Thread.sleep
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class ExecutionOrderTest {
    private var currentCount: Int = 0

    @BeforeTest
    fun reset() {
        currentCount = 0
    }

    fun expect(num: Int) {
        assertEquals(num, currentCount)
        currentCount++
    }
}

class ReentrantLockTest : ExecutionOrderTest() {
    @Test
    fun `reentrant lock hold release count`() {
        val lock = ReentrantLock()

        assertEquals(false, lock.isHeldByCurrentThread)
        assertEquals(0, lock.holdCount)
        lock.lock()
        assertEquals(true, lock.isHeldByCurrentThread)
        assertEquals(1, lock.holdCount)
        lock.lock()
        assertEquals(true, lock.isHeldByCurrentThread)
        assertEquals(2, lock.holdCount)

        lock.unlock()
        assertEquals(true, lock.isHeldByCurrentThread)
        assertEquals(1, lock.holdCount)
        lock.unlock()
        assertEquals(false, lock.isHeldByCurrentThread)
        assertEquals(0, lock.holdCount)
    }

    @Test
    fun `reentrant lock`() {
        val lock = ReentrantLock()
// A thread invoking lock will return, successfully acquiring the lock, when the lock is not owned by another thread.
        expect(0)
        thread {
            expect(2)
            lock.lock()
            expect(3)
            sleep(100)
            expect(4)
            lock.unlock()
            expect(5)
        }
        expect(1)
        sleep(10)
        lock.lock()
        expect(6)
    }
}
