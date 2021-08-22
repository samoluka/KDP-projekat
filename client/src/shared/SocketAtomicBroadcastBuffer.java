package shared;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketAtomicBroadcastBuffer<T> implements AtomicBroadcastBuffer<T> {

	private Socket client;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public SocketAtomicBroadcastBuffer(Socket client, ObjectInputStream in, ObjectOutputStream out) {
		super();
		this.client = client;
		this.in = in;
		this.out = out;
	}

	@Override
	public void put(T item) {
		try (Socket client = this.client;
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
		try {
			out.writeObject("get");
			out.flush();
//			out.writeInt(id); 
//			out.flush();
			T item = (T) in.readObject();
			return item;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				wait(100000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
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
