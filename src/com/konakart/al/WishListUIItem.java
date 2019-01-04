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

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Used by EditWishListForm to present the wish list data in a list on the screen
 */
@SuppressWarnings("serial")
public class WishListUIItem implements Serializable
{

    /** Id of the wish list item so that we can populate it with data from this form */
    private int wishListItemId;

    /** Name of the product * */
    private String prodName;

    /** Image of the product * */
    private String prodImage;

    /** Array of option names */
    private String[] optNameArray;

    /** Id of the product * */
    private int prodId;

    /** Priority of the wish list item * */
    private int priority;

    /** Total Price inc tax of the product */
    private BigDecimal totalPriceIncTax;

    /** Total Price ex tax of the product */
    private BigDecimal totalPriceExTax;

    /** Number of items desired in a list such as a wedding list */
    private int quantityDesired;

    /** Number of items received in a list such as a wedding list */
    private int quantityReceived;

    /** Comments on the wish list item */
    private String comments;

    /** Custom variable */
    private String custom1 = null;

    /** Custom variable */
    private String custom2 = null;

    /** Custom variable */
    private String custom3 = null;

    /** Custom variable */
    private String custom4 = null;

    /** Custom variable */
    private String custom5 = null;

    /** Constructor */
    public WishListUIItem()
    {

    }

    /**
     * Constructor
     * 
     * @param wishListItemId
     *            the wishListItemId
     * 
     * @param prodId
     *            the prodId
     * @param prodName
     *            the prodName
     * @param prodImage
     *            the prodImage
     * @param totalPriceExTax
     *            the totalPriceExTax
     * @param totalPriceIncTax
     *            the totalPriceIncTax
     * @param priority
     *            the priority
     */
    public WishListUIItem(int wishListItemId, int prodId, String prodName, String prodImage,
            BigDecimal totalPriceExTax, BigDecimal totalPriceIncTax, int priority)
    {
        this.wishListItemId = wishListItemId;
        this.prodId = prodId;
        this.prodName = prodName;
        this.prodImage = prodImage;
        this.totalPriceIncTax = totalPriceIncTax;
        this.totalPriceExTax = totalPriceExTax;
        this.priority = priority;
    }

    /**
     * Constructor
     * 
     * @param wishListItemId
     *            the wishListItemId
     * 
     * @param prodId
     *            the prodId
     * @param prodName
     *            the prodName
     * @param prodImage
     *            the prodImage
     * @param totalPriceExTax
     *            the totalPriceExTax
     * @param totalPriceIncTax
     *            the totalPriceIncTax
     * @param priority
     *            the priority
     * @param quantityDesired 
     *            the quantityDesired
     * @param quantityReceived 
     *            the quantityReceived
     * @param comments
     *            the comments
     */
    public WishListUIItem(int wishListItemId, int prodId, String prodName, String prodImage,
            BigDecimal totalPriceExTax, BigDecimal totalPriceIncTax, int priority,
            int quantityDesired, int quantityReceived, String comments)
    {
        this.wishListItemId = wishListItemId;
        this.prodId = prodId;
        this.prodName = prodName;
        this.prodImage = prodImage;
        this.totalPriceIncTax = totalPriceIncTax;
        this.totalPriceExTax = totalPriceExTax;
        this.priority = priority;
        this.quantityDesired = quantityDesired;
        this.quantityReceived = quantityReceived;
        this.comments = comments;
    }

    /**
     * @return Returns the optNameArray.
     */
    public String[] getOptNameArray()
    {
        return optNameArray;
    }

    /**
     * @param optNameArray
     *            The optNameArray to set.
     */
    public void setOptNameArray(String[] optNameArray)
    {
        this.optNameArray = optNameArray;
    }

    /**
     * @return Returns the prodId.
     */
    public int getProdId()
    {
        return prodId;
    }

    /**
     * @param prodId
     *            The prodId to set.
     */
    public void setProdId(int prodId)
    {
        this.prodId = prodId;
    }

