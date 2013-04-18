package Learning;

import java.util.Hashtable;

import Similarity.Similarity;

public interface LearningAlgorithm {
	
	Hashtable<String,Double> learnWeights(String trainData, String PropertyList, Similarity sim);
	
	

}
