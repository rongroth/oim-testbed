package com.intuit.fdp.oim.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.concurrent.TimeUnit;

import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

public class CollectdUnixSockClient {
	
	/**
	 * Identifier from Collectd: host/plugin-instance/type-instance
	 * 	we are going to use:
	 * 		host -> componentName (likely the service name)
	 * 		plugin -> "method" (literal)
	 * 		instance -> methodName
	 * 		type -> TBD
	 * 		instance -> [success, error]
	 * 	examples: 
	 * 		- fpo/method-acquire/dataType-success
	 * 		- fpo/method-delete/dataType-error
	 * 		- fdi/method-search/dataType-success
	 * 		- fdi/method-search/dataType-error
	 * 
	 * For RATE (TPS) - 	PUTVAL serviceName/method-methodName/counter-all interval=10 N:requestCount
	 * for Success		PUTVAL serviceName/method-methodName/counter-success interval=10 N:successRequestCount
	 * for Error			PUTVAL serviceName/method-methodName/counter-error interval=10 N:errorRequestCount
	 * 
	 * for latency		PUTVAL serviceName/method-methodName/latency-[error,success] N:milliseconds
	 * */
	private String putValLatency = "PUTVAL %s/method-%s/latency-%s N:%d\n";
	private String putValCount = "PUTVAL %s/method-%s/count-%s N:%d\n";
	private String putValStatus = "PUTVAL %s/method-%s/response_code-%s N:%d\n";
	
	private java.io.File path = null;
	private UnixSocketAddress address = null;
	private UnixSocketChannel channel = null;
	private PrintWriter w = null;
	
	public CollectdUnixSockClient() {
		try {
			initialize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initialize() throws IOException {
		if(checkUnixSockPath()) {
	        address = new UnixSocketAddress(path);
	        channel = UnixSocketChannel.open(address);			
	        System.out.println("connected to " + channel.getRemoteSocketAddress());
	        w = new PrintWriter(Channels.newOutputStream(channel));
		}
	}
	
	private boolean checkUnixSockPath() throws IOException {
        path = new java.io.File("/usr/local/var/run/collectd-unixsock");
        int retries = 0;
        while (!path.exists()) {
            try {
				TimeUnit.MILLISECONDS.sleep(500L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            retries++;
            if (retries > 10) {
                throw new IOException(
                    String.format(
                        "File %s does not exist after retry",
                        path.getAbsolutePath()
                    )
                );
            }
        }
        
        return true;
 	}

	public void sendLatency(String componentName, String methodName, String status, long timestamp, long elapsedTime) {
		String data = String.format(putValLatency, componentName, methodName, status, elapsedTime);
		send(data);
	}
	
	public void sendCount(String componentName, String methodName, String status, int callCount) {
		String data = String.format(putValCount, componentName, methodName, status, callCount);
		send(data);
	}
	
	public void sendStatus(String componentName, String methodName, String status, int intStatus) {
		String data = String.format(putValStatus, componentName, methodName, status, intStatus);
		send(data);
	}
	
	private void send(String data) {
		ByteBuffer txbb = ByteBuffer.wrap(data.getBytes());
        try {
			channel.write(txbb);
			System.out.println("written to server: " + data);
	        ByteBuffer rxbb = ByteBuffer.allocate(1024);
	        channel.read(rxbb);
	        String rxs = new String(rxbb.array());
	        System.out.println("read from server: " + rxs);
	        if(rxs.indexOf("Success") < 0)
	        {
	        		IOException e = new IOException("Error from UnixSock - " + rxs);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
