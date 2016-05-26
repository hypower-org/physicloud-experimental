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
import edu.hsc.hypower.physicloud.util.JsonFieldNames;
import edu.hsc.hypower.physicloud.util.NeighborData;

/**
 * 
 * @author pmartin@hsc.edu
 *		   hackleyb18@hsc.edu	
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

				// TODO: Need a more sophisticated neighbor data book keeping mechanism!		
				JsonObject jsonInfo = msg.body();
				String tempIp = jsonInfo.getString(JsonFieldNames.IP_ADDR);
				int tempProcesses = jsonInfo.getInteger(JsonFieldNames.PROCESSES);					// I changed this field to current # of processes rather than vendor defined frequency
				long tempMem = jsonInfo.getLong(JsonFieldNames.MEMORY);
				int tempPCore = jsonInfo.getInteger(JsonFieldNames.P_CORES);
				int tempLCore = jsonInfo.getInteger(JsonFieldNames.L_CORES);
				double tempLoad = jsonInfo.getDouble(JsonFieldNames.LOAD);


				System.out.println("...heartbeat received from " + tempIp + " with " + tempProcesses + " processes and available memory: " + tempMem);
				if(tempIp != ipAddr){
					LocalMap<String,NeighborData> neighborMap = vertx.sharedData()
							.getLocalMap(KernelMapNames.NEIGHBORS);

					// TODO: Reinstate once the JsonObject is pulled apart correctly.
					neighborMap.put(tempIp, new NeighborData(tempIp, tempProcesses, tempMem, tempPCore, tempLCore, tempLoad));

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


	private final JsonObject createJson()	{

		SystemInfo sysInfo = new SystemInfo();
		HardwareAbstractionLayer hardwareLayer = sysInfo.getHardware();													
		int process = hardwareLayer.getProcessor().getProcessCount();			//Number of current processes		
		long memAvail = hardwareLayer.getMemory().getAvailable();				//Memory Usage			 
		int pCore = hardwareLayer.getProcessor().getPhysicalProcessorCount();	//Number of Cores
		int lCore = hardwareLayer.getProcessor().getLogicalProcessorCount();	//Logical Cores
		double pLoad = hardwareLayer.getProcessor().getSystemCpuLoad();			//Task Load

		JsonObject hbInfo = new JsonObject();									//Format the JSON with the correct data
		hbInfo.put(JsonFieldNames.IP_ADDR,  ipAddr);
		hbInfo.put(JsonFieldNames.PROCESSES, process);
		hbInfo.put(JsonFieldNames.MEMORY, memAvail);
		hbInfo.put(JsonFieldNames.P_CORES, pCore);
		hbInfo.put(JsonFieldNames.L_CORES, lCore);
		hbInfo.put(JsonFieldNames.LOAD, pLoad);

		return hbInfo;

	}
}

