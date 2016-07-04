package edu.hsc.hypower.physicloud.hw;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;

import java.util.ArrayList;

import com.phidgets.*;
import com.phidgets.event.*;

import edu.hsc.hypower.physicloud.util.DataArray;
import edu.hsc.hypower.physicloud.util.DataTuple;

public class PhidgetRFIDVerticle extends AbstractVerticle {

	private final static String NO_TAG = "NO_TAG";
	
	private RFIDPhidget rfid;
	private final String verticleName;
	int count;

	public PhidgetRFIDVerticle(String name){
		verticleName = name;
		count = 0;
		
		try {
			rfid = new RFIDPhidget();
			rfid.openAny();
			rfid.waitForAttachment();
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void start() throws Exception {
		super.start();

		rfid.setAntennaOn(true);
		rfid.setLEDOn(false);
		LocalMap<String, DataArray> rfidMap = vertx.sharedData().getLocalMap(verticleName);
		DataTuple tagDetectData = new DataTuple(new Boolean(false));
		DataTuple tagId = new DataTuple(PhidgetRFIDVerticle.NO_TAG);
		ArrayList<DataTuple> initDataTuples = new ArrayList<DataTuple>();
		initDataTuples.add(tagDetectData);
		initDataTuples.add(tagId);
		rfidMap.put("rfid", new DataArray(initDataTuples));
		
		rfid.addTagGainListener(new TagGainListener(){

			@Override
			public void tagGained(TagGainEvent tge) {
				DataTuple tagDetectData = new DataTuple(new Boolean(true));
				DataTuple tagId = new DataTuple(tge.getValue());
				ArrayList<DataTuple> updatedDataTuples = new ArrayList<DataTuple>();
				updatedDataTuples.add(tagDetectData);
				updatedDataTuples.add(tagId);
				System.out.println("New RFID data :" + updatedDataTuples);
				rfidMap.put("rfid", new DataArray(updatedDataTuples));
			}
			
		});
		
		rfid.addTagLossListener(new TagLossListener(){

			@Override
			public void tagLost(TagLossEvent tle) {
				DataTuple tagDetectData = new DataTuple(new Boolean(false));
				DataTuple tagId = new DataTuple(PhidgetRFIDVerticle.NO_TAG);
				ArrayList<DataTuple> updatedDataTuples = new ArrayList<DataTuple>();
				updatedDataTuples.add(tagDetectData);
				updatedDataTuples.add(tagId);
				System.out.println("New RFID data :" + updatedDataTuples);
				rfidMap.put("rfid", new DataArray(updatedDataTuples));
			}
			
		});
		
	}

	@Override
	public void stop() throws Exception {
		rfid.close();
		super.stop();
	}

	public static void main(String[] args){
		
		Vertx vertx = Vertx.factory.vertx();
		vertx.deployVerticle(new PhidgetRFIDVerticle("RFID.0"), new Handler<AsyncResult<String>>(){
			@Override
			public void handle(AsyncResult<String> event) {
				System.out.println(PhidgetRFIDVerticle.class.getName() + " Started!");
				
			}
		});
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		vertx.close();
	}

}
