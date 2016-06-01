package edu.hsc.hypower.physicloud.core;

import edu.hsc.hypower.physicloud.KernelChannels;
import edu.hsc.hypower.physicloud.KernelMapNames;
import edu.hsc.hypower.physicloud.util.JsonFieldNames;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import io.vertx.core.Vertx;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.hsc.hypower.physicloud.*;
import edu.hsc.hypower.physicloud.util.NeighborData;

public class ResourceManagerVerticle extends AbstractVerticle {
	
	//Event Bus 

	EventBus receiver = vertx.eventBus();
	
	

	// TODO: Use the OSHI library to load the system properties.
	private final OperatingSystem os;
	private final CentralProcessor cp;
	
	private final long updatePeriod;
	private LocalMap<String,Object> resourceMap;

	
	receiver.consumer(systIn.get + "." + KernelChannels.READ_REQUEST, this::handleRequest);
	
	public ResourceManagerVerticle(long up){
		SystemInfo si = new SystemInfo();
		os = si.getOperatingSystem();
		cp = si.getHardware().getProcessor();
		updatePeriod = up;
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		resourceMap = vertx.sharedData().getLocalMap(KernelMapNames.RESOURCES);
		
		vertx.setPeriodic(updatePeriod, ResourceManagerVerticle::updateCyberResources);
	}
	
	private final void handleRequest(Message<JsonObject> msg){

		JsonObject request = msg.body();

		System.out.println("Request Receieved");

		//Create System Info/Hardware Abstraction Layer

		SystemInfo sysInfo = new SystemInfo();
		HardwareAbstractionLayer hardLayer = sysInfo.getHardware();

		//Store Ip Address of CPU

		String ipAddr = request.getString(JsonFieldNames.IP_ADDR);
		String reqInfo = request.getString("Requested Value");

		System.out.println("Requester IP Address:" + ipAddr + "\n" + "Requested Value: " + reqInfo);


		//Create JSON to store IP address and requested resource

		JsonObject infoReply = new JsonObject();

		infoReply.put(JsonFieldNames.IP_ADDR, ipAddr);

		//Pull requested information and place into JSON file using switch statement

		switch(reqInfo)
		{

		case JsonFieldNames.MEMORY: 
			infoReply.put("Requested Value", hardLayer.getMemory().getAvailable());
			break;

		case JsonFieldNames.P_CORES :
			infoReply.put("Requested Value", hardLayer.getProcessor().getPhysicalProcessorCount());
			break;

		case JsonFieldNames.L_CORES : 
			infoReply.put("Requested Value", hardLayer.getProcessor().getLogicalProcessorCount());
			break;

		case JsonFieldNames.LOAD :
			infoReply.put("Requested Value", hardLayer.getProcessor().getSystemCpuLoad());
			break;

		default:
			infoReply.put("Requested Value", "No Value or Improper Request");
			break;
		}

		msg.reply(infoReply);
		//This output statement is only valid for the test case of memory being the requested resource
		//We need to figure out a way to output various data types
		System.out.println("Value of Requested Resource: " + infoReply.getLong("Requested Value"));
		System.out.println("Reply Sent!");

	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
	}

	private static final void updateCyberResources(Long l){
		
	}
	
}
