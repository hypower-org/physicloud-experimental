package edu.hsc.hypower.physicloud.core;

import edu.hsc.hypower.physicloud.KernelMapNames;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OperatingSystem;

public class ResourceManagerVerticle extends AbstractVerticle {

	// TODO: Use the OSHI library to load the system properties.
	private final OperatingSystem os;
	private final CentralProcessor cp;
	
	private final long updatePeriod;
	private LocalMap<String,Object> resourceMap;
	
	public ResourceManagerVerticle(long up){
		SystemInfo si = new SystemInfo();
		os = si.getOperatingSystem();
		cp = si.getHardware().getProcessor();
		updatePeriod = up;
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		resourceMap = vertx.sharedData().getLocalMap(KernelMapNames.RESOURCES);
		
		vertx.setPeriodic(updatePeriod, ResourceManagerVerticle::updateCyberResources);
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
	}

	private static final void updateCyberResources(Long l){
		
	}
	
}
