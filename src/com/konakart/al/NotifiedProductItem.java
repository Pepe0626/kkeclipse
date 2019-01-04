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


/**
 * Used by EditNotifiedProductForm to present the notified product data in a list on the screen.
 */
@SuppressWarnings("serial")
public class NotifiedProductItem implements Serializable
{
    /** Name of the product */
    private String prodName;

    /** Whether to remove the product from list */
    private boolean remove;

    /** Id of the product item */
    private int prodId;

    /** Constructor */
    public NotifiedProductItem()
    {

    }

    /**
     * Constructor
     * 
     * @param prodId
     *            the prodId
     * @param prodName
     *            the prodName
     */
    public NotifiedProductItem(int prodId, String prodName) 
    {
        this.prodId = prodId;
        this.prodName = prodName;
    }

    /**
     * @return Returns the prodId.
     */
    public int getProdId()
    {
        return prodId;
    }

    /**
     * @param prodId The prodId to set.
     */
    public void setProdId(int prodId)
    {
        this.prodId = prodId;
    }

    /**
     * @return Returns the prodName.
     */
    public String getProdName()
    {
        return prodName;
    }

    /**
     * @param prodName The prodName to set.
     */
    public void setProdName(String prodName)
    {
        this.prodName = prodName;
    }

    /**
     * @return Returns the remove.
     */
    public boolean isRemove()
    {
        return remove;
    }

    /**
     * @param remove The remove to set.
     */
    public void setRemove(boolean remove)
    {
        this.remove = remove;
    }


}
