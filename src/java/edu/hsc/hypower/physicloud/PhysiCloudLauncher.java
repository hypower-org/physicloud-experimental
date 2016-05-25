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

import edu.hsc.hypower.physicloud.core.HeartBeatVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import oshi.SystemInfo;

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
			//String configFileName = args[0];
			JsonNode rootNode = mapper.readTree(new File("test.json"));

			String nodeIp = rootNode.get("IP").asText();											// retrieve IP	
			System.out.println("Sensor node IP Address: " + nodeIp);

			String deviceLocation = rootNode.get("Location").asText();								// retrieve location
			System.out.println("Sensor node location: " + deviceLocation);

			String secCode = rootNode.get("securityCode").asText();									// retrieve security code
			System.out.println("Sensor node security code: " + secCode);

			long heartBeatPeriod = rootNode.get("heartBeatPeriod").asLong();						// retrieve heart beat period
			System.out.println("Sensor node heart beat period: " + heartBeatPeriod);

			HashMap<String, ArrayList<String>> deviceMap = new HashMap<String, ArrayList<String>>(); 

			Iterator<String> it = rootNode.fieldNames();
			while(it.hasNext())
			{
				String fieldName = it.next();		
				if(fieldName.indexOf('.') != -1)
				{
					String devName = fieldName.substring(fieldName.indexOf('.')+1);				
					JsonNode tempNode = rootNode.get(fieldName);

					Iterator<String> internalIt = tempNode.fieldNames();
					ArrayList<String> forMap = new ArrayList<String>();
					System.out.println(devName);

					while(internalIt.hasNext())
					{
						String nextField = internalIt.next();
						String sensorType = tempNode.get(nextField).asText();
						String locNum = nextField.substring(4,5);
						String last = sensorType + '.' + locNum;
						forMap.add(last);
						System.out.println(last);								
					}

					// TODO: The results of this map should be submitted to the HeartBeatVerticle
					deviceMap.put(devName, forMap);

				}
			}

			VertxOptions opts = new VertxOptions()
					.setWorkerPoolSize(Runtime.getRuntime().availableProcessors())
					.setClusterHost(nodeIp);

			Handler<AsyncResult<Vertx>> resultHandler = new Handler<AsyncResult<Vertx>>(){
				@Override
				public void handle(AsyncResult<Vertx> asyncRes) {
					if(asyncRes.succeeded()){
						System.out.println("Clustered vertx launched.");
						Vertx vertx = asyncRes.result();

						// TODO: when successfully clustered, launch heartbeat verticle, resource verticle, task manager verticle...
						vertx.deployVerticle(new HeartBeatVerticle(nodeIp, heartBeatPeriod));
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

