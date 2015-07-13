package org.fastnet.util;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XMLBuilder {

	public static final String LISTDELIM = "_5_";
	public static final String LISTDELIM2 = "_52_";
	public static final String FDELIM = "_6_";
	public static final String STRLISTDELIM = "_#_";
	private static final String SP = " ";
	private static final char EQ = '=';
	private static final char QS = '"';
	private static final char QE = '"';
	private static final String NN = "";
	private static final char CH2 = '>';
	private static final char CH = '<';
	private static final String STR = "</";
	private static final String NL = "\n";
	private static final String STR2 = "  ";
	private static final String XML_HEADER = "<?xml version=\'1.0\'?>";
	private StringBuffer doc;
	private String rootElement;
	private int indent = 0;

	public XMLBuilder() {
		doc = new StringBuffer();
	}

	public XMLBuilder(String rootElement) {
		this(rootElement, true);
	}

	public XMLBuilder(String rootElement, boolean withHeader) {
		this();
		this.rootElement = rootElement;
		if (withHeader) {
			doc.append(XML_HEADER);
		}
		startElement(rootElement);
	}

	private void repeat() {
		for (int a = 0; a < indent; a++) {
			doc.append(STR2);
		}
	}

	private StringBuffer append(char ch) {
		return doc.append(ch);
	}

	public void startElement(String name) {
		doc.append(NL);
		indent++;
		repeat();
		append(CH);
		doc.append(name);
		doc.append(CH2);
	}

	public void endElement(String name) {
		doc.append(NL);
		repeat();
		doc.append(STR);
		doc.append(name);
		doc.append(CH2);
		indent--;
	}

	public void openElement(String name) {
		doc.append(NL);
		indent++;
		repeat();
		append(CH);
		doc.append(name);
		doc.append(SP);
		indent++;
	}

	public void closeElement() {
		doc.append(CH2);
		indent--;
	}

	public void element(String name, Object value) {
		element(name, value == null ? NN : value.toString());
	}

	public void element(String name, int value) {
		element(name, String.valueOf(value));
	}

	public void element(String name, long value) {
		element(name, String.valueOf(value));
	}

	public void element(String name, String value) {
		startElement(name);
		indent++;
		repeat();
		doc.append(value);
		indent--;
		endElement(name);
	}

	public void param(String name, Object value) {
		param(name, value == null ? SP : (value.getClass().equals(Date.class) ? LangUtils.getFormat().format(value) : value.toString()));
	}

	public void param(String name, int value) {
		param(name, String.valueOf(value));
	}

	public void param(String name, long value) {
		param(name, String.valueOf(value));
	}

	public void param(String name, String value) {
		doc.append(SP);
		doc.append(name);
		doc.append(EQ);
		doc.append(QS);
		doc.append(value);
		doc.append(QE);
		doc.append(SP);
	}

	public String buildDocument() {
		if (rootElement != null) {
			endElement(rootElement);
		}
		return doc.toString();
	}

	public void element(IXMLable subElement) {
		repeat();
		doc.append(subElement != null ? subElement.toXML() : SP);
		doc.append(SP);
	}

	/**
	 * List<Field> fieldList = new ArrayList<Field>(); exploreFields(fieldList, clazz);
	 * 
	 * @param fieldList
	 * @param clazz
	 */
	public static void exploreFields(List<Field> fieldList, Class<?> clazz) {
		fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
		Iterator<Field> iterator = fieldList.iterator();
		while (iterator.hasNext()) {
			Field next = iterator.next();
			if (Modifier.isTransient(next.getModifiers()) || Modifier.isStatic(next.getModifiers())) {
				iterator.remove();
			} else {
				next.setAccessible(true);
			}
		}
		if (clazz.getSuperclass() != null) {
			exploreFields(fieldList, clazz.getSuperclass());
		}
	}

	public String toXML(Object item, boolean withHeader) throws IllegalArgumentException, IllegalAccessException {
		if (withHeader) {
			doc.append(XML_HEADER);
		}
		toXML("", item);
		return doc.toString();
	}

	private void toXML(String fieldName, Object item) throws IllegalArgumentException, IllegalAccessException {
		List<Field> fieldList = new ArrayList<Field>();
		exploreFields(fieldList, item.getClass());
		String name = (fieldName.length() > 0 ? fieldName + FDELIM : "") + item.getClass().getName();
		openElement(name);
		Iterator<Field> iterator = fieldList.iterator();
		while (iterator.hasNext()) {
			Field field = iterator.next();
			if (LangUtils.isPrimitive(field.getType())) {
				param(field.getName(), field.get(item));
				iterator.remove();
			} else {
				if (List.class.isAssignableFrom(field.getType())) {
					List<?> list = (List) field.get(item);
					if (list != null && list.size() > 0) {
						Object object = list.get(0);
						if (object.getClass().equals(String.class)) {
							param(field.getName(), LangUtils.listToStr(list, STRLISTDELIM));
							iterator.remove();
						}
					}
				}
			}
		}
		closeElement();
		iterator = fieldList.iterator();
		while (iterator.hasNext()) {
			Field field = iterator.next();
			if (!LangUtils.isPrimitive(field.getType())) {
				if (List.class.isAssignableFrom(field.getType())) {
					List<?> list = (List) field.get(item);
					if (list != null && list.size() > 0) {
						String name2 = field.getName() + LISTDELIM + list.getClass().getName();
						startElement(name2);
						Iterator<?> iterator2 = list.iterator();
						while (iterator2.hasNext()) {
							Object next = iterator2.next();
							if (next != null) {
								toXML(LISTDELIM2, next);
							}
						}
						endElement(name2);
					}
				} else if (Map.class.isAssignableFrom(field.getType())) {
				} else {
					Object item2 = field.get(item);
					if (item2 != null) {
						toXML(field.getName(), item2);
					}
				}
			}
		}
		endElement(name);
	}

	public <T> T fromXml(String text) throws ParserConfigurationException, SAXException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		InputSource source = new InputSource(new StringReader(text));
		SAXHandler dh = new SAXHandler();
		parser.parse(source, dh);
		return dh.getItem();
	}
}
