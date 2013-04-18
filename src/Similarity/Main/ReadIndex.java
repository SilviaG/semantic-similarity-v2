package Similarity.Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.TreeMap;



import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;
import Similarity.Matcher.IndexBasedMatcher;
import Similarity.Matcher.Match;
import Utils.Utils;

public class ReadIndex {
	static String dir = "OntologyMatchingTest/Library/";
	static String INDEX_DIR = dir+"libIndex";
	static ArrayList<String> matchingSet1 = new ArrayList<String>();
	static ArrayList<String> matchingSet2 = new ArrayList<String>();
	
	public static void main(String [] args){
		try{
			

			IndexReader reader = IndexReader.open(FSDirectory.open(new File(INDEX_DIR)), true);
//			readIndex(reader);
			String [] fileList = {
//					"nyt-dbpedia-locations-mappings.rdf",
//					"nyt-dbpedia-organizations-mappings.rdf",
//					"nyt-dbpedia-people-mappings.rdf",	
					"OntologyMatchingTest/Library/refalign/referenceSKOS.rdf",
			};
			for(String standardfile:fileList){
			//String standardfile = "nyt-dbpedia-people-mappings.rdf";
				System.out.println("Processing file "+standardfile);
				int [] blockSize = {2,10,50,100};
				matchingSet1 = Utils.getMatchingDocs(standardfile,"entity1",0,10000000);
				matchingSet2 = Utils.getMatchingDocs(standardfile,"entity2",0,10000000);
				for(int size:blockSize){
					size = 100;
//					String res1 = "http://zbw.eu/stw/descriptor/19821-0";
//					String res2 = "http://lod.gesis.org/thesoz/concept/10044987";
//					readIndex(reader,res1);
//					readIndex(reader,res2);
//					readIndex(reader,res1,res2);
//					checkRecall(reader, standardfile, size);		
//					computeReduction(reader, size);
//					System.out.println(reader.numDocs());
					long start = System.currentTimeMillis();
					findMatch(reader,size);
					long end = System.currentTimeMillis();
					System.out.println("time used: "+ (end-start));
					break;
				}
			}
		}catch(Exception e){
			
			e.printStackTrace();
		}
	}
	public static void findMatch(IndexReader reader, int maxDoc) throws Exception{
		Hashtable<String, Match> allMatches = new Hashtable<String, Match>();
		TermEnum terms = reader.terms(); 

		IndexBasedMatcher matcher = new IndexBasedMatcher();
//    	FileInputStream fstream = new FileInputStream("OntologyMatchingTest/Library/stw.rdf");
//    	FileInputStream fstream2 = new FileInputStream("OntologyMatchingTest/Library/thesoz.rdf");
//		Model model = ModelFactory.createDefaultModel();
//		//m.read(fstream,"");
//		model.read(fstream,"");
//		model.read(fstream2,"");
//		String filename = "lib.rdf";
//		OutputStream output = new FileOutputStream(filename);
//		model.write(output,"N-TRIPLE");
		
//		ArrayList<String> properties = Utils.getProperties(model);
		Hashtable<String,Double> weightVector = new Hashtable<String,Double> ();
		String weightFile ="OntologyMatchingTest/Library/theProperty.rdf";
		weightVector = readWeight(weightFile);
		for(String key:weightVector.keySet()){	
			System.out.println(key+" "+weightVector.get(key));
		}
//		weightVector.put("<http://www.w3.org/2004/02/skos/core#prefLabel>", 10.0);
		Similarity sim = new WeightedFeatureModelSimilarity();//new EntityFeatureModelSimilarity();//
		sim.setWeights(weightVector);
		
		matcher.setSimilarity(sim);
		matcher.setMatchingSet1(matchingSet1);
		matcher.setMatchingSet2(matchingSet2);
		while(terms.next()){
			Term term = terms.term();
			int numDocs = terms.docFreq();
//			TermDocs docs = reader.termDocs(term);
			
//			while(docs.next()){
//				numDocs ++;					
//			}
//			System.out.println(term+" "+numDocs);
			if(numDocs >= 2){	
				
				if(numDocs <= maxDoc){
//					System.out.println(term+" "+numDocs);
					

					boolean inSet2 = false;
					boolean inSet1 = false;
					TermDocs docs = reader.termDocs(term);
					while(docs.next()){
						//construct triple content
						double maxScore = 0;
						String maxScoreURL = "";
						int docId = docs.doc();
						Document doc = reader.document(docId);
						String url1 = doc.get("url");
						if(!inSet1 && matchingSet1.contains(url1)){
							inSet1=true;
						}else if(!inSet2 && matchingSet2.contains(url1)){
							inSet2=true;
						}
						if(inSet1&&inSet2){
							break;
						}
							
					}
					
					if(!(inSet1 && inSet2))
						continue;
					System.out.println(term.toString()+" "+numDocs);
					Hashtable<String, Match> matches = matcher.findMatch(reader, term);
					if(matches==null)
						continue;
					for(String key:matches.keySet()){
						Match m = matches.get(key);

						if(allMatches.containsKey(key)){
							if(allMatches.get(key).getScore() < m.getScore()){
								allMatches.put(key, m);
								System.out.println("update: "+m.getEntity1()+" "+m.getEntity2()+" "+m.getScore());
							}
						}else{
								allMatches.put(key, m);
								System.out.println(m.getEntity1()+" "+m.getEntity2()+" "+m.getScore());							
						}

					}
					writeToFile(INDEX_DIR+".rdf",allMatches);
				}
			}
		}
	}
	public static void writeToFile(String filename, Hashtable<String, Match> allMatches){
		try{
			 FileWriter fstream = new FileWriter(filename);
			  BufferedWriter out = new BufferedWriter(fstream);
				out.write("<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:align=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n<Alignment>\n<xml>yes</xml>\n<level>0</level>\n<type>**</type>\n<onto1>http://www.instancematching.org/IIMB2012/</onto1>\n<onto2>http://www.instancematching.org/IIMB2012/001</onto2>\n");
			  for(String key:allMatches.keySet()){
				  Match m = allMatches.get(key);
				  out.write("<map>\n<Cell>\n<entity1 rdf:resource=\""+m.getEntity1()+"\"/>\n<entity2 rdf:resource=\""+m.getEntity2()+"\"/>\n<relation>=</relation>\n<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">"+m.getScore()+"</measure>\n</Cell>\n</map>\n");
				  System.out.println("<map>\n<Cell>\n<entity1 rdf:resource=\""+m.getEntity1()+"\"/>\n<entity2 rdf:resource=\""+m.getEntity2()+"\"/>\n<relation>=</relation>\n<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">"+m.getScore()+"</measure>\n</Cell>\n</map>\n");
			 
			  }
				out.write("</Alignment>\n</rdf:RDF>\n");
			  out.flush();
			  out.close();	
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public static void computeReduction(IndexReader reader, int maxDoc) throws Exception{
//		int maxDoc = 10000000;
		TermEnum terms = reader.terms(); 
		int overalComputation = 0;
		while(terms.next()){
			Term term = terms.term();
			int numDocs = terms.docFreq();
//			TermDocs docs = reader.termDocs(term);
//			while(docs.next()){
//				numDocs ++;					
//			}
			if(numDocs >= 2){				
				if(numDocs <= maxDoc){
	//				System.out.print("term: "+term.toString()+" ");
					int numCom = computationNum(numDocs);
					overalComputation += numCom;
//					System.out.println(numDocs+" : "+numCom);
				}
			}
		}
		System.out.println("total computation required for blocksize "+maxDoc+": "+overalComputation);
	}
	public static void readIndex(IndexReader reader, String res1){
		try {
			int docId = Utils.getDocId(reader, "url", res1);
			Document doc = reader.document(docId);
			String url = doc.get("url");
		    System.out.println(url);
		    String contents = doc.get("triples");
		    System.out.println("triples: "+contents);
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void readIndex(IndexReader reader, String res1, String res2) throws CorruptIndexException, IOException{
		int maxDoc = 100;
		TermEnum terms = reader.terms(); 
		int overalComputation = 0;

		while(terms.next()){
			Term term = terms.term();
			int numDocs = 0;
			TermDocs docs = reader.termDocs(term);
//			if(term.toString().contains("url:"))
//				continue;
			System.out.println(term.toString());
			
			while(docs.next()){	
				int docId = docs.doc();
				Document doc = reader.document(docId);
				
				String url = doc.get("url");
				if((res1==null && res2==null)||(url.equals(res1)||url.equals(res2))){
					System.out.println(url);
					String contents = doc.get("triples");
					System.out.println("triples: "+contents);
				}
//				String contents = doc.get("contents");
//				if(url.contains("SunCom") || url.contains("61753815637034985472")){
//					System.out.println(url);
//					System.out.println("term: "+term.toString());
//					System.out.println("contents: "+contents);
//				}
			}
		}
	}
	public static void checkRecall(IndexReader reader, String standardfile, int maxDoc) throws Exception {
	
//		int maxDoc = 100;
		String maxTerm = "";

		
	//	String standardfile = "nyt-dbpedia-locations-mappings.rdf";	
        FileInputStream fstream = new FileInputStream(standardfile);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String triple;
        int totalcount = 0 ;
        int matchcount = 0 ;


        String entity1 = "";
        String entity2 = "";
        int missed = 0;
        int total = 0;
        int found = 0;
        while ((triple = br.readLine()) != null )   {
        	triple = triple.trim();
        	if(triple.charAt(0)!='<')
        		continue;
        	
      	if(triple.contains("entity1"))
    		entity1="<"+triple.substring(triple.indexOf("\"")+1,triple.lastIndexOf("\""))+">";
    	
    	if(triple.contains("entity2"))
    		entity2="<"+triple.substring(triple.indexOf("\"")+1,triple.lastIndexOf("\""))+">";
        
        if(entity1.equals("")||entity2.equals(""))
        	continue;
        		
//        System.out.println(total);
        entity1 = entity1.replaceAll("%2C", ",").replaceAll("%27","'").replaceAll("%28","(").replaceAll("%29",")").replaceAll("%27","'").replaceAll("%21", "!");
        entity2 = entity2.replaceAll("%2C", ",").replaceAll("%27","'").replaceAll("%28","(").replaceAll("%29",")").replaceAll("%27","'").replaceAll("%21", "!");
        total ++;
        boolean notFound = true;

		TermEnum terms = reader.terms();     
		while(terms.next()){
			Term term = terms.term();
			int numDocs = terms.docFreq();
			TermDocs tdocs = reader.termDocs(term);
			boolean inBlocksize = (numDocs < maxDoc);
//			while(tdocs.next()){
//				numDocs ++;
//				if(numDocs>maxDoc){
//					inBlocksize = false;
//					break;
//				}
//			}
			
			if(!inBlocksize||numDocs < 2){
				continue;
			}
//			if(!entity2.contains("SunCom") || !entity1.contains("61753815637034985472")){
//				continue;
//			}
			TermDocs docs = reader.termDocs(term);
	        boolean found1 = false;
	        boolean found2 = false;
			while(docs.next()){	
				int docId = docs.doc();
				Document doc = reader.document(docId);
				String url = "<"+doc.get("url")+">";
				//System.out.println(url);
				if(url.equals(entity1)){
					found1 = true;
				}
				else if(url.equals(entity2)){
					found2 = true;
				}
				if(found1 && found2)
					break;
				
			}
			if(found1 && found2){
				System.out.println("found "+entity1+" "+entity2+" in term: "+term);
//				System.out.println("entity1: "+);
				found++;
				notFound=false;
				break;
			}

		}
        if(notFound){
        	System.err.println(entity1+" "+entity2);
        }
        
        entity1 = "";
        entity2 = "";

        }
        System.out.println("Blocksize "+maxDoc+" : "+found+" number pair found out of "+total+" ration: "+((double)found/(double)total));
	}
	public static Hashtable<String, Double> readWeight(String weightFile){
		
		Hashtable<String, Double> weights = new Hashtable<String, Double> ();
		
		try{
			  FileInputStream fstream = new FileInputStream(weightFile);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;

			  while ((strLine = br.readLine()) != null)   {
				  String weight[] = strLine.split(" ");
				  weights.put(weight[0], Double.parseDouble(weight[1]));
			  }
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		return weights;
	}
	public static int computationNum(int num){
		int sum = 0;
		for(int i = num -1; i>0; i--){
			sum += i;
		}
		return sum;
	}
//	if(numDocs > 2){				
//	if(numDocs < maxDoc){
//		System.out.print("term: "+term.toString()+" ");
//		int numCom = computationNum(numDocs);
//		overalComputation += numCom;
//		System.out.println(numDocs+" : "+numCom);
//	}
//}
}
