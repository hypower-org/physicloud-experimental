package edu.hsc.hypower.physicloud;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hsc.hypower.physicloud.core.DynamicVerticle;
import edu.hsc.hypower.physicloud.core.HeartBeatVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * Class that experiments with setting up the api for clojure to access the PhysiCloud runtime.
 * @author pjmartin
 *
 */
public final class PhysiCloudFromClojure {

	private Vertx vertxHook;
	
	public PhysiCloudFromClojure(){

	}

	public final void go(){
		ObjectMapper mapper = new ObjectMapper();
		//String configFileName = args[0];
		JsonNode rootNode;
		try {

			rootNode = mapper.readTree(new File("test.json"));
			String nodeIp = rootNode.get("IP").asText();											// retrieve IP	
			System.out.println("Sensor node IP Address: " + nodeIp);

			String deviceLocation = rootNode.get("Location").asText();								// retrieve location
			System.out.println("Sensor node location: " + deviceLocation);

			String secCode = rootNode.get("securityCode").asText();									// retrieve security code
			System.out.println("Sensor node security code: " + secCode);

			long heartBeatPeriod = rootNode.get("heartBeatPeriod").asLong();						// retrieve heart beat period
			System.out.println("Sensor node heart beat period: " + heartBeatPeriod);

			VertxOptions opts = new VertxOptions()
					.setWorkerPoolSize(Runtime.getRuntime().availableProcessors())
					.setClusterHost(nodeIp);

			Handler<AsyncResult<Vertx>> resultHandler = new Handler<AsyncResult<Vertx>>(){
				@Override
				public void handle(AsyncResult<Vertx> asyncRes) {
					if(asyncRes.succeeded()){
						System.out.println("Clustered vertx launched.");
						vertxHook = asyncRes.result();

						vertxHook.deployVerticle(new HeartBeatVerticle(nodeIp, heartBeatPeriod), 
								new DeploymentOptions().setWorker(true));						
					}
				}
			};
			Vertx.clusteredVertx(opts, resultHandler);

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public final void stop(){
		vertxHook.close(new Handler<AsyncResult<Void>>(){

			@Override
			public void handle(AsyncResult<Void> event) {
				System.err.println("PhysiCloud stopped.");
				System.exit(0);
			}
			
		});
	}

	public final void deployFunction(final String fnName, final String fn, long updatePeriod){
		
		vertxHook.deployVerticle(new DynamicVerticle(fnName, fn, updatePeriod), 
				result -> {
					if(result.succeeded()){
						System.out.println(" DynamicVerticle " + fnName + " deployed!");
					}
				});
		
		vertxHook.eventBus().consumer(fnName, msg -> { System.out.println(msg.body()); });
		
	}
	
}
