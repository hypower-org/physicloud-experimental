package edu.hsc.hypower.physicloud.hw;

import io.vertx.core.AbstractVerticle;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;

public class PhidgetRFIDVerticle extends AbstractVerticle {
	
	private RFIDPhidget rkit;
	
	
	@Override
	public void start() throws Exception {
		super.start();
		try{
			rkit = new RFIDPhidget();
			rkit.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new RFID Reader has been attached.");
				}
		});
		rkit.waitForAttachment();
		
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
		
		vertx.setPeriodic(10000, this::updateRFID);

}
	
	public final void updateRFID(Long l){
		
		//Still need to add way to read information from RFID reader
	}
	
	
	@Override
	public void stop() throws Exception {
		super.stop();
	}

	
}
