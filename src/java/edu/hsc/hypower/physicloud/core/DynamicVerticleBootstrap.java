package edu.hsc.hypower.physicloud.core;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class DynamicVerticleBootstrap {

	public static void main(String[] args){
		
		VertxOptions opts = new VertxOptions();
		opts.setWorkerPoolSize(Runtime.getRuntime().availableProcessors());
		
		Vertx mainVtx = Vertx.factory.vertx();
		mainVtx.eventBus().consumer("incoming", msg -> { System.out.println("Received " + msg.body());});

		String testFuncStr = "(defn add-one [n] (+ 1 n))";
		mainVtx.deployVerticle(new DynamicVerticle("add-one", testFuncStr));
		
	}

}
