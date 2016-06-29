package edu.hsc.hypower.physicloud.core;

import edu.hsc.hypower.physicloud.KernelChannels;

import edu.hsc.hypower.physicloud.KernelMapNames;
import edu.hsc.hypower.physicloud.hw.PhidgetInterfaceKitVerticle;
import edu.hsc.hypower.physicloud.hw.PhidgetNames;
import edu.hsc.hypower.physicloud.util.JsonFieldNames;
import edu.hsc.hypower.physicloud.util.DataTuple;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hsc.hypower.physicloud.*;
import edu.hsc.hypower.physicloud.util.NeighborData;

/**
 * 
 * @author pmartin@hsc.edu
 *		   hackleyb18@hsc.edu	
 *		   kengask18@hsc.edu	
 */

public class ResourceManagerVerticle extends AbstractVerticle {

	private final JsonNode rootNode;

	private final long updatePeriod;
	private final String ipAddress;
	private final HashMap<String,Long> dataTransmitTimers;
	private String devReq = new String();
	private String sensReq = new String();
	
	// This hashmap is for the counter, but I am still thinking on how to use it
	private HashMap<String,Integer> deviceCounter;
	
	
	private final static long MIN_RESOURCE_UPDATE = 10; // 10 ms; in the future might need to be dynamic based on traffic.

	public ResourceManagerVerticle(String ipAddr, long up, JsonNode node){
		ipAddress = ipAddr;
		updatePeriod = up;
		rootNode = node;
		dataTransmitTimers = new HashMap<String,Long>();
	}

	@Override
	public void start() throws Exception {
		super.start();

		int hwCount = 0;
		LocalMap<Integer, String> deviceMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);

		//	START PARSING
		Iterator<String> deviceFields = rootNode.fieldNames();
		while(deviceFields.hasNext())
		{
			String deviceKey = deviceFields.next();		
			if(deviceKey.indexOf('.') != -1)
			{
				String deviceName = deviceKey.substring(deviceKey.indexOf('.')+1);	

				//	IKIT SECTION. CREATE THREE MAPS
				if(deviceName.equals(PhidgetNames.PHIDGET_IKIT))
				{
					JsonNode deviceNode = rootNode.get(deviceKey);
					Map<Integer, String> ikitAnIn = new HashMap<Integer, String>();
					boolean hasAin = false;
					Map<Integer, String> ikitDIn = new HashMap<Integer, String>();
					boolean hasDin = false;
					Map<Integer, String> ikitDOut = new HashMap<Integer, String>();
					boolean hasDou = false;

					Iterator<String> sensorFields = deviceNode.fieldNames();

					while(sensorFields.hasNext())
					{
						// This section of code parses the internals of the PhidgetIKit details.
						String ioKey = sensorFields.next();

						// ANALOG INPUT 
						String[] inputTypes = ioKey.split("[.]");
						String part1 = inputTypes[0];

						if(part1.equals(PhidgetNames.AIN))
						{
							String sensorType = deviceNode.get(ioKey).asText();
							Integer locNum = Integer.parseInt(ioKey.substring(4,5));
							ikitAnIn.put(locNum, sensorType);
							hasAin = true;
						}

						//	DIGITAL INPUT

						if(part1.equals(PhidgetNames.DIN))
						{
							String sensorType = deviceNode.get(ioKey).asText();
							Integer locNum = Integer.parseInt(ioKey.substring(4,5));
							ikitDIn.put(locNum, sensorType);
							hasDin = true;
						}

						//	DIGITAL OUTPUT

						if(part1.equals(PhidgetNames.DOU))
						{
							String sensorType = deviceNode.get(ioKey).asText();
							Integer locNum = Integer.parseInt(ioKey.substring(4,5));
							ikitDOut.put(locNum, sensorType);
							hasDou = true;
						}
					}

					// Kind of hacky here...but we are boxed in by our json format.
					String phidgetIKITName = PhidgetNames.PHIDGET_IKIT + Integer.toString(hwCount);
					int deviceCount = 0; 
					if(hasAin){
						deviceMap.put(deviceCount, PhidgetNames.PHIDGET_IKIT + Integer.toString(hwCount) + "." + PhidgetNames.AIN);
						deviceCount++;
					}
					if(hasDin){
						deviceMap.put(deviceCount, PhidgetNames.PHIDGET_IKIT + Integer.toString(hwCount) + "." + PhidgetNames.DIN);
						deviceCount++;
					}
					if(hasDou){
						deviceMap.put(deviceCount, PhidgetNames.PHIDGET_IKIT + Integer.toString(hwCount) + "." + PhidgetNames.DOU);
						deviceCount++;
					}

					// Deploy PhidgetIKitVerticle
					vertx.deployVerticle(new PhidgetInterfaceKitVerticle(phidgetIKITName, ikitAnIn, ikitDIn, ikitDOut), 
							new DeploymentOptions().setWorker(true),
							new Handler<AsyncResult<String>>(){
						@Override
						public void handle(AsyncResult<String> res) {
							if(!res.succeeded()){
								System.err.println("Error deploying PhidgetInterfaceKitVerticle! TODO: Handle this issue.");
							}
							else{
								System.out.println("PhidgetInterfaceKitVerticle deployed: " + res.result());
							}
						}
					});
					hwCount++;

				}

				if(deviceName.equals(PhidgetNames.PHIDGET_GPS))
				{
					//TODO: EXPAND THIS FUNCTIONALITY 
					// specify for phidget gps
				}
				
				if(deviceName.equals(PhidgetNames.PHIDGET_RFID))
				{
					//TODO: EXPAND THIS FUNCTIONALITY 
					// specify for phidget RFID
				}

			}
		}
		
