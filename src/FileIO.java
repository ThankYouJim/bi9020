import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class FileIO {
	protected ArrayList<String> data;
	protected int size = 0;
	
	public FileIO(String fileName) {
		this.data = (ArrayList<String>) Utils.readFrFile(fileName);
	}
	
	public int getSize() { return size; }
	public void setSize(int size) { this.size = size; }
	
	public String[] getSequence() {
		return data.toArray(new String[0]);
	}
	
	// Read from console
	public String toString() {
		return data.toString();
	}
}

// FASTA FILE CLASSES

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
		this.setSize(data.size());
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
		if (sc.hasNext()) {	// line should be empty
			sc.close();
			throw new FASTAException("sequence only contains letters");
		}
		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String s : data)
			sb.append(s+"\n");
		return sb.toString();
	}
}

class FASTAEntry {
	private String header;
	private String sequence;
	
	public FASTAEntry() {}
	
	/** 
	 * This is a "fast create" constructor where it doesn't check the header/sequence
	 */
	public FASTAEntry(String header, String sequence) throws FASTAException {
		this.header = header;
		this.sequence = sequence;
	}
	
	/**
	 *  Creates a FASTA entry file from a list
	 * @param data list 
	 */
	public FASTAEntry(List<String> data) throws FASTAException {
		if (data.size() <= 1)
			throw new FASTAException("does not contain a header/sequence pair");
		
		// set header from data
		setHeader(data.get(0));
		Iterator<String> it = data.subList(1, data.size()).iterator();
		// set the sequence from data
		StringBuilder sb = new StringBuilder();
		// parse the rest of the string lines to sequence
		while (it.hasNext())
			sb.append(it.next());
		setSequence(sb.toString());
	}
	
	public String getHeader() { return header; }
	public void setHeader(String header) throws FASTAException {
		checkHeader(header);
		this.header = header;
	} 
	public String getSequence() { return sequence; }
	public void setSequence(String sequence) throws FASTAException { 
		checkSequence(sequence);
		this.sequence = sequence;
	}
	
	public void checkHeader(String s) throws FASTAException {
		if (s.charAt(0) != '>') {
			// TODO: other conditions
			throw new FASTAException("invalid header format");
		}
	}
	
	public void checkSequence(String s) throws FASTAException {
		try (Scanner sc = new Scanner(s).useDelimiter("[A-Za-z]+")) {
			if (sc.hasNext()) {
				sc.close();
				throw new FASTAException("sequence should only contain letters");
			}
			sc.close();
		}
	}
	
	public void validate(MutMatrixFileIO matrix) throws FASTAException {
		char[] array = getSequence().toCharArray();
		for (int i=0; i < array.length; i++) {
			if (!matrix.contains(array[i]))
				throw new FASTAException("sequence does not match the data on matrix");
		}
	}
	
	public String toString() {
//		final int limit = 80;
//		StringBuilder sb = new StringBuilder();
//		
//		int len = getSequence().length();
//		int start = 0;
//		int end = start + limit - 1;
//		int max = (end <= len-1 ? end : len/limit);
//		if (end <= len-1) {
//			sb.append(getSequence().substring(start, max));
//			start += 80;
//		}
		
		return header+"\n"+sequence+"\n";
	}
}

// Exception class for FASTA files
class FASTAException extends Exception {
	public FASTAException(String message) {
		super("Invalid FASTA format file: " + message + ".");
	}
}

// MATRIX FILES CLASSES

class MatrixFileIO extends FileIO {
	// we have two indexes in case the i and j are listed differently 
	protected List<Character> iIndex = null;
	protected List<Character> jIndex = null;
	protected List<List<Integer>> matrix;
	
	public MatrixFileIO(String fileName) { 
		super(fileName);
		matrix = new ArrayList<List<Integer>>();
	}
	
	public boolean populate() {
		boolean indexFilled = false;	// if horizontal index is filled
		jIndex = new ArrayList<Character>(data.size()-1);
		for (String s : data) {
			String[] tokens = s.split(",");
				
			// make a temporary list of the token array from 1 to end
			if (indexFilled) {
				List<String> tokenList = new ArrayList<String>(Arrays.asList(tokens).subList(1, tokens.length));
				// from row 1 onwards add first element to j index
				jIndex.add(tokens[0].charAt(0));
				
				// populate rest of token to the int array
				List<Integer> row = new ArrayList<Integer>(tokenList.size());
				for (String prob : tokenList)
					row.add(Integer.parseInt(prob));
				matrix.add(row);
			}
			
			// fill the first line of data as horizontal index
			else {
				iIndex = new ArrayList<Character>(tokens.length - 1);
				// disregard the first empty element
				for (String tok : Arrays.copyOfRange(tokens, 1, tokens.length))
					iIndex.add(tok.charAt(0));
				indexFilled = true;
			}
			
			// Check for duplicate in index
//			if (iIndex.size() != tokenList.size() && jIndex.size() != tokenList.size()) {
//				// TODO: throw custom exception
//				System.out.println("Indexes contains duplicate! Please recheck file.");
//				return false;
//			}
		}
		setSize(iIndex.size());
		return true;
	}
	
	public boolean contains(Character code) {
//		System.out.println(code+"|"+iIndex.contains(code) +","+ jIndex.contains(code));
		if (iIndex.contains(code) && jIndex.contains(code)) {
			return true;
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
		
		return -999;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (List<Integer> l:matrix)
			sb.append(l.toString() + "\n");
		return "I:" + iIndex.toString() + "\nJ:" + jIndex.toString()
			+ "\nMATRIX:\n" + sb.toString();
	}
}

class MutMatrixFileIO extends MatrixFileIO {
	public MutMatrixFileIO(String fileName) {
		super(fileName);
		// remove the unwanted last line of the file.
		this.data.remove(data.size()-1);
	}

	// Randomize a integer and calculate if the cumulative probability
	// determines if the amino acid will mutate
	// return the new mutated code, otherwise return the same thing
	public Character mutate(char code) {
		int p = (int) (Math.random() * 10001);
		int cumulativeP = 0;

		int i = iIndex.indexOf(code);
		for (int j=0; j < getSize(); j++) {
			cumulativeP += matrix.get(i).get(j);
			if (p <= cumulativeP) {
				return jIndex.get(j);
			}
		}
		return code;		// redundant
	}
	
	// mutates a whole string
	public String evolve(String sequence) {
		char[] seq = new char[sequence.length()];
		for (int i=0; i < seq.length; i++)
			seq[i] = mutate(sequence.charAt(i));
		
		return String.valueOf(seq);
	}
}
