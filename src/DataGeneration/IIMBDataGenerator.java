package DataGeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class IIMBDataGenerator {
	
	
	public static void main(String [] args){
		String [] objects = {
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/finis_valorum",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/gaborone",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/moldova",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/keisha_castle-hughes",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/soviet_union",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/count_dooku",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/malaysia",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/princess_leia_organa",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/bandar_seri_begawan",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/new_york_state",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/item1100644725660487971",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/item3004205805706597508",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/item3396301672450137012",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/item339702977373133449",
				"http://oaei.ontologymatching.org/2012/IIMBDATA/en/item3111802397045256762",					
		};	
		
		String dataDirectory = "./Data/IIMB/";
		File file = new File(dataDirectory);
		File[] files = file.listFiles();
		
		try{
			int count = 0;
			int dataOutputCount = 0;
			for(File f:files){
				System.out.println(f.toString());
				Model model2 = ModelFactory.createDefaultModel();
					
		        FileInputStream fstream2 = new FileInputStream(f.toString());
		        if(f.toString().endsWith("rdf"))
		        	model2.read(fstream2,"");  
		        else
		        	model2.read(fstream2,"","N-TRIPLE"); 
						        			        	
	       		Hashtable<String, ArrayList<String>> descriptions2 = new Hashtable<String, ArrayList<String>>();
	    		getAllDescriptions(model2,  descriptions2);
	    		   		
	    		for(String object:objects){
	    			if(!descriptions2.keySet().contains(object))
	    				continue;	 
	    			
		    	  	FileWriter fstream021 = new FileWriter("./TTLData/mutatedDataTTL"+dataOutputCount+".rdf");
		    	  	BufferedWriter out02 = new BufferedWriter(fstream021);			    	  	
//		    		out02.write(object+"\n");
		    		getRecDescriptionsTTL(object,descriptions2,out02);
			    	out02.flush();
			    	out02.close();
		    		
		    	  	FileWriter fstream022 = new FileWriter("./NTriple/mutatedDataNTriple"+dataOutputCount+".rdf");
		    	  	BufferedWriter out03 = new BufferedWriter(fstream022);

		    		//out03.write(object+"\n");
		    		getRecDescriptionsNTriple(object,descriptions2,out03);
			    	out03.flush();
			    	out03.close();
		    	  	dataOutputCount++;   			

	    			for(int i = 0 ; i < 4; i++){
	    				SimilarityDataGenerator SDG = new SimilarityDataGenerator();
			    		Hashtable<String, ArrayList<String>> newDescription = SDG.generateData(object, descriptions2, true);
			    		
			    		boolean goPrint = false;
			    		for(String testKey:newDescription.keySet()){
			    			if(!newDescription.get(testKey).isEmpty()){
			    				goPrint = true;
			    			}
			    		}
			    		
			    		if(!goPrint){
			    			i--;
			    			continue;
			    		}
//			    		System.out.println(object);
			    		
			    	  	FileWriter fstream21 = new FileWriter("./TTLData/mutatedDataTTL"+dataOutputCount+".rdf");
			    	  	BufferedWriter out2 = new BufferedWriter(fstream21);			    	  	
//			    		out2.write(object+"\n");
			    		getRecDescriptionsTTL(SDG.getNewURL(),newDescription,out2);
	//		    		printInTTL(out2, newDescription);
				    	out2.flush();
				    	out2.close();
			    		
			    	  	FileWriter fstream22 = new FileWriter("./NTriple/mutatedDataNTriple"+dataOutputCount+".rdf");
			    	  	BufferedWriter out3 = new BufferedWriter(fstream22);
	
//			    		out3.write(object+"\n");
//			    	  	System.out.println(SDG.getNewURL());
			    	  	getRecDescriptionsNTriple(SDG.getNewURL(),newDescription,out3);
//			    		printInNTriple(out3, newDescription);
				    	out3.flush();
				    	out3.close();
			    	  	dataOutputCount++;
			    	  	
			    		}
	    			}
	    		}


			}catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
		public static void printInTTL(BufferedWriter out2, Hashtable<String, ArrayList<String>> newDescription) throws Exception{
	    	for(String key:newDescription.keySet()){
	    		System.out.println(key);
	    		out2.write(key+"\n");
	    		for(String value:newDescription.get(key)){
	    			System.out.println(value);
	    				String [] values = value.split(" ",3);
	    				String property = values[1].replaceAll("http:/.*(#|/)", "_:");
	    				String v = values[2];
	    				if(v.contains("http://www.w3.org/2001/XMLSchema#") && !v.contains("<http://www.w3.org/2001/XMLSchema#"))
	    					v = v.replaceAll("http://www.w3.org/2001/XMLSchema#","<http://www.w3.org/2001/XMLSchema#")+">";
	    				out2.write(" \t"+property+" \t\t"+v+"\n");
	    			}
	    		}	
		}
	    public static void printInNTriple(BufferedWriter out2, Hashtable<String, ArrayList<String>> newDescription) throws Exception{
	    	for(String key:newDescription.keySet()){
	    		for(String value:newDescription.get(key)){
	    				String [] values = value.split(" ",3);
	    				String property = values[1].replaceAll("http:/.*(#|/)", "_:");
	    				String v = "";
	    				if(values[2].startsWith("http"))
	    					v = "<"+values[2]+">";
	    				else
	    					v = values[2];
	    				if(v.contains("http://www.w3.org/2001/XMLSchema#") && !v.contains("<http://www.w3.org/2001/XMLSchema#"))
	    					v = v.replaceAll("http://www.w3.org/2001/XMLSchema#","<http://www.w3.org/2001/XMLSchema#")+">";
	    				out2.write("<"+values[0]+"> <"+values[1]+"> "+v+" .\n");
	    			}
	    		}
	    }
		public static void getRecDescriptionsTTL(String url, Hashtable<String, ArrayList<String>> descriptions, BufferedWriter out2)throws Exception{
			
			ArrayList<String> urlList = new ArrayList<String>();
			urlList.add(url);
			while(!urlList.isEmpty()){
				String key = urlList.get(0);
				urlList.remove(0);

				if(!descriptions.containsKey(key))
					continue;
				out2.write(key+"\n");				
				for(String value:descriptions.get(key)){
    				String [] values = value.split(" ",3);
    				String property = values[1].replaceAll("http:/.*(#|/)", "_:");
    				String v = values[2];
    				if(v.contains("http://www.w3.org/2001/XMLSchema#") && !v.contains("<http://www.w3.org/2001/XMLSchema#"))
    					v = v.replaceAll("http://www.w3.org/2001/XMLSchema#","<http://www.w3.org/2001/XMLSchema#")+">";
    				
    				out2.write(" \t"+property+" \t"+v+"\n");
    				
					if(v.startsWith("http:") && !property.contains("type"))
						urlList.add(v);					
				}
				out2.write("\n");
			}		
		}
		public static void getRecDescriptionsNTriple(String url, Hashtable<String, ArrayList<String>> descriptions, BufferedWriter out2)throws Exception{
			
			ArrayList<String> urlList = new ArrayList<String>();
			urlList.add(url);
			while(!urlList.isEmpty()){
				String key = urlList.get(0);
				urlList.remove(0);
				if(!descriptions.containsKey(key))
					continue;
				
				for(String value:descriptions.get(key)){
    				String [] values = value.split(" ",3);
    				String property = values[1].replaceAll("http:/.*(#|/)", "_:");
    				String v = values[2];
    				v = v.replaceAll("\n", "").replaceAll("\"", "").replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*", "");
    				if(v.startsWith("http"))
    					v = "<"+v+">";
    				else
    					v = "\""+v+"\"";
//    				if(v.contains("http://www.w3.org/2001/XMLSchema#") && !v.contains("<http://www.w3.org/2001/XMLSchema#"))
//    					v = v.replaceAll("http://www.w3.org/2001/XMLSchema#","<http://www.w3.org/2001/XMLSchema#")+">";
    			
    				out2.write("<"+values[0]+"> <"+values[1]+"> "+v+" .\n");
    				
					if(values[2].startsWith("http:") && !property.contains("type"))
						urlList.add(values[2]);					
				}
				out2.write("\n");
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
