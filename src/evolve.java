/**
 * BINF9020 Assignment 1 Part 1
 * Simulates the mutation of a sequence across 500 generations
 * 
 * @author z5137601 Chong Chin Yi
 * rewritten on 29/07/17
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class evolve {
	final static String PROMPT = "$>evolve <FASTA input file> <FASTA output file>";
	
	public static void main(String[] args) {
		MutMatrixFileIO matrix = null;
		FASTAEntry originSeq = null;
		List<String> mutations = null;
		
		// READ USER COMMAND
		// Display error message if input and output FASTA files are not provided
		Utils.readConsole(args, 2, PROMPT);
		
		// READ MATRIX FILE
		matrix = new MutMatrixFileIO("matrix");
		matrix.populate();
		
		// READ SEQUENCE FROM FILE SPECIFIED FROM CONSOLE
		try {
			// load the FASTAEntry file with list read from input file
			originSeq = new FASTAEntry(Utils.readFrFile(args[0]));
			// check with the already loaded matrix class if the content is valid
			originSeq.validate(matrix);
		} catch (FASTAException e) {
			e.printStackTrace();
		}
		
		// START THE MUTATION FOR 500 ROUNDS
		// 500 + 1 including the origin sequence
		mutations = new ArrayList<String>(Utils.GEN + 1);
		mutations.add(originSeq.getSequence());
		
		for (int i = 0; i < Utils.GEN; i++) {
			String newSeq = matrix.evolve(mutations.get(i));
			mutations.add(newSeq);
		}
		
		// WRITE TO FILE
		// TODO: write to file without the extra empty lines at the end of file
		try (FileWriter fw = new FileWriter(new File(args[1]))) {
			// write all 
			for (int i = 0; i < mutations.size(); i++) {
				// will look like: >sXXX
				String header = String.format(">s%03d", i);
				fw.write(header);
				fw.write("\n");
				
				// Split long sequence to line(s) of 80 chars
				int len = mutations.get(i).length();
				int lines = len / Utils.LINE_LIMIT;
				for (int l = 0; l <= lines; l++) {
					int top = l * Utils.LINE_LIMIT;			// 0, 80, 160, ...
					int tmp = top + Utils.LINE_LIMIT - 1;	// 79, 159, 239, ...
					// if there are more than 80 chars left, print 80 chars
					// otherwise get the remaining length 
					int end = (tmp < len ? tmp : top + (len%Utils.LINE_LIMIT));
					fw.write(mutations.get(i).substring(top, end));
					fw.write("\n");
				}
				fw.write("\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
