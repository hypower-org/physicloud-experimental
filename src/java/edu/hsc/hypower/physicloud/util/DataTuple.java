package edu.hsc.hypower.physicloud.util;

public class DataTuple {
	
	private final String type;
	private final String data;
	
	public DataTuple(String t, String d){
		type = t;
		data = d;
	}
	
	public DataTuple(Float f){
		type = f.getClass().getSimpleName();
		data = f.toString();
		
	}
	
	public DataTuple(Double d){
		type = d.getClass().getSimpleName();
		data = d.toString();
	}
	
	public DataTuple(Integer i){
		type = i.getClass().getSimpleName();
		data = i.toString();
	}
	
	public DataTuple(Boolean b){
		type = b.getClass().getSimpleName();
		data = b.toString();
	}
	
	public DataTuple(String s){
		type = "String";
		data = s;
	}
	
	public final String getType(){
		return this.type;
	}
	
	public final String getData(){
		return this.data;
	}
	
	@Override
	public final String toString() {
		String sTup = "<" + this.type + "," + this.data + ">";
		return sTup;
	}

	public final DataTuple fromString(String s){
	
		String[] parseTuple = s.substring(1,s.length()-1).split(",");
		System.out.println(parseTuple[0]);
		DataTuple tup = new DataTuple(parseTuple[0], parseTuple[1]);
		return tup;
		
	}
	
	

}
