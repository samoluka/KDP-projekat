package shared;

import java.util.HashMap;
import java.util.Map.Entry;

public class MonitorAtomicBroadcastBuffer<T> implements AtomicBroadcastBuffer<T> {

	private T[] buffer;

	private int n = 0;
	private int b;

	private int head;

	// private List<Integer> tail;

	private int[] counter;

	private boolean[] availableItem;

	private long writeNext;
	// private List<Long> readNext;

	private HashMap<Integer, Integer> tail = new HashMap<>();
	private HashMap<Integer, Long> readNext = new HashMap<>();

	@SuppressWarnings("unchecked")
	public MonitorAtomicBroadcastBuffer(int b) {
		this.b = b;

		buffer = (T[]) new Object[b];

		head = 0;
		counter = new int[b];
		for (int i = 0; i < b; i++)
			counter[i] = n;

		availableItem = new boolean[b];
		for (int i = 0; i < b; i++)
			availableItem[i] = false;

		writeNext = 0;
	}

	@Override
	public synchronized T get(int id) {
		while (readNext.get(id) == writeNext || availableItem[tail.get(id)] == false)
			try {
				wait();
			} catch (InterruptedException e) {
			}

		int t = tail.get(id);
		T item = buffer[t];
		counter[t]++;
		if (counter[t] >= n) {
			availableItem[t] = false;
			notifyAll();
		}
		readNext.put(id, readNext.get(id) + 1);
		tail.put(id, (t + 1) % b);
		return item;
	}

	@Override
	public synchronized void put(T item) {
		while (!(counter[head] >= n && availableItem[head] == false))
			try {
				wait();
			} catch (InterruptedException e) {
			}

		buffer[head] = item;

		counter[head] = 0;
		availableItem[head] = true;
		writeNext++;
		head = (head + 1) % b;

		notifyAll();
	}

	@Override
	public synchronized void addListener(int id) {
		int t = head;
		for (Entry<Integer, Integer> pair : tail.entrySet()) {
			if (pair.getValue() > head) {
				if (t > head && pair.getValue() < t || t < head) {
					t = pair.getValue();
				}
			} else {
				if (t > head) {
					continue;
				}
				if (pair.getValue() < t) {
					t = pair.getValue();
				}
			}
		}
		if (n == 0) {
			t = 0;
		}
		Long maxTicket = (long) -1;
		for (Entry<Integer, Long> pair : readNext.entrySet()) {
			if (pair.getValue() > maxTicket) {
				maxTicket = pair.getValue();
			}
		}
		tail.put(id, t);
		readNext.put(id, ++maxTicket);
		n += 1;
		for (int i = 0; i < b; i++) {
			if (availableItem[i] == false) {
				counter[i] = n;
			}
		}
	}

	@Override
	public synchronized void removeListener(int id) {
		n--;
		// long r = readNext.get(id);
		readNext.remove(id);
		tail.remove(id);
	}

}
