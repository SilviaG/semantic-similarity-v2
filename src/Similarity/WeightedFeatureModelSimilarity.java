package Similarity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import Similarity.InformationEntropy.InformationEntropy;

public class WeightedFeatureModelSimilarity implements Similarity{
	double alpha;
	double beta;
	Similarity textSim;
	Hashtable<String, Double> weightVector;
	Hashtable<String, Double> featureSimVec;
	Hashtable<String, Integer> featureUsedCount;
	Hashtable<String, Double> computedEntropy;
	InformationEntropy IE;
	int highlevel = 3;
	
	Hashtable<String, ArrayList<String>> statements1;
	Hashtable<String, ArrayList<String>> statements2;
	
//	double weightSum;
	ArrayList<String> weightSumAdded;
	int numTriplematter = 0;
	Hashtable<String, Double> computedSim;
	boolean useWeight = true;
	
	
	public WeightedFeatureModelSimilarity(){
		alpha = 1;
		beta = 1;
		textSim = new JaccardSimilarity();
		weightVector = new Hashtable<String, Double>();
		featureSimVec = new Hashtable<String, Double>(); 
		featureUsedCount = new Hashtable<String, Integer>(); 
		weightSumAdded = new ArrayList<String>();
		computedSim = new Hashtable<String, Double>();
		computedEntropy = new Hashtable<String, Double>();
//		weightSum = 0;
		
	}
	public WeightedFeatureModelSimilarity(Model m){
		alpha = 1;
		beta = 1;
		textSim = new JaccardSimilarity();
		weightVector = new Hashtable<String, Double>();
		featureSimVec = new Hashtable<String, Double>(); 
		featureUsedCount = new Hashtable<String, Integer>(); 
		weightSumAdded = new ArrayList<String>();
		computedSim = new Hashtable<String, Double>();
		computedEntropy = new Hashtable<String, Double>();
		IE = new InformationEntropy(m);
//		weightSum = 0;
		
	}
	public void setModel(Model m){
		IE = new InformationEntropy(m);
	}
	public void setLevel(int level){
		highlevel = level;
	}

	public void setFirstObjectDescription(Object description1) {
		statements1 = (Hashtable<String, ArrayList<String>>)description1;	
	}

	public void setSecondObjectDescription(Object description2) {
		statements2 = (Hashtable<String, ArrayList<String>>)description2;	
	}

	public double computeSimilarity(String object1,  String object2, int level) {
		double object1_weightSum = 0;
		double object2_weightSum = 0;
		

		ArrayList<String> properties = new ArrayList<String>();
		if(computedSim.keySet().contains(object1+object2+level)){
			return computedSim.get(object1+object2+level);
		}		
		
		if(level ==0)
			return textSim.computeSimilarity(object1,object2,level);
		
		ArrayList<String> topStmt1 = statements1.get(object1);
		ArrayList<String> topStmt2 = statements2.get(object2);

		if(topStmt1==null || topStmt2==null)
			return textSim.computeSimilarity(object1,object2,level);
		
		ArrayList<String> matchedStmt2 =  new ArrayList<String>();
		
		double pvSimSum = 0.0;
		double object2_common = 0;
		
		for(String stmt1:topStmt1){
			double maxPVSim = 0;
			String currentProcessedStmt2 = null;

			for(String stmt2:topStmt2){
		
				double pvSim = pvSimilarity(stmt1,stmt2, level);

				if(pvSim > maxPVSim){

					maxPVSim = pvSim;
					currentProcessedStmt2 = stmt2;
				}

			}

			if(maxPVSim != 0){

				if(!matchedStmt2.contains(currentProcessedStmt2)){
					matchedStmt2.add(currentProcessedStmt2);
					object2_common += maxPVSim;
				}	
				
				String [] triple1 = stmt1.split(" ",3);
				
//				if(level==highlevel){
//					System.out.println("comparing "+stmt1+" "+currentProcessedStmt2+" "+maxPVSim);
//				}
				
				pvSimSum += maxPVSim;
				if(!properties.contains(triple1[1]))
					properties.add(triple1[1]);

				if(level == highlevel){
					if(featureSimVec.containsKey(triple1[1])){	
						int count= featureUsedCount.get(triple1[1]);
						featureUsedCount.put(triple1[1], count);
						double curSim = (featureSimVec.get(triple1[1]))*count+maxPVSim;
						featureUsedCount.put(triple1[1], (count++));
						featureSimVec.put(triple1[1],curSim/(double)count);
					}else{
						featureSimVec.put(triple1[1], maxPVSim);
						featureUsedCount.put(triple1[1],1);
					}
				}
			}
		}

		
		for(String stmt1:topStmt1){
			String [] triple1 = stmt1.split(" ",3);
			if(useWeight&&weightVector.containsKey(triple1[1])){
				object1_weightSum += weightVector.get(triple1[1]);
			}else{
				object1_weightSum += 1;
			}
		}
		for(String stmt2:topStmt2){
			String [] triple2 = stmt2.split(" ",3);	
			if(useWeight&&weightVector.containsKey(triple2[1])){
				object2_weightSum += weightVector.get(triple2[1]);
			}else{
				object2_weightSum += 1;
			}
		}
		double object1_unique = object1_weightSum - pvSimSum;
		double object2_unique = object2_weightSum -object2_common;


		if((pvSimSum + alpha * (object1_unique) + beta * (object2_unique)) == 0)
			return 0;
		
		double EFMSimilarity = pvSimSum / (pvSimSum + alpha * (object1_unique) + beta * (object2_unique));
		computedSim.put(object1+object2+level, EFMSimilarity);	
		if(IE != null){
			if(computedEntropy.contains(properties.toString())){
				return computedEntropy.get(properties.toString());
			}
			double entropy = IE.computeJointEntropy(properties);
			computedEntropy.put(properties.toString(), entropy);
			if(level == 3)
			System.out.println(object1+" "+object2+" entropy: "+entropy+" sim: "+EFMSimilarity);
			EFMSimilarity = EFMSimilarity * entropy;
		}
		return EFMSimilarity;
	}

