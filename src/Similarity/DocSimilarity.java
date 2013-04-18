package Similarity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.math.linear.OpenMapRealVector;
import org.apache.commons.math.linear.RealVectorFormat;
import org.apache.commons.math.linear.SparseRealVector;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.FSDirectory;


public class DocSimilarity implements Similarity{


	private Map<String, Integer> terms;
	private DocVector myDocV;
	private IndexReader reader1;
	private IndexReader reader2;
	
	public DocSimilarity(int docId, IndexReader reader, Map<String, Integer> terms){
		
		try{

		    this.terms = terms;
		    myDocV = this.getDocVector(docId, reader);
		    
		}catch(Exception e){
			
		}
	}
	public DocSimilarity(){
		
	}
	public DocSimilarity(Map<String, Integer> terms, IndexReader reader, IndexReader reader2){

		    this.terms = terms;
		    this.reader1 = reader;
		    this.reader2 = reader2;
	}
//	
  public double compareTo(int docId, IndexReader reader) throws Exception{
	  DocVector thisDocV = this.getDocVector(docId, reader);
	  return getCosineSimilarity(myDocV, thisDocV);
  }
  
  public double compareDocs(int doc1,int doc2) throws Exception{
	  DocVector doc1Vec = this.getDocVector(doc1, reader1);
	  DocVector doc2Vec = this.getDocVector(doc2, reader2);
	  return getCosineSimilarity(doc1Vec, doc2Vec);
  }
  
  private DocVector getDocVector(int docId, IndexReader reader) throws Exception{
	  DocVector docV = new DocVector(terms);  
	  
	  TermFreqVector[] tfvs = reader.getTermFreqVectors(docId);
	  
      for (TermFreqVector tfv : tfvs) {
          String[] termTexts = tfv.getTerms();
          int[] termFreqs = tfv.getTermFrequencies();
          for (int j = 0; j < termTexts.length; j++) {
        	
            docV.setEntry(termTexts[j], termFreqs[j]);
          }
        }	 

      docV.normalize();
      
	  return docV;
  }
  
  private double getCosineSimilarity(DocVector d1, DocVector d2) {
	  //System.out.println("compute cosine: "+d1.vector.dotProduct(d2.vector)+"  "+d1.vector.getNorm() * d2.vector.getNorm());
    return (d1.vector.dotProduct(d2.vector)) /
      (d1.vector.getNorm() * d2.vector.getNorm());
  }

  private class DocVector {
    public Map<String,Integer> terms;
    public SparseRealVector vector;
    
    public DocVector(Map<String,Integer> terms) {
      this.terms = terms;
      this.vector = new OpenMapRealVector(terms.size());
    }
    
    public void setEntry(String term, int freq) {
      if (terms.containsKey(term)) {
        int pos = terms.get(term);
        vector.setEntry(pos, (double) freq);
      }
    }
    
    public void normalize() {
      double sum = vector.getL1Norm();
      vector = (SparseRealVector) vector.mapDivide(sum);
    }
    
    public String toString() {
      RealVectorFormat formatter = new RealVectorFormat();
      return formatter.format(vector);
    }
  }

@Override
public double computeSimilarity(String object1, String object2, int depth) {
	
	try {
//		System.out.println(object1+" "+object2);
		//object1 = "http://www.instancematching.org/IIMB2012/ADDONS#item4803701898110658090";
		int docId1=getDocId("url", "<"+object1+">", reader1);
		int docId2=getDocId("url", "<"+object2+">", reader2);
//		int docId1=getDocId("url", object1, reader1);
//		int docId2=getDocId("url", object2, reader2);
		return compareDocs(docId1,docId2);
	} catch (Exception e) {
		e.printStackTrace();
	}
	return 0;
}
private int getDocId(String field, String value, IndexReader reader) throws IOException{
	Term idTerm = new Term(field, value);
	TermDocs docs = reader.termDocs(idTerm);
	int docId = 0;
	while(docs.next()){
		docId = docs.doc();
	}
	return docId;
}	
@Override
public void setFirstObjectDescription(Object description1) {
	try{
		File f = new File((String)description1);
		reader1 = IndexReader.open(FSDirectory.open(f), true);
	}catch(Exception e){
		e.printStackTrace();
	}	
}
@Override
public void setSecondObjectDescription(Object description2) {
	try{
		File f = new File((String)description2);
		reader2 = IndexReader.open(FSDirectory.open(f), true);
	}catch(Exception e){
		e.printStackTrace();
	}
}
@Override
public void setWeights(Hashtable<String, Double> weightVector) {
	// TODO Auto-generated method stub
	
}
@Override
public Hashtable<String, Double> getFeatureSim(String object1, String object2,
		int depth) {
	// TODO Auto-generated method stub
	return null;
}
@Override
public void setLevel(int level) {
	// TODO Auto-generated method stub
	
}
}

