package viewer.manuzioParser;

import database.ConnectionPoolFactory;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import viewer.ManuzioViewer;
import viewer.manuzioParser.Type.number;

/**
 * <p>This class is used to build a Schema of Manuzio language.</p>
 *
 * @author Marco Fratton, matricola 823206, Facolt� di informatica Ca' Foscari
 * in Venice
 *
 */
public class Schema {

    private String name;
    private Type MinimalUnit = null;
    private Type MaximalUnit = null;
    private Map<String, Type> typeSet = new HashMap<String, Type>();	//mapping singular-name types
    private Map<String, Type> pluralSet = new HashMap<String, Type>();	//mapping plural-name types 

    /**
     * Default constructor which builds an empty Schema with the given name
     *
     * @param name - the name of the Schema
     */
    Schema(String name) {
        this.name = name;
    }

    /**
     * <p>Builds a new Manuzio Type with the given
     * <code>typeName</code> and
     * <code>pluralName</code>, and adds the type just created to the current
     * Schema.</p>
     *
     * @param typeName - the name of the Type
     * @param pluralName - the name of a collection of those types
     * @return the Type just created
     * @see Type
     */
    Type addType(String typeName, String pluralName) {
        Type type = new Type(typeName, pluralName);
        typeSet.put(type.getTypeName(), type);
        pluralSet.put(type.getPluralName(), type);
        return type;
    }

    /**
     * <p>Gets the name of the Schema</p>
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Gets the Minimal Unit of the Schema, that is the deepest Type and the
     * smallest part of text which can be manipulated.</p>
     *
     * @return the minimalUnit
     */
    public Type getMinimalUnit() {
        return MinimalUnit;
    }

    /**
     * <p>Gets the Maximal Unit of the Schema, that is the Type which holds all
     * other types.</p>
     *
     * @return the maximalUnit
     */
    public Type getMaximalUnit() {
        return MaximalUnit;
    }

    /**
     * <p>Gets all the types used in this Schema.</p>
     *
     * @return an array of Type
     */
    public Type[] getTypeSet() {
        return typeSet.values().toArray(new Type[0]);
    }

