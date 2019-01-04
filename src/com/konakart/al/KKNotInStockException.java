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

/**
 * Exception thrown when a product is not in stock
 */
@SuppressWarnings("serial")
public class KKNotInStockException extends NestableException
{

    /**
     * Constructs a new <code>KKNotInStockException</code> without specified detail message.
     */
    public KKNotInStockException()
    {
    }

    /**
     * Constructs a new <code>KKNotInStockException</code> with specified detail message.
     * 
     * @param msg
     *            the error message.
     */
    public KKNotInStockException(String msg)
    {
        super(msg);
    }

    /**
     * Constructs a new <code>KKNotInStockException</code> with specified nested
     * <code>Throwable</code>.
     * 
     * @param nested
     *            the exception or error that caused this exception to be thrown.
     */
    public KKNotInStockException(Throwable nested)
    {
        super(nested);
    }

    /**
     * Constructs a new <code>KKNotInStockException</code> with specified detail message and
     * nested <code>Throwable</code>.
     * 
     * @param msg
     *            the error message.
     * @param nested
     *            the exception or error that caused this exception to be thrown.
     */
    public KKNotInStockException(String msg, Throwable nested)
    {
        super(msg, nested);
    }

}
