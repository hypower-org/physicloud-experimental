package edu.hsc.hypower.physicloud.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;

import java.util.Map;
import java.util.Set;

import edu.hsc.hypower.physicloud.*;

/**
 * 
 * @author pmartin@hsc.edu
 *
 */
public class HeartBeatVerticle extends AbstractVerticle {

	private final String ipAddr;
	private final Long hbPeriod;
	
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
				
				LocalMap<String,Object> configData = vertx.sharedData().getLocalMap(KernelMapNames.CONFIG);
				JsonObject configObj = new JsonObject();
				for(String keys : configData.keySet()){
					
				}
				
			}
		});
		
	}

	@Override
	public void stop() throws Exception {
		
		super.stop();
	}

}
