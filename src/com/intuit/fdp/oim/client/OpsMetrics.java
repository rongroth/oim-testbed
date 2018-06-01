package com.intuit.fdp.oim.client;

import java.util.HashMap;

/**
 * This should be used an instance per thread because of the latency measurements. 
 * The flow/method call counts are to be aggregated across all threads within a service. 
 * Thus, the singleton callCounts HashMap is shared.
 * 
 * @author rgroth
 *
 */
public class OpsMetrics {
	
	private final String OK_STATUS = "ok";
	private final String SUCCESS_STATUS = "success";
	
	private String componentName;
	private long startTime;
	/**
	 * Nature of callCounts HashMap<String, int[]>
	 * By using int[] for the integer counter, we can do a get (skipping contains()) and
	 * check for a null.
	 */
	private static HashMap<String, int[]> callCounts = new HashMap<String, int[]>();
	private CollectdUnixSockClient sock = new CollectdUnixSockClient();
	
	public OpsMetrics(String componentName) {
		this.componentName = componentName;
	}
	
	public void start(String methodName) {
		this.startTime = System.currentTimeMillis();
	}
	
	public void stop(String methodName, String status) {
		long currentTime = System.currentTimeMillis();
		sendLatency(methodName, status, currentTime, currentTime-this.startTime);
		sendCount(methodName, status);
		sendStatus(methodName, status);
	}

	private void sendLatency(String methodName, String status, long timestamp, long elapsedTime) {
		sock.sendLatency(componentName, methodName, status, timestamp, elapsedTime);
	}

	/**
	 * Thread safety discussion (or deciding against the need for synchronization)
	 * Using singleton instance of callCounts HashMap<String, int[]>.
	 * Because we aren't synchronizing two anomalies can happen:
	 * 1. multiple threads could get a null, create the int[]{1} and overwrite each other
	 *    on the put and lose the count of any number of those that have been overwritten.
	 * 2. multiple threads could get the same value of callCount and they could all
	 *    increment it before the count is sent, resulting in the same count being
	 *    sent multiple times.
	 * The first case will cause an initial under count of TPS and however many orphaned
	 * int[]{1} to garbage collect. Not a big deal.
	 * The second won't lose any call count, but since the same value show up in multiple
	 * rows in the time series data, it could skew the TPS temporarily. Again, not a big deal.
	 * Therefore, initially there will be no synchronization in order to minimally impact 
	 * the performance of our systems. rwg
	 */
	private void sendCount(String methodName, String status) {
		int[] callCount = callCounts.get(methodName);
		if(callCount == null) {
			callCount = new int[] {1};
			callCounts.put(methodName, callCount);
		} else {
			callCount[0]++;
		}
		sock.sendCount(componentName, methodName, status, callCount[0]);
	}
	
	private void sendStatus(String methodName, String status) {
		sock.sendStatus(componentName, methodName, status, translateStatus(status));
	}
	
	/**
	 * A successful status will be zero and an error will be 1. This will allow the sum
	 * of the values over time (the errors) to be divided by the total number of values 
	 * (all requests) to provide the percentage of errors.
	 * @param status
	 * @return
	 */
	private int translateStatus(String status) {
		if(status.toLowerCase().indexOf(OK_STATUS) >= 0)
			return 0;
		if(status.toLowerCase().indexOf(SUCCESS_STATUS) >= 0)
			return 0;
		return 1;
	}

}
