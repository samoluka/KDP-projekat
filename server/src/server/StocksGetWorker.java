package server;

import static server.Server.numberOfStocksServers;
import static server.Server.stocks;
import static server.Server.stocksChanges;
import static server.Server.x;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import shared.ChangeMessage;
import shared.StocksMessage;

public class StocksGetWorker implements Worker {

	@Override
	public void work(Socket client, ObjectOutputStream out, ObjectInputStream in, int id)
			throws IOException, Exception {
		while (true) {
			try {
				if (numberOfStocksServers.get() == 0) {
					out.writeObject("noStocks");
					out.flush();
				} else {
					out.writeObject("stocks");
					out.flush();
					HashMap<String, Integer> hs = new HashMap<>();
					hs.putAll(stocks);
					StocksMessage sm1 = new StocksMessage(hs);
					HashMap<String, Double> cs = new HashMap<>();
					cs.putAll(stocksChanges);
					ChangeMessage sm2 = new ChangeMessage(cs);
					out.writeObject(sm1);
					out.writeObject(sm2);
					out.flush();
				}
				Thread.sleep(x.get());
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				return;
			}
		}
	}
}
