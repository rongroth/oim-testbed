package com.intuit.fdp.oim.client;

import java.util.concurrent.ThreadLocalRandom;

public class OimClientTest {
	
	final int NUM_RANDOM_METHODS = 3;
	final int NUM_THREADS = 20;
	
	public OimClientTest() {
	}
	
	void doRandomWork() {
		
		System.out.println("in doRandomWork()");
		
		for(int i = 0; i<NUM_THREADS; i++) {
			System.out.println("for loop:" + i);
			new Thread() {
				
				private OpsMetrics om = new OpsMetrics("OimClientTest-" + this.getName());
				
				public void run() {
					while(true) {
						// create a random value
						int randomMethodValue = randomInt(0, NUM_RANDOM_METHODS);
						
						switch(randomMethodValue) {
						case(0) :
							method0();
							break;
						case(1):
							method1();
							break;
						case(2) :
							method2();
							break;
						default:
							break;
						}
					}
				}
				
				private void method0() {
					om.start("method0");
					randomSleep(200, 300);
					om.stop("method0", randomStatus(2));
				}
				
				private void method1() {
					om.start("method1");
					randomSleep(500, 800);
					om.stop("method1", randomStatus(6));
				}
				
				private void method2() {
					om.start("method2");
					randomSleep(50,80);
					om.stop("method2", randomStatus(4));
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
				
				private int randomInt(int min, int max) {
					return ThreadLocalRandom.current().nextInt(min, max);
				}
				
			}.start();
		}
	}
	
	
	

	public static void main(String[] args) {
		
		OimClientTest client = new OimClientTest();

		client.doRandomWork();
	}

}
