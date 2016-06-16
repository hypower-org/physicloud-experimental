package edu.hsc.hypower.physicloud.hw;

import io.vertx.core.AbstractVerticle;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;

public class ThreeByThreeVerticle {
	
	private ThreeByThreePhidget tkit;
	
	
	@Override
	public void start() throws Exception {
		super.start();
		try{
			tkit = new ThreeByThreePhidget();
			tkit.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new 3/3/3 Sensor has been attached.");
				}
		});
		tkit.waitForAttachment();
		
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
		
		vertx.setPeriodic(10000, this::update3x3);

}
	
	public final void update3x3(Long l){
		
			//Will update later
	}
	
	
	@Override
	public void stop() throws Exception {
		super.stop();
	}

}