		vertx.eventBus().consumer(ipAddress + "." + KernelChannels.RESOURCE_QUERY, this::handleReadRequest);
	}
	
	//Returns if a resource is a available
	
	private final void handleResourceQuery(Message<JsonObject> readReqMsg){
		JsonObject request = readReqMsg.body();
		String ipAddr = request.getString(JsonFieldNames.IP_ADDR);
		String reqInfo = request.getString("Requested Resource");
		
		System.out.println("Asking for " + reqInfo);
		
		LocalMap<Integer, String> deviceMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);		
		JsonObject readResReply = new JsonObject();	
		readResReply.put(JsonFieldNames.IP_ADDR, ipAddr);
		System.out.println("Requester IP Address:" + ipAddr + "\n" + "Requested Value: " + reqInfo);
		
		ArrayList<String> deviceNames = new ArrayList<String>(deviceMap.values());
		
		outerloop:
		for(String deviceName : deviceNames){

			for(Object key : vertx.sharedData().getLocalMap(deviceName).keySet()){
				
				if(((String) key).compareTo(reqInfo) == 0){
					readResReply.put("isAllowed", true);
					break outerloop;
				}
			}
		}
		
		if(!readResReply.containsKey("isAllowed"))
			readResReply.put("isAllowed", false);
	
		System.out.println(readResReply.encodePrettily());
		readReqMsg.reply(readResReply);
	}
	
	//Replies with availability of resource and channel name of periodic
	//that is created, sending data of the requested resource
	
	private final void handleReadRequest(Message<JsonObject> readReqMsg){
		
		JsonObject request = readReqMsg.body();
		String ipAddr = request.getString(JsonFieldNames.IP_ADDR);
		String reqInfo = request.getString("Requested Resource");
		Integer updateTime = request.getInteger(JsonFieldNames.UPDATE_TIME);
		
		System.out.println("Asking for " + reqInfo);
		
		LocalMap<Integer, String> deviceMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);		
		JsonObject readResReply = new JsonObject();
		System.out.println("Requester IP Address:" + ipAddr + "\n" + "Requested Value: " + reqInfo);
		
		ArrayList<String> deviceNames = new ArrayList<String>(deviceMap.values());
		
		outerloop:
		for(String deviceName : deviceNames){

			for(Object key : vertx.sharedData().getLocalMap(deviceName).keySet()){
				
				if(((String) key).compareTo(reqInfo) == 0){
					readResReply.put("isAllowed", true);
					// TODO: need a counter to keep track of the number of resource channels open for this device.
					// Cache the selected device name for use in the data transmission later...
					devReq = deviceName;
					sensReq = (String) key;
					readResReply.put("channelName", reqInfo + "@." + ipAddr);
					break outerloop;
				}
			}
			
		}
		if(!readResReply.containsKey("isAllowed"))
			readResReply.put("isAllowed", false);

	
		System.out.println(readResReply.encodePrettily());
		
		readReqMsg.reply(readResReply);

		// TODO: This timer needs to be stored for later cancellation.
		// See my new HashMap above! We will store the name of the resource channel and associate a timer with it.
		
		// TODO: Make it anonymous so that the correct resource is read. Look at the data held in reqInfo above.
		long timerId = vertx.setPeriodic(updatePeriod, new Handler<Long>(){
			@Override
			public void handle(Long event) {
				
				DataTuple message = new DataTuple(vertx.sharedData().getLocalMap(devReq).get(sensReq));
				vertx.eventBus().publish(reqInfo + "@." + ipAddress, message);
			}
		});
		dataTransmitTimers.put(readResReply.getString("channelName"), timerId);
		// TODO: At some point, we will need to handle the removal of the data transmission.
		
	}
	
	private final void stopDataTransmission(){
	
	}

	

	@Override
	public void stop() throws Exception {
		System.out.println(this.getClass().getSimpleName() + " stopping.");
		super.stop();
	}

}
