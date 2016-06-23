package edu.hsc.hypower.physicloud;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;

import edu.hsc.hypower.physicloud.core.DynamicVerticle;
import edu.hsc.hypower.physicloud.core.HeartBeatVerticle;

import clojure.lang.PersistentHashMap;
import edu.hsc.hypower.physicloud.core.*;
import edu.hsc.hypower.physicloud.util.NeighborData;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * The new PhysiCloud launching class.
 * @author pmartin@hsc.edu
 *         hackleyb18@hsc.edu
 *         kangask18@hsc.edu
 */

public class PhysiCloudRuntime {


	private Vertx vertxHook;
	private JsonNode rootNode;
	private String nodeIp;
	private String deviceLocation;
	private String secCode;
	private long heartBeatPeriod;

	// 3 - Any other information we may want hold onto in the core of the runtime.

	public PhysiCloudRuntime(String configFileName) throws JsonProcessingException, IOException{
		ObjectMapper mapper = new ObjectMapper();

		JsonNode rootNode = mapper.readTree(new File(configFileName + ".json"));

		// TODO: You did not assign the values to the member variables...(see line 96)
		// retrieve IP
		nodeIp = rootNode.get("IP").asText();
		System.out.println("Sensor node IP Address: " + nodeIp);
		// retrieve location
		deviceLocation = rootNode.get("Location").asText();
		System.out.println("Sensor node location: " + deviceLocation);
		// retrieve security code
		secCode = rootNode.get("securityCode").asText();
		System.out.println("Sensor node security code: " + secCode);
		// retrieve heart beat period
		heartBeatPeriod = rootNode.get("heartBeatPeriod").asLong();
		System.out.println("Sensor node heart beat period: " + heartBeatPeriod);
	}


	public static void main(String[] args) throws JsonProcessingException, IOException {

		PhysiCloudRuntime test = new PhysiCloudRuntime(args[0]);

		test.start();
		
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		            	Boolean isRes =   test.isResourceAvailable("temp.0");
		            	if(isRes == true)
		            		System.out.println("Resource available!");
		            	else
		            		System.out.println("Resource unavailable :(");
		        
		            	test.stopAll();
		            }
		        }, 
		        10000
		);
		
		
	}

	public final void start(){

		Config clusterConfig = new Config();
		clusterConfig.getNetworkConfig().setPort(5000);
		clusterConfig.getNetworkConfig().setPortAutoIncrement(true);
		// TODO: ...so you are passing an initialized value here!
		clusterConfig.getNetworkConfig().getInterfaces().setEnabled(true).addInterface(nodeIp);
		ClusterManager mgr = new HazelcastClusterManager(clusterConfig);

		VertxOptions opts = new VertxOptions()
				.setWorkerPoolSize(Runtime.getRuntime().availableProcessors())
				.setClusterManager(mgr)
				.setClusterHost(nodeIp);

		Handler<AsyncResult<Vertx>> resultHandler = new Handler<AsyncResult<Vertx>>(){
			@Override
			public void handle(AsyncResult<Vertx> asyncRes) {

				if(asyncRes.succeeded()){
					System.out.println("Clustered vertx launched.");
					// TODO: Need to assign!
					vertxHook = asyncRes.result();

					vertxHook.eventBus().consumer(KernelChannels.KERNEL, new Handler<Message<JsonObject>>(){
						@Override
						public void handle(Message<JsonObject> msg) {

							System.out.println("Received a stop message!");
							JsonObject whoStops = msg.body();

							// TODO: I tested via clojure and the message is received but it does not stop. Check your
							// logic.
							if(whoStops.getString("nIpAddr") == nodeIp)
								stop();

							else if(whoStops.getString("nIpAddr") == "ALL")
								stop();
						}
					});



					vertxHook.deployVerticle(new ResourceManagerVerticle(nodeIp, 500, rootNode),
							new Handler<AsyncResult<String>>(){
						@Override
						public void handle(AsyncResult<String> res) {
										if(res.succeeded()){
								System.out.println("Deployed ResourceManagerVerticle!");
							}
						}
					});

					vertxHook.deployVerticle(new HeartBeatVerticle(nodeIp, heartBeatPeriod), 
							new DeploymentOptions().setWorker(true), 
							new Handler<AsyncResult<String>>(){

						@Override
						public void handle(AsyncResult<String> res) {
							if(res.succeeded()){
								System.out.println("Deployed HeartBeatVerticle!");
							}
						}
					});

					vertxHook.deployVerticle(new RequestTestVerticle(),
							new Handler<AsyncResult<String>>(){
						@Override
						public void handle(AsyncResult<String> res) {
							if(res.succeeded()){
								System.out.println("Deployed RequestTestVerticle!");
							}
						}
					});


				}
			}
		};
		Vertx.clusteredVertx(opts, resultHandler); 
	}
	

	
	
	public final void stop(){
	
		vertxHook.close();
		
			}

	public final void stop(String ipAddr){
		//TODO Test it

		JsonObject onlySleepNow = new JsonObject();
		onlySleepNow.put("nIpAddr", ipAddr);

		vertxHook.eventBus().publish(KernelChannels.KERNEL, onlySleepNow);
	}

	public final void stopAll(){			

		JsonObject onlySleepNow = new JsonObject();
		onlySleepNow.put("nIpAddr", "ALL");
		vertxHook.eventBus().publish(KernelChannels.KERNEL, onlySleepNow);
	}

	//		public final PersistentHashMap getNeighborData(){
	//			//TODO implement getNeighborData function
	//			//Gets neighbor map and returns a PersistentHashMap (clojure object)
	//
	//			LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);
	//
	//			
	//			PersistentHashMap nData = new PersistentHashMap(0, null, false, nData);
	//			return nData;
	//		}

	public final boolean isResourceAvailable(String resource){

		LocalMap<Integer,String> neighborMap = vertxHook.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);

		for(int i = 0; i < neighborMap.size(); i++){

			for(Object key : vertxHook.sharedData().getLocalMap(neighborMap.get(i)).keySet()){
				if(key == resource)
					return true;
			}
		}

		return false;		
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