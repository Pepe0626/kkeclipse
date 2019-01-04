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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.KKException;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;

/**
 * Thread which runs to return order totals for a multi-vendor order
 */
public class OrderTotalFetcher extends Thread
{
    /**
     * The <code>Log</code> instance for this class.
     */
    private Log log = LogFactory.getLog(OrderTotalFetcher.class);

    /**
     * Engine Instance
     */
    private KKEngIf eng = null;

    /**
     * The parent order
     */
    private OrderIf parentOrder;

    /**
     * language id
     */
    private int languageId;

    /**
     * Index of the order in the array of vendor orders
     */
    int orderIndex;

    /**
     * Constructor
     * 
     * @param _eng
     *            the _eng
     * @param _parentOrder
     *            the _parentOrder
     * @param _orderIndex
     *            the _orderIndex
     * @param _languageId
     *            the _languageId
     * 
     */
    public OrderTotalFetcher(KKEngIf _eng, OrderIf _parentOrder, int _orderIndex, int _languageId)
    {
        eng = _eng;
        parentOrder = _parentOrder;
        orderIndex = _orderIndex;
        languageId = _languageId;
    }

    /**
     * Fetch the order totals
     */
    public void run()
    {
        try
        {
            OrderIf vendorOrder = parentOrder.getVendorOrders()[orderIndex];
            vendorOrder = eng.getOrderTotals(vendorOrder, languageId);
            parentOrder.getVendorOrders()[orderIndex] = vendorOrder;
        } catch (KKException e)
        {
            log.error("Error encountered in thread fetching order totals for order in store id = "
                    + parentOrder.getStoreId(), e);
        }
    }

}
