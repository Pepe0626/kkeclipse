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
 * Simplified WishList Object with formatted prices to send required data back to the browser in JSON format
 */
public class WishListJson
{
   
    private OptionJson[] opts;
    
    private String formattedPrice="";
        
    private int prodId;
    
    private String prodName;
    
    private String prodImgSrc;

    /**
     * Constructor
     */
    public WishListJson()
    {
    }

    /**
     * @return the opts
     */
    public OptionJson[] getOpts()
    {
        return opts;
    }

    /**
     * @param opts the opts to set
     */
    public void setOpts(OptionJson[] opts)
    {
        this.opts = opts;
    }

    /**
     * @return the formattedPrice
     */
    public String getFormattedPrice()
    {
        return formattedPrice;
    }

    /**
     * @param formattedPrice the formattedPrice to set
     */
    public void setFormattedPrice(String formattedPrice)
    {
        this.formattedPrice = formattedPrice;
    }

    /**
     * @return the prodId
     */
    public int getProdId()
    {
        return prodId;
    }

    /**
     * @param prodId the prodId to set
     */
    public void setProdId(int prodId)
    {
        this.prodId = prodId;
    }

    /**
     * @return the prodName
     */
    public String getProdName()
    {
        return prodName;
    }

    /**
     * @param prodName the prodName to set
     */
    public void setProdName(String prodName)
    {
        this.prodName = prodName;
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
