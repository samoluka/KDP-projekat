package client;

import static client.Client.connect;
import static client.Client.disconnect;
import static client.Client.kill;
import static client.Client.ta;

import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import shared.SocketAtomicBroadcastBuffer;
import shared.TextMessage;

public class WorkingThread extends Thread {

	private SocketAtomicBroadcastBuffer<String> sharedBuff;
	private String host;
	private int port;
	private ConcurrentHashMap<String, Integer> stockPrice = new ConcurrentHashMap<String, Integer>();
	private Semaphore updateStocks = new Semaphore(0);
	private Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
			StringBuilder sb = new StringBuilder();
			while (true) {
				try {
					updateStocks.acquire();
					if (kill.get())
						return;
					ta.setText("Trenutno vreme: " + LocalDateTime.now().toString() + "\n");
					sb.setLength(0);
					for (Entry<String, Integer> e : stockPrice.entrySet()) {
						sb.append(e.getKey() + " " + e.getValue() + '\n');
					}
					ta.setText(ta.getText() + sb.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("GASIM");
					return;
				}
			}
		}
	});

//	public WorkingThread(Socket server) {
//		this.server = server;
//	}

	public WorkingThread(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void run() {
		t.start();
		System.out.println("KILL " + kill.get());
		try (Socket socket = new Socket(host, port);
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());) {
			TextMessage msg = new TextMessage("server.Client");
			out.writeObject(msg);
			int id = in.readInt();
			System.out.println("Moj id je " + id);
			sharedBuff = new SocketAtomicBroadcastBuffer<>(socket, in, out);
			while (!kill.get()) {
				System.out.println("CEKAM PORUKU");
				String[] stockInfo = sharedBuff.get(id).split(";");
				stockPrice.put(stockInfo[0], Integer.parseInt(stockInfo[1]));
				updateStocks.release();
				System.out.println("JAVLJAM");
			}
		} catch (Exception e) {
			e.printStackTrace();
			ta.setForeground(Color.RED);
			ta.setText("no connect to server");
			connect.setText("connect to server");
			connect.setEnabled(true);
			disconnect.setText("disconnected from server");
			disconnect.setEnabled(false);
			return;
		}
	}

}
