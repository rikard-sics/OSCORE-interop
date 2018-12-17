/*******************************************************************************
 * Copyright (c) 2018 RISE SICS and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Tobias Andersson (RISE SICS)
 *    Rikard Höglund (RISE SICS)
 *    
 ******************************************************************************/
package org.eclipse.californium.oscore;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.CoapEndpoint.CoapEndpointBuilder;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import java.util.Timer;
import java.util.TimerTask;

import COSE.AlgorithmID;

/** 
 * InteropServer for performing interop testing
 *
 * Following test spec:
 * https://ericssonresearch.github.com/OSCOAP/test-spec5.html
 *
 * Author: Rikard Höglund
 * 
 */
public class InteropServer {

	private final static HashMapCtxDB db = HashMapCtxDB.getInstance();
	private final static String uriLocal = "coap://localhost";
	private final static AlgorithmID alg = AlgorithmID.AES_CCM_16_64_128;
	private final static AlgorithmID kdf = AlgorithmID.HKDF_HMAC_SHA_256;

	private final static byte[] master_secret = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B,
			0x0C, 0x0D, 0x0E, 0x0F, 0x10 };
	private final static byte[] master_salt = { (byte) 0x9e, (byte) 0x7c, (byte) 0xa9, (byte) 0x22, (byte) 0x23,
			(byte) 0x78, (byte) 0x63, (byte) 0x40 };
	private final static byte[] sid = new byte[] { 0x01 };
	private final static byte[] rid = new byte[0];
	
	private final static byte[] id_context_D = { (byte) 0x37, (byte) 0xcb, (byte) 0xf3, (byte) 0x21, (byte) 0x00, (byte) 0x17, (byte) 0xa2, (byte) 0xd3 };
	
	public static void main(String[] args) throws OSException {
		OSCoreCtx ctx_B = new OSCoreCtx(master_secret, false, alg, sid, rid, kdf, 32, master_salt, null);
		OSCoreCtx ctx_D = new OSCoreCtx(master_secret, false, alg, sid, rid, kdf, 32, master_salt, id_context_D);

		db.addContext(uriLocal, ctx_B); //Change to CTX_D for TEST_2b
		Util.printOSCOREKeyInformation(db, uriLocal);
		
		OSCoreCoapStackFactory.useAsDefault();

		final CoapServer server = new CoapServer(5683);
		
		CoapEndpointBuilder builder = new CoapEndpointBuilder();
		InetAddress address = null;
		try {
			address = InetAddress.getByName("[aaaa::1]");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(address instanceof Inet6Address) {
			System.out.println("IPV6");
		}
		
		int port = 50000;
		InetSocketAddress socketAddress = new InetSocketAddress(address, port);
		builder.setInetSocketAddress(socketAddress);
		CoapEndpoint endp = builder.build();
		server.addEndpoint(endp);


		OSCoreResource hello = new OSCoreResource("hello", true) {

			@Override
			public void handleGET(CoapExchange exchange) {
				System.out.println("Accessing hello resource");
				Response r = new Response(ResponseCode.CONTENT);
				r.setPayload("Hello Resource");
				exchange.respond(r);
			}
		};

		OSCoreResource hello1 = new OSCoreResource("1", true) {

			@Override
			public void handleGET(CoapExchange exchange) {
				System.out.println("Accessing hello/1 resource");
				Response r = new Response(ResponseCode.CONTENT);
				r.setPayload("Hello World!");
				exchange.respond(r);
				server.destroy();
			}
		};

		server.add(hello.add(hello1));
		
		/** --- Resources for interop tests follow --- **/
		
		//Base resource for OSCORE interop test resources
		OSCoreResource oscore = new OSCoreResource("oscore", true) {

		};
		
		//Second level base resource for OSCORE interop test resources
		OSCoreResource oscore_hello = new OSCoreResource("hello", true) {

		};
		
		//CoAP resource for OSCORE interop tests
		CoapResource oscore_hello_coap = new CoapResource("coap", true) {

			@Override
			public void handleGET(CoapExchange exchange) {
				System.out.println("Accessing /oscore/hello/coap resource");
				System.out.println("Incoming parsed CoAP request: ");
				System.out.println(Utils.prettyPrint((exchange.advanced().getRequest())));
				
				Response r = new Response(ResponseCode.CONTENT);
				r.setPayload("Hello World!");
				r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
				
				//Utils.prettyPrint(r);
				
				exchange.respond(r);
				server.destroy();
			}
		};
		
		
		//1 resource for OSCORE interop tests
		OSCoreResource oscore_hello_1 = new OSCoreResource("1", true) {
			Timer timer;
			@Override
			public void handleGET(CoapExchange exchange) {
				System.out.println("Accessing /oscore/hello/1 resource");
				System.out.println("Incoming parsed CoAP request: ");
				System.out.println(Utils.prettyPrint((exchange.advanced().getRequest())));
				
				Response r = new Response(ResponseCode.CONTENT);
				r.setPayload("Hello World!");
				r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
				
				//System.out.println("Outgoing original CoAP response: ");
				//Utils.prettyPrint(r);
				
				exchange.respond(r);
				//server.destroy();
				
				//Destroy server after 2 seconds (has time for 2 messages in Test 15)
				if(timer == null) {
					timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							server.destroy();
							System.exit(0);
						}
					}, 2 * 1000);
				}
			}
		};
		
		//2 resource for OSCORE interop tests
		OSCoreResource oscore_hello_2 = new OSCoreResource("2", true) {

			@Override
			public void handleGET(CoapExchange exchange) {
				System.out.println("Accessing /oscore/hello/2 resource");
				System.out.println("Incoming parsed CoAP request: ");
				System.out.println(Utils.prettyPrint((exchange.advanced().getRequest())));
				
				Response r = new Response(ResponseCode.CONTENT);
				r.setPayload("Hello World!");
				r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
				r.getOptions().getETags().add(new byte[] { 0x2b });
				
				//System.out.println("Outgoing original CoAP response: ");
				//Utils.prettyPrint(r);
				
				exchange.respond(r);
				server.destroy();
			}
		};
		
		//3 resource for OSCORE interop tests
		OSCoreResource oscore_hello_3 = new OSCoreResource("3", true) {

			@Override
			public void handleGET(CoapExchange exchange) {
				System.out.println("Accessing /oscore/hello/3 resource");
				System.out.println("Incoming parsed CoAP request: ");
				System.out.println(Utils.prettyPrint((exchange.advanced().getRequest())));
				
				Response r = new Response(ResponseCode.CONTENT);
				r.setPayload("Hello World!");
				r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
				r.getOptions().setMaxAge(5);
				
				//System.out.println("Outgoing original CoAP response: ");
				//Utils.prettyPrint(r);
				
				exchange.respond(r);
				server.destroy();
			}
		};
		
		//6 resource for OSCORE interop tests
		OSCoreResource oscore_hello_6 = new OSCoreResource("6", true) {

			private byte[] resourceValue;
			
			@Override
			public void handlePOST(CoapExchange exchange) {
				System.out.println("Accessing /oscore/hello/6 resource");
				System.out.println("Incoming parsed CoAP request: ");
				System.out.print("Payload:\t");
				for(int i = 0 ; i < exchange.getRequestPayload().length ; i++)
					System.out.print(String.format("0x%02x", exchange.getRequestPayload()[i]));
				System.out.println("");
				System.out.println(Utils.prettyPrint((exchange.advanced().getRequest())));
				
				resourceValue = exchange.getRequestPayload();
				
				Response r = new Response(ResponseCode.CHANGED);
				r.setPayload(resourceValue);
				r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
				
				//System.out.println("Outgoing original CoAP response: ");
				//Utils.prettyPrint(r);
				
				exchange.respond(r);
				server.destroy();
			}
		};
		
		//7 resource for OSCORE interop tests
		OSCoreResource oscore_hello_7 = new OSCoreResource("7", true) {

			private byte[] resourceValue;
			
			@Override
			public void handlePUT(CoapExchange exchange) {
				System.out.println("Accessing /oscore/hello/7 resource");
				System.out.println("Incoming parsed CoAP request: ");
				System.out.print("Payload:\t");
				for(int i = 0 ; i < exchange.getRequestPayload().length ; i++)
					System.out.print(String.format("0x%02x", exchange.getRequestPayload()[i]));
				System.out.println("");
				System.out.println(Utils.prettyPrint((exchange.advanced().getRequest())));
				
				//Check if ETag matches or if "If-None-Match" is set
				boolean valid = false;
				byte validETag = 0x7b;
				List<byte[]> ifMatchValues = exchange.advanced().getRequest().getOptions().getIfMatch();
				for(int i = 0 ; i < ifMatchValues.size() ; i++)
					if(ifMatchValues.get(i).length == 1 && ifMatchValues.get(i)[0] == validETag)
						valid = true;
				if(exchange.advanced().getRequest().getOptions().hasIfNoneMatch())
					valid = false;
				
				System.out.println("If-(None)-Match valid: " + valid);
				
				//Create response depending on validity
				Response r = new Response(ResponseCode.PRECONDITION_FAILED);
				if(valid) {
					resourceValue = exchange.getRequestPayload();
				
					r = new Response(ResponseCode.CHANGED);
					r.setPayload(resourceValue);
					r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
					r.getOptions().getETags().add(new byte[] { validETag });
				}
				
				//System.out.println("Outgoing original CoAP response: ");
				//Utils.prettyPrint(r);
				
				exchange.respond(r);
				server.destroy();
			}
		};
		
		//test resource for OSCORE interop tests
		OSCoreResource oscore_test = new OSCoreResource("test", true) {

			@Override
			public void handleDELETE(CoapExchange exchange) {
				System.out.println("Accessing /oscore/test resource");
				System.out.println("Incoming parsed CoAP request: ");
				System.out.println(Utils.prettyPrint((exchange.advanced().getRequest())));
				
				Response r = new Response(ResponseCode.DELETED);
				
				//System.out.println("Outgoing original CoAP response: ");
				//Utils.prettyPrint(r);
				
				exchange.respond(r);
				server.destroy();
			}
		};
		
		//Creating resource hierarchy
		oscore_hello.add(oscore_hello_coap);
		
		oscore_hello.add(oscore_hello_1);
		oscore_hello.add(oscore_hello_2);
		oscore_hello.add(oscore_hello_3);
		oscore_hello.add(oscore_hello_6);
		oscore_hello.add(oscore_hello_7);
		
		oscore.add(oscore_hello);
		oscore.add(oscore_test);
		
		server.add(oscore);
		
		/** --- End of resources for interop tests **/
		
		server.start();
	}
}
