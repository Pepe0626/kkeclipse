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
package com.konakart.al.json;

/**
 * Simplified Order Object to send required data back to the browser in JSON format
 */
public class OrderJson
{

    /**
     * storeId
     */
    private String storeId;
    
    /**
     * Vendor Orders
     */
    private OrderJson[] vendorOrders;

    /**
     * orderProducts
     */
    private OrderProductJson[] orderProducts;
    
    /**
     * orderTotals
     */
    private OrderTotalJson[] orderTotals;
    
    /**
     * shippingQuote
     */
    private ShippingQuoteJson shippingQuote;

    /**
     * Constructor
     */
    public OrderJson()
    {
    }


    /**
     * @return the storeId
     */
    public String getStoreId()
    {
        return storeId;
    }


    /**
     * @param storeId the storeId to set
     */
    public void setStoreId(String storeId)
    {
        this.storeId = storeId;
    }


    /**
     * @return the orderProducts
     */
    public OrderProductJson[] getOrderProducts()
    {
        return orderProducts;
    }


    /**
     * @param orderProducts the orderProducts to set
     */
    public void setOrderProducts(OrderProductJson[] orderProducts)
    {
        this.orderProducts = orderProducts;
    }


    /**
     * @return the orderTotals
     */
    public OrderTotalJson[] getOrderTotals()
    {
        return orderTotals;
    }


    /**
     * @param orderTotals the orderTotals to set
     */
    public void setOrderTotals(OrderTotalJson[] orderTotals)
    {
        this.orderTotals = orderTotals;
    }


    /**
     * @return the vendorOrders
     */
    public OrderJson[] getVendorOrders()
    {
        return vendorOrders;
    }


    /**
     * @param vendorOrders the vendorOrders to set
     */
    public void setVendorOrders(OrderJson[] vendorOrders)
    {
        this.vendorOrders = vendorOrders;
    }


    /**
     * @return the shippingQuote
     */
    public ShippingQuoteJson getShippingQuote()
    {
        return shippingQuote;
    }


    /**
     * @param shippingQuote the shippingQuote to set
     */
    public void setShippingQuote(ShippingQuoteJson shippingQuote)
    {
        this.shippingQuote = shippingQuote;
    }

}
