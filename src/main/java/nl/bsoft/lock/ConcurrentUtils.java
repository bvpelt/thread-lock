package nl.bsoft.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by bvpelt on 5/5/17.
 */

public class ConcurrentUtils {
    private final Logger log = LoggerFactory.getLogger(ConcurrentUtils.class);

    public void stop(ExecutorService executor) {
        stop(executor, 60);
        /*
        try {
            executor.shutdown();

            executor.awaitTermination(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("termination interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("killing non-finished tasks");
            }
            executor.shutdownNow();
        }
        */
    }

    public void stop(ExecutorService executor, long timeOut) {
        try {
            executor.shutdown();

            executor.awaitTermination(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("termination interrupted");
        } finally {
            if (!executor.isTerminated()) {
                log.error("killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }

    public void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            log.error("Sleep interrupted: {}", e);
            throw new IllegalStateException(e);
        }
    }


}