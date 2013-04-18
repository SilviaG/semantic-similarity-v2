package Learning;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import libsvm.LibSVM;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;

import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;
import Utils.Utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class constructingTrainingSet {
	public static void main(String [] args){
		
		try {
			
			int level = 3;
			int index = 5;
//			String fileindex="";
//			if(index < 10)
//				fileindex = "0"+index;
			
			String onto1 = "OntologyMatchingTest/edas.owl";
			String onto2 = "OntologyMatchingTest/iasted.owl";
			String trainData = "OntologyMatchingTest/ontomyresult5/edas-iasted.rdf";
			String reffile = "OntologyMatchingTest/refalign/edas-iasted.rdf";
			String PropertyList = onto1;

			
			Model model1 = ModelFactory.createDefaultModel();
			Model model2 = ModelFactory.createDefaultModel();
			Hashtable<String, Double> weightVector = new Hashtable<String,Double>();//null;
			try{
	        	FileInputStream fstream = new FileInputStream(onto1);
	        	model1.read(fstream,"");
	        	FileInputStream fstream2 = new FileInputStream(onto2);
	        	model2.read(fstream2,"");    

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
//			trainingData.addAll(CTS.constructData(reffile, reffile, PropertyList, sim));
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
				weightVector.put(features.get(i), weights[i]);
				System.out.println(features.get(i)+" "+weights[i]);
			}
			
		
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
//	public static void getAllDescriptions(Model model, Hashtable<String, ArrayList<String>> descriptions){
//
//		StmtIterator stmtItr = model.listStatements();
//						
//		while(stmtItr.hasNext()){
//			Statement stmt = stmtItr.next();		
//			String stmtString = stmt.toString().replaceAll(",", "").replaceAll("\\[", "").replaceAll("\\]", "");
//			String subject = stmt.getSubject().toString();
//			
//			if(descriptions.keySet().contains(subject)){
//				ArrayList<String> statements = descriptions.get(subject);;
//				statements.add(stmtString);
//				descriptions.put(subject, statements);			
//			}else{
//				ArrayList<String> statements = new ArrayList<String>();
//				statements.add(stmtString);
//				descriptions.put(subject, statements);					
//			}
//
//		}
//
//
//	}

}
