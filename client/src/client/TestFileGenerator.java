package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class TestFileGenerator {
	public static int getRandomNumber(int min, int max) {
		return (int) ((Math.random() * (max - min)) + min);
	}

	public static void main(String[] args) throws IOException {
		int num = Integer.parseInt(args[0]);
		int lineNum = Integer.parseInt(args[1]);

		File stocksFile = new File("stocks.txt");
		HashMap<String, Integer> stocks = new HashMap<>();
		List<String> l = new LinkedList<>();
		Scanner myReader = new Scanner(stocksFile);
		while (myReader.hasNextLine()) {
			String[] data = myReader.nextLine().split(";");
			stocks.put(data[0], Integer.parseInt(data[1]));
			l.add(data[0]);
		}
		myReader.close();

		for (int i = 0; i < num; i++) {
			FileWriter myWriter = new FileWriter(args[2 + i]);
			for (int j = 0; j < lineNum; j++) {
				String command;
				double r = Math.random();
				if (r > 0.5) {
					command = "sell;";
				} else {
					command = "buy;";
				}
				int rr = getRandomNumber(0, l.size());
				command += l.get(rr) + ";";
				int cena = getRandomNumber(100000, 10000000);
				int coll = getRandomNumber(5, 50);
				command += coll + ";" + cena + "\n";
				myWriter.write(command);
			}
			myWriter.write("stop");
			myWriter.close();
		}
	}

}
