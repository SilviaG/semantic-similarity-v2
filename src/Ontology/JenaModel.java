package Ontology;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class JenaModel {

	public static void printStatement(Model m){
		
	}
	
	public static void printStatement(Model m, Resource r, Property p, RDFNode o){
		printStatementDir(m,r,p,o);
		printStatementRev(m,r,p,o);
	}
	public static void printStatementDir(Model m, Resource r, Property p, RDFNode o){
		StmtIterator itr = m.listStatements(new SimpleSelector(r, p , (RDFNode)o));
		while(itr.hasNext()){
			Statement stmt = itr.nextStatement();
			System.out.println(stmt.toString());
		}	
		
	}
	public static void printStatementRev(Model m, Resource r, Property p, RDFNode o){
		StmtIterator itr = m.listStatements(new SimpleSelector(null, p , (RDFNode)r));
		while(itr.hasNext()){
			Statement stmt = itr.nextStatement();
			System.out.println(stmt.toString());
		}		
	}
	public static StmtIterator getStatementDir(Model m, Resource r, Property p, RDFNode o){
		StmtIterator itr = m.listStatements(new SimpleSelector(r, p , (RDFNode)o));
		return itr;
	}
	public static StmtIterator getStatementRev(Model m, Resource r, Property p, RDFNode o){
		StmtIterator itr = m.listStatements(new SimpleSelector(null, p , (RDFNode)r));
		return itr;
	}
}
