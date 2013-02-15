package viewer.manuzioParser;

/**
 * <p>This class defines the properties of the relation between the component Type and its container</p>
 * @author Marco Fratton, matricola 823206, Facolt� di informatica Ca' Foscari in Venice
 *
 */
public class ComponentProperty {
	private String componentName = null;  //if != null -> this type is a component of another type
	private Type container = null;		//this type is a component of that variable
	private Type component = null;		//these properties refers to this component
	private boolean isPlural = false;	//if true the container has multiple components of this type
	private boolean isOptional = false; //if true this component may exist or not
	
	
	/**
	 * <p>Package constructor to build the property for a Manuzio Component from the Parser.</p>
	 * <p>If 'isOptional' == false implicates this component must be used once (or many time if 'isPlural' == true). If true this component might not be used.</p>
	 * @param compName - the name of this Component
	 * @param container - A Type Object representing the owner of this component
	 * @param isOptional - indicates if this component might be unused or not
	 * @see Type
	 */
	ComponentProperty (String compName, Type container, boolean isOptional)
	{
		this.container = container;
		this.componentName = compName;
		this.isOptional = isOptional;
	}
	
	
	/**
	 * <p>Package constructor to build the property for a Manuzio Component.</p>
	 * <p>If 'isPlural' == true implicates the 'container' may have many 'compType' components. If false the 'container' might only have one 'compType' component.</p>
	 * <p>If 'isOptional' == false implicates this component must be used once (or many time if 'isPlural' == true). If true this component might not be used.</p>
	 * @param compName - the name of this Component
	 * @param compType - a Type Object representing the Component's type 
	 * @param container - A Type Object representing the owner of this component
	 * @param isPlural - indicates whether or not there could be one or many of this component
	 * @param isOptional - indicates if this component might be unused or not
	 * @see Type
	 */
	ComponentProperty (String compName, Type compType, Type container, boolean isPlural, boolean isOptional)
	{
		this.component = compType;
		this.container = container;
		this.componentName = compName;
		this.isOptional = isOptional;
		this.isPlural = isPlural;
	}
	
	/**
	 * <p>Indicates whether or not there could be one or many of this component.</p>
	 * @return a Boolean value
	 */
	public boolean isPlural() {return isPlural;}

	/**
	 * <p>Indicates if this component might be unused or not.</p> 
	 * @return a Boolean value
	 */
	public boolean isOptional() {return isOptional;}

	/**
	 * <p>Gets the name of this component.</p>
	 * @return the componentName
	 */
	public String getComponentName() {return componentName;}

	/**
	 * <p>Gets the Type Object of the container of this component.</p>
	 * @return this container's Type Object
	 */
	public Type getContainer() {return container;}
	
	/**
	 * <p>Gets the Type Object of the component.</p>
	 * @return this container's Type Object
	 */
	public Type getComponent() {return component;}
	
	/**
	 * <p>Sets if this component is plural or singolar.</p>
	 * <p>If 'isPlural' == true implicates the 'container' may have many 'compType' components. If false the 'container' might only have one 'compType' component.</p>
	 * @param plural a boolean value
	 */
	void setPlural(boolean plural){this.isPlural= plural; }
	
	/**
	 * <p>Sets if this component is optional or not.</p>
	 * <p>If 'isPlural' == true implicates the 'container' may have many 'compType' components. If false the 'container' might only have one 'compType' component.</p>
	 * @param optional a boolean value
	 */
	void setOptional(boolean optional){this.isOptional = optional; }
	
	/**
	 * <p>Attach these properties to the Type Object component referred</p>
	 * @param type the Type Object of this relation
	 */
	void setComponent (Type type){this.component = type;}
}
