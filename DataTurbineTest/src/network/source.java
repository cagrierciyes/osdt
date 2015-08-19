
package network;

import com.rbnb.sapi.*;
import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class source {

	private Source source;
	private ChannelMap cMap;

	private parsexml xmlparser; // xmlparser object

	private Vector<Vector<Integer>> channelindexes; // container to hold seperate channel indexes for RDV sink.
	/*Test */
	public source(String tlm)  // Constructor
	{
		try
		{
			source=new Source();  // create source
			source.OpenRBNBConnection("localhost:3333", tlm); // connect to rbnb
			cMap = new ChannelMap(); // create channel map

			xmlparser = new parsexml(); // create that object for parsing xml files.
			xmlparser.parse(); // creating parameter containers from xml files
			xmlparser.print(); // print parameters

			channelindexes = new Vector<Vector<Integer>>();

			createchannels();  // creating seperate channels from parser
		}

		catch (SAPIException se) { se.printStackTrace(); }
	}

	void createchannels() // create channels for RDV sink
	{
		int i,j;

		for(i=0 ; i < (xmlparser.get_foldernames()).size(); i++)
		{
			Vector<Integer> indexes = new Vector<Integer>();
			for(j=0 ; j < ((xmlparser.get_parameternames()).get(i)).size() ; j++)
			{
				try
				{
					indexes.addElement(cMap.Add((xmlparser.get_foldernames()).get(i) + "/" + ((xmlparser.get_parameternames()).get(i)).get(j)));
				}
				catch (SAPIException se) { se.printStackTrace(); }
			}

			channelindexes.addElement(indexes);

		}

	}
	
	public short bytesToShort(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}
	
	public byte[] shortToBytes(short value) {
	    return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array();
	}
	
	public static int bytesToInt(byte[] b) {
	    final ByteBuffer bb = ByteBuffer.wrap(b);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    return bb.getInt();
	}

	public static byte[] intToBytes(int i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putInt(i);
	    return bb.array();
	}

	public void send(byte[] rawdata , int app_id) throws IOException   //send data to rbnb server
	{
		int pointer = 0;
		int i = 0;
		boolean available = false;

		for(; i < xmlparser.get_applicationids().size(); i++)
		{
			if(xmlparser.get_applicationids().get(i) == app_id){
				available = true;
				System.out.println(" XML FILE OK / APID: " + app_id);
				break;
			}		
		}

		if(available){

			for(int j = 0; j < xmlparser.get_parametertypes().get(i).size(); j++)
			{
				if(xmlparser.get_parametertypes().get(i).get(j).equals("S8") || xmlparser.get_parametertypes().get(i).get(j).equals("U8")) // U8 & S8
				{
					try
					{			
						short[] array = new short[1];
						
						if(xmlparser.get_endianfields().get(i).get(j).equals("LE") || ( xmlparser.get_endianfields().get(i).get(j).equals("empty") && xmlparser.get_defaultendians().get(i).equals("LE"))){
							array[0] = (short) ((short) (rawdata[pointer] & 0xFF) & (xmlparser.get_parametermasks().get(i).get(j) & 0x000000FF));
							
							System.out.println("LE USHORT: " + array[0]);
						}
						else{
							array[0] = (short) ((short) (rawdata[pointer] & 0xFF) & (xmlparser.get_parametermasks().get(i).get(j) & 0x000000FF));
							System.out.println("BE USHORT: " +array[0]);
						}
						
						cMap.PutDataAsInt16(channelindexes.get(i).get(j),array);
						//cMap.PutUserInfo(channelindexes.get(i).get(j) , xmlparser.get_parameterinfos().get(i).get(j));
						source.Flush(cMap);
						pointer++;
					}
					catch (SAPIException se) { se.printStackTrace(); }

				}
				else if(xmlparser.get_parametertypes().get(i).get(j).equals("S16") || xmlparser.get_parametertypes().get(i).get(j).equals("U16")) //S16 & U16
				{			
					try
					{
						int[] array = new int[1];
			
						if(xmlparser.get_endianfields().get(i).get(j).equals("LE") || ( xmlparser.get_endianfields().get(i).get(j).equals("empty") && xmlparser.get_defaultendians().get(i).equals("LE"))){
							array[0] = (int) (((rawdata[pointer] & 0xFF) + ((rawdata[pointer+1] & 0xFF) << 8)) & (xmlparser.get_parametermasks().get(i).get(j) & 0x0000FFFF));
							System.out.println("LE USHORT: " + array[0]);
						}
						else{
							array[0] = (int) ((((rawdata[pointer] & 0xFF) << 8) + (rawdata[pointer+1] & 0xFF)) & (xmlparser.get_parametermasks().get(i).get(j) & 0x0000FFFF));
							System.out.println("BE USHORT: " +array[0]);
						}
					
						cMap.PutDataAsInt32(channelindexes.get(i).get(j),array);
						//cMap.PutUserInfo(channelindexes.get(i).get(j) , xmlparser.get_parameterinfos().get(i).get(j));
						source.Flush(cMap);
						pointer = pointer + 2;
					}
					catch (SAPIException se) { se.printStackTrace(); }
				}
				else if(xmlparser.get_parametertypes().get(i).get(j).equals("S32") || xmlparser.get_parametertypes().get(i).get(j).equals("U32")) //S32 & U32
				{
					try
					{
						long[] array = new long[1];
						
						if(xmlparser.get_endianfields().get(i).get(j).equals("LE") || ( xmlparser.get_endianfields().get(i).get(j).equals("empty") && xmlparser.get_defaultendians().get(i).equals("LE"))){
							array[0] = (long) (((rawdata[pointer] & 0xFF) + ((rawdata[pointer+1] & 0xFF) << 8)+ ((rawdata[pointer+2] & 0xFF) << 16)+ ((rawdata[pointer+3] & 0xFF) << 24)) & (xmlparser.get_parametermasks().get(i).get(j) & 0xFFFFFFFF));
							System.out.println("LE UINT: " + array[0]);
						}
						else{
							array[0] = (long) (((rawdata[pointer+3] & 0xFF) + ((rawdata[pointer+2] & 0xFF) << 8)+ ((rawdata[pointer+1] & 0xFF) << 16)+ ((rawdata[pointer] & 0xFF) << 24)) & (xmlparser.get_parametermasks().get(i).get(j) & 0xFFFFFFFF));
							System.out.println("BE UINT: " +array[0]);
						}


						cMap.PutDataAsInt64(channelindexes.get(i).get(j),array);
						//cMap.PutUserInfo(channelindexes.get(i).get(j) , xmlparser.get_parameterinfos().get(i).get(j));
						source.Flush(cMap);
						pointer = pointer + 4;
					}
					catch (SAPIException se) { se.printStackTrace(); }
				}

				else if(xmlparser.get_parametertypes().get(i).get(j).equals("TIME") ) // TIME
				{
					pointer = pointer + 6;
				}

				else if(xmlparser.get_parametertypes().get(i).get(j).equals("STRING") ) // STRING
				{
					String str = "";
					for(int k = 0 ; k < xmlparser.get_parametersizes().get(i).get(j) ; k++)
					{
						char c = (char) (rawdata[pointer]  & xmlparser.get_parametermasks().get(i).get(j));
						str = str + c ;
						pointer++;
					}

					try
					{
						cMap.PutDataAsString(channelindexes.get(i).get(j) , str);
						//cMap.PutUserInfo(channelindexes.get(i).get(j) , xmlparser.get_parameterinfos().get(i).get(j));
						source.Flush(cMap);
					}
					catch (SAPIException se) { se.printStackTrace(); }
				}

				else if(xmlparser.get_parametertypes().get(i).get(j).equals("FLOAT") ) // FLOAT
				{
					try{

						float[] array = new float[1];
						byte[] bytes = { rawdata[pointer] , rawdata[pointer + 1] , rawdata[pointer + 2] , rawdata[pointer + 3]};

						if(xmlparser.get_endianfields().get(i).get(j).equals("empty") )
						{
							if(xmlparser.get_defaultendians().get(i).equals("LE") )
							{
								array[0] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
							}
							else
							{
								array[0] = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
							}
						}

						else
						{
							if(xmlparser.get_endianfields().get(i).get(j).equals("LE") )
								array[0] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
							else
								array[0] = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
						}
						
						cMap.PutDataAsFloat32(channelindexes.get(i).get(j),array);
						//cMap.PutUserInfo(channelindexes.get(i).get(j) , xmlparser.get_parameterinfos().get(i).get(j));
						source.Flush(cMap);
						pointer = pointer + 4;
					}
					catch (SAPIException se) { se.printStackTrace(); }

				}

				else if(xmlparser.get_parametertypes().get(i).get(j).equals("DOUBLE") ) // DOUBLE
				{
					try{

						double[] array = new double[1];
						byte[] bytes = { rawdata[pointer] , rawdata[pointer + 1] , rawdata[pointer + 2] , rawdata[pointer + 3] , rawdata[pointer + 4] , rawdata[pointer + 5] , rawdata[pointer + 6] , rawdata[pointer + 7]}; 

						if(xmlparser.get_endianfields().get(i).get(j).equals("empty") )
						{
							if(xmlparser.get_defaultendians().get(i).equals("LE") )
							{
								array[0] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
							}
							else
							{
								array[0] = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
							}
						}

						else
						{
							if(xmlparser.get_endianfields().get(i).get(j).equals("LE") )
								array[0] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
							else
								array[0] = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
						}


						cMap.PutDataAsFloat64(channelindexes.get(i).get(j),array);
						//cMap.PutUserInfo(channelindexes.get(i).get(j) , xmlparser.get_parameterinfos().get(i).get(j));
						source.Flush(cMap);
						pointer = pointer + 8;
					}
					catch (SAPIException se) { se.printStackTrace(); }

				}

				else
				{
					System.out.println(" XML TYPE ERROR / APID: " + app_id);
				}

			} 
		}else{
				System.out.println(" XML FILE ERROR / APID: " + app_id);
		}
	}
}
/*
0x08 0x01 
0xC0 0x02 
0x01 0x15
0x50 0x46 0x0F 0x00 0xE3 0x07 

0x00
0x00
0x01
0x00 
0x00
0x01
0x01
0x01

0x3D 0x00 
0x29 0x00

0x01
0x00
0x00
0x00

0x00 0x00 0x00 0x00 
0x02 0x00
0x01
0x00 

0x01 0x00 0x00 0x00
0x0A 0x00
0x01
0x00 

0x02 0x00 0x00 0x00 
0x02 0x00 0x01 0x00 0x03 0x00 0x00 0x00 0x02 0x00 0x01 0x00 0x04 0x00 0x00 0x00 
0x01 0x00 0x01 0x00 0x05 0x00 0x00 0x00 0x02 0x00 0x01 0x00 0x07 0x00 0x00 0x00 
0x02 0x00 0x01 0x00 0x08 0x00 0x00 0x00 0x02 0x00 0x01 0x00 0x09 0x00 0x00 0x00 
0x0A 0x00 0x01 0x00 0x0A 0x00 0x00 0x00 0x03 0x00 0x01 0x00 0x0B 0x00 0x00 0x00 
0x03 0x00 0x01 0x00 0x0C 0x00 0x00 0x00 0x01 0x00 0x01 0x00 0x0D 0x00 0x00 0x00 
0x06 0x00 0x01 0x00 0x0E 0x00 0x00 0x00 0x03 0x00 0x01 0x00 0x0F 0x00 0x00 0x00 
0x02 0x00 0x01 0x00 0x10 0x00 0x00 0x00 0x01 0x00 0x01 0x00 0x11 0x00 0x00 0x00 
0x04 0x00 0x01 0x00 0x12 0x00 0x00 0x00 0x01 0x00 0x01 0x00 0x13 0x00 0x00 0x00 
0x03 0x00 0x01 0x00 0x14 0x00 0x00 0x00 0x01 0x00 0x01 0x00 0x00 0x00 0x00 0x00 
0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 
0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 
0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 
0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 
0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 
0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00
 * */

