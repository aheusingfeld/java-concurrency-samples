package com.innoq.samples.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Beispielhafte Verwendung eines "CyclicBarrier" zur Summation von Matrixelementen.
 *
 * Sample based on ideas from "Java Concurrency in Practice" - http://jcip.net/
 */
public class Summary {
	private static int matrix[][] = { { 1 }, { 2, 2 }, { 3, 3, 3 },
			{ 4, 4, 4, 4 }, { 5, 5, 5, 5, 5 } };
	private static int results[];

	private static class Summer extends Thread {
		int row;
		CyclicBarrier barrier;

		Summer(CyclicBarrier barrier, int row) {
			this.barrier = barrier;
			this.row = row;
		}

		public void run() {
			int columns = matrix[row].length;
			int sum = 0;
			for (int i = 0; i < columns; i++) {
				sum += matrix[row][i];
			}
			results[row] = sum;
			System.out.println("Results for row " + row + " are : " + sum);
			// wait for others
			try {
				barrier.await();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} catch (BrokenBarrierException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {
		final int rows = matrix.length;
		results = new int[rows];
		Runnable merger = new Runnable() {
			public void run() {
				int sum = 0;
				for (int i = 0; i < rows; i++) {
					sum += results[i];
				}
				System.out.println("Results are: " + sum);
			}
		};
		CyclicBarrier barrier = new CyclicBarrier(rows, merger);
		for (int i = 0; i < rows; i++) {
			new Summer(barrier, i).start();
		}
		System.out.println("Waiting...");
	}
}