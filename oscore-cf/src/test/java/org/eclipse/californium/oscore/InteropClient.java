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

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.CoapEndpoint.CoapEndpointBuilder;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;

import COSE.AlgorithmID;

/**
 * OSCORE Client for interop testing
 * 
 * Following test spec:
 * https://ericssonresearch.github.com/OSCOAP/test-spec5.html
 * 
 * Author: Rikard Höglund
 *
 */
public class InteropClient {
	
	private final static HashMapCtxDB db = HashMapCtxDB.getInstance();
	private final static String baseUri = "coap://127.0.0.1";
	private final static AlgorithmID alg = AlgorithmID.AES_CCM_16_64_128;
	private final static AlgorithmID kdf = AlgorithmID.HKDF_HMAC_SHA_256;

	private final static byte[] master_secret = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B,
			0x0C, 0x0D, 0x0E, 0x0F, 0x10 };
	private final static byte[] master_salt = { (byte) 0x9e, (byte) 0x7c, (byte) 0xa9, (byte) 0x22, (byte) 0x23,
			(byte) 0x78, (byte) 0x63, (byte) 0x40 };
	private final static byte[] sid = new byte[0];
	private final static byte[] rid = new byte[] { 0x01 };
	
	private final static byte[] id_context_C = { (byte) 0x37, (byte) 0xcb, (byte) 0xf3, (byte) 0x21, (byte) 0x00, (byte) 0x17, (byte) 0xa2, (byte) 0xd3 };
	
	private static OSCoreCtx ctx_A;
	private static OSCoreCtx ctx_C;
	
	
	public static void main(String[] args) throws OSException {
		ctx_A = new OSCoreCtx(master_secret, true, alg, sid, rid, kdf, 32, master_salt, null);
		ctx_C = new OSCoreCtx(master_secret, true, alg, sid, rid, kdf, 32, master_salt, id_context_C);

		//Avoid CoAP retransmissions
		//(Should no longer be needed)
		NetworkConfig config = NetworkConfig.getStandard();
		config.setInt(NetworkConfig.Keys.MAX_RETRANSMIT, 0);
		
		
		TEST_1a_static();
	}

	/** --- Interop tests follow --- **/
	
	public static void TEST_0a() {
		String resourceUri = "/oscore/hello/coap";
		CoapClient c = new CoapClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);

		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	public static void TEST_1a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);

		String resourceUri = "/oscore/hello/1";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);

		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	public static void TEST_2a() throws OSException {
		db.addContext(baseUri, ctx_C);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/hello/1";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	public static void TEST_3a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/hello/2";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);
		r.getOptions().setUriQuery("first=1");
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.print("ETag:\t");
		for(int i = 0 ; i < resp.getOptions().getETagCount() ; i++)
			for(int n = 0 ; n < resp.getOptions().getETags().get(i).length ; n++)
				System.out.print(String.format("0x%02x", resp.getOptions().getETags().get(i)[n]));
		System.out.println("");
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}

	public static void TEST_4a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/hello/3";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);
		r.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Max-Age:\t" + String.format("0x%02x",resp.getOptions().getMaxAge()));
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	public static void TEST_5a() {
		
		System.out.println("Not implemented. No observe support.");
		
	}
	
	public static void TEST_6a() {
		
		System.out.println("Not implemented. No observe support.");
		
	}
	
	public static void TEST_7a() {
		
		System.out.println("Not implemented. No observe support.");
		
	}
	
	public static void TEST_8a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/hello/6";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.POST);
		r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		r.setPayload(new byte[] { 0x4a });
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.print("Payload:\t");
		for(int i = 0 ; i < r.getPayload().length ; i++)
			System.out.print(String.format("0x%02x", r.getPayload()[i]));
		System.out.println("");
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.print("Payload:\t");
		for(int i = 0 ; i < resp.getPayload().length ; i++)
			System.out.print(String.format("0x%02x", resp.getPayload()[i]));
		System.out.println("");
		
	}
	
	public static void TEST_9a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/hello/7";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.PUT);
		r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		r.getOptions().addIfMatch(new byte[] { 0x7b });
		r.setPayload(new byte[] { 0x7a });
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.print("Payload:\t");
		for(int i = 0 ; i < r.getPayload().length ; i++)
			System.out.print(String.format("0x%02x", r.getPayload()[i]));
		System.out.println("");
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.print("ETag:\t");
		for(int i = 0 ; i < resp.getOptions().getETagCount() ; i++)
			for(int n = 0 ; n < resp.getOptions().getETags().get(i).length ; n++)
				System.out.print(String.format("0x%02x", resp.getOptions().getETags().get(i)[n]));
		System.out.println("");
		System.out.print("Payload:\t");
		for(int i = 0 ; i < resp.getPayload().length ; i++)
			System.out.print(String.format("0x%02x", resp.getPayload()[i]));
		System.out.println("");
		
	}
	
	public static void TEST_10a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/hello/7";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.PUT);
		r.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		r.getOptions().setIfNoneMatch(true);
		r.setPayload(new byte[] { (byte) 0x8a });
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.print("Payload:\t");
		for(int i = 0 ; i < r.getPayload().length ; i++)
			System.out.print(String.format("0x%02x", r.getPayload()[i]));
		System.out.println("");
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		
	}
	
	public static void TEST_11a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/test";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.DELETE);
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		
	}
	
	public static void TEST_12a() throws OSException {
		byte[] sid_bad = new byte[] { (byte) 0xFF };
		OSCoreCtx ctx_A_bad = new OSCoreCtx(master_secret, true, alg, sid_bad, rid, kdf, 32, master_salt, null);
		db.addContext(baseUri, ctx_A_bad);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/hello/1";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	public static void TEST_13a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		byte[] sender_key_bad = { 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
				0x01, 0x01, 0x01, 0x01, 0x01 };
		db.getContext(baseUri).setSenderKey(sender_key_bad);
		
		String resourceUri = "/oscore/hello/1";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	public static void TEST_14a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		byte[] recipient_key_bad = { 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
				0x01, 0x01, 0x01, 0x01, 0x01 };
		db.getContext(baseUri).setRecipientKey(recipient_key_bad);
		
		String resourceUri = "/oscore/hello/1";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	public static void TEST_15a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		int senderSeqNumberBefore = db.getContext(baseUri).getSenderSeq();
		
		String resourceUri = "/oscore/hello/1";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);

		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
		//Reset the sender sequence number to before first transmission
		db.getContext(baseUri).setSenderSeq(senderSeqNumberBefore);
	
		//Send message again
		r = new Request(Code.GET);

		resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	//Run against server without OSCORE support
	public static void TEST_16a() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);
		
		String resourceUri = "/oscore/hello/coap";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);

		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	public static void TEST_17a() {
		String resourceUri = "/oscore/hello/1";
		CoapClient c = new CoapClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);

		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
	
	/** --- End of interop tests --- **/
	
	public static void TEST_1a_static() throws OSException {
		db.addContext(baseUri, ctx_A);
		Util.printOSCOREKeyInformation(db, baseUri);

		String resourceUri = "/oscore/hello/1";
		OscoreClient c = new OscoreClient(baseUri + resourceUri);
		Request r = new Request(Code.GET);
		
		//Set the CoAP and UDP level information to be static
		c.c.setTimeout((long)100 * 1000);
		CoapEndpointBuilder builder = new CoapEndpointBuilder();
		builder.setPort(50000); //Source port
		CoapEndpoint endp = builder.build();
		c.c.setEndpoint(endp);
		r.setMID(8);
		r.setToken(new byte[] { 0x22, 0x22, 0x22, 0x22, 0x22, 0x22, 0x22, 0x22});
		
		CoapResponse resp = c.advanced(r);
		
		System.out.println("Original CoAP message:");
		System.out.println("Uri-Path: " + c.getURI());
		System.out.println(Utils.prettyPrint(r));
		
		System.out.println("Parsed CoAP response: ");
		System.out.println("Response code:\t" + resp.getCode());
		System.out.println("Content-Format:\t" + resp.getOptions().getContentFormat());
		System.out.println("Payload:\t" + resp.getResponseText());
		
	}
	
