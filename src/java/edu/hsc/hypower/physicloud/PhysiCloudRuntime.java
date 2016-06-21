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
 */

public class PhysiCloudRuntime {

	
	private Vertx vertx;
	
	private JsonNode rootNode;
	
	private String nodeIp;
	
	private String deviceLocation;
	
	private String secCode;
	
	private long heartBeatPeriod;
	
	// 3 - Any other information we may want hold onto in the core of the runtime.
	
	public PhysiCloudRuntime(String configFileName) throws JsonProcessingException, IOException{
		ObjectMapper mapper = new ObjectMapper();

		JsonNode rootNode = mapper.readTree(new File(configFileName + ".json"));
		
		String nodeIp = rootNode.get("IP").asText();											// retrieve IP	
		System.out.println("Sensor node IP Address: " + nodeIp);

		String deviceLocation = rootNode.get("Location").asText();								// retrieve location
		System.out.println("Sensor node location: " + deviceLocation);

		String secCode = rootNode.get("securityCode").asText();									// retrieve security code
		System.out.println("Sensor node security code: " + secCode);

		long heartBeatPeriod = rootNode.get("heartBeatPeriod").asLong();						// retrieve heart beat period
		System.out.println("Sensor node heart beat period: " + heartBeatPeriod);
	}
	
	
	public static void main(String[] args) throws JsonProcessingException, IOException {
		
		PhysiCloudRuntime test = new PhysiCloudRuntime(args[0]);
		
		test.start();
//		new java.util.Timer().schedule( 
//		        new java.util.TimerTask() {
//		            @Override
//		            public void run() {
//		                test.stop();
//		            }
//		        }, 
//		        10000 
//		);
		
	}

	public final void start(){

		// TODO: all vertx config and launching is performed in the start() function.
		// Set up hazelcast correctly
		Config clusterConfig = new Config();
		clusterConfig.getNetworkConfig().setPort(5000);
		clusterConfig.getNetworkConfig().setPortAutoIncrement(true);
		clusterConfig.getNetworkConfig().getInterfaces().setEnabled(true).addInterface(nodeIp);
		ClusterManager mgr = new HazelcastClusterManager(clusterConfig);

		VertxOptions opts = new VertxOptions()
				.setWorkerPoolSize(Runtime.getRuntime().availableProcessors())
				.setClusterManager(mgr)
				.setClusterHost(nodeIp);

		Handler<AsyncResult<Vertx>> resultHandler = new Handler<AsyncResult<Vertx>>(){
			@Override
			public void handle(AsyncResult<Vertx> asyncRes) {
				
				vertx.eventBus().consumer(KernelChannels.HEARTBEAT, new Handler<Message<JsonObject>>(){
					@Override
					public void handle(Message<JsonObject> msg) {
						
						JsonObject whoStops = msg.body();
						
						if(whoStops.getString("nIpAddr") == nodeIp)
							stop();
						
						else if(whoStops.getString("nIpAddr") == "ALL")
							stop();
					}});
					
				
				if(asyncRes.succeeded()){
					System.out.println("Clustered vertx launched.");
					Vertx vertx = asyncRes.result();

					vertx.deployVerticle(new ResourceManagerVerticle(nodeIp, 500, rootNode),
							new Handler<AsyncResult<String>>(){
						@Override
						public void handle(AsyncResult<String> res) {
							if(res.succeeded()){
								System.out.println("Deployed ResourceManagerVerticle!");
							}
						}
					});

					vertx.deployVerticle(new HeartBeatVerticle(nodeIp, heartBeatPeriod), 
							new DeploymentOptions().setWorker(true), 
							new Handler<AsyncResult<String>>(){

						@Override
						public void handle(AsyncResult<String> res) {
							if(res.succeeded()){
								System.out.println("Deployed HeartBeatVerticle!");
							}
						}
					});

					vertx.deployVerticle(new RequestTestVerticle(),
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
		//TODO Test it
		
		vertx.close(new Handler<AsyncResult<Void>>(){

			@Override
			public void handle(AsyncResult<Void> event) {
				System.err.println("PhysiCloud stopped.");
				System.exit(0);
			}

		});}

		public final void stop(String ipAddr){
			//TODO Test it
			
			JsonObject onlySleepNow = new JsonObject();
			onlySleepNow.put("nIpAddr", ipAddr);
			vertx.eventBus().publish(KernelChannels.HEARTBEAT, onlySleepNow);
		}

		public final void stopAll(){			
			//TODO Test it
			
			JsonObject onlySleepNow = new JsonObject();
			onlySleepNow.put("nIpAddr", "ALL");
			vertx.eventBus().publish(KernelChannels.HEARTBEAT, onlySleepNow);
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
				
			LocalMap<Integer,String> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);
			
			for(int i = 0; i < neighborMap.size(); i++){
	
				for(Object key : vertx.sharedData().getLocalMap(neighborMap.get(i)).keySet()){
					if(key == resource)
						return true;
				}
			}
			
			return false;		
		}
		
		public final void deployFunction(final String fnName, final String fn, long updatePeriod){
			
			vertx.deployVerticle(new DynamicVerticle(fnName, fn, updatePeriod), 
					result -> {
						if(result.succeeded()){
							System.out.println(" DynamicVerticle " + fnName + " deployed!");
						}
					});
			
			vertx.eventBus().consumer(fnName, msg -> { System.out.println(msg.body()); });
			
		}	
		
	}