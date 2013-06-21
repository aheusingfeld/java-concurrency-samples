package com.innoq.samples.concurrent.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FibonacciLong extends RecursiveTask<Long> {

	private static final long serialVersionUID = 1L;

	private static int sequentialThreshold = 10;
	private final int n;

	public FibonacciLong(int n) {
		this.n = n;
	}

	protected Long compute() {
		if (n <= sequentialThreshold) {
			return seqFib(n);
		} else {
			final FibonacciLong f1 = new FibonacciLong(n - 1);
			f1.fork();
			final FibonacciLong f2 = new FibonacciLong(n - 2);
			return f2.compute() + f1.join();
		}
	}

	private static long seqFib(long n) {
		return n <= 1 ? n : seqFib(n - 1) + seqFib(n - 2);
	}

	public static void main(String[] argc) {
		final ForkJoinPool pool = new ForkJoinPool();
		final FibonacciLong f = new FibonacciLong(45);
		long starttime = System.currentTimeMillis();
		final long result = pool.invoke(f);
		System.out.println("result = " + result + " took "
				+ (System.currentTimeMillis() - starttime) + "ms");
		System.out.println(pool);
	}

}
