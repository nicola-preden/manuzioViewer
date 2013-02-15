package viewer.manuzioParser;

/**
 * <p>This class represents the Type's Attribute of Manuzio language</p>
 * @author Marco Fratton, matricola 823206, Facolt� di informatica Ca' Foscari in Venice
 *
 */
public class Attribute {
	private String atName;
	private String atType;
	private boolean isPlural;
	
	
	/**
	 *<p>Makes a Manuzio Attribute with given name and value type</p>
	 *<p>Has the same effect as the use of the constructor Attribute (String name, String valueType, false).<p>
	 * @param name - a String Object
	 * @param valueType - a String representing the attribute's type
	 */
	Attribute (String name, String valueType) {this.atName = name; this.atType = valueType; this.isPlural = false;}
	
	/**
	 *<p>Makes a Manuzio Attribute with given name and value type, specifying its relation with the type</p>
	 * @param name - a String Object
	 * @param valueType - a String representing the attribute's type
	 */
	Attribute (String name, String valueType, boolean isPlural) {this.atName = name; this.atType = valueType; this.isPlural = isPlural;}
	
	
	/**
	 * <p>Gets the Attribute's name</p>
	 * @return a String Object
	 */
	public String getAtName () {return this.atName;} 
	
		 
	 /**
	 * <p>Gets the Attribute's type</p>
	 * @return a String Object
	 */
	public String getAtType () {return this.atType;} 
	
	/**
	 * <p>Specify if this Attribute is only related to the collection of its related Type or not.<p>
	 * @return a boolean value
	 */
	public boolean isPlural(){return this.isPlural;}
}
