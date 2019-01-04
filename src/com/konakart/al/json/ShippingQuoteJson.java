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
 * SimplifiedShipping Quote Object to send required data back to the browser in JSON format
 */
public class ShippingQuoteJson
{
    /**
     * formattedTotalIncTax
     */
    private String formattedTotalIncTax;

    /**
     * formattedTotalExTax
     */
    private String formattedTotalExTax;
    
    /**
     * Title of quote
     */
    private String title;

    /**
     * Constructor
     */
    public ShippingQuoteJson()
    {
    }

    /**
     * @return the formattedTotalIncTax
     */
    public String getFormattedTotalIncTax()
    {
        return formattedTotalIncTax;
    }

    /**
     * @param formattedTotalIncTax
     *            the formattedTotalIncTax to set
     */
    public void setFormattedTotalIncTax(String formattedTotalIncTax)
    {
        this.formattedTotalIncTax = formattedTotalIncTax;
    }

    /**
     * @return the formattedTotalExTax
     */
    public String getFormattedTotalExTax()
    {
        return formattedTotalExTax;
    }

    /**
     * @param formattedTotalExTax
     *            the formattedTotalExTax to set
     */
    public void setFormattedTotalExTax(String formattedTotalExTax)
    {
        this.formattedTotalExTax = formattedTotalExTax;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
}
