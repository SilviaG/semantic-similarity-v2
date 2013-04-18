package Learning;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;


import Similarity.Similarity;
import Similarity.WeightedStructureDataSimilarity;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class PeceptronLearning implements LearningAlgorithm{
	Hashtable<String, Double> weightVector = new Hashtable<String, Double>();
	int level = 3;
	double coe = 1;
	double upperBound = 5000;
	//Hashtable<String, Integer> typeCount = new Hashtable<String, Integer>();
	
	public Hashtable<String, Double> learnWeights(String trainData, String PropertyList, Similarity sim) {
		try{
			
			ArrayList<TrainningData> data = parseData(trainData);
			ArrayList<String> features = new ArrayList<String>();

			Model model = ModelFactory.createDefaultModel();
			FileInputStream fstream = new FileInputStream(PropertyList);
			model.read(fstream,"");
			fstream.close();
			
			features = getProperties(model);
			
			for(int i=0; i < features.size(); i++){
				weightVector.put(features.get(i), 1.0);
			}
			
			//IndexReader reader = IndexReader.open(FSDirectory.open(new File(knowledgeDirectory)), true);
			for(TrainningData td:data){
				System.out.println(td.getFirstEntity()+" "+td.getSecondEntity());
				adaptWeight(sim, td.getFirstEntity(),td.getSecondEntity(),td.getScore());
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public ArrayList<Double> getNumRepresentation(Similarity sim, String entity1, String entity2, double score, double label){
		Hashtable<String, Double> featureSimVec = sim.getFeatureSim(entity1,entity2,level);
		ArrayList<Double> simRepresentation = new ArrayList<Double>();
		for(String key:featureSimVec.keySet()){
			simRepresentation.add(featureSimVec.get(key));
		}
		simRepresentation.add(label);
		return simRepresentation;
	}
	public void adaptWeight(Similarity sim, String entity1, String entity2, double score){
		
		System.out.println("learn via entity: "+entity1+" "+entity2);
		if(entity1.contains("russian")||entity1.contains("star_wars"))
			return;
		double expectedSim = score;
		double preSim = -1;
		sim.setWeights(weightVector);
		double currentSim = sim.computeSimilarity(entity1, entity2,level);	
		System.out.println("current:"+currentSim + " previous:"+ preSim+" expected:"+expectedSim);
		while((Math.abs(currentSim - expectedSim) > 0.005) && (currentSim - preSim > 0.0005)){
			preSim = currentSim;
			Hashtable<String, Double> previousWeight = new Hashtable<String, Double>();
			previousWeight.putAll(weightVector);
			Set<String> features = weightVector.keySet();
			Hashtable<String, Double> featureSimVec = sim.getFeatureSim(entity1,entity2,level);

			for(String feature:featureSimVec.keySet()){
				System.out.println("prev: "+feature);
				if(!features.contains(feature)){
					System.err.println("error not in features:"+feature);
					continue;
				}
				double currentWeight = weightVector.get(feature);
				double newWeight;
				if(currentSim < expectedSim){
					//newWeight = currentWeight + (0.5)*(currentWeight)+(0.5)*coe+(featureSimVec.get(feature));
					if((featureSimVec.get(feature)/currentWeight) <= 0.2 && currentWeight > 0){
						newWeight = currentWeight - currentWeight*(0.5); //increase weight
						System.err.println(feature +" reduce weight to "+newWeight+" "+(featureSimVec.get(feature)/currentWeight));
					}else{
						double thisFeaSim = 1+featureSimVec.get(feature);
						newWeight = currentWeight +coe*(thisFeaSim);
						System.err.println(feature +" add weight to "+newWeight+" "+(featureSimVec.get(feature)/currentWeight));
					}					
				}else{
					//newWeight = currentWeight - (0.5)*(currentWeight)-(0.5)*coe-(featureSimVec.get(feature));
					if((featureSimVec.get(feature)/currentWeight) > 0.5 && currentWeight > 50 ){
						newWeight = currentWeight + coe*(0.5);						
					}else{
						double thisFeaSim = 1+featureSimVec.get(feature);
						newWeight = currentWeight -coe*(thisFeaSim);
						//newWeight = currentWeight - (0.5)*(currentWeight)-coe*(featureSimVec.get(feature));
					}		
//					if(featureSimVec.get(feature) == 0 ){
//						newWeight = currentWeight + (0.5)*(currentWeight)+coe*(0.5); //increase weight
//					}else{
//						double thisFeaSim = 1+featureSimVec.get(feature);
//						newWeight = currentWeight + (0.5)*(1.0/currentWeight)+coe*(thisFeaSim);
//					}					
//				}else{
//					//newWeight = currentWeight - (0.5)*(currentWeight)-(0.5)*coe-(featureSimVec.get(feature));
//					if(featureSimVec.get(feature) == 0 ){
//						newWeight = currentWeight - (0.5)*(currentWeight)-coe*(0.5);						
//					}else{
//						double thisFeaSim = 1+featureSimVec.get(feature);
//						newWeight = currentWeight - (0.5)*(1.0/currentWeight)-coe*(thisFeaSim);
//						//newWeight = currentWeight - (0.5)*(currentWeight)-coe*(featureSimVec.get(feature));
//					}					
				}
				
				System.out.println(feature+" "+featureSimVec.get(feature)+": "+currentWeight+" to "+newWeight);
				
				if(newWeight < upperBound)
					weightVector.put(feature, newWeight);
				
				sim.setWeights(weightVector);
				currentSim = sim.computeSimilarity(entity1,entity2, level);
				System.out.println("recompute current:"+currentSim + " previous:"+ preSim+" expected:"+expectedSim);
				if(currentSim - preSim < 0.0005)	
					weightVector = previousWeight;				
			

			}
			
			sim.setWeights(weightVector);
			currentSim = sim.computeSimilarity(entity1,entity2, level);
			System.out.println("recompute current:"+currentSim + " previous:"+ preSim+" expected:"+expectedSim);
			if(currentSim - preSim < 0.0005)	
				weightVector = previousWeight;	

		}
		sim.setWeights(weightVector);
		currentSim = sim.computeSimilarity(entity1,entity2, level);
		System.out.println("final output current:"+currentSim + " previous:"+ preSim+" expected:"+expectedSim);
		for(String key:weightVector.keySet()){
			System.out.println(key+" "+weightVector.get(key));
		}
		
		return;
	}
	public static ArrayList<String> getProperties(Model m){
		ArrayList<String> properties = new ArrayList<String>();
		try{			
	   		String queryString = "SELECT DISTINCT ?p WHERE { " +
	   			"{" +	   			
	   				"{ ?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty>. } " +
	   				"UNION { ?s ?p ?o. } " +
	   				"UNION {?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>}" +
	   			"} "+
			"}";	   		
	   		QueryExecution qe = QueryExecutionFactory.create(queryString, m);
			ResultSet queryResults = qe.execSelect();
			
			double thisMeasure = 0.0;
			int count = 0 ;
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String property = qs.get("?p").toString();
				properties.add(property);

			}
			qe.close();			
		}catch(Exception e){
			e.printStackTrace();
		}
		return properties;		
	}
	public ArrayList<TrainningData> parseData(String trainData){
		ArrayList<TrainningData> data = new ArrayList<TrainningData>();
		String strLine="";
		  try{

			  FileInputStream fstream = new FileInputStream(trainData);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  
			  String entity1 = null;
			  String entity2 = null;
			  double score = -1;
			  while ((strLine = br.readLine()) != null)   {
				  if(strLine.contains("entity1")){
					  if(strLine.contains("'"))
						  entity1= strLine.substring(strLine.indexOf('\'')+1,strLine.lastIndexOf('\''));
					  if(strLine.contains("\""))
						  entity1=strLine.substring(strLine.indexOf('"')+1,strLine.lastIndexOf('"'));
				  }else if(strLine.contains("entity2")){
					  if(strLine.contains("'"))
						  entity2= strLine.substring(strLine.indexOf('\'')+1,strLine.lastIndexOf('\''));
					  if(strLine.contains("\""))
						  entity2= strLine.substring(strLine.indexOf('"')+1,strLine.lastIndexOf('"'));
				  }else if(strLine.contains("measure")){
					  score = Double.parseDouble(strLine.substring(strLine.indexOf('>')+1,strLine.lastIndexOf('<')));
				  }
				  
				  if(entity1 != null && entity2 != null && score > -1){
					  data.add(new TrainningData(entity1,entity2,score));
					  entity1=null;
					  entity2=null;
					  score = -1;
				  }
			  }

			  in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage()+""+strLine);
			  }
		
		
		return data;
	}


}
