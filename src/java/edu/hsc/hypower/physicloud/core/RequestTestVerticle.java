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

		vertx.setPeriodic(1000, this::sendRequestMessage);


	}

	private final void sendRequestMessage(Long timerEvent){
		
		//JsonObject for request
		JsonObject reqMsg = new JsonObject();
	
		//Local copy of NeighborData
		LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);
		
	
		String[] ipSet = (String[]) neighborMap.keySet().toArray();
		
		reqMsg.put("Requested Resource", "temperature.0");
		
		for(int i = 0; i < ipSet.length; i++){
			
			//TODO- ADD REPLY HANDLER
			vertx.eventBus().send(ipSet[i] + ".KernelChannels.HEARTBEAT", reqMsg, new Handler<AsyncResult<Message<JsonObject>>>());
			
			// TODO-RECEIVE REPLY HERE
			JsonObject resultReply = reply.result().body();
			
			//Output result from reply
			if(reply.succeeded()){		
			
				if(resultReply.getBoolean("Is Available")){
					System.out.println("Resource is Available");
				}
				else{
					System.out.println("Resource not Available");
				}
			}
		}
		


	}

}
