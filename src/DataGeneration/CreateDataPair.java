package DataGeneration;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

import Similarity.EntityFeatureModelSimilarity;
import Similarity.Similarity;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CreateDataPair {
	
	public static void main (String [] args){
		/*
		 * create data pair as diversitified as possible
		 * goal is to generate 200 pairs after survey
		 * now generate 300 pairs, with 30 in range of 0 and 0.1 and 
		 */
		/*
		for(int i = 0 ; i < 75; i++){
			for(int j = i+1 ; j < ((i+5)-(i%5)); j++){
				System.out.println(i+", "+j);
			}
		}
		*/
		
		String object1 = "http://www.entitysimilarity.edu/url/id/746047971";
		
		  FileInputStream fstream2;
		  String object2 = "http://www.entitysimilarity.edu/url/id/798063377";	  
		try {
			String file = "WeightedEntitySimilarityResult2.txt";
			fstream2 = new FileInputStream(file);

			DataInputStream in2 = new DataInputStream(fstream2);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			String strLine;
			Hashtable<Double, ArrayList<String>> pairs = new Hashtable<Double, ArrayList<String>> ();
			
			ArrayList<String> paired = new ArrayList<String>();
			
			while((strLine = br2.readLine()) != null){
				String [] line = strLine.split(" ",3);
			//	System.out.println(strLine);
				if(line.length < 3)
					continue;
				
				if(Double.parseDouble(line[2])== 0)
					continue;
				
				double score = Math.floor(Double.parseDouble(line[2])*10);
				boolean processed = false;
				boolean secondProcess = false;
				boolean thirdprocess = false;
				while(!processed){
				if(pairs.containsKey(score)){
					if(pairs.get(score).size() >= 30){
						score++;
						if(thirdprocess){
							break;
						}
						if(secondProcess){
							score = score - 3;
							thirdprocess = true;
						}
						secondProcess = true;

						continue;
					}

					ArrayList<String> pair = pairs.get(score);
					int count =0;
					for(String str:pair){
						if(str.contains(line[0])||str.contains(line[1])){
							count ++;
						}
					}
					if(count > 5){
						processed = true;
						continue;
					}
					
					pair.add(strLine);
					pairs.put(score, pair);
				}else{
					ArrayList<String> pair = new ArrayList<String>();
					pair.add(strLine);
//					paired.add(strLine);
					pairs.put(score, pair);
				}
				processed = true;
				}
			}

			
			String urlfile = "urlList.txt";
			InputStream fstream3 = new FileInputStream(urlfile);

			DataInputStream in3 = new DataInputStream(fstream3);
			BufferedReader br3 = new BufferedReader(new InputStreamReader(in3));	
			ArrayList<String> urls = new ArrayList<String>();
			while((strLine = br3.readLine())!=null){
				urls.add(strLine);
			}
			
			int count = 0 ;
			for(Double d:pairs.keySet()){
				//System.out.println(d+" range: ");
				for(String s:pairs.get(d)){
					//System.out.println(s);
					String [] sp = s.split(" ",3);
					sp[1]=sp[1].substring(0,sp[1].length()-1);
					System.out.println(urls.indexOf(sp[0])+", "+urls.indexOf(sp[1])+" : "+sp[2]);
					//count++;
				}
			}
			//System.out.println(count);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void getRecDescriptions(String url, Hashtable<String, ArrayList<String>> descriptions){

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
					if(v.contains("http://www.w3.org/2001/XMLSchema#") && !v.contains("<http://www.w3.org/2001/XMLSchema#"))
						v = v.replaceAll("http://www.w3.org/2001/XMLSchema#","<http://www.w3.org/2001/XMLSchema#")+">";
					if(v.startsWith("http:") && !property.contains("type"))
						urlList.add(v);					
			}
		}		
	}

	public static void getDescriptions(String key, Hashtable<String, ArrayList<String>> descriptions){
		if(!descriptions.containsKey(key))
			return;
		
		for(String value:descriptions.get(key)){
			System.out.println(value);
				String [] values = value.split(" ",3);
				String property = values[1].replaceAll("http:/.*(#|/)", "_:");
				String v = values[2];
				if(v.contains("http://www.w3.org/2001/XMLSchema#") && !v.contains("<http://www.w3.org/2001/XMLSchema#"))
					v = v.replaceAll("http://www.w3.org/2001/XMLSchema#","<http://www.w3.org/2001/XMLSchema#")+">";
				System.out.println(key+" \t"+property+" \t"+v+"\n");
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
