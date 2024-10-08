package org.minturtle.careersupport.common;

import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BlockHoundTest {

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return null;
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            fail("Should fail");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof BlockingOperationError);
        } catch (Exception e) {
            fail(e);
        }
    }
}