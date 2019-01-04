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

import java.math.BigDecimal;

import com.konakart.util.KKConstants;

/**
 * 
 * This class is used to provide the constraints for filtering a result set of products. The default
 * values are all set to not filter.
 */
public class ProductFilter
{
    private int categoryId = KKConstants.NOT_SET;

    private int manufacturerId = KKConstants.NOT_SET;

    private int tagId = KKConstants.NOT_SET;

    private BigDecimal priceFrom;

    private BigDecimal priceTo;

    private boolean removeAllTags = false;

    /**
     * Constructor
     * 
     */
    public ProductFilter()
    {

    }

    /**
     * Filter by category id when set to a positive number.
     * 
     * @return the categoryId
     */
    public int getCategoryId()
    {
        return categoryId;
    }

    /**
     * Filter by category id when set to a positive number.
     * 
     * @param categoryId
     *            the categoryId to set
     */
    public void setCategoryId(int categoryId)
    {
        this.categoryId = categoryId;
    }

    /**
     * Filter by tag id when set to a positive number.
     * 
     * @return the tagId
     */
    public int getTagId()
    {
        return tagId;
    }

    /**
     * Filter by tag id when set to a positive number.
     * 
     * @param tagId
     *            the tagId to set
     */
    public void setTagId(int tagId)
    {
        this.tagId = tagId;
    }

    /**
     * Filter by manufacturer id when set to a positive number.
     * 
     * @return the manufacturerId
     */
    public int getManufacturerId()
    {
        return manufacturerId;
    }

    /**
     * Filter by manufacturer id when set to a positive number.
     * 
     * @param manufacturerId
     *            the manufacturerId to set
     */
    public void setManufacturerId(int manufacturerId)
    {
        this.manufacturerId = manufacturerId;
    }

    /**
     * Remove all tag filters when set.
     * 
     * @return the removeAllTags
     */
    public boolean isRemoveAllTags()
    {
        return removeAllTags;
    }

    /**
     * Remove all tag filters when set.
     * 
     * @param removeAllTags
     *            the removeAllTags to set
     */
    public void setRemoveAllTags(boolean removeAllTags)
    {
        this.removeAllTags = removeAllTags;
    }

    /**
     * @return the priceFrom
     */
    public BigDecimal getPriceFrom()
    {
        return priceFrom;
    }

    /**
     * @param priceFrom the priceFrom to set
     */
    public void setPriceFrom(BigDecimal priceFrom)
    {
        this.priceFrom = priceFrom;
    }

    /**
     * @return the priceTo
     */
    public BigDecimal getPriceTo()
    {
        return priceTo;
    }

    /**
     * @param priceTo the priceTo to set
     */
    public void setPriceTo(BigDecimal priceTo)
    {
        this.priceTo = priceTo;
    }

}
