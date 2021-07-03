package com.filenet.cpe.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class XMLCreator {
	
	private Document dom;
	private boolean isEnabled;
	
	//Document List Error Flag - used for tracking success or failure
	private int errorFlag;
	
	public XMLCreator() {
		dom = null;
		isEnabled = false;
		errorFlag = 0;
	}
	
	public Document getDom() {
		return dom;
	}

	public void setDom(Document dom) {
		this.dom = dom;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public int getErrorFlag() {
		return errorFlag;
	}

	public void setErrorFlag(int errorFlag) {
		this.errorFlag = errorFlag;
	}
	
	public void createDocument()
	{
		//get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try
		{
			//get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			//Create an instance of DOM
			dom = db.newDocument();
		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
	}
	
	public Element createRootElement(String rootElementName)
	{
		//Create the Root Element
		Element rootEle = dom.createElement(rootElementName);
		
		return rootEle;
	}
	
	public Element createRootElementAttribute(Element rootElementName, String rootAttrName, String rootAttrValue)
	{
		rootElementName.setAttribute(rootAttrName, rootAttrValue);
		return rootElementName;
	}
	
	public Element createSubElementAttribute(Element subElementName, String subAttrName, String subAttrValue)
	{
		subElementName.setAttribute(subAttrName, subAttrValue);
		return subElementName;
	}
	
	public Element createSubElement(String subElementName)
	{
		Element subEle = dom.createElement(subElementName);
		return subEle;
	}
	
	public Element createSubElementChild(String subElementName)
	{
		Element subEleChild = dom.createElement(subElementName);
		return subEleChild;
	}
	
	public Text createSubElementChildValue(String value)
	{
		//Create Text Value for Sub Element
		Text elementText = dom.createTextNode(value);
		return elementText;
	}
	
	public void rootElementSave(Element rootElement)
	{
		//Add Root Element to Document
		dom.appendChild(rootElement);
		//Enable the boolean
		isEnabled = true;
	}
	
	public Element subElementSave(Element rootElement, Element subElement)
	{
		//Add Sub Element to Root Element
		rootElement.appendChild(subElement);
		return rootElement;
	}
	
	//Save Text Element to Sub Element
	public Element textNodeSave(Element subElement, Text textNode)
	{
		//Add Text Element to Sub Element
		subElement.appendChild(textNode);
		return subElement;
	}
	
	//Save XML to File
	public void saveXMLtoFile(String path)
	{
		try
		{
			OutputFormat format = new OutputFormat(dom);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(
					new FileOutputStream(new File(path)), format);
			
			serializer.serialize(dom);
		}
		catch (IOException ie)
		{
			ie.printStackTrace();
		}
	}
	
	//Save XML to Byte Array Output Stream
	public ByteArrayOutputStream saveXMLtoStream()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			OutputFormat format = new OutputFormat(dom);
			format.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(baos, format);
			serializer.serialize(dom);
		}
		catch (IOException ie)
		{
			ie.printStackTrace();
		}
		
		return baos;
	}
	
}
