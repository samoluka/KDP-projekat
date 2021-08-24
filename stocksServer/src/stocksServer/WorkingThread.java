package stocksServer;

import static stocksServer.StocksServer.stockArea;
import static stocksServer.StocksServer.transactionArea;

import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;

import shared.StocksMessage;
import shared.TextMessage;;

public class WorkingThread extends Thread {

	private String host;
	private int port;
	private JButton b;
	private JButton d;
	private AtomicBoolean kill;

	private static String transactionMsg;
	private HashMap<String, Integer> stocks = new HashMap<>();
	private List<String> transactions = new LinkedList<>();

	public WorkingThread(String host, int port, JButton b, JButton disconnect, AtomicBoolean kill) {
		this.host = host;
		this.port = port;
		this.b = b;
		this.d = disconnect;
		this.kill = kill;
	}

	@Override
	public void run() {
		try (Socket socket = new Socket(host, port);
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());) {
			TextMessage msg = new TextMessage("server.StocksServer");
			out.writeObject(msg);
			StocksMessage stocksMsg = new StocksMessage(stocks);

			while (true) {
				if (this.kill.get()) {
					return;
				}
				String operation = (String) in.readObject();
//				Worker w = (Worker) Class.forName(className).getConstructor().newInstance();
//				this.stocks = w.work(out, in, this.stocks);

				switch (operation) {
				case "set":
					stocksMsg = (StocksMessage) in.readObject();
					out.writeObject("OK DONE");
					stocks = stocksMsg.getBody();
					updateTextArea();
					break;
				case "get":
					stocksMsg.setBody(stocks);
					out.writeObject(stocksMsg);
					String code = (String) in.readObject();
					if (!"OK DONE".equals(code)) {
						System.err.println("NESTO NIJE OK PREKIDAM PROGRAM");
						return;
					}
					// updateTextArea();
					break;
				case "buy":
				case "sell":
					transactionMsg = (String) in.readObject();
					out.writeObject("OK DONE");
					out.flush();
					transactions.add(transactionMsg);
					updateTransactionArea();
					break;
				case "cancel":
					String[] transactionInfo = ((String) in.readObject()).split(";");
					transactionMsg = transactionInfo[1] + ";" + transactionInfo[2] + ";" + transactionInfo[3] + ";"
							+ transactionInfo[5];
					out.writeObject("OK DONE");
					out.flush();
					transactions.removeIf((s) -> s.equals(transactionMsg));
					updateTransactionArea();
					break;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			stockArea.setForeground(Color.RED);
			stockArea.setText("no connect to server");
			b.setText("connect to server");
			b.setEnabled(true);
			d.setText("disconnected from server");
			d.setEnabled(false);
			return;
		}
	}

	private void updateTextArea() {
		StringBuilder sb = new StringBuilder();
		Date date = new Date();
		// System.out.println("Vreme: " + new Timestamp(date.getTime()) + "\n");
		sb.append("Vreme: " + new Timestamp(date.getTime()) + "\n");
		for (Entry<String, Integer> pair : this.stocks.entrySet()) {
			String key = pair.getKey();
			Integer value = pair.getValue();
			sb.append(key + " " + value + "\n");
			// System.out.println(key + " " + value);
		}
		stockArea.setText(sb.toString());
	}

	private void updateTransactionArea() {
		StringBuilder sb = new StringBuilder();
		for (String s : transactions) {
			sb.append(s + "\n");
		}
		transactionArea.setText(sb.toString());
	}

}
