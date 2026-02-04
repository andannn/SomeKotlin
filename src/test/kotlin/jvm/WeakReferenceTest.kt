package jvm

import java.lang.ref.WeakReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WeakReferenceTest {
    @Test
    fun `WeakReference test`() {
        class TestModel

        var strongRef: TestModel? = TestModel()
        val weakRef = WeakReference(strongRef)

        strongRef = null
        System.gc()

        assertEquals(null, weakRef.get())
    }

    @Test
    fun `weak reference test 2`() {
        class TestModel

        class TestModelContainer(
            model: TestModel,
        ) {
            private val ref = WeakReference(model)

            fun exist() = ref.get() != null
        }

        var strongRef: TestModel? = TestModel()

        val testModelContainer = TestModelContainer(strongRef!!)
        assertEquals(true, testModelContainer.exist())

        strongRef = null
        System.gc()

        assertEquals(false, testModelContainer.exist())
    }

    @Test
    fun `weak reference test 3`() {
        class SimulatedEntry(
            key: Any,
            value: Any?,
        ) : WeakReference<Any>(key)

        var threadLocalKey: Any? = Any()

        val bigDataValue = ByteArray(1024 * 1024)
        val entry = SimulatedEntry(threadLocalKey!!, bigDataValue)

        assertNotNull(entry.get())

        threadLocalKey = null
        System.gc()

        assertNull(entry.get())
    }
}
