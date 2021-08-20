package shared;

import java.util.HashMap;
import java.util.Map.Entry;

public class StocksMessage implements Message<HashMap<String, Integer>> {

	private HashMap<String, Integer> map;

	public StocksMessage(HashMap<String, Integer> map) {
		super();
		this.map = map;
	}

	@Override
	public void setId(long id) {
		// TODO Auto-generated method stub
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setBody(HashMap<String, Integer> body) {
		this.map = body;
	}

	@Override
	public HashMap<String, Integer> getBody() {
		return this.map;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> pair : map.entrySet()) {
			String key = pair.getKey();
			Integer value = pair.getValue();
			sb.append(key + " " + value + "\n");
		}
		return sb.toString();
	}

}
