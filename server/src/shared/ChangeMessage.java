package shared;

import java.util.HashMap;
import java.util.Map.Entry;

public class ChangeMessage implements Message<HashMap<String, Double>> {

	private HashMap<String, Double> map;

	public ChangeMessage(HashMap<String, Double> map) {
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
	public void setBody(HashMap<String, Double> body) {
		this.map = body;
	}

	@Override
	public HashMap<String, Double> getBody() {
		return this.map;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Double> pair : map.entrySet()) {
			String key = pair.getKey();
			Double value = pair.getValue();
			sb.append(key + " " + value + "\n");
		}
		return sb.toString();
	}

}
