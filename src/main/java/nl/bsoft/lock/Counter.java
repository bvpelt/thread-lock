package nl.bsoft.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bvpelt on 5/5/17.
 */
public class Counter {
    private final Logger log = LoggerFactory.getLogger(Counter.class);

    int count = 0;

    void increment() {
        count = count + 1;
    }

    void decrement() {
        count = count - 1;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
