package com.innoq.samples.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;

/**
 * Sample based on ideas from "Java Concurrency in Practice" -
 * Copyright http://jcip.net/
 */
public class ExchangerTest {
	private static final int FULL = 10;
	private static final int COUNT = FULL * 20;

    // 'Random' shares static modifiers and should not be used in concurrent situation. See ThreadLocalRandom for an alternative.
	private static final Random random = new Random();

    // the 'volatile' keyword notifies all referencing threads when the value is changed so they update their local copy.
	private static volatile int sum = 0;

    // An Exchanger can be used to swap objects between Threads
	private static Exchanger<List<Integer>> exchanger = new Exchanger<List<Integer>>();
	private static List<Integer> initiallyEmptyBuffer;
	private static List<Integer> initiallyFullBuffer;
	private static CountDownLatch stopLatch = new CountDownLatch(2);

	private static class FillingLoop implements Runnable {
		public void run() {
			List<Integer> currentBuffer = initiallyEmptyBuffer;
			try {
				for (int i = 0; i < COUNT; i++) {
					if (currentBuffer == null)
						break; // stop on null
					Integer item = random.nextInt(100);
					System.out.println("Added: " + item);
					currentBuffer.add(item);
					if (currentBuffer.size() == FULL)
						currentBuffer = exchanger.exchange(currentBuffer);
				}
			} catch (InterruptedException ex) {
				System.out.println("Bad exchange on filling side");
			}
			stopLatch.countDown();
		}
	}

	private static class EmptyingLoop implements Runnable {
		public void run() {
			List<Integer> currentBuffer = initiallyFullBuffer;
			try {
				for (int i = 0; i < COUNT; i++) {
					if (currentBuffer == null)
						break; // stop on null
					Integer item = currentBuffer.remove(0);
					System.out.println("Got: " + item);
					sum += item.intValue();
					if (currentBuffer.isEmpty()) {
						currentBuffer = exchanger.exchange(currentBuffer);
					}
				}
			} catch (InterruptedException ex) {
				System.out.println("Bad exchange on emptying side");
			}
			stopLatch.countDown();
		}
	}

	public static void main(String args[]) {
		initiallyEmptyBuffer = new ArrayList<Integer>();
		initiallyFullBuffer = new ArrayList<Integer>(FULL);
		for (int i = 0; i < FULL; i++) {
			initiallyFullBuffer.add(random.nextInt(100));
		}
		new Thread(new FillingLoop()).start();
		new Thread(new EmptyingLoop()).start();
		try {
			stopLatch.await();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.out.println("Sum of all items is.... " + sum);
	}
}