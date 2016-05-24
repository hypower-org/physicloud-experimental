package edu.hsc.hypower;


import java.io.*;
import java.util.Map.Entry;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.jna.platform.linux.Libc.Sysinfo;
import oshi.software.*;
import oshi.json.*;


import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileProps;



public class OshiTest 
{
	public static void main(String[] args) 
	{
		Vertx vertx = Vertx.vertx();
		
		SystemInfo test = new SystemInfo();
		HardwareAbstractionLayer htes = test.getHardware();
		
		//Processor Speed
		long pspeed = htes.getProcessor().getVendorFreq();
		
		//Memory Usage
		long mem = htes.getMemory().getAvailable();
	
		//Number of Cores	import io.vertx.core.file.AsyncFile;
		int pcore = htes.getProcessor().getPhysicalProcessorCount();
		int lcore = htes.getProcessor().getLogicalProcessorCount();
		
		//External Sensors
		
		//Task Load
		int pcount = htes.getProcessor().getProcessCount();
		double pload = htes.getProcessor().getSystemCpuLoad();
		//double [] ptick = htes.getProcessor().getProcessorCpuLoadBetweenTicks();
		
		
	
		
		//Place all necessary information into a JSON file
		
		JsonObject hbtest = new JsonObject();
		
		hbtest.put("Processor Speed", pspeed);
		hbtest.put("Available Memory", mem);
		hbtest.put("Physical Number of Cores", pcore);
		hbtest.put("Logical Number of Cores", lcore);
		hbtest.put("Processes Running", pcount);
		hbtest.put("Processor Load", pload);
		//hbtest.put("CPU Load Between Ticks", ptick);
		
	
		
		
		Buffer tbuff = Buffer.buffer();
		//Buffer all = Buffer.buffer();	
		
		hbtest.writeToBuffer(tbuff);
	
		
		vertx.fileSystem().writeFile("hbinfo.json", tbuff, new AsyncResultHandler<Void>() 
		{

			public void handle(AsyncResult asyncResult) 
			{
				if (asyncResult.succeeded())
					System.out.println("JSON Successfully Created");
				else if (asyncResult.failed())
					System.out.println("JSON Creation Unsuccessful");
			}
		}
				);
	
		
		
		
	
	}
	

}
