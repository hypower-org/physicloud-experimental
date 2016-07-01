package edu.hsc.hypower.physicloud.hw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.phidgets.*;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.GPSPositionChangeListener;

import edu.hsc.hypower.physicloud.KernelChannels;
import edu.hsc.hypower.physicloud.util.DataArray;
import edu.hsc.hypower.physicloud.util.DataTuple;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;

public class PhidgetGPSVerticle extends AbstractVerticle {

	private final String verticleName;
	private final Long updatePeriod = 100l;
	private static final String LAT_LONG = "latitudeLongitude";
	private static final String ALT_VEL = "altitudeVelocity";
	private GPSPhidget gps;
	int count;

	public PhidgetGPSVerticle(String n){
		verticleName = n;
		count = 0;
	}

	public void start() throws Exception {
		super.start();
		try {
			gps = new GPSPhidget();
			gps.openAny();
			gps.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae)	{
					System.out.println("A new Phidget GPS has been attached.");
				}
			});
			gps.waitForAttachment();
			count++;

		} catch (PhidgetException e) {
			e.printStackTrace();
		}

		vertx.setPeriodic(updatePeriod, this::updateSensorData);	

	}


	public final void updateSensorData(Long l) {


		LocalMap<String, DataArray> gpsMap = vertx.sharedData().getLocalMap(verticleName + "." + count);
		ArrayList<DataTuple> dataArr = new ArrayList<DataTuple>();

		GPSPositionChangeListener GPSPositionChangeListener = null;
		gps.addGPSPositionChangeListener(GPSPositionChangeListener);

		try {

			dataArr.add(new DataTuple(new Double(gps.getLatitude())));
			dataArr.add(new DataTuple(new Double(gps.getLongitude())));
			dataArr.add(new DataTuple(new Double(gps.getAltitude())));
			dataArr.add(new DataTuple(new Double(gps.getVelocity())));
			gpsMap.put(verticleName + "." + count, new DataArray(dataArr));

		} catch (PhidgetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		gps.close();
		super.stop();
	}






}

