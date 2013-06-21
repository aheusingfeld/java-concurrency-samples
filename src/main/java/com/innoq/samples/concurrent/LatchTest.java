package com.innoq.samples.concurrent;

import java.util.concurrent.CountDownLatch;

/**
 * Beispiel der Verwendung eines CountDownLatch.
 * 
 * Latches sind Bedingungen, die nach dem Setzen nicht mehr geaendert werden. Auch hier kann so eine Threadgruppe synchronisiert werden, z.B. der Start verzoegert werden, bis alle Threads ihre Initialisierung abgeschlossen haben.
 *
 * Sample based on ideas from "Java Concurrency in Practice"
 * Copyright http://jcip.net/
 */
public class LatchTest {
	private static final int COUNT = 10;

	private static class Worker implements Runnable {
		CountDownLatch startLatch;
		CountDownLatch stopLatch;
		String name;

		Worker(CountDownLatch startLatch, CountDownLatch stopLatch, String name) {
			this.startLatch = startLatch;
			this.stopLatch = stopLatch;
			this.name = name;
		}

		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			System.out.println("Running: " + name);
			stopLatch.countDown();
		}
	}

	public static void main(String args[]) {
		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch stopSignal = new CountDownLatch(COUNT);
		for (int i = 0; i < COUNT; i++) {
			new Thread(new Worker(startSignal, stopSignal, Integer.toString(i)))
					.start();
		}
		System.out.println("Go");
		startSignal.countDown();
		try {
			stopSignal.await();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.out.println("Done");
	}
}