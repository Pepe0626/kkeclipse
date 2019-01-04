//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is the proprietary property of
// DS Data Systems UK Ltd. and is protected by English copyright law,
// the laws of foreign jurisdictions, and international treaties,
// as applicable. No part of this document may be reproduced,
// transmitted, transcribed, transferred, modified, published, or
// translated into any language, in any form or by any means, for
// any purpose other than expressly permitted by DS Data Systems UK Ltd.
// in writing.
//
package com.konakart.al;

import org.apache.commons.lang.exception.NestableException;

import com.konakart.KonaKartVersion;

/**
 * Exception thrown by the KonaKart client engine
 */
@SuppressWarnings("serial")
public class KKAppException extends NestableException {
	
    /**
     * Constructs a new <code>KKAppException</code> without specified detail
     * message.
     */
    public KKAppException()
    {
    }

    /**
     * Constructs a new <code>KKAppException</code> with specified detail
     * message.
     *
     * @param msg the error message.
     */
    public KKAppException(String msg)
    {
        super(msg + " [" + KonaKartVersion.KKAPP_VERSION_NUMBER + "]");
    }

    /**
     * Constructs a new <code>KKAppException</code> with specified nested
     * <code>Throwable</code>.
     *
     * @param nested the exception or error that caused this exception
     *               to be thrown.
     */
    public KKAppException(Throwable nested)
    {
        super(nested);
    }

    /**
     * Constructs a new <code>KKAppException</code> with specified detail
     * message and nested <code>Throwable</code>.
     *
     * @param msg the error message.
     * @param nested the exception or error that caused this exception
     *               to be thrown.
     */
    public KKAppException(String msg, Throwable nested)
    {
        super(msg + " [" + KonaKartVersion.KKAPP_VERSION_NUMBER + "]", nested);
    }
}
