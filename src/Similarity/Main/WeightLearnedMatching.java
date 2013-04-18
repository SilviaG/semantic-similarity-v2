package Similarity.Main;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import libsvm.LibSVM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import Learning.constructTrainingSet;
import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;
import Similarity.Matcher.OntologyEntityMatcher;
import Similarity.Matcher.WebOfDataMatcher;
import Utils.Utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

public class WeightLearnedMatching {
	public static void main(String [] args){
		
		try {
			
			int level = 3;
			int index = 5;
//			String fileindex="";
//			if(index < 10)
//				fileindex = "0"+index;
//			String directory = "OntologyMatchingTest/multifarm/";
//
//			String onto1 = directory+"cn/cmt-cn.owl";
//			String onto2 = directory+"cz/cmt-cz.owl";
//			String output = "cmt-cmt-cn-cz.rdf";		
//			String reffile = directory+"refalign/cn-cz/"+output;
//			String trainData = directory+"ontomyresult/"+output;
//			output = directory+"ontomyresult/cmt-cmt-cn-cz2.rdf";
			int id = 5;
			
			String onto1 = "../iimb/onto.owl";
			String onto2 = "../iimb2012-src/00"+id+"/onto-degibbed.nt";
			String reffile = "../iimb/00"+id+"/refalign.rdf";
			String output = "myiimb/00"+id+"2.rdf";
			String trainData = "myiimb/00"+id+".rdf";
//			String onto1 = "OntologyMatchingTest/edas.owl";
//			String onto2 = "OntologyMatchingTest/iasted.owl";
//			String trainData = "OntologyMatchingTest/ontomyresult/edas-iasted.rdf";
//			String reffile = "OntologyMatchingTest/refalign/edas-iasted.rdf";
//			String output = "OntologyMatchingTest/ontomyresult/edas-iasted2.rdf";

			String PropertyList = onto1;

			
			Model model1 = ModelFactory.createOntologyModel();
			Model model2 = ModelFactory.createOntologyModel();
			Hashtable<String, Double> weightVector = new Hashtable<String,Double>();//null;
			BufferedWriter out = null;

			try{
	        	FileInputStream fstream = new FileInputStream(onto1);
	        	model1.read(fstream,"");
	        	FileInputStream fstream2 = new FileInputStream(onto2);
	        	model2.read(fstream2,"","N-TRIPLE");    

	        	FileWriter ofstream = new FileWriter(output);
	        	out = new BufferedWriter(ofstream);
	        	
			}catch(Exception e){
				e.printStackTrace();
			}
			
			Hashtable<String, ArrayList<String>> descriptions1 = new Hashtable<String, ArrayList<String>>();
			Utils.getAllDescriptions(model1,  descriptions1);
			
			Hashtable<String, ArrayList<String>> descriptions2 = new Hashtable<String, ArrayList<String>>();
			Utils.getAllDescriptions(model2,  descriptions2);
			
			Similarity sim = new WeightedFeatureModelSimilarity();
			for(String key:weightVector.keySet())
				weightVector.put(key,1.0);
			sim.setWeights(weightVector);
			sim.setLevel(level);
			sim.setFirstObjectDescription(descriptions1);
			sim.setSecondObjectDescription(descriptions2);
			
			constructTrainingSet CTS = new constructTrainingSet();
			ArrayList<ArrayList<Double>> trainingData = CTS.constructData(trainData, reffile, PropertyList, sim);
			ArrayList<ArrayList<Double>> trainingData2 = CTS.constructData(reffile, reffile, PropertyList, sim);
			trainingData.addAll(trainingData2);
			ArrayList<String> features = CTS.getFeatures();
			
			ArrayList<Instance> instances = new ArrayList<Instance>();

			for(ArrayList<Double> data:trainingData){
				Instance inst = new SparseInstance(); 
	        	for(int i = 0 ; i < data.size() - 1; i++){
	        		Double score = data.get(i);
	        		inst.put(i, score);
	        	}
	        	inst.setClassValue(data.get(data.size()-1));
	        	instances.add(inst);
			}
			LibSVM libsvm = new LibSVM();
			Dataset data = new DefaultDataset(instances) ;
			libsvm.buildClassifier(data);
			double [] weights = libsvm.getWeights();
			
			if(features.size() != weights.length){
				System.err.println("error: features size not match weights size "+features.size()+" "+weights.length);
			}
			
			for(int i = 0 ; i < weights.length ; i++){
				if(weights[i]==0){
					weights[i]= 1;
					weightVector.put(features.get(i), weights[i]);
				}else{
					if(weights[i] > 0)
						weights[i] += 1;
					weightVector.put(features.get(i), weights[i]);
				}
				System.out.println(features.get(i)+" "+weightVector.get(features.get(i)));
			}
			
			ArrayList<String> matchingDocs = Utils.getMatchingDocs(reffile,"entity1",0,100000);
			ArrayList<String> matchingDocs2 = Utils.getMatchingDocs(reffile,"entity2",0,100000);		
//			Similarity sim = new WeightedFeatureModelSimilarity();	
			sim.setWeights(weightVector);
			sim.setLevel(level);
			
			WebOfDataMatcher WDM = new OntologyEntityMatcher(model2,sim,level,matchingDocs2);	
			ResIterator subjects = model1.listSubjects();
			Hashtable<String, ArrayList<String>> descriptions = new Hashtable<String, ArrayList<String>>();
			Utils.getAllDescriptions(model1,  descriptions);
			
			out.write("<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:align=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n<Alignment>\n<xml>yes</xml>\n<level>0</level>\n<type>**</type>\n<onto1>http://www.instancematching.org/IIMB2012/</onto1>\n<onto2>http://www.instancematching.org/IIMB2012/001</onto2>\n");

			while(subjects.hasNext()){
				Resource subject = subjects.next();
				if(!matchingDocs.contains(subject.toString()))
					continue;
					
				System.out.println("matching... ("+subject.toString()+")");
//				Hashtable<String,Double> matches = WDM.findMatch(subject.toString(),descriptions);
				String matching = WDM.findMatch(subject.toString(),descriptions);
				
				try{
					double tmaxSimScore = WDM.getMatchScore(subject.toString(),descriptions);
					System.out.println(Utils.formatOutput(subject.toString(),matching,tmaxSimScore+""));
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
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
}
