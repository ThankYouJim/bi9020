import java.util.ArrayList;
import java.util.List;


public class Score {
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

class GlobalScore {
	List<Score> scores;
	public GlobalScore(int size) {
		scores = new ArrayList<Score>(size);
	}
	
	public void populate(String[] sequences, MatrixFileIO align) {
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