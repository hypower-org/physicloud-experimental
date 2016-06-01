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

	// Local structure to keep track of timing. If we have not heard from a neighbor in a long
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

		// TODO: Add another periodic event to handleNeighborTimeouts... 

		vertx.setPeriodic(hbPeriod, this::handleHeartbeat);

		vertx.eventBus().consumer(KernelChannels.HEARTBEAT, new Handler<Message<JsonObject>>(){

			@Override
			public void handle(Message<JsonObject> msg) {											

				// Need a more sophisticated neighbor data book keeping mechanism!		
				JsonObject jsonInfo = msg.body();
				String tempIp = jsonInfo.getString(JsonFieldNames.IP_ADDR);
				long tempMem = jsonInfo.getLong(JsonFieldNames.MEMORY);
				int tempPCore = jsonInfo.getInteger(JsonFieldNames.P_CORES);
				int tempLCore = jsonInfo.getInteger(JsonFieldNames.L_CORES);
				double tempLoad = jsonInfo.getDouble(JsonFieldNames.LOAD);

				System.out.println("...heartbeat received from " + tempIp + " with available memory: " + tempMem);

				if(tempIp != ipAddr){
					LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);

					neighborMap.put(tempIp, new NeighborData(tempIp, tempMem, tempPCore, tempLCore, tempLoad));

					// Update the last update time from this neighbor.
					
					neighborUpdateTimes.put(tempIp, System.currentTimeMillis());
					
					// One idea of how to remove neighborData for nodes that take too long to respond
					
					if(System.currentTimeMillis() - neighborUpdateTimes.get(tempIp) > NEIGHBOR_TIMEOUT)	{
						neighborMap.remove(tempIp);
					}
						
				}
			}
		});

	}

	/**
	 * Creates the heartbeat message for this cyber-physical unit and publishes it to all other CPUs.
	 * @param timerEvent
	 */
	private final void handleHeartbeat(Long timerEvent){
		SystemInfo sysInfo = new SystemInfo();
		HardwareAbstractionLayer hardwareLayer = sysInfo.getHardware();													
		long memAvail = hardwareLayer.getMemory().getAvailable();				//Memory Usage			 
		int pCore = hardwareLayer.getProcessor().getPhysicalProcessorCount();	//Number of Cores
		int lCore = hardwareLayer.getProcessor().getLogicalProcessorCount();	//Logical Cores
		double pLoad = hardwareLayer.getProcessor().getSystemCpuLoad();			//Task Load

		JsonObject hbInfo = new JsonObject();									//Format the JSON with the correct data
		hbInfo.put(JsonFieldNames.IP_ADDR,  ipAddr);
		hbInfo.put(JsonFieldNames.MEMORY, memAvail);
		hbInfo.put(JsonFieldNames.P_CORES, pCore);
		hbInfo.put(JsonFieldNames.L_CORES, lCore);
		hbInfo.put(JsonFieldNames.LOAD, pLoad);

		vertx.eventBus().publish(KernelChannels.HEARTBEAT, hbInfo);
		System.out.println("Heartbeat sent...");
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}
}

