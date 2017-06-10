package nl.bsoft.lock;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;

/**
 * Created by bvpelt on 6/10/17.
 */
public class SynchronizedLockTest {
    private final Logger log = LoggerFactory.getLogger(SynchronizedLockTest.class);

    @Rule
    public TestName name = new TestName();

    private final int maxNumber = 100000; // number of loops

    private int count = 0;

    private ReentrantLock lock = new ReentrantLock();

    void increment() {
        count = count + 1;
    }

    synchronized void incrementSync() {
        count = count + 1;
    }

    void reentrentLockIncrement() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Test
    public void test() {
        log.info("Start test: {}", name.getMethodName());

        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void incrementCounterNormaal() {
        log.info("Start test: {}", name.getMethodName());

        for (int i = 0; i < 10; i++) {
            increment();
        }
        Assert.assertEquals(10, getCount());
        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void incrementCounterTwoThreads() {
        log.info("Start test: {}", name.getMethodName());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            IntStream.range(0, maxNumber)
                    .forEach(i -> executor.submit(this::increment));
            log.info("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                log.error("cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.info("shutdown finished");
        }
        // Purpose of test is to show increments has race conditions and is not maxNumber as expected
        log.info("Expected value: {}, actual value: {} not equal is ok", maxNumber, getCount());
        Assert.assertNotSame(maxNumber, getCount());
        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void incrementSynchCounterTwoThreads() {
        log.info("Start test: {}", name.getMethodName());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            IntStream.range(0, maxNumber)
                    .forEach(i -> executor.submit(this::incrementSync));
            log.info("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                log.error("cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.info("shutdown finished");
        }
        // Purpose of test is to show increments has race conditions and is not maxNumber as expected
        log.info("Expected value: {}, actual value: {} equal is ok", maxNumber, getCount());
        Assert.assertEquals(maxNumber, getCount());
        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void incrementReentrantLockCounterTwoThreads() {
        log.info("Start test: {}", name.getMethodName());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            IntStream.range(0, maxNumber)
                    .forEach(i -> executor.submit(this::reentrentLockIncrement));
            log.info("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                log.error("cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.info("shutdown finished");
        }
        // Purpose of test is to show increments has race conditions and is not maxNumber as expected
        log.info("Expected value: {}, actual value: {} equal is ok", maxNumber, getCount());
        Assert.assertEquals(maxNumber, getCount());
        log.info("End   test: {}", name.getMethodName());
    }

    Callable task = () -> {
        try {
            lock.lock();
            try {
                log.info("Got lock");
                sleep(1000);
            } finally {
                lock.unlock();
            }
            return 1;
        } catch (InterruptedException e) {
            log.error("Task interrupted: {}", e);
            throw new IllegalStateException("task interrupted", e);
        }
    };

    @Test
    public void pingPong() {
        log.info("Start test: {}", name.getMethodName());

        ExecutorService executor = Executors.newFixedThreadPool(3);
        Future<Integer> future01 = executor.submit(task);
        Future<Integer> future02 = executor.submit(task);
        Future<Integer> future03 = executor.submit(task);
        try {

            future01.get(2L,TimeUnit.SECONDS);
            future02.get(2L,TimeUnit.SECONDS);
            future03.get(2L,TimeUnit.SECONDS);
            log.info("future01 done? " + future01.isDone());
            log.info("future02 done? " + future02.isDone());
            log.info("future03 done? " + future03.isDone());
            Integer result = future01.get();
            result = future02.get();
            result = future03.get();

            log.info("future01 done? " + future01.isDone());
            log.info("future02 done? " + future02.isDone());
            log.info("future03 done? " + future03.isDone());
            log.info("result: " + result);

            log.info("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("tasks interrupted");
        } catch (ExecutionException e) {
            log.error("tasks execution error");
        } catch (TimeoutException e) {
            log.error("Timeout occured");
        } finally {
            if (!executor.isTerminated()) {
                log.error("cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.info("shutdown finished");
        }

        log.info("End   test: {}", name.getMethodName());
    }
}
