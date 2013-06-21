/**
 * 
 */
package com.innoq.samples.concurrent.forkjoin;

import java.math.BigInteger;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FibonacciBigInt extends RecursiveTask<BigInteger> {

	private static final long serialVersionUID = 1L;

	private static int sequentialThreshold = 10;
	private static final BigInteger TWO = BigInteger.valueOf(2);

	private final int n;

	public FibonacciBigInt(int n) {
		this.n = n;
	}

	protected BigInteger compute() {
		if (n <= sequentialThreshold) {
			return seqFib(BigInteger.valueOf(n));
		} else {
			final FibonacciBigInt f1 = new FibonacciBigInt(n - 1);
			f1.fork();
			final FibonacciBigInt f2 = new FibonacciBigInt(n - 2);
			return f2.compute().add(f1.join());
		}
	}

	private static BigInteger seqFib(BigInteger n) {
		return n.compareTo(BigInteger.ONE) <= 0 ? n : seqFib(
				n.subtract(BigInteger.ONE)).add(seqFib(n.subtract(TWO)));
	}

	public static void main(String[] argc) {
		final ForkJoinPool pool = new ForkJoinPool();
		final FibonacciBigInt f = new FibonacciBigInt(45);
		long starttime = System.currentTimeMillis();
		final BigInteger result = pool.invoke(f);
		System.out.println("result = " + result + " took "
				+ (System.currentTimeMillis() - starttime) + "ms");
		System.out.println(pool);
	}

}
