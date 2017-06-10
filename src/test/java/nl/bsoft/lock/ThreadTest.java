package nl.bsoft.lock;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Unit test for simple App.
 */
public class ThreadTest {
    private final Logger log = LoggerFactory.getLogger(ThreadTest.class);

    @Rule
    public TestName name = new TestName();

    @Test
    public void Test() {
        log.info("Start test: {}", name.getMethodName());

        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void IncrementCounter() {
        log.info("Start test: {}", name.getMethodName());

        Counter cnt = new Counter();
        cnt.increment();
        Assert.assertEquals(1, cnt.getCount());

        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void StartThreadSingleShot() {
        log.info("Start test: {}", name.getMethodName());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("In thread: {}", threadName);
        });

        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void StartThreadAndStop() {
        log.info("Start test: {}", name.getMethodName());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("In thread: {}", threadName);
        });
        try {
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

        log.info("End   test: {}", name.getMethodName());
    }


    @Test
    public void StartCallable() {
        log.info("Start test: {}", name.getMethodName());

        Callable<Integer> task = () -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                return 123;
            } catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Integer> future = executor.submit(task);
        try {
            log.info("future done? " + future.isDone());
            Integer result = future.get();

            log.info("future done? " + future.isDone());
            log.info("result: " + result);

            log.info("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("tasks interrupted");
        } catch (ExecutionException e) {
            log.error("tasks execution error");
        } finally {
            if (!executor.isTerminated()) {
                log.error("cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.info("shutdown finished");
        }

        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void StartCallableTimeout() {
        log.info("Start test: {}", name.getMethodName());

        Callable<Integer> task = () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                return 123;
            } catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Integer> future = executor.submit(task);
        try {

            future.get(1L,TimeUnit.SECONDS);
            log.info("future done? " + future.isDone());
            Integer result = future.get();

            log.info("future done? " + future.isDone());
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

    @Test
    public void StartCallableAllList() {
        log.info("Start test: {}", name.getMethodName());

        Callable<Integer> task01 = () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                return 100;
            } catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        Callable<Integer> task02 = () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                return 200;
            } catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        Callable<Integer> task03 = () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                return 300;
            } catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Integer>> callables = Arrays.asList(task01, task02, task03);

        try {

        executor.invokeAll(callables)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .forEach(s -> log.info("Result: {}", s));
//                .forEach(s -> System.out.println("Result: " + s));
//                .forEach(System.out::println);


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

        log.info("End   test: {}", name.getMethodName());
    }

    Callable<Integer> task(Integer result, long timeOut) {
        return () -> {
            TimeUnit.SECONDS.sleep(timeOut);
            log.info("Ready waiting in task, result: {}, timeout: {}", result, timeOut);
            return result;
        };
    }

    @Test
    public void StartCallableAnyList() {
        log.info("Start test: {}", name.getMethodName());

        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Integer>> callables = Arrays.asList(task(100,3), task(200,2), task(300,1));

        try {

            Integer result = executor.invokeAny(callables);
            log.info("Result: {}", result);

            log.info("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("tasks interrupted");
        } catch (ExecutionException e) {
            log.error("execution error");
        }
        finally {
            if (!executor.isTerminated()) {
                log.error("cancel non-finished tasks");
            }
            executor.shutdownNow();
            log.info("shutdown finished");
        }

        log.info("End   test: {}", name.getMethodName());
    }


    Callable taskScheduled() {
        return () -> {
            log.info("Running scheduled task: {}", System.nanoTime());
            return null;
        };
    }

    @Test
    public void StartScheduled() {
        log.info("Start test: {}", name.getMethodName());

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        try {

            ScheduledFuture<?> future = executor.schedule(taskScheduled(), 3, TimeUnit.SECONDS);
            TimeUnit.MILLISECONDS.sleep(1337);
            long remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
            log.info("Remaining Delay: {} ms", remainingDelay);


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

        log.info("End   test: {}", name.getMethodName());
    }


    Runnable taskScheduledPeriodic() {
        return () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500);
                // TimeUnit.SECONDS.sleep(2);
                log.info("Running scheduled periodic task Scheduling: {}", System.nanoTime());
            } catch (InterruptedException e) {
                log.error("task interrupted");
            }
        };
    }

    @Test
    public void StartScheduledPeriodical() {
        log.info("Start test: {}", name.getMethodName());

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        try {

            int initialDelay = 0;
            int period = 1;
            ScheduledFuture<?> future = executor.scheduleAtFixedRate(taskScheduledPeriodic(), initialDelay, period, TimeUnit.SECONDS);

            Thread.sleep(4000);
            /*
            TimeUnit.MILLISECONDS.sleep(1337);
            long remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
            log.info("Remaining Delay: {} ms", remainingDelay);
            */

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

        log.info("End   test: {}", name.getMethodName());
    }

    @Test
    public void StartScheduledFixedDelay() {
        log.info("Start test: {}", name.getMethodName());

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        try {

            int initialDelay = 0;
            int period = 1;
            ScheduledFuture<?> future = executor.scheduleWithFixedDelay(taskScheduledPeriodic(), initialDelay, period, TimeUnit.SECONDS);

            Thread.sleep(4000);
            /*
            TimeUnit.MILLISECONDS.sleep(1337);
            long remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
            log.info("Remaining Delay: {} ms", remainingDelay);
            */

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

        log.info("End   test: {}", name.getMethodName());
    }

}
