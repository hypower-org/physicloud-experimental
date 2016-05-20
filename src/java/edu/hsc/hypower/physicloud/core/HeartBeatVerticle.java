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

/**
 * 
 * @author pmartin@hsc.edu
 *
 */
public class HeartBeatVerticle extends AbstractVerticle {

	private final static long NEIGHBOR_TIMEOUT = 5000;
	
	private final String ipAddr;
	private final Long hbPeriod;
	
	private HashMap<String,Long> neighborUpdateTimes;
	
	public HeartBeatVerticle(String ip, Long hbp){
		ipAddr = ip;
		hbPeriod = hbp;
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
				// TODO: receive the heartbeat message
			}
			
		});
		
		
		
	}

	@Override
	public void stop() throws Exception {
		
		super.stop();
	}

}
