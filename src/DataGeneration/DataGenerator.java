package DataGeneration;

import java.util.ArrayList;
import java.util.Hashtable;


public interface DataGenerator {
	
	Hashtable<String, ArrayList<String>> generateData(String url, Hashtable<String, ArrayList<String>> triples);
	String getNewURL();

}
