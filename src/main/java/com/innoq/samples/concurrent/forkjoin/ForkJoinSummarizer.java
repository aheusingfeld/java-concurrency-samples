package com.innoq.samples.concurrent.forkjoin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Inspired by PhilippS' "Clojure Reducers" talk, this is an example of a summarizer using the fork join framework.
 * User: ahe
 * Date: 20.06.13
 * Time: 14:59
 */
public class ForkJoinSummarizer extends RecursiveTask<Long> {

    private final List<Integer> num;
    private final int seqThreshold;

    public ForkJoinSummarizer(List<Integer> num, final int seqThreshold) {
        this.num = num;
        this.seqThreshold = seqThreshold;
    }

    @Override
    protected Long compute() {
        if (num.size() < seqThreshold) {
            return sum(num);
        } else {
            final int midPosition = num.size() / 2;
            final ForkJoinSummarizer sum1 = new ForkJoinSummarizer(num.subList(0, midPosition), seqThreshold);
            sum1.fork();
            final ForkJoinSummarizer sum2 = new ForkJoinSummarizer(num.subList(midPosition, num.size()), seqThreshold);
            return sum2.compute() + sum1.join();
        }
    }

    private Long sum(final List<Integer> num) {
        long result = 0;
        for (long n : num) {
            // sum odd numbers
            result += (n % 2 == 0) ? 0 : n;
        }
        // NOTE: This test neglects stripping the additional effort needed for auto-boxing from 'long' to 'Long'
        return result;
    }



    // static utility methods

    private static Long sumOddEntries(List<Integer> num) {
        long result = 0;
        for (long n : num) {
            // sumOddEntries odd numbers
            result += (n % 2 == 0) ? 0 : n;
        }
        // NOTE: This test neglects stripping the additional effort needed for auto-boxing from 'long' to 'Long'
        return result;
    }

    /**
     * Creates an {@link ArrayList} containing all integers from 1 to the specified value.
     * @param lastListEntry - positive integer which shall be taken as the last/ highest entry of the returned list
     * @return list containing integers from 1 to #lastListEntry
     */
    private static List<Integer> initializeList(int lastListEntry) {
        final ArrayList<Integer> longs = new ArrayList<>();
        for (int i = 1; i <= lastListEntry; i++) {
            longs.add(i);
        }
        return longs;
    }

    private static void doTest(int iterations, int sumUpTo) {
        System.out.println("Starting test! Summarize numbers from 1 to " + sumUpTo + " for " + iterations + " times!");
        final List<Integer> list = initializeList(sumUpTo);

        Long result = (long) 0;
        long start = System.nanoTime();
        for(long m = 0; m< iterations; m++) {
            // do it sequentially
            long res = 0;
            for (long n : list) {
                // sumOddEntries odd numbers
                res += (n % 2 == 0) ? 0 : n;
            }
            result = res;
        }
        long elapsedTime = ((System.nanoTime() - start) / NANOS_PER_MS);
        System.out.println("Result (seq):      " + result + "; Duration: " + elapsedTime
                + "ms; Duration/iter: " + (elapsedTime / iterations) + "ms");


        // do it sequentially with static utility method
        start = System.nanoTime();
        for(long n = 0; n< iterations; n++) {
            result = ForkJoinSummarizer.sumOddEntries(list);
        }
        elapsedTime = ((System.nanoTime() - start) / NANOS_PER_MS);
        System.out.println("Result (seq static): " + result + "; Duration: " + elapsedTime
                + "ms; Duration/iter: " + (elapsedTime / iterations) + "ms");


        // (potentially) do it in parallel
        final ForkJoinPool pool = new ForkJoinPool();
        start = System.nanoTime();
        for(long n = 0; n< iterations; n++) {
            final ForkJoinSummarizer summary = new ForkJoinSummarizer(list, SEQ_THRESHOLD);

            // actually 'Runtime.getRuntime().availableProcessors()' is used by default!
            result = pool.invoke(summary);
        }
        elapsedTime = ((System.nanoTime() - start) / NANOS_PER_MS);
        System.out.println("Result (parallel): " + result + "; Duration: " + elapsedTime
                + "ms; Duration/iter: " + (elapsedTime / iterations) + "ms; Parallelism: " + pool.getParallelism()
                + "; ThreadPool size: " + pool.getPoolSize()+ "; Avg. Steal/iter: " + (pool.getStealCount() / iterations));
    }


    /**
     * Threshold indicating when sequential work is more efficient.
     */
    private static final int SEQ_THRESHOLD = 800;
    private static final long NANOS_PER_MS = 1000000L;

    public static void main(String[] args) {
        System.out.println("Warming up...");
        doTest(1_000, 2_000_000);
        System.out.println("Warm up finished.");

        System.out.println("\nStart measuring...");
        doTest(10_000, 2_000_000);
        System.out.println("Measuring done.");

    }
}
