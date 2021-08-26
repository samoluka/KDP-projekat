package client;

import static client.Client.action;
import static client.Client.activeTransaction;
import static client.Client.buy;
import static client.Client.cancel;
import static client.Client.connect;
import static client.Client.disconnect;
import static client.Client.kill;
import static client.Client.msgLabel;
import static client.Client.pField;
import static client.Client.qField;
import static client.Client.refreshTransaction;
import static client.Client.sa;
import static client.Client.sell;
import static client.Client.status;
import static client.Client.stockCancelField;
import static client.Client.stockField;
import static client.Client.ta;
import static client.Client.usernameField;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import shared.ChangeMessage;
import shared.StocksMessage;
import shared.TextMessage;

public class WorkingThread extends Thread {

	private String host;
	private int port;
	private HashMap<String, Integer> stockPrice = new HashMap<String, Integer>();
	private HashMap<String, Double> stockChange = new HashMap<>();
	private Semaphore updateStocks = new Semaphore(1);
	private Semaphore updateTextArea = new Semaphore(0);
	private int id = 0;

	private Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
			StringBuilder sb = new StringBuilder();
			while (true) {
				try {
					updateTextArea.acquire();
					if (kill.get())
						return;
					sb.setLength(0);
					sb.append("Update time: " + LocalDateTime.now().toString() + "\n");
					for (Entry<String, Integer> e : stockPrice.entrySet()) {
						Double d = stockChange.get(e.getKey());
						if (d == null)
							d = (double) 0;
						sb.append(e.getKey() + " " + e.getValue() + " " + d + '\n');
					}
					sa.setText(sb.toString());
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
			this.id = in.readInt();
			out.writeObject(usernameField.getText());
			out.flush();
			while (!kill.get()) {
				// System.out.println("CEKAM PORUKU");
				switch (action) {
				case "sell":
				case "buy":
					String stock = stockField.getText() + ";" + qField.getText() + ";" + pField.getText();
					out.writeObject(action);
					out.writeObject(stock);
					out.flush();
					action = "";
					break;
				case "cancel":
					out.writeObject(action);
					out.flush();
					out.writeInt(Integer.parseInt(stockCancelField.getText()));
					out.flush();
					action = "";
					break;
				case "refreshtransaction":
					out.writeObject("refresh");
					out.flush();
					action = "";
					break;
				case "status":
					out.writeObject(action);
					out.writeInt(Integer.parseInt(stockCancelField.getText()));
					out.flush();
					action = "";
				}

				String code = (String) in.readObject();
				switch (code) {
				case "stocks":
					StocksMessage sm = (StocksMessage) in.readObject();
					ChangeMessage cm = (ChangeMessage) in.readObject();
					updateStocks.acquire();
					stockPrice = sm.getBody();
					stockChange = cm.getBody();
					updateTextArea.release();
					msgLabel.setText("");
					break;
				case "Canceled":
					msgLabel.setText("Transaction canceled");
					System.out.println("Transaction canceled");
					activeTransaction.set(false);
					break;
				case "transactions":
					String trA = (String) in.readObject();
					String trF = (String) in.readObject();
					out.writeObject("OK DONE");
					out.flush();
					updateTransactionArea(trA.split("\t"), trF.split("\t"));
					break;
				case "status":
					Integer status = in.readInt();
					msgLabel.setText(status.toString());
					System.out.println(status.toString());
					break;
				case "noStocks":
					msgLabel.setText("Server is currently down");
					System.out.println("Server is currently down");
					action = "";
					break;
				default:
					msgLabel.setText(code);
					System.out.println(code);
					activeTransaction.set(true);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Wsa.setForeground(Color.RED);
			sa.setText("no connect to server");
			connect.setText("connect to server");
			connect.setEnabled(true);
			disconnect.setText("disconnected from server");
			disconnect.setEnabled(false);
			buy.setEnabled(false);
			sell.setEnabled(false);
			cancel.setEnabled(false);
			refreshTransaction.setEnabled(false);
			stockField.setEnabled(false);
			qField.setEnabled(false);
			pField.setEnabled(false);
			status.setEnabled(false);
			stockCancelField.setEditable(false);
			ta.setText("");
			activeTransaction.set(false);
			return;
		}
	}

	private void updateTransactionArea(String[] trA, String[] trF) {
		StringBuilder sb = new StringBuilder();
		sb.append("Update time: " + LocalDateTime.now().toString() + "\n");
		sb.append("type;stock name;quantity;price;id;user\n");
		sb.append("Active transactions: \n");
		for (String s : trA) {
			sb.append(s + "\n");
		}
		sb.append("Finished transactions: \n");
		for (String s : trF) {
			sb.append(s + "\n");
		}
		ta.setText(sb.toString());
		System.out.println(sb.toString());
	}

}
