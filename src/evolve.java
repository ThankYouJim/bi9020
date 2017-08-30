/**
 * BINF9020 Assignment 1 Part 1
 * z5137601 Chong Chin Yi
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class evolve {
	final static String PROMPT = "$>evolve <FASTA input file> <FASTA output file>";
	
	static Matrix matrix = new Matrix();
	
	static String mutate(String sequence) {
		char[] charSeq = sequence.toCharArray(); 

		// Randomize a integer and calculate if the cummutaive probability determines if the amino acid will mutate
		for (int i=0; i<charSeq.length; i++) {
			int p = (int) (Math.random() * 10001);
			int cumulativeP = 0;
			Entry entry = matrix.getEntries(Character.toString(charSeq[i]));
			for (Map.Entry<String, Integer> e : entry.getMap().entrySet()) {
				cumulativeP += e.getValue();
				if (p <= cumulativeP) {
					charSeq[i] = e.getKey().charAt(0);
					break;
				}
			}
		}
		
		String mutSeq = String.valueOf(charSeq);
		
		return mutSeq;
	}
	
	public static void main(String[] args) {
		MutMatrixFileIO matrix = null;
		FASTAEntry originSeq = null;
		List<String> sequences = null;
		
		// READ USER COMMAND
		// Display error message if input and output FASTA files are not provided
		Utils.readConsole(args, 2, PROMPT);
		
		// READ MATRIX FILE
		matrix = new MutMatrixFileIO("matrix"); 
		
		// LOAD INPUT SEQUENCE DATA
		try {
			// load the FASTAEntry file with list read from input file
			originSeq = new FASTAEntry(Utils.readFrFile(args[0]));
			// check with the already load matrix class if the content is valid
			originSeq.validate(matrix);
		} catch (FASTAException e) {
			e.printStackTrace();
		}
		
		// START THE MUTATION FOR 500 ROUNDS
		// 500 + 1 to include the origin sequence
		sequences = new ArrayList<String>(Utils.GEN + 1);
		sequences.add(originSeq.getSequence());
		
		
		// WRITE TO FILE
		
//		try {
//			File file = new File(args[1]);
//			fw = new FileWriter(file);
//
//			// Write the origin sequence
//			Set<String> split0 = new HashSet<String>();
//			String seq = generations[0].getSequence();
//			while (seq.length() > 80) {
//				String s = seq.substring(0, 80);
//				seq = seq.substring(81, seq.length());
//				split0.add(s);
//			}
//			fw.write(generations[0].getHeader());
//			fw.write("\n");
//			for (String s : split0)
//				fw.write(s);
//			fw.write("\n\n");
//			
//			for (int gen = 1; gen < GENERATIONS; gen++) {
//				String mutHeader = generations[0].getHeader() + " Mutation " + gen;
//				String mutSequence = mutate(generations[gen-1].getSequence());
//				Fasta fasta = new Fasta(mutHeader, mutSequence);
//				generations[gen] = fasta;
//				
//				// Split the sequence to 80 chars per line for FASTA format
//				Set<String> split = new HashSet<String>();
//				while (mutSequence.length() > 80) {
//					String s = mutSequence.substring(0, 80);
//					mutSequence = mutSequence.substring(81, mutSequence.length());
//					split.add(s);
//				}
//				
//				fw.write(generations[gen].getHeader());
//				fw.write("\n");
//				for (String s : split)
//					fw.write(s);
//				fw.write("\n\n");
//			}
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}

// FASTA class
class Fasta {
	private String header;
	private String sequence;
	
	public Fasta() {}
	public Fasta(String header, String sequence) {
		this.header = header;
		this.sequence = sequence;
	}
	public String getHeader() {return header;}
	public String getSequence() {return sequence;}
	public void setHeader(String header) { this.header = header; } 
	public void setSequence(String sequence) { this.sequence = sequence; } 
	
	public String toString() {
		return header+"\n"+sequence+"\n";
	}
}

class Matrix {
	Map<String, Entry> entries;
	public Matrix () {
		entries = new HashMap<String, Entry>(20);
	}
	public Entry getEntries(String code) {
		if (entries.containsKey(code))
			return entries.get(code);
		return null;
	}
	
	public void addEntry(String original, String target, int probability) {
		Entry entry;
		if ((entry = getEntries(original)) == null) {
			entries.put(original, new Entry());
			entry = getEntries(original);
		}
		entry.add(target, probability);
	}
	
	public String toString() {
		return entries.toString(); 
	}
}

class Entry {
	Map<String, Integer> items;
	public Entry() {
		items = new HashMap<String, Integer>(20);
	}
	public void add(String target, int probability) {
		items.put(target, probability);
	}
	public Map<String, Integer> getMap() {
		return items;
	}
	public String toString() {
		return items.toString();
	}
}
