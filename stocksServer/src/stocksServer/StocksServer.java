package stocksServer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class StocksServer {

	static int port;
	static String host;
	static WorkingThread wt;
	static JTextArea transactionArea = new JTextArea();
	static JTextArea stockArea = new JTextArea();

	public static void main(String[] args) {
		port = Integer.parseInt(args[0]);
		host = args[1];
		// Creating the Frame
		JFrame frame = new JFrame("Stocks Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 600);

		// Creating the port and host text filed
		JPanel panel = new JPanel();
		JLabel labelHost = new JLabel("Enter host address");
		JTextField hostField = new JTextField(20);
		JLabel labelPort = new JLabel("Enter host port");
		JTextField portField = new JTextField(20);
		JLabel usernameLabel = new JLabel("Enter username");
		JLabel passwordLabel = new JLabel("Enter password");
		JTextField uField = new JTextField(20);
		JPasswordField pField = new JPasswordField(20);
		JButton connect = new JButton("Connect to server");
		JButton disconnect = new JButton("Disconnected from server");
		disconnect.setEnabled(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel p1 = new JPanel();
		p1.add(labelHost);
		p1.add(hostField);
		JPanel p2 = new JPanel();
		p2.add(labelPort);
		p2.add(portField);
		JPanel p3 = new JPanel();
		p3.add(usernameLabel);
		p3.add(uField);
		JPanel p4 = new JPanel();
		p4.add(passwordLabel);
		p4.add(pField);
		JPanel p5 = new JPanel();
		p5.add(connect);
		p5.add(disconnect);
		panel.add(p1); // Components Added using Flow Layout
		panel.add(p2);
		panel.add(p3);
		panel.add(p4);
		panel.add(p5);
//		panel.add(disconnect);

		// Text Area at the Center
		stockArea.setEditable(false);
		transactionArea.setEditable(false);
		// Adding Components to the frame.
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BoxLayout(textAreaPanel, BoxLayout.X_AXIS));
		textAreaPanel.add(stockArea);
		textAreaPanel.add(transactionArea);
		JScrollPane scrollT = new JScrollPane(transactionArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane scrollS = new JScrollPane(stockArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textAreaPanel.add(scrollT);
		textAreaPanel.add(scrollS);
		frame.getContentPane().add(BorderLayout.CENTER, textAreaPanel);
		frame.setVisible(true);
		AtomicBoolean kill = new AtomicBoolean(false);
		connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				host = hostField.getText();
				try {
					port = Integer.parseInt(portField.getText());
				} catch (Exception ex) {
					stockArea.setForeground(Color.RED);
					stockArea.setText("Port must be integer");
					System.out.println("Greska");
					return;
				}
				stockArea.setForeground(Color.BLACK);
				JButton b = (JButton) e.getSource();
				b.setText("connected to server");
				disconnect.setText("disconnect from server");
				b.setEnabled(false);
				disconnect.setEnabled(true);
				System.out.println(host + " " + port);
				wt = new WorkingThread(host, port, connect, disconnect, kill);
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
				stockArea.setForeground(Color.RED);
				stockArea.setText("connection lost");
				connect.setText("connect to server");
				connect.setEnabled(true);
				disconnect.setText("disconnected from server");
				disconnect.setEnabled(false);
			}
		});

	}

}
