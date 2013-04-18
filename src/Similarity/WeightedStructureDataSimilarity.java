package Similarity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;

import Ontology.JenaModel;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class WeightedStructureDataSimilarity implements Similarity{
	
	public Model knowledgeModel;
	public Model mappingModel;
	public IndexReader reader;
	public static boolean debug = false;
	public Hashtable<String,Double> weightVector;
	int highlevel = 2;
	
	private final int level = 3;
	private final double MatchThreshold = 0.75;
	private final boolean ontoMappingWeight = false;
	
	public WeightedStructureDataSimilarity(String mappingFilename, String indexFilename) throws CorruptIndexException, IOException{
    	FileInputStream fstream = new FileInputStream(mappingFilename);        
    	mappingModel = ModelFactory.createDefaultModel();
    	mappingModel.read(fstream,"");
    	knowledgeModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
		fstream.close();
		setIndexReader(indexFilename);
	}
//	public WeightedStructureDataSimilarity(String mappingFilename, IndexReader reader) throws CorruptIndexException, IOException{

	public void setLevel(int level){
		highlevel = level;
	}
	public double computeSimilarity(Object object1, Object object2) {
		try{
			String ob1 = (String)object1;
			String ob2 = (String)object2;
			int docId1 = getDocId("URI", "<"+ob1+">");
			int docId2 = getDocId("URI", "<"+ob2+">");
			
			return getSimilarity(docId1,docId2,this.level);
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}


	public void setWeights(Hashtable<String, Double> weightVector) {
		this.weightVector = weightVector;
		
	}

	public Hashtable<String, Double> getFeatureSim(Object object1,Object object2) {
		
		String ob1 = (String)object1;
		String ob2 = (String)object2;
		int docId1;
		int docId2;
		try {
			docId1 = getDocId("URI", "<"+ob1+">");
			docId2 = getDocId("URI", "<"+ob2+">");
			Document doc1 = reader.document(docId1);
			Document doc2 = reader.document(docId2);
			
			String r1 = doc1.get("URI");
			String r2 = doc2.get("URI");

			
			String r1_content = doc1.get("contents");
			String r2_content = doc2.get("contents");

			
			r1_content = r1_content.replaceAll("\\n", " .\n");
			r2_content = r2_content.replaceAll("\\n", " .\n");
			
			InputStream istream = new ByteArrayInputStream(r1_content.getBytes());
			InputStream istream2 = new ByteArrayInputStream(r2_content.getBytes());
			

			
			knowledgeModel.read(istream,"","N-TRIPLE");
			knowledgeModel.read(istream2,"","N-TRIPLE");

			
			long start1 = System.currentTimeMillis();
			Hashtable<String,Double> featureSimVec = computeFeatureSimVec(r1,r2,level);
			long end1 = System.currentTimeMillis();


			istream.close();
			istream2.close();
			
			if(level == this.level){
				knowledgeModel.removeAll();
			}
			return featureSimVec;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		


		return null;
	}
	public double getSimilarity(int docId1, int docId2, int level) throws Exception{

		if(level==0)
			return 0;
		
		Document doc1 = reader.document(docId1);
		Document doc2 = reader.document(docId2);
		
		String r1 = doc1.get("URI");
		String r2 = doc2.get("URI");
	
		String r1_content = doc1.get("contents").replaceAll("\\n", " .\n");
		String r2_content = doc2.get("contents").replaceAll("\\n", " .\n");		
		
		InputStream istream = new ByteArrayInputStream(r1_content.getBytes());
		InputStream istream2 = new ByteArrayInputStream(r2_content.getBytes());
		
		knowledgeModel.read(istream,"","N-TRIPLE");
		knowledgeModel.read(istream2,"","N-TRIPLE");
	
		long start1 = System.currentTimeMillis();
		double featureSim = this.computeFeatureSimialrity(r1, r2, level - 1);
		long end1 = System.currentTimeMillis();

		istream.close();
		istream2.close();
		
		if(level == this.level){
			knowledgeModel.removeAll();
		}
		
		if(debug && level == this.level){
			System.out.println(r1+" vs "+r2+" : ");
			System.out.println("feature sim between "+r1+" and "+r2+" : "+featureSim+" time: "+(end1-start1));
		}

		return featureSim;	
	}
	public Hashtable<String,Double> computeFeatureSimVec(String resource1_url, String resource2_url, int level) throws Exception{


		
		Hashtable<String, Double> myFeatureSim = new Hashtable<String, Double>();
//		if(debug)
//		System.out.println("print r1 r2: "+resource1_url+" "+resource2_url);

		//long start1 = System.currentTimeMillis();
		resource1_url = resource1_url.replace("<","").replace(">", "");
		resource2_url = resource2_url.replace("<","").replace(">", "");
		Resource r1 = knowledgeModel.getResource(resource1_url);
		Resource r2 = knowledgeModel.getResource(resource2_url);
		
		StmtIterator r1dirStatements = JenaModel.getStatementDir(knowledgeModel, r1, null, null);
		//StmtIterator r1revStatements = JenaModel.getStatementRev(knowledgeModel, r1, null, null);
		
		StmtIterator r2dirStatements = JenaModel.getStatementDir(knowledgeModel, r2, null, null);
		//StmtIterator r2revStatements = JenaModel.getStatementRev(knowledgeModel, r2, null, null);
		
		ArrayList<String> r1statements = new ArrayList<String>();
		ArrayList<String> r2statements = new ArrayList<String>();
		
		while(r1dirStatements.hasNext()){
			Statement r1dirStatement = r1dirStatements.next();
			String r1property = r1dirStatement.getPredicate().toString();
			String r1object = r1dirStatement.getObject().toString();
			if(!r1property.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			r1statements.add(r1property+" "+r1object);
		}

		while(r2dirStatements.hasNext()){
			Statement r2dirStatement = r2dirStatements.next();
			String r2property = r2dirStatement.getPredicate().toString();
			String r2object = r2dirStatement.getObject().toString();
			if(!r2property.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			r2statements.add(r2property+" "+r2object);
		}
		
		/*
		while(r2revStatements.hasNext()){
			Statement r2revStatement = r2revStatements.next();
			String r2subject = r2revStatement.getSubject().toString();
			String r2property = r2revStatement.getPredicate().toString();
			if(!r2property.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			//r2statements.add(r2subject+" "+r2property);
				r2statements.add(r2property+" "+r2subject);
			}
		while(r1revStatements.hasNext()){
			Statement r1revStatement = r1revStatements.next();
			String r1subject = r1revStatement.getSubject().toString();
			String r1property = r1revStatement.getPredicate().toString();
			if(!r1property.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			//r1statements.add(r1subject+" "+r1property);
				r1statements.add(r1property+" "+r1subject);
		}
		*/
		
		//long end1 = System.currentTimeMillis();
		
		//System.out.println("initializing take "+(end1-start1));
		
		//long start2 = System.currentTimeMillis();
		ArrayList<String> commonFeature = new ArrayList<String>();
		ArrayList<String> testArray = new ArrayList<String>();
		double featureMatchCount = 0;
		for(int i = 0 ; i < r1statements.size(); i ++){
			String r1statement = r1statements.get(i);
			double current_obj_sim=0.0;
			String [] r1_pro_obj = r1statement.split(" ",2);

			for(int j = 0 ; j < r2statements.size(); j++){
				
				String r2statement = r2statements.get(j);				
				String [] r2_pro_obj = r2statement.split(" ",2);

				if(r1statement.equals(r2statement)){
					commonFeature.add(r1statement);
					current_obj_sim=1;
					myFeatureSim.put(r1_pro_obj[0], current_obj_sim);
					//featureMatchCount++;
					break;
				}else{
					
					

					String r1infoString = r1_pro_obj[1];
					String r2infoString = r2_pro_obj[1];
//					double property_sim = 0.0;

					double obj_sim =0.0;
					
					if(r1_pro_obj[0].equals(r2_pro_obj[0])||(getEntitySimilarity(r1_pro_obj[0],r2_pro_obj[0])>0) || isSameProperty(r1_pro_obj[0],r2_pro_obj[0])){
//						property_sim = 1;
						
						if((r1_pro_obj[1].contains("http://")) && (r2_pro_obj[1].contains("http://")) &&(!r1_pro_obj[1].contains("^^http://")) && (!r2_pro_obj[1].contains("^^http://"))){
							
							
							int doc1 = getDocId("URI", "<"+r1_pro_obj[1]+">");
							int doc2 = getDocId("URI", "<"+r2_pro_obj[1]+">");
							//long start4 = System.currentTimeMillis();
							obj_sim=getSimilarity(doc1,doc2,level);
							if(obj_sim>current_obj_sim){
								current_obj_sim=obj_sim;
								myFeatureSim.put(r1_pro_obj[0], current_obj_sim);
							}
//							if(current_obj_sim>0.4){
//								break;
//							}
							//long end4 = System.currentTimeMillis();
//							testArray.add(r1_pro_obj[1]+" "+r2_pro_obj[1]+" "+obj_sim);
							//System.out.println("obj sim takes "+(end4-start4));
							
							
						}else{
							
							r1infoString = r1_pro_obj[1].replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");
							r2infoString = r2_pro_obj[1].replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");
							
						
							if((r1infoString.contains("http://"))){
								r1infoString = getStatementString(r1infoString).replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");
							}
							else if(r2infoString.contains("http://")){
								r2infoString = getStatementString(r2infoString).replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");
							}
							//long start3 = System.currentTimeMillis();
							//obj_sim=computeTermSimilarity(r1infoString,r2infoString);
							obj_sim=computeSentenceSimilarity(r1infoString,r2infoString);
							if(obj_sim>current_obj_sim){
								current_obj_sim=obj_sim;
								myFeatureSim.put(r1_pro_obj[0], current_obj_sim);
							}
//							if(current_obj_sim>0.4){
//								break;
//							}
//							testArray.add(r1_pro_obj[1]+" "+r2_pro_obj[1]+" "+obj_sim);
							
						}
						
					}				
				}
					
			}
	//		myFeatureSim.put(r1_pro_obj[0], current_obj_sim);
	//		featureMatchCount+=(current_obj_sim*weightVector.get(r1_pro_obj[0]));
		}
		

		return myFeatureSim;
	}
	private double computeFeatureSimialrity(String resource1_url, String resource2_url, int level) throws Exception{
		ArrayList<String> r1statements = getStatements(resource1_url);
		ArrayList<String> r2statements = getStatements(resource2_url);
		
		
		double featureMatchCount = 0;
		double r1featureWeights = 0;
		double r2featureWeights = 0;
		
		for(int i = 0 ; i < r1statements.size(); i ++){
			String r1statement = r1statements.get(i);
			double current_obj_sim=0.0;
			String [] r1_pro_obj = r1statement.split(" ",2);
			String [] r2_pro_obj;

			if(weightVector!=null&&weightVector.containsKey(r1_pro_obj[0])){
				r1featureWeights += weightVector.get(r1_pro_obj[0]);
			}
			for(int j = 0 ; j < r2statements.size(); j++){
						
				String r2statement = r2statements.get(j);				
				r2_pro_obj = r2statement.split(" ",2);
				if(i==0){
					if(weightVector!=null&&weightVector.containsKey(r2_pro_obj[0])){
						r2featureWeights += weightVector.get(r2_pro_obj[0]);
					}
				}
				if(r1statement.equals(r2statement)){
//					commonFeature.add(r1statement);
					current_obj_sim=1;
					break;
				}else{
					String r1infoString = r1_pro_obj[1];
					String r2infoString = r2_pro_obj[1];
					
					double obj_sim =0.0;
					
					if(r1_pro_obj[0].equals(r2_pro_obj[0])||(getEntitySimilarity(r1_pro_obj[0],r2_pro_obj[0])>0)
							|| isSameProperty(r1_pro_obj[0],r2_pro_obj[0])) //isSameProperty should be delete in the final version
					{
						
						if((r1_pro_obj[1].contains("http://")) && (r2_pro_obj[1].contains("http://")) &&(!r1_pro_obj[1].contains("^^http://")) && (!r2_pro_obj[1].contains("^^http://"))){
							
							if(debug && !r1statement.contains("rdf-syntax-ns#type"))
								System.out.println("1 compared feature: "+r1statement+" "+r2statement+" "+obj_sim);
					
							int doc1 = getDocId("URI", "<"+r1_pro_obj[1]+">");
							int doc2 = getDocId("URI", "<"+r2_pro_obj[1]+">");

							obj_sim=getSimilarity(doc1,doc2,level);
							if(obj_sim>current_obj_sim)
								current_obj_sim=obj_sim;
							if(current_obj_sim>0.4){
								break;
							}
							
						}else{
							
							r1infoString = r1_pro_obj[1].replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");
							r2infoString = r2_pro_obj[1].replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");
							
						
							if((r1infoString.contains("http://"))){
								r1infoString = getStatementString(r1infoString).replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");	
							}
							else if(r2infoString.contains("http://")){
								r2infoString = getStatementString(r2infoString).replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");
							}
							
							obj_sim=computeSentenceSimilarity(r1infoString,r2infoString);
							
							if(debug)
								System.out.println("compared feature: r1 ("+r1statement+") r2 ("+r2statement+") "+obj_sim);
							
							
							if(obj_sim>current_obj_sim){
								current_obj_sim=obj_sim;
							}
//							if(current_obj_sim>0.4){
//								break;
//							}							
						}
						
					}				
				}
					
//				if(level==0&&current_obj_sim>1){
//					System.out.println();
//				}
			}
//			myFeatureSim.put(r1_pro_obj[0], current_obj_sim);
			if(weightVector!=null&&weightVector.containsKey(r1_pro_obj[0]))
				featureMatchCount+=(current_obj_sim*weightVector.get(r1_pro_obj[0]));
			else
				featureMatchCount+=current_obj_sim;
		}

		if(r1featureWeights <= 0)
			r1featureWeights = r1statements.size();
		
		if(r2featureWeights <= 0)
			r2featureWeights = r2statements.size();
		
		double r1uniqueFeature = r1featureWeights - featureMatchCount;//r1statements.size() - featureMatchCount;
		double r2uniqueFeature = r2featureWeights - featureMatchCount;// r2statements.size() - featureMatchCount;
		
		
//		double featureSim = 3*featureMatchCount - r1uniqueFeature - r2uniqueFeature;
		double uniqueFeature=(r1uniqueFeature+r2uniqueFeature)/2;
		

		if((r1uniqueFeature+r2uniqueFeature+featureMatchCount)==0)
			return this.MatchThreshold;
		

		
		return (featureMatchCount)/(double)(uniqueFeature+featureMatchCount);
		
	}
	private ArrayList<String> getStatements(String resource_url){
		
		resource_url = resource_url.replace("<","").replace(">", "");;
		Resource resource = knowledgeModel.getResource(resource_url);
		
		StmtIterator dirStatements = JenaModel.getStatementDir(knowledgeModel, resource, null, null);
//		StmtIterator revStatements = JenaModel.getStatementRev(knowledgeModel, resource, null, null);
		
		ArrayList<String> statements = new ArrayList<String>();
		
		while(dirStatements.hasNext()){
			Statement r1dirStatement = dirStatements.next();
			String r1property = r1dirStatement.getPredicate().toString();
			String r1object = r1dirStatement.getObject().toString();
			statements.add(r1property+" "+r1object);
		}
//		while(revStatements.hasNext()){
//			Statement r1revStatement = revStatements.next();
//			String r1subject = r1revStatement.getSubject().toString();
//			String r1property = r1revStatement.getPredicate().toString();
//			statements.add(r1property+" "+r1subject);
//		}		
		
		return statements;
	}
	private void setIndexReader(String filename) throws CorruptIndexException, IOException{
		File INDEX_DIR = new File(filename);
		reader = IndexReader.open(FSDirectory.open(INDEX_DIR), true);		
	}
	private int getDocId(String field, String value) throws IOException{
		Term idTerm = new Term(field, value);
		TermDocs docs = reader.termDocs(idTerm);
		int docId = 0;
		while(docs.next()){
			docId = docs.doc();
		}
		return docId;
	}	
	private double getEntitySimilarity(String entity1, String entity2) {
		try{			
	   		String queryString = "SELECT DISTINCT ?measure WHERE { " +
			"{" +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> <"+entity1+">." +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> <"+entity2+">. " +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure. " +
			"} UNION " +
			"{" +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> <"+entity2+">." +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> <"+entity1+">. " +
			"?s <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure. " +
			"} " +
			"}";
	   		
	   		QueryExecution qe = QueryExecutionFactory.create(queryString, mappingModel);
			ResultSet queryResults = qe.execSelect();
			
			double thisMeasure = 0.0;
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String measure = qs.get("?measure").toString();
				String [] temp = measure.split("\\^\\^");
				measure = temp[0];
				thisMeasure += Double.parseDouble(measure);
			}
		
			qe.close();
			if(this.ontoMappingWeight)
				return thisMeasure;
			
			if(thisMeasure > 0)
				return 1;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0.0;
	}
	//should not include in final version
	private boolean isSameProperty(String p1, String p2){
		p1 = p1.replaceAll("http://www.instancematching.org/IIMB2012/ADDONS#", "http://oaei.ontologymatching.org/2012/IIMBTBOX/");
		p2 = p2.replaceAll("http://www.instancematching.org/IIMB2012/ADDONS#", "http://oaei.ontologymatching.org/2012/IIMBTBOX/");
		return p1.equals(p2);		
	}
	
	
	private double computeSentenceSimilarity(String concept1, String concept2){
	
		
		if((concept1.equals("M")&&concept2.equals("Male"))||(concept2.equals("M")&&concept1.equals("Male")))
			return 1;
		
		if((concept1.equals("F")&&concept2.equals("Female"))||(concept2.equals("F")&&concept1.equals("Female")))
			return 1;
				
		boolean numeric = true;
		if(!(isNumeric(concept1) && isNumeric(concept2))){
			numeric = false;
			concept1=concept1.replaceAll("\\.", "").replaceAll("-", "").replaceAll("_", "").replaceAll(",", "").replaceAll("@.*", "");
			concept2=concept2.replaceAll("\\.", "").replaceAll("-", "").replaceAll("_", "").replaceAll(",", "").replaceAll("@.*", "");
		}
		
		String [] words1 = removeDuplicate(concept1).split(" ");
		String [] words2 = removeDuplicate(concept2).split(" ");
		String [] words = removeDuplicate(concept1+" "+concept2).split(" ");

		String newString = "";
		ArrayList<String> wordList = new ArrayList<String>();
		
		double commons = 0 ; 
		for(int i = 0 ; i < words1.length; i ++){
			for(int j =0 ; j < words2.length; j++){
				if(words1[i].toLowerCase().equals(words2[j].toLowerCase())||(StringCompare(words1[i],words2[j])>0)){
					commons++;
					break;
				}
			}
		}
		double length = (double)words.length;
		if(numeric)
			length = 1;
		
		return commons/((double)(length));
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
	private String removeDuplicate(String str){
		String [] words = str.split(" ");
		String newString = "";
		ArrayList<String> wordList = new ArrayList<String>();
		for(int i = 0 ; i < words.length; i ++){
			if(!wordList.contains(words[i])){
				wordList.add(words[i]);
				newString += words[i]+" ";
			}

		}
		return newString;
	}
	private double StringCompare(String str1, String str2){

		
		if(isNumeric(str1) && isNumeric(str2)){
			return numberCompare(Double.parseDouble(str1),Double.parseDouble(str2));
		}

		str1 = str1.replaceAll("[^a-zA-Z0-9]+"," ");
		str2 = str2.replaceAll("[^a-zA-Z0-9]+"," ");
		
		
		if(StringDistance.getSim(str1, str2) > 0){
			return 1.0;
		}
		return 0.0;
		
	}
	private double numberCompare(double num1, double num2){
		if(Math.abs(Math.abs(num1) - Math.abs(num2)) < 1)
			return 1;
		else
			return 0;
	}
	private void loadData(int docId) throws Exception{
		Document doc = reader.document(docId);
		String r1_content = doc.get("contents");
		r1_content = r1_content.replaceAll("\\n", " .\n");
		InputStream istream = new ByteArrayInputStream(r1_content.getBytes());		
		knowledgeModel.read(istream,"","N-TRIPLE");
	}
	private String getStatementString(String url) throws Exception{
		int doc = getDocId("URI", "<"+url+">");
		loadData(doc);
		
		Resource r = knowledgeModel.getResource(url);
		StmtIterator dirStatements = JenaModel.getStatementDir(knowledgeModel, r, null, null);
		StringBuilder statementString = new StringBuilder();
		while(dirStatements.hasNext()){
			Statement r1dirStatement = dirStatements.next();
			String r1property = r1dirStatement.getPredicate().toString();
			String r1object = r1dirStatement.getObject().toString().replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","");
			if(!r1property.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !r1object.contains("http://")){
				statementString.append(r1object);
				statementString.append(" ");
			}
		}

		return statementString.toString();
	}


	@Override
	public double computeSimilarity(String object1, String object2, int depth) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setFirstObjectDescription(Object description1) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setSecondObjectDescription(Object description2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Hashtable<String, Double> getFeatureSim(String object1,
			String object2, int depth) {
		// TODO Auto-generated method stub
		return null;
	}

}
