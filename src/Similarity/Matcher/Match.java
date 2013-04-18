package Similarity.Matcher;

public class Match {
	double score;
	String entity1;
	String entity2;
	
	public Match(String e1,String e2,double s){
		entity1 = e1;
		entity2 = e2;
		score = s;
	}
	public String getEntity1(){
		return entity1;
	}

	public String getEntity2(){
		return entity2;
	}

	public double getScore(){
		return score;
	}

}
