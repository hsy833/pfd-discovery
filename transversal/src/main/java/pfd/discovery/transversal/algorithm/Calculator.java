package pfd.discovery.transversal.algorithm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pfd.discovery.basic.common.Attrset;
import pfd.discovery.basic.common.Pfd;
import pfd.discovery.basic.common.Operations;

/**
 * Calculator for Transversal Algorithm including Sequence and Parallel Computation
 * 
 * Date: 20/09/2015
 * @author Senyang He
 */
public class Calculator {
	
	/**
	 * MEASUREMENTS
	 */
	public double timeAlgo;
	public double timeCnn;
	public int sizeBefore;
	
	/**
	 * ATTRIBUTES
	 */
	private int tNumOfAttribute;
	private int numberOfDegree;//numberOfdegree/k means the possible degree, alpha(k+1) means impossible
	private int possLhs;
	private Transversal tvl;
	
	private String[] R;
	private int[] worldSize;
	private String[][] rworld;
	
	@SuppressWarnings("rawtypes") private ArrayList[] jHypergraph;
	
	
	/**
	 * METHODS
	 */
	public String[] getR() {
		return R;
	}
	
	public String[][] getRworld() {
		return rworld;
	}
	
	public void loadDataSet(String filePath, int totalLines, int numOfDegree, int numA, String delimiter, int[] degreeArray){
		
		BufferedReader reader = null;String tuple = "";
		this.tNumOfAttribute = numA;
		rworld = new String[totalLines][tNumOfAttribute+1];
		
		//if(numOfDegree==1){ seed = 0L;}//258649L
		//Random rnd = new Random(seed);
		int rowId=0;
		try{
			reader = new BufferedReader(new FileReader(filePath));
			
			while((tuple=reader.readLine())!=null && rowId<totalLines){
				
				String[] values = null;
				if(delimiter.equals(",")){
					values = tuple.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				}else{
					values = tuple.split(delimiter);
				}
				System.arraycopy(values, 0, rworld[rowId], 0, values.length);
				
				String alpha = Integer.toString(degreeArray[rowId]);
				rworld[rowId][tNumOfAttribute] = alpha;//---
				rowId++;
			}
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//sort rworld, ignored when kedgree=1
		if(numOfDegree>1){
			Arrays.sort(rworld, Operations.degreeCompare);
		}
	}
	
	public void setParameters(int ndegree){
		
		this.numberOfDegree = ndegree;
		//this.tNumberOfTuple = rworld.length;
		//double p2 = Math.pow(2.0, (double)(tNumOfAttribute-1));---------------------------------------
		this.possLhs = 100000;//(int) ((p2-1)*tNumOfAttribute);-----------------------------------------
		
		//-------------------------------------
		jHypergraph = new ArrayList[tNumOfAttribute];//first time need to use add()
		for(int v=0;v<tNumOfAttribute;v++){jHypergraph[v] = new ArrayList<Attrset>();}
		
		worldSize = new int[numberOfDegree];
		for(int k=0; k<numberOfDegree; k++){
			String alpha_degree = Integer.toString(k+1);
			
			int size_counter = 0;			
			for(String[] tuple : rworld){
				if(alpha_degree.equals(tuple[tNumOfAttribute])){
					size_counter++;
				}
			}
			
			worldSize[k] = size_counter;
		}
		
		this.tvl = new Transversal(this.rworld, this.tNumOfAttribute);
	}
	
	/**
	 * Function for implementing algorithms from their own classes 
	 * 
	 * Output: pFDs without cover for each attribute
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Pfd> processSeq(int current_degree, int startIdx, int numOfTuple){
		
		ArrayList<Pfd> result = new ArrayList<Pfd>(possLhs);
		int jcapacity = numOfTuple*(numOfTuple-1)/2;
		//For each A from R
		for(int j=0; j<R.length; j++){
			jHypergraph[j].ensureCapacity(jcapacity);
			
			//compute JA
			for(int i=startIdx; i<startIdx+numOfTuple; i++){
				String current_value = rworld[i][j];
				//comparison to previous tuples
				for(int c=0; c<startIdx;c++){
					
					if(!current_value.equals(rworld[c][j])){
						Attrset set_element = tvl.getDisagreeSet(i, c);
						jHypergraph[j].add(set_element);
					}
				}
				
				//comparison to new tuples
				for(int c=i+1; c<startIdx+numOfTuple; c++){
					if(!current_value.equals(rworld[c][j])){
						Attrset set_element = tvl.getDisagreeSet(i, c);
						jHypergraph[j].add(set_element);
					}
				}
			}
			
			//compute KA
			ArrayList<Attrset> ka = Operations.minimize(jHypergraph[j]);
			jHypergraph[j].clear();jHypergraph[j].addAll(ka);
			ka=null;jHypergraph[j].trimToSize();
			
			//compute LA
			ArrayList<Attrset> la = tvl.compute_trans(jHypergraph[j]);
			
			//output the discovered pFDs
			int beta = numberOfDegree + 1 - current_degree;
			for(int n=0; n<la.size(); n++){
				
				if(!la.get(n).findElement(j)){

					Attrset right = new Attrset(1,j);
					Pfd fd = new Pfd(la.get(n), right, beta, false);
					result.add(fd);
				}
			}
			
			la.clear();
			la=null;
		}
		
		return result;
	}
	
	/**
	 * Large-scale version for process
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Pfd> largeSeq(int current_degree, int startIdx, int numOfTuple){
		
		ArrayList<Pfd> result = new ArrayList<Pfd>(possLhs);
		int jcapacity = numOfTuple*(numOfTuple-1)/2;
		//For each A from R
		for(int j=0; j<tNumOfAttribute; j++){
			jHypergraph[j].ensureCapacity(jcapacity);
			
			//compute JA
			for(int i=startIdx; i<startIdx+numOfTuple; i++){
				String current_value = rworld[i][j];

				//comparison to previous tuples
				for(int c=0; c<startIdx;c++){
					if(!current_value.equals(rworld[c][j])){
						
						Attrset set_element = tvl.getDisagreeSet(i, c);
						if(Operations.isMinimal(set_element, jHypergraph[j])){
							jHypergraph[j].add(set_element);
						}else{
							set_element.getContent().clear();
							set_element=null;
						}
					}
				}
				
				//comparison to new tuples
				for(int c=i+1; c<startIdx+numOfTuple; c++){
					if(!current_value.equals(rworld[c][j])){
						
						Attrset set_element = tvl.getDisagreeSet(i, c);
						if(Operations.isMinimal(set_element, jHypergraph[j])){
							jHypergraph[j].add(set_element);
						}else{
							set_element.getContent().clear();
							set_element=null;
						}
					}
				}
			}
			
			//compute KA
			jHypergraph[j].trimToSize();
			
			//For special condition, when an attribute has only one value, no disagree set
			if(jHypergraph[j].size()==0){
				int b = numberOfDegree + 1 - current_degree;
				Pfd specialCase = new Pfd(new Attrset(), new Attrset(1,j), b, false);
				result.add(specialCase);
				continue;
			}
			
			//compute LA
			ArrayList<Attrset> la = tvl.compute_trans(jHypergraph[j]);
			
			//output the discovered pFDs
			int beta = numberOfDegree + 1 - current_degree;
			for(int n=0; n<la.size(); n++){
				if(!la.get(n).findElement(j)){

					Attrset right = new Attrset(1,j);
					Pfd fd = new Pfd(la.get(n), right, beta, false);
					result.add(fd);
				}
			}
			
			la.clear();
			la=null;
		}
		
		return result;
	}
	
	/**
	 * Function for cleaning memory
	 */
	@SuppressWarnings("unchecked")
	private void cleanMem(){
		for(ArrayList<Attrset> element:jHypergraph){
			element.clear();
			element.trimToSize();
		}
	}
	
	/**
	 * Function for implementing all discovery algorithms sequentially
	 * 
	 * Return array of pFDs with canonical cover
	 */
	public ArrayList<Pfd> computePFDSeq(){
		ArrayList<Pfd> sigma = new ArrayList<Pfd>(possLhs*numberOfDegree);
		
		long startTime = System.nanoTime();
		
		for(int k=0; k<numberOfDegree; k++){//Discover all possible pFDs
			
			int startIdx = 0;
			int numOfTuple = this.worldSize[k];
			
			if(k!=0){
				for(int a=0; a<k; a++){
					startIdx += worldSize[a];
				}
			}
			
			ArrayList<Pfd> discovered = this.largeSeq(k+1, startIdx, numOfTuple);
			sigma.addAll(discovered); discovered=null;
		}
		
		this.cleanMem();
		long midTime = System.nanoTime();
		
//		long endTime = System.nanoTime();
		this.timeAlgo = (midTime - startTime)/1000000.0;
//		this.timeCnn = (endTime - midTime)/1000000.0;
		
		sizeBefore = 0;
		for(Pfd f : sigma){
			sizeBefore += f.left.getSize()+f.right.getSize();
		}
		
		//return result;
		return sigma;
	}
	
	public ArrayList<Pfd> computePFDPara(){
		
		TaskPara.prepare(possLhs*numberOfDegree, tNumOfAttribute, rworld, tvl);
		ExecutorService executor = Executors.newFixedThreadPool(5);//numberOfDegree
		
		long startTime = System.nanoTime();
		
		for(int k=1; k<=numberOfDegree; k++){
			
			int numOfTuple = 0, current_beta = numberOfDegree + 1 - k;
			for(int a=0; a<k; a++){ numOfTuple += worldSize[a];}
			
			TaskPara task = new TaskPara(current_beta, numOfTuple);
			executor.submit(task);
		}

		executor.shutdown();
		try {
			executor.awaitTermination(2, TimeUnit.HOURS);
		} catch (InterruptedException e) {}
		
		long midTime = System.nanoTime();
		
//		Canonical cover = new Canonical();
//		ArrayList<Pfd> result = cover.cnnCover(TaskPara.getResult(), numberOfDegree);
		
		//long endTime = System.nanoTime();
		this.timeAlgo = (midTime - startTime)/1000000.0;
//		this.timeCnn = cover.cnnTime;//(endTime - midTime)/1000000.0;
		
		//return result;
		return TaskPara.getResult();
	}
}
