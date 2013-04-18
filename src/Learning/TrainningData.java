package Learning;

import java.util.ArrayList;

public class TrainningData {
	
	String entity1;
	String entity2;
	ArrayList<Double> featureVecSimRep;
	double label;
	double score;
	
	public TrainningData(String entity1, String entity2, double score){
		this.entity1 = entity1;
		this.entity2 = entity2;
		this.score = score;

	}
	public TrainningData(String entity1, String entity2, double score, double label){
		this.entity1 = entity1;
		this.entity2 = entity2;
		this.score = score;
		//featureVecSimRep = featureVecSim;
		this.label = label;

	}
	public double getLabel(){
		return label;
	}
	public ArrayList<Double> getFeatureVecSimRep(){
		return featureVecSimRep;
	}
	public String getFirstEntity(){
		return entity1;
	}
	
	public String getSecondEntity(){
		return entity2;
	}
	
	public double getScore(){
		return score;
	}

}
