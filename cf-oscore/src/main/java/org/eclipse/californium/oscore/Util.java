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
 *    Rikard Höglund (RISE SICS)
 *    
 ******************************************************************************/
package org.eclipse.californium.oscore;

import javax.xml.bind.DatatypeConverter;

/**
 * 
 * Support functions for debugging and interop client/server.
 * TODO: Use org.eclipse.californium.core.Utils.toHexText() instead?
 *
 */
public class Util {
	/*
	 * Method for printing a byte array as hexadecimal
	 * 
	 * 
	 */
	public static String arrayToString(byte[] array) {
		StringBuilder s = new StringBuilder();
		
		if(array == null) {
			s.append("null");
			return s.toString();
		}

		s.append(DatatypeConverter.printHexBinary(array));
		s.append(" (" + array.length + " bytes)");
		
		return s.toString();
	}
	
	/*
	 * Method for printing the currently used key information.
	 * It is extracted from the OSCORE context in the database
	 * 
	 * 
	 */
	public static void printOSCOREKeyInformation(HashMapCtxDB db, String baseUri) {
		byte[] master_secret, master_salt, common_iv, id_context;
		byte[] sender_id, sender_key;
		int sender_seq_number;
		byte[] recipient_id, recipient_key;	
		
		try {
			//Common context
			master_secret = db.getContext(baseUri).getMasterSecret();
			master_salt = db.getContext(baseUri).getSalt();
			common_iv = db.getContext(baseUri).getCommonIV();
			id_context = db.getContext(baseUri).getIdContext();
			
			//Sender context
			sender_id = db.getContext(baseUri).getSenderId();
			sender_key = db.getContext(baseUri).getSenderKey();
			sender_seq_number = db.getContext(baseUri).getSenderSeq();
			//sender_iv;
			
			//Recipient context
			recipient_id = db.getContext(baseUri).getRecipientId();
			recipient_key = db.getContext(baseUri).getRecipientKey();
			//recipient_iv;
		} catch (OSException e) {
			System.out.println("Error retrieving OSCORE context!");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Common Context:");
		System.out.print("\tMaster Secret: ");
		System.out.println(arrayToString(master_secret));
		System.out.print("\tMaster Salt: ");
		System.out.println(arrayToString(master_salt));
		System.out.print("\tCommon IV: ");
		System.out.println(arrayToString(common_iv));
		System.out.print("\tID Context: ");
		System.out.println(arrayToString(id_context));
		
		System.out.println("Sender Context:");
		System.out.print("\tSender ID: ");
		System.out.println(arrayToString(sender_id));
		System.out.print("\tSender Key: ");
		System.out.println(arrayToString(sender_key));
		System.out.print("\tSender Seq Number: ");
		System.out.println(sender_seq_number);
		
		System.out.println("Recipient Context: ");
		System.out.print("\tRecipient ID: ");
		System.out.println(arrayToString(recipient_id));
		System.out.print("\tRecipient Key: ");
		System.out.println(arrayToString(recipient_key));
		
	}
}
