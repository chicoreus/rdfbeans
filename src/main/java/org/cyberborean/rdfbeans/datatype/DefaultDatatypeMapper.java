package org.cyberborean.rdfbeans.datatype;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;


/**
 * Default DatatypeMapper implementation based on XML-Schema data types for 
 * representation of Java primitive wrapper classes and dates as typed RDF 
 * literals. 
 * 
 * <ul>
 * <li>Instances of {@link String} are represented as plain (untyped) RDF
 * literals.</li>
 * <li>Instances of {@link Boolean}, {@link Integer}, {@link Float},
 * {@link Double}, {@link Byte}, {@link Long} and {@link Short} are represented
 * as RDF literals with corresponding XML-Schema datatypes.</li>
 * <li>Instances of {@link Date} are represented as RDF literals of <code>xsd:dateTime</code>
 * type, serialized into ISO8601 date/time format.</li>
 * </ul>
 * 
 * 
 */
public class DefaultDatatypeMapper implements DatatypeMapper {

	private static final Map<Class<?>, IRI> DATATYPE_MAP = new HashMap<>();
	static {
		
		// standard XML-Schema datatypes
		DATATYPE_MAP.put(String.class, XSD.STRING);
		DATATYPE_MAP.put(Integer.class, XSD.INT);
		DATATYPE_MAP.put(Date.class, XSD.DATETIME);
		DATATYPE_MAP.put(Boolean.class, XSD.BOOLEAN);
		DATATYPE_MAP.put(Float.class, XSD.FLOAT);
		DATATYPE_MAP.put(Double.class, XSD.DOUBLE);
		DATATYPE_MAP.put(Byte.class, XSD.BYTE);
		DATATYPE_MAP.put(Long.class, XSD.LONG);
		DATATYPE_MAP.put(Short.class, XSD.SHORT);
		DATATYPE_MAP.put(BigDecimal.class, XSD.DECIMAL);
		DATATYPE_MAP.put(java.net.URI.class, XSD.ANYURI);
		
		// custom datatypes
		DATATYPE_MAP.put(Character.class, Java.CHAR);
	}

	public static IRI getDatatypeURI(Class<?> c) {
		// Check for direct mapping
		IRI uri = DATATYPE_MAP.get(c);
		if (uri == null) {
			// Check for first assignable type mapping 
			for (Map.Entry<Class<?>, IRI> entry : DATATYPE_MAP.entrySet()) {
				if (entry.getKey().isAssignableFrom(c)) {
					return entry.getValue();
				}
			}
		}
		return uri;
	}

	public Object getJavaObject(Literal l) {
		IRI dt = l.getDatatype();
		if ((dt == null) || XSD.STRING.equals(dt)) {
			return l.stringValue();
		} 
		else if (XSD.BOOLEAN.equals(dt)) {
			return l.booleanValue();
		} 
		else if (XSD.INT.equals(dt)) {
			return l.intValue(); //Integer.valueOf(l.intValue());
		} 
		else if (XSD.BYTE.equals(dt)) {
			return l.byteValue(); //Byte.valueOf(l.byteValue());
		} 
		else if (XSD.LONG.equals(dt)) {
			return l.longValue();//Long.valueOf(l.longValue());
		} 
		else if (XSD.SHORT.equals(dt)) {
			return l.shortValue(); //Short.valueOf(l.shortValue());
		} 
		else if (XSD.FLOAT.equals(dt)) {
			return l.floatValue(); //Float.valueOf(l.floatValue());
		} 
		else if (XSD.DOUBLE.equals(dt)) {
			return l.doubleValue();//Double.valueOf(l.doubleValue());
		} 
		else if (XSD.DECIMAL.equals(dt)) {
			return l.decimalValue();
		}
		else if (XSD.ANYURI.equals(dt)) {
			return java.net.URI.create(l.stringValue());
		}
		else if (XSD.DATETIME.equals(dt)) {
			return l.calendarValue().toGregorianCalendar().getTime();
		} 
		else if (Java.CHAR.equals(dt)) {
			String s = l.stringValue();
			return !s.isEmpty() ? l.stringValue().charAt(0) : '\u0000';
		}
		return l.stringValue();
	}

	@Override
	public Literal getRDFValue(Object value, ValueFactory vf) {
		if (value instanceof Date) {
			return vf.createLiteral((Date)value);
		}
		IRI dtUri = getDatatypeURI(value.getClass());
		if (dtUri != null) {
			if (dtUri.equals(XSD.STRING)) {
				return vf.createLiteral(value.toString());
			}
			return vf.createLiteral(value.toString(), dtUri);
		}
		return null;
	}
}
