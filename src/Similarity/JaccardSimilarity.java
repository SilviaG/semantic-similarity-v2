package Similarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class JaccardSimilarity implements Similarity{
	int highlevel = 2;
//	ArrayList<String> stopWords ;
//	ArrayList<String> garbagePattern;
	
	public JaccardSimilarity(){
//		stopWords = new ArrayList<String>(Arrays.asList((
////				"able," +
////				"about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because," +
////				"been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from," +
////				"get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just," +
////				"least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on," +
////				"only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the," +
////				"their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what," +
////				"when,where,which,while,who,whom,why,will,with,would,yet,you,your," +
//				"a,b,c,d,e,g,h,i,j,k,l,n,o,p,q,r,s,t,u,v,w,x,y,z,has").split(",")));
//		garbagePattern = new ArrayList<String>(Arrays.asList(("aa,bb,cc,dd,ee,ff,gg,hh,ii,jj,kk,oo,pp,qq,rr,uu,vv,ww," +
//				"xx,yy,zz").split(",")));
		
	}
	//assume the string passed in only contains number space and letter, and is ready for comparison.
	public double computeSimilarity(String concept1,  String concept2, int level) {
		if(concept1.equals(concept2)){
			return 1;
		}else
			return 0;
		
//		concept1 = concept1.toLowerCase().replaceAll("\"", "");
//		concept2 = concept2.toLowerCase().replaceAll("\"", "");
//		
//		
////		if(nameMatch(concept1,concept2) > 0 )
////			return 1;
//
//		if(StringCompare(concept1,concept2) > 0)
//			return 1;
//		
//		concept1 = concept1
//				//.replaceAll("<","").replaceAll(">","")
////							.replaceAll("\\^\\^http://www.w3.org/2001/xmlschema#.*","")
//							.replaceAll("http:/.*(#|/)", "")
//							.replaceAll("[^a-z0-9\\s]+"," ")
//						    .replaceAll("@.*", "");
////						    .replaceAll("[^a-z0-9\\s\\.]+"," ")
////									   .replaceAll(" \\.", "")
////									   .replaceAll("has","");
//	//	String [] org_words1= toPhrase(concept1.replaceAll("[^a-z0-9\\s]+"," ").trim()).split("\\s+");
//
//		concept2 = concept2
//				//.replaceAll("<","").replaceAll(">","")
////						   .replaceAll("\\^\\^http://www.w3.org/2001/xmlschema#.*","")
//						   .replaceAll("http:/.*(#|/)", "")
//						   .replaceAll("[^a-z0-9\\s]+"," ")
//						   .replaceAll("@.*", "");
////						   .replaceAll("[^a-z0-9\\s\\.]+"," ")
////				   		   .replaceAll(" \\.", "")
////				   		   .replaceAll("has","");
//		//String [] org_words2= toPhrase(concept2.replaceAll("[^a-z0-9\\s]+"," ").trim()).split("\\s+");
//
//		
//
//		
////		ArrayList<String> words1 = removeMeaningless(org_words1);
////		ArrayList<String> words2 = removeMeaningless(org_words2);
//		
////		ArrayList<String> words1 = new ArrayList<String> (Arrays.asList(org_words1));
////		ArrayList<String> words2 = new ArrayList<String>(Arrays.asList(org_words2));
////		if(specialMatch(concept1,concept2) > 0)
////			return 1;
//
////		concept1 = concept1.replace("January", "1")
////				.replace("February", "2")
////				.replace("March", "3")
////				.replace("April", "4")
////				.replace("May", "5")
////				.replace("June", "6")
////				.replace("July", "7")
////				.replace("August", "8")
////				.replace("September", "9")
////				.replace("October", "10")
////				.replace("November", "11")
////				.replace("December", "12");
////		concept2 = concept2.replace("January", "1")
////				.replace("February", "2")
////				.replace("March", "3")
////				.replace("April", "4")
////				.replace("May", "5")
////				.replace("June", "6")
////				.replace("July", "7")
////				.replace("August", "8")
////				.replace("September", "9")
////				.replace("October", "10")
////				.replace("November", "11")
////				.replace("December", "12");
//		
//		String [] org_words1= concept1.trim().split("\\s+");
//		String [] org_words2= concept2.trim().split("\\s+");
//		
//		double commons = 0 ; 
//		for(int i = 0 ; i < org_words1.length; i ++){
//			for(int j =0 ; j < org_words2.length; j++){
//				if((org_words1[i]).equals(org_words2[j])){
//					org_words2[j] = " ";
//					commons= commons+2;
//					break;
//				}
//			}
//
//		}
////		for(int i = 0 ; i < org_words1.length; i ++){
////			for(int j =0 ; j < org_words2.length; j++){
////				if((org_words1[i].equals(org_words2[j])  || specialMatch(org_words1[i],org_words2[j])==1)){
////					org_words2[j] = " ";
////					commons= commons+2;
////					break;
////				}
////			}
////
////		}
//		
//		
////		return commons/((double)(org_words1.length+org_words2.length));
//		return commons/((double)(org_words1.length+org_words2.length));

	}
	public void setLevel(int level){
		highlevel = level;
	}
	private double nameMatch(String name1, String name2){
		if(name1.length()<1 || name2.length()<1)
			return 0;
		
		String [] name1parts = name1.trim().split(" ");
		String [] name2parts = name2.trim().split(" ");
	
		if(name1parts.length != 2 || name2parts.length!=2)
			return 0;
 
		if(name1parts[0].length() < 2 || name2parts[0].length() < 2 || name1parts[1].length() < 2 || name2parts[1].length() < 2){
			return 0;
		}
		int common =0;
		if(name1parts[0].equals(name2parts[0])){
			common++;
		}
		else if(name1parts[0].charAt(0)==name2parts[0].charAt(0)){
//			System.out.println(name2parts[0].charAt(1)+" "+(name2parts[0].charAt(1)=='.'));
				if(name2parts[0].charAt(1)=='.' || name1parts[0].charAt(1)=='.')
					common++;
			}

			if(name1parts[1].equals(name2parts[1])){
				common++;
			}
			else if(name1parts[1].charAt(0)==name2parts[1].charAt(0)){
				if(name2parts[1].charAt(1)=='.' || name1parts[1].charAt(1)=='.')
					common++;			
			}
			
			if(common == 2)
				return 1;
			
//			if(name1parts[1].equals(name2parts[0])){
//				common++;
//			}
//			else if(name1parts[1].charAt(0)==name2parts[0].charAt(0)){
////				System.out.println(name2parts[0].charAt(1)+" "+(name2parts[0].charAt(1)=='.'));
//					if(name2parts[0].charAt(1)=='.' || name1parts[1].charAt(1)=='.')
//						common++;
//				}
//				if(name1parts[0].equals(name2parts[1])){
//					common++;
//				}
//				else if(name1parts[0].charAt(0)==name2parts[1].charAt(0)){
//					if(name2parts[0].charAt(1)=='.' || name1parts[1].charAt(1)=='.')
//						common++;			
//				}
				

		if(common == 2)
			return 1;
		return 0;

	}
	private double StringCompare(String str1, String str2){
		
		if(isNumeric(str1) && isNumeric(str2)){
			return numberCompare(Double.parseDouble(str1),Double.parseDouble(str2));
		}
		
//		double sm = specialMatch(str1,str2);
//		if(sm > 0)
//			return sm;
		//double strDis = StringDistance.getSim(str1, str2);
		if(str1.equals(str2)||StringDistance.getSim(str1, str2) > 0)
			return 1.0;
		
		return 0.0;
	
	}

	private double specialMatch(String v1, String v2){
		
		//gender match
	//	System.out.println("v1 "+v1+" v2 "+v2);
		if(v1.length() < 1 || v2.length() < 1)
			return 0;
		if(v1.equals("f") && v2.equals("female"))
			return 1;
		else if(v1.equals("m") && v2.equals("male"))
			return 1;
		else if (v2.equals("f") && v1.equals("female"))
			return 1;
		else if (v2.equals("m") && v1.equals("male"))
			return 1;
//		if((v1.equals("f")||v1.equals("m")) && (v1.equals(""+v2.charAt(0)) && (v2.equals("male") ||v2.equals("female")))){
//					return 1;
//		}
//		else if((v2.equals("f")||v2.equals("m")) && (v2.equals(""+v1.charAt(0)) && (v1.equals("male") ||v1.equals("female")))){
//					return 1;
//		}
		
		
//		if(v1.length()==1 && v1.equals(""+v2.charAt(0))){
//			return 1;
//		}
//		else if(v2.length()==1 && v2.equals(""+v1.charAt(0))){
//			return 1;
//		}

		
		return 0;
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
	private double numberCompare(double num1, double num2){
		if(Math.abs(num1 - num2) < 0.1)
			return 1;
		else
			return 0;
	}
//	private ArrayList<String> removeMeaningless(String [] str){
//
//		ArrayList<String> returnString = new ArrayList<String>();	
//		for(int i =0;i <str.length;i++){
//			boolean inGarbage = false;
//			for(String gstr:garbagePattern){
//				if(str[i].contains(gstr)){
//					inGarbage = true;
//					break;
//				}
//			}
//			
//			if(inGarbage)
//				continue;
//			
//			boolean inStopWord = false;
//			for(String substr:stopWords){
//				if(str[i].equals(substr)){
//					inStopWord=true;
//					break;
//				}
//			}
//			if(!inStopWord&&str[i].length()>0)
//				returnString.add(str[i]);
//		}
//
//		return returnString ;
//	}
	

	@Override
	public void setWeights(Hashtable<String, Double> weightVector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Hashtable<String, Double> getFeatureSim(String object1,
			String object2, int level) {

		return null;
	}
	@Override
	public void setFirstObjectDescription(Object description1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setSecondObjectDescription(Object description2) {
		// TODO Auto-generated method stub
		
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
