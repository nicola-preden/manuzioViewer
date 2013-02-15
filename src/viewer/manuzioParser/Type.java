package viewer.manuzioParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>This class defines a type of Manuzio language</p>
 * @author Marco Fratton, matricola 823206, Facolt� di informatica Ca' Foscari in Venice
 *
 */
public class Type {
	public static enum number {SINGULAR, PLURAL, BOTH};	//enum type to select the number for the Type
	private String typeName;
	private String pluralName;
	private Type supertype = null;
	private Set <Type> subTypes = new HashSet <Type>();	
	private Set<ComponentProperty> containers = new HashSet <ComponentProperty>();
	private Map <String, Attribute> attributes = new HashMap <String, Attribute>();
	private Map <String, Method> methods = new HashMap <String, Method>();
	private Map <String, Attribute> pluralAttributes = new HashMap <String, Attribute>();
	private Map <String, Method> pluralMethods = new HashMap <String, Method>();
	private Map <String, ComponentProperty> components = new HashMap <String, ComponentProperty>();
	
	/**
	 * <p>Default constructor which creates a new Manuzio Type with no components, attributes or methods.</p>
	 * @param typeName - the name of this Type
	 * @param pluralName - the name of a collection of these types
	 */
	Type (String typeName, String pluralName) {this.typeName = typeName; this.pluralName = pluralName;}
	
	/**
	 * <p>Makes this Type a subtype of the selected Type 's'.</p><p>If this subtype has any methods, components or attributes, if they don't have the same identifier as the supertype'ones, they're added.
	 * Otherwise they're substitute with the newest ones.</p> 
	 * @param s - a Type Object
	 */
	void addSupertype(Type s){
		s.subTypes.add(this);
		this.supertype = s;
	}
	
	/**
	 * <p>Package method to link this Type component to its Type container using a ComponentProperty Object</p>
	 * @param comp - the ComponentProperty Object used to link
	 */
	void addContainer (ComponentProperty comp){this.containers.add(comp);}
	
	/**
	 * <p>Package method to add a Manuzio Attribute to this Type</p>
	 * <p>If isPlural is true, then this Attribute can be used only by the collection of these Type</p>
	 * @param atName - the Attribute's name 
	 * @param atType - the Attribute's type of value
	 * @param isPlural - indicates whether or not this Attribute can only be used by this plural Type
	 */
	void addAttribute (String atName, String atType, boolean isPlural)
	{
		Attribute at = new Attribute (atName, atType, isPlural);
		if (isPlural)
			this.pluralAttributes.put(atName, at);
		else
			attributes.put(atName, at);
	}
	
	/**
	 * <p>Package method to add a Manuzio Attribute to this Type</p>
	 * <p>Has the same effect as call addAttribute (String atName, String atType, false)</p>
	 * @param atName - the Attribute's name 
	 * @param atType - the Attribute's type of value
	 */
	void addAttribute (String atName, String atType)
	{
		Attribute at = new Attribute (atName, atType);
		attributes.put(atName, at);
	}
	
	/**
	 * <p>Package method to add a Manuzio Method to this Type</p>
	 * <p>If isPlural is true, then this Method can be used only by the collection of these Type</p>
	 * @param methodName - the Method's name 
	 * @param syntax - the Method's syntax
	 * @param isPlural - indicates whether or not this Method can only be used by this plural Type
	 */
	void addMethod (String methodName, String syntax, boolean isPlural)
	{
		Method met = new Method (methodName, syntax, isPlural);
		methods.put(methodName, met);
		if (isPlural)
			this.pluralMethods.put(methodName, met);
		else
			pluralMethods.put(methodName, met);
	}
	
	/**
	 * <p>Package method to add a Manuzio Method to this Type</p>
	 * <p>Has the same effect as call addMethod (String methodName, String syntax, false)</p>
	 * @param methodName - the Method's name 
	 * @param syntax - the Method's type of value
	 */
	void addMethod (String methodName, String syntax)
	{
		Method met = new Method (methodName, syntax);
		methods.put(methodName, met);
	}
	
	/**
	 * <p>Package method to add a Manuzio Component to this Type from the Parser.</p>
	 * @param compName - the name of this Component
	 * @param isOptional - indicates if this component might be unused or not
	 * @return the ComponentProperty Object just created
	 * @see Type
	 */
	ComponentProperty addComponent (String compName,  boolean isOptional)
	{
		ComponentProperty comp = new ComponentProperty (compName, this, isOptional);
		this.components.put(compName, comp);
		return comp;
	}
	
