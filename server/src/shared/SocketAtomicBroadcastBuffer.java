package shared;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketAtomicBroadcastBuffer<T> implements AtomicBroadcastBuffer<T> {

	private String host;
	private int port;

	public SocketAtomicBroadcastBuffer(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	@Override
	public void put(T item) {
		try (Socket client = new Socket(host, port);
				ObjectInputStream in = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {
			out.writeObject("put");
			String ret = (String) in.readObject();
			if ("OK DONE".equals(ret)) {
				out.writeObject(item);
				in.readObject(); // ACK / OK, za sinhronu komunikaciju

			} else {
				System.err.println(String.format("*** Operation %s not supported.", "put"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public T get(int id) {
		try (Socket client = new Socket(host, port);
				ObjectInputStream in = new ObjectInputStream(client.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {

			out.writeObject("get");
			String ret = (String) in.readObject();
			if ("OK DONE".equals(ret)) {
				out.writeInt(id);
				out.flush();
				T item = (T) in.readObject();
				out.writeObject("OK");
				out.flush();
				return item;
			} else {
				System.err.println(String.format("*** Operation %s not supported.", "put"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void addListener(int id) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeListener(int id) {
		// TODO Auto-generated method stub

	}

}
