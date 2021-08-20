package stocksServer;

import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JTextArea;

import shared.TextMessage;

public class WorkingThread extends Thread {

	private String host;
	private int port;
	private JTextArea ta;
	private JButton b;
	private JButton d;
	private AtomicBoolean kill;

	private HashMap<String, Integer> stocks = new HashMap<>();

	public WorkingThread(String host, int port, JTextArea ta, JButton b, JButton disconnect, AtomicBoolean kill) {
		this.host = host;
		this.port = port;
		this.ta = ta;
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
			while (true) {
				if (this.kill.get()) {
					return;
				}
				String className = ((TextMessage) in.readObject()).getBody();
				Worker w = (Worker) Class.forName(className).getConstructor().newInstance();
				this.stocks = w.work(out, in, this.stocks);
				TextMessage okmsg = new TextMessage("OK DONE");
				out.writeObject(okmsg);
				ta.setText("");
				for (Entry<String, Integer> pair : this.stocks.entrySet()) {
					String key = pair.getKey();
					Integer value = pair.getValue();
					ta.setText(ta.getText() + "\n" + key + " " + value + "\n");
					System.out.println(key + "\n" + value);
					Date date = new Date();
					ta.setText(ta.getText() + "\n" + "Vreme: " + new Timestamp(date.getTime()));
					System.out.println("Vreme: " + new Timestamp(date.getTime()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ta.setForeground(Color.RED);
			ta.setText("no connect to server");
			b.setText("connect to server");
			b.setEnabled(true);
			d.setText("disconnected from server");
			d.setEnabled(false);
			return;
		}
	}

}
