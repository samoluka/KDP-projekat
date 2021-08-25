package stocksServer;

import static stocksServer.StocksServer.stockArea;
import static stocksServer.StocksServer.transactionArea;

import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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

	private HashMap<String, List<String>> transactionsBuy = new HashMap<>();
	private HashMap<String, List<String>> transactionsSell = new HashMap<>();

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
					String transactionsMsg = (String) in.readObject();
					out.writeObject("OK DONE");
					stocks = stocksMsg.getBody();
					updateTransactions(transactionsMsg);
					updateTextArea();
					updateTransactionArea();
					break;
				case "get":
					StringBuilder sb = new StringBuilder();
					for (Entry<String, Integer> pair : stocks.entrySet()) {
						String key = pair.getKey();
						Integer value = pair.getValue();
						sb.append(key + ";" + value + "\t");
					}
					out.writeObject(sb.toString());
					out.flush();
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
					// transactions.add(transactionMsg);
					addTransaction(transactionMsg);
					String str = findTransactions(transactionMsg);
					updateValues(transactionMsg);
					remove(transactionMsg, str);
					out.writeObject(str);
					out.writeObject("OK DONE");
					out.flush();
					updateTransactionArea();
					updateTextArea();
					break;
				case "cancel":
					int idT = in.readInt();
					out.writeObject("OK DONE");
					out.flush();
					removeTransaction(idT);
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

	private void updateValues(String transactionsMsg2) {
		if (transactionsMsg2 == null) {
			return;
		}
		String[] trans = transactionsMsg2.split(";");
		Integer newValue = (int) (stocks.get(trans[1]) * 1.1);
		stocks.put(trans[1], newValue);

	}

	private void remove(String transactionMsg2, String str) {
		if (str == "") {
			return;
		}
		String[] ids = str.split(";");
		String[] t = transactionMsg2.split(";");
		Set<String> idSet = Arrays.asList(ids).stream().collect(Collectors.toSet());
		List<String> l = transactionsBuy.get(t[1]);
		if (l != null)
			l.removeIf((String s) -> s.split(";").length > 2 && idSet.contains(s.split(";")[2]));
		transactionsBuy.put(t[1], l);
		l = transactionsSell.get(t[1]);
		if (l != null)
			l.removeIf((String s) -> s.split(";").length > 2 && idSet.contains(s.split(";")[2]));
		transactionsSell.put(t[1], l);
	}

	private void removeTransaction(int idT) {
		for (Entry<String, List<String>> e : transactionsBuy.entrySet()) {
			List<String> l = e.getValue();
			for (String s : l) {
				if (s.split(";").length > 2 && Integer.parseInt(s.split(";")[2]) == idT) {
					l.remove(s);
					transactionsBuy.put(e.getKey(), l);
					return;
				}
			}
		}
		for (Entry<String, List<String>> e : transactionsSell.entrySet()) {
			List<String> l = e.getValue();
			for (String s : l) {
				if (s.split(";").length > 2 && Integer.parseInt(s.split(";")[2]) == idT) {
					l.remove(s);
					transactionsSell.put(e.getKey(), l);
					return;
				}
			}
		}
	}

	private void addTransaction(String transactionMsg2) {
		if (transactionMsg2 == null) {
			return;
		}
		String[] trans = transactionMsg2.split(";");
		if (trans.length > 5) {
			List<String> l;
			HashMap<String, List<String>> tMap = null;
			if (trans[0].equals("sell"))
				tMap = transactionsSell;
			else if (trans[0].equals("buy"))
				tMap = transactionsBuy;
			if (tMap != null) {
				l = tMap.get(trans[1]);
				if (l == null)
					l = new LinkedList<>();
				l.add(trans[2] + ";" + trans[3] + ";" + trans[4] + ";" + trans[5]);
				tMap.put(trans[1], l);
			}
		}
	}

	private String findTransactions(String transaction) {
//		while (!transactions.isEmpty() && transactions.get(0).split(";").length < 4) {
//			transactions.remove(0);
//		}
//		if (!transactions.isEmpty() && transactions.get(0).split(";").length > 4) {
//			String str = transactions.get(0).split(";")[4];
//			transactions.remove(0);
//			System.out.println("ukranjam " + str);
//			return str;
//		}
		if (transaction == null) {
			return "";
		}
		String[] trans = transaction.split(";");
		if (trans.length > 5) {
			HashMap<String, List<String>> tMap = null;
			if (trans[0].equals("sell")) {
				tMap = transactionsBuy;
				List<String> l = tMap.get(trans[1]);
				if (l == null)
					return "";
				String[] arr = new String[l.size()];
				l.toArray(arr);
				if (arr.length > 0) {
					int price = Integer.parseInt(trans[3]);
					int n = Integer.parseInt(trans[2]);
					String str = findBuyers(arr, 0, 0, 0, price, n);
					if (str != "") {
						str += ";" + trans[4];
					} else {
						List<String> l2 = transactionsSell.get(trans[1]);
						if (l == null)
							return "";
						String[] arr2 = new String[l2.size()];
						l2.toArray(arr2);
						for (String s : arr) {
							String[] t = s.split(";");
							if (t.length > 2) {
								price = Integer.parseInt(t[1]);
								n = Integer.parseInt(t[0]);
								str = findSellers(arr2, 0, 0, 0, price, n);
								if (str != "") {
									str += ";" + t[2];
									break;
								}
							}
						}
					}
					return str;
				}
			} else if (trans[0].equals("buy")) {
				tMap = transactionsSell;
				List<String> l = tMap.get(trans[1]);
				if (l == null)
					return "";
				String[] arr = new String[l.size()];
				l.toArray(arr);
				if (arr.length > 0) {
					int price = Integer.parseInt(trans[3]);
					int n = Integer.parseInt(trans[2]);
					String str = findSellers(arr, 0, 0, 0, price, n);
					if (str != "") {
						str += ";" + trans[4];
					} else {
						List<String> l2 = transactionsBuy.get(trans[1]);
						if (l == null)
							return "";
						String[] arr2 = new String[l2.size()];
						l2.toArray(arr2);
						for (String s : arr) {
							String[] t = s.split(";");
							if (t.length > 2) {
								price = Integer.parseInt(t[1]);
								n = Integer.parseInt(t[0]);
								str = findBuyers(arr2, 0, 0, 0, price, n);
								if (str != "") {
									str += ";" + t[2];
									break;
								}
							}
						}
					}
					return str;
				}
			}
		}
		return "";

	}

	private String findBuyers(String[] l, int ind, int currPrice, int currN, int minPrice, int maxN) {
		String ret = "";
		if (ind > l.length - 1) {
			return ret;
		}
		int elemPrice = Integer.parseInt(l[ind].split(";")[1]);
		int elemN = Integer.parseInt(l[ind].split(";")[0]);
		if (currPrice + elemPrice >= minPrice && currN + elemN <= maxN) {
			return l[ind].split(";")[2];
		}
		if (currPrice + elemPrice < minPrice && currN + elemN < maxN) {
			String subRet = findBuyers(l, ind + 1, currPrice + elemPrice, currN + elemN, minPrice, maxN);
			if (subRet != "") {
				return l[ind].split(";")[2] + ";" + subRet;
			}
		}
		return findBuyers(l, ind + 1, currPrice, currN, minPrice, maxN);
	}

	private String findSellers(String[] l, int ind, int currPrice, int currN, int maxPrice, int minN) {
		String ret = "";
		if (ind > l.length - 1) {
			return ret;
		}
		int elemPrice = Integer.parseInt(l[ind].split(";")[1]);
		int elemN = Integer.parseInt(l[ind].split(";")[0]);
		if (currPrice + elemPrice <= maxPrice && currN + elemN >= minN) {
			return l[ind].split(";")[2];
		}
		if (currPrice + elemPrice < maxPrice && currN + elemN < minN) {
			String subRet = findSellers(l, ind + 1, currPrice + elemPrice, currN + elemN, maxPrice, minN);
			if (subRet != "") {
				return l[ind].split(";")[2] + ";" + subRet;
			}
		}
		return findBuyers(l, ind + 1, currPrice, currN, maxPrice, minN);
	}

	private void updateTransactions(String t) {
		if (t == null) {
			return;
		}
		String[] allTransactions = t.split("\t");
		for (String tr : allTransactions)
			addTransaction(tr);
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
		sb.append("Sell transactions:\n");
		for (Entry<String, List<String>> e : transactionsSell.entrySet()) {
			List<String> l = e.getValue();
			for (String s : l) {
				sb.append(e.getKey() + ";" + s + "\n");
			}
		}
		sb.append("Buy transactions:\n");
		for (Entry<String, List<String>> e : transactionsBuy.entrySet()) {
			List<String> l = e.getValue();
			for (String s : l) {
				sb.append(e.getKey() + ";" + s + "\n");
			}
		}
		transactionArea.setText(sb.toString());
	}

}
