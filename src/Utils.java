import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	// Magic Numbers
	final static int GEN = 500;
	final static int LINE_LIMIT = 80;
	final static int MUT_PROB = 10000;
	
	/**
	 * Reads user input from the console 
	 * @param args	args from main
	 * @param n		correct number of args
	 * @param prompt prints message to console if n does not match
	 * @return
	 */
	public static boolean readConsole(String[] args, int n, String prompt) {
		if (args.length != n) {
			System.err.println("Incorrect number of arguments.");
			System.err.println("Try: " + prompt);
			return false;
		}
		return true;
	}
	
	/** 
	 * Opens and read file and parse the line as strings into a list
	 * @param fileName name of the file to be read
	 * @return List<> a list of strings
	 */
	public static List<String> readFrFile(String fileName) {
		FileReader fr = null;
		BufferedReader br = null;
		
		List<String> data = new ArrayList<String>();
		String readLine = "";
		
		try {
			fr = new FileReader(new File(fileName));
			br = new BufferedReader(fr);
			while ((readLine = br.readLine()) != null) {
				readLine = readLine.trim();	// remove any unwanted space at end of line
				data.add(readLine);
			}
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: File " + fileName + " does not exist.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// close readers
		try {
			br.close(); fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * Opens a new file and writes content to it
	 */
	public static void writeToFile(String fileName, String content) {
		try {
			FileWriter fw = new FileWriter(new File(fileName));
			fw.write(content);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("This is the utils.java file.");
	}
}
