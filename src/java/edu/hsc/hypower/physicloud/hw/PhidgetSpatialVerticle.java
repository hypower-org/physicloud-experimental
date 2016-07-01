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
import edu.hsc.hypower.physicloud.util.DataTuple;
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
			
			// To further add functionality at some point we should verify that the values we collect are in the correct range
			// TODO: Use getAccelerationMax() etc to check 
			
			int gAxisCount = sp.getGyroAxisCount();
			int accelerationAxis = sp.getAccelerationAxisCount();
			ArrayList<DataTuple> dataArr = new ArrayList<DataTuple>();

			for(int i = 0; i < accelerationAxis; i++)
			{				
				dataArr.add(new DataTuple(new Float(sp.getAcceleration(i))));
			}

			for(int i = 0; i < gAxisCount; i++)
			{
				dataArr.add(new DataTuple(new Float(sp.getAcceleration(i))));
			}
			
			spatialMap.put(verticleName + "." + count, new DataArray(dataArr));
			
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
