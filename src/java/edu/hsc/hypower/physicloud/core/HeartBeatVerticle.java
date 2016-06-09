package edu.hsc.hypower.physicloud.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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

		vertx.setPeriodic(hbPeriod, this::handleHeartbeat);

		vertx.setPeriodic(2000, this::timeoutChecker);

		//		vertx.setPeriodic(5000, this::removalChecker);

		vertx.eventBus().consumer(KernelChannels.HEARTBEAT, new Handler<Message<JsonObject>>(){

			@Override
			public void handle(Message<JsonObject> msg) {											

				// Need a more sophisticated neighbor data book keeping mechanism!		
				JsonObject jsonInfo = msg.body();
				String tempIp = jsonInfo.getString(JsonFieldNames.IP_ADDR);
				
				System.out.println(jsonInfo.encodePrettily());

				if(tempIp != ipAddr){
					System.out.println("Heartbeat received from " + tempIp);
					LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);

					neighborMap.put(tempIp, new NeighborData(tempIp));

					// Update the last update time from this neighbor.
					neighborUpdateTimes.put(tempIp, System.currentTimeMillis());
				}
			}
		});

	}

	/**
	 * Creates the heartbeat message for this cyber-physical unit and publishes it to all other CPUs.
	 * @param timerEvent
	 */
	private final void handleHeartbeat(Long timerEvent){

		//Creates the initial JSON and places the IP address into the file
		JsonObject hbInfo = new JsonObject();
		hbInfo.put(JsonFieldNames.IP_ADDR,  ipAddr);

		
		LocalMap<Integer, String> deviceMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);
	
		JsonArray sensorArray = new JsonArray();


		//Store list of sensors in array and place proper information into JSON
		for(int i = 0; i < deviceMap.size(); i++){
			sensorArray.clear();
			for(Object key : vertx.sharedData().getLocalMap(deviceMap.get(i)).keySet()){ 
				sensorArray.add(key);
			}
			hbInfo.put(deviceMap.get(i), sensorArray);
		}

		vertx.eventBus().publish(KernelChannels.HEARTBEAT, hbInfo);
		//		System.out.println("Heartbeat sent...");
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}	

	private final void timeoutChecker(Long timerEvent){

		String neighborTimeoutKey = new String();
		for(Entry<String, Long> entry : neighborUpdateTimes.entrySet()){
			if(System.currentTimeMillis() - entry.getValue()  > NEIGHBOR_TIMEOUT){	
				neighborTimeoutKey = entry.getKey();
				LocalMap<String, NeighborData> removalMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);
				removalMap.remove(entry.getKey());
				System.out.println(neighborTimeoutKey + " removed from cluster");
			}
		}

		if(!neighborTimeoutKey.isEmpty()){
			neighborUpdateTimes.remove(neighborTimeoutKey);
		}

	}

	//	private final void removalChecker(Long timerEvent){
	//		LocalMap<String,NeighborData> rMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);
	//		
	//		for(String key: rMap.keySet()){
	//			System.out.println(key + " " + rMap.get(key));
	//		}
	//		
	//	}

}
