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

public class RequestTestVerticle extends AbstractVerticle {
	
	@Override
	public void start() throws Exception {
		super.start();
		
		// TODO: We are not doing write requests yet. Do not need.
//		vertx.eventBus().consumer(KernelChannels.WRITE_REQUEST, handler) 
		
		// TODO: All this verticle should do is start a periodic function that sends a message to
		// the READ_REQUEST channel.
		
		// Here is a start...set a periodic task that sends a request every second (1000 ms).
		vertx.setPeriodic(1000, this::sendRequestMessage);
		
		private final void sendRequestMessage(Message<JsonObject> msg){
			
			
		}

		//Create a Json to hold the values of the IP Address and the requested resource
//		JsonObject jsonRequest = new JsonObject();
//		
//		jsonRequest.put(JsonFieldNames.IP_ADDR, value)
//		jsonRequest.put("Requested Value", value)
		
	}
	
	private final void sendRequestMessage(Long timerEvent){
		
		// Send the message here!
//		vertx.eventBus().publish(KernelChannels.READ_REQUEST, jsonRequest);
		
	}
	
}
