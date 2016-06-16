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
		
		



	}


	@Override
	public void stop() throws Exception {
		super.stop();
	}

}
