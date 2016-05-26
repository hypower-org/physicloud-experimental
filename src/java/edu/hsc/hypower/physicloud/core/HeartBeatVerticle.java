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

		vertx.setPeriodic(hbPeriod, this::handleHeartbeat);

		vertx.eventBus().consumer(KernelChannels.HEARTBEAT, new Handler<Message<JsonObject>>(){

			@Override
			public void handle(Message<JsonObject> msg) {											
				// Lines 64-74 will be changed upon completion of the Vert.x message codec.
				
				// TODO: Ok that is fine. I commented out the code since its dependencies 

				// TODO: Need a more sophisticated neighbor data book keeping mechanism!		
				// I was messing around with the idea of sending a JSON OBject and then parsing, but I believe it requires the codec
				// TODO: Yes - because you are trying to send a JSON file! If you just send a Vertx Json object, it will work fine.
				JsonObject jsonInfo = msg.body();
//				JSONParser parser = new JSONParser();
//				JSONObject tempObj;
//				try {
//					tempObj = (JSONObject) parser.parse(jsonInfo);

				// TODO: Look at how I use the JsonObject and unpack it...
				String tempIp = jsonInfo.getString("ipAddr");
				long tempSpeed = jsonInfo.getLong("proSpeed");

				//					String tempIp = tempObj.get("ipAddr").toString();
				
				// TODO: These are bugs below!!! You will get null pointer exceptions since the keys you use on
				// the Json object do not match the keys in your createJson function below.
				
				//					long tempSpeed = (long) tempObj.get("Processor Speed");
				//					long tempMem = (long) tempObj.get("Available Memory");
				//					int tempPCore = (int) tempObj.get("Physical Number of Cores");						
				//					int tempLCore = (int) tempObj.get("Logical Number of Cores");
				//					double tempLoad = (double) tempObj.get("Processor Load");

				System.out.println("...heartbeat received from " + tempIp + " with speed " + tempSpeed + "Hz");
				if(tempIp != ipAddr){
					LocalMap<String,NeighborData> neighborMap = vertx.sharedData()
							.getLocalMap(KernelMapNames.NEIGHBORS);

					// TODO: Reinstate once the JsonObject is pulled apart correctly.
					//						neighborMap.put(tempIp, new NeighborData(tempIp, tempSpeed, tempMem, tempPCore, tempLCore, tempLoad));

					// Update the last update time from this neighbor.
					neighborUpdateTimes.put(tempIp, System.currentTimeMillis());
				}
			}
		});

	}

	// See use as a function reference above.
	private final void handleHeartbeat(Long timerEvent){
		// TODO: send the heart beat message
		JsonObject sendInfo = createJson();
		vertx.eventBus().publish(KernelChannels.HEARTBEAT, sendInfo);
		System.out.println("Heartbeat sent...");
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}


	// TODO: Creating and transmitting the *entire file* is too much. We can just send the JsonObject itself and unpack it.
	// BUT, we will need some constant string variables (much like the KernelChannels and KernelMapNames classes I made).
	private final JsonObject createJson()	{

		SystemInfo sysInfo = new SystemInfo();
		HardwareAbstractionLayer hardwareLayer = sysInfo.getHardware();													
		long pSpeed = hardwareLayer.getProcessor().getVendorFreq();				//Processor Speed		
		long memAvail = hardwareLayer.getMemory().getAvailable();				//Memory Usage			 
		int pCore = hardwareLayer.getProcessor().getPhysicalProcessorCount();	//Number of Cores
		int lCore = hardwareLayer.getProcessor().getLogicalProcessorCount();	//LOgical Cores
		double pLoad = hardwareLayer.getProcessor().getSystemCpuLoad();			//Task Load

		JsonObject hbInfo = new JsonObject();									//Format the JSON with the correct data
		hbInfo.put("ipAddr",  ipAddr);
		hbInfo.put("proSpeed", pSpeed);
		hbInfo.put("availMemory", memAvail);
		hbInfo.put("pCores", pCore);
		hbInfo.put("lCores", lCore);
		hbInfo.put("pLoad", pLoad);

//		try
//		{
//			File file = new File("hbInfo.json");
//			file.createNewFile();
//			FileWriter filewriter = new FileWriter(file);
//			System.out.println("Creating JSON file");
//
//			filewriter.write(hbInfo.toString());
//			filewriter.flush();
//			filewriter.close();
//		}
//		catch(IOException e) {
//			e.printStackTrace();
//		}

		return hbInfo;

	}
}

