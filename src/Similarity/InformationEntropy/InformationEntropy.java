package Similarity.InformationEntropy;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import Utils.Utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class InformationEntropy {
	
	Model model;
	double numEntity;
	ResIterator ritr;
	ArrayList<String> properties;
	ArrayList<ArrayList<Double>> JointEntropyMatrix;
	
	public InformationEntropy(Model m){
		model = m;
		ritr = m.listSubjects();
		numEntity = ritr.toSet().size();
		properties = Utils.getProperties(model);
		JointEntropyMatrix = new ArrayList<ArrayList<Double>>();
	}
	
	public double computeShannonEntropy(String property){
			
		Property p = ResourceFactory.createProperty(property);
		Resource r = null;
		RDFNode o = null;
		StmtIterator sitr = model.listStatements(r, p, o);
		
		Hashtable<String, Integer> frequencyTable = new Hashtable<String, Integer>();
//		Set<String> queriedEntity = new HashSet<String>();
		double numObject = 0;
		while(sitr.hasNext()){
			Statement stmt = sitr.next();
			RDFNode node = stmt.getObject();
			String nodeStr = node.toString();
			if(frequencyTable.keySet().contains(nodeStr)){
				int count = frequencyTable.get(nodeStr) + 1;
				frequencyTable.put(nodeStr,count);
			}else{
				frequencyTable.put(node.toString(),1);
			}
//			queriedEntity.add(stmt.getSubject().toString());
			numObject ++;
		}
//		frequencyTable.put("null", (int)(numEntity - queriedEntity.size()));
		double entropy = 0;
		for(String key:frequencyTable.keySet()){
//			if(frequencyTable.get(key) > 1)
//				System.out.println(key + frequencyTable.get(key));
			double probability = frequencyTable.get(key)/numObject;
			entropy += probability * (Math.log10(probability)/Math.log10(numObject));
		}
		entropy = entropy * -1;
		
		return entropy;
	}
	public double computeJointEntropy(List<String> properties){
		
		if(properties == null || properties.size() == 0)
			return 0;
		
		Hashtable<String, Integer> frequencyTable = new Hashtable<String, Integer>();	
		Set<String> entities = new HashSet<String>();
		
		ArrayList<String> object1s = new ArrayList<String>();
		ArrayList<String> object2s = new ArrayList<String>();
		Hashtable <String, ArrayList<String>> result = new Hashtable <String, ArrayList<String>> ();
		int size = query(properties,entities,result);
		
		//Set<String> queriedEntity = new HashSet<String>();
		int j = 0 ;
		while(j < size){
			String key = "";
			for(int i = 0 ; i < properties.size(); i++){
				key += result.get(properties.get(i)).get(j);
			
			}
			if(frequencyTable.keySet().contains(key)){
				int count = frequencyTable.get(key) + 1;
				frequencyTable.put(key,count);
			}else{
				frequencyTable.put(key,1);
			}	
			j++;
		}

		
//		frequencyTable.put("null", (int)(numEntity - entities.size()));
		double entropy = 0;
		for(String key:frequencyTable.keySet()){
			double probability = (double)frequencyTable.get(key)/size;
			entropy += probability * (Math.log10(probability)/Math.log10(size));
		}
		entropy = entropy * -1;		
		return entropy;
	}
	public double computeJointEntropy(String property1, String property2){
		
		Hashtable<String, Integer> frequencyTable = new Hashtable<String, Integer>();	
		Set<String> entities = new HashSet<String>();
		
		ArrayList<String> object1s = new ArrayList<String>();
		ArrayList<String> object2s = new ArrayList<String>();
		query(property1,property2,entities,object1s,object2s);
		
		//Set<String> queriedEntity = new HashSet<String>();
		for(int i = 0 ; i < object1s.size() && i < object2s.size(); i++){
			String key = object1s.get(i)+object2s.get(i);
			if(frequencyTable.keySet().contains(key)){
				int count = frequencyTable.get(key) + 1;
				frequencyTable.put(key,count);
			}else{
				frequencyTable.put(key,1);
			}
		}
//		frequencyTable.put("null", (int)(numEntity - entities.size()));
		double entropy = 0;
		for(String key:frequencyTable.keySet()){
			double probability = (double)frequencyTable.get(key)/object1s.size();
			entropy += probability * (Math.log10(probability)/Math.log10(object1s.size()));
		}
		entropy = entropy * -1;		
		return entropy;
	}
	private int query(List<String> properties, Set<String> entities, Hashtable <String, ArrayList<String>> results){
		int count = 0 ;
		try{		
			String returnValues = " ?s ";
			String queryBody = "";
			for(int i = 0 ; i < properties.size(); i++){
				returnValues +=" ?o"+i+" ";
				queryBody +=" ?s <"+properties.get(i)+"> ?o"+i+" . ";
			}
	   		String queryString = "SELECT DISTINCT "+returnValues+" WHERE { " +
	   				queryBody +
			"}";

	   		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
			ResultSet queryResults = qe.execSelect();

		
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String subjects = qs.get("?s").toString();
				entities.add(subjects);
				for(int i = 0 ; i < properties.size(); i++){
					String thisObjectValue = qs.get("?o"+i).toString();
					String property = properties.get(i);
					if(results.containsKey(property)){
						ArrayList<String> thisResult = results.get(property);
						thisResult.add(thisObjectValue);
						results.put(property, thisResult);
					}else{
						ArrayList<String> thisResult = new ArrayList<String>();
						thisResult.add(thisObjectValue);
						results.put(property, thisResult);
					}
				}
				count++;
			}

			qe.close();
			

			
		}catch(Exception e){
			e.printStackTrace();
		}
		return count;
	}
	private void query(String property1, String property2, Set<String> entities, ArrayList<String> object1s, ArrayList<String> object2s){
		try{		
	   		String queryString = "SELECT DISTINCT ?s ?o1 ?o2 WHERE { " +
				"?s <"+property1+"> ?o1 ." +
				"?s <"+property2+"> ?o2 ." +
			"}";

	   		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
			ResultSet queryResults = qe.execSelect();

			
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String subjects = qs.get("?s").toString();
				String object1 = qs.get("?o1").toString();
				String object2 = qs.get("?o2").toString();
				entities.add(subjects);
				object1s.add(object1);
				object2s.add(object2);
				
			}

			qe.close();
			

			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
