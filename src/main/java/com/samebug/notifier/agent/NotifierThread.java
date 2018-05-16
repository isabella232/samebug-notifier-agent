package com.samebug.notifier.agent;

import com.samebug.notifier.client.CrashMonitorClient;

import java.text.MessageFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NotifierThread extends Thread {
    private final BlockingQueue<Throwable> queue;
    private final CrashMonitorClient client;
    private final static AtomicBoolean shutdown = new AtomicBoolean(false);
    private final static AtomicInteger threadCounter = new AtomicInteger(0);

    public NotifierThread() {
        super(MessageFormat.format("Samebug Notifier Thread {0}", threadCounter.incrementAndGet()));
        this.queue = ExceptionQueue.getInstance();
        this.client = new CrashMonitorClient();
        Runtime.getRuntime().addShutdownHook(shutdownHook());
        setDaemon(true);
        start();
    }

    public void run() {
        while (!(shutdown.get() && queue.isEmpty())) {
            try {
                client.report(queue.take());
            } catch (Exception e) {
                System.err.println("Failed to report exception by samebug notifier agent!");
                // TODO need some protection against looping on reporting the report failure
//                e.printStackTrace(System.err);
            }
        }
    }

    private class CleanupThread extends Thread {
        public void run() {
            // TODO somehow we should guarantee that no new throwables are accepted to the queue, or that we will wait at most x seconds
            shutdown.set(true);
            try {
                NotifierThread.this.join();
            } catch (InterruptedException e) {
                System.err.println("Failed to clean up notifier thread in samebug notifier agent!");
                e.printStackTrace(System.err);
            }
        }
    }

    private Thread shutdownHook() {
        return new CleanupThread();
    }
}
