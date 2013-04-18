package Similarity.Main;

public class tests {
	public static void main(String [] args){
		System.out.println(nameMatch("Paul J.","John P."));
	}

	public static double nameMatch(String name1, String name2){
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
			
			if(name1parts[1].equals(name2parts[0])){
				common++;
			}
			else if(name1parts[1].charAt(0)==name2parts[0].charAt(0)){
//				System.out.println(name2parts[0].charAt(1)+" "+(name2parts[0].charAt(1)=='.'));
					if(name2parts[0].charAt(1)=='.' || name1parts[1].charAt(1)=='.')
						common++;
				}
				if(name1parts[0].equals(name2parts[1])){
					common++;
				}
				else if(name1parts[0].charAt(0)==name2parts[1].charAt(0)){
					if(name2parts[0].charAt(1)=='.' || name1parts[1].charAt(1)=='.')
						common++;			
				}
				

		if(common == 2)
			return 1;
		return 0;

	}
}
