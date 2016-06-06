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

public class PhidgetInterfaceKitVerticle extends AbstractVerticle {

	private final String name;
	private Map<Integer, String> analogIn;
	private Map<Integer, String> digitalIn;
	private Map<Integer, String> digitalOut;
	public Map<String, Float> sensorData;
	private InterfaceKitPhidget ikit;


	public PhidgetInterfaceKitVerticle(String n, Map<Integer, String> aIn, Map<Integer, String> dIn, Map<Integer, String> dOut){
		name = n;
		analogIn = aIn;
		digitalIn = dIn;
		digitalOut = dOut;
		sensorData = null;

	}


	@Override
	public void start() throws Exception {
		super.start();
		ikit = new InterfaceKitPhidget();

		// Now that device is attached, collect data here.
		
//		try {
//			vertx.setPeriodic(500, this::updateSensorData);
//		} catch (PhidgetException e) {
//			e.printStackTrace();
//		}
		
		//	Begin to read sensor data
		ikit.openAny();
		//	Create AttachListener Function instead of using waitForAttachment()
		//	Not sure if that would block the event loop but I do not want to find out
		ikit.addAttachListener(new AttachListener() {
			public void attached(AttachEvent ae)	{
				System.out.println("A new Phidget IKIT has been attached.");


			}
		});
	}


	public final Map<String, Float> updateSensorData(Long l) throws PhidgetException	{

		//	Update Analog In Sensors
		
		for(int i = 0; i < analogIn.keySet().size(); i++)	{
			
			float data = ikit.getSensorValue(i);
			String sensorType = analogIn.get(new Integer(i));
			sensorData.put(sensorType + '.' + Integer.toString(i), data);

		}
		
		//	Update Digital In Sensors
		
		// TODO: We need a separate map for digital in's and out's. Should we make two separate maps, or should we try to make sensorData a template map?
		
		for(int i = 0; i < digitalIn.keySet().size(); i++)	{
			
			boolean data = ikit.getInputState(i);
			String sensorType = digitalIn.get(new Integer(i));
//			sensorData.put(sensorType + '.' + Integer.toString(i), data);

		}
		
		return sensorData;


	}




	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
	}



}
