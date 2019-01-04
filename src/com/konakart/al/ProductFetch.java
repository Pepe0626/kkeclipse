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
 * 
 * This class is used to provide options for the FetchProduct method. Default values are set for
 * when fetching a new result set from the database.
 */
public class ProductFetch
{
    /** Constant that defines the type of operation - Fetch a new result set */
    public static final int OP_GET = 1;

    /** Constant that defines the type of operation - Navigate (page) an existing result set */
    public static final int OP_NAVIGATE = 2;

    /**
     * Constant that defines the type of operation - Filter an existing result set with more
     * constraints
     */
    public static final int OP_FILTER = 3;

    /** Constant that defines the type of operation - Sort an existing result set */
    public static final int OP_SORT = 4;

    private int operation = OP_GET;

    private boolean getSpecials = false;

    private boolean setCustomFacets = false;

    /**
     * Constructor
     * 
     */
    public ProductFetch()
    {

    }

    /**
     * The type of operation. Valid values are:
     * <ul>
     * <li>ProductFetch.OP_GET - Fetch a new result set</li>
     * <li>ProductFetch.OP_NAVIGATE - Navigate (page) an existing result set</li>
     * <li>ProductFetch.OP_FILTER - Filter an existing result set with more constraints</li>
     * <li>ProductFetch.OP_SORT - Sort an existing result set</li>
     * </ul>
     * 
     * @return the operation
     */
    public int getOperation()
    {
        return operation;
    }

    /**
     * The type of operation. Valid values are:
     * <ul>
     * <li>ProductFetch.OP_GET - Fetch a new result set</li>
     * <li>ProductFetch.OP_NAVIGATE - Navigate (page) an existing result set</li>
     * <li>ProductFetch.OP_FILTER - Filter an existing result set with more constraints</li>
     * <li>ProductFetch.OP_SORT - Sort an existing result set</li>
     * </ul>
     * 
     * @param operation
     *            the operation to set
     */
    public void setOperation(int operation)
    {
        this.operation = operation;
    }

    /**
     * When set to true the engine is called with getSpecialsPerCategory() rather than
     * searchForProductsWithOptions()
     * 
     * @return the getSpecials
     */
    public boolean isGetSpecials()
    {
        return getSpecials;
    }

    /**
     * When set to true the engine is called with getSpecialsPerCategory() rather than
     * searchForProductsWithOptions()
     * 
     * @param getSpecials
     *            the getSpecials to set
     */
    public void setGetSpecials(boolean getSpecials)
    {
        this.getSpecials = getSpecials;
    }

    /**
     * If true, then the customFacets retrieved from Solr are used to create a new list of
     * currentTagGroups. When filtering products, the customFacets returned from Solr will only
     * contain facets that contain products and this may not be the desired result. i.e. You may
     * want to continue displaying all available facets.
     * 
     * @return the setCustomFacets
     */
    public boolean isSetCustomFacets()
    {
        return setCustomFacets;
    }

    /**
     * If true, then the customFacets retrieved from Solr are used to create a new list of
     * currentTagGroups. When filtering products, the customFacets returned from Solr will only
     * contain facets that contain products and this may not be the desired result. i.e. You may
     * want to continue displaying all available facets.
     * 
     * @param setCustomFacets
     *            the setCustomFacets to set
     */
    public void setSetCustomFacets(boolean setCustomFacets)
    {
        this.setCustomFacets = setCustomFacets;
    }

}