	/**
	 * <p>Package method to add a Manuzio Component to this Type.</p>
	 * <p>If 'isPlural' == true implicates this type may have many 'compType' components. If false this type might only have one 'compType' component.</p>
	 * <p>This has the same effect as calling addComponent(compName, compType, isPlural, false);</p>
	 * @param compName - the name of this Component
	 * @param compType - a Type Object representing the Component's type 
	 * @param isPlural - indicates whether or not there could be one or many of this component
	 * @see Type
	 */
	void addComponent (String compName, Type compType, boolean isPlural)
	{
		addComponent(compName, compType, isPlural, false);			
	}
	
	
	/**
	 * <p>Package method to add a Manuzio Component to this Type.</p>
	 * <p>If 'isPlural' == true implicates this type may have many 'compType' components. If false this type might only have one 'compType' component.</p>
	 * <p>If 'isOptional' == false implicates this component might be used once (or many time if 'isPlural' == true). If true this component might not be used.</p>
	 * @param compName - the name of this Component
	 * @param compType - a Type Object representing the Component's type 
	 * @param isPlural - indicates whether or not there could be one or many of this component
	 * @param isOptional - indicates if this component might be unused or not
	 * @see Type
	 */
	void addComponent (String compName, Type compType, boolean isPlural, boolean isOptional)
	{
		ComponentProperty comp = new ComponentProperty (compName, compType, this, isPlural, isOptional);
		this.components.put(compName, comp);
		compType.addContainer(comp);
	}
	
	/**
	 * <p>Gets the Type's name.</p>
	 * @return the typeName
	 */
	public String getTypeName() {return typeName;}

	/**
	 * <p>Gets the Type's plural name. The plural name refers to a List of this Type.</p>
	 * @return the pluralName
	 */
	public String getPluralName() {return pluralName;}

	/**
	 * <p>Indicates whether or not this is a component of another Type.</p>
	 * @return a Boolean value
	 */
	public boolean isComponent() {
		if (this.containers.isEmpty())
			return false;
		else
			return true;
	}
	
	/**
	 * <p>Indicates whether or not this Type has other Type components.</p>
	 * @return a Boolean value
	 */
	public boolean hasComponents() {
		if (this.components.isEmpty())
			return false;
		else
			return true;
	}
	
	/**
	 * <p>Indicates whether or not this Type has any Attribute.</p>
	 * <p>num could only be:</p><ul>
	 * <li>number.SINGULAR -> allows only the Attributes of this Type</li>
	 * <li>number.PLURAL -> allows only the Attributes related to the collection of this Type</li>
	 * <li>number.BOTH -> allows all Attributes</li></ul>
	 * @param num - enum type variable to specify the kind of Attribute wanted
	 * @return a Boolean value
	 * @throws IllegalArgumentException if num is not one of the values described before
	 */
	public boolean hasAt(number num) {
		switch (num)
		{
			case SINGULAR:
				if (this.attributes.isEmpty())
					return false;
				else
					return true;
			case PLURAL:
				if (this.pluralAttributes.isEmpty())
					return false;
				else
					return true;
			case BOTH:
				if (!this.attributes.isEmpty())
					return true;
				else{
					if (this.pluralAttributes.isEmpty())
						return false;
					else
						return true;
				}
			default:
				throw new IllegalArgumentException("bad data input. Allowed only number.SINGULAR, number.PLURAL, number.BOTH.");
		}
	}

	/**
	 * <p>Indicates whether or not this Type has any Method.</p>
	 * <p>num could only be:</p><ul>
	 * <li>number.SINGULAR -> allows only the Methods of this Type</li>
	 * <li>number.PLURAL -> allows only the Methods related to the collection of this Type</li>
	 * <li>number.BOTH -> allows all Methods</li></ul>
	 * @param num - enum type variable to specify the kind of Method wanted
	 * @return a Boolean value
	 * @throws IllegalArgumentException if num is not one of the values described before
	 */
	public boolean hasMethods(number num) {
		switch (num)
		{
			case SINGULAR:
				if (this.methods.isEmpty())
					return false;
				else
					return true;
			case PLURAL:
				if (this.pluralMethods.isEmpty())
					return false;
				else
					return true;
			case BOTH:
				if (!this.methods.isEmpty())
					return true;
				else{
					if (this.pluralMethods.isEmpty())
						return false;
					else
						return true;
				}
			default:
				throw new IllegalArgumentException("bad data input. Allowed only number.SINGULAR, number.PLURAL, number.BOTH.");
		}
	}
	
