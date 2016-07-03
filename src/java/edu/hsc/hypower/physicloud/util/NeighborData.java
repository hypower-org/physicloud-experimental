package edu.hsc.hypower.physicloud.util;

import java.util.ArrayList;
import java.util.HashMap;

import io.vertx.core.shareddata.Shareable;

/**
 * An immutable class that stores all necessary PhysiCloud neighbor data.
 * 
 * @author pmartin@hsc.edu
 *		   hackleyb18@hsc.edu
 */
public final class NeighborData implements Shareable{

	private final String ipAddr;
	private final HashMap<String, ArrayList<String>> deviceData;
	
	public NeighborData(String ipAddr, HashMap<String, ArrayList<String>> deviceData) {
		this.ipAddr = ipAddr;
		this.deviceData = deviceData;
	}


	public final String getIpAddr() {
		return ipAddr;
	}
	
	public final HashMap<String, ArrayList<String>> getDeviceData() {
		return new HashMap<String,ArrayList<String>>(deviceData);
	}
	
}
