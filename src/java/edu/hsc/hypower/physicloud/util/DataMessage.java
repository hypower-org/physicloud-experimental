package edu.hsc.hypower.physicloud.util;

import java.util.ArrayList;

/**
 * A class that holds a list of DataTuples. One packages PhysiCloud data using
 * these tuples.
 * 
 * Example: GPS has two floats, lat and long. Then a receiving task would declare
 * the need for the resource: (float, float) gps. The tupleList will hold two elements
 * satisfying this data need.
 * 
 * @author hackleyb18@hsc.edu
 *
 */
public class DataMessage {

	private final String id;
	private final ArrayList<DataTuple> tupleList;

	public DataMessage(String n, ArrayList<DataTuple> x){
		id = n;
		tupleList = x;
	}
	
	public String getId() {
		return id;
	}
	
	public ArrayList<DataTuple> getList()	{
		return tupleList;
	}
	
	public int getLength()	{
		
		return tupleList.size();
	}
}
