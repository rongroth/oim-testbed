package com.intuit.fdp.oim.client;

public class ThreadTest {

	public static void main(String[] args) {
		for(int i=0; i<10; i++) {
			new Thread() {
				public void run() {
					System.out.println(this.getName());
					while(true);
				}
			}.start();
		}
	}

}
