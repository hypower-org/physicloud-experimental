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

public class PhidgetSpatialVerticle extends AbstractVerticle{


	private final String verticleName;
	private final Long updatePeriod = 100l;
	private SpatialPhidget sp;	

	public PhidgetSpatialVerticle(String n){
		verticleName = n;
	}

	@Override
	public void start() throws Exception {
		super.start();
		try{
			sp = new SpatialPhidget();
			sp.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new 3/3/3 Sensor has been attached.");
				}
			});
			sp.waitForAttachment();

		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		vertx.setPeriodic(10000, this::updateSensorData);

	}

	public final void updateSensorData(Long l){

		try {
			LocalMap<Integer, Double> accelerationMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.ACCELERATION);
			LocalMap<Integer, Double> gyroMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.ACCELERATION);

			int gyroAxisCount = sp.getGyroAxisCount();
			int accelerationAxis = sp.getAccelerationAxisCount();

			for(int i = 0; i < accelerationAxis; i++)
			{				
				accelerationMap.put((Integer) i, sp.getAcceleration(i));
			}

			for(int i = 0; i < gyroAxisCount; i++)
			{
				gyroMap.put((Integer) i, sp.getAngularRate(i)); 
			}
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void stop() throws Exception {
		sp.close();
		super.stop();
	}

}
