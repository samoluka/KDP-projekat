package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import shared.MonitorAtomicBroadcastBuffer;
import shared.Pair;

public class Server {
	static ConcurrentHashMap<String, Integer> stocks = new ConcurrentHashMap<>();
	static ConcurrentHashMap<Integer, List<String>> stocksOn = new ConcurrentHashMap<>();
	static AtomicBoolean needBalancing = new AtomicBoolean(true);
	static AtomicInteger balanceNumber = new AtomicInteger(0);
	static AtomicInteger numberOfStocksServers = new AtomicInteger(0);
	static ConcurrentHashMap<Integer, AtomicBoolean> needUpdate = new ConcurrentHashMap<>();
	static MonitorAtomicBroadcastBuffer<String> buff = new MonitorAtomicBroadcastBuffer<>(1000);
	static AtomicInteger lookingFor = new AtomicInteger(0);
	static ConcurrentHashMap<Integer, Pair<ObjectInputStream, ObjectOutputStream>> workerStreamMap = new ConcurrentHashMap<>();
	static ConcurrentHashMap<Integer, String> transactionsActive = new ConcurrentHashMap<>();
	static ConcurrentHashMap<Integer, Semaphore> serverStockMutex = new ConcurrentHashMap<>();
	private static int id = 1;

	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);

		stocks.put("Stock A", 1000);
		stocks.put("Stock B", 2000);
		stocks.put("Stock C", 3000);
		stocks.put("Stock D", 4000);

		try (ServerSocket server = new ServerSocket(port)) {
			System.out.println("Sever started...");
			UpdateStocks us = new UpdateStocks(needBalancing, stocksOn, stocks, null, null, 0, buff);
			us.start();
			while (true) {
				Socket client = server.accept();
				new WorkingThread(client, id++).start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
