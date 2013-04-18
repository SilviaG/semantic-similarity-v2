package Similarity.Main;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.lucene.index.CorruptIndexException;

import Learning.LearningAlgorithm;
import Learning.PeceptronLearning;
import Similarity.Similarity;
import Similarity.WeightedStructureDataSimilarity;

public class SimilarityTest {

	public static void main(String [] args){
		try {
			
			String trainData = "../im@oaei2012-000-080/001/refalign.rdf";
			String PropertyList = "onto.owl";
			String knowledgeDirectory = "../im@oaei2012-000-080/001/basicFeatureIndex-new";
			String mappingFile = "onto.owl";
			Similarity sim = new WeightedStructureDataSimilarity(mappingFile, knowledgeDirectory);
			LearningAlgorithm LA = new PeceptronLearning();
			Hashtable<String,Double> weights = LA.learnWeights(trainData, PropertyList, sim);
			for(String property:weights.keySet()){
				System.out.println(property+" "+weights.get(property));
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 

		
	}
	
	
}
