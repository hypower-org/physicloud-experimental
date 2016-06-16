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

public class ThreeByThreeVerticle extends AbstractVerticle{


	private final String verticleName;
	private final Long updatePeriod = 100l;
	private SpatialPhidget tkit;	

	public ThreeByThreeVerticle(String n){
		verticleName = n;
	}

	@Override
	public void start() throws Exception {
		super.start();
		try{
			tkit = new SpatialPhidget();
			tkit.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new 3/3/3 Sensor has been attached.");
				}
			});
			tkit.waitForAttachment();

		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		vertx.setPeriodic(10000, this::updateSensorData);

	}

	public final void updateSensorData(Long l){

		try {
			LocalMap<Integer, Double> accelerationMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.ACCELERATION);
			LocalMap<Integer, Double> gyroMap = vertx.sharedData().getLocalMap(verticleName + "." + PhidgetNames.ACCELERATION);

			int gyroAxisCount = tkit.getGyroAxisCount();
			int accelerationAxis = tkit.getAccelerationAxisCount();

			for(int i = 0; i < accelerationAxis; i++)
			{				
				accelerationMap.put((Integer) i, tkit.getAcceleration(i));
			}

			for(int i = 0; i < gyroAxisCount; i++)
			{
				gyroMap.put((Integer) i, tkit.getAngularRate(i)); 
			}
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void stop() throws Exception {
		super.stop();
	}

}
