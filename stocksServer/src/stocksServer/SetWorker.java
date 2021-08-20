package stocksServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import shared.StocksMessage;

public class SetWorker implements Worker {

	@Override
	public HashMap<String, Integer> work(ObjectOutputStream out, ObjectInputStream in, HashMap<String, Integer> map)
			throws IOException, Exception {
		StocksMessage stocks = (StocksMessage) in.readObject();
		System.out.println("Primio: " + stocks.toString());
		return stocks.getBody();
	}

}
