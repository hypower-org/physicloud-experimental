package edu.hsc.hypower.physicloud.hw;

public class IkitConverter {
	
	//Temperature
	
	public float rawToTemp(int rawValue){
		float temp = (float) ((rawValue * 0.22222) - 61.11);
		return temp;
	}
	
	//Humidity
	
	public float rawToHumidity(int rawValue){
		float humid = (float) ((rawValue * 0.1906) - 40.2);
		return humid;
	}
	
	//Magnetic
	
	public float magnetToGauss(int rawValue){
		float gauss = 500 - rawValue;
		return gauss;
	}
	

}
