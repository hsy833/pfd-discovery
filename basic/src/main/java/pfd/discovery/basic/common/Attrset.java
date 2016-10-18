package pfd.discovery.basic.common;

import java.util.ArrayList;

/**
 * Class for representing a set of attributes, 
 * e.g. the X,Y in FD X->Y
 * 
 * Date: 20/09/2015
 * @author Senyang He
 */
public class Attrset {
	
	//variable content records attributes' indices in R
	public ArrayList<Integer> content;
	
	/**
	 * Constructors 
	 */
	public Attrset() {
		this.content = new ArrayList<Integer>();
	}
	
	public Attrset(int capacity){
		this.content = new ArrayList<Integer>(capacity);
	}
	
	public Attrset(ArrayList<Integer> original){
		this.content = new ArrayList<Integer>(original);
	}
	
	public Attrset(Attrset target){
		this.content = target.content;
	}
	
	public Attrset(int numOfElements, int right_single_element){
		this.content = new ArrayList<Integer>(numOfElements);
		this.content.add(right_single_element);
	}
	
	/**
	 * setters and getters
	 */
	public ArrayList<Integer> getContent() {
		return content;
	}

	public void setContent(ArrayList<Integer> content) {
		this.content.clear();
		//this.content.ensureCapacity(content.size());
		//this.content.addAll(content);
		this.content = new ArrayList<Integer>(content);
	}

	public int getSize(){
		return content.size();
	}
	
	/**
	 * Methods
	 */
	public static boolean includes(Attrset a, Attrset b){

		return a.getContent().containsAll(b.getContent());
	}
	
	public void addElement(int value){
		this.content.add(value);
	}
	
	public void append(Attrset target){
		//need an independent array---------------------------------------
		this.content.addAll(target.getContent());
		
	}
	
	public boolean findElement(int attribute){
		
		return this.content.contains(attribute);
	}
	
	public void removeElement(int index){
		this.content.remove(index);
	}
	
	///may need more methods to avoid direct access to content property---------
	
	
}
