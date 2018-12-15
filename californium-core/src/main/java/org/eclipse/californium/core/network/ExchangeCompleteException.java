/*******************************************************************************
 * Copyright (c) 2018 Bosch Software Innovations GmbH and others.
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
 *    Bosch Software Innovations GmbH - initial implementation
 ******************************************************************************/
package org.eclipse.californium.core.network;

/**
 * Exception indicating, that the exchange is already complete.
 */
public class ExchangeCompleteException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	private void printExceptionCreated() {
		//Added further debug prints on creation of exceptions TODO: Remove //Rikard
		String exceptionName = this.getClass().toString();
		String methodName = this.getStackTrace()[0].getMethodName();
		String fileName = this.getStackTrace()[0].getFileName();
		int lineNumber = this.getStackTrace()[0].getLineNumber();
		System.err.println("Warning: Exception " + exceptionName  + " (" + this.getLocalizedMessage() +
				")" + " in method " + methodName + " at (" + fileName + ":" + lineNumber + ")");
	}

	/**
	 * Create new instance with message.
	 * 
	 * @param message message
	 * @param caller caller of {@link Exchange#setComplete()}
	 */
	public ExchangeCompleteException(String message, Throwable caller) {
		super(message, caller);
		printExceptionCreated();
	}
}
