package pfd.discovery.basic.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import pfd.discovery.basic.common.Attrset;
import pfd.discovery.basic.common.Pfd; 

public class Operations {

//  Java vector has containsAll() already
//	public static boolean isSubset(Vector<Integer> b, Vector<Integer> a){
//		
//		return false;
//		
//	}
	
	//comparator for sorting by size
	public static Comparator<Attrset> AttrsetSizeCompare
									= new Comparator<Attrset>(){
		
		public int compare(Attrset a, Attrset b){
			int sizeofa = a.getSize();
			int sizeofb = b.getSize();
			
			return sizeofa - sizeofb;
			
		}
	};
	
	//comparator for sorting by degree
	public static Comparator<String[]> degreeCompare
									= new Comparator<String[]>(){

		public int compare(String[] t1, String[] t2) {
			
			int alpha1 = Integer.parseInt(t1[t1.length-1]);
			int alpha2 = Integer.parseInt(t2[t2.length-1]);
			
			return alpha1-alpha2;
		}
										
	};
	
	//comparator for Pfd
	public static Comparator<Pfd> pfdDegreeCompare
									= new Comparator<Pfd>(){
		
		public int compare(Pfd fd1, Pfd fd2) {
			
			int value = fd1.certainty-fd2.certainty;
			//if they has same degree, then compare the size of them
			if(value==0){
				value = fd1.getFullSize() - fd2.getFullSize();
			}
			
			return value;
		}
	};
	
	public static boolean sameAttrset(Attrset a, Attrset b){
		
		if(a.getSize()==b.getSize() && Attrset.includes(a, b)){return true;}
		else {return false;}
	}
	
	public static ArrayList<Attrset> minimize(ArrayList<Attrset> targetSet){
		
		int tLength = targetSet.size();
		targetSet.sort(AttrsetSizeCompare);
		boolean[] del = new boolean[tLength];
		Arrays.fill(del, false);
		
		int counter=tLength;
		for(int m=0; m<tLength; m++){
			Attrset target = targetSet.get(m);
			
			int n=0;
			while(target.getSize()>0 && n<tLength && del[m]==false){
				
				if(m!=n && del[n]==false){
					if(Attrset.includes(target, targetSet.get(n))){
						del[m]=true;
						counter--;
					}
				}
				n++;
			}
		}
		
		//The reason: the times of removing are much more than times of adding 
		ArrayList<Attrset> result = new ArrayList<Attrset>(counter);
		for(int i=0;i<tLength;i++){
			if(del[i]==false){
				result.add(targetSet.get(i));
			}			
		}
		
		//Clearance
		del=null;
		
		return result;
	}
	
	public static boolean isMinimal(Attrset newY, ArrayList<Attrset> currentJa){
		
		//check if the subsets of Y exists
		for(Attrset currentX : currentJa){
			if(newY.getSize() >= currentX.getSize()){
				if(Attrset.includes(newY, currentX)){return false;}
			}
		}
		
		//check if Y is some elements' subset
		for(int i = currentJa.size()-1;i>=0;i--){
			//if(currentJa.get(i).includes(newY)){
			if(Attrset.includes(currentJa.get(i), newY)){
				currentJa.remove(i);
			}
		}
		
		return true;
	}
}
