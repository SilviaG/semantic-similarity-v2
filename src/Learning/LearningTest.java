package Learning;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;

public class LearningTest {
	
	public static void main(String [] args){
		
		try {
			
			int level = 3;
			
			String onto1 = "../iimb/onto.owl";
			String onto2 = "../iimb/075/onto.owl";
			String output = "075test.txt";
			String trainData = "../im@oaei2012-000-080/075/refalign.rdf";
			String PropertyList = "onto.owl";

			
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
			
			ResIterator subjects = model1.listSubjects();
			Hashtable<String, ArrayList<String>> descriptions1 = new Hashtable<String, ArrayList<String>>();
			getAllDescriptions(model1,  descriptions1);
			
			ResIterator subjects2 = model2.listSubjects();
			Hashtable<String, ArrayList<String>> descriptions2 = new Hashtable<String, ArrayList<String>>();
			getAllDescriptions(model2,  descriptions2);
			
			Similarity sim = new WeightedFeatureModelSimilarity();
			for(String key:weightVector.keySet())
				weightVector.put(key,1.0);
			sim.setWeights(weightVector);
			sim.setLevel(level);
			sim.setFirstObjectDescription(descriptions1);
			sim.setSecondObjectDescription(descriptions2);
			
//			Model model = ModelFactory.createDefaultModel();
//			FileInputStream fstream = new FileInputStream(PropertyList);
//			model.read(fstream,"");
//			fstream.close();
//			ArrayList<String> features = new ArrayList<String>();
//			features = PeceptronLearning.getProperties(model);
//			
//			for(int i=0; i < features.size(); i++){
//				weightVector.put(features.get(i), 0.0);
//			}
//			sim.setWeights(weightVector);
//			String object2 = "http://oaei.ontologymatching.org/2012/IIMBDATA/en/item6706980954412267443";
//			String object1 = "http://oaei.ontologymatching.org/2012/IIMBDATA/en/russian";
//			double score = sim.computeSimilarity(object1, object2, level);
//			System.out.println(score);
			PeceptronLearning LA = new PeceptronLearning();
			//ArrayList<Double> numRep = LA.getNumRepresentation(sim, entity1, entity2, score, 1);
			Hashtable<String,Double> weights = LA.learnWeights(trainData, PropertyList, sim);
			for(String property:weights.keySet()){
				System.out.println(property+" "+weights.get(property));
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
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
}
