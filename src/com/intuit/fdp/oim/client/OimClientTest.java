package com.intuit.fdp.oim.client;

import java.util.concurrent.ThreadLocalRandom;

public class OimClientTest {
	
	final int NUM_RANDOM_METHODS = 3;
	final int NUM_INTERATIONS = 1000;
	private OpsMetrics om;
	
	public OimClientTest() {
		om = new OpsMetrics("OimClientTest");
	}
	
	private int randomInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max);
	}
	
	void doRandomWork() {
		
		System.out.println("in doRandomWork()");
		
		int error = 0;
		int randomMethodValue = 0;
		for(int i = 0; i<NUM_INTERATIONS; i++) {
			
			// create a random value
			randomMethodValue = randomInt(0, NUM_RANDOM_METHODS);
			
			switch(randomMethodValue) {
			case(0) :
				error = method0();
				break;
			case(1):
				error = method1();
				break;
			case(2) :
				error = method2();
				break;
			default:
				break;
			}
		}
	}
	
	private int method0() {
		om.start("method0");
		randomSleep(100, 300);
		om.stop("method0", randomStatus(2));
		return 0;
	}
	
	private int method1() {
		om.start("method1");
		randomSleep(500, 800);
		om.stop("method1", randomStatus(6));
		return 0;
	}
	
	private int method2() {
		om.start("method2");
		randomSleep(20,80);
		om.stop("method2", randomStatus(23));
		return 0;
	}

	final private String[] status = {
		"FDP-103",
		"FDP-185",
		"FDP-352"
	};

	private String randomStatus(int errorPercent) {
		int percent = randomInt(0,100);
		if(errorPercent > percent) {
			return status[randomInt(0,status.length-1)];
		}
		
		return "success";
	}

	private void randomSleep(int min, int max) {
		try {
			Thread.sleep(randomInt(min, max));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	

	public static void main(String[] args) {
		
		OimClientTest client = new OimClientTest();

		while(true) {
			client.doRandomWork();
		}
	}

}
