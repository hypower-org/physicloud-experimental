package edu.hsc.hypower.physicloud.util;

/**
 * An immutable class that stores all necessary PhysiCloud neighbor data.
 * 
 * @author pmartin@hsc.edu
 *
 */
public final class NeighborData {

	private final String ipAddr;

	public NeighborData(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public final String getIpAddr() {
		return ipAddr;
	}

}
