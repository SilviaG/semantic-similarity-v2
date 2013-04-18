package Similarity.Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

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

public class LargeBioMatcher {
	public static void main(String [] args){
	int level = 3;

//	String directory = "OntologyMatchingTest/largeBioMed/";
//
//	String onto1 = directory+"oaei2012_FMA_small_overlapping_nci.owl";
//	String onto2 = directory+"oaei2012_NCI_small_overlapping_fma.owl";
//	String output = "oaei2012_FMA2NCI_original_UMLS_mappings.rdf";		
//	String ref = directory+"refalign/"+output;
//	output = directory+"ontomyresult/"+output;
	String directory = "OntologyMatchingTest/Library/";

	String onto1 = directory+"stw.rdf";
	String onto2 = directory+"thesoz.rdf";
	String output = "referenceSKOS.rdf";		
	String ref = directory+"refalign/"+output;
	output = directory+"ontomyresult/"+output;
//	String weights = "svmWeightNN.txt";

	Model onto1Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
	Model onto2Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
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
    	
		for(int i=0; i < properties.size(); i++){
//			if(properties.get(i).contains("rdf-schema#label")){
//				weightVector.put(properties.get(i), 0.0);//1.9884379808889294
//			}else if(properties.get(i).contains("rdf-schema#first")){
//				weightVector.put(properties.get(i), 1.077);//0773323246941886
//			}else if(properties.get(i).contains("rdf-schema#subClassOf")){
//				weightVector.put(properties.get(i), 1.096);//09582838913705305 10,0,0,1 (239), 10,1.077,1.096,1 (242), 2.989,1.077,1.096,1.0 (222)
//			}else{
				weightVector.put(properties.get(i), 1.0);
//			}
		}
		
//		for(String key:weightVector.keySet()){
//			System.out.println(key+" "+weightVector.get(key));
//		}
    	
		out.write("<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:align=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n<Alignment>\n<xml>yes</xml>\n<level>0</level>\n<type>**</type>\n<onto1>http://www.instancematching.org/IIMB2012/</onto1>\n<onto2>http://www.instancematching.org/IIMB2012/001</onto2>\n");
	}catch(Exception e){
		e.printStackTrace();
	}
	
//	for(String key:weightVector.keySet()){
//		System.out.println(key+" "+weightVector.get(key));
//	}
	
	ArrayList<String> matchingDocs = Utils.getMatchingDocs(ref,"entity1",0,100000);
	ArrayList<String> matchingDocs2 = Utils.getMatchingDocs(ref,"entity2",0,100000);		
	Similarity sim = new WeightedFeatureModelSimilarity();	
	sim.setWeights(weightVector);
	sim.setLevel(level);
	
	WebOfDataMatcher WDM = new OntologyEntityMatcher(onto2Model,sim,level,matchingDocs2);
	
	ResIterator subjects = onto1Model.listSubjects();
	Hashtable<String, ArrayList<String>> descriptions = new Hashtable<String, ArrayList<String>>();
	Utils.getAllDescriptions(onto1Model,  descriptions);
	System.out.println("onto1: "+descriptions.keySet().size());
	while(subjects.hasNext()){
		Resource subject = subjects.next();
		if(!matchingDocs.contains(subject.toString()))
			continue;
			
		System.out.println("matching... ("+subject.toString()+")");
//		Hashtable<String,Double> matches = WDM.findMatch(subject.toString(),descriptions);
		String matching = WDM.findMatch(subject.toString(),descriptions);
		
		try{
			double tmaxSimScore = WDM.getMatchScore(subject.toString(),descriptions);
			System.out.println(formatOutput(subject.toString(),matching,tmaxSimScore+""));
			Hashtable<String,Double> matches = new Hashtable<String,Double>();
			matches.put(matching, tmaxSimScore);
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

public static Hashtable<String, Double> readWeight(String weightFile){
	
	Hashtable<String, Double> weights = new Hashtable<String, Double> ();
	
	try{
		  FileInputStream fstream = new FileInputStream(weightFile);
		  // Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String strLine;
//		  String w = "0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0 0.0 1.413030306171438 0.0 0.0 -0.04999999999999999 0.0 -1.217 0.0 0.0 0.0 0.4114572809806125 0.06075637623561647 0.008375695449165832 0.0 0.0 0.0 -0.221876448967834 0.0 0.0 2.8421709430404007E-14 0.0 -1.5207392588599893 8.325318540493065 0.0 0.0 -1.0908941130781784 2.1187517216982705 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0";
//		  String w = "0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.133 0.0 0.0 -0.004866196571204517 0.0 -0.005723511053153922 0.0 0.0 0.0 0.6124459408847889 8.259670542614117E-4 0.2500006557872725 0.0 0.0 0.0 0.0 0.0 0.0 -6.552940792659001E-4 0.0 -1.173576217093168 9.008980812735025 0.0 0.0 -1.0259586480376726E-6 2.0002092795406354 0.0 0.0 -0.07150010217247615 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0";
//		  String [] tempWeight = w.split(" ");
//		  int index = 0;
		  while ((strLine = br.readLine()) != null)   {
			  String weight[] = strLine.split(" ");
//			  System.out.println(weight[1]);
			  weights.put(weight[0], Double.parseDouble(weight[1]));// Double.parseDouble(tempWeight[index]));
//			  index++;
		  }
	}catch(Exception e){
		e.printStackTrace();
	}	
	
	return weights;
}
}
