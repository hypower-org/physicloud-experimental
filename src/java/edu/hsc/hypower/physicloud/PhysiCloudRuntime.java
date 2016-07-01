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
import edu.hsc.hypower.physicloud.util.DataMessage;
import edu.hsc.hypower.physicloud.util.DataMessageCodec;
import edu.hsc.hypower.physicloud.util.JsonFieldNames;
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

	private static final String STOP = "isTimeToStop";
	private static final String IPADDR = "ipAddr";
	private static final String ALL = "*";

	public PhysiCloudRuntime(String configFileName) throws JsonProcessingException, IOException{
		ObjectMapper mapper = new ObjectMapper();

		rootNode = mapper.readTree(new File(configFileName + ".json"));

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



					}
				}, 
				10
				);


		new java.util.Timer().schedule( 
				new java.util.TimerTask() {
					@Override
					public void run() {

						test.stopAll();

					}
				}, 
				10000000
				);


	}

	public final void start(){

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
					vertxHook = asyncRes.result();
					vertxHook.eventBus().registerDefaultCodec(DataMessage.class, new DataMessageCodec());

					vertxHook.eventBus().consumer(KernelChannels.KERNEL, new Handler<Message<JsonObject>>(){
						@Override
						public void handle(Message<JsonObject> msg) {
							System.out.println("Received a stop message!");
							JsonObject incomingStopMsg = msg.body();

							// If the IP is the same, or it is the ALL message; AND it is a STOP.
							if((incomingStopMsg.getString(IPADDR).equals(nodeIp) || incomingStopMsg.getString(IPADDR).equals(ALL))
									&& incomingStopMsg.getBoolean(STOP)){
								stop();
							}
						}
					});

					vertxHook.deployVerticle(new ResourceManagerVerticle(nodeIp, 500, rootNode),
							new Handler<AsyncResult<String>>(){
						@Override
						public void handle(AsyncResult<String> res) {
							if(res.succeeded()){
								System.out.println("Deployed ResourceManagerVerticle!");
							}
							else{
								System.out.println("Failed to deployed ResourceManagerVerticle!");
								res.cause().printStackTrace();
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

				}
			}
		};
		Vertx.clusteredVertx(opts, resultHandler); 
	}

	public final void stop(){
		vertxHook.close();
	}

	// Changed the stop protocol.
	public final void stop(String ipAddr){
		JsonObject stopMsg = new JsonObject();
		stopMsg.put(STOP, true);
		stopMsg.put(IPADDR, ipAddr);
		vertxHook.eventBus().publish(KernelChannels.KERNEL, stopMsg);
	}

	public final void stopAll(){			
		JsonObject stopAllMsg = new JsonObject();
		stopAllMsg.put(STOP, true);
		stopAllMsg.put(IPADDR, ALL);
		vertxHook.eventBus().publish(KernelChannels.KERNEL, stopAllMsg);
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

	public final boolean isResourceAvailable(final String resourceName){

		LocalMap<Integer,String> neighborMap = vertxHook.sharedData().getLocalMap(KernelMapNames.AVAILABLE_DEVICES);

		for(int i = 0; i < neighborMap.size(); i++){

			for(Object key : vertxHook.sharedData().getLocalMap(neighborMap.get(i)).keySet()){
				if(key == resourceName)
					return true;
			}
		}

		return false;		
	}

	public final ArrayList<String> listAllResources(){
		ArrayList<String> resources = new ArrayList<String>();



		return resources;
	}

	public final void subscribeToResource(final String resourceName, long updatePeriod, String ipAddr){
		// TODO: for dynamic testing in clojure - may not be part of the final API.
		JsonObject requestMsg = new JsonObject();
		requestMsg.put(JsonFieldNames.IP_ADDR, ipAddr);
		requestMsg.put(JsonFieldNames.UPDATE_TIME, updatePeriod);
		requestMsg.put(JsonFieldNames.REQ_RES, resourceName);

		vertxHook.eventBus().send(ipAddr + "." + KernelChannels.READ_REQUEST, requestMsg, new Handler<AsyncResult<Message<JsonObject>>>() {

			@Override
			public void handle(AsyncResult<Message<JsonObject>> reply) {

				if(reply.succeeded()){

					JsonObject resultReply = reply.result().body();
					String channelName = resultReply.getString(JsonFieldNames.CHANNEL_NAME);
					vertxHook.eventBus().consumer(channelName, new Handler<Message<DataMessage>>()	{

						@Override
						public void handle(Message<DataMessage> event) {
							// TODO Auto-generated method stub
							System.out.println(event.body());
						}	

					});

				}

				else{
					System.out.println("Reply did not succeed...");
				}

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