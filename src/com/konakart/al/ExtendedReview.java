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

import com.konakart.app.Review;
import com.konakart.appif.ReviewIf;

/**
 * 
 * This class is the same as the Review class except that it also contains some product attributes
 * to facilitate its use in the UI.
 */
public class ExtendedReview extends Review
{
    /**
     * Image
     */
    private String image;

    /**
     * Product Name
     */
    private String productName;

    /**
     * @param r
     *            the r
     */
    public ExtendedReview(ReviewIf r)
    {
        this.setAverageRating(r.getAverageRating());
        this.setCustomerId(r.getCustomerId());
        this.setCustomerName(r.getCustomerName());
        this.setDateAdded(r.getDateAdded());
        this.setId(r.getId());
        this.setLanguageId(r.getLanguageId());
        this.setLanguageName(r.getLanguageName());
        this.setProductId(r.getProductId());
        this.setRating(r.getRating());
        this.setReviewText(r.getReviewText());
        this.setTimesRead(r.getTimesRead());
    }

    /**
     * @return Returns the image.
     */
    public String getImage()
    {
        return image;
    }

    /**
     * @param image
     *            The image to set.
     */
    public void setImage(String image)
    {
        this.image = image;
    }

    /**
     * @return Returns the productName.
     */
    public String getProductName()
    {
        return productName;
    }

    /**
     * @param productName
     *            The productName to set.
     */
    public void setProductName(String productName)
    {
        this.productName = productName;
    }
}
