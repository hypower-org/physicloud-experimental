package edu.hsc.hypower;

import clojure.lang.Compiler;
import clojure.lang.IFn;

import java.io.StringReader;
import java.util.ArrayList;

import clojure.java.api.*;

public class ClojureInJavaTest {

	public static void main(String[] args){
		String testFuncStr = "(ns test-ns) (defn add-one [n] (+ 1 n))";
		String computeAvg = "(ns test-ns) (defn compute-avg [coll] (let [nitems (count coll)] (float (/ (reduce + 0 coll) nitems))))";
		// Need this function to load the string into the clojure environment.
		Clojure.read(testFuncStr);
		Clojure.read(computeAvg);
		// Binds the function name 
		Compiler.load(new StringReader(testFuncStr));
		Compiler.load(new StringReader(computeAvg));
		
		IFn addOneFn = Clojure.var("test-ns", "add-one");
		IFn computeAvgFn = Clojure.var("test-ns", "compute-avg");
//		for(int i = 0; i < 20; i++){
//			long s = System.currentTimeMillis();
//			System.out.println(addOneFn.invoke(20));
//			System.out.println(System.currentTimeMillis() - s);
//		}
		ArrayList<Long> times = new ArrayList<Long>();
		for(int i = 0; i < 100; i++){
			long s = System.nanoTime();
			Object result = computeAvgFn.invoke(Clojure.read("[11 45 5 6 7 8 9 10 90 89]"));
			long totalT = (System.nanoTime() - s);
//			System.out.println("Time to exec in ns: " + totalT);
//			System.out.println("average: " +  result);
			times.add(totalT);
		}
		long allTime = times.stream().reduce((a,b) -> a + b).get();
		System.out.println("Over set of " + times.size() + " items...");
		System.out.println("average execution in ns: " + (float) (allTime /times.size() )); 
		
	}
	
}
