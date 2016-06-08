package edu.hsc.hypower.physicloud.core;

import edu.hsc.hypower.physicloud.KernelChannels;

import edu.hsc.hypower.physicloud.KernelMapNames;
import edu.hsc.hypower.physicloud.hw.PhidgetInterfaceKitVerticle;
import edu.hsc.hypower.physicloud.hw.PhidgetNames;
import edu.hsc.hypower.physicloud.util.JsonFieldNames;

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

	//	private LocalMap<String,Object> resourceMap;

	public ResourceManagerVerticle(long up, JsonNode node){
		updatePeriod = up;
		rootNode = node;
	}

	@Override
	public void start() throws Exception {
		super.start();

		// TODO: will need another way (non-oshi or jhardware) - not a priority right now
		//		resourceMap = vertx.sharedData().getLocalMap(KernelMapNames.RESOURCES);
		//		vertx.setPeriodic(updatePeriod, ResourceManagerVerticle::updateCyberResources);

		int devCount = 0;
		LocalMap<String, Buffer> deviceMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);

		//	START PARSING
		Iterator<String> it = rootNode.fieldNames();
		while(it.hasNext())
		{
			String deviceKey = it.next();		
			if(deviceKey.indexOf('.') != -1)
			{
				//	TRAVERSAL IS NOW AT THE DEVICE NAME

				String devName = deviceKey.substring(deviceKey.indexOf('.')+1);	

				//	IKIT SECTION. CREATE THREE MAPS

				if(devName.equals(PhidgetNames.PHIDGET_IKIT))
				{
					devCount++;
					String delims = ".";
					JsonNode tempNode = rootNode.get(deviceKey);
					Map<Integer, String> ikitAnIn = new HashMap<Integer, String>();
					Map<Integer, String> ikitDIn = new HashMap<Integer, String>();
					Map<Integer, String> ikitDOut = new HashMap<Integer, String>();

					Iterator<String> internalIt = tempNode.fieldNames();

					while(internalIt.hasNext())
					{

						String ioKey = internalIt.next();

						// ANALOG INPUT 

						String[] inputTypes = ioKey.split("[.]");
						String part1 = inputTypes[0];

						if(part1.equals(PhidgetNames.AIN))
						{
							String sensorType = tempNode.get(ioKey).asText();
							Integer locNum = Integer.parseInt(ioKey.substring(4,5));
							ikitAnIn.put(locNum, sensorType);
							System.out.println(locNum + " " + sensorType);

						}

						//	DIGITAL INPUT

						if(part1.equals(PhidgetNames.DIN))
						{
							String sensorType = tempNode.get(ioKey).asText();
							Integer locNum = Integer.parseInt(ioKey.substring(4,5));
							ikitDIn.put(locNum, sensorType);	
							System.out.println(locNum + " " + sensorType);

						}

						//	DIGITAL OUTPUT

						if(part1.equals(PhidgetNames.DOU))
						{
							String sensorType = tempNode.get(ioKey).asText();
							Integer locNum = Integer.parseInt(ioKey.substring(4,5));
							ikitDOut.put(locNum, sensorType);
							System.out.println(locNum + " " + sensorType);
						}
					}


					// Publish device name
					String phidgetIKITName = PhidgetNames.PHIDGET_IKIT + Integer.toString(devCount);
<<<<<<< HEAD
					
					// TODO: put this device name into the deviceMap...
					Buffer phidgetBuffer = null;
					phidgetBuffer.appendString(phidgetIKITName);
					deviceMap.put("devices", phidgetBuffer);
=======
					//					Buffer phidgetBuffer = null;
					//					phidgetBuffer.appendString(phidgetIKITName);
					// TODO: Following the Vertx example online for shared data with Buffers:
					deviceMap.put("devices", Buffer.buffer().appendString(phidgetIKITName));
>>>>>>> 74803bdce6a7cf0f51fcd225afd2b3bb2efa0e34

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
				}

				if(devName.equals(PhidgetNames.PHIDGET_GPS))
				{
					// specify for phidget gps
				}

				//	Add devices to the "available resources map"...
				

				// TODO: As I noted in OneNote, this should be communication via a local map, not the event bus.
				// If it is on the event bus, everyone can get it!

			}
		} 
	}


	private final void handleRequest(Message<JsonObject> msg){

		JsonObject request = msg.body();

		System.out.println("Request Receieved");

		//Store Ip Address of CPU
		String ipAddr = request.getString(JsonFieldNames.IP_ADDR);
		String reqInfo = request.getString("Requested Value");

		System.out.println("Requester IP Address:" + ipAddr + "\n" + "Requested Value: " + reqInfo);


		//Create JSON to store IP address and requested resource

		JsonObject infoReply = new JsonObject();


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

}
