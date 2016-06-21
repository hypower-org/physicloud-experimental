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
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * The new PhysiCloud launching class.
 * @author pmartin@hsc.edu
 *         hackleyb18@hsc.edu
 */
// TODO: You need to declare classes correctly!
public class PhysiCloudRuntime {

	// TODO: The member variables of the class need to be here...
	
	// 1 - The vertx object
	private Vertx vertx;
	// 2 - All of the results from parsing the config file: nodeIp, etc. Make them private member variables.
	
	private String nodeIp;
	
	private String deviceLocation;
	
	private String secCode;
	
	private long heartBeatPeriod;
	
	// 3 - Any other information we may want hold onto in the core of the runtime.
	
	// TODO: Now make the constructor...
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

	public final void start(){

		try	{

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
					
					// TODO: We also need subscriptions to the KernelChannels.KERNEL channel to listen for the stop
					// message. You can design this message any way you want: a string or small JsonObject.
					
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

			//After moving things to their proper places it seems
			//this doesn't need to go here.
//		} catch (IOException e) {
//			System.err.println("ERROR: " + e.getMessage());
//			System.exit(-1);
		} 
	}

	// TODO: Yes, they are member functions. This is the correct location, but they depend on the vertx variable.
	// See my testing class PhysiCloudFromClojure to see how I handle the Vertx variable.
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
			// TODO: This function sends the "poison pill" message that makes another
			// CPU stop execution.

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

		public final PersistentHashMap getNeighborData(){
			//TODO implement getNeighborData function
			//Gets neighbor map and returns a PersistentHashMap (clojure object)

			LocalMap<String,NeighborData> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);

			//I am unsure how to use the PersistentHashMap, as the API is not too helpful
			PersistentHashMap nData = new PersistentHashMap(String, NeighborData);
			return


		}

		public final boolean isResourceAvailable(String resource){
			//Potentially finished

			LocalMap<Integer,String> neighborMap = vertx.sharedData().getLocalMap(KernelMapNames.NEIGHBORS);
			ArrayList<String> deviceNames = new ArrayList<String>(vertx.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES).values());
			outerloop:
				for(String deviceName : deviceNames){

					for(Object key : vertx.sharedData().getLocalMap(deviceName).keySet()){

						if(((String) key).compareTo(resource) == 0){
							return true;
						}
					}
				}
			return false;


		}
	}








}

