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
		
		// TODO: Note my use of the "function reference" notation from Java 8. As our verticles
		// get more complicated, we should implement the functions in the verticle class and pass
		// the function reference to the Vertx call.
		// For more details, see the Java 8 book and look up function reference.
		vertx.setPeriodic(hbPeriod, this::handleHeartbeat);
		
		vertx.eventBus().consumer(KernelChannels.HEARTBEAT, new Handler<Message<String>>(){

			@Override
			public void handle(Message<String> event) {
				
				// TODO: Need a more sophisticated neighbor data book keeping mechanism!
				String ip = event.body();
				System.out.println("...heartbeat received from " + ip);
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

	// See use as a function reference above.
	private final void handleHeartbeat(Long timerEvent){
		// TODO: send the hearbeat message
		vertx.eventBus().publish(KernelChannels.HEARTBEAT, ipAddr);
		System.out.println("Heartbeat sent...");
	}
	
	@Override
	public void stop() throws Exception {
		
		super.stop();
	}

}
