package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
	private static int port;
	private static String host;
	private static WorkingThread wt;
	static JButton connect = new JButton("Connect to server");
	static JButton disconnect = new JButton("Disconnected from server");
	static JButton sell = new JButton("Sell");
	static JButton buy = new JButton("Buy");
	static JButton cancel = new JButton("cancel");
	static JButton status = new JButton("status");
	static JTextField stockField = new JTextField(20);
	static JTextField qField = new JTextField(20);
	static JTextField pField = new JTextField(20);
	static JTextArea sa = new JTextArea();
	static JTextArea ta = new JTextArea();
	static AtomicBoolean kill = new AtomicBoolean(false);
	static Semaphore mutex = new Semaphore(1);
	static String action = "";
	static LocalDateTime lastTransactionTime;
	static AtomicBoolean activeTransaction = new AtomicBoolean(false);
	static JLabel msgLabel = new JLabel("no active transaction");
	static JTextField stockCancelField = new JTextField(5);
	static JTextField usernameField = new JTextField(20);
	static JButton refreshTransaction = new JButton("refreshTransaction");
	static int z = 5000;

//	public static void main(String[] args) {
//		port = Integer.parseInt(args[0]);
//		host = args[1];
//		try (Socket server = new Socket(host, port);) { 
//			WorkingThread wt = new WorkingThread(server);
//			wt.run();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	private static void click(String a) {
		action = a;
		// sell.setEnabled(false);
		// buy.setEnabled(false);
		// stockField.setEnabled(false);
//		cancel.setEnabled(false);
//		lastTransactionTime = LocalDateTime.now();
//		Timer timer = new Timer(z, new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				cancel.setEnabled(true);
//			}
//		});
//		timer.setRepeats(false);
//		timer.start();
	}

	public static void main(String[] args) {
		port = Integer.parseInt(args[0]);
		host = args[1];
		// Creating the Frame
		JFrame frame = new JFrame("Stocks client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 600);

		// Creating the port and host text filed
		JPanel panel = new JPanel();
		JLabel labelHost = new JLabel("Enter host address");
		JTextField hostField = new JTextField(20);
		JLabel labelPort = new JLabel("Enter host port");
		JTextField portField = new JTextField(20);
		JLabel labelStock = new JLabel("Enter stock");
		JLabel labelQ = new JLabel("Enter quantity");
		JLabel labelP = new JLabel("Enter price");
		JLabel labelStatus = new JLabel("Enter transaction Id to get status or cancel the transaction");
		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		JPanel p3 = new JPanel();
		JPanel p4 = new JPanel();
		JPanel p5 = new JPanel();
		JPanel p6 = new JPanel();
		JPanel p7 = new JPanel();
		JPanel p9 = new JPanel();
		JPanel p8 = new JPanel();
		JPanel p10 = new JPanel();
		JPanel p11 = new JPanel();
		p1.add(labelHost);
		p1.add(hostField);
		p2.add(labelPort);
		p2.add(portField);
		p11.add(new JLabel("Enter password"));
		p7.add(new JLabel("Enter username"));
		p7.add(usernameField);
		p11.add(new JPasswordField(20));
		p3.add(connect);
		p3.add(disconnect);
		p4.add(labelStock);
		p4.add(stockField);
		p8.add(labelP);
		p8.add(pField);
		p9.add(labelQ);
		p9.add(qField);
		p5.add(sell);
		p5.add(buy);
		p5.add(refreshTransaction);
		p6.add(labelStatus);
		p6.add(stockCancelField);
		p10.add(cancel);
		p10.add(status);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(p1);
		panel.add(p2);
		panel.add(p7);
		panel.add(p11);
		panel.add(p3);
		panel.add(p4);
		panel.add(p8);
		panel.add(p9);
		panel.add(p5);
		panel.add(p6);
		panel.add(p10);
		panel.add(msgLabel);

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
		// Text Area at the Center
		// ta.setEditable(false);
		sa.setEditable(false);
		// Adding Components to the frame.
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BoxLayout(textAreaPanel, BoxLayout.X_AXIS));
		textAreaPanel.add(sa);
		textAreaPanel.add(ta);
		JScrollPane scrollT = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane scrollS = new JScrollPane(sa, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textAreaPanel.add(scrollT);
		textAreaPanel.add(scrollS);
		frame.getContentPane().add(BorderLayout.CENTER, textAreaPanel);
		frame.setVisible(true);
		ta.setVisible(true);

		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				host = hostField.getText();
				try {
					port = Integer.parseInt(portField.getText());
				} catch (Exception ex) {
					// ta.setForeground(Color.RED);
					sa.setText("Port must be integer");
					System.out.println("Greska");
					return;
				}
				// ta.setForeground(Color.BLACK);
				JButton b = (JButton) e.getSource();
				b.setText("connected to server");
				disconnect.setText("disconnect from server");
				b.setEnabled(false);
				disconnect.setEnabled(true);
				buy.setEnabled(true);
				sell.setEnabled(true);
				cancel.setEnabled(true);
				refreshTransaction.setEnabled(true);
				stockField.setEnabled(true);
				qField.setEnabled(true);
				pField.setEnabled(true);
				status.setEnabled(true);
				stockCancelField.setEditable(true);
				sa.setText("");
				ta.setText("");
				System.out.println(host + " " + port);
				wt = new WorkingThread(host, port);
				wt.start();
			}
		});
		disconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				kill.set(true);
				System.out.println("SETOVAO KILL");
				try {
					wt.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				kill.set(false);
				// sa.setForeground(Color.RED);
				sa.setText("connection lost");
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
				// sa.setText("");
				ta.setText("");
				stockCancelField.setEditable(false);
				activeTransaction.set(false);
			}
		});

		buy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				click(((JButton) e.getSource()).getText().toLowerCase());
			}
		});
		sell.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				click(((JButton) e.getSource()).getText().toLowerCase());
			}
		});
		refreshTransaction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				click(((JButton) e.getSource()).getText().toLowerCase());
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				if (activeTransaction.get())
				action = "cancel";
			}
		});
		status.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				if (activeTransaction.get())
				action = "status";
			}
		});

	}
}
