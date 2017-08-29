import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * BINF9020 Assignment 1 Part 2
 * z5137601 Chong Chin Yi
 */


public class align {
	final static int dTest = 8;	// 8 for initial testing
	final static int d = 7;
	final static int e = 1;
	
	final static String prompt = "$> align <sequence_file> <alignment_file>";
	
	public static void main(String[] args) throws FASTAException {
		
		// Validate console input
		if (args.length != 2) {
			System.out.println("ERROR: wrong number of arguments.");
			System.out.println("Try: " + prompt);
			return;
		}
		
		// Read FASTA file from console
		FASTAFileIO fasta = new FASTAFileIO(args[0]);
		fasta.validate();
		
		// Read alignment matrix from console
		AlignFileIO align = new AlignFileIO("BLOSUM62");
//		align.validate();
		align.makeMatrix();
		
		// Calculate score and write to file (incomplete)
		GlobalScore score = new GlobalScore(fasta.getSize());
		score.populate(fasta.getSequence(), align);
		
		utils.writeToFile(args[1], score.toString());
	}
}

class utils {
	
	public static List<String> readFrFile(String fileName) {
		FileReader fr = null;
		BufferedReader br = null;
		
		List<String> data = new ArrayList<String>();
		String readLine = "";
		
		try {
			fr = new FileReader(new File(fileName));
			br = new BufferedReader(fr);
			while ((readLine = br.readLine()) != null) {
				readLine = readLine.trim();
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
	
	public static void writeToFile(String fileName, String content) {
		try {
			FileWriter fw = new FileWriter(new File(fileName));
			fw.write(content);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

// Generic File class to store lines of string from a file
class FileIO {
	ArrayList<String> data;
	int size;
	
	public FileIO(String fileName) {
		this.data = (ArrayList<String>) utils.readFrFile(fileName);
		size = 0;
	}
	
	public int getSize() {
		return size;
	}
	
	public String[] getSequence() {
		return data.toArray(new String[0]);
	}
	
	public String toString() {
		return data.toString();
	}
}

class FASTAFileIO extends FileIO {
	
	public FASTAFileIO(String fileName) {
		super(fileName);
	}
	
	// Sequence must come after a header, otherwise file is invalid FASTA
	// Header indexes must be sequential otherwise assume file is 'corrupted'
	// Ignore excess or lack-of empty lines
	public boolean validate() throws FASTAException {
		data.removeAll(Arrays.asList("", null));	// remove all empty lines
		String[] array = data.toArray(new String[0]);

		if (array.length %2 != 0)
			throw new FASTAException("missing header or sequence in file");
		
		for (int i=0; i<array.length; i+=2) {
			// check if header + sequence pair exist
			if (isHeader(array[i]) && isSequence(array[i+1]))
				data.remove(array[i]);
			else
				throw new FASTAException("invalid format");
		}
		size = data.size();
		return true;
	}
	
	public boolean isHeader(String s) throws FASTAException {
//		Scanner sc = new Scanner(s).useDelimiter("[ ]+");
//		sc.next();
//		if (sc.hasNext() && sc.hasNextInt() == false)
//			throw new FASTAException("wrong header format (1)");
		return true;
	}
	
	public boolean isSequence(String s) throws FASTAException {
		Scanner sc = new Scanner(s).useDelimiter("[A-Za-z]+");
		if (sc.hasNext())	// line should be empty
			throw new FASTAException("sequence only contains letters");
		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String s : data)
			sb.append(s+"\n");
		return sb.toString();
	}
}

// A custom file to store the alignment matrix data and indexes
class AlignFileIO extends FileIO {
	List<String> iIndex = null;
	List<String> jIndex = null;
	List<List<Integer>> matrix;
	
	public AlignFileIO(String fileName) { 
		super(fileName);
		matrix = new ArrayList<List<Integer>>();
	}

	public boolean makeMatrix() {
		boolean indexFilled = false;	// if horizontal index is filled
		int jCt = 0;
		jIndex = new ArrayList<String>(data.size()-1);
		for (String s : data) {
			String[] tokens = s.split(",");
			int size = tokens.length;
				
			// make a temporary list of the token array from 1 to end
			List<String> list = new ArrayList<String>(Arrays.asList(tokens).subList(1, size));
			if (indexFilled) {
				// from row 1 onwards add first element to j index
				jIndex.add(tokens[0]);
				
				// populate rest of token to the int array
				List<Integer> intList = new ArrayList<Integer>(list.size());
				for (String score : list)
					intList.add(Integer.parseInt(score));
				matrix.add(intList);
				
				jCt++;
			}
			
			// fill the first line of data as horizontal index
			else {
				iIndex = new ArrayList<String>(list);
				indexFilled = true;
			}
			
			// Check for duplicate in index
			if (iIndex.size() != list.size() && jIndex.size() != list.size()) {
				// TODO: throw custom exception
				System.out.println("Indexes contains duplicate! Please recheck file.");
			}
		}
		return false;
	}
	
	public int find(String si, String sj) {
		int i, j;
		if (iIndex.contains(si.toUpperCase()) && jIndex.contains(sj.toUpperCase())) {
			i = iIndex.indexOf(si);
			j = jIndex.indexOf(sj);
			int score = matrix.get(i).get(j);
			return score;
		}
		
		return -999;	// error
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (List<Integer> l:matrix)
			sb.append(l.toString() + "\n");
		return "I:" + iIndex.toString() + "\nJ:" + jIndex.toString()
			+ "\nMATRIX:\n" + sb.toString();
	}
}

// A class to store the score and pID of each sequence
class GlobalScore {
	List<Score> scores;
	public GlobalScore(int size) {
		scores = new ArrayList<Score>(size);
	}
	
	public void populate(String[] sequences, AlignFileIO align) {
		int ct = 0;
		String origin = sequences[0];
		
		for (int i=0; i<sequences.length; i++) {
			for (int j=0; j<sequences[i].length(); j++) {
				String s0 = String.valueOf(origin.charAt(j));
				String sq = String.valueOf(sequences[i].charAt(j));
				ct += align.find(s0, sq);
			}
			scores.add(new Score(i, ct));
			ct = 0;
		}
	}
	
	public int traceback() {
		int k, id;
		
		return -999;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%3s %5s %21s\n", "N", "Score", "Percentage Identities"));
		for (Score s : scores)
			sb.append(s.toString());
		return sb.toString();
	}
}

class Score {
	private int n;	// index
	private int score;
	private int pID;	// percentage identity
	
	public Score() {
		n = score = pID = 0;
	}
	public Score(int n, int score) {
		this.n = n;
		this.score = score;
	}
	public void setN(int n) { this.n = n; }
	public void setScore(int score) { this.score = score; }
	public void setPID(int pID) { this.pID = pID; }
	
	public String toString() {
		return String.format("%3d %5d %10d\n", n, score, pID);
	}
}


// SOME CUSTOM EXCEPTION CLASSES FOR THE CUSTOM FILE TYPES
class argsException extends Exception {
	public argsException(String message) {
	}
}

class FASTAException extends Exception {
	public FASTAException(String message) {
		super("Invalid FASTA format file: " + message + ".");
	}
}
