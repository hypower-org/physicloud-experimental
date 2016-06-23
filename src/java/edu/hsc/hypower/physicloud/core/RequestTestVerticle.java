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
import edu.hsc.hypower.physicloud.util.JsonFieldNames;
import edu.hsc.hypower.physicloud.util.NeighborData;

public class RequestTestVerticle extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		super.start();

		System.out.println("Requests have started");

		vertx.setPeriodic(10000, this::sendRequestMessage);
	}

	private final void sendRequestMessage(Long timerEvent){

		//JsonObject for request
		JsonObject reqMsg = new JsonObject();

		//Local copy of NeighborData
		LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);

		Set<String> ipSet = neighborMap.keySet();

		reqMsg.put("Requested Resource", "temperature.0");
		reqMsg.put(JsonFieldNames.UPDATE_TIME, 100);
		reqMsg.put(JsonFieldNames.IP_ADDR, "?");
		//TODO How do I get the IP address of the current node?

		for(String s : ipSet){
			vertx.eventBus().send(s + "." + KernelChannels.READ_REQUEST, reqMsg, new Handler<AsyncResult<Message<JsonObject>>>() {


				public void handle(AsyncResult<Message<JsonObject>> msg){	

					if(msg.succeeded()){

						JsonObject resultReply = msg.result().body();

						//Output result from reply


						if(resultReply.getBoolean("Is Available")){
							System.out.println("Resource is Available");
						}
						else{
							System.out.println("Resource not Available");
						}

					}}});
		}
		
		//This handles the setPeriodic that sends the data in the ResourceManager
		vertx.eventBus().consumer("reqRes.@" + localIp, new Handler<Message<JsonObject>>() {
			
			
			public void handle(Message<JsonObject> msg){
				JsonObject data = msg.body();
				
				System.out.println("Value of data: " + data.getString("Data"));
			}
			
		});
		
	}

}
