package edu.hsc.hypower.physicloud.util;

import io.vertx.core.shareddata.Shareable;

import java.util.ArrayList;
import java.util.HashMap;

import edu.hsc.hypower.physicloud.util.*;

public final class DataArray implements Shareable{

	private final ArrayList<DataTuple> dataArray;

	public DataArray(ArrayList<DataTuple> dataArr) {
		this.dataArray = dataArr;
	}


	public final ArrayList<DataTuple> getDataTuples() {
		return new ArrayList<DataTuple>(dataArray);
	}



	public static void main(String[] args) {

		DataTuple x = new DataTuple("this is a dataTuple", "please work");
		ArrayList<DataTuple> tupleList = new ArrayList<DataTuple>();
		tupleList.add(x);

		// Create an actual DataArray

		DataArray arr = new DataArray(tupleList);

		ArrayList<DataTuple> moreTesting = arr.getDataTuples();

		for(int i = 0; i < moreTesting.size(); i++)	{
			System.out.println(moreTesting.get(i).getType());
			System.out.println(moreTesting.get(i).getData());
		}

	}

}