	/**
	 * <p>Indicates whether or not this Type has a supertype.</p>
	 * @return a Boolean value
	 */
	public boolean hasSuperType() {
		if (this.supertype == null)
			return false;
		else
			return true;
	}
	
	/**
	 * <p>Indicates whether or not this Type has any subtypes.</p>
	 * @return a Boolean value
	 */
	public boolean hasSubTypes() {
		if (this.subTypes.isEmpty())
			return false;
		else
			return true;
	}	
	
	/**
	 * <p>Indicates whether or not this is the Maximal Unit, that is the Type which holds all other types.</p>
	 * @return a Boolean value
	 */
	public boolean isMaximalUnit() {
		if (this.containers.isEmpty() && !this.hasSuperType())
			return true;
		else
			return false;
	}
	
	/**
	 * <p>Indicates whether or not this is  the Minimal Unit, that is the deepest Type and the smallest part of text which can be manipulated.</p>
	 * @return a Boolean value
	 */
	public boolean isMinimalUnit() {
		if (!this.hasComponents() && this.supertype == null)
			return true;
		else
			return false;
	}
	
	/**
	 * <p>Gets the Own attributes of this Type.</p>
	 * <p>Note that the attributes inherited from its supertype aren't returned.</p> 
	 * <p>num could only be:</p><ul>
	 * <li>number.SINGULAR -> allows only the Attributes of this Type</li>
	 * <li>number.PLURAL -> allows only the Attributes related to the collection of this Type</li>
	 * <li>number.BOTH -> allows all Attributes</li></ul>
	 * @param num - enum type variable to specify the kind of Attribute wanted
	 * @return an Array of Attribute
	 * @throws IllegalArgumentException if num is not one of the values described before
	 */
	public Attribute [] getOwnAt (number num)
	{
		switch (num)
		{
			case SINGULAR:
				return this.attributes.values().toArray(new Attribute [0]);
			case PLURAL:
				return this.pluralAttributes.values().toArray(new Attribute [0]);
			case BOTH:
				Set <Attribute> at = new HashSet <Attribute> ();
				at.addAll(this.attributes.values());
				at.addAll(this.pluralAttributes.values());
				return at.toArray(new Attribute [0]);
			default:
				throw new IllegalArgumentException("bad data input. Allowed only number.SINGULAR, number.PLURAL, number.BOTH.");
		}

	}
	

	/**
	 * <p>Gets the Own methods of this Type.</p>
	 * <p>Note that the methods inherited from its supertype aren't returned.</p> 
	 * <p>num could only be:</p><ul>
	 * <li>number.SINGULAR -> allows only the Methods of this Type</li>
	 * <li>number.PLURAL -> allows only the Methods related to the collection of this Type</li>
	 * <li>number.BOTH -> allows all Methods</li></ul>
	 * @param num - enum type variable to specify the kind of Method wanted
	 * @return an Array of Method
	 * @throws IllegalArgumentException if num is not one of the values described before
	 */
	public Method [] getOwnMethods (number num)
	{
		switch (num)
		{
			case SINGULAR:
				return this.methods.values().toArray(new Method [0]);
			case PLURAL:
				return this.pluralMethods.values().toArray(new Method [0]);
			case BOTH:
				Set <Method> met = new HashSet <Method>();
				met.addAll(this.methods.values());
				met.addAll(this.pluralMethods.values());
				return met.toArray(new Method [0]);
			default:
				throw new IllegalArgumentException("bad data input. Allowed only number.SINGULAR, number.PLURAL, number.BOTH.");
		}
	}
	
	
	/**
	 * <p>Gets the own components of this Type. Note that the components inherited from its supertype aren't returned. </p>
	 * @return an Array of ComponentProperty
	 */
	public ComponentProperty[] getOwnComponents (){return this.components.values().toArray(new ComponentProperty [0]);}

	/**
	 * <p>Gets the own component's types of this Type.</p>
	 * <p>Note that only the Type of each Component is returned, but not the property of the relation itself.</p>
	 * <p>Note that the components inherited from its supertype aren't returned.</p> 
	 * @return an Array of Type
	 * @see manuzioParser.Type#getComponents()
	 */
	public Type [] getOwnComponentTypes()
	{
		ComponentProperty[] comp = getOwnComponents();
		Type [] types = new Type [comp.length];
		for (int i = 0; i < comp.length; i++)
			types[i] = comp[i].getComponent();
		return types;		
	}

