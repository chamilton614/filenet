package com.hp.docsolutions.filenet.p8.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class XMLReader {

	Document dom;
	
	public XMLReader() {
		dom = null;
	}
	
	public Document getDom() {
		return dom;
	}

	public void setDom(Document dom) {
		this.dom = dom;
	}

	public void readFile(String xmlFile)
	{
		//Get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try
		{
			//Get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			//Read XML into Document Object
			dom = db.parse(xmlFile);
		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (SAXException se)
		{
			se.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	public Element getRootElement()
	{
		Element rootElement;
		//Get the Document Root Element
		rootElement = dom.getDocumentElement();
		
		return rootElement;
	}
	
	public NodeList getElementNodesByTag(Element element, String tagName)
	{
		NodeList nl = null;
		//Get the List of Nodes from the Element
		nl = element.getElementsByTagName(tagName);
		return nl;
	}
	
	public String getElementAttributeValue(Element element, String attribute)
	{
		String value = "";
		//Get Element Attribute Value
		value = element.getAttribute(attribute);
		return value;
	}
	
	public String getElementTextValueByTag(Element element, String tagName)
	{
		String value = "";
		NodeList nl = null;
		//Get Element Text Value
		nl = element.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0)
		{
			Element el = (Element)nl.item(0);
			value = el.getFirstChild().getNodeValue();
		}
		return value;
	}
}
