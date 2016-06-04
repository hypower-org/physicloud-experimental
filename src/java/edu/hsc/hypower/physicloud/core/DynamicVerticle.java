package edu.hsc.hypower.physicloud.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import clojure.java.api.Clojure;
import clojure.lang.Compiler;
import clojure.lang.IFn;

import java.io.StringReader;


/**
 * This verticle reads a clojure string function and executes it within
 * a periodic Vertx function.
 * @author pmartin@hsc.edu
 *
 */
public class DynamicVerticle extends AbstractVerticle {

	private final static String NS = "(ns user)";
	
	private final String funcString;
	private final IFn function;
	private final String name;
	private final long timerPeriod;
	
	public DynamicVerticle(String fnName, String fnString, long period){
		name = fnName;
		timerPeriod = period;
		
		// Use clojure to read and load the supplied function.
		funcString = NS + fnString;
		// TODO: only handles defn functions! Anonymous functions to come...
		Clojure.read(funcString);
		Compiler.load(new StringReader(funcString));
		function = Clojure.var("user", fnName);
	}
	
	@Override
	public void start() throws Exception {
		vertx.setPeriodic(timerPeriod, this::executeOnTimer);
	}

	@Override
	public void stop() throws Exception {
		System.err.println(name + " execution stopped.");
		super.stop();
	}
	
	private final void executeOnTimer(Long timerEvent){
		
		// Invoke is hacky right now!!!
		Object value = function.invoke(new Float((float)Math.random()));
		Double numericValue = (Double) value;
		System.out.println(numericValue);
		vertx.eventBus().publish(name, new JsonObject().put("value", numericValue));
		
	}
	
}
