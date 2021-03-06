/*******************************************************************************
 *     Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 *     Licensed under the Apache License, Version 2.0 (the "License"); 
 *     you may not use this file except in compliance with the License. 
 *     You may obtain a copy of the License at 
 *    
 *        http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.utils;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.ebayopensource.turmeric.utils.config.SchemaValidationLevel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public abstract class XMLParseUtils { 
		
	public static final String SYS_PROP_CONFIG_SCHEMA_CHECK 
		= "com.ebay.securitycommon.utils.schemacheck";

	protected static final SchemaValidationLevel s_schemaCheckLevel;
	
	static {
	   	String schemaCheckStr = System.getProperty(SYS_PROP_CONFIG_SCHEMA_CHECK);
	   	if (schemaCheckStr == null) {
	   		s_schemaCheckLevel = SchemaValidationLevel.NONE;
	   	} else {
	   		schemaCheckStr = schemaCheckStr.toUpperCase();
	   		s_schemaCheckLevel = SchemaValidationLevel.valueOf(schemaCheckStr);

    	}
	}
	
	public static SchemaValidationLevel getSchemaCheckLevel() {
		return s_schemaCheckLevel;
	}
	
	


	public static synchronized Document parseXML(String fileName, String schemaName, boolean isOptional, String topLevelName) throws Exception {
		ClassLoader classLoader = ContextUtils.getClassLoader();
		InputStream	inStream = classLoader.getResourceAsStream(fileName);
		if (inStream == null) {
			// TODO - verify that we don't need a global file, even now that the
			// monitoring lives there.
			if (isOptional) {
				return null;
			}

			throw new Exception("cannot load filename:" + fileName);
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document result = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ErrorHandler errorHandler = new ParseErrorHandler(fileName, getSchemaCheckLevel());
			builder.setErrorHandler(errorHandler);
			result = builder.parse(inStream);
		} catch (ParserConfigurationException e) {
			throw new Exception("parse error: " + e.toString());
		} catch (SAXException e) {
			throw new Exception("parse error: " + e.toString());
		} catch (IOException e) {
			throw new Exception("parse error: " + e.toString());
		}
		Element docElement = result.getDocumentElement();
        if (docElement == null) {
        	throw new Exception("validation error: Document has no top-level element");
        }
        validate(schemaName, fileName, docElement, getSchemaCheckLevel());
        if (!docElement.getNodeName().equals(topLevelName)) {
        	throw new Exception("validation error: Top-level element name: " + docElement.getNodeName() + "; expected: " + topLevelName);
        }
		return result;
    }

	
	private static void validate(String schemaName, String filename, Node document, SchemaValidationLevel checkLevel) throws Exception {
		if (checkLevel.equals(SchemaValidationLevel.NONE)) {
			return;
		}
		ClassLoader classLoader = ContextUtils.getClassLoader();
		URL url = classLoader.getResource(schemaName);
		if (url == null) {
			throw new Exception("cannot load file: " + schemaName);
    	}

		SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);

		try {
			Schema schema = factory.newSchema(url);
			Validator validator = schema.newValidator();
			ErrorHandler errorHandler = new ParseErrorHandler(filename, checkLevel);
			validator.setErrorHandler(errorHandler);
			validator.validate(new DOMSource(document));
		} catch (SAXException se) {
			throw new Exception("parse error on filename: " + filename + ", " + se.toString());
		} catch (IOException ioe) {
			throw new Exception("parse error on schema: " + filename + ", " + ioe.toString());
		}
	}

	private static class ParseErrorHandler implements ErrorHandler {
		private final String m_filename;
		private final SchemaValidationLevel m_checkLevel;
		
		ParseErrorHandler(String filename, SchemaValidationLevel checkLevel) {
			m_filename = filename;
			m_checkLevel = checkLevel;
		}
		public void warning(SAXParseException e) {
			reportError(Level.WARNING, e);
		}

		public void error(SAXParseException e) throws SAXParseException {
			reportError(Level.SEVERE, e);
			if (m_checkLevel.equals(SchemaValidationLevel.ERROR)) {
				throw e;
			}
		}
		
		public void fatalError(SAXParseException e) throws SAXParseException {
			reportError(Level.SEVERE, e);
			if (m_checkLevel.equals(SchemaValidationLevel.ERROR)) {
				throw e;
			}
		}
		
		private void reportError(Level level, SAXParseException e) {
			StringBuffer b = new StringBuffer();
			b.append(m_filename);
			int line = e.getLineNumber();
			if (line != -1) {
				b.append(" line ");
				b.append(line);
			}
			b.append(": ");
			b.append(e.getMessage());
			//LogManager.getInstance(this.getClass()).log(level, b.toString());
		}
	}

}
