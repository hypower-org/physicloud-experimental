package edu.hsc.hypower;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.LocalMap;
import clojure.java.api.Clojure;
import clojure.lang.Compiler;
import clojure.lang.IFn;

import java.io.StringReader;


/**
 * This verticle reads a clojure string function and executes it within
 * a periodic Vertx function.
 * @author pjmartin
 *
 */
public class DynamicVerticle extends AbstractVerticle {

	private final static String NS = "(ns user)";
	
	private final String funcString;
	private final IFn function;
	
	public DynamicVerticle(String fnName, String fnString){
		
		// Use clojure to read and load the supplied function.
		funcString = NS + fnString;
		Clojure.read(funcString);
		Compiler.load(new StringReader(funcString));
		function = Clojure.var("user", fnName);
	}
	
	@Override
	public void start() throws Exception {
		
		vertx.setPeriodic(500, new Handler<Long>(){

			@Override
			public void handle(Long l) {
				LocalMap<String, Long> dataMap = vertx.sharedData().getLocalMap("localData");
				System.out.println("Current value = " + dataMap.get("currVal"));
				Object val = function.invoke(dataMap.get("currVal"));
				vertx.eventBus().publish("incoming", val.toString());
			}
			
		});
		
		vertx.setPeriodic(400, new Handler<Long>(){
			@Override
			public void handle(Long l) {
				LocalMap<String, Long> dataMap = vertx.sharedData().getLocalMap("localData");
				dataMap.put("currVal", (long) (Math.floor(Math.random() * 100)));
			}
		});
		
	}

	@Override
	public void stop() throws Exception {
		System.err.println("Done!");
		super.stop();
	}
	
}
