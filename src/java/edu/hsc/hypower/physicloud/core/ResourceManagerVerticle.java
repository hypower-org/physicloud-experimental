package edu.hsc.hypower.physicloud.core;

import edu.hsc.hypower.physicloud.KernelChannels;

import edu.hsc.hypower.physicloud.KernelMapNames;
import edu.hsc.hypower.physicloud.hw.PhidgetGPSVerticle;
import edu.hsc.hypower.physicloud.hw.PhidgetInterfaceKitVerticle;
import edu.hsc.hypower.physicloud.hw.PhidgetNames;
import edu.hsc.hypower.physicloud.hw.PhidgetRFIDVerticle;
import edu.hsc.hypower.physicloud.hw.PhidgetSpatialVerticle;
import edu.hsc.hypower.physicloud.util.JsonFieldNames;
import edu.hsc.hypower.physicloud.util.DataArray;
import edu.hsc.hypower.physicloud.util.DataMessage;
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
 *		   kangask18@hsc.edu	
 */

public class ResourceManagerVerticle extends AbstractVerticle {

	private static final String NO_DEVICE = "NO_DEVICE";

	private final JsonNode rootNode;

	private final long updatePeriod;
	private final String ipAddress;
	private final HashMap<String,Long> dataTransmitTimers;

	// This hashmap is for the counter, but I am still thinking on how to use it
	private HashMap<String,Integer> deviceCounter = new HashMap<String, Integer>();


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
				System.out.println(deviceName);

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
						deviceMap.put(deviceCount, phidgetIKITName + "." + PhidgetNames.AIN);
						deviceCount++;
					}
					if(hasDin){
						deviceMap.put(deviceCount, phidgetIKITName + "." + PhidgetNames.DIN);
						deviceCount++;
					}
					if(hasDou){
						deviceMap.put(deviceCount, phidgetIKITName + "." + PhidgetNames.DOU);
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

					int deviceCount = 0;
					String gpsStringName = PhidgetNames.PHIDGET_GPS + "." + Integer.toString(deviceCount);
					deviceMap.put(deviceCount, gpsStringName);
					deviceCount++;

					vertx.deployVerticle(new PhidgetGPSVerticle(gpsStringName),
							new DeploymentOptions().setWorker(true), 
							new Handler<AsyncResult<String>>() {

						@Override
						public void handle(AsyncResult<String> res) {
							// TODO Auto-generated method stub

							if(!res.succeeded()){
								System.err.println("Error deploying PhidgetGpsVerticle! TODO: Handle this issue.");
							}
							else{
								System.out.println("PhidgetGPSVerticle deployed: " + res.result());
							}

						}
					});

				}

				if(deviceName.equals(PhidgetNames.PHIDGET_RFID))
				{
					int deviceCount = 0;
					String rfidStringName = PhidgetNames.PHIDGET_RFID + "." + Integer.toString(deviceCount);
					deviceMap.put(deviceCount, rfidStringName);
					deviceCount++;

					vertx.deployVerticle(new PhidgetRFIDVerticle(rfidStringName), 
							new DeploymentOptions().setWorker(true), 
							new Handler<AsyncResult<String>>(){

						@Override
						public void handle(AsyncResult<String> res) {
							if(!res.succeeded()){
								System.err.println("Error deploying PhidgetRFIDVerticle! TODO: Handle this issue.");
							}
							else{
								System.out.println("PhidgetRFIDVerticle deployed: " + res.result());
							}

						}

					});
				}

				if(deviceName.equals(PhidgetNames.PHIDGET_SPATIAL)){
					//TODO: EXPAND THIS FUNCTIONALITY 
					// specify for phidget Spatial

					int deviceCount = 0;
					String spatialStringName = PhidgetNames.PHIDGET_SPATIAL + "." + Integer.toString(deviceCount);
					deviceMap.put(deviceCount, spatialStringName);
					deviceCount++;

					vertx.deployVerticle(new PhidgetSpatialVerticle(spatialStringName), 
							new DeploymentOptions().setWorker(true), 
							new Handler<AsyncResult<String>>(){

						@Override
						public void handle(AsyncResult<String> res) {
							if(!res.succeeded()){
								System.err.println("Error deploying PhidgetSpatialVerticle! TODO: Handle this issue.");
							}
							else{
								System.out.println("PhidgetSpatialVerticle deployed: " + res.result());
							}

						}

					});
				}

			}
		}
		vertx.eventBus().consumer(ipAddress + "." + KernelChannels.RESOURCE_QUERY, this::handleResourceQuery);
		vertx.eventBus().consumer(ipAddress + "." + KernelChannels.READ_REQUEST, this::handleReadRequest);
		vertx.eventBus().consumer(ipAddress + "." + KernelChannels.RESOURCE_UNSUB, this::handleUnsubscribe);
	}

	//Returns if a resource is a available

	private final void handleResourceQuery(Message<JsonObject> readReqMsg){
		JsonObject request = readReqMsg.body();
		String ipAddr = request.getString(JsonFieldNames.IP_ADDR);
		String reqResourceName = request.getString(JsonFieldNames.REQ_RES);

		System.out.println("Asking for " + reqResourceName);

		LocalMap<Integer, String> deviceMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);		
		JsonObject readResReply = new JsonObject();	
		readResReply.put(JsonFieldNames.IP_ADDR, ipAddr);
		System.out.println("Requester IP Address:" + ipAddr + "\n" + "Requested Value: " + reqResourceName);

		ArrayList<String> deviceNames = new ArrayList<String>(deviceMap.values());
		final String deviceName = checkResourceAvailability(reqResourceName, deviceNames); 
		if(deviceName.compareTo(ResourceManagerVerticle.NO_DEVICE) != 0){
			readResReply.put("isAllowed", true);
		}
		else{
			readResReply.put("isAllowed", false);
		}
		System.out.println(readResReply.encodePrettily());
		readReqMsg.reply(readResReply);
	}

	//Replies with availability of resource and channel name of periodic
	//that is created, sending data of the requested resource

	private final void handleReadRequest(Message<JsonObject> readReqMsg){

		JsonObject request = readReqMsg.body();
		String requestingIpAddr = request.getString(JsonFieldNames.IP_ADDR);
		final String reqResourceName = request.getString(JsonFieldNames.REQ_RES);
		Integer resourceUpdatePeriod = request.getInteger(JsonFieldNames.UPDATE_TIME);

		System.out.println("Asking for " + reqResourceName);

		LocalMap<Integer, String> deviceMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);		
		JsonObject readResReply = new JsonObject();
		System.out.println("Requester IP Address:" + requestingIpAddr + "\n" + "Requested Value: " + reqResourceName);

		ArrayList<String> deviceNames = new ArrayList<String>(deviceMap.values());

		// Call device check...
		final String deviceName = checkResourceAvailability(reqResourceName, deviceNames); 
		Integer i = 0;
		if(deviceName.compareTo(ResourceManagerVerticle.NO_DEVICE) != 0){
			// Do all of the wonderful resource subscription logic!
			readResReply.put("isAllowed", true);
			// TODO: Tweak counter 
			i++;
			deviceCounter.put(deviceName, i);
			// Cache the selected device name for use in the data transmission later...
			readResReply.put(JsonFieldNames.CHANNEL_NAME, reqResourceName + "@" + ipAddress);
			System.out.print("Creating channel: " + reqResourceName + "@" + ipAddress);
			long timerId = MIN_RESOURCE_UPDATE;
			if(resourceUpdatePeriod >= MIN_RESOURCE_UPDATE)
			{
				timerId = vertx.setPeriodic(resourceUpdatePeriod, new Handler<Long>(){
					@Override
					public void handle(Long event) {
						LocalMap<String, DataArray> dataMap = vertx.sharedData().getLocalMap(deviceName);
						DataMessage message = new DataMessage(deviceName, dataMap.get(reqResourceName).getDataTuples());
						vertx.eventBus().publish(reqResourceName + "@" + ipAddress, message);
					}
				});

				dataTransmitTimers.put(readResReply.getString(JsonFieldNames.CHANNEL_NAME), timerId);
			}

			// TODO: At some point, we will need to handle the removal of the data transmission.

		}
		else{
			System.err.println(reqResourceName + " not found!");
			readResReply.put("isAllowed", false);
		}

		System.out.println(readResReply.encodePrettily());

		readReqMsg.reply(readResReply);

	}

	private final void handleUnsubscribe(Message<JsonObject> readReqMsg){
		Long chanID = dataTransmitTimers.remove(readReqMsg.body().getValue(JsonFieldNames.UNSUB));
		vertx.cancelTimer(chanID);

	}



	@Override
	public void stop() throws Exception {
		System.out.println(this.getClass().getSimpleName() + " stopping.");
		super.stop();
	}

	private final String checkResourceAvailability(final String reqResourceName, final ArrayList<String> deviceNames){
		String deviceName = ResourceManagerVerticle.NO_DEVICE;
		foundDevice:
			for(String devName : deviceNames){
				Set<Object> resourceKeys = vertx.sharedData().getLocalMap(devName).keySet();
				for(Object resourceKey : resourceKeys){
					if(((String) resourceKey).compareTo(reqResourceName) == 0){
						deviceName = devName;
						break foundDevice;
					}
				}
			}
		return deviceName;
	}

}
