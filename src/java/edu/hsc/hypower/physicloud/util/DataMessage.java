package edu.hsc.hypower.physicloud.util;

import java.awt.List;
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


public class DataMessage {

	private final String id;
	private final ArrayList<String> tupleList;

	public DataMessage(String n, ArrayList<String> x){
		id = n;
		tupleList = x;
	}
	
	public String getId() {
		return id;
	}
	
	public ArrayList<String> getList()	{
		return tupleList;
	}
	
	public int getLength()	{
		
		return tupleList.size();
	}
}