    /**
     * @return Returns the prodImage.
     */
    public String getProdImage()
    {
        return prodImage;
    }

    /**
     * @param prodImage
     *            The prodImage to set.
     */
    public void setProdImage(String prodImage)
    {
        this.prodImage = prodImage;
    }

    /**
     * @return Returns the prodName.
     */
    public String getProdName()
    {
        return prodName;
    }

    /**
     * @param prodName
     *            The prodName to set.
     */
    public void setProdName(String prodName)
    {
        this.prodName = prodName;
    }


    /**
     * @return Returns the totalPriceExTax.
     */
    public BigDecimal getTotalPriceExTax()
    {
        return totalPriceExTax;
    }

    /**
     * @param totalPriceExTax
     *            The totalPriceExTax to set.
     */
    public void setTotalPriceExTax(BigDecimal totalPriceExTax)
    {
        this.totalPriceExTax = totalPriceExTax;
    }

    /**
     * @return Returns the totalPriceIncTax.
     */
    public BigDecimal getTotalPriceIncTax()
    {
        return totalPriceIncTax;
    }

    /**
     * @param totalPriceIncTax
     *            The totalPriceIncTax to set.
     */
    public void setTotalPriceIncTax(BigDecimal totalPriceIncTax)
    {
        this.totalPriceIncTax = totalPriceIncTax;
    }

    /**
     * @return Returns the custom1.
     */
    public String getCustom1()
    {
        return custom1;
    }

    /**
     * @param custom1
     *            The custom1 to set.
     */
    public void setCustom1(String custom1)
    {
        this.custom1 = custom1;
    }

    /**
     * @return Returns the custom2.
     */
    public String getCustom2()
    {
        return custom2;
    }

    /**
     * @param custom2
     *            The custom2 to set.
     */
    public void setCustom2(String custom2)
    {
        this.custom2 = custom2;
    }

    /**
     * @return Returns the custom3.
     */
    public String getCustom3()
    {
        return custom3;
    }

    /**
     * @param custom3
     *            The custom3 to set.
     */
    public void setCustom3(String custom3)
    {
        this.custom3 = custom3;
    }

    /**
     * @return Returns the custom4.
     */
    public String getCustom4()
    {
        return custom4;
    }

    /**
     * @param custom4
     *            The custom4 to set.
     */
    public void setCustom4(String custom4)
    {
        this.custom4 = custom4;
    }

    /**
     * @return Returns the custom5.
     */
    public String getCustom5()
    {
        return custom5;
    }

    /**
     * @param custom5
     *            The custom5 to set.
     */
    public void setCustom5(String custom5)
    {
        this.custom5 = custom5;
    }

    /**
     * @return Returns the wishListItemId.
     */
    public int getWishListItemId()
    {
        return wishListItemId;
    }

    /**
     * @param wishListItemId
     *            The wishListItemId to set.
     */
    public void setWishListItemId(int wishListItemId)
    {
        this.wishListItemId = wishListItemId;
    }

    /**
     * @return Returns the priority.
     */
    public int getPriority()
    {
        return priority;
    }

    /**
     * @param priority
     *            The priority to set.
     */
    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    /**
     * @return the quantityDesired
     */
    public int getQuantityDesired()
    {
        return quantityDesired;
    }

    /**
     * @param quantityDesired
     *            the quantityDesired to set
     */
    public void setQuantityDesired(int quantityDesired)
    {
        this.quantityDesired = quantityDesired;
    }

    /**
     * @return the quantityReceived
     */
    public int getQuantityReceived()
    {
        return quantityReceived;
    }

    /**
     * @param quantityReceived
     *            the quantityReceived to set
     */
    public void setQuantityReceived(int quantityReceived)
    {
        this.quantityReceived = quantityReceived;
    }

    /**
     * @return the comments
     */
    public String getComments()
    {
        return comments;
    }

    /**
     * @param comments
     *            the comments to set
     */
    public void setComments(String comments)
    {
        this.comments = comments;
    }

}
