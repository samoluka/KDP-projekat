package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public interface Worker {
	public abstract void work(Socket client, ObjectOutputStream out, ObjectInputStream in, int id)
			throws IOException, Exception;
}