//	private static void printResponse(CoapResponse resp) {
//		if (resp != null) {
//			System.out.println("RESPONSE CODE: " + resp.getCode().name() + " " + resp.getCode());
//			if (resp.getPayload() != null) {
//				System.out.print("RESPONSE PAYLOAD: ");
//				for (byte b : resp.getPayload()) {
//					System.out.print(Integer.toHexString(b & 0xff) + " ");
//				}
//				System.out.println();
//			}
//			System.out.println("RESPONSE TEXT: " + resp.getResponseText());
//		} else {
//			System.out.println("RESPONSE IS NULL");
//		}
//	}
		
	/**
	 * Separate class to handle an OSCORE client instance
	 * 
	 * @author segrid-2
	 *
	 */
	public static class OscoreClient
	{
		private String uri;
		private Code method;
		
		public CoapClient c;
		
		public OscoreClient(Code method, String uri)
		{	
			OSCoreCoapStackFactory.useAsDefault();
			c = new CoapClient(uri);
			
			this.method = method;
			this.uri = uri;
		}
		
		public OscoreClient(String uri)
		{
			this(null, uri);
		}
		
		public String getURI() {
			return uri;
		}
		
		/**
		 * Send a CoapRequest via OSCORE
		 * 
		 * @return Response to CoAP request
		 */
		CoapResponse send()
		{
			OSCoreCoapStackFactory.useAsDefault();
			CoapClient c = new CoapClient(uri);

			Request r = new Request(method);
			r.getOptions().setOscore(new byte[0]);
			CoapResponse resp = c.advanced(r);
			
			return resp;
		}
		
		/**
		 * Sends an arbitrary CoAP request using OSCORE
		 * 
		 * @return Response to CoAP request
		 */
		CoapResponse advanced(Request r)
		{
			if(!r.getOptions().hasOscore()) {
				r.getOptions().setOscore(new byte[0]);
			}
			CoapResponse resp = c.advanced(r);
			
			return resp;
			
		}

	}

}
