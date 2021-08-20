package shared;

public interface AtomicBroadcastBuffer<T> {
	public void put(T item);

	public T get(int id);

	public void addListener(int id);

	public void removeListener(int id);
}
