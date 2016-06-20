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

import edu.hsc.hypower.physicloud.core.*;
import edu.hsc.hypower.physicloud.util.NeighborData;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
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
	
}

		public final void start(){
		// Read in properties file, which is JSON
		try	{

			ObjectMapper mapper = new ObjectMapper();

			//String configFileName = args[0];
			
			JsonNode rootNode = mapper.readTree(new File(configFileName + ".json"));

			String nodeIp = rootNode.get("IP").asText();											// retrieve IP	
			System.out.println("Sensor node IP Address: " + nodeIp);

			String deviceLocation = rootNode.get("Location").asText();								// retrieve location
			System.out.println("Sensor node location: " + deviceLocation);

			String secCode = rootNode.get("securityCode").asText();									// retrieve security code
			System.out.println("Sensor node security code: " + secCode);

			long heartBeatPeriod = rootNode.get("heartBeatPeriod").asLong();						// retrieve heart beat period
			System.out.println("Sensor node heart beat period: " + heartBeatPeriod);

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
					if(asyncRes.succeeded()){
						System.out.println("Clustered vertx launched.");
						Vertx vertx = asyncRes.result();

						// TODO: when successfully clustered, launch heartbeat verticle, resource verticle, task manager verticle...
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

		} catch (JsonProcessingException e) {
			System.err.println("JSON ERROR: " + e.getMessage());
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("ERROR: " + e.getMessage());
			System.exit(-1);
		} 
	}
	
	//I am not sure if this is the correct place for these functions, but I am going to flesh out
	//their functionality at least
	
	public final void stop(){
		vertx.close(new Handler<AsyncResult<Void>>(){

			@Override
			public void handle(AsyncResult<Void> event) {
				System.err.println("PhysiCloud stopped.");
				System.exit(0);
			}
			
		});
		
		public final void stop(String ipAddr){
			//TODO Finish stop function
			
			vertx.close(new Handler<AsyncResult<Void>>(){

				@Override
				public void handle(AsyncResult<Void> event) {
					// TODO Auto-generated method stub
					System.exit(0);
				}
				
			});}
			
			public final void stopAll(){			
				//TODO implement stopAll function
		
					
				}
				
			public final "RETURN TYPE HERE" getNeighborData(){
				LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);
				//TODO implement getNeighborData function
				//Gets neighbor map and returns a PersistentHashMap (clojure object)
			}
			
			public final boolean isResourceAvailable(String resource){
				//TODO implement isResourceAvailable function
				//Checks if the passed in resource is available in the local map
				
				LocalMap<Integer,String> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);
				ArrayList<String> deviceNames = new ArrayList<String>(deviceMap.values());
				outerloop:
				for(String deviceName : deviceNames){
					
					for(Object key : vertx.sharedData().getLocalMap(deviceName).keySet()){
						
						if(((String) key).compareTo(resource) == 0){
							return true;
						}
					}
				}
			
		
			}
				
			
		}
	


