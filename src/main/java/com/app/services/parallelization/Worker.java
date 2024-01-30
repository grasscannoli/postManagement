package com.app.services.parallelization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Worker method to run a Callable
 * @param <V>
 */
public class Worker<V> {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private Callable<V> work;
    private V valueOnError;

    public Worker(Callable<V> work, V valueOnError) {
        this.work = work;
        this.valueOnError = valueOnError;
    }

    public V executeWork() {
        try {
            return work.call();
        } catch (Throwable t) {
            logger.error("Worker failed with error ", t);
            return valueOnError;
        }
    }
}
