package server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import shared.Pair;

public class Server {
	static ConcurrentHashMap<String, Integer> stocks = new ConcurrentHashMap<>();
	static ConcurrentHashMap<String, Double> stocksChanges = new ConcurrentHashMap<>();
	static ConcurrentHashMap<Integer, List<String>> stocksOn = new ConcurrentHashMap<>();
	static AtomicBoolean needBalancing = new AtomicBoolean(true);
	static AtomicInteger balanceNumber = new AtomicInteger(0);
	static AtomicInteger numberOfStocksServers = new AtomicInteger(0);
	static AtomicInteger lookingFor = new AtomicInteger(0);
	static ConcurrentHashMap<Integer, Pair<ObjectInputStream, ObjectOutputStream>> workerStreamMap = new ConcurrentHashMap<>();
	static ConcurrentLinkedQueue<String> transactionsActive = new ConcurrentLinkedQueue<>();
	static ConcurrentHashMap<Integer, List<String>> transactionsOn = new ConcurrentHashMap<>();
	static ConcurrentHashMap<Integer, Semaphore> serverStockMutex = new ConcurrentHashMap<>();
	static ConcurrentLinkedQueue<String> transactionsFinished = new ConcurrentLinkedQueue<String>();
	static ConcurrentHashMap<Integer, LocalDateTime> transactionTime = new ConcurrentHashMap<Integer, LocalDateTime>();
	private static ServerSocket srv;
	private static int id = 1;
	static AtomicLong x = new AtomicLong();
	static AtomicLong y = new AtomicLong();
	static AtomicLong z = new AtomicLong();
	static Thread t;
	static AtomicBoolean kill = new AtomicBoolean(false);

	public static void main(String[] args) {
		int port = 0;
		initStocks();
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
			x.set(Integer.parseInt(args[1]));
			y.set(Integer.parseInt(args[2]));
			z.set(Integer.parseInt(args[3]));
			if (!(z.get() > 2 * x.get() && x.get() > y.get())) {
				System.err.println("ne vazi relacija  (z > 2*x, x > y)");
				return;
			}

			try (ServerSocket server = new ServerSocket(port)) {
				System.out.println("Sever started...");
				while (true) {
					Socket client = server.accept();
					new WorkingThread(client, id++).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JLabel labelPort = new JLabel("Enter port");
			JTextField portField = new JTextField(20);
			JLabel labelX = new JLabel("Enter x");
			JTextField xField = new JTextField(10);
			JLabel labelY = new JLabel("Enter y");
			JTextField yField = new JTextField(10);
			JLabel labelZ = new JLabel("Enter z");
			JTextField zField = new JTextField(10);
			JButton save = new JButton("save");
			JButton start = new JButton("start");
			JButton stop = new JButton("stop");
			JFrame frame = new JFrame("Server");
			JPanel p = new JPanel();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(600, 130);

			p.add(labelPort);
			p.add(portField);
			p.add(start);
			p.add(stop);
			JPanel p2 = new JPanel();
			p2.add(labelX);
			p2.add(xField);
			p2.add(labelY);
			p2.add(yField);
			p2.add(labelZ);
			p2.add(zField);
			p2.add(save);
			JLabel msg = new JLabel("                  ");
			frame.getContentPane().add(BorderLayout.CENTER, msg);
			frame.getContentPane().add(BorderLayout.NORTH, p);
			frame.getContentPane().add(BorderLayout.SOUTH, p2);
			frame.setVisible(true);

			start.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					t = new Thread(new Runnable() {
						@Override
						public void run() {
							int port = 0;
							try {
								port = Integer.parseInt(portField.getText());
							} catch (Exception e) {
								msg.setText("port must be integers");
								return;
							}
							long xx, yy, zz;
							kill.set(false);
							try {
								xx = Long.parseLong(xField.getText());
								yy = Long.parseLong(yField.getText());
								zz = Long.parseLong(zField.getText());
							} catch (Exception e) {
								msg.setText("x,y,z must be integers");
								return;
							}
							if (!(zz > 2 * xx && xx > yy)) {
								msg.setText("ne vazi relacija  (z > 2*x, x > y)");
								return;
							}
							x.set(xx);
							y.set(yy);
							z.set(zz);
							List<WorkingThread> ww = new LinkedList<>();
							try (ServerSocket server = new ServerSocket(port)) {
								System.out.println("Sever started...");
								msg.setText("Sever started...");
								srv = server;
								while (true) {
									Socket client = server.accept();
									WorkingThread w = new WorkingThread(client, id++);
									w.setDaemon(true);
									ww.add(w);
									w.start();
								}

							} catch (IOException e) {
								e.printStackTrace();
							}
							kill.set(true);
							for (WorkingThread w : ww)
								try {
									w.join();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							// stocks.clear();
							// stocksChanges.clear();
							stocksOn.clear();
							needBalancing.set(true);
							balanceNumber.set(0);
							numberOfStocksServers.set(0);
							lookingFor.set(0);
							workerStreamMap.clear();
							// transactionsActive.clear();
							transactionsOn.clear();
							serverStockMutex.clear();
							// transactionsFinished.clear();
							// transactionTime.clear();
							id = 1;
						}
					});
					t.start();
				}
			});
			stop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						srv.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						t.join();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					msg.setText("Sever stoped...");
				}
			});
			save.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					long xx, yy, zz;
					try {
						xx = Long.parseLong(xField.getText());
						yy = Long.parseLong(yField.getText());
						zz = Long.parseLong(zField.getText());
					} catch (Exception ex) {
						msg.setText("x,y,z must be integers");
						return;
					}
					if (!(zz > 2 * xx && xx > yy)) {
						msg.setText("ne vazi relacija  (z > 2*x, x > y)");
						return;
					}
					x.set(xx);
					y.set(yy);
					z.set(zz);
				}
			});

		}
	}

	private static void initStocks() {
		try {
			File myObj = new File("stocks.txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String[] data = myReader.nextLine().split(";");
				stocks.put(data[0], Integer.parseInt(data[1]));
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}
}
