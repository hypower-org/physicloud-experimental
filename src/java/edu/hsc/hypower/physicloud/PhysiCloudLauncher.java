package edu.hsc.hypower.physicloud;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import oshi.SystemInfo;

/**
 * The new PhysiCloud launching class.
 * @author pmartin@hsc.edu
 *
 */
public class PhysiCloudLauncher extends Launcher {

	public static void main(String[] args) {
		
//		SystemInfo si = new SystemInfo();
//		System.out.println("proc speed (MHz) = " + (si.getHardware().getProcessor().getVendorFreq() / 1E6 ));
//		System.out.println("# processors = " + si.getHardware().getProcessor().getPhysicalProcessorCount());
//		System.out.println("# cores = " + si.getHardware().getProcessor().getLogicalProcessorCount());
		
//		DeploymentOptions options = new DeploymentOptions();
		
		// TODO: Read configuration file - extract IP addr, etc.
		
		
		// TODO: cluster using the ip address
		
		// TODO: when successfully clustered, launch heartbeat verticle, resource verticle, task manager verticle...

		
	}

}
