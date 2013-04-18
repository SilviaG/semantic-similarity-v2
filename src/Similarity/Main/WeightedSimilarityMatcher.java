package Similarity.Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

import Similarity.EntityFeatureModelSimilarity;
import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;
import Similarity.Matcher.EntityMatcher;
import Similarity.Matcher.WebOfDataMatcher;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class WeightedSimilarityMatcher {
	public static void main(String [] args){

		int level = 3;
		String onto2 = "../iimb/001/onto.owl";
		String onto1 = "../iimb/onto.owl";
		String output = "001.txt";
		String weights = "062weightNew.txt";

		
		Model onto1Model = ModelFactory.createDefaultModel();
		Model onto2Model = ModelFactory.createDefaultModel();
		BufferedWriter out = null;
		Hashtable<String, Double> weightVector = null;
		try{
        	FileInputStream fstream = new FileInputStream(onto1);
        	onto1Model.read(fstream,"");
        	FileInputStream fstream2 = new FileInputStream(onto2);
        	onto2Model.read(fstream2,"");    
        	
        	FileWriter ofstream = new FileWriter(output);
        	out = new BufferedWriter(ofstream);        	
        	weightVector = readWeight(weights);
        	
			out.write("<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:align=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n<Alignment>\n<xml>yes</xml>\n<level>0</level>\n<type>**</type>\n<onto1>http://www.instancematching.org/IIMB2012/</onto1>\n<onto2>http://www.instancematching.org/IIMB2012/001</onto2>\n");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Similarity sim = new WeightedFeatureModelSimilarity();
	//	Similarity sim = new EntityFeatureModelSimilarity();
		for(String key:weightVector.keySet()){
			weightVector.put(key, 1.0);
		}
		sim.setWeights(weightVector);
		sim.setLevel(level);
		
		WebOfDataMatcher WDM = new EntityMatcher(onto2Model,sim,level);
		
		ResIterator subjects = onto1Model.listSubjects();
		Hashtable<String, ArrayList<String>> descriptions = new Hashtable<String, ArrayList<String>>();
		getAllDescriptions(onto1Model,  descriptions);

		while(subjects.hasNext()){
			Resource subject = subjects.next();
			System.out.println("matching... ("+subject.toString()+")");
			String match = WDM.findMatch(subject.toString(),descriptions);

			
			try{
				double maxSimScore = WDM.getMatchScore(subject.toString(),descriptions);
//				System.out.println(formatOutput(subject.toString(),match,maxSimScore+""));
				out.write("<map>\n<Cell>\n<entity1 rdf:resource=\""+subject.toString()+"\"/>\n<entity2 rdf:resource=\""+match+"\"/>\n<relation>=</relation>\n<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">"+maxSimScore+"</measure>\n</Cell>\n</map>\n");
				out.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		try{
			out.write("</Alignment>\n</rdf:RDF>\n");
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static String formatOutput(String e1, String e2, String notes){
		String output = "<match>\n";
		output += "<entity1 rdf:resource=\"";
		output += e1;
		output += "\" />\n";
		output += "<entity2 rdf:resource=\"";
		output += e2;
		output += "\" />\n";
		output += "<Note>";
		output += notes;
		output += "</Note>\n";
		output += "</match>\n";
		return output;
		
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
