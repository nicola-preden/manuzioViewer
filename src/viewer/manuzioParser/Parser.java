package viewer.manuzioParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import viewer.manuzioParser.Type.number;

/**
 * <p>This class analyzes the Manuzio code input.</p>
 * @author Marco Fratton, matricola 823206, Facolt� di informatica Ca' Foscari in Venice
 */
public class Parser {
	private static final String end = "";
	private static StreamTokenizer tok = null;
	private static Reader r = null;
	private static int tempChar = ' '; //used to check if there is a white space between ':' and '=' keywords
	
	private Parser(){} //you shouldn't instantiate this class 
	
	/**
	 * <p>Basic constructor to analyze the code as a one big String.</p>  
	 * @param code - a String Object which contains the code to analyze.
	 * @return the Schema built analyzing the code
	 * @throws IOException if an I/O error occurs
	 * @throws ParseException if there are lexical, syntactical or semantical errors in the source code
	 */
	public static Schema parsing (String code) throws IOException, ParseException
	{
		r = new StringReader (code);
		tokenizer ();
		Schema s = analyze();
		r.close(); 
		return s;
	}

	/**
	 * <p>Advanced constructor which loads the source code from a file.</p>
	 * @param fileName - a File Object containing the source code.
	 * @return the Schema built analyzing the code in the file
	 * @throws IOException if the files does not exists or it cannot be read for any reason.
	 * @throws ParseException if there are lexical, syntactical or semantical errors in the source code
	 */
	public static Schema parsing (File fileName) throws IOException, ParseException
	{
		r = new FileReader (fileName);
		tokenizer ();
		Schema s = analyze();
		r.close(); 
		return s;
	}
	
	
	/**
	 * <p>Private method to analyze the source code and to build a Manuzio Schema.</p>
	 * @return a Manuzio Schema Object made from this code source
	 * @throws IOException if a I\O error occurs
	 * @throws ParseException if there are lexical or syntactical errors in the source code
	 */
		private static Schema analyze () throws IOException, ParseException
	{
		Schema schema = null;
		Map <Type, String> supertype = new HashMap  <Type, String> (); //supertypes to add
		Map <String,  Set <ComponentProperty>> missingComponents = new HashMap  <String,  Set <ComponentProperty>> (); //Type of the component missing -> CoponentProperty Object
		Map <String, String> errors = new HashMap <String, String>();
		String s = nextToken();		//String analyzed
		Type currentType = null;
		ComponentProperty comp = null;
		String temp;	//used to memorize temporally part of the code
		
		if (!s.equals("Schema"))	
			throw new ParseException("Error at line " + tok.lineno() + " ('" + s + "') -> 'Schema' expected",  tok.lineno());
		s = nextToken();
		checkKeyWord(s);
		schema = new Schema (s);
		s = nextToken();
		
		while (!s.equals("End") && !s.equals(end)) //looks over the tokens list
		{
			if (s.equals("type")) //builds each type
			{
				boolean minCandidate = true;
				s = nextToken();
				checkKeyWord(s);
				if (schema.containsType(s) || schema.containsPluralType(s))  //checks if there is another type with the same name
					throw new ParseException("Error at line " + tok.lineno() + " -> '" + s + "' duplicate codename", tok.lineno());
				temp = s;
				s = nextToken();
				checkKeyWord(s);
				if (schema.containsPluralType(s) || schema.containsType(s) || temp.equals(s))  //checks if there is another type with the same name
					throw new ParseException("Error at line " + tok.lineno() + " -> '" + s + "' duplicate codename", tok.lineno());
				currentType = schema.addType(temp, s);					
				s = nextToken();
				if (s.equals("is"))
				{
					s = nextToken();
					checkKeyWord(s);
					supertype.put(currentType, s);
					if (errors.containsKey(s))
						errors.put(s, (errors.get(s) + " - " +tok.lineno()));
					else
						errors.put(s, "" + tok.lineno());
					s = nextToken();
					minCandidate = false;
				}
				
				if (s.equals("has"))	//starts checking components
				{
					s = nextToken();
					boolean hasComponent = false; 	//used to check if at least a component is build
					
					while (!s.equals("attributes") && !s.equals("methods") && !s.equals("end") && !s.equals(end))	//search each component 
					{
						boolean isOptional = false;		//used to memorize if optional
						if (s.equals("optional"))
						{
							isOptional = true;
							s = nextToken();
						}
						checkKeyWord(s);
						if (currentType.containsComp(s)) 	//checks if there is another component with the same name
							throw new ParseException("Error at line " + tok.lineno() + " -> '" + s + "' duplicate codename", tok.lineno());
						comp = currentType.addComponent(s, isOptional);
						s = nextToken();
						if (!s.equals(":"))
							throw new ParseException ("Error at line " + tok.lineno() + " ('" + s + "') -> ':' expected", tok.lineno());
						s = nextToken();

						if (missingComponents.containsKey(s))
							missingComponents.get(s).add(comp);
						else
						{
							Set <ComponentProperty> tempSet = new HashSet <ComponentProperty>();
							tempSet.add(comp);
							missingComponents.put(s, tempSet); 		
						}
						
						if (errors.containsKey(s))
							errors.put(s, (errors.get(s) + " - " +tok.lineno()));
						else
							errors.put(s, "" + tok.lineno());
						s = nextToken();
						
						if (!hasComponent)
							hasComponent = true;
					}
				if (!hasComponent)
					throw new ParseException("Syntax error at line " + tok.lineno()+ "-> declared type '" + currentType.getTypeName() + "' with no components", tok.lineno());
				}
				else	 //memorizes the Unit type
					{
						if (minCandidate)
						{
							if (schema.getMinimalUnit() == null)
								schema.setMinUnit(currentType);
							else
								throw new ParseException("Semantical error at line " + tok.lineno()+ "-> could only exist one Minimal Unit at time", tok.lineno());
						}
					}
			
				if (s.equals("attributes"))	//starts checking attributes
				{
					s = nextToken();
					boolean hasAttribute = false; 	//used to check if at least an attribute is given 
					while (!s.equals("methods") && !s.equals("end") && !s.equals(end))	//search each attribute
					{
						boolean isPlural = false;	//used to check if the Attribute refers to this Plural Type 
						if (s.equals("plural"))
						{
							isPlural = true;
							s = nextToken();
						}
						checkKeyWord(s);
						if (currentType.containsAt(s, number.BOTH)) //checks if there is another attribute with the same name
							throw new ParseException("Error at line " + tok.lineno() + " -> '" + s + "' duplicate codename", tok.lineno());
						temp = s;
						s = nextToken();
						if (!s.equals(":"))
							throw new ParseException ("Error at line " + tok.lineno() + " ('" + s + "') -> ':' expected", tok.lineno());
						s = nextToken();
						checkKeyWord(s);
						currentType.addAttribute(temp, s, isPlural);
						s = nextToken();
						if (!hasAttribute)
							hasAttribute = true;
					}
					if (!hasAttribute)
						throw new ParseException("Syntax error at line " + tok.lineno()+ "-> declared type '" + currentType.getTypeName() + "' with attributes but they are missing", tok.lineno());
				}
				
				if (s.equals("methods"))	//starts checking methods
				{
					s = nextToken();
					boolean hasMethod = false; 	//used to check if at least a method is given 
					while (!s.equals("end") && !s.equals(end))
					{
						String method = "";
						boolean isPlural = false;	//used to check if the Method refers to this Plural Type 
						if (s.equals("plural"))
						{
							isPlural = true;
							s = nextToken();
						}
						checkKeyWord(s);
						if (currentType.containsMethod(s, number.BOTH)) //checks if there is another method with the same name
							throw new ParseException("Error at line " + tok.lineno() + " -> '" + s + "' duplicate codename", tok.lineno());
						temp = s;
						s = nextToken();
						if (!s.equals(":="))
							throw new ParseException ("Error at line " + tok.lineno() + " ('" + s + "') -> ':=' expected", tok.lineno());
						s = nextToken();
						if (!s.equals("["))
							throw new ParseException ("Error at line " + tok.lineno() + " ('" + s + "') -> '[' expected", tok.lineno());
						s = nextToken();						
						while (!s.equals("]"))
						{
							if (s.equals(end))
								throw new ParseException ("Error at line " + tok.lineno() + " ('" + s + "') -> ']' expected", tok.lineno());
							method = method + s;
							s = nextToken();							
						}
						if (method == "")
							throw new ParseException ("Syntax error at line " + tok.lineno()+ "-> missing method's syntax '" + temp +"'", tok.lineno());
						currentType.addMethod(temp, method, isPlural);
						s = nextToken();
						if (!hasMethod)
							hasMethod = true;
					}
					if (!hasMethod)
						throw new ParseException("Syntax error at line " + tok.lineno()+ "-> declared type '" + currentType.getTypeName() + "' with methods but they are missing", tok.lineno());
				}
			
				if (!s.equals("end")){	//end of the type
					checkKeyWord(s);
					throw new ParseException("Error at line " + tok.lineno() + " ('" + s + "') -> 'has', 'attributes', 'methods' or 'end' expected", tok.lineno());
				}
			}
			else
				throw new ParseException("Error at line " + tok.lineno() + " ('" + s + "') -> 'type' expected", tok.lineno());
			s = nextToken();
		}
		if (!s.equals("End")) //close the Schema
			throw new ParseException("Error at line " + tok.lineno() + " ('" + s + "') -> 'End' expected", tok.lineno());
		s = nextToken();
		if (!s.equals(end)) //close the Schema
			throw new ParseException("Error at line " + tok.lineno() + " -> exceeding commands after 'End'", tok.lineno());

		if (schema.isEmpty()) //checks if the schema is empty
			throw new ParseException("Semantical error -> empty Schema declared",-1);
		
		for (Type t : supertype.keySet())	//adds supertypes
		{
			String type = supertype.get(t);
			if (!schema.containsType(type))
				if (!schema.containsPluralType(type))
					throw new ParseException("Error at lines " + errors.get(type) + " -> missing Type '" + type + "'",  Integer.parseInt((errors.get(type).split(" "))[0])); //shows only the first occurrence
				else
					throw new ParseException("Semantical error at lines " + errors.get(type) + " ('" + type + "') -> you can't extend plural Types",  Integer.parseInt((errors.get(type).split(" "))[0]));
			Type sup = schema.getType(type);
			if (sup.equals(schema.getMinimalUnit()) && t.hasComponents())
				throw new ParseException("Semantical error at lines " + errors.get(type) + " -> the Minimal Unit Type's extension  '" + t.getTypeName() + "' cannot have any component",  Integer.parseInt((errors.get(type).split(" "))[0]));
			t.addSupertype(sup);
		}
		
		for (String component : missingComponents.keySet())		//adds components
		{
			if (schema.containsType(component))
				for (ComponentProperty prop : missingComponents.get(component))
				{
					prop.setComponent(schema.getType(component));
					schema.getType(component).addContainer(prop);
				}
			else
				if (schema.containsPluralType(component))
					for (ComponentProperty prop : missingComponents.get(component))
					{
						prop.setComponent(schema.getPluralType(component));
						prop.setPlural(true);
						schema.getPluralType(component).addContainer(prop);
					}
				else
					throw new ParseException("Error at lines " + errors.get(component) + " -> missing Type '" + component + "'", Integer.parseInt((errors.get(component).split(" "))[0])); //shows only the first occurrence 
		}

		
		checkCycles(schema);		//try to find cycles
		setMaxUnit(schema);				//finds the Maximal Unit
		
		return schema;
	}
		
		
	/**
	 * <p>Private method to tokenize the source code. It's the first step of the parsing procedure.</p>
	 */
	private static void tokenizer () 	//Here are specified the tokenizer's rules
	{
		tok = new StreamTokenizer (r);
		tok.resetSyntax();
		tok.whitespaceChars(0, 32); //code ASCII
		tok.wordChars(33, 255);
		tok.ordinaryChar(':');
		tok.ordinaryChar('[');
		tok.ordinaryChar(']');
		tok.ordinaryChar('=');
	}
	
