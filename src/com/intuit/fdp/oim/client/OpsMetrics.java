package com.intuit.fdp.oim.client;

public class OpsMetrics {
	
	private String componentName;
	private long startTime;
	
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
	}

	private void sendLatency(String methodName, String status, long timestamp, long elapsedTime) {
		CollectdUnixSockClient.getInstance().sendLatency(componentName, methodName, status, timestamp, elapsedTime);
	}

	private void sendCount(String methodName, String status) {
		CollectdUnixSockClient.getInstance().sendCount(componentName, methodName, status);
	}

}
