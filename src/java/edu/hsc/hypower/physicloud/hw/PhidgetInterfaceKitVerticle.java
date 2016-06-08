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
			//	Begin to read sensor data
			ikit.openAny();
			//	Create AttachListener Function instead of using waitForAttachment()
			//	Not sure if that would block the event loop but I do not want to find out
			ikit.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new Phidget IKIT has been attached.");
				}
			});
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		vertx.setPeriodic(500, this::updateSensorData);
		
	}

	public final void updateSensorData(Long l) throws PhidgetException	{

		// TODO: The maps are built inside of this function. They will be accessed
		// by the vertx.sharedData() process. See how the HBVerticle does it.
		// Repeat for making a dinDataMap...but it is String to Boolean.
		LocalMap<String, Float> ainDataMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.AIN);
		// The above line just made the LocalMap called "PhidgetIKitX.ain" where X is the number of the device.
		
		// TODO: As long as the keySet for each sub-device exists (not empty), then you place the data into a map
		// with the name:
		// name + "." + sub-device name -- see my new PhidgetNames class that holds the AIN, DIN, DOU.
		// Then the HBVerticle will 
		
		//	Update Analog In Sensors
		for(int i = 0; i < analogIn.keySet().size(); i++)	{
			
			
			
			// TODO: This is fine for now. We will need to have the conversion factors in here for each type of sensor.
			// We will deal with this later...for now just store the raw value.
			float data = ikit.getSensorValue(i);
			String sensorType = analogIn.get(new Integer(i));
//			sensorData.put(sensorType + '.' + Integer.toString(i), data);

		}
		
		//	Update Digital In Sensors
		
		// TODO: We need a separate map for digital in's and out's. Should we make two separate maps, or should we try to make sensorData a template map?
		
		for(int i = 0; i < digitalIn.keySet().size(); i++)	{
			
			boolean data = ikit.getInputState(i);
			String sensorType = digitalIn.get(new Integer(i));
//			sensorData.put(sensorType + '.' + Integer.toString(i), data);

		}
		
//		return sensorData;


	}




	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
	}



}
