package edu.hsc.hypower;


import java.util.ArrayList;


import edu.hsc.hypower.physicloud.util.DataMessage;
import edu.hsc.hypower.physicloud.util.DataMessageCodec;
import edu.hsc.hypower.physicloud.util.DataTuple;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

public class CodecTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Vertx v = Vertx.vertx();
		v.eventBus().registerDefaultCodec(DataMessage.class, new DataMessageCodec());
		v.setPeriodic(1000, new Handler<Long>(){

			@Override
			public void handle(Long event) {
				
				DataTuple x = new DataTuple(new Float(500));
				ArrayList<DataTuple> tupleArr = new ArrayList<DataTuple>();
				tupleArr.add(x);
				DataMessage m = new DataMessage("id", tupleArr);
				v.eventBus().publish("1010101", m);
			}
			
		});
		
		v.eventBus().consumer("1010101", new Handler<Message<DataMessage>>(){

			@Override
			public void handle(Message<DataMessage> event) {
				// TODO Auto-generated method stub
				DataMessage x = event.body();
				System.out.println(x.getId());
				
			}
			
		});
		
		
		
		
	}

}
