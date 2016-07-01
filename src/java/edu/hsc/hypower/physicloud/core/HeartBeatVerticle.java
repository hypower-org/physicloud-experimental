package edu.hsc.hypower.physicloud.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
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
import java.util.Iterator;
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

		vertx.setPeriodic(5000, this::timeoutChecker);

		vertx.eventBus().consumer(KernelChannels.HEARTBEAT, new Handler<Message<JsonObject>>(){
			@Override
			public void handle(Message<JsonObject> msg) {											

				// Need a more sophisticated neighbor data book keeping mechanism!		
				JsonObject hbJsonMsg = msg.body();
				String hbIpAddr = hbJsonMsg.getString(JsonFieldNames.IP_ADDR);

				if(hbIpAddr != ipAddr){
					System.out.println("Heartbeat received from " + hbIpAddr);
					LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);
					
//					System.out.println(hbJsonMsg.encodePrettily());

					//Instantiation of objects necessary for parsing JSON
					HashMap<String, ArrayList<String>> neighborResourceMap = new HashMap<String,ArrayList<String>>();
					for(String fieldName : hbJsonMsg.fieldNames()){
						// If it is not the IP, it will be a resource array
						if(fieldName.compareTo(JsonFieldNames.IP_ADDR) != 0){
							ArrayList<String> resourceNames = new ArrayList<String>();
							JsonArray resourceArr = hbJsonMsg.getJsonArray(fieldName);
							for(int i = 0; i < resourceArr.size(); i++){
								resourceNames.add(resourceArr.getString(i));
							}
							neighborResourceMap.put(fieldName, resourceNames);
						}
					}
					neighborMap.put(hbIpAddr, new NeighborData(hbIpAddr, neighborResourceMap));

					// Update the last update time from this neighbor.
					neighborUpdateTimes.put(hbIpAddr, System.currentTimeMillis());
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
		JsonObject hbInfoMsg = new JsonObject();
		hbInfoMsg.put(JsonFieldNames.IP_ADDR,  ipAddr);

		
		LocalMap<Integer, String> deviceMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);
	
		JsonArray sensorArray = new JsonArray();
		//Store list of sensors in array and place proper information into JSON
		for(int i = 0; i < deviceMap.size(); i++){
			sensorArray.clear();
			for(Object key : vertx.sharedData().getLocalMap(deviceMap.get(i)).keySet()){
				sensorArray.add(key);
			}
			hbInfoMsg.put(deviceMap.get(i), sensorArray);
		}
		System.out.println(ipAddr + " alive.");
		vertx.eventBus().publish(KernelChannels.HEARTBEAT, hbInfoMsg);
	}

	@Override
	public void stop() throws Exception {
		System.out.println(this.getClass().getSimpleName() + " stopping.");
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

}
