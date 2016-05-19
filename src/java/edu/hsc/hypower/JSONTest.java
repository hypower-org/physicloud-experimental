package edu.hsc.hypower;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
						
			
			
			
			
		}
		
		 
	}


