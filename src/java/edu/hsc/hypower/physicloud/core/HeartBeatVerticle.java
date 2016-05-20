package edu.hsc.hypower.physicloud.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;

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
		
		vertx.setPeriodic(hbPeriod, new Handler<Long>(){
			@Override
			public void handle(Long event) {
				// TODO: send the hearbeat message
				vertx.eventBus().publish(KernelChannels.HEARTBEAT, ipAddr);
			}
		});
		
		vertx.eventBus().consumer(KernelChannels.HEARTBEAT, new Handler<Message<String>>(){

			@Override
			public void handle(Message<String> event) {
				
				// TODO: Need a more sophisticated neighbor data book keeping mechanism!
				String ip = event.body();
				if(ip != ipAddr){
					LocalMap<String,NeighborData> neighborMap = vertx.sharedData()
																.getLocalMap(KernelMapNames.NEIGHBORS);
					neighborMap.put(ip, new NeighborData(ip));
					// Update the last update time from this neighbor.
					neighborUpdateTimes.put(ip, System.currentTimeMillis());
				}
			}
			
		});
		
		
		
	}

	@Override
	public void stop() throws Exception {
		
		super.stop();
	}

}
