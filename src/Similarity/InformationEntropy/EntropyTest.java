package Similarity.InformationEntropy;

import java.io.FileInputStream;
import java.util.ArrayList;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class EntropyTest {
	
	public static void main(String [] args){
		try{
			
			String onto1 = "../iimb/000/onto.owl";//"OntologyMatchingTest/Conference.owl";
			FileInputStream fstream = new FileInputStream(onto1);
			Model model = ModelFactory.createDefaultModel();
			model.read(fstream,"");
			InformationEntropy IE = new InformationEntropy(model);
			
			//String property2 = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
			String property = "http://oaei.ontologymatching.org/2012/IIMBTBOX/name";
			String property2 = "http://oaei.ontologymatching.org/2012/IIMBTBOX/gender";
			String property3 = "http://oaei.ontologymatching.org/2012/IIMBTBOX/acted_by";
			//String property = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
			ArrayList<String> properties = new ArrayList<String>();
//			properties.add(property);
			properties.add(property2);
			properties.add(property3);
			double entropy = IE.computeShannonEntropy(property);
			

			//String property2 = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
			double jointEntropy = IE.computeJointEntropy(properties);
			
			System.out.println(property+" "+entropy);
			System.out.println(property+" "+property2+" "+jointEntropy);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
