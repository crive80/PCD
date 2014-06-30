import java.io.Serializable;

public class Resource implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4406994920356113048L;
	private String name; 
	private int parts; 
	private String owner; 
	
	public Resource(String n, int p){
		name = n; 
		parts = p; 
	}
	public String getName(){
		return name; 
	}
	public int getParts() {
		return parts; 
	}
	public String getOwner() {
		return owner; 
	}
	public boolean equals(Resource f) {
		boolean result;
		if(f == null)
			result = false;
		else
		    result = name.equals(f.getName()) &&  parts == f.parts;
		return result;
		}
}
