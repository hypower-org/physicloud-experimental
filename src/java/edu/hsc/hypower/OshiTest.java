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
import io.vertx.core.json.JsonObject;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileProps;

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
		
		// TODO: See my use of JsonObject here! I removed the Jackon JSONObject class.
		JsonObject hbJson = new JsonObject();

		hbJson.put("Processor Speed", pSpeed);
		hbJson.put("Available Memory", memAvail);
		hbJson.put("Physical Number of Cores", pCore);
		hbJson.put("Logical Number of Cores", lCore);
		hbJson.put("Processor Load", pLoad);

		//Buffer tBuff = Buffer.buffer();
		

		//hbtest.writeToBuffer(tBuff);
		
		try
		{
			File file = new File("hbInfo.json");
			file.createNewFile();
			FileWriter filewriter = new FileWriter(file);
			System.out.println("Creating JSON file");
			
			filewriter.write(hbJson.toString());
			filewriter.flush();
			filewriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
			
		}
}
		