	private double pvSimilarity(String stmt1, String stmt2,int level){
		

 
		String [] triple1 = stmt1.split(" ",3);
		String [] triple2 = stmt2.split(" ",3);
			

		if(! (triple1[1].equals(triple2[1]) || isSameProperty(triple1[1],triple2[1])))
			return 0;
		//double weight = 0;
		double weight =0;
//		if(triple1[1].contains("type")){
//			if(weightVector.containsKey(triple1[2]))
//				weight = weightVector.get(triple1[2]);
//			else if(weightVector.containsKey(triple2[2]))
//				weight = weightVector.get(triple2[2]);		
//		}
//		else 
		if(useWeight){
			if(weightVector.containsKey(triple1[1])){
				weight = weightVector.get(triple1[1]);			
			}else if(weightVector.containsKey(triple2[1])){
				weight = weightVector.get(triple2[1]);
			}else{
				return 0;
			}
		}else{
			weight = 1;
		}
		
		if(weight == 0)
			return 0;
		
//		if(level == highlevel){
//			weightSum += weight;
//		}else{
//			weight = 1;
//		}	
		
		String object1 = triple1[2];
		String object2 = triple2[2];	
		boolean object1IsURL = object1.startsWith("http");
		boolean object2IsURL = object2.startsWith("http");
		
		object1 = object1.replaceAll("has", "");
		object2 = object2.replaceAll("has", "");
		if(object1IsURL && object2IsURL){
			return weight*computeSimilarity(object1,object2, level-1);
		}
		
		if((!object1IsURL) && (!object2IsURL)){
			double thisSim = textSim.computeSimilarity(object1, object2,1);
			return weight*thisSim;
		}
		
		if(object1IsURL)
			object1 = getStatmentString(object1,statements1);
		
		if(object2IsURL)
			object2 = getStatmentString(object2,statements2);
			
		return weight*textSim.computeSimilarity(object1, object2,1);
		
	}
	
	public String getStatmentString(String url, Hashtable<String, ArrayList<String>> statement){
		ArrayList<String> thisStmts = statement.get(url);
		
		if(thisStmts == null){
	//		thisStmts = statements2.get(url);
			if(thisStmts == null)
				return "";
		}
		
		StringBuilder stmtString = new StringBuilder();
		for(String stmt:thisStmts){
			String object = stmt.split(" ",3)[2];
			object = object.replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","").replaceAll("http:/.*(#|/)", "");
			if(!stmt.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")){
				stmtString.append(object);
				stmtString.append(" ");
			}
		}
		return stmtString.toString();
	}
	
	
	public void setWeights(Hashtable<String, Double> weightVector) {
		this.weightVector = weightVector;
		for(String key:weightVector.keySet()){
			featureSimVec.put(key, 0.0);
			featureUsedCount.put(key, 0);
		}
	}

	public Hashtable<String, Double> getFeatureSim(String object1,  String object2, int level) {
		if(featureSimVec.isEmpty())
			computeSimilarity(object1,object2,level);
		return featureSimVec;
	}

	private boolean isSameProperty(String p1, String p2){
//		p1 = p1.replaceAll("http://www.instancematching.org/IIMB2012/ADDONS#", "http://oaei.ontologymatching.org/2012/IIMBTBOX/");
//		p2 = p2.replaceAll("http://www.instancematching.org/IIMB2012/ADDONS#", "http://oaei.ontologymatching.org/2012/IIMBTBOX/");
//		p1 = p1.replaceAll("mainly_spoken_in", "spoken_in");
//		p2 = p2.replaceAll("mainly_spoken_in", "spoken_in");
//		p1 = p1.replaceAll("born_in", "native_city_of");
//		p2 = p2.replaceAll("born_in", "native_city_of");
//		p1 = p1.replaceAll("performs", "acted_by");
//		p2 = p2.replaceAll("performs", "acted_by");
		p1 = p1.replaceAll("http://www.w3.org/2004/02/skos/core#definition", "http://www.w3.org/2000/01/rdf-schema#comment");
		p2 = p2.replaceAll("http://www.w3.org/2004/02/skos/core#definition", "http://www.w3.org/2000/01/rdf-schema#comment");
		p1 = p1.replaceAll("http://www.w3.org/2000/01/rdf-schema#subClassOf", "http://www.w3.org/2004/02/skos/core#broader");
		p2 = p2.replaceAll("http://www.w3.org/2000/01/rdf-schema#subClassOf", "http://www.w3.org/2004/02/skos/core#broader");


		p1 = p1.replaceAll("literalForm", "prefLabel");
		p2 = p2.replaceAll("literalForm", "prefLabel");		
		
		if((p1.contains("label")||p1.contains("Label")) && (p2.contains("label")||p2.contains("Label"))){
			return true;
		}

		
		return p1.equals(p2);
	}

}
