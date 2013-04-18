package Similarity.Matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import Similarity.Similarity;
import Utils.Utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class OntologyEntityMatcher implements WebOfDataMatcher {
	Model model;
	Similarity similarity;
	int level;
	String url;
	double score;
	ArrayList<String> mlist;
    int numMatches;
    Hashtable<String, ArrayList<String>> thisDescriptions ;
	public OntologyEntityMatcher(Model m, Similarity s, int level, ArrayList<String> mlist){
		model = ModelFactory.createOntologyModel();
		model = processModel(m);
		
		similarity = s;
		this.level = level;
		score = 0;
		url = "";
		this.mlist = mlist;
		numMatches = 3;
		thisDescriptions = new Hashtable<String, ArrayList<String>>();
		Utils.getAllDescriptions(model,thisDescriptions);
	}	
	public Model processModel(Model model){
		
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
			
			if(newObject.equals(""))
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
	public Hashtable<String, Double> findMatches(String url, Hashtable<String, ArrayList<String>> description) {
		double maxSimScore = 0.0;
	//	ArrayList<String> matches = new ArrayList<String> ();
		Hashtable<String, Double> match_score = new Hashtable<String, Double> ();
	//	String matchRes = "";
		ResIterator subjects = model.listSubjects();
		Hashtable<String, ArrayList<String>> thisDescriptions = new Hashtable<String, ArrayList<String>>();
		Utils.getAllDescriptions(model,thisDescriptions);
		ArrayList<String> processed = new ArrayList<String>();
		while(subjects.hasNext()){
			Resource subject = subjects.next();
			if(!mlist.contains(subject.toString()))
				continue;
			
			similarity.setFirstObjectDescription(thisDescriptions);
			similarity.setSecondObjectDescription(description);
			String sub = subject.toString();
			
			double score = similarity.computeSimilarity(sub,url,level);
			if(score == maxSimScore && score > 0){
				match_score.put(sub, score);
			}else if(score > maxSimScore){
				match_score.clear();
				maxSimScore = score;
				match_score.put(sub, score);
			}
		}
//		for(String key:match_score.keySet()){
//			System.out.println(match_score.get(key));
//		}
		return match_score;
	}

	public String findMatch(String url, Hashtable<String, ArrayList<String>> description){
		double maxSimScore = 0.0;
		String matchRes = "";
		ResIterator subjects = model.listSubjects();

		
		while(subjects.hasNext()){
			Resource subject = subjects.next();
			if(!mlist.contains(subject.toString()))
				continue;
			
			similarity.setFirstObjectDescription(description);
			similarity.setSecondObjectDescription(thisDescriptions);
			double score = similarity.computeSimilarity(url,subject.toString(),level);
			if(score > maxSimScore){
				maxSimScore = score;
				matchRes = subject.toString();
				
			}
		}
		score = maxSimScore;
		this.url = url;
		return matchRes;
	}
	public double getMatchScore(String url, Hashtable<String, ArrayList<String>> description){
		if(!this.url.equals(url))
			findMatch(url,description);
		return score;
	}
	public void getAllDescriptions( Hashtable<String, ArrayList<String>> descriptions){
		StmtIterator stmtItr = model.listStatements();
		
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
				
				descriptions.put(subject, statements);					
			}
//			if(descriptions.keySet().contains(object)){
//				ArrayList<String> statements = descriptions.get(object);;
//				statements.add(stmtString);
//				descriptions.put(object, statements);			
//			}else{
//				ArrayList<String> statements = new ArrayList<String>();
//				statements.add(stmtString);
////				System.out.println(stmtString);
//				String label = toPhrase(object.replaceAll("http:/.*?#|/", ""));
////				System.out.println(subject+" http://www.w3.org/2000/01/rdf-schema#label "+label);
////				statements.add(object+" http://www.w3.org/2000/01/rdf-schema#label "+label);
//				descriptions.put(object, statements);					
//			}
		}


	}
	public static String toPhrase(String str) {
		   String ret = "";
		   for (int i = 0; i != str.length(); ++i) {
		       char c = str.charAt(i);
		       if (c >= 'A' && c <= 'Z') ret += "_"+(char)(c-'A'+'a');
		       else ret += c;
		   }
		   return ret;
		}

}
