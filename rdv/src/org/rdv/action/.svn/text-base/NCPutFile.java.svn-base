/*
 RDV 2.0 NCPutFile.java
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class NCPutFile
{
	private String sessionKey;	
	private DocumentBuilderFactory factory;
	private DocumentBuilder parser;
	private String UserName;
	private String UserPassword;
	private static Log log=LogFactory.getLog(NCPutFile.class.getName());

	public NCPutFile(String name, String password) throws Exception
	{
		sessionKey = null;
		UserName = name;
		UserPassword = password;
		factory = DocumentBuilderFactory.newInstance();
		parser = factory.newDocumentBuilder();
		getSessionID(UserName, UserPassword);	//verify name and password are correct before continuing
	}

	
	public void writeFileToNEESCentral(File localFile, String name, String path) throws Exception
	{
		VerifySession();
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpPost httppost = new HttpPost("https://neesws.neeshub.org:9443/REST/DataFile");
		
		
		FileBody bin = new FileBody(localFile);
		StringBody comment = new StringBody("<DataFile viewable=\"MEMBERS\"><name>" + name + "</name><path>" + path + "</path><curationStatus>Uncurated</curationStatus></DataFile>");
		//comment.writeTo(System.out);
		
		StringBody auth = new StringBody(sessionKey);
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("upload", bin);
		reqEntity.addPart("message", comment);
		reqEntity.addPart("GAsession", auth);
		
		httppost.setEntity(reqEntity);
		
		log.debug("executing request " + httppost.getRequestLine());
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity();
		
		log.debug("----------------------------------------");
		log.debug(response.getStatusLine());
		
		if(resEntity == null)
		{
			throw new Exception("File failed to upload to NEESCentral");
		}
		
		int status = response.getStatusLine().getStatusCode();
	
		if(400 == status)
		{
			throw new Exception("A file in NEESCentral already has that path.");
		}
		
		if(200 != status)
		{
			throw new Exception("File failed to upload to NEESCentral");
		}
		
		if (resEntity != null)
		{
			resEntity.consumeContent();
		}
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
		
		String t = experimentName + "/" + trialName + "/" + repetitionName + "/" + RelativePath;
		
		temp = t.split("/");
		
		String ptr = getProjectDataFileURI(pid);
		int i = 0;
		
		try
		{
			for(i = 0; i < temp.length; i++)
			{
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
			
			throw new Exception("Cannot locate project information for '" + s + "'");
		}
		
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
	

