package viewer.manuzioParser;

/**
 * <p>This class represents the Type's Method of Manuzio language</p>
 * @author Marco Fratton, matricola 823206, Facolt� di informatica Ca' Foscari in Venice
 *
 */
public class Method {
	private String methodName;
	private String syntax;
	private boolean isPlural;
	
	/**
	 *<p>Makes a Manuzio Method with given name and syntax</p>
	 *<p>Has the same effect as the use of the constructor Method (String name, String valueType, false).<p>
	 * @param name - a String Object
	 * @param syntax - a String representing the Method's syntax
	 */
	Method (String name, String syntax) {this.methodName = name; this.syntax = syntax; this.isPlural = false;}
	
	/**
	 *<p>Makes a Manuzio Method with given name and syntax, specifying its relation with the type</p>
	 * @param name - a String Object
	 * @param syntax - a String representing the Method's syntax
	 */
	Method (String name, String syntax, boolean isPlural) {this.methodName = name; this.syntax = syntax; this.isPlural = isPlural;}
	
	
	/**
	 * <p>Gets the Method's name</p>
	 * @return a String Object
	 */
	public String getMethodName () {return this.methodName;} 
	
		 
	 /**
	 * <p>Gets the Method's syntax</p>
	 * @return a String Object
	 */
	public String getMethodSyntax () {return this.syntax;} 
	
	/**
	 * <p>Specify if this Method is only related to the collection of its related Type or not.<p>
	 * @return a boolean value
	 */
	public boolean isPlural(){return this.isPlural;}
}
