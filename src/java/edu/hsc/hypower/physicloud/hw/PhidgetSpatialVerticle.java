package edu.hsc.hypower.physicloud.hw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.GPSPositionChangeListener;

import edu.hsc.hypower.physicloud.KernelChannels;
import edu.hsc.hypower.physicloud.util.DataArray;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;

public class PhidgetSpatialVerticle extends AbstractVerticle{


	private final String verticleName;
	private final Long updatePeriod = 100l;
	private SpatialPhidget sp;	
	private int count; 

	public PhidgetSpatialVerticle(String n){
		verticleName = n;
		count = 0; 
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
			count++;
				
		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		vertx.setPeriodic(10000, this::updateSensorData);

	}

	public final void updateSensorData(Long l){

		try {
			LocalMap<String, DataArray> spatialMap = vertx.sharedData().getLocalMap(verticleName + "." + count);

			int gAxisCount = sp.getGyroAxisCount();
			int accelerationAxis = sp.getAccelerationAxisCount();

//			for(int i = 0; i < accelerationAxis; i++)
//			{				
//				accelerationMap.put((Integer) i, sp.getAcceleration(i));
//			}
//
//			for(int i = 0; i < gAxisCount; i++)
//			{
//				gyroMap.put((Integer) i, sp.getAngularRate(i)); 
//			}
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
