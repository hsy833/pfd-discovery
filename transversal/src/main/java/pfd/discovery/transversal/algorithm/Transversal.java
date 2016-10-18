package pfd.discovery.transversal.algorithm;

import java.util.ArrayList;

import pfd.discovery.basic.common.Attrset;
import pfd.discovery.basic.common.Operations;

public class Transversal {
	
	private int numOfAtr;
	private String[][] relations;
	
	public Transversal(String[][] rworld, int numOfAtr){
		this.numOfAtr = numOfAtr;
		this.relations = rworld;
	}

	public Attrset getDisagreeSet(int t1, int t2){
		Attrset disagree = new Attrset(this.numOfAtr);
		
		//check if t1(X) != t2(X)
		for(int j=0; j<numOfAtr; j++){
			String v1 = relations[t1][j];
			String v2 = relations[t2][j];
			
			if(!v1.equals(v2)){
				disagree.addElement(j);
			}
		}
		
		return disagree;
	}
	
	public ArrayList<Attrset> compute_trans(ArrayList<Attrset> hgraph){
		
		ArrayList<Attrset> tr = new ArrayList<Attrset>();
		ArrayList<Attrset> step_temp = new ArrayList<Attrset>();
		
		//for each edge in hyper-graph
		for(int i=0; i<hgraph.size(); i++){
			ArrayList<Integer> edgeCotent = hgraph.get(i).getContent();
				
			if(tr.isEmpty()){
				tr.ensureCapacity(edgeCotent.size());
				for(int v:edgeCotent){
					Attrset e = new Attrset(1, v);
					tr.add(e);
				}
			}else{
				step_temp.ensureCapacity(tr.size()*edgeCotent.size());
				//if tr is not empty, do union
				for(int k=0; k<tr.size();k++){
					for(int j=0; j<edgeCotent.size(); j++){
						
						int e = edgeCotent.get(j);
						if(tr.get(k).findElement(e)){
							step_temp.add(tr.get(k));
						}else{
							Attrset tmp = new Attrset(tr.get(k).getContent());
							tmp.addElement(e);
							step_temp.add(tmp);
						}
					}
				}
				
				//after union, minimize the set
				ArrayList<Attrset> minimized = Operations.minimize(step_temp);
				tr.clear();
				tr.ensureCapacity(minimized.size());
				tr.addAll(minimized);
				
				//Clearance
				step_temp.clear();
				step_temp.trimToSize();
				minimized.clear();
				minimized.trimToSize();
			}
		}
		
		tr.trimToSize();
		
		return tr;
	}
	
}