    /**
     * <p>Checks if there is the given typeName in this Schema's Types.</p>
     *
     * @param name - the typeName to check
     * @return true if found, false elsewhere
     */
    public boolean containsType(String name) {
        if (this.typeSet.containsKey(name)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>Checks if there is the given plural typeName in this Schema's Types.
     * This differs from getTypeSet() because searches among the plural
     * typeName.</p>
     *
     * @param name - the plural typeName to check
     * @return true if found, false elsewhere
     */
    public boolean containsPluralType(String name) {
        if (this.pluralSet.containsKey(name)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>Gets the Type matching the given typeName.</p>
     *
     * @param name - the name of the Type
     * @return the Type searched, or null if cannot be found
     */
    public Type getType(String name) {
        return this.typeSet.get(name);
    }

    /**
     * <p>Gets the Type matching the given plural typeName. This differs from
     * getType() because searches among the plural typeName.</p>
     *
     * @param name - the plural name of the Type
     * @return the Type searched, or null if cannot be found
     */
    public Type getPluralType(String name) {
        return this.pluralSet.get(name);
    }

    /**
     * <p>Sets the Minimal Unit of this Schema</p>
     *
     * @param min - the Minimal Unit Type to set
     */
    void setMinUnit(Type min) {
        this.MinimalUnit = min;
    }

    /**
     * <p>Sets the Maximal Unit of this Schema</p>
     *
     * @param max - the Maximal Unit Type to set
     */
    void setMaxUnit(Type max) {
        this.MaximalUnit = max;
    }

    /**
     * Gets the number of Types in this Schema
     *
     * @return - the number of Types
     */
    public int sizeTypes() {
        return this.typeSet.size();
    }

    /**
     * <p>Checks if this schema is empty or not</p>
     *
     * @return - a boolean value
     */
    public boolean isEmpty() {
        return (this.typeSet.size() == 0);
    }

    /**
     * <p>Loads the Manuzio Schema from the database specified in the given
     * <code>url</code></p> <p>NOTE: the database must have a Manuzio Structure
     * version 3.1</p>
     *
     * @param connection a connection to the manuzio DB
     * @return the Schema Object loaded from the given database
     * @throws SQLException if a database error occurs.
     * @throws ParseException if the given database has a bad format, as if the
     * version is not 3.1 or if the database is empty
     */
    public static Schema loadFromDB(Connection connection) throws SQLException, ParseException {
        Schema schema = null;
        Map<String, String> ext = new HashMap<String, String>();	//memorizes the extensions
        ResultSet res = null;	//results of a query
        PreparedStatement stat = null; //query

        Connection conn = connection; //connects to DB

        try {	//checks database version
            double version;
            stat = conn.prepareStatement("SELECT schema_name, db_version FROM schema");
            res = stat.executeQuery();
            if (!res.next()) {
                throw new ParseException("Invalid database format:\nNo Schema definition found.", -1);
            }
            version = res.getDouble("db_version");
            if (version != ManuzioViewer.getVersion()) {
                throw new ParseException("Invalid database format:\nSupported version is " + ManuzioViewer.getVersion() + ".\nDatabase version is " + version + ".", -1);
            }

            //builds the schema
            schema = new Schema(res.getString("schema_name"));

            if (res.next()) {
                throw new ParseException("Invalid database format:\nMultiple Schema definitions found.", -1);
            }
        } catch (SQLException e) {
            //closes resources
            ManuzioViewer.close(res);
            ManuzioViewer.close(stat);
            ManuzioViewer.close(conn);
            throw new ParseException("Invalid database format:\n" + e.getMessage(), -1);
        }

        try {
            //loads types
            stat = conn.prepareStatement("SELECT type_name, plural_name, extends FROM types");
            res = stat.executeQuery();
            while (res.next()) {
                String supertype = res.getString("extends");
                schema.addType(res.getString("type_name"), res.getString("plural_name"));
                if (supertype != null) {
                    ext.put(res.getString("type_name"), supertype);	//memorizes the type's extensions
                }
            }

            //links types
            stat = conn.prepareStatement("SELECT * FROM type_composition");
            res = stat.executeQuery();
            while (res.next()) {
                Type container = schema.getType(res.getString("container_type"));
                container.addComponent(res.getString("component_name"), schema.getType(res.getString("contained_type")), res.getBoolean("is_plural"), res.getBoolean("optional"));
            }

            //loads supertypes
            for (String s : ext.keySet()) {
                Type type = schema.getType(s);
                Type supertype = schema.getType(ext.get(s));
                type.addSupertype(supertype);
            }

            //checks schema's integrity and loads the Minimal Unit
            Parser.checkCycles(schema);

            //loads the Maximal Unit
            Parser.setMaxUnit(schema);

            //loads attributes
            stat = conn.prepareStatement("SELECT type_name, label, type, is_plural FROM attribute_types");
            res = stat.executeQuery();
            while (res.next()) {
                Type type = schema.getType(res.getString("type_name"));
                type.addAttribute(res.getString("label"), res.getString("type"), res.getBoolean("is_plural"));
            }

            //loads methods
            stat = conn.prepareStatement("SELECT type_name, label, syntax, is_plural FROM methods");
            res = stat.executeQuery();
            while (res.next()) {
                Type type = schema.getType(res.getString("type_name"));
                type.addMethod(res.getString("label"), res.getString("syntax"), res.getBoolean("is_plural"));
            }

            return schema;
        } catch (SQLException | ParseException e) {
            throw e;
        } finally {
            //closes resources
            ManuzioViewer.close(res);
            ManuzioViewer.close(stat);
            ManuzioViewer.close(conn);
        }
    }

    /**
     * <p>Saves the Schema into the database
     * just created</p> <p>If
     * <code>override = true</code> and already exists a database with the given
     * name, then tries to delete and substitute it with a new database</p>
     *
     * @param url the server path -either of the
     * form <code>jdbc:subprotocol:serverPath</code>, or only the serverPath
     * itself
     * @param dbName the name given to the new database
     * @param user the username to log in the server
     * @param password - the password used to log in the server using the
     * account of <code>user</code>
     * @throws SQLException if a server error occurs.
     * @throws ParseException if the Schema to save is empty
     */
    public void saveToDB(String url, String dbName, String user, String password) throws SQLException, ParseException {
        if (this.isEmpty()) {
            throw new ParseException("Error: the Schema is empty.", -1);
        }
        Connection conn = ConnectionPoolFactory.getConnection(url + "/" + dbName, user, password); //connects to the server

        PreparedStatement query_ins_type = null, query_ins_att = null, query_ins_met = null, query_type_comp = null, query_schema = null, query_supertype = null; //query
        Set<ComponentProperty> compSet = new HashSet<ComponentProperty>();	//memorizes the type's structure
        Map<String, String> supertype = new HashMap<String, String>();	//memorizes the type's extensions

        // conn = ManuzioViewer.buildManuzioDB(url, dbName, user, password, override); //builds the database (creato attraverso l'interfaccia grafica)

        try {
            //prepare the statements 
            query_ins_type = conn.prepareStatement("INSERT INTO types (type_name, plural_name) VALUES (? , ?)");
            query_ins_att = conn.prepareStatement("INSERT INTO attribute_types (type_name, label, type, is_plural, editable) VALUES (? , ? , ?, ?, true)");
            query_ins_met = conn.prepareStatement("INSERT INTO methods (type_name, label, syntax, is_plural) VALUES (? , ? , ?, ?)");
            query_type_comp = conn.prepareStatement("INSERT INTO type_composition (container_type, contained_type, component_name, optional, is_plural) VALUES (?, ?, ?, ?, ?)");
            query_schema = conn.prepareStatement("INSERT INTO schema (schema_name, max_unit, min_unit, db_version) VALUES (?, ?, ?, ?)");
            query_supertype = conn.prepareStatement("UPDATE types SET extends = ? WHERE type_name = ?");

            //for each Type
            for (Type t : this.getTypeSet()) {
                //adds the Type
                query_ins_type.setString(1, t.getTypeName());
                query_ins_type.setString(2, t.getPluralName());
                if (t.hasSuperType()) {
                    supertype.put(t.getTypeName(), t.getSuperType().getTypeName());
                }
                query_ins_type.executeUpdate();


                //adds each Attribute
                if (t.hasAt(number.BOTH)) {
                    for (Attribute a : t.getOwnAt(number.BOTH)) {
                        query_ins_att.setString(1, t.getTypeName());
                        query_ins_att.setString(2, a.getAtName());
                        query_ins_att.setString(3, a.getAtType());
                        query_ins_att.setBoolean(4, a.isPlural());
                        query_ins_att.executeUpdate();
                    }
                }

                //adds each Method
                if (t.hasMethods(number.BOTH)) {
                    for (Method m : t.getOwnMethods(number.BOTH)) {
                        query_ins_met.setString(1, t.getTypeName());
                        query_ins_met.setString(2, m.getMethodName());
                        query_ins_met.setString(3, m.getMethodSyntax());
                        query_ins_met.setBoolean(4, m.isPlural());
                        query_ins_met.executeUpdate();
                    }
                }

                compSet.addAll(Arrays.asList(t.getOwnComponents()));	//fills the container of this schema's type structure
            }

            //links each type
            for (ComponentProperty comp : compSet) {
                query_type_comp.setString(1, comp.getContainer().getTypeName());
                query_type_comp.setString(2, comp.getComponent().getTypeName());
                query_type_comp.setString(3, comp.getComponentName());
                query_type_comp.setBoolean(4, comp.isOptional());
                query_type_comp.setBoolean(5, comp.isPlural());
                query_type_comp.executeUpdate();
            }

            //adds supertypes
            for (String type : supertype.keySet()) {
                query_supertype.setString(1, supertype.get(type));
                query_supertype.setString(2, type);
                query_supertype.executeUpdate();
            }

            //builds the schema
            query_schema.setString(1, this.getName());
            query_schema.setString(2, this.getMaximalUnit().getTypeName());
            query_schema.setString(3, this.getMinimalUnit().getTypeName());
            query_schema.setDouble(4, ManuzioViewer.getVersion());
            query_schema.executeUpdate();

            //closes resources
            ManuzioViewer.close(query_schema, query_type_comp, query_ins_type, query_ins_att, query_ins_met, query_supertype);
            ManuzioViewer.close(conn);

        } catch (SQLException e) {
            //closes resources
            ManuzioViewer.close(query_schema, query_type_comp, query_ins_type, query_ins_att, query_ins_met, query_supertype);
            ManuzioViewer.close(conn);
            ManuzioViewer.deleteManuzioDB(url, dbName, user, password); //to not leave the DB in a inconsistent status
            throw e;
        }
    }

    /**
     * <p>Loads the Schema from a source code as a one big String.</p>
     *
     * @param code - a String Object which contains the code to analyze.
     * @return the Schema built analyzing the code
     * @throws IOException if an I/O error occurs
     * @throws ParseException if there are lexical, syntactical or semantical
     * errors in the source code
     */
    public static Schema buildFromSourceCode(String code) throws IOException, ParseException {
        return Parser.parsing(code);
    }

    /**
     * <p>Loads the Schema from a source code from a file.</p>
     *
     * @param f - file which contains the code to analyze.
     * @return the Schema built analyzing the code from the given file
     * @throws IOException if an I/O error occurs
     * @throws ParseException if there are lexical, syntactical or semantical
     * errors in the source code
     */
    public static Schema loadFromFile(File f) throws IOException, ParseException {
        return Parser.parsing(f);
    }
}