	/**
	 * <p>Gets the attributes of this Type.</p>
	 * <p>num could only be:</p><ul>
	 * <li>number.SINGULAR -> allows only the Attributes of this Type</li>
	 * <li>number.PLURAL -> allows only the Attributes related to the collection of this Type</li>
	 * <li>number.BOTH -> allows all Attributes</li></ul>
	 * @param num - enum type variable to specify the kind of Attribute wanted
	 * @return an Array of Attribute
	 * @throws IllegalArgumentException if num is not one of the values described before
	 */
	public Attribute [] getAt (number num)
	{
		if (supertype != null)	//adds all attributes of the supertype; if an attribute is duplicated (has equals names), the attribute of this Type override the other one 
		{	
			Set <Attribute> atSet;
			atSet = new HashSet <Attribute>();
			Attribute[] superAt = this.supertype.getAt(num);
			switch (num)
			{
				case SINGULAR:
					for (Attribute at : superAt)
						if (!this.attributes.containsKey(at.getAtName()))
							atSet.add(at);				
					atSet.addAll(this.attributes.values());
					return atSet.toArray(new Attribute[0]);
				case PLURAL:
					for (Attribute at : superAt)
						if (!this.pluralAttributes.containsKey(at.getAtName()))
							atSet.add(at);				
					atSet.addAll(this.pluralAttributes.values());
					return atSet.toArray(new Attribute[0]);
				case BOTH:
					for (Attribute at : superAt)
						if (!this.attributes.containsKey(at.getAtName()) && !this.pluralAttributes.containsKey(at.getAtName()))
							atSet.add(at);
					atSet.addAll(this.attributes.values());
					atSet.addAll(this.pluralAttributes.values());
					return atSet.toArray(new Attribute[0]);
				default:
					throw new IllegalArgumentException("bad data input. Allowed only number.SINGULAR, number.PLURAL, number.BOTH.");
			}
		}
		else
			return this.getOwnAt(num);
	}

	/**
	 * <p>Gets the methods of this Type.</p>
	 * <p>num could only be:</p><ul>
	 * <li>number.SINGULAR -> allows only the Methods of this Type</li>
	 * <li>number.PLURAL -> allows only the Methods related to the collection of this Type</li>
	 * <li>number.BOTH -> allows all Methods</li></ul>
	 * @param num - enum type variable to specify the kind of Method wanted
	 * @return an Array of Method
	 * @throws IllegalArgumentException if num is not one of the values described before
	 */
	public Method [] getMethods (number num)
	{
		if (supertype != null)	//adds all Methods of the supertype; if an Method is duplicated (has equals names), the Method of this Type override the other one 
		{	
			Set <Method> methodSet;
			methodSet = new HashSet <Method>();
			Method[] superMethod = this.supertype.getMethods(num);
			switch (num)
			{
				case SINGULAR:
					for (Method at : superMethod)
						if (!this.methods.containsKey(at.getMethodName()))
							methodSet.add(at);				
					methodSet.addAll(this.methods.values());
					return methodSet.toArray(new Method[0]);
				case PLURAL:
					for (Method at : superMethod)
						if (!this.pluralMethods.containsKey(at.getMethodName()))
							methodSet.add(at);				
					methodSet.addAll(this.pluralMethods.values());
					return methodSet.toArray(new Method[0]);
				case BOTH:
					for (Method at : superMethod)
						if (!this.methods.containsKey(at.getMethodName()) && !this.pluralMethods.containsKey(at.getMethodName()))
							methodSet.add(at);
					methodSet.addAll(this.methods.values());
					methodSet.addAll(this.pluralMethods.values());
					return methodSet.toArray(new Method[0]);
				default:
					throw new IllegalArgumentException("bad data input. Allowed only number.SINGULAR, number.PLURAL, number.BOTH.");
			}
		}
		else
			return this.getOwnMethods(num);
	}
	
	/**
	 * <p>Gets the components of this Type.</p>
	 * @return an Array of ComponentProperty
	 */
	public ComponentProperty [] getComponents()
	{
		
		if (supertype != null)		//adds all components of the supertype; if a component is duplicated (has equals names), the component of this Type override the other one 
		{
			Set <ComponentProperty> compSet = new HashSet <ComponentProperty>();
			ComponentProperty[] superComp = this.supertype.getComponents();
			
			for (ComponentProperty comp : superComp)
				if (!this.components.containsKey(comp.getComponentName()))
					compSet.add(comp);				
			compSet.addAll(this.components.values());
			return compSet.toArray(new ComponentProperty [0]);
		}
		else
			return this.components.values().toArray(new ComponentProperty [0]);		
	}
	
