package edu.hsc.hypower.physicloud.hw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;


import edu.hsc.hypower.physicloud.KernelChannels;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;

public class PhidgetGPSVerticle extends AbstractVerticle {

	private final String verticleName;
	private final Long updatePeriod = 100l;

	private final ArrayList<Double> latitude;
	private final ArrayList<Double> longitude;
	private final ArrayList<Double> altitude;
	private final ArrayList<Double> velocity;

	private GPSPhidget gps;

	public PhidgetGPSVerticle(String n, ArrayList<Double> latIn,ArrayList<Double> longIn, ArrayList<Double> altIn, ArrayList<Double> velIn){
		verticleName = n;
		latitude = latIn;
		longitude = longIn;
		altitude = altIn;
		velocity = velIn;
	}

	public void start() throws Exception {
		super.start();
		try {
			gps = new GPSPhidget();
			// TODO: need to handle HW attachment better!
			//		long startTime = System.currentTimeMillis();
			gps.openAny();
			gps.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new Phidget GPS has been attached.");
				}
			});
			gps.waitForAttachment();
			//		long endTime   = System.currentTimeMillis();
			//		System.out.println(endTime - startTime);

		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		vertx.setPeriodic(updatePeriod, this::updateSensorData);	

	}


	public final void updateSensorData(Long l) {

		LocalMap<Double, Double> latLongMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.LAT_LONG);
		LocalMap<Double, Double> altVelMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.ALT_VEL);
		
		try {
			latLongMap.put(gps.getLongitude(), gps.getLatitude());
			altVelMap.put(gps.getAltitude(), gps.getVelocity());
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}






}

