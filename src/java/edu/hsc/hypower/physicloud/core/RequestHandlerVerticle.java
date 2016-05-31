package edu.hsc.hypower.physicloud.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
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

public class RequestHandlerVerticle extends AbstractVerticle {
	
	@Override
	public void start() throws Exception{
		super.start();
		
		//Event Bus 
		
		EventBus receiver = vertx.eventBus();
		
		receiver.consumer(KernelChannels.READ_REQUEST, message-> {
			
		
				

		//Create System Info/Hardware Abstraction Layer
		
		SystemInfo sysInfo = new SystemInfo();
		HardwareAbstractionLayer hardLayer = sysInfo.getHardware();
		
		//Store Ip Address of CPU
		
		String ipAddr = jsonRequest.getString(JsonFieldNames.IP_ADDR);
		String reqInfo = jsonRequest.getString(JsonFieldNames.REQ_RESOUR);
		
		//Create JSON to store IP address and requested resource
		
		JsonObject infoReply = new JsonObject();
		
		//Pull requested information and place into JSON file using switch statement
		
		switch(reqInfo){
		
		case "Available Memory": 
			infoReply.put("Requeaddresssted Value", hardLayer.getMemory().getAvailable());
			break;
			
		case "Physical Cores" :
			infoReply.put("Requested Value", hardLayer.getProcessor().getPhysicalProcessorCount());
			break;
			
		case "Logical Cores" : 
			infoReply.put("Requested Value", hardLayer.getProcessor().getLogicalProcessorCount());
			break;
			
		case "Processor Load" :
			infoReply.put("Requested Value", hardLayer.getProcessor().getSystemCpuLoad());
			break;
			
		default:
			infoReply.put("Requested Value", "No Value or Improper Request");
			break;
		
		}
		
	}}
		
	
		
		

		
		
		
		@Override
		public void stop() throws Exception {
			super.stop();
	}

}