	/**
	 * <p>Gets the component's types of this Type.</p>
	 * <p>Note that only the Type of each Component is returned, but not the property of the relation itself.</p> 
	 * @return an Array of Type
	 * @see manuzioParser.Type#getComponents()
	 */
	public Type [] getComponentTypes()
	{
		ComponentProperty[] comp = getComponents();
		Type [] types = new Type [comp.length];
		for (int i = 0; i < comp.length; i++)
			types[i] = comp[i].getComponent();
		return types;		
	}
	
	/**
	 * <p>Gets the Property of the relation between this component and its container.</p>
	 * @return an Array of ComponentProperty
	 */
	public ComponentProperty [] getContainers() {return this.containers.toArray(new ComponentProperty[0]);}
	
	/**
	 * <p>Gets the Types of the Type which contain this component.</p>
	 * <p>Note that only the Type is returned, but not the property of the relation itself.</p> 
	 * @return an Array of Type
	 * @see manuzioParser.Type#getComponents()
	 */
	public Type [] getContainerTypes()
	{
		ComponentProperty[] comp = getContainers();
		Type [] types = new Type [comp.length];
		for (int i = 0; i < comp.length; i++)
			types[i] = comp[i].getContainer();
		return types;		
	}
	
	/**
	 * <p>Gets the Types which extend this type, or null if none extends this.</p>
	 * @return an Array of Type or null 
	 */
	public Type [] getSubTypes(){return this.subTypes.toArray(new Type[0]);}
	
	/**
	 * <p>Gets the Type extended by this type, or none if this type extends nothing.</p>
	 * @return a Type Object or null
	 */
	public Type getSuperType(){return this.supertype;}
	
	/**
	 * <p>Checks if there is a Component matching the given Component Name in this Type.</p>
	 * @param name - the name of the Component to search
	 * @return true if the Component is found, false otherwise
	 */
	public boolean containsComp (String name){if (this.components.containsKey(name)) return true; else return false;}
	
	/**
	 * <p>Checks if there is an Attribute matching the given Attribute Name in this Type.</p>
	 * <p>num could only be:</p><ul>
	 * <li>number.SINGULAR -> search among the Attributes of this Type</li>
	 * <li>number.PLURAL -> search among the Attributes related to the collection of this Type</li>
	 * <li>number.BOTH -> search among all Attributes</li></ul>
	 * @param name - the name of the Attribute to search
	 * @param num - enum type variable to specify the kind of Attribute wanted
	 * @return true if the Attribute is found, false otherwise
	 * @throws IllegalArgumentException if num is not one of the values described before
	 */
	public boolean containsAt(String name, number num){
		switch (num)
		{
			case BOTH:
				if (this.attributes.containsKey(name)) 
					return true; 
				else 
					if (this.pluralAttributes.containsKey(name))
						return true; 
					else
						return false;
			case PLURAL:
				if (this.pluralAttributes.containsKey(name)) return true; else return false;
			case SINGULAR:
				if (this.attributes.containsKey(name)) return true; else return false;
			default:
				throw new IllegalArgumentException("bad data input. Allowed only number.SINGULAR, number.PLURAL, number.BOTH.");
		}
		
		}
	
	/**
	 * <p>Checks if there is a Method matching the given Method Name in this Type.</p>
	 * <p>num could only be:</p><ul>
	 * <li>number.SINGULAR -> search among the Methods of this Type</li>
	 * <li>number.PLURAL -> search among the Methods related to the collection of this Type</li>
	 * <li>number.BOTH -> search among all Methods</li></ul>
	 * @param name - the name of the Method to search
	 * @param num - enum type variable to specify the kind of Method wanted
	 * @return true if the Method is found, false otherwise
	 * @throws IllegalArgumentException if num is not one of the values described before
	 */
	public boolean containsMethod(String name, number num){
		switch (num)
		{
			case BOTH:
				if (this.methods.containsKey(name)) 
					return true; 
				else 
					if (this.pluralMethods.containsKey(name))
						return true; 
					else
						return false;
			case PLURAL:
				if (this.pluralMethods.containsKey(name)) return true; else return false;
			case SINGULAR:
				if (this.methods.containsKey(name)) return true; else return false;
			default:
				throw new IllegalArgumentException("bad data input. Allowed only number.SINGULAR, number.PLURAL, number.BOTH.");
		}
	}
	
}
