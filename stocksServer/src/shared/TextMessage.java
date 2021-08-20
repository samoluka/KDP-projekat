package shared;

public class TextMessage implements Message<String> {

	private static final long serialVersionUID = 1L;

	static long cnt = 0;

	String body;
	long id;

	public TextMessage() {
		this("", createID());
	}

	public TextMessage(String s) {
		this(s, createID());
	}

	public TextMessage(String body, long id) {
		this.body = body;
		this.id = id;
	}

	public TextMessage(TextMessage m) {
		this(m.getBody(), m.getId());
	}

	@Override
	public String toString() {
		String s = "";
		s += "Message ID " + id + " message: " + body;
		return s;
	}

	@Override
	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public long getId() {
		return id;
	}

	public static synchronized long createID() {
		return cnt++;
	}
}
