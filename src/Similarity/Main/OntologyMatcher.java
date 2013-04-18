package Similarity.Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Hashtable;


import Similarity.EntityFeatureModelSimilarity;
import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;
import Similarity.Matcher.OntologyEntityMatcher;
import Similarity.Matcher.WebOfDataMatcher;
import Utils.Utils;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class OntologyMatcher {
	public static void main(String [] args) throws IOException{
		double threshold = 0;
		int level = 2;
//		for(int i = 76 ; i < 81; i++){
//			String id = i+"";
//			if(i < 10)
//				id = "00"+id;
//			else
//				id = "0"+id;
		String [] ontos1 = {
				"cmt",
				"confOf",
				"Conference",
				"edas",
				"ekaw",
				"iasted",

		};
		String [] ontos2 ={
				"conference",
				"confOf",
				"edas",
				"ekaw",
				"iasted",
				"sigkdd",		
		};
		long starttime = System.currentTimeMillis();
		for(String o1: ontos1){
//		String onto1 = "OntologyMatchingTest/cmt.owl";
//		String onto2 = "OntologyMatchingTest/confOf.owl";
			String onto1 = "OntologyMatchingTest/" +o1+".owl";
			for(String o2:ontos2){
			String onto2 = "OntologyMatchingTest/" +o2+".owl";
				if(onto1.equals(onto2))
					continue;

				
		String output = o1+"-"+o2+".rdf";		
		String ref = "OntologyMatchingTest/refalign/"+output;
		output = "OntologyMatchingTest/ontomyresultEntropy/"+output;
//		String weights = "svmWeightNN.txt";

		Model onto1Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
		Model onto2Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_TRANS_INF);
		BufferedWriter out = null;
		Hashtable<String, Double> weightVector = new Hashtable<String, Double>();
		try{
        	FileInputStream fstream = new FileInputStream(onto1);
        	onto1Model.read(fstream,"");
        	FileInputStream fstream2 = new FileInputStream(onto2);
        	onto2Model.read(fstream2,"");    
        	
        	FileWriter ofstream = new FileWriter(output);
        	out = new BufferedWriter(ofstream);
        	
        	ArrayList<String> properties = Utils.getProperties(onto1Model);
  //      	onto1Model.list
        	
			for(int i=0; i < properties.size(); i++){
				if(properties.get(i).contains("rdf-schema#label")){
					weightVector.put(properties.get(i), 10.0);//1.9884379808889294
				}else if(properties.get(i).contains("rdf-schema#first")){
					weightVector.put(properties.get(i), 1.077);//0773323246941886
				}else if(properties.get(i).contains("rdf-schema#subClassOf")){
					weightVector.put(properties.get(i), 1.096);//09582838913705305 10,0,0,1 (239), 10,1.077,1.096,1 (242), 2.989,1.077,1.096,1.0 (222)
				}else{
					weightVector.put(properties.get(i), 1.0);
				}
			}
			
//			for(String key:weightVector.keySet()){
//				System.out.println(key+" "+weightVector.get(key));
//			}
        	
			out.write("<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:align=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n<Alignment>\n<xml>yes</xml>\n<level>0</level>\n<type>**</type>\n<onto1>http://www.instancematching.org/IIMB2012/</onto1>\n<onto2>http://www.instancematching.org/IIMB2012/001</onto2>\n");
		}catch(Exception e){
			e.printStackTrace();
		}
		
//		for(String key:weightVector.keySet()){
//			System.out.println(key+" "+weightVector.get(key));
//		}
		
		ArrayList<String> matchingDocs = getMatchingDocs(ref,"entity1",0,100000);
		ArrayList<String> matchingDocs2 = getMatchingDocs(ref,"entity2",0,100000);		
		Similarity sim = new WeightedFeatureModelSimilarity(onto1Model);	
		sim.setWeights(weightVector);
		sim.setLevel(level);
		
		WebOfDataMatcher WDM = new OntologyEntityMatcher(onto2Model,sim,level,matchingDocs2);
		
		ResIterator subjects = onto1Model.listSubjects();
		Hashtable<String, ArrayList<String>> descriptions = new Hashtable<String, ArrayList<String>>();
		Utils.getAllDescriptions(onto1Model,  descriptions);

		while(subjects.hasNext()){
			Resource subject = subjects.next();
			if(!matchingDocs.contains(subject.toString()))
				continue;
				
			System.out.println("matching... ("+subject.toString()+")");
//			Hashtable<String,Double> matches = WDM.findMatch(subject.toString(),descriptions);
			String matching = WDM.findMatch(subject.toString(),descriptions);
			
			try{
				double tmaxSimScore = WDM.getMatchScore(subject.toString(),descriptions);
				
				Hashtable<String,Double> matches = new Hashtable<String,Double>();
				matches.put(matching, tmaxSimScore);
				for(String match:matches.keySet()){

					double maxSimScore = matches.get(match);
					if(maxSimScore > threshold){
						System.out.println(formatOutput(subject.toString(),matching,maxSimScore+""));
					out.write("<map>\n<Cell>\n<entity1 rdf:resource=\""+subject.toString()+"\"/>\n<entity2 rdf:resource=\""+match+"\"/>\n<relation>=</relation>\n<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">"+maxSimScore+"</measure>\n</Cell>\n</map>\n");
					}
				}
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
		}
		long endtime = System.currentTimeMillis();
		System.out.println("Time used: "+((float)(endtime - starttime)/1000.0));
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
//			if(stmtString.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.w3.org/2002/07/owl#Class"))
//				System.out.println(stmtString);
			
			String subject = stmt.getSubject().toString();
			String object = stmt.getObject().toString();
			
			if(descriptions.keySet().contains(subject)){
				ArrayList<String> statements = descriptions.get(subject);;
				statements.add(stmtString);
				descriptions.put(subject, statements);			
			}else{
				ArrayList<String> statements = new ArrayList<String>();
				statements.add(stmtString);
//				System.out.println(stmtString);
				String label = toPhrase(subject.replaceAll("http:/.*?#|/", ""));
//				System.out.println(subject+" http://www.w3.org/2000/01/rdf-schema#label "+label);
				statements.add(subject+" http://www.w3.org/2000/01/rdf-schema#label "+label);
				descriptions.put(subject, statements);					
			}
//			if(descriptions.keySet().contains(object)){
//				ArrayList<String> statements = descriptions.get(object);;
//				statements.add(stmtString);
//				descriptions.put(object, statements);			
//			}else{
//				ArrayList<String> statements = new ArrayList<String>();
//				statements.add(stmtString);
////				System.out.println(stmtString);
//				String label = toPhrase(object.replaceAll("http:/.*?#|/", ""));
////				System.out.println(subject+" http://www.w3.org/2000/01/rdf-schema#label "+label);
//				statements.add(object+" http://www.w3.org/2000/01/rdf-schema#label "+label);
//				descriptions.put(object, statements);					
//			}
		}
	}
//	public static Hashtable<String,Double> initWeight(){
//		
//	}
	public static Hashtable<String, Double> readWeight(String weightFile){
		
		Hashtable<String, Double> weights = new Hashtable<String, Double> ();
		
		try{
			  FileInputStream fstream = new FileInputStream(weightFile);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
//			  String w = "0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0 0.0 1.413030306171438 0.0 0.0 -0.04999999999999999 0.0 -1.217 0.0 0.0 0.0 0.4114572809806125 0.06075637623561647 0.008375695449165832 0.0 0.0 0.0 -0.221876448967834 0.0 0.0 2.8421709430404007E-14 0.0 -1.5207392588599893 8.325318540493065 0.0 0.0 -1.0908941130781784 2.1187517216982705 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0";
//			  String w = "0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.133 0.0 0.0 -0.004866196571204517 0.0 -0.005723511053153922 0.0 0.0 0.0 0.6124459408847889 8.259670542614117E-4 0.2500006557872725 0.0 0.0 0.0 0.0 0.0 0.0 -6.552940792659001E-4 0.0 -1.173576217093168 9.008980812735025 0.0 0.0 -1.0259586480376726E-6 2.0002092795406354 0.0 0.0 -0.07150010217247615 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0";
//			  String [] tempWeight = w.split(" ");
//			  int index = 0;
			  while ((strLine = br.readLine()) != null)   {
				  String weight[] = strLine.split(" ");
	//			  System.out.println(weight[1]);
				  weights.put(weight[0], Double.parseDouble(weight[1]));// Double.parseDouble(tempWeight[index]));
//				  index++;
			  }
		}catch(Exception e){
			e.printStackTrace();
		}	
		
		return weights;
	}
	public static ArrayList<String> getMatchingDocs(String filename,String entityType,int startindex, int endindex){
		ArrayList<String> entities= new ArrayList<String>();
//		System.out.println(filename);
		  try{

			  FileInputStream fstream = new FileInputStream(filename);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  int currentindex = 1;
			  while ((strLine = br.readLine()) != null && currentindex <= endindex)   {

				  if(strLine.contains(entityType)){
					  
					  if(currentindex >= startindex){
						  String entity = "";
						  if(strLine.contains("\"")){
							  entity=strLine.substring(strLine.indexOf('"')+1,strLine.lastIndexOf('"'));
						  }
						  if(strLine.contains("'")){
							  entity=strLine.substring(strLine.indexOf('\'')+1,strLine.lastIndexOf('\''));
						  }
						   entities.add(URLDecoder.decode(entity));
//						   System.out.println(URLDecoder.decode(entity));
					  }
					  currentindex ++;
				  }
			  }

			  in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
		
		
		return entities;
	}
	public static String toPhrase(String str) {
		   String ret = "";
		   for (int i = 0; i != str.length(); ++i) {
		       char c = str.charAt(i);
		       if (c >= 'A' && c <= 'Z') ret += "_"+(char)(c-'A'+'a');
		       else ret += c;
		   }
		   return ret;
		}
}
