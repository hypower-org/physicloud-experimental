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
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * The new PhysiCloud launching class.
 * @author pmartin@hsc.edu
 *         hackleyb18@hsc.edu
 */
public class PhysiCloudLauncher {

	public static void main(String[] args) {

		// Read in properties file, which is JSON
		try	{

			ObjectMapper mapper = new ObjectMapper();
			// TODO: The PCLauncher now accepts a string name for the json file.
			// It is passed as a parameter on the command line.
			String configFileName = args[0];
			
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
						
						vertx.deployVerticle(new RequestTestVerticle(nodeIp),
						new Handler<AsyncResult<String>>(){
							@Override
							public void handle(AsyncResult<String> res) {
								if(res.succeeded()){
									System.out.println("Deployed RequestTestVerticle!");
								}
							}
						});
						
						vertx.setPeriodic(1000000, new Handler<Long>() {

							@Override
							public void handle(Long event) {
								// TODO Auto-generated method stub
								
								vertx.close(new Handler<AsyncResult<Void>>(){

									@Override
									public void handle(AsyncResult<Void> event) {
										// TODO Auto-generated method stub
										System.exit(0);
									}
									
								});
								
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
}

