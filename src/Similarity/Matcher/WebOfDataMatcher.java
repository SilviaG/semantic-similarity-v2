package Similarity.Matcher;

import java.util.ArrayList;
import java.util.Hashtable;

public interface WebOfDataMatcher {
	
	String findMatch(String url, Hashtable<String, ArrayList<String>> statements);

	double getMatchScore(String url, Hashtable<String, ArrayList<String>> statements);
	
	public Hashtable<String, Double> findMatches(String url, Hashtable<String, ArrayList<String>> description) ;
}
