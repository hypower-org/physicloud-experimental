package edu.hsc.hypower;


import java.io.*;
import java.io.FileWriter;
import java.util.Map.Entry;
import java.io.IOException;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.jna.platform.linux.Libc.Sysinfo;
import oshi.software.*;
import oshi.json.*;


import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileProps;

import org.json.simple.JSONObject;



public class OshiTest 
{
	public static void main(String[] args) 
	{
		Vertx vertx = Vertx.vertx();

	
		SystemInfo sysInfo = new SystemInfo();
	
		HardwareAbstractionLayer hardwareLayer = sysInfo.getHardware();

		//Processor Speed
		long pSpeed = hardwareLayer.getProcessor().getVendorFreq();

		//Memory Usage
		long memAvail = hardwareLayer.getMemory().getAvailable();

		//Number of Cores	 
		int pCore = hardwareLayer.getProcessor().getPhysicalProcessorCount();
		int lCore = hardwareLayer.getProcessor().getLogicalProcessorCount();

				
		//External Sensors

		//Task Load
		double pLoad = hardwareLayer.getProcessor().getSystemCpuLoad();


		//Place all necessary information into a JSON file

		JSONObject hbtest = new JSONObject();

		hbtest.put("Processor Speed", pSpeed);
		hbtest.put("Available Memory", memAvail);
		hbtest.put("Physical Number of Cores", pCore);
		hbtest.put("Logical Number of Cores", lCore);
		hbtest.put("Processor Load", pLoad);

		//Buffer tBuff = Buffer.buffer();
		

		//hbtest.writeToBuffer(tBuff);
		
		try
		{
			File file = new File("hbInfo.json");
			file.createNewFile();
			FileWriter filewriter = new FileWriter(file);
			System.out.println("Creating JSON file");
			
			filewriter.write(hbtest.toJSONString());
			filewriter.flush();
			filewriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
			
		}
}
		


