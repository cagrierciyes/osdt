/*
 RDV 2.0 NCGetFile.java
 Author: Charles Cowart
 Date: 02/2010
 */

package org.rdv.action;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class NCGetFile
{
	private String sessionKey;	
	private DocumentBuilderFactory factory;
	private DocumentBuilder parser;
	private DocumentBuilder parser2;
	private String UserName;
	private String UserPassword;
	
	public NCGetFile(String name, String password) throws Exception
	{
		sessionKey = null;
		UserName = name;
		UserPassword = password;
		factory = DocumentBuilderFactory.newInstance();
		parser = factory.newDocumentBuilder();
		parser2 = factory.newDocumentBuilder();
		getSessionID(UserName, UserPassword);	//verify name and password are correct before continuing
	}

	private String appendSessionKeytoURI(String URI)
	{
		return URI + "?GAsession=" + sessionKey;
	}
	
	private String genRepetitionURI(int pid, int eid, int tid, int rid)
	{
		return "https://neesws.neeshub.org:9443/REST/Project/" + pid + "/Experiment/" + eid + "/Trial/" + tid + "/Repetition/" + rid;
	}
	
	private String genTrialURI(int pid, int eid, int tid)
	{
		return "https://neesws.neeshub.org:9443/REST/Project/" + pid + "/Experiment/" + eid + "/Trial/" + tid;
	}
	
	private String genExperimentURI(int pid, int eid)
	{
		return "https://neesws.neeshub.org:9443/REST/Project/" + pid + "/Experiment/" + eid;
	}
	
	private String genProjectURI(int pid)
	{
		return "https://neesws.neeshub.org:9443/REST/Project/" + pid;
	}

	private String getProjectName(int pid) throws Exception
	{
		String result = null;
		try
		{
			URL myURL = new URL(appendSessionKeytoURI(genProjectURI(pid)));
			HttpsURLConnection myConnection = (HttpsURLConnection) myURL.openConnection();
	
			parser.reset();
			Document d = parser.parse(myConnection.getInputStream());
			
			NodeList list = d.getDocumentElement().getElementsByTagName("Project");
		
			Node project = list.item(0);	//there should only be one Project element
		
			Node name = project.getFirstChild();	//should be name
		
			Node value = name.getFirstChild();	//text node
			result = value.getNodeValue();
		
			parser.reset();
		}catch(Exception e)
		{
			throw new Exception("Cannot locate project information");
		}
		
		return result;
	}

	private String getProjectDataFileURI(int pid) throws Exception
	{
		String result = null;
		try
		{
			URL myURL = new URL(appendSessionKeytoURI(genProjectURI(pid))); 
					
			HttpsURLConnection myConnection = (HttpsURLConnection) myURL.openConnection();
		
			parser.reset();
			Document d = parser.parse(myConnection.getInputStream());
			
			NodeList list = d.getDocumentElement().getElementsByTagName("Project");
		
			Node project = list.item(0);	//there should only be one Project element
		
			Node name = project.getLastChild();	//should be DataFile
		
			NamedNodeMap map = name.getAttributes();
		
			result = "https://neesws.neeshub.org:9443" + map.getNamedItem("link").getNodeValue();
				
			parser.reset();
		}catch(Exception e)
		{
			throw new Exception("Cannot locate main project file information");
		}
		
		return result;
	}
	
	private String getNextDataFileURI(String previousDataFilePath, String nextDataFileName) throws Exception
	{
		String result = null;

			URL myURL = new URL(appendSessionKeytoURI(previousDataFilePath)); 
	
			HttpsURLConnection myConnection = (HttpsURLConnection) myURL.openConnection();
		
			parser.reset();
			Document d = parser.parse(myConnection.getInputStream());
		
			NodeList tmpList = d.getDocumentElement().getElementsByTagName("DataFile");
		
			Element tmpElement = (Element)tmpList.item(0);			//Assume that first element is home directory with equal to experiment name.
		
			tmpList = tmpElement.getElementsByTagName("DataFile");	//get all DataFiles (subFolders, files) elements from the project home folder element.
		
			NodeList tmpList2;
			String tmpString;
			Element tmpElement2;
		
			result = null;
		
			NamedNodeMap tmpMap;
		
			for(int i = 0; i < tmpList.getLength(); i++)
			{
				tmpElement = (Element) tmpList.item(i);
				tmpList2 = tmpElement.getElementsByTagName("name");
				tmpElement2 = (Element) tmpList2.item(0);	//assume first returned is name of subfolder or file.
				tmpString = tmpElement2.getFirstChild().getNodeValue();
			
				if(tmpString.compareTo(nextDataFileName) == 0)
				{
					tmpMap = tmpElement.getAttributes();
					tmpString = tmpMap.getNamedItem("link").getNodeValue();
					result = "https://neesws.neeshub.org:9443" + tmpString;
					break;
				}
			}
		
			parser.reset();

		
		return result;
	}
	
	public String getUserFileURI(int pid, String experimentName, String trialName, String repetitionName, String RelativePath) throws Exception
	{
		VerifySession();
		
		String[] temp;
		
		//(re)combine required strings with potential relative path strings.
		String t = experimentName + "/" + trialName + "/" + repetitionName + "/" + RelativePath;
		
		//split complete path along delimiter so that we can drill down from project info.
		temp = t.split("/");
		
		//get start (project information: includes DataFile paths, which are diff from project folder 'logical' paths)
		String ptr = getProjectDataFileURI(pid);
		int i = 0;
		
		try
		{
			for(i = 0; i < temp.length; i++)
			{
				//traverse each folder info in turn
				//looking for the DataFile id for the next level
				//in the path.
				ptr = getNextDataFileURI(ptr, temp[i]);
			}
		}catch(Exception e)
		{
			String s;
	
			i--;
			
			if(i == -1)
			{
				s = "" + pid;
			}else
			{
				s = temp[i];
			}
			
			//if at any point WS doesn't return valid xml data with the DataFile element
			//we are looking for, assume the folder name isn't valid, or an error in
			//transmission occured.
			throw new Exception("Cannot locate project information for '" + s + "'");
		}
		
		//ptr should currently hold the data for the folder which stores the file
		//(the name of which is the final element of the array).
		//get the file's download URL.
		String result = null;
		try
		{
			URL myURL = new URL(appendSessionKeytoURI(ptr)); 
		
			HttpsURLConnection myConnection = (HttpsURLConnection) myURL.openConnection();
		
			parser.reset();
		
			Document d = parser.parse(myConnection.getInputStream());
		
			NodeList tmpList = d.getDocumentElement().getElementsByTagName("DataFile");
		
			Element tmpElement = (Element)tmpList.item(0);

			tmpList = tmpElement.getElementsByTagName("url");
			tmpElement = (Element) tmpList.item(0);
			result = tmpElement.getFirstChild().getNodeValue();

			parser.reset();
		}catch(Exception e)
		{
			throw new Exception("Cannot locate project information for project relative path '" + RelativePath + "'");
		}
		
		//return the url to the user. This url can be used to download the file.
		return result;
	}
	
	public boolean writeFileToLocal(String NCPath, File local) throws Exception
	{
		VerifySession();
		
		try
		{
			local.createNewFile();
		}catch(Exception e)
		{
			throw new Exception("Cannot create file locally");
		}
		
		try
		{
			URL myURL = new URL(appendSessionKeytoURI(NCPath)); 
			HttpsURLConnection myConnection = (HttpsURLConnection) myURL.openConnection();
			FileOutputStream out;
			out = new FileOutputStream(local);
			InputStreamReader in = new InputStreamReader(myConnection.getInputStream());
			int ch;
			while((ch = in.read()) > -1)
			{
				out.write(ch);
			}
			out.flush();
			out.close();
		}catch(Exception e)
		{
			throw new Exception("Cannot download file to local");
		}
							
		return true;
	}
	
	public int FindProjectID(String ProjectName) throws Exception
	{
		VerifySession();
		
		URL wsURL = new URL(appendSessionKeytoURI("https://neesws.neeshub.org:9443/REST/Project"));
		HttpsURLConnection blah = (HttpsURLConnection) wsURL.openConnection();
		
		parser.reset();
		
		Document d = parser.parse(blah.getInputStream());
		NodeList list = d.getDocumentElement().getElementsByTagName("Project");
		Node node, node2;
		NamedNodeMap map;
		
		String id, link, name;
		
		URL url;
		HttpsURLConnection blah2;
		Document d2;
		NodeList list2;
		
		for(int i = list.getLength() - 1; i >= 0; i--)
		{
			node = list.item(i);
			map = node.getAttributes();
			node2 = map.getNamedItem("id");
			id = node2.getNodeValue();
			node2 = map.getNamedItem("link");
			link = "https://neesws.neeshub.org:9443" + node2.getNodeValue();
			
			
			url = new URL(appendSessionKeytoURI(link));
			blah2 = (HttpsURLConnection) url.openConnection();
			
			parser2.reset();
			
			d2 = parser2.parse(blah2.getInputStream());
			
			list2 = d2.getDocumentElement().getElementsByTagName("name");
			node = list2.item(0);
			node = node.getFirstChild();
			name = node.getNodeValue();
						
			if(name.compareTo(ProjectName) == 0)
			{
				parser2.reset();
				return Integer.parseInt(id);
			}
		}
		
		throw new Exception("Cannot find Project ID for '" + ProjectName + "'");
	}
	
	
	
	private void getSessionID(String name, String password) throws Exception
	{
		//securely connect to Grid Auth to authenticate user, obtain session ID.
		try
		{
		  String auth_url = "https://neesws.neeshub.org:9443/GRIDAUTH/gridauth.cgi?username=" + name + "&password=" + password;
			//String auth_url = "https://central.nees.org/cgi-bin/gridauth.cgi?username=" + name + "&password=" + password;

			URL authURL = new URL(auth_url);
		
			HttpsURLConnection connection = (HttpsURLConnection) authURL.openConnection();
			
			//obtain a document representation of the GridAuth response
			parser.reset();
			Document d = parser.parse(connection.getInputStream());
		
			//obtain references to 'key' elements in GridAuth response
			NodeList list = d.getDocumentElement().getElementsByTagName("key");
		
			//9th key element is hardcoded as 'key=session'
			//once session element is found, first child element will be the 'text'
			//of the element. The text in this case will be the session ID value.
			sessionKey = ((Text)list.item(8).getFirstChild()).getNodeValue();
			parser.reset();
		}catch(Exception e)
		{
			throw new Exception("Cannot Login");
		}
	}
		   
	private void VerifySession() throws Exception
	{
			//later add check for timeout
		   if(sessionKey == null)
		   {
			   getSessionID(UserName, UserPassword);
		   }
	}
}
	

