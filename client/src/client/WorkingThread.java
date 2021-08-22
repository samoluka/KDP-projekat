package client;

import static client.Client.action;
import static client.Client.activeTransaction;
import static client.Client.buy;
import static client.Client.cancel;
import static client.Client.connect;
import static client.Client.disconnect;
import static client.Client.kill;
import static client.Client.msgLabel;
import static client.Client.sell;
import static client.Client.stockField;
import static client.Client.ta;

import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import shared.SocketAtomicBroadcastBuffer;
import shared.StocksMessage;
import shared.TextMessage;

public class WorkingThread extends Thread {

	private SocketAtomicBroadcastBuffer<String> sharedBuff;
	private String host;
	private int port;
	private HashMap<String, Integer> stockPrice = new HashMap<String, Integer>();
	private Semaphore updateStocks = new Semaphore(1);
	private Semaphore updateTextArea = new Semaphore(0);
	private int id;

	private Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
			StringBuilder sb = new StringBuilder();
			while (true) {
				try {
					updateTextArea.acquire();
					if (kill.get())
						return;
					ta.setText("Trenutno vreme: " + LocalDateTime.now().toString() + "\n" + "moj id je: " + id + "\n");
					sb.setLength(0);
					for (Entry<String, Integer> e : stockPrice.entrySet()) {
						sb.append(e.getKey() + " " + e.getValue() + '\n');
					}
					ta.setText(ta.getText() + sb.toString());
					updateStocks.release();
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
			id = in.readInt();
			// System.out.println("Moj id je " + id);
			sharedBuff = new SocketAtomicBroadcastBuffer<>(socket, in, out);
			while (!kill.get()) {
				// System.out.println("CEKAM PORUKU");
				switch (action) {
				case "sell":
				case "buy":
					String stock = stockField.getText();
					out.writeObject(action);
					out.writeObject(stock);
					out.flush();
					action = "";
					break;
				case "cancel":
					out.writeObject(action);
					out.flush();
					action = "";
					break;
				}
				String code = (String) in.readObject();
				switch (code) {
				case "stocks":
					StocksMessage sm = (StocksMessage) in.readObject();
					updateStocks.acquire();
					stockPrice = sm.getBody();
					updateTextArea.release();
					break;
				case "Canceled":
					msgLabel.setText("Transaction canceled");
					activeTransaction.set(false);
					buy.setEnabled(true);
					sell.setEnabled(true);
					stockField.setEnabled(true);
					break;
				default:
					msgLabel.setText(code);
					activeTransaction.set(true);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ta.setForeground(Color.RED);
			ta.setText("no connect to server");
			connect.setText("connect to server");
			connect.setEnabled(true);
			disconnect.setText("disconnected from server");
			disconnect.setEnabled(false);
			buy.setEnabled(false);
			sell.setEnabled(false);
			cancel.setEnabled(false);
			stockField.setEnabled(false);
			activeTransaction.set(false);
			return;
		}
	}

}
