package stocksServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import shared.StocksMessage;

public class GetWorker implements Worker {

	@Override
	public HashMap<String, Integer> work(ObjectOutputStream out, ObjectInputStream in, HashMap<String, Integer> map)
			throws IOException, Exception {
		StocksMessage stocks = new StocksMessage(map);
		out.writeObject(stocks);
		return map;
	}

}
