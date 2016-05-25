package edu.hsc.hypower.physicloud.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.hsc.hypower.physicloud.*;
import edu.hsc.hypower.physicloud.util.NeighborData;

/**
 * 
 * @author pmartin@hsc.edu
 *
 */
public class HeartBeatVerticle extends AbstractVerticle {

	private final static long NEIGHBOR_TIMEOUT = 5000;

	private final String ipAddr;
	private final Long hbPeriod;

	// Local structure to keep track of timing. If we have not heard from a neighber in a long
	// time (now 5 seconds) we need to do something...
	private HashMap<String,Long> neighborUpdateTimes;

	public HeartBeatVerticle(String ip, Long hbp){
		ipAddr = ip;
		hbPeriod = hbp;
		neighborUpdateTimes = new HashMap<String,Long>();
	}

	@Override
	public void start() throws Exception {
		super.start();

		// TODO: Note my use of the "function reference" notation from Java 8. As our verticles
		// get more complicated, we should implement the functions in the verticle class and pass
		// the function reference to the Vertx call.
		// For more details, see the Java 8 book and look up function reference.
		vertx.setPeriodic(hbPeriod, this::handleHeartbeat);

		vertx.eventBus().consumer(KernelChannels.HEARTBEAT, new Handler<Message<String>>(){

			@Override
			public void handle(Message<String> event) {

				// TODO: Need a more sophisticated neighbor data book keeping mechanism!
				String jsonInfo = event.body();
				JSONParser parser = new JSONParser();
				JSONObject tempObj;
				try {
					tempObj = (JSONObject) parser.parse(jsonInfo);
					String tempIp = tempObj.get("ipAddr").toString();
					long tempSpeed = (long) tempObj.get("Processor Speed");
					long tempMem = (long) tempObj.get("Available Memory");
					int tempPCore = (int) tempObj.get("Physical Number of Cores");						
					int tempLCore = (int) tempObj.get("Logical Number of Cores");
					double tempLoad = (double) tempObj.get("Processor Load");

					System.out.println("...heartbeat received from " + tempIp);
					if(tempIp != ipAddr){
						LocalMap<String,NeighborData> neighborMap = vertx.sharedData()
								.getLocalMap(KernelMapNames.NEIGHBORS);
						neighborMap.put(tempIp, new NeighborData(tempIp, tempSpeed, tempMem, tempPCore, tempLCore, tempLoad));
						// Update the last update time from this neighbor.
						neighborUpdateTimes.put(tempIp, System.currentTimeMillis());
					}

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

		});



	}

	// See use as a function reference above.
	private final void handleHeartbeat(Long timerEvent){
		// TODO: send the heart beat message
		JSONObject sendInfo = this.createJson();
		vertx.eventBus().publish(KernelChannels.HEARTBEAT, sendInfo);
		System.out.println("Heartbeat sent...");
	}

	@Override
	public void stop() throws Exception {

		super.stop();
	}


	//	Function to create a JSON file with the correct specifications : modified code from OshiTest.java

	private final JSONObject createJson()	{

		SystemInfo sysInfo = new SystemInfo();
		HardwareAbstractionLayer hardwareLayer = sysInfo.getHardware();													
		long pSpeed = hardwareLayer.getProcessor().getVendorFreq();				//Processor Speed		
		long memAvail = hardwareLayer.getMemory().getAvailable();				//Memory Usage			 
		int pCore = hardwareLayer.getProcessor().getPhysicalProcessorCount();	//Number of Cores
		int lCore = hardwareLayer.getProcessor().getLogicalProcessorCount();	//LOgical Cores
		double pLoad = hardwareLayer.getProcessor().getSystemCpuLoad();			//Task Load

		//External Sensor data.... ?


		JSONObject hbInfo = new JSONObject();									//Format the JSON with the correct data
		hbInfo.put("ipAddr",  ipAddr);
		hbInfo.put("proSpeed", pSpeed);
		hbInfo.put("availMemory", memAvail);
		hbInfo.put("pCores", pCore);
		hbInfo.put("lCores", lCore);
		hbInfo.put("pLoad", pLoad);

		try
		{
			File file = new File("hbInfo.json");
			file.createNewFile();
			FileWriter filewriter = new FileWriter(file);
			System.out.println("Creating JSON file");

			filewriter.write(hbInfo.toString());
			filewriter.flush();
			filewriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		return hbInfo;

	}
}

