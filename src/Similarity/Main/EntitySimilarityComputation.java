package Similarity.Main;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

import Similarity.DocSimilarity;
import Similarity.EntityFeatureModelSimilarity;
import Similarity.EntropyBasedSimilarity;
import Similarity.JaccardSimilarity;
import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class EntitySimilarityComputation {
	
	public static void main(String [] args){

		int level = 3;

		String onto1 = "../iimb/onto.owl";
		String onto2 = "./NTriple/mutatedDataNTriple2";
		String weights = "062weightNew.txt";
		
		FileWriter fstream021;
		BufferedWriter out02=null;
		try {
			fstream021 = new FileWriter("EntropyBasedSimilarityResult.txt");
		  	out02 = new BufferedWriter(fstream021);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


	  	try{
		  FileInputStream fstreams = new FileInputStream("pairs.csv");
		  // Get the object of DataInputStream
		  DataInputStream ins = new DataInputStream(fstreams);
		  BufferedReader brs = new BufferedReader(new InputStreamReader(ins));
		  String strLines;
		  while ((strLines = brs.readLine()) != null)   {

			 String [] sp = strLines.split(",");
			 String j = sp[0];
			 String i = sp[1];
//			 int j = 65;
//			 int i = 68;
			onto1 = "./NTriple/mutatedDataNTriple"+j+".rdf";
			
			
			  FileInputStream fstream1;
				String object1 = "http://oaei.ontologymatching.org/2012/IIMBDATA/en/finis_valorum";	  
			try {
				fstream1 = new FileInputStream(onto1);

			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream1);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine = br.readLine();
			  object1 = strLine.split(" ")[0].replaceAll("<", "").replaceAll(">", "");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			onto2 = "./NTriple/mutatedDataNTriple"+i+".rdf";

		Model model1 = ModelFactory.createDefaultModel();
		Model model2 = ModelFactory.createDefaultModel();
		Hashtable<String, Double> weightVector = null;
		IndexReader reader = null;
		try{
        	FileInputStream fstream = new FileInputStream(onto1);
        	model1.read(fstream,"","N-TRIPLE");
        	FileInputStream fstream2 = new FileInputStream(onto2);
        	model2.read(fstream2,"","N-TRIPLE");    
        	
        	weightVector = readWeight(weights);
        	
        	
        	reader = IndexReader.open(FSDirectory.open(new File("vsm")), true);
		}catch(Exception e){
			System.out.println(onto1+" "+onto2);
			e.printStackTrace();
			return;
		}
//		Similarity sim = new JaccardSimilarity();
//		Similarity sim = new EntityFeatureModelSimilarity();
//		Similarity sim = new WeightedFeatureModelSimilarity();
//		Similarity sim = new EntropyBasedSimilarity();
	    Map<String, Integer> terms = new HashMap<String,Integer>();
	    TermEnum termEnum = reader.terms(new Term("contents"));
	    int pos = 0;
	    
	    while (termEnum.next()) {
	      Term term = termEnum.term();
	      if (! "contents".equals(term.field())) 
	        break;
	      terms.put(term.text(), pos++);
	    }
	    
	    Set<String> keys = terms.keySet();
	    Iterator<String> itr = keys.iterator();
	    while(itr.hasNext()){
	    	String key = itr.next();
	    }
	    
	    Similarity sim = new DocSimilarity(terms, reader,reader);
//		Similarity sim = new DocSimilarity();
		sim.setWeights(weightVector);
		sim.setLevel(level);
		
//		ResIterator subjects = model1.listSubjects();
//		Hashtable<String, ArrayList<String>> descriptions1 = new Hashtable<String, ArrayList<String>>();
//		getAllDescriptions(model1,  descriptions1);
//		
//		ResIterator subjects2 = model2.listSubjects();
//		Hashtable<String, ArrayList<String>> descriptions2 = new Hashtable<String, ArrayList<String>>();
//		getAllDescriptions(model2,  descriptions2);
		
		sim.setFirstObjectDescription("vsm");
		sim.setSecondObjectDescription("vsm");


	//	String object1 = "http://www.entitysimilarity.edu/url/id/746047971";
		
		  FileInputStream fstream2;
			String object2 = "http://www.entitysimilarity.edu/url/id/798063377";	  
		try {
			fstream2 = new FileInputStream(onto2);

		  // Get the object of DataInputStream
		  DataInputStream in2 = new DataInputStream(fstream2);
		  BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		  String strLine = br2.readLine();
		  object2 = strLine.split(" ")[0].replaceAll("<", "").replaceAll(">", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		try {

//		String o1 =	getLiterals(object1, descriptions1, level);
//		String o2 =	getLiterals(object2, descriptions2, level);
//		double score = sim.computeSimilarity(o1, o2, level);
		double score = sim.computeSimilarity(object1, object2, level);

	  	out02.write(object1+" "+object2+": "+score+"\n");
//		System.out.println(j+" "+i+": "+score);
	  	System.out.println(score);
	//  	System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  }
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	}
	public static String getLiterals(String object, Hashtable<String, ArrayList<String>> descriptions, int level){
		String rStr = "";
		if(!descriptions.containsKey(object)|| level ==0)
			return object;
		for(String t:descriptions.get(object)){
//			System.out.println(t);
			String nobject = t.split(" ",3)[2];
			rStr = rStr + " " + getLiterals(nobject, descriptions, level -1);
		}		
		return rStr;
	}
	public static void printDescription(String  object, Hashtable<String, ArrayList<String>> descriptions, int level){
		if(!descriptions.containsKey(object)||level==0)
			return;
		for(String t:descriptions.get(object)){
			System.out.println(t);
			String nobject = t.split(" ",3)[2];
			printDescription(nobject, descriptions, level -1);
		}	
		
	}
	
	public static void getAllDescriptions(Model model, Hashtable<String, ArrayList<String>> descriptions){

		StmtIterator stmtItr = model.listStatements();
						
		while(stmtItr.hasNext()){
			Statement stmt = stmtItr.next();		
			String stmtString = stmt.toString().replaceAll(",", "").replaceAll("\\[", "").replaceAll("\\]", "");
			String subject = stmt.getSubject().toString();
			
			if(descriptions.keySet().contains(subject)){
				ArrayList<String> statements = descriptions.get(subject);;
				statements.add(stmtString);
				descriptions.put(subject, statements);			
			}else{
				ArrayList<String> statements = new ArrayList<String>();
				statements.add(stmtString);
				descriptions.put(subject, statements);					
			}

		}


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

}
