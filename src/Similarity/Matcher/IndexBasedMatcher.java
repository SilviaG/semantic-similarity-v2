package Similarity.Matcher;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import Similarity.EntityFeatureModelSimilarity;
import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;

public class IndexBasedMatcher {

	ArrayList<String> matchingSet1;
	ArrayList<String> matchingSet2;
	Similarity sim;
	public IndexBasedMatcher(){

	}
	public void setSimilarity(Similarity sim){
		this.sim = sim;
	}
	public void setMatchingSet1(ArrayList<String> set1){
		matchingSet1 = set1;
	}
	public void setMatchingSet2(ArrayList<String> set2){
		matchingSet2 = set2;
	}

	public Hashtable<String, Match> findMatch(IndexReader reader, Term term) throws Exception{
//		if(!term.toString().contains("kings"))
//			return null;
	
		TermDocs docs = reader.termDocs(term);

		Hashtable<String, Match> matches = new Hashtable<String, Match>();

		while(docs.next()){
			//construct triple content
			double maxScore = 0;
			String maxScoreURL = "";
			int docId = docs.doc();
			Document doc = reader.document(docId);
			String url1 = doc.get("url");
//			if(url1.contains("nytimes")||url1.contains("geoname"))
//				continue;
			if(!matchingSet1.contains(url1))
				continue;
			
			Hashtable<String, ArrayList<String>> thisDescriptions = fetchTriples(url1, reader);
			sim.setFirstObjectDescription(thisDescriptions);
			TermDocs docs2 = reader.termDocs(term);			
			while(docs2.next()){
				//construct triple content

				int docId2 = docs2.doc();
				Document doc2 = reader.document(docId2);
				String url2 = doc2.get("url");
				
//				if(!((url2.contains("58534283240795407241")||url2.contains("33906611967721951"))&&url1.contains("Acadian_Peninsula")))
//					continue;
				
//				if(url2.contains("dbpedia")||url1.contains("geoname"))
//					continue;
				if(!matchingSet2.contains(url2))
					continue;
				
				Hashtable<String, ArrayList<String>> description2 = fetchTriples(url2, reader);
				sim.setSecondObjectDescription(description2);
				double score = sim.computeSimilarity(url1, url2, 2);
				if(score > maxScore){
					maxScore = score;
					maxScoreURL = url2;
				}
			}
			if(!maxScoreURL.equals("")){
				url1 = url1.trim();
				if(url1.contains("<"))
					url1 = url1.substring(1, url1.length()-1);
				maxScoreURL = maxScoreURL.trim();
				if(maxScoreURL.contains("<"))
					maxScoreURL = maxScoreURL.substring(1, maxScoreURL.length()-1);
				Match m = new Match(url1,maxScoreURL,maxScore);
				if(matches.containsKey(url1)){
					if(matches.get(url1).getScore() < maxScore)
						matches.put(url1, m);
				}else
					matches.put(url1, m);
				
			}
		}
		return matches;
	}

	public String findMatch(String url, Hashtable<String, ArrayList<String>> description){
		double maxSimScore = 0.0;
		String matchRes = "";
//		Hashtable<String, ArrayList<String>> thisDescriptions = fetchTriples(url, reader);
//		
//		while(subjects.hasNext()){
//			Resource subject = subjects.next();
//			similarity.setFirstObjectDescription(description);
//			similarity.setSecondObjectDescription(thisDescriptions);
//			double score = similarity.computeSimilarity(url,subject.toString(),level);
//			if(score > maxSimScore){
//				maxSimScore = score;
//				matchRes = subject.toString();
//				
//			}
//		}
//		score = maxSimScore;
//		this.url = url;
		return matchRes;
	}
	public Hashtable<String, ArrayList<String>> fetchTriples(String url, IndexReader reader)throws IOException{
		int docId = getDocId("url",url, reader);
		if(docId < 0)
			return null;
		
		Hashtable<String, ArrayList<String>> descriptions = new Hashtable<String, ArrayList<String>>();
		Document doc = reader.document(docId);
		String triples_field = doc.get("triples");
		String [] triples = triples_field.split("\n");
		ArrayList<String> content = new ArrayList<String> (Arrays.asList(triples));
		for(String triple:triples){
			String [] spo = triple.split(" ",3);
			if(descriptions.containsKey(spo[0])){
				ArrayList<String> thisContent = descriptions.get(spo[0]);
				thisContent.add(triple);
				descriptions.put(spo[0], thisContent);
			}else{
				ArrayList<String> thisContent = new ArrayList<String>();
				thisContent.add(triple);
				descriptions.put(spo[0], thisContent);				
			}
		}
//		for(String triple:triples){
//			String [] spo = triple.split(" ");
//			if(spo[3].startsWith("http")){
//				Hashtable<String, ArrayList<String>> thisContent = fetchTriples(spo[3],reader, level -1);
//				if(thisContent!=null){
//					descriptions.putAll(thisContent);					
//				}
//			}
//		}
		return descriptions;
				
	}
	public int getDocId(String field, String value, IndexReader reader) throws IOException{
		Term idTerm = new Term(field, value);
		TermDocs docs = reader.termDocs(idTerm);
		int docId = -1;
		while(docs.next()){
			docId = docs.doc();
		}
		return docId;
	}

}
