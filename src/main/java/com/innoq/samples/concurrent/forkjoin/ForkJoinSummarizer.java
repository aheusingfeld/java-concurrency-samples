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

    /**
     * Threshold indicating when sequential work is more efficient.
     */
    private static int SEQ_THRESHOLD = 700;
    private static int SUM_UP_TO = 5_000_000;

    private final List<Integer> num;

    public ForkJoinSummarizer(List<Integer> num) {
        this.num = num;
    }

    @Override
    protected Long compute() {
        if (num.size() < SEQ_THRESHOLD) {
            return sum(num);
        } else {
            int midPosition = num.size() / 2;
            final ForkJoinSummarizer sum1 = new ForkJoinSummarizer(num.subList(0, midPosition));
            sum1.fork();
            final ForkJoinSummarizer sum2 = new ForkJoinSummarizer(num.subList(midPosition, num.size()));
            return sum2.compute() + sum1.join();
        }
    }

    private Long sum(List<Integer> num) {
        long result = 0;
        for (long n : num) {
            // sum odd numbers
            result += (n % 2 == 0) ? 0 : n;
        }
        return result;
    }


    // utility methods

    public static void main(String[] args) {

        final List<Integer> list = initializeList(SUM_UP_TO);
        Long result = 0l;

        // do it sequentially
        long start = System.currentTimeMillis();
        for (int i : list) {
            result += (i % 2 == 0) ? 0 : i;
        }
        System.out.println("Result (seq): " + result + " Duration: " + (System.currentTimeMillis() - start));


        // (potentially) do it in parallel
        final ForkJoinSummarizer summary = new ForkJoinSummarizer(list);

        // actually 'Runtime.getRuntime().availableProcessors()' is used by default!
        final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        start = System.currentTimeMillis();
        result = pool.invoke(summary);
        System.out.println("Result (parallel): " + result + "; Duration: " + (System.currentTimeMillis() - start));
        System.out.println("      Parallelism: " + pool.getParallelism() + "; ThreadPool size: " + pool.getPoolSize() + "; Steals: " + pool.getStealCount());
    }

    private static List<Integer> initializeList(int l) {
        final ArrayList<Integer> longs = new ArrayList<Integer>();
        for (int i = 1; i <= l; i++) {
            longs.add(i);
        }
        return longs;
    }
}
