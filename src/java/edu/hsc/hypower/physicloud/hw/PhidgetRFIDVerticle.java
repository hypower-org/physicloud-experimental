package edu.hsc.hypower.physicloud.hw;

import io.vertx.core.AbstractVerticle;

import java.util.ArrayList;

import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.TagGainEvent;
import com.phidgets.event.TagGainListener;
import com.phidgets.event.TagLossEvent;
import com.phidgets.event.TagLossListener;

public class PhidgetRFIDVerticle extends AbstractVerticle {

	private RFIDPhidget rfid;
	private ArrayList<String> tagList;
	private ArrayList<String> correctKeys;
	private final String verticleName;
	int count;
	
	public PhidgetRFIDVerticle(String n){
		verticleName = n;
		count = 0;
	}


	@Override
	public void start() throws Exception {
		super.start();

		correctKeys.add("1000e0a20a");		

		try{
			rfid = new RFIDPhidget();
			rfid.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new RFID Reader has been attached.");
				}
			});
			rfid.waitForAttachment();
			count++;

		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		rfid.addTagGainListener(new TagGainListener()
		{
			public void tagGained(TagGainEvent oe)
			{
				for(String s : correctKeys)
				{
					if(oe.getValue() == s)   //This is the ID of one of the white cards 
					{
						System.out.println("Menu Unlocked");
					}
					else
						System.out.println("Improper RFID Tag");

				}
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
