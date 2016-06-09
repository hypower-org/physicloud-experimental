package edu.hsc.hypower.physicloud.hw;

import java.util.HashMap;
import java.util.Map;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;

import com.fasterxml.jackson.databind.JsonNode;

import edu.hsc.hypower.physicloud.KernelChannels;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;

public class PhidgetInterfaceKitVerticle extends AbstractVerticle {

	private final String verticleName;
	private final Long updatePeriod = 100l;
	
	private final Map<Integer, String> analogIn;
	private final Map<Integer, String> digitalIn;
	private final Map<Integer, String> digitalOut;
	
	private InterfaceKitPhidget ikit;

	public PhidgetInterfaceKitVerticle(String n, Map<Integer, String> aIn, Map<Integer, String> dIn, Map<Integer, String> dOut){
		verticleName = n;
		analogIn = aIn;
		digitalIn = dIn;
		digitalOut = dOut;
//		sensorData = null;
	}


	@Override
	public void start() throws Exception {
		super.start();
		try {
			ikit = new InterfaceKitPhidget();
			// TODO: need to handle HW attachment better!
			long startTime = System.currentTimeMillis();
			ikit.openAny();
			ikit.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new Phidget IKIT has been attached.");
				}
			});
			ikit.waitForAttachment();
			//	Create AttachListener Function instead of using waitForAttachment()
			//	Not sure if that would block the event loop but I do not want to find out
			// TODO: good point but we will need to error handle this. I added a handler to the deployment.
			// Eventually we want this verticle to throw an exception or signal vertx that it failed.
			// We should probably spawn waitForAttachment() with the executeBlocking API in vertx.
			
			long endTime   = System.currentTimeMillis();
			System.out.println(endTime - startTime);
			
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		vertx.setPeriodic(updatePeriod, this::updateSensorData);
		
	}

	public final void updateSensorData(Long l) {

		// TODO: The maps are built inside of this function. They will be accessed
		// by the vertx.sharedData() process. See how the HBVerticle does it.
		// Repeat for making a dinDataMap...but it is String to Boolean.
		LocalMap<String, Float> ainDataMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.AIN);
		LocalMap<String, Boolean> dinDataMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.DIN);
//		LocalMap<String, Boolean> douDataMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.DIN);
				
		// TODO: As long as the keySet for each sub-device exists (not empty), then you place the data into a map
		// with the name:
		// name + "." + sub-device name -- see my new PhidgetNames class that holds the AIN, DIN, DOU.
		// Then the HBVerticle will 
		
		for(int i = 0; i < analogIn.keySet().size(); i++)	{
			
			// We will deal with this later...for now just store the raw value.
			float data;
			try {
				data = ikit.getSensorValue(i);
				String sensorType = analogIn.get(new Integer(i));
//				sensorData.put(sensorType + '.' + Integer.toString(i), data);
				ainDataMap.put(sensorType + "." + Integer.toString(i), data);
			} catch (PhidgetException pe) {
				pe.printStackTrace();
			}

		}
		
		//	Update Digital In Sensors
		
		
		for(int i = 0; i < digitalIn.keySet().size(); i++)	{
			
			try {
				boolean data = ikit.getInputState(i);
				String sensorType = digitalIn.get(new Integer(i));
			} catch (PhidgetException pe) {
				pe.printStackTrace();
			}
		}
		


	}




	@Override
	public void stop() throws Exception {
		super.stop();
	}



}
