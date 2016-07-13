package edu.hsc.hypower.physicloud.hw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;

import edu.hsc.hypower.physicloud.KernelChannels;
import edu.hsc.hypower.physicloud.util.DataArray;
import edu.hsc.hypower.physicloud.util.DataTuple;
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

	private Long updateTimerId;

	public PhidgetInterfaceKitVerticle(String n, Map<Integer, String> aIn, Map<Integer, String> dIn, Map<Integer, String> dOut){
		verticleName = n;
		analogIn = aIn;
		digitalIn = dIn;
		digitalOut = dOut;
	}


	@Override
	public void start() throws Exception {
		super.start();
		try {
			ikit = new InterfaceKitPhidget();
			// TODO: need to handle HW attachment better!
			//			long startTime = System.currentTimeMillis();
			ikit.openAny();
//			ikit.addAttachListener(new AttachListener() {
//				public void attached(AttachEvent ae)	{
//					System.out.println("A new Phidget IKIT has been attached.");
//				}
//			});
			ikit.waitForAttachment();

		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		updateTimerId = vertx.setPeriodic(updatePeriod, this::updateSensorData);

	}

	public final void updateSensorData(Long l) {

		LocalMap<String, DataArray> ainDataMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.AIN);
		LocalMap<String, DataArray> dinDataMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.DIN);

		// As long as the keySet for each sub-device exists (not empty), then you place the data into a map
		// with the name:
		// name + "." + sub-device name
		for(int i = 0; i < analogIn.keySet().size(); i++)	{

			// We will deal with this later...for now just store the raw value.
			DataArray data;
			ArrayList<DataTuple> dList = new ArrayList<DataTuple>();
			
			try {
				DataTuple dTuple = new DataTuple(ikit.getSensorValue(i));
				dList.add(dTuple);
				data = new DataArray(dList);
				String sensorType = analogIn.get(new Integer(i));
				ainDataMap.put(sensorType + "." + Integer.toString(i), data);
				System.out.println(sensorType + "." + Integer.toString(i));
			} catch (PhidgetException pe) {
				pe.printStackTrace();
			}

		}

		//	Update Digital In Sensors


		for(int i = 0; i < digitalIn.keySet().size(); i++)	{
			
			DataArray data;
			ArrayList<DataTuple> dList = new ArrayList<DataTuple>();

			try {
				DataTuple dTuple = new DataTuple(ikit.getInputState(i));
				dList.add(dTuple);
				data = new DataArray(dList);
				String sensorType = digitalIn.get(new Integer(i));
				dinDataMap.put(sensorType + "." + Integer.toString(i), data);
			} catch (PhidgetException pe) {
				pe.printStackTrace();
			}
		}



	}




	@Override
	public void stop() throws Exception {
		vertx.cancelTimer(updateTimerId);
		ikit.close();
		super.stop();
	}



}
