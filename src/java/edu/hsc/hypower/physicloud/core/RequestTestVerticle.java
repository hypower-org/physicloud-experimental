package edu.hsc.hypower.physicloud.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.hsc.hypower.physicloud.*;
import edu.hsc.hypower.physicloud.util.DataTuple;
import edu.hsc.hypower.physicloud.util.JsonFieldNames;
import edu.hsc.hypower.physicloud.util.NeighborData;

public class RequestTestVerticle extends AbstractVerticle {

	private final String ipAddr;
	
	public RequestTestVerticle(String ip){
		ipAddr = ip;
	}
	
	@Override
	public void start() throws Exception {
		super.start();

		System.out.println("Requests have started");

		vertx.setPeriodic(1000, this::sendRequestMessage);
	}

	private final void sendRequestMessage(Long timerEvent){

		//JsonObject for request
		JsonObject readReqMsg = new JsonObject();

		//Local copy of NeighborData
		LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);

		Set<String> ipSet = neighborMap.keySet();

		readReqMsg.put(JsonFieldNames.REQ_RES, "temperature.0");
		readReqMsg.put(JsonFieldNames.UPDATE_TIME, 100);
		readReqMsg.put(JsonFieldNames.IP_ADDR, ipAddr);
		
		
		//This currently sends a request to all CPUs on the cluster for testing purposes
		
		for(String s : ipSet){
			vertx.eventBus().send(s + "." + KernelChannels.RESOURCE_QUERY, readReqMsg, new Handler<AsyncResult<Message<JsonObject>>>() {


				public void handle(AsyncResult<Message<JsonObject>> msg){	

					if(msg.succeeded()){

						JsonObject resultReply = msg.result().body();

						//Output result from reply


						if(resultReply.getBoolean("isAllowed")){
							System.out.println("Resource is Available");
						}
						else{
							System.out.println("Resource not Available");
						}

					}}});
		}
		
		//This handles the setPeriodic that sends the data in the ResourceManager
		vertx.eventBus().consumer(readReqMsg.getString(JsonFieldNames.REQ_RES) + "@." + ipAddr, new Handler<Message<DataTuple>>() {
			
			
			public void handle(Message<DataTuple> msg){
				
				DataTuple data = msg.body();
				
				System.out.println("Value of data: " + data.getData());
			}
			
		});
		
	}

}
