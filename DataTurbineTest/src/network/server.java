package network;

import java.io.*;
import java.net.*;

public class server
{
	public static source src;
	public static void main(String args[]) throws InterruptedException
	{
		DatagramSocket sock = null;

		try
		{
			//1. creating a server socket, parameter is local port number
			sock = new DatagramSocket(1235);

			//buffer to receive incoming data
			byte[] buffer = new byte[65536];
			DatagramPacket incoming = new DatagramPacket(buffer, buffer.length); // UDP packet

			src = new source("TELEMETRY");  // creating source from xmlparser vectors

			/* Communication Loop */ 
			while(true)
			{
				sock.receive(incoming);
				byte[] data = incoming.getData(); // receive byte array

				int application_id=(int) ( (data[0] & 0xFF) * 256 + (data[1] & 0xFF)); //getting parser value
				src.send(data, application_id); // send to rbnb

			}

		}

		catch(IOException e)
		{
			System.err.println("IOException " + e);
		}
	}


	/* public static void parse_DS(byte[] data , short size)
   {
    	//-----Start parse --------//

    	int application_id=(int) ( (data[0] & 0xFF) * 256 + (data[1] & 0xFF));
    	if(  ( (application_id & (short)(0xFFF)) != (short) (0x8b8) ) )
    	{
    		echo("application_id did not match");

    	}

    	int ds_sequence_count = (int) ( (data[2] & 0xFF) * 256 + (data[3] & 0xFF));
    	if(  ( (ds_sequence_count & (short)(0xC000)) != 0 ) )
    		ds_sequence_count = (int) (0xFFFF);

    	int packet_length = (int) ( (data[4] & 0xFF) * 256 + (data[5] & 0xFF));
    	if(packet_length != size - 7)
    	{
    		echo("packet_length did not match");

    	}

    } */

	public static void echo(String msg)
	{
		System.out.println(msg);
	}

}