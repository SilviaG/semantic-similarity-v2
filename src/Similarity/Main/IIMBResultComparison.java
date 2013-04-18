package Similarity.Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class IIMBResultComparison {

	//"semsim062new.rdf";//"/Users/zhengj3/Downloads/OAEI2012InstanceMatchingIIMBResult/062/semsim.rdf";//
//	public static String outputfile = "../im@oaei2012-000-080-1/062/semsim.rdf";
//	public static String standardfile = "../im@oaei2012-000-080/062/refalign.rdf";
//	public static String indexfile = "../im@oaei2012-000-080/062/basicFeatureIndex-new";
	static String id = "1";
	public static String outputfile = "myiimb/00"+id+".rdf";
	public static String standardfile = "../im@oaei2012-000-080/0"+id+"/refalign.rdf";
	public static String indexfile = "../im@oaei2012-000-080/004/basicFeatureIndex-new";


	
	public static Model mappingModel;
	public static int global_matchcount=0;
	public static int global_totalcount=0;
	
	public static void main(String [] args){
		mappingModel = ModelFactory.createDefaultModel();

		String usage = "java ResultComparsion.java -o yourResult -r referenceResult ";
		try{
				
			for(int i = 0 ; i < args.length ; i ++){
				if(args[i].equals("-o")){
					i++;
					if(args[i].charAt(0)=='-'){
						System.err.println(usage);
						return;
					}
						
					outputfile = args[i];
					
				}else if(args[i].equals("-r")){
					i++;
					if(args[i].charAt(0)=='-'){
						System.err.println(usage);
						return;						
					}
					standardfile = args[i];
				}				
				
			}
			
			double stat=0;
			int count = 0;
			

			int level = 3;
			int fileindex = 5;
			for(int i = 0 ; i < 80+1; i++){
				String id = i+"";
				if(i < 10)
					id = "00"+id;
				else
					id = "0"+id;
				
				String ref = "../iimb/"+id+"/refalign.rdf";
				String output = "myiimb/"+id+".rdf";
				standardfile = ref;
				outputfile = output;
			//count++;
			stat += compare(outputfile,standardfile,indexfile);
			}
			System.out.println("overall performance: "+stat/80.0);
//			return;
					
			
		//	System.out.println(global_matchcount+" "+global_totalcount+" "+((double)global_matchcount/(double)global_totalcount));
			//return;
		}catch(Exception e){
				//e.printStackTrace();
			}
		
		
		
	}
	public static double inRef(String reffile, String filename){
		
		try{
		FileInputStream mstream = new FileInputStream(reffile);	
	
		Model m = ModelFactory.createDefaultModel();
		m.removeAll();
		m.read(mstream,"");
		
		
        FileInputStream fstream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String triple;
        int totalcount = 0 ;
        int matchcount = 0 ;


        String entity1 = "";
        String entity2 = "";
        while ((triple = br.readLine()) != null )   {
        	
        	if(triple.charAt(0)!='<')
        		continue;
        	
        	if(triple.contains("entity1"))
        		entity1="<"+triple.substring(triple.indexOf('\'')+1,triple.lastIndexOf('\''))+">";
        	
        	if(triple.contains("entity2"))
        		entity2="<"+triple.substring(triple.indexOf('\'')+1,triple.lastIndexOf('\''))+">";
//        	System.out.println(triple);            
//        	System.out.println("e1: "+entity1);
//        	System.out.println("e2: "+entity2);

            if(entity1.equals("")||entity2.equals(""))
            	continue;
            
//            entity1 = entity1.replaceAll("\\(", "\\%28").replaceAll("\\)", "\\%29").replaceAll(",", "\\%2C").replaceAll("'", "\\%27");
//            entity2 = entity2.replaceAll("\\(", "%28").replaceAll("\\)", "%29").replaceAll(",", "%2C").replaceAll("'", "%27");
            double match = getEntitySimilarity(entity1,entity2,m);
            return match;
        }
        }catch(Exception e){
        	e.printStackTrace();
        }
		return 0;
	}
	public static double compare(String filename,String reffile,String index) {
		try{
		FileInputStream mstream = new FileInputStream(reffile);	
	
		
		mappingModel.removeAll();
		mappingModel.read(mstream,"");
		
		
        FileInputStream fstream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String triple;
        int totalcount = 0 ;
        int matchcount = 0 ;


        String entity1 = "";
        String entity2 = "";
        while ((triple = br.readLine()) != null )   {
        	triple = triple.trim();
        	if(triple.charAt(0)!='<')
        		continue;
        	
        	if(triple.contains("entity1") && triple.contains("\""))
        		entity1="<"+triple.substring(triple.indexOf('"')+1,triple.lastIndexOf('"'))+">";
        	else if(triple.contains("entity1") && triple.contains("'"))
        		entity1="<"+triple.substring(triple.indexOf("'")+1,triple.lastIndexOf("'"))+">";
        	
        	if(triple.contains("entity2") && triple.contains("\""))
        		entity2="<"+triple.substring(triple.indexOf('"')+1,triple.lastIndexOf('"'))+">";
  	
        	else if(triple.contains("entity2") && triple.contains("'"))
        		entity2="<"+triple.substring(triple.indexOf("'")+1,triple.lastIndexOf("'"))+">";
//        	System.out.println(triple);            
//        	System.out.println("e1: "+entity1);
//        	System.out.println("e2: "+entity2);

            if(entity1.equals("")||entity2.equals(""))
            	continue;
            
            
//            entity1 = entity2.replaceAll("\\(", "%28").replaceAll("\\)", "%29").replaceAll(",", "%2C").replaceAll("'","%27").replaceAll("", "%21");
//            entity2 = entity2.replaceAll("%28", "\\(").replaceAll("%29", "\\)").replaceAll("%2C", ",").replaceAll("%27","'").replaceAll("%21", "!");
            double match = getEntitySimilarity(entity1,entity2,mappingModel);
            
            
			if(match == 0){
				String measure = br.readLine();
				measure = br.readLine();
				String correctmatch = getMatchEntity(entity1);
				//measure ="<"+measure.substring(measure.indexOf('<')+1,measure.lastIndexOf('>'))+">";
//				if(!correctmatch.equals(""))
 //           	System.err.println("not matching "+entity1+" "+entity2+" "+correctmatch);
//				else
//					matchcount++;
				
//				String refMatch = getMatchEntity(entity1);
//				refMatch= "<"+refMatch+">";
//				System.out.println(refMatch);
//				System.out.println(entity1);
//				int orgDoc = getDocId("URI", entity1, reader);
//				int myMatchDoc = getDocId("URI", entity2, reader);
//				int refMatchDoc = getDocId("URI", refMatch, reader);
//				
//				String orgCont = reader.document(orgDoc).get("contents");
//				String myDocCont = reader.document(myMatchDoc).get("contents");
//				String refDocCont = reader.document(refMatchDoc).get("contents");
				
//				String output = "";
//				output += "org: "+orgDoc+"\n "+orgCont+"\n";
//				output += "match: "+myMatchDoc+"\n "+myDocCont+"\n";
//				output += "ref: "+refMatchDoc+"\n "+refDocCont+"\n";
//				output += "====================================\n";
//				
//				log("error_report.txt", output);
            }
            else{
            	matchcount++;
  //          	System.out.println("matching "+entity1+" "+entity2);
            }
            
            totalcount++;
            entity1="";
            entity2="";
        }

    //    if((double)matchcount/(double)totalcount<0.92){
        //System.out.println(filename+": "+matchcount+" out of "+getNumberMatch()+" with total tested "+totalcount+": "+((double)matchcount/(double)totalcount));
    //    }
        System.out.println(((double)matchcount/(double)totalcount));
        global_matchcount += matchcount;
        global_totalcount += totalcount;
        return (double)matchcount/(double)totalcount;
		}catch(Exception e){
			//e.printStackTrace();
		}
		return 0.0;
	}
	public static void readDoc(IndexReader reader, String URI) throws IOException{
		int orgDoc = getDocId("URI", URI, reader);		
		String orgCont = reader.document(orgDoc).get("contents");
		System.out.println(orgDoc+URI+":");
		System.out.println(orgCont);
	}
	public static void log(String filename,String output) throws Exception{
		 FileWriter ofstream = new FileWriter(filename,true);
		 BufferedWriter out = new BufferedWriter(ofstream);
		 out.append(output);
		 out.close();
		
	}
	public static boolean inIndex(IndexReader reader, String URI){
		try{
		for(int i =0 ; i < reader.numDocs(); i++){

			if(reader.document(i).get("URI").contains(URI)){
				return true;
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
		
	}
	public static int getDocId(String field, String value, IndexReader reader) throws IOException{
		Term idTerm = new Term(field, value);
		TermDocs docs = reader.termDocs(idTerm);
		//System.out.println(field+" || "+value);
		int docId = 0;
		while(docs.next()){
			docId = docs.doc();
			//System.out.println("doc id for "+value+": "+docId);
		}
		return docId;
	}
	public static void correctAlignRef(String filename) throws Exception{
		FileInputStream mstream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(mstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
		FileWriter ofstream = new FileWriter(filename+"2");
		BufferedWriter out = new BufferedWriter(ofstream);
        while((line = br.readLine()) != null){
        	out.write(line+"\n");
        	if(line.contains("</Cell>")){        		
        		out.write("</map>\n<map>\n");
        	}     	
        }
        out.flush();
        out.close();
        in.close();
        
        
	}
	public static int getNumberMatch(){
		try{

			//System.err.println("sparql: "+entity1+" "+entity2);
			
	   		String queryString = "SELECT  ?measure WHERE { " +
			"{" +

			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure. " +
			"} "+
			"}";	   		
	   		//System.err.println(queryString);
	   		QueryExecution qe = QueryExecutionFactory.create(queryString, mappingModel);
			ResultSet queryResults = qe.execSelect();
			
			double thisMeasure = 0.0;
			int count = 0 ;
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String measure = qs.get("?measure").toString();
				String [] temp = measure.split("\\^\\^");
				measure = temp[0];
				thisMeasure += Double.parseDouble(measure);
				count ++;
		
//				System.err.println("sim between "+entity1+" "+entity2+" is "+measure);
			}
		//System.out.println("number of result: "+count);
			qe.close();
			return count;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;		
	}
	public static String getMatchEntity(String entity1) {
		try{

			//System.err.println("sparql: "+entity1+" "+entity2);
			
	   		String queryString = "SELECT DISTINCT ?m WHERE { " +
			"{" +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> "+entity1+"." +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> ?m. " +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure. " +
			"} UNION " +
			"{" +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> ?m." +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> "+entity1+". " +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure. " +
			"} " +
			"}";
//	   		String queryString = "SELECT  ?measure WHERE { " +
//			"{" +
//
//			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignmentmeasure> ?measure. " +
//			"} "+
//			"}";	   		
//	   		System.err.println(queryString);
	   		QueryExecution qe = QueryExecutionFactory.create(queryString, mappingModel);
			ResultSet queryResults = qe.execSelect();
			
			String thisMatch = "";
			int count = 0 ;
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				thisMatch = qs.get("?m").toString();
//				String [] temp = measure.split("\\^\\^");
//				measure = temp[0];
//				thisMatch += Double.parseDouble(measure);
				count ++;
		
//				System.err.println("sim between "+entity1+" "+entity2+" is "+measure);
			}
		//System.out.println("number of result: "+count);
			qe.close();
			//System.err.println("measure between "+entity1+" and "+entity2+" is "+thisMeasure);
//			if(InstanceMatching.ontoMappingWeight)
//				return thisMeasure;
//			
//			if(thisMeasure > 0)
//				return 1;
			return thisMatch;
			
		}catch(Exception e){
			e.printStackTrace();
		}
//		return 0.0;
		return "";
	}
	public static double getEntitySimilarity(String entity1, String entity2, Model model) {
		String measure ="";
		try{
			//System.out.println(entity1+" vs "+entity2);
			
			
	   		String queryString = "SELECT DISTINCT ?measure WHERE { " +
			"{" +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> "+entity1+"." +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> "+entity2+". " +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure. " +
			"} UNION " +
			"{" +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> "+entity2+"." +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> "+entity1+". " +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure. " +
			"} " +
			"}";

	   		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
			ResultSet queryResults = qe.execSelect();
			
			double thisMeasure = 0.0;
			int count = 0 ;
			
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				measure = qs.get("?measure").toString();
				String [] temp = measure.split("\\^\\^");
				measure = temp[0];
				thisMeasure += Double.parseDouble(measure);
				count ++;
			}

			qe.close();
			
			if(thisMeasure > 0)
				return 1;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0.0;
	}
}
