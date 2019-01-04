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
 * Simplified Order Product Object to send required data back to the browser in JSON format
 */
public class OrderProductJson
{

    /**
     * name
     */
    private String name;
    
    /**
     * quantity
     */
    private int quantity;

    /**
     * finalPriceExTax: price of product with options without tax
     */
    private String formattedFinalPriceExTax;

    /**
     * finalPriceIncTax: price of product with options with tax
     */
    private String formattedFinalPriceIncTax;

    /**
     * tax rate that needs to be applied to the final price
     */
    private String formattedTaxRate;
    
    /**
     * product id
     */
    private int productId;
    
    
    /**
     * Img src path
     */
    private String prodImgSrc;


    /**
     * Array of options - opts
     */
    private OptionJson[] opts;

    /**
     * Constructor
     */
    public OrderProductJson()
    {
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the formattedFinalPriceExTax
     */
    public String getFormattedFinalPriceExTax()
    {
        return formattedFinalPriceExTax;
    }

    /**
     * @param formattedFinalPriceExTax
     *            the formattedFinalPriceExTax to set
     */
    public void setFormattedFinalPriceExTax(String formattedFinalPriceExTax)
    {
        this.formattedFinalPriceExTax = formattedFinalPriceExTax;
    }

    /**
     * @return the formattedFinalPriceIncTax
     */
    public String getFormattedFinalPriceIncTax()
    {
        return formattedFinalPriceIncTax;
    }

    /**
     * @param formattedFinalPriceIncTax
     *            the formattedFinalPriceIncTax to set
     */
    public void setFormattedFinalPriceIncTax(String formattedFinalPriceIncTax)
    {
        this.formattedFinalPriceIncTax = formattedFinalPriceIncTax;
    }

    /**
     * @return the formattedTaxRate
     */
    public String getFormattedTaxRate()
    {
        return formattedTaxRate;
    }

    /**
     * @param formattedTaxRate
     *            the formattedTaxRate to set
     */
    public void setFormattedTaxRate(String formattedTaxRate)
    {
        this.formattedTaxRate = formattedTaxRate;
    }

    /**
     * @return the opts
     */
    public OptionJson[] getOpts()
    {
        return opts;
    }

    /**
     * @param opts
     *            the opts to set
     */
    public void setOpts(OptionJson[] opts)
    {
        this.opts = opts;
    }

    /**
     * @return the quantity
     */
    public int getQuantity()
    {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    /**
     * @return the productId
     */
    public int getProductId()
    {
        return productId;
    }

    /**
     * @param productId the productId to set
     */
    public void setProductId(int productId)
    {
        this.productId = productId;
    }

    /**
     * @return the prodImgSrc
     */
    public String getProdImgSrc()
    {
        return prodImgSrc;
    }

    /**
     * @param prodImgSrc the prodImgSrc to set
     */
    public void setProdImgSrc(String prodImgSrc)
    {
        this.prodImgSrc = prodImgSrc;
    }

}
