package com.innoq.samples.concurrent.forkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FibonacciInteger extends RecursiveTask<Integer> {

	private static final long serialVersionUID = 1L;

	private static int sequentialThreshold = 10;
	private final int n;

	public FibonacciInteger(int n) {
		this.n = n;
	}

	protected Integer compute() {
		if (n <= sequentialThreshold) {
			return seqFib(n);
		} else {
			FibonacciInteger f1 = new FibonacciInteger(n - 1);
			f1.fork();
			FibonacciInteger f2 = new FibonacciInteger(n - 2);
			return f2.compute() + f1.join();
		}
	}

	private static int seqFib(int n) {
		return n <= 1 ? n : seqFib(n - 1) + seqFib(n - 2);
	}

	public static void main(String[] argc) {
		ForkJoinPool pool = new ForkJoinPool();
		FibonacciInteger f = new FibonacciInteger(45); // 15, 25, 40, 50
		long starttime = System.currentTimeMillis();
		int result = pool.invoke(f);
		System.out.println("result = " + result + " took "
				+ (System.currentTimeMillis() - starttime) + "ms");
		System.out.println(pool);
	}

}
