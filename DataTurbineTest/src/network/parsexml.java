package network;

import static java.lang.Math.pow;
import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class parsexml {
	
	static private Vector<String>  foldernames;
	static private Vector<Integer> application_ids;
	static private Vector<String>  defaultendians;
	static private Vector<Vector<String>> parameternames;
	static private Vector<Vector<String>> parametertypes;
	static private Vector<Vector<String>> parameterinfos;
	static private Vector<Vector<Integer>> parametermasks;
	static private Vector<Vector<Integer>> parametersizes;
	static private Vector<Vector<String>> endianfields;

	public parsexml()
	{
		foldernames = new Vector<String>();
		application_ids = new Vector<Integer>(); 
		defaultendians = new Vector<String>();
		parameternames = new Vector<Vector<String>>();
		parametertypes = new Vector<Vector<String>>();
		parameterinfos = new Vector<Vector<String>>();
		parametermasks = new Vector<Vector<Integer>>();
		parametersizes = new Vector<Vector<Integer>>();
		endianfields = new Vector<Vector<String>>();
	}

	public  void parse() {

		File folder = new File("config/");      // searching in current path into the "config" file
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			
			if (file.isFile()) 
			{
				System.out.println(file.getName());
				
				try
				{
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(file);
					doc.getDocumentElement().normalize();

					NodeList nList = doc.getElementsByTagName("name");  // name field
					Node nNode = nList.item(0); // name element(node)
					foldernames.addElement(nNode.getTextContent());
					//System.out.println(foldernames.get(foldernames.size() - 1));

					nList = doc.getElementsByTagName("configs"); // getting application id 
					nNode = nList.item(0);
					Element eElement = (Element) nNode;
					nList = eElement.getElementsByTagName("config");
					nNode = nList.item(0);

					if (nNode.getNodeType() == Node.ELEMENT_NODE)
					{

						eElement = (Element) nNode;
						nNode = eElement.getElementsByTagName("tlmmidx").item(0);
						application_ids.addElement(ConvertHexaStringtoInteger(nNode.getTextContent()))  ;
						nNode = eElement.getElementsByTagName("endian").item(0);
						defaultendians.addElement(nNode.getTextContent());
						
						//System.out.println(application_ids.get(application_ids.size() - 1));

					}

					nList = doc.getElementsByTagName("telemetry"); // getting telemetry --> name, type, task
					nNode = nList.item(0);
					eElement = (Element) nNode;
					nList = eElement.getElementsByTagName("parameter");

					Vector<String> names = new Vector<String>();
					Vector<String> types = new Vector<String>();
					Vector<String> infos = new Vector<String>();
					Vector<Integer> masks = new Vector<Integer>();
					Vector<Integer> sizes = new Vector<Integer>();
					Vector<String> endians = new Vector<String>();

					for (int temp = 0; temp < nList.getLength(); temp++) 
					{
						nNode = nList.item(temp);
						
						if (nNode.getNodeType() == Node.ELEMENT_NODE)
						{
							eElement = (Element) nNode;
							nNode = eElement.getElementsByTagName("name").item(0); //name
							names.addElement(nNode.getTextContent());


							nNode = eElement.getElementsByTagName("type").item(0); //type
							types.addElement(nNode.getTextContent());


							NodeList nList2 = eElement.getElementsByTagName("info"); //info
							if(nList2.getLength() != 0)
							{
								nNode = nList2.item(0);
								infos.addElement(nNode.getTextContent());

							}
							else
								infos.addElement("No Info");

							nList2 = eElement.getElementsByTagName("mask"); //mask
							if(nList2.getLength() != 0)
							{
								nNode = nList2.item(0);
								masks.addElement(ConvertHexaStringtoInteger(nNode.getTextContent()));

							}
							else
								masks.addElement(0xFFFFFFFF);

							nList2 = eElement.getElementsByTagName("size"); //size
							if(nList2.getLength() != 0)
							{
								nNode = nList2.item(0);
								sizes.addElement(Integer.parseInt(nNode.getTextContent()));

							}
							else
								sizes.addElement(0);
							
							nList2 = eElement.getElementsByTagName("endian"); //size
							if(nList2.getLength() != 0)
							{
								nNode = nList2.item(0);
								endians.addElement(nNode.getTextContent());

							}
							else
								endians.addElement("empty");
						}
					}

					parameternames.addElement(names);
					parametertypes.addElement(types);
					parameterinfos.addElement(infos);
					parametermasks.addElement(masks);
					parametersizes.addElement(sizes);
					endianfields.addElement(endians);
				}

				catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

	}

	public void print()
	{

		for(int i = 0; i < foldernames.size(); i++)
		{
			System.out.println(foldernames.get(i));
			System.out.println(application_ids.get(i));
			for(int j = 0; j < (parameternames.get(i)).size(); j++)
			{

				System.out.println((parameternames.get(i)).get(j) + " , " + (parametertypes.get(i)).get(j) + " , " + (parameterinfos.get(i)).get(j) + " , " + (parametermasks.get(i)).get(j) + " , " + (parametersizes.get(i)).get(j));                    
			}
		}

	}

	static int ConvertHexaStringtoInteger(String str)  //  U16 & U8
	{
		int j,k,l;
		int result = 0;
		k= 16;
		for(int i = 2; i< str.length() ; i++)
		{
			if ( (int)(str.charAt(i)) > (int)('@') )
			{
				j = (int)(str.charAt(i)) - (int)('A') + 10;
			}
			else
				j = (int)(str.charAt(i)) - (int)('0');
			l = str.length() - i - 1;
			result += ( pow(k,l) ) * j;
		}

		return result;

	}

	public Vector<String> get_foldernames()
	{
		return foldernames;
	}

	public Vector<Integer> get_applicationids()
	{
		return application_ids;
	}

	public Vector<Vector<String>> get_parameternames()
	{
		return parameternames;
	}

	public Vector<Vector<String>> get_parametertypes()
	{
		return parametertypes;
	}

	public Vector<Vector<String>> get_parameterinfos()
	{
		return parameterinfos;
	}

	public Vector<Vector<Integer>> get_parametermasks()
	{
		return parametermasks;
	}

	public Vector<Vector<Integer>> get_parametersizes()
	{
		return parametersizes;
	}
	
	public Vector<Vector<String>> get_endianfields()
	{
		return endianfields;
	}
	
	public Vector<String> get_defaultendians()
	{
		return defaultendians;
	}

}
