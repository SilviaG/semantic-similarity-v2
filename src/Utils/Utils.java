package Utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;


import Similarity.Similarity;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Utils {
	
	public static String toPhrase(String str) {
		String ret = "";
		for (int i = 0; i != str.length(); ++i) {
		    char c = str.charAt(i);
		    if (c >= 'A' && c <= 'Z') ret += "_"+(char)(c-'A'+'a');
		    else ret += c;
		}
		return ret;
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
	   		
	   		Query query = QueryFactory.create(queryString);
	   		QueryExecution qe = QueryExecutionFactory.create(query, m);
			ResultSet queryResults = qe.execSelect();
		//	queryResults.
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String property = qs.get("?p").toString();
				properties.add(property);

			}
			qe.close();			
		}catch(Exception e){
			e.printStackTrace();
		}
		if(!properties.contains("http://www.w3.org/2000/01/rdf-schema#label"))
		properties.add("http://www.w3.org/2000/01/rdf-schema#label");
		if(!properties.contains("http://www.w3.org/2000/01/rdf-schema#isDomainOf"))
		properties.add("http://www.w3.org/2000/01/rdf-schema#isDomainOf");
		if(!properties.contains("http://www.w3.org/2000/01/rdf-schema#isRangeOf"))
		properties.add("http://www.w3.org/2000/01/rdf-schema#isRangeOf");
		return properties;		
	}
	public static String formatOutput(String e1, String e2, String notes){
		String output = "<match>\n";
		output += "<entity1 rdf:resource=\"";
		output += e1;
		output += "\" />\n";
		output += "<entity2 rdf:resource=\"";
		output += e2;
		output += "\" />\n";
		output += "<Note>";
		output += notes;
		output += "</Note>\n";
		output += "</match>\n";
		return output;
		
	}
	public static ArrayList<String> getMatchingDocs(String filename,String entityType,int startindex, int endindex){
		ArrayList<String> entities= new ArrayList<String>();
//		System.out.println(filename);
		  try{

			  FileInputStream fstream = new FileInputStream(filename);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  int currentindex = 1;
			  while ((strLine = br.readLine()) != null && currentindex <= endindex)   {

				  if(strLine.contains(entityType)){
					  
					  if(currentindex >= startindex){
						  String entity = "";
						  if(strLine.contains("\"")){
							  entity=strLine.substring(strLine.indexOf('"')+1,strLine.lastIndexOf('"'));
						  }
						  if(strLine.contains("'")){
							  entity=strLine.substring(strLine.indexOf('\'')+1,strLine.lastIndexOf('\''));
						  }
						   entities.add(URLDecoder.decode(entity));
//						   System.out.println(URLDecoder.decode(entity));
					  }
					  currentindex ++;
				  }
			  }

			  in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
		
		
		return entities;
	}
	public static void getAllDescriptions(Model model, Hashtable<String, ArrayList<String>> descriptions){
		StmtIterator stmtItr = model.listStatements();
		ArrayList<Statement> newStatements = new ArrayList<Statement>();
		while(stmtItr.hasNext()){
			Statement stmt = stmtItr.next();		
			String stmtString = stmt.toString().replaceAll(",", "").replaceAll("\\[", "").replaceAll("\\]", "");
			String subject = stmt.getSubject().toString();
			String object = stmt.getObject().toString();
			
			
			if(descriptions.keySet().contains(subject)){
				ArrayList<String> statements = descriptions.get(subject);;
				statements.add(stmtString);				
				descriptions.put(subject, statements);
			}else{
				ArrayList<String> statements = new ArrayList<String>();
				statements.add(stmtString);
				String label = toPhrase(subject.replaceAll("http:/.*?#|/", ""));
//				System.out.println(subject+" http://www.w3.org/2000/01/rdf-schema#label "+label);				
				statements.add(subject+" http://www.w3.org/2000/01/rdf-schema#label "+label);
				Property newProperty = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
				Literal newObject = ResourceFactory.createPlainLiteral(label);
				Statement newStatement = ResourceFactory.createStatement(stmt.getSubject(), newProperty, newObject);
				newStatements.add(newStatement);
				descriptions.put(subject, statements);					
			}

//			if(stmtString.contains("http://www.w3.org/2000/01/rdf-schema#range")||stmtString.contains("http://www.w3.org/2000/01/rdf-schema#domain")){
//				if(stmtString.contains("http://www.w3.org/2000/01/rdf-schema#range")){
//					stmtString = object +" http://www.w3.org/2000/01/rdf-schema#isRangeOf " + subject ;
//				}else{
//					stmtString = object +" http://www.w3.org/2000/01/rdf-schema#isDomainOf " + subject ;
//				}
//				if(descriptions.keySet().contains(object)){
//					ArrayList<String> statements = descriptions.get(object);
//					statements.add(stmtString);
//					descriptions.put(object, statements);			
//				}else{
//					ArrayList<String> statements = new ArrayList<String>();
//					statements.add(stmtString);
//					String label = toPhrase(object.replaceAll("http:/.*?#|/", ""));
//					statements.add(object+" http://www.w3.org/2000/01/rdf-schema#label "+label);
//					descriptions.put(object, statements);					
//				}
//			}
			
		}
		model.add(newStatements);

	}
	public static int getDocId(IndexReader reader, String field, String value) throws IOException{
		Term idTerm = new Term(field, value);
		TermDocs docs = reader.termDocs(idTerm);
		int docId = 0;
		while(docs.next()){
			docId = docs.doc();
		}
		return docId;
	}	
}
