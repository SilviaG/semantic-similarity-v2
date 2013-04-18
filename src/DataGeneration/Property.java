package DataGeneration;

public class Property {
	
	String property;
	String type;
	
	public Property(String p, String t){
		property = p;
		type = t;
	}
	
	public String getProperty(){
		return property;
	}
	
	public String getType(){
		return type;
	}

}
