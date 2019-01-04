//
// (c) 2013 DS Data Systems UK Ltd, All rights reserved.
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

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.KKException;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.ShippingQuoteIf;

/**
 * Thread which runs to return a shipping quote
 */
public class ShippingQuoteFetcher extends Thread
{
    /**
     * The <code>Log</code> instance for this class.
     */
    private Log log = LogFactory.getLog(ShippingQuoteFetcher.class);

    /**
     * Engine Instance
     */
    private KKEngIf eng = null;

    /**
     * The order
     */
    private OrderIf order;

    /**
     * language id
     */
    private int languageId;

    /**
     * Hash map where to place the result
     */
    HashMap<String, ShippingQuoteIf[]> vendorShippingQuoteMap;

    /**
     * Constructor
     * 
     * @param _eng
     *            the _eng
     * @param _order
     *            the _order
     * @param _vendorShippingQuoteMap
     *            the _vendorShippingQuoteMap
     * @param _languageId
     *            the _languageId
     * 
     * 
     */
    public ShippingQuoteFetcher(KKEngIf _eng, OrderIf _order,
            HashMap<String, ShippingQuoteIf[]> _vendorShippingQuoteMap, int _languageId)
    {
        eng = _eng;
        order = _order;
        languageId = _languageId;
        vendorShippingQuoteMap = _vendorShippingQuoteMap;
    }

    /**
     * Fetch the shipping quotes
     */
    public void run()
    {
        try
        {
            ShippingQuoteIf[] vendorQuotes = eng.getShippingQuotes(order, languageId);
            vendorShippingQuoteMap.put(order.getStoreId(), vendorQuotes);
        } catch (KKException e)
        {
            log.error(
                    "Error encountered in thread fetching shipping quotes for order in store id = "
                            + order.getStoreId(), e);
        }
    }

}
