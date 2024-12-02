package org.minturtle.careersupport.common

import org.assertj.core.api.Assertions.assertThat
import reactor.blockhound.BlockingOperationError
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.fail

class BlockhoundTest {

    @Test
    fun `BlockHound should detect blocking operations`() {
        try {
            val task = FutureTask<Void?> {
                Thread.sleep(0)
                null
            }

            Schedulers.parallel().schedule(task)
            task.get(10, TimeUnit.SECONDS)
            fail("Should fail")
        } catch (e: ExecutionException) {
            assertThat(e.cause).isInstanceOf(BlockingOperationError::class.java)
        } catch (e: Exception) {
            fail("Unexpected exception", e)
        }
    }
}