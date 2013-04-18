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

import org.apache.lucene.index.IndexReader;

import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;
import Similarity.Matcher.EntityMatcher;
import Similarity.Matcher.IIMBEntityMatcher;
import Similarity.Matcher.WebOfDataMatcher;
import Utils.Utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class IIMBMatcher {
	
	public static void main(String [] args) throws IOException{

		int level = 3;
		//71 //60
		for(int i = 5 ; i <= 6; i++){
			String id = i+"";
			if(i < 10)
				id = "00"+id;
			else
				id = "0"+id;
			
		String onto1 = "../iimb/onto.owl";
		String onto2 = "../iimb2012-src/"+id+"/onto-degibbed.nt";
		String ref = "../iimb/"+id+"/refalign.rdf";
		String output = "myiimb/"+id+"2.rdf";
		String weights = "034pweight.txt";
		System.out.println(onto2);
//		System.in.read();
		Model onto1Model = ModelFactory.createDefaultModel();
		Model onto2Model = ModelFactory.createDefaultModel();
		BufferedWriter out = null;
		Hashtable<String, Double> weightVector = null;
		try{
        	FileInputStream fstream = new FileInputStream(onto1);
        	onto1Model.read(fstream,"");
        	FileInputStream fstream2 = new FileInputStream(onto2);
        	onto2Model.read(fstream2,"","N-TRIPLE");    
        	
        	FileWriter ofstream = new FileWriter(output);
        	out = new BufferedWriter(ofstream);        	
        	weightVector = readWeight(weights);
        	
			out.write("<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:align=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n<Alignment>\n<xml>yes</xml>\n<level>0</level>\n<type>**</type>\n<onto1>http://www.instancematching.org/IIMB2012/</onto1>\n<onto2>http://www.instancematching.org/IIMB2012/001</onto2>\n");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(String key:weightVector.keySet()){
			System.out.println(key+" "+weightVector.get(key));
		}
		
		ArrayList<String> matchingDocs = getMatchingDocs(ref,"entity1",0,100000);
		ArrayList<String> matchingDocs2 = getMatchingDocs(ref,"entity2",0,100000);		
		Similarity sim = new WeightedFeatureModelSimilarity();
		
		sim.setWeights(weightVector);
		sim.setLevel(level);
		
		WebOfDataMatcher WDM = new IIMBEntityMatcher(onto2Model,sim,level,matchingDocs2);
		
		ResIterator subjects = onto1Model.listSubjects();
		Hashtable<String, ArrayList<String>> descriptions = new Hashtable<String, ArrayList<String>>();
		Utils.getAllDescriptions(onto1Model,  descriptions);

		while(subjects.hasNext()){
			Resource subject = subjects.next();
			if(!matchingDocs.contains(subject.toString()))
				continue;
				
			System.out.println("matching... ("+subject.toString()+")");
			String mymatch = WDM.findMatch(subject.toString(),descriptions);
			Hashtable<String,Double> matches = new Hashtable<String,Double>();
			
			//Hashtable<String,Double> match = WDM.findMatch(subject.toString(),descriptions);

			
			try{
				double mymaxSimScore = WDM.getMatchScore(subject.toString(),descriptions);
				matches.put(mymatch,mymaxSimScore);
//				System.out.println(formatOutput(subject.toString(),match,maxSimScore+""));
				for(String match:matches.keySet()){
					double maxSimScore = matches.get(match);
					out.write("<map>\n<Cell>\n<entity1 rdf:resource=\""+subject.toString()+"\"/>\n<entity2 rdf:resource=\""+match+"\"/>\n<relation>=</relation>\n<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">"+maxSimScore+"</measure>\n</Cell>\n</map>\n");
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

}