	/**
	 * <p>This method returns the next token defined by the rules of tokenizer().</p> 
	 * @return a String Object
	 * @throws IOException if an I/O error occurs.
	 * @throws ParseException if it is bad used a key character 
	 */
	private static String nextToken() throws IOException, ParseException{
			int i = tok.nextToken();
			if (i != StreamTokenizer.TT_WORD)
			{
				if (i == ':')
				{
					tempChar = r.read();
					if (tempChar == '=')
					{
						tempChar = ' ';
						return (":=");
					}
					else 
					{
						if (tempChar == '[' || tempChar == ']' || tempChar == ':' )
							throw new ParseException("Error at line " + tok.lineno() + " -> '" + (char)(tempChar) + "' reserved character: you cannot use it in your codenames", tok.lineno());
						return (":");
					}
				}
				else
					if (i == StreamTokenizer.TT_EOF)
						return end;
					else
						return ("" + (char)(i));
			}
			else
				if (tempChar >= 33)
				{
					char temp = (char)tempChar;
					tempChar = ' ';					
					return (temp + tok.sval);
				}
				else
					return (tok.sval);	
	}
	
	/**
	 * <p>Checks if the given String is a key character or a keyword.</p>
	 * @param s - the String checked
	 * @throws ParseException - if the given String is a key character or a keyword
	 */
	private static void checkKeyWord (String s) throws ParseException
	{
		if (s.equals(end))
			throw new ParseException("Error at line " + tok.lineno() + " -> expected codename", tok.lineno());
		if (s.equals("[") || s.equals("]") || s.equals(":") || s.equals("=") || s.equals(":="))
			throw new ParseException("Error at line " + tok.lineno() + " -> '" + s + "' reserved character: you cannot use it in your codenames", tok.lineno());
		if (s.equals("is") || s.equals("has") || s.equals("attributes") || s.equals("methods") || s.equals("Schema") || s.equals("type")|| s.equals("optional") 
				|| s.equals("end") || s.equals("End") || s.equals("plural"))
			throw new ParseException("Error at line " + tok.lineno() + " -> '" + s + "' reserved keyword", tok.lineno());
	}
	
