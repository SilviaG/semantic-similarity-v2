package Similarity.Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

import Similarity.DocSimilarity;
import Similarity.Similarity;
import Similarity.WeightedFeatureModelSimilarity;
import Utils.Utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class TwoEntityCompare {
	public static void main(String [] args){

		int level = 3;

		String directory = "OntologyMatchingTest/multifarm/";

		String onto1 = directory+"cn/cmt-cn.owl";
		String onto2 = directory+"en/cmt-en.owl";
		String output = "cmt-cmt-cn-en.rdf";		
		String ref = directory+"refalign/cn-en/"+output;
		output = directory+"ontomyresult/"+output;
		String weights = "062weightNew.txt";

	  	try{

			

		Model model1 = ModelFactory.createDefaultModel();
		Model model2 = ModelFactory.createDefaultModel();
		Model tmodel = ModelFactory.createDefaultModel();
		Hashtable<String, Double> weightVector = null;
	//	IndexReader reader = null;
		try{
        	FileInputStream fstream = new FileInputStream(onto1);
        	model1.read(fstream,"");
        	FileInputStream fstream2 = new FileInputStream(onto2);
        	model2.read(fstream2,"");    
        	tmodel = processModel(model2); 	
        	weightVector = readWeight(weights);
        	
        	
       // 	reader = IndexReader.open(FSDirectory.open(new File("vsm")), true);
		}catch(Exception e){
			System.out.println(onto1+" "+onto2);
			e.printStackTrace();
			return;
		}
//		Similarity sim = new JaccardSimilarity();
//		Similarity sim = new EntityFeatureModelSimilarity();
		Similarity sim = new WeightedFeatureModelSimilarity(model1);
    	ArrayList<String> properties = Utils.getProperties(model1);

		for(int i=0; i < properties.size(); i++){
//			if(properties.get(i).contains("rdf-schema#label")){
//				weightVector.put(properties.get(i), 2.989);//1.9884379808889294
//			}else if(properties.get(i).contains("rdf-schema#first")){
//				weightVector.put(properties.get(i), 1.077);//0773323246941886
//			}else if(properties.get(i).contains("rdf-schema#subClassOf")){
//				weightVector.put(properties.get(i), 1.096);//09582838913705305 10,0,0,1 (239), 10,1.077,1.096,1 (242), 2.989,1.077,1.096,1.0 (222)
//			}else{
				weightVector.put(properties.get(i), 1.0);
//			}
		}
//		Similarity sim = new EntropyBasedSimilarity();
//	    Map<String, Integer> terms = new HashMap<String,Integer>();
//	    TermEnum termEnum = reader.terms(new Term("contents"));
//	    int pos = 0;
//	    
//	    while (termEnum.next()) {
//	      Term term = termEnum.term();
//	      if (! "contents".equals(term.field())) 
//	        break;
//	      terms.put(term.text(), pos++);
//	    }
//	    
//	    Set<String> keys = terms.keySet();
//	    Iterator<String> itr = keys.iterator();
//	    while(itr.hasNext()){
//	    	String key = itr.next();
//	    }
//	    
//	    Similarity sim = new DocSimilarity(terms, reader,reader);
//		for(String key:weightVector.keySet())
//			weightVector.put(key,1.0);
//		
		sim.setWeights(weightVector);
		sim.setLevel(level);
		
		ResIterator subjects = model1.listSubjects();
		Hashtable<String, ArrayList<String>> descriptions1 = new Hashtable<String, ArrayList<String>>();
		getAllDescriptions(model1,  descriptions1);
		
		ResIterator subjects2 = model2.listSubjects();
		Hashtable<String, ArrayList<String>> descriptions2 = new Hashtable<String, ArrayList<String>>();
		getAllDescriptions(tmodel,  descriptions2);
		
		sim.setFirstObjectDescription(descriptions1);
		sim.setSecondObjectDescription(descriptions2);


		String object1 = "http://cmt_cn#c-6306630-6583290";
			String object2 = "http://cmt_en#op-2674759-3900303";
			String object3 = "http://cmt_en#c-9519178-7885109";	  


	//		printDescription(object2,descriptions2,3);
		try {

//		String o1 =	getLiterals(object1, descriptions1, level);
//		String o2 =	getLiterals(object2, descriptions2, level);
//		double score = sim.computeSimilarity(o1, o2, level);
			double score = sim.computeSimilarity(object1, object2, level);

//		  	out02.write(object1+" "+object2+": "+score+"\n");
			System.out.println(object1+" "+object2+": "+score);
			score = sim.computeSimilarity(object1, object3, level);

//		  	out02.write(object1+" "+object2+": "+score+"\n");
			System.out.println(object1+" "+object3+": "+score);
//	  	System.out.println(score);
	//  	System.in.read();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	}
	public static String getLiterals(String object, Hashtable<String, ArrayList<String>> descriptions, int level){
		String rStr = "";
		if(!descriptions.containsKey(object)|| level ==0)
			return object;
		for(String t:descriptions.get(object)){
//			System.out.println(t);
			String nobject = t.split(" ",3)[2];
			rStr = rStr + " " + getLiterals(nobject, descriptions, level -1);
		}		
		return rStr;
	}
	public static void printDescription(String  object, Hashtable<String, ArrayList<String>> descriptions, int level){
		if(!descriptions.containsKey(object)||level==0)
			return;
		for(String t:descriptions.get(object)){
			System.out.println(t);
			String nobject = t.split(" ",3)[2];
			printDescription(nobject, descriptions, level -1);
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
	public static Hashtable<String, Double> readWeight(String weightFile){
		
		Hashtable<String, Double> weights = new Hashtable<String, Double> ();
		
		try{
			  FileInputStream fstream = new FileInputStream(weightFile);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;

			  while ((strLine = br.readLine()) != null)   {
				  String weight[] = strLine.split(" ");
				  weights.put(weight[0], Double.parseDouble(weight[1]));
			  }
		}catch(Exception e){
			e.printStackTrace();
		}	
		
		return weights;
	}
	public static Model processModel(Model model){
		
		StmtIterator itr = model.listStatements();
		ArrayList<Statement> stmts = new ArrayList<Statement>();
		while(itr.hasNext()){
			Statement stmt = itr.next();
			Statement newStmt = ResourceFactory.createStatement(stmt.getSubject(),stmt.getPredicate(),stmt.getObject());
			String object = stmt.getObject().toString().toLowerCase();
			
			if(object.startsWith("http")||isNumeric(object)){
				stmts.add(stmt);
				continue;
			}
			object = object.replaceAll("\\^\\^http://www.w3.org/2001/xmlschema#.*","")
					   .replaceAll("http:/.*?#|/", "")
					   .replaceAll("[^a-z0-9\\s\\-\\.]+"," ").replaceAll("\"", "");
			String newObject = removeMeaningless(object.replaceAll("[^a-z\\s\\.]+","").trim().split("\\s+"));
			
			if(newObject.equals("")||newObject.equals(" "))
				continue;
			
			newStmt = newStmt.changeObject(newObject);
			stmts.add(newStmt);
//			System.out.println(newStmt);
		}
		Model tmodel = ModelFactory.createDefaultModel();
		tmodel.add(stmts);
		return tmodel;
		
	}
	public static boolean isNumeric(String str)  
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
	public static String removeMeaningless(String [] str){
		ArrayList<String> stopWords = new ArrayList<String>(Arrays.asList((
//				"able," +
//				"about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because," +
//				"been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from," +
//				"get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just," +
//				"least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on," +
//				"only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the," +
//				"their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what," +
//				"when,where,which,while,who,whom,why,will,with,would,yet,you,your," +
				"a,b,c,d,e,g,h,i,j,k,l,n,o,p,q,r,s,t,u,v,w,x,y,z").split(",")));
		ArrayList<String> garbagePattern = new ArrayList<String>(Arrays.asList(("aa,bbb,ccc,ddd,fff,ggg,hhh,ii,jj,kk,oo,pp,qq,rrr,uu,vvv,ww," +
				"xx,yy,zz").split(",")));
		String returnString = "";	
		for(int i =0;i <str.length;i++){
			boolean inGarbage = false;
			for(String gstr:garbagePattern){
				if(str[i].contains(gstr)){
					inGarbage = true;
					break;
				}
			}
			
			if(inGarbage)
				continue;
			
			boolean inStopWord = false;
			for(String substr:stopWords){
				if(str[i].equals(substr)){
					inStopWord=true;
					break;
				}
			}
			if(!inStopWord&&str[i].length()>0)
				returnString += str[i]+" ";
		}

		return returnString.trim() ;
	}

}
