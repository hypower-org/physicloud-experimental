package edu.hsc.hypower;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JSONTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Vertx vertx = Vertx.vertx();

		JsonObject obj1 = new JsonObject();

		String jsonString = "{\"IP\":\"Static Ip Goes Here\"}";

		JsonObject obj2 = new JsonObject(jsonString);

		System.out.println(obj2.isEmpty());

		obj2.put("Device", "Phidget");

		String device = obj2.getString("Device");

		System.out.println(obj2);

		obj2.encodePrettily();

		System.out.println(obj2);

		String obj2String = obj2.toString();


		JsonObject config = vertx.fileSystem().readFileBlocking("test.json").toJsonObject();

		String ip = config.getString("IP");

		System.out.println(ip);


		for(String deviceKey : config.getJsonObject("devices").fieldNames())
		{

			JsonObject deviceObject = config.getJsonObject("devices").getJsonObject(deviceKey);
			System.out.println(deviceObject);


			for(Entry<String, Object> location : deviceObject)
			{
				System.out.println(location.toString());
				String parseLoc = location.toString();
				String locNum = parseLoc.substring(4,5);
				String sensorType = parseLoc.substring(6, parseLoc.length());
				System.out.println(sensorType + "." + locNum);

			}
		}











	}


}


