package edu.hsc.hypower.physicloud.hw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.GPSPositionChangeListener;

import edu.hsc.hypower.physicloud.KernelChannels;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;

public class PhidgetGPSVerticle extends AbstractVerticle {

	private final String verticleName;
	private final Long updatePeriod = 100l;

	private GPSPhidget gps;

	// I experimented with structuring the class similar to IKITVerticle, with Map<K,V> 
	// as private data members but I did not see the need for them

	public PhidgetGPSVerticle(String n){
		verticleName = n;
	}

	public void start() throws Exception {
		super.start();
		try {
			gps = new GPSPhidget();
			gps.openAny();
			gps.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new Phidget GPS has been attached.");
				}
			});
			gps.waitForAttachment();
			
		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		vertx.setPeriodic(updatePeriod, this::updateSensorData);	

	}


	public final void updateSensorData(Long l) {

		// Are these maps alright? or would we rather have a list for each sensor value?

		LocalMap<Double, Double> latLongMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.LAT_LONG);
		LocalMap<Double, Double> altVelMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.ALT_VEL);

		// This almost seems too simple, but because of the simple "getter" functions provided by the library I think it is all we need
		// Do we need to incorporate the GPSPositionChangeListener?
		

		GPSPositionChangeListener GPSPositionChangeListener = null;
		gps.addGPSPositionChangeListener(GPSPositionChangeListener);
		
		try {
			latLongMap.put(gps.getLongitude(), gps.getLatitude());
			altVelMap.put(gps.getAltitude(), gps.getVelocity());
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		gps.close();
		super.stop();
	}






}

