package DataGeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class DataGeneratorTest {
	
	public static void main(String [] args){


		int dataOutputCount = 1;
		String [] objects = {
//					"http://dbpedia.org/resource/Kobe",
//					"http://data.nytimes.com/51634877270486455581",
//					"http://dbpedia.org/resource/Port_Elizabeth",
//					"http://data.nytimes.com/N52566963358471093961",
//					"http://dbpedia.org/resource/Humphrey_Bogart",
//					"http://dbpedia.org/resource/Richard_Causey",
//					"http://data.nytimes.com/N72808302412021887693",
//					"http://data.nytimes.com/18532313587809356983",
//					"http://dbpedia.org/resource/Strom_Thurmond",
//					"http://dbpedia.org/resource/SLM_Corporation",
//					"http://data.nytimes.com/N12784021743396067312",
//					"http://dbpedia.org/resource/Walter_Reed_Army_Medical_Center",
//					"http://data.nytimes.com/75022206802150252562",	
					
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/bristol",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/gaborone",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/maseru",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/keisha_castle-hughes",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/soviet_union",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/count_dooku",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/malaysia",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/princess_leia_organa",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/bandar_seri_begawan",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/new_york_state",
					"http://oaei.ontologymatching.org/2012/IIMBDATA/en/boba_fett",
					"",
					"",
					"",
					"",					
		};
		
		String dataDirectory = "./Data/";
		
		//String onto2 = "../iimb/onto.owl";
		
		File file = new File(dataDirectory);
		File[] files = file.listFiles();



		try{
  		  FileWriter fstream = new FileWriter("compareData.txt");
  		  BufferedWriter out = new BufferedWriter(fstream);
  		  int count = 0;
			for(File f:files){
				if(count == 1)
					break;
				count ++;
				System.out.println(f.toString());
				Model model2 = ModelFactory.createDefaultModel();
				
	        	FileInputStream fstream2 = new FileInputStream("./Data/onto.rdf");
	        	if(f.toString().endsWith("rdf"))
	        		model2.read(fstream2,"");  
	        	else
	        		model2.read(fstream2,"","N-TRIPLE"); 
			
	        	
	        	
       		Hashtable<String, ArrayList<String>> descriptions2 = new Hashtable<String, ArrayList<String>>();
    		getAllDescriptions(model2,  descriptions2);
    		   		
    		for(String object:objects){
    			if(!descriptions2.keySet().contains(object))
    				continue;
    			
    			Hashtable<String, Hashtable<String, ArrayList<String>>> printObject = new Hashtable<String, Hashtable<String, ArrayList<String>>>();
    			printObject.put(object, descriptions2);
    			for(int i = 0 ; i < 4; i++){
    				SimilarityDataGenerator SDG = new SimilarityDataGenerator();
		    		Hashtable<String, ArrayList<String>> newDescription = SDG.generateData(object, descriptions2,true);
		    		
		    		for(String pkey:printObject.keySet()){
		    			Hashtable<String, ArrayList<String>> printingDescription = printObject.get(pkey);

		    	  		dataOutputCount ++;
		    	  		
			    		for(String key:printingDescription.keySet()){
			    			if(key.contains(".rdf"))
			    				continue;
			    	  		
			    			System.out.println(key);
			    			out.write(key+"\n");
			    			for(String value:printingDescription.get(key)){
			    				String [] values = value.split(" ",3);
			    				String property = values[1].replaceAll("http:/.*(#|/)", "_:");
			    				System.out.println(" \t"+property+" \t"+values[2]);
			    				out.write(" \t"+property+" \t"+values[2]+" ,\n");
//			    				String v = "";
//			    				if(values[2].startsWith("http"))
//			    					v = "<"+values[2]+">";
//			    				else
//			    					v = values[2];
//			    				out2.write("<"+values[0]+"> <"+values[1]+"> "+v+" .\n");
			    			}
			    		}
			    		out.write("=====================================================\n");
			    		System.out.println("=====================================================");
			    		
		    	  		FileWriter fstream21 = new FileWriter("./DataPair/dataPair"+dataOutputCount);
		    	  		BufferedWriter out2 = new BufferedWriter(fstream21);
			    		for(String key:newDescription.keySet()){
			    			System.out.println(key);
			    			out.write(key+"\n");
			    			for(String value:newDescription.get(key)){
			    				String [] values = value.split(" ",3);
			    				String property = values[1].replaceAll("http:/.*(#|/)", "_:");
			    				System.out.println(" \t"+property+" \t"+values[2]);
			    				out.write(" \t"+property+" \t"+values[2]+"\n");
			    				String v = "";
			    				if(values[2].startsWith("http"))
			    					v = "<"+values[2]+">";
			    				else
			    					v = values[2];
			    				out2.write("<"+values[0]+"> <"+values[1]+"> "+v+" .\n");
			    			}
			    		}
			    		
			    		out2.flush();
			    		out2.close();
			    		out.write("\n\n" +
			    				"COMPARE "+SDG.getNewURL()+" "+pkey+"\n"+
			    				"SCORE:\n*****************************************************\n\n\n");
			    		System.out.println("\n\n" +
			    				"COMPARE "+SDG.getNewURL()+" "+pkey+"\n"+
			    				"SCORE:\n*****************************************************\n\n");
			    		out.flush();			    		
		    		}

		    		printObject.put(SDG.getNewURL(),new Hashtable<String, ArrayList<String>>(newDescription));
    			}
    		}

			}
    		out.flush();
    		out.close();

		}catch(Exception e){
			e.printStackTrace();
			return;
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
