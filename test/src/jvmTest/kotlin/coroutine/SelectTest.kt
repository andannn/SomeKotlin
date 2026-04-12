package coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlin.test.Test

class SelectTest {
    @Test
    fun select_defferred_Test(): Unit = runBlocking {
        suspend fun requestData1(): String {
            delay(100_000)
            return "Data1"
        }

        suspend fun requestData2(): String {
            delay(1000)
            return "Data2"
        }

        suspend fun askMultipleForData(): String = coroutineScope {
            select<String> {
                async { requestData1() }
                    .onAwait
                    .invoke(
                        {
                            it
                        }
                    )
                async { requestData2() }.onAwait { it }
            }.also { coroutineContext.cancelChildren() }
        }

        askMultipleForData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun select_channel_Test(): Unit = runBlocking {
        fun CoroutineScope.produceString(s: String, time: Long) =
            produce {
                while (true) {
                    delay(time)
                    send(s)
                }
            }

        val fooChannel = produceString("foo", 210L)
        val barChannel = produceString("BAR", 500L)

        repeat(7) {
            select {
                fooChannel.onReceive {
                    println("From fooChannel: $it")
                }
                barChannel.onReceive {
                    println("From barChannel: $it")
                }
            }
        }

        coroutineContext.cancelChildren()
    }
}