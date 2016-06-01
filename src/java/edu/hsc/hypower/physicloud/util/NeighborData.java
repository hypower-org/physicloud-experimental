package edu.hsc.hypower.physicloud.util;

/**
 * An immutable class that stores all necessary PhysiCloud neighbor data.
 * 
 * @author pmartin@hsc.edu
 *		   hackleyb18@hsc.edu
 */
public final class NeighborData {

	private final String ipAddr;
	private final long memAvail;
	private final int pCore;
	private final int lCore;
	private final double pLoad;

	public NeighborData(String ipAddr, long memAvail, int pCore, int lCore, double pLoad) {
		this.ipAddr = ipAddr;
		this.memAvail = memAvail;
		this.pCore = pCore;
		this.lCore = lCore;
		this.pLoad = pLoad;
	}

	public final String getIpAddr() {
		return ipAddr;
	}
	
	public long memAvail()	{
		return memAvail;
	}

	public int getpCore() {
		return pCore;
	}

	public int getlCore() {
		return lCore;
	}

	public double getpLoad() {
		return pLoad;
	}

}
