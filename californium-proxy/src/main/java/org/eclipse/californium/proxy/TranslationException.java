/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
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
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and re-implementation
 *    Francesco Corazza - HTTP cross-proxy
 ******************************************************************************/
package org.eclipse.californium.proxy;

import java.io.IOException;


/**
 * The Class TranslationException.
 */
public class TranslationException extends Exception {
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

	public TranslationException() {
		super();
		printExceptionCreated();

	}

	public TranslationException(String message) {
		super(message);
		printExceptionCreated();
	}

	public TranslationException(String string, IOException e) {
		super(string, e);
		printExceptionCreated();
	}

	public TranslationException(String message, Throwable cause) {
		super(message, cause);
		printExceptionCreated();
	}

	public TranslationException(Throwable cause) {
		super(cause);
		printExceptionCreated();
	}
}
