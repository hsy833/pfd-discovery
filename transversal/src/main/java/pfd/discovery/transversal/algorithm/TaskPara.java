package pfd.discovery.transversal.algorithm;

import java.util.ArrayList;

import pfd.discovery.basic.common.Attrset;
import pfd.discovery.basic.common.Pfd;
import pfd.discovery.basic.common.Operations;

public class TaskPara implements Runnable {
	
	private static ArrayList<Pfd> result;
	private static Object lockObj; 
	private static int numOfAtr;
	private static String[][] rworld;
	private static Transversal tvl;
	
	private int currentBeta;
	private int numOfTuple;
	private int jcapacity;
	private ArrayList<Attrset> ja;//will be repeatedly used
	
	public static ArrayList<Pfd> getResult() {
		return result;
	}
	
	public static void clearResult(){
		result.clear();
		result.trimToSize();
	}
	
	public static void prepare(int capcity, int numofatr, String[][] r, Transversal t) {
		TaskPara.result = new ArrayList<Pfd>(capcity);
		lockObj = new Object();
		numOfAtr = numofatr;
		rworld = r;
		tvl = t;
	}

	public TaskPara(int currentBeta, int numOfTuple) {
		
		this.currentBeta = currentBeta;
		this.numOfTuple = numOfTuple;
		this.jcapacity = numOfTuple*(numOfTuple-1)/2;
		ja = new ArrayList<Attrset>();
	}
	
	//Computation by Transversal Algorithm
	public void run() {
		
		System.out.println("one thread");
		
		//For each A in R
		for(int j=0; j<numOfAtr; j++){
			ja.ensureCapacity(jcapacity);
			//compute JA
			for(int i=0; i<numOfTuple; i++){
				String current_value = rworld[i][j];
				//comparison to other tuples
				for(int c=i+1; c<numOfTuple; c++){
					if(!current_value.equals(rworld[c][j])){
						Attrset set_element = tvl.getDisagreeSet(i, c);
						if(Operations.isMinimal(set_element, ja)){
							ja.add(set_element);
						}else{
							set_element.getContent().clear();
							set_element=null;
						}
					}
				}
			}

			//compute KA
			ja.trimToSize();	
			
			//compute LA
			ArrayList<Attrset> la = tvl.compute_trans(ja);
			ja.clear();
			ja.trimToSize();
			
			//output discovered pFDs
			//synchronized (lockObj) {//maybe better???------------------------
				for(int n=0; n<la.size(); n++){				
					if(!la.get(n).findElement(j)){
	
						Attrset right = new Attrset(1,j);
						Pfd fd = new Pfd(la.get(n), right, currentBeta, false);
						//lock on
						synchronized (lockObj) {
							result.add(fd);
						}
					}
				}
			//}
		}
	}
	
}
