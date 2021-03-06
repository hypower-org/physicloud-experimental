package edu.hsc.hypower.physicloud.util;

import java.util.ArrayList;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DataMessageCodec implements MessageCodec<DataMessage, DataMessage>{

	@Override								
	public void encodeToWire(Buffer buffer, DataMessage msg) {
				
		JsonObject jsonToEncode = new JsonObject();

		jsonToEncode.put("id", msg.getId());
		JsonArray tupleArray = new JsonArray();

		ArrayList<DataTuple> tupleList = msg.getTupleList();

		for(int i = 0; i < tupleList.size(); i++) {
			tupleArray.add(tupleList.get(i).toString());
		}

		jsonToEncode.put("data", tupleArray);

		// Encode object to string
		String jsonToStr = jsonToEncode.encode();

		// Append length and data to buffer
		int length = jsonToStr.getBytes().length;
		buffer.appendInt(length);
		buffer.appendString(jsonToStr);
	}


	@Override
	public DataMessage decodeFromWire(int position, Buffer buffer) {
		
		int pos = position;
		int length = buffer.getInt(pos);

		String jsonStr = buffer.getString(pos+=4, pos+=length);
		JsonObject contentJson = new JsonObject(jsonStr);

		String id = contentJson.getString("id");
		JsonArray data = contentJson.getJsonArray("data");
		ArrayList<DataTuple> forMessage = new ArrayList<DataTuple>();

		for(int i = 0; i < data.size(); i++)	{
			String tupleData = data.getString(i);
			forMessage.add(DataTuple.fromString(tupleData));
		}

		return new DataMessage(id, forMessage);

	}

	@Override
	public String name() {

		return this.getClass().getSimpleName();
	}

	@Override
	public byte systemCodecID() {

		return -1;
	}


	@Override
	public DataMessage transform(DataMessage s) {
		return s;
	}

}
