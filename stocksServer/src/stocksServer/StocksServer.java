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
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class StocksServer {

	static int port;
	static String host;
	static WorkingThread wt;

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
		JButton connect = new JButton("Connect to server");
		JButton disconnect = new JButton("Disconnected from server");
		disconnect.setEnabled(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(labelHost); // Components Added using Flow Layout
		panel.add(hostField);
		panel.add(labelPort);
		panel.add(portField);
		panel.add(connect);
		panel.add(disconnect);

		// Text Area at the Center
		JTextArea ta = new JTextArea();
		ta.setEditable(false);

		// Adding Components to the frame.
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		frame.getContentPane().add(BorderLayout.CENTER, ta);
		frame.setVisible(true);
		AtomicBoolean kill = new AtomicBoolean(false);
		connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				host = hostField.getText();
				try {
					port = Integer.parseInt(portField.getText());
				} catch (Exception ex) {
					ta.setForeground(Color.RED);
					ta.setText("Port must be integer");
					System.out.println("Greska");
					return;
				}
				ta.setForeground(Color.BLACK);
				JButton b = (JButton) e.getSource();
				b.setText("connected to server");
				disconnect.setText("disconnect from server");
				b.setEnabled(false);
				disconnect.setEnabled(true);
				System.out.println(host + " " + port);
				wt = new WorkingThread(host, port, ta, connect, disconnect, kill);
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
				ta.setForeground(Color.RED);
				ta.setText("connection lost");
				connect.setText("connect to server");
				connect.setEnabled(true);
				disconnect.setText("disconnected from server");
				disconnect.setEnabled(false);
			}
		});

	}

}
