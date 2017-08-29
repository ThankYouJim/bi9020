/**
 * BINF9020 Assignment 1 Part 1
 * z5137601 Chong Chin Yi
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class evolve {
	
	// Magic Numbers
	static int TYPES = 20;
	static int GENERATIONS = 501;
	
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
		
		// File readers and writers
		FileReader fr = null;
		FileWriter fw = null;
		BufferedReader br = null;

		Fasta[] generations = new Fasta[GENERATIONS];
		Fasta original = new Fasta();

		String[] index = new String[TYPES];	// stores 20 types of amino acids
		
		
		// READ USER COMMAND
		
		// Display message if input and output FASTA files are not provided
		if (args.length != 2) {
			System.err.println("Incorrect command. Try: $>evolve <input file> <output file>");
			System.err.println("eg.: $>evolve s001 s501");
			return;
		}
		
		// Welcome message
		System.out.println("=== BINF9020 Ass 1 Part 1 - Amino Acid Mutation Simulation ===");
		System.out.println("Command: $>evolve <input file> <output file>");

		
		// READ MATRIX FILE
		
		try {
			fr = new FileReader(new File("matrix"));
			br = new BufferedReader(fr);

			String readLine = "";
			String delims = ",";
			
			int ct = 0; // counter just so not to read the last line of the matrix file
			
			// Read rest of line into Item class
			while ((readLine = br.readLine()) != null && ct <= TYPES) {
				readLine = readLine.trim();
				String[] tokens = readLine.split(delims);	// tokens.length = 21
				// Read first line as index of amino acids
				if (ct == 0)
					for(int i=0; i < tokens.length-1; i++) {
						index[i] = tokens[i+1];	// ignore the first empty space
					}
				else {
					String target = tokens[0];
					for(int i=1; i < tokens.length; i++) {
//						double probability = Double.parseDouble(tokens[i]) / 10000;
						int probability = Integer.parseInt(tokens[i]);	// range from 0~10000
						matrix.addEntry(index[i-1], target, probability);
					}
				}
				ct++;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// LOAD INPUT FILE
		
		// Read and load the original sequence to the Fasta array
		try {
			fr = new FileReader(new File(args[0]));
			br = new BufferedReader(fr);
			
			String readLine = "";
			String sequence = "";
			// read sequence header
			if ((readLine = br.readLine()) != null)
				original.setHeader(readLine);	
			// set rest of the sequence to the class sequence
			while ((readLine = br.readLine()) != null)
				sequence += readLine;
			original.setSequence(sequence);
			
			// Check if the file is valid FASTA format
			int ct = 0; 
			boolean check = true;
			while (check == true && ct < sequence.length()) {
				check = false;
				char c = sequence.charAt(ct);
				for (int i=0; i<TYPES; i++) {
					char x = index[i].charAt(0);
					if (c == x) {
						check = true;
						break;
					}
				}
				ct++;
			}
			
			if (check == false) {
				System.err.println("Invalid FASTA file! Exiting...");
			}
			
			generations[0] = original;
			
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			System.err.println("Specified input file does not exist!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: Detect invalid FASTA file format as exception
		
		
		// WRITE TO FILE
		
		try {
			File file = new File(args[1]);
			fw = new FileWriter(file);

			// Write the origin sequence
			Set<String> split0 = new HashSet<String>();
			String seq = generations[0].getSequence();
			while (seq.length() > 80) {
				String s = seq.substring(0, 80);
				seq = seq.substring(81, seq.length());
				split0.add(s);
			}
			fw.write(generations[0].getHeader());
			fw.write("\n");
			for (String s : split0)
				fw.write(s);
			fw.write("\n\n");
			
			for (int gen = 1; gen < GENERATIONS; gen++) {
				String mutHeader = generations[0].getHeader() + " Mutation " + gen;
				String mutSequence = mutate(generations[gen-1].getSequence());
				Fasta fasta = new Fasta(mutHeader, mutSequence);
				generations[gen] = fasta;
				
				// Split the sequence to 80 chars per line for FASTA format
				Set<String> split = new HashSet<String>();
				while (mutSequence.length() > 80) {
					String s = mutSequence.substring(0, 80);
					mutSequence = mutSequence.substring(81, mutSequence.length());
					split.add(s);
				}
				
				fw.write(generations[gen].getHeader());
				fw.write("\n");
				for (String s : split)
					fw.write(s);
				fw.write("\n\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
