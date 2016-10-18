package pfd.discovery.basic.common;

public class Pfd {
	public Attrset left;
	public Attrset right;
	public int certainty;
	public boolean redundant;
	
	public Pfd(Pfd target){
		this.left = target.left;
		this.right = target.right;
		this.certainty = target.certainty;
		this.redundant = target.redundant;
	}
	
	public Pfd(Attrset x, Attrset y, int c, boolean red){
		this.left = x;
		this.right = y;
		this.certainty = c;
		this.redundant = red;
	}

	public void setRedundant() {
		this.redundant = true;
		//this.left.getContent().clear();
		//this.right.getContent().clear();
	}

	public boolean sameAs(Pfd target) {
		
		boolean lFlag = Operations.sameAttrset(this.left, target.left);
		boolean rFlag = Operations.sameAttrset(this.right, target.right);
		boolean cFlag = (this.certainty==target.certainty);
		
		return lFlag && rFlag && cFlag;
	}
	
	public int getFullSize(){
		return this.left.getSize()+this.right.getSize();
	}
}
