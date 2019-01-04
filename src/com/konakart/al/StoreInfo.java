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


/**
 * Used to configure a KKAppEng for a store. It is normally passed in the constructor.
 */
public class StoreInfo
{
    /** Id of the store */
    private String storeId;

    /** Constructor */
    public StoreInfo()
    {
    }

    /**
     * Constructor
     * 
     * @param storeId storeId
     */
    public StoreInfo(String storeId)
    {
        this.storeId = storeId;
    }

    /**
     * @return Returns the storeId.
     */
    public String getStoreId()
    {
        return storeId;
    }

    /**
     * @param storeId
     *            The storeId to set.
     */
    public void setStoreId(String storeId)
    {
        this.storeId = storeId;
    }
}
