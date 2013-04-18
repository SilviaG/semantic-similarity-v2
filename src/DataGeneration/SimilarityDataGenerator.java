package DataGeneration;

import java.util.ArrayList;
import java.util.Hashtable;

import com.hp.hpl.jena.rdf.model.Model;

public class SimilarityDataGenerator{
	
	//Model OntologyModel;
	ArrayList<Property> Properties;
	Model dataModel;
	final int maximum = 999999999;
	Hashtable<String, String> URLMap;
	final double addTripleProbability = 0.33;
	final double removeTripleProbability = 0.225;
	final double manipulateTripleProbability = 0.66;
	final double propertyMapProbability = 0.5;
	Hashtable<String, ArrayList<String>> tripleBank;
	Hashtable<String, String> propertyMap;
	Hashtable<String, ArrayList<String>> newDescription;
	int urlCount;
	String newURL;
	String currentURL;
	
	public SimilarityDataGenerator(){
		tripleBank = new Hashtable<String, ArrayList<String>>();
		URLMap = new Hashtable<String, String>();
		propertyMap = new Hashtable<String, String>();
		newDescription = new Hashtable<String, ArrayList<String>>();
		newURL = "";
		urlCount = 1;
	}
	public String getNewURL(){
		return newURL;
	}


	public Hashtable<String, ArrayList<String>> generateData(String url, Hashtable<String, ArrayList<String>> triples, boolean first) {
		
		ArrayList<String> newTriples = new ArrayList<String>();
		
//		String newURL = "";
//		if(!url.startsWith("http://www.entitysimilarity.edu/url/id/")){
//			newURL = maskURL(url);
//			this.newURL = newURL;
//		}
		String newURL = maskURL(url);
		currentURL = newURL;
		
		if(first) 
			this.newURL = newURL;
		
		if(!triples.containsKey(url))
			return newDescription;

		for(String triple:triples.get(url)){
			if(first){
				double action = Math.random();
				if(action < removeTripleProbability){
					continue;
				}
				else if(action < manipulateTripleProbability){
					String newTriple = dataMannipulate(newURL, triple, triples);
					newTriples.add(newTriple);
				}
			}else{
				String [] thisTriples = triple.split(" ",3);
				String newTriple = newURL+" "+thisTriples[1]+" "+thisTriples[2];
				newTriples.add(newTriple);
			}
		}
		
		while(Math.random() < addTripleProbability && first){
			int pid = 0;
			String randomObject = generateURL();
			String e = newURL+" http://www.entitysimilarity.edu/property/pid"+pid+" "+randomObject;
			newTriples.add(e);
		}


		newDescription.put(newURL, newTriples);
		return newDescription;
		
	}
	public String dataMannipulate(String newSubject,String triple, Hashtable<String, ArrayList<String>> triples){
		
		String [] thisTriples = triple.split(" ",3);
		String newProperty = propertyReplacement(thisTriples[1]);
		String newObject = thisTriples[2];
		
		if(!thisTriples[1].contains("#type"))
			newObject = objectManipulate(thisTriples[2], triples);
		
		return newSubject+" "+newProperty+" "+newObject;
	}

	private String objectManipulate( String object, Hashtable<String, ArrayList<String>> triples){
		
		if(Math.random() > manipulateTripleProbability)
			return object;
		
		
		if(object.startsWith("http")){
			generateData(object, triples, false);
			return currentURL;
		}
		String date = isDate(object);
		if(date != null){
			return date;
		}
		
		if(isNumeric(object)){
			if(Math.random()>0.5){
				return Double.toString(Double.parseDouble(object) * Math.random());
			}else{
				return Double.toString(Double.parseDouble(object) - Math.random());
			}
			
		}
		
		object = changeLiteral(object);
		
		
		return object;
	}

	private String propertyReplacement(String property){
		if(!propertyMap.containsKey(property))
			return property;
		
		if(Math.random() > propertyMapProbability)
			return propertyMap.get(property);
		
		return property;
	}
	private String changeLiteral(String object){
		object = object.replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","")
				   .replaceAll("http:/.*(#|/)", "")
				   .replaceAll("[^a-zA-Z0-9\\s]+"," ")
				   .replaceAll("\"", "");
		
		double action = Math.random() ;
		String newObject = "";
		
		if(action < 0.5){
			String [] strs = object.split(" ");
			for(String str:strs){
				if(str.equals(""))
					continue;
				
				if(Math.random() > 0.5)
					newObject += str+" ";
			}
			
			if(newObject.equals(""))
				newObject += object;
			
		}else{
			String [] words = object.split(" ");
			if(words.length > 5){
				return "\""+object+"\"";
			}
			
			for(String str:words){
				if(str.length()>0){
					if(Math.random()>0.5)
						newObject += str.charAt(0)+". ";
					else
						newObject += str+" ";
				}
			}
			return "\""+newObject.toUpperCase()+"\"";
		}
		return "\""+newObject+"\"";
		
	}
	private boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	private String isDate(String str)  
	{  
		return null;
	}
	public String maskURL(String url){
		if(URLMap.containsKey(url))
			return URLMap.get(url);
		
		String thisNewURL="";
		do{
			thisNewURL = generateURL();
		}while(URLMap.containsValue(thisNewURL));
		URLMap.put(url,thisNewURL);
		
		return thisNewURL;
	}
	public String generateURL(){
		String mask = "http://www.entitysimilarity.edu/url/id/";	
		String randomNumString = "";
		int randomNum = 0 + (int)(Math.random()*maximum);
		String maxString = ""+maximum;
		randomNumString = ""+randomNum;
		int diffLength = maxString.length() - randomNumString.length() ;
		for(int i = 0 ; i < diffLength; i ++){
			randomNumString = "0"+randomNumString;
		}	
		return mask+randomNumString;
		
	}
//	private String addTriple(String url, String type, ArrayList<Integer> added){
//	int index = -1;
//	
//	if(tripleBank.containsKey(type)){
//		ArrayList<String> triples = tripleBank.get(type);
//		if(added.size() < triples.size()){
//			index = (int)Math.round(Math.random()*(triples.size()-1));
//			while(added.contains(index)){
//				index = (int)Math.round(Math.random()*(triples.size()-1));
//			}
//			added.add(index);
//			return triples.get(index);
//		}
//	}			
//	return null;
//}
}
