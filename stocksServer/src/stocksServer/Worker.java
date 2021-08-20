package stocksServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public interface Worker {
	public abstract HashMap<String, Integer> work(ObjectOutputStream out, ObjectInputStream in,
			HashMap<String, Integer> map) throws IOException, Exception;
}
