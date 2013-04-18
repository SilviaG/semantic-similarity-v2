package Learning;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.sf.javaml.core.AbstractInstance;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import libsvm.LibSVM;

public class libsvmClassifier {
	public static void main(String [] args){
		LibSVM libsvm = new LibSVM();
		
		ArrayList<Instance> instances = new ArrayList<Instance>();
		String filename = "simVec075.txt";
		Instance myInst = new SparseInstance();
		try{
	        FileInputStream fstream = new FileInputStream(filename);
	        DataInputStream in = new DataInputStream(fstream);
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        String instLine;
	        int index = 0 ;
	        while ((instLine = br.readLine()) != null )  {
	        	Instance inst = new SparseInstance(); 
	        	String [] data = instLine.split("\t");
	        	index ++;
	        	boolean skip = false;
	        	boolean first = true;
	        	for(int i = 0 ; i < data.length - 1; i++){
	        		String score = data[i];

	        		if(Double.parseDouble(score)>0){
	        			if(first){
	        				skip = true;
	        				first =false;
	        			}
	        		}
	        		if(!skip){
	        			inst.put(i, Double.parseDouble(score));
	        		}else{
	        			inst.put(i, 0.0);
	        		}
	        		//inst.add(Double.parseDouble(score));
	        		if(index==581){
	        			if(!skip)
	        				myInst.put(i, Double.parseDouble(score));
	        			else
	        				myInst.put(i, 0.0);
	        		}
	        		skip=false;

	        	}
	        	inst.setClassValue(data[data.length-1]);
	        	instances.add(inst);
//        		if(index==3){
//        			myInst.setClassValue(data[data.length-1]);
//        		}
	        }
			
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("start training....");
		Dataset data = new DefaultDataset(instances) ;
		libsvm.buildClassifier(data);
		double [] weights = libsvm.getWeights();
		for(double w:weights){
			System.out.print(w+" ");
		}
		System.out.println();
		
		System.out.println(libsvm.classify(myInst));
		
	}

}
