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

		// TODO: Java variable names should be descriptive and in "camelCase". Try sysInfo, systemInfo, info, etc...
		// Use the Eclipse Refactor tool to change variable names automatically.
		SystemInfo test = new SystemInfo();
		// TODO: Again, better java variables make code more readable. How about hardwareLayer?
		HardwareAbstractionLayer htes = test.getHardware();

		// TODO: change all variables to align with Java camelCase standard...
		//Processor Speed
		long pspeed = htes.getProcessor().getVendorFreq();

		//Memory Usage
		long mem = htes.getMemory().getAvailable();

		//Number of Cores	import io.vertx.core.file.AsyncFile;
		int pcore = htes.getProcessor().getPhysicalProcessorCount();
		int lcore = htes.getProcessor().getLogicalProcessorCount();

		// TODO: I want the physicloud systems to report their available memory too.
		
		//External Sensors

		//Task Load
		// TODO: Process count will give us too much info. CPU load is more useful measure for us.
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

		// TODO: Writing a generic file does not necessarily produce a readable JSON. You would have to write
		// the file using a Json output codec of some sort.
		vertx.fileSystem().writeFile("hbinfo.json", tbuff, new AsyncResultHandler<Void>() 
		{

			public void handle(AsyncResult asyncResult) 
			{
				if (asyncResult.succeeded())
					System.out.println("JSON Successfully Created");
				else if (asyncResult.failed())
					System.out.println("JSON Creation Unsuccessful");
			}
		});
	}
}
