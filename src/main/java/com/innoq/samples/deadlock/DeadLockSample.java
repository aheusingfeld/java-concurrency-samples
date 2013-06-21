package com.innoq.samples.deadlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Deadlock sample code from http://javaeesupportpatterns.blogspot.de/2013/01/java-concurrency-hidden-thread-deadlocks.html
 */
public class DeadLockSample {
    /**
     * A simple thread task representation
     * @author Pierre-Hugues Charbonneau
     *
     */
    static class Task {

        // Object used for FLAT lock
        private final Object sharedObject = new Object();
        // ReentrantReadWriteLock used for WRITE & READ locks
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        /**
         *  Execution pattern #1
         */
        public void executeTask1() {

            // 1. Attempt to acquire a ReentrantReadWriteLock READ lock
            lock.writeLock().lock();

            // Wait 2 seconds to simulate some work...
            try { Thread.sleep(2000);}catch (Throwable any) {}

            try {
                // 2. Attempt to acquire a Flat lock...
                synchronized (sharedObject) {}
            }
            // Remove the READ lock
            finally {
                lock.writeLock().unlock();
            }

            System.out.println("executeTask1() :: Work Done!");
        }

        /**
         *  Execution pattern #2
         */
        public void executeTask2() {

            // 1. Attempt to acquire a Flat lock
            synchronized (sharedObject) {

                // Wait 2 seconds to simulate some work...
                try { Thread.sleep(2000);} catch (Throwable any) {}

                // 2. Attempt to acquire a WRITE lock
                lock.writeLock().lock();

                try {
                    // Do nothing
                }

                // Remove the WRITE lock
                finally {
                    lock.writeLock().unlock();
                }
            }

            System.out.println("executeTask2() :: Work Done!");
        }

        public ReentrantReadWriteLock getReentrantReadWriteLock() {
            return lock;
        }
    }

    public static void main(String[] args) {
        final CountDownLatch endSignal = new CountDownLatch(2);
        final Task task = new Task();
        Runnable run1 = new Runnable() {

            @Override
            public void run() {
                task.executeTask1();
                endSignal.countDown();
            }
        };

        Runnable run2 = new Runnable() {

            @Override
            public void run() {
                task.executeTask2();
                endSignal.countDown();
            }
        };
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(run1);
        executor.execute(run2);
        executor.shutdown();
        System.out.println("Perform a thread dump now and check whether the JVM finds the deadlock!");
        // NOTE: The JVM will not recognize deadlocks for "read locks" as they don't obtain lock ownership.
        // Change the 'lock.readLock()' in executeTask1 to 'lock.writeLock()'
        // and run the ThreadDump again to see the difference.
        //
        // To get hold of READ locks you can query "lock.getReadLockCount()" before trying to acquire a lock.


// Wait until all threads are finish
        while (!executor.isTerminated()) {
            try {
                endSignal.await();
            } catch (InterruptedException e) {}
        }
        // ...will never be reached because of the deadlock
        System.out.println("Finished!");

    }

}
