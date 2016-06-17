package edu.hsc.hypower.physicloud.hw;

import io.vertx.core.AbstractVerticle;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.TagGainEvent;
import com.phidgets.event.TagGainListener;
import com.phidgets.event.TagLossEvent;
import com.phidgets.event.TagLossListener;

public class PhidgetRFIDVerticle extends AbstractVerticle {
	
	private RFIDPhidget rfid;
	
	
	@Override
	public void start() throws Exception {
		super.start();
		try{
			rfid = new RFIDPhidget();
			rfid.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new RFID Reader has been attached.");
				}
		});
		rfid.waitForAttachment();
		
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
		
		rfid.addTagGainListener(new TagGainListener()
		{
			public void tagGained(TagGainEvent oe)
			{
				if(oe.getValue() == "1000e0a20a")//This is the ID of one of the white cards 
				{
					System.out.println("Menu Unlocked");
				}
				else
					System.out.println("Improper RFID Tag");
		
			}
		});
		
//		rfid.addTagLossListener(new TagLossListener()
//		{
//			public void tagLost(TagLossEvent oe)
//			{
//				System.out.println(oe);
//			}
//		});
		

}
	

	
	@Override
	public void stop() throws Exception {
		rfid.close();
		super.stop();
	}

	
}