	/**
	 * <p>Checks if there is exactly one Maximal Unit in the Schema, and sets the Schema's Max Unit with the Unit found in this way</p>
	 * @param schema - A Manuzio Schema Object
	 * @throws ParseException if cannot be found any max Unit, or if there are found more than one
	 */
	public static void setMaxUnit (Schema schema) throws ParseException
	{
		Type max = null;
		for (Type type : schema.getTypeSet())
			if (type.isMaximalUnit())
				if (max == null)
					max = type;
				else
					throw new ParseException("Semantical error -> '" + max.getTypeName() + "' - '" + type.getTypeName() + "' : could only exist one Maximal Unit at time", -1);
		if (max == null)
			throw new ParseException("Semantical error -> missing Maximal Unit", -1); // -1 because you can't know where is the wrong line 
		else
			schema.setMaxUnit(max);
	}
	
	/**
	 * <p>Checks if there is exactly one Minimal Unit in the Schema, and sets the Schema's Min Unit with the Unit found in this way</p>
	 * @param schema - A Manuzio Schema Object
	 * @throws ParseException if cannot be found any min Unit, or if there are found more than one
	 */
	public static void setMinUnit (Schema schema) throws ParseException
	{
		Type min = null;
		for (Type type : schema.getTypeSet())
			if (type.isMinimalUnit())
				if (min == null)
					min = type;
				else
					throw new ParseException("Semantical error -> '" + min.getTypeName() + "' - '" + type.getTypeName() + "' : could only exist one Minimal Unit at time", -1);
		if (min == null)
			throw new ParseException("Semantical error -> missing Minimal Unit", -1); // -1 because you can't know where is the wrong line 
		else 
			schema.setMinUnit(min);
	}
	
	
	/**
	 * <p>Checks if there are cycles in the given Schema. This also calls setMinUnit (schema).</p>
	 * <p>Best used after SetMaxUnit
	 * @param schema - A Manuzio Schema Object
	 * @throws ParseException - if a cycle is found
	 */
	public static void checkCycles(Schema schema) throws ParseException
	{
		 setMinUnit(schema);
		 Set <Type> safe = new HashSet <Type> ();	//in here there are no cycles
		 LinkedList <Type> missing = new LinkedList <Type> ();
		 missing.add(schema.getMinimalUnit());
	do{
		 while (!missing.isEmpty()) 	//checks each type starting from MinUnit and going up with its containers
		 {
			 Type analyzed = missing.removeFirst();
			 if (safe.contains(analyzed))
				 continue;
			 
			 Set<Type> done = new HashSet <Type>(safe);
			 LinkedList <Type> trying = new LinkedList <Type>();
			 trying.addAll(Arrays.asList(analyzed.getOwnComponentTypes()));
			if (analyzed.hasSubTypes())
				trying.addAll(Arrays.asList(analyzed.getSubTypes()));
				
			while (!trying.isEmpty())	//tries to reach itself searching among its components and subtypes
			{
				Type current = trying.removeFirst();
				if (done.contains(current))
					continue;			
				if (current.getTypeName().equals(analyzed.getTypeName()))
					throw new ParseException("Semantical error -> cycle found in Type '" + analyzed.getTypeName() + "'", -1); // -1 because you can't know where is the wrong line 
				trying.addAll(Arrays.asList(current.getOwnComponentTypes()));
				if (current.hasSubTypes())
					trying.addAll(Arrays.asList(current.getSubTypes()));
				done.add(current);				
			}
				 
			safe.add(analyzed);	 
			missing.addAll(Arrays.asList(analyzed.getContainerTypes()));
			if (analyzed.hasSubTypes())
				missing.addAll(Arrays.asList(analyzed.getSubTypes()));		
		}
		 
		 if (schema.sizeTypes() != safe.size()){	//has not searched from each type
				for (Type r : schema.getTypeSet())
					if (!safe.contains(r))
						missing.add(r);	
		 }
		 else 
			 break;
	}while	(true);
		 
	}
	
}