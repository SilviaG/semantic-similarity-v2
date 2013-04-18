package Similarity.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import Utils.Utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class JenaModelIndexer {
	static Model m;
	public static void main(String [] args){
		try{
			String onto1 = "OntologyMatchingTest/Library/stw.rdf";
			String onto2 = "OntologyMatchingTest/Library/thesoz.rdf";
	    	FileInputStream fstream = new FileInputStream(onto1);
	    	FileInputStream fstream2 = new FileInputStream(onto2);
			m = ModelFactory.createDefaultModel();
			//m.read(fstream,"");
			m.read(fstream,"");
			m.read(fstream2,"");
			String filename = "lib.rdf";
			OutputStream output = new FileOutputStream(filename);
			m.write(output,"N-TRIPLE");
			
			//String res = "http://zbw.eu/stw/descriptor/16981-0";

			File INDEX_DIR = new File("OntologyMatchingTest/Library/libIndex");			 
		    IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);
		    Date start = new Date();	
		    System.out.println("Indexing to directory '" +INDEX_DIR+ "'...");
		    ResIterator ritr = m.listSubjects();
//		    int count = 0;
//			while(ritr.hasNext()){
//				ritr.next();
//				count++;
//			}		
//			System.out.println("num resource: "+count);
//		    ritr = m.listSubjects();
//			System.in.read();
		    String standardfile = "OntologyMatchingTest/Library/refalign/referenceSKOS.rdf";
		    ArrayList<String> matchingSet1 = Utils.getMatchingDocs(standardfile,"entity1",0,10000000);
		    ArrayList<String> matchingSet2 = Utils.getMatchingDocs(standardfile,"entity2",0,10000000);		    
			int count = 0;
			while(ritr.hasNext()){
				String res = ritr.next().toString();
				System.out.println(count);
				StringBuilder contents= new StringBuilder();
				StringBuilder triples = new StringBuilder();
				if(matchingSet1.contains(res) || matchingSet2.contains(res)){
					System.out.println("Indexing "+count+": "+res);
					printResource(res, 2,contents,triples);
//				System.out.println("c: "+contents);
//				System.out.println("t: "+triples);
					IndexFiles.indexDocs(writer, contents.toString(), triples.toString(), res);
				}
				count ++;
			}
		      System.out.println("Optimizing...");
		      writer.optimize();
		      writer.close();
		      Date end = new Date();
		      System.out.println(end.getTime() - start.getTime() + " total milliseconds");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void printResource(String resource, int level, StringBuilder contents, StringBuilder triples){
		if(level == 0)
			return;
		Resource res = ResourceFactory.createResource(resource);
		Property p = null;
		RDFNode n = null;
		
		StmtIterator sitr = m.listStatements(res, p, n);
		while(sitr.hasNext()){
			Statement stmt = sitr.next();
//			System.out.println(stmt.toString());
			String stmtString = stmt.toString();
			String objString = stmt.getObject().toString();

			triples.append(stmt.getSubject().toString()+" "+stmt.getPredicate().toString()+" "+stmt.getObject().toString());
			triples.append("\n");
			if(stmt.getObject().isResource()){				
				printResource(objString,level - 1,contents,triples);
			}else{
				if(stmt.getPredicate().toString().toLowerCase().contains("label")&&stmt.getObject().isLiteral()){
					contents.append(IndexFiles.reviseString(objString));
					contents.append(" ");
				}
			}

		}
	}
}
