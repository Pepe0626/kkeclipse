//
// (c) 2017 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is free software; you can redistribute 
// it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This software is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package com.konakart.actions;

import java.math.BigDecimal;

import com.konakart.al.KKAppEng;
import com.konakart.app.KKException;
import com.konakart.appif.CustomerIf;
import com.konakart.bl.CustomerMgr;

/**
 * Base Action containing common B2B functionality
 */
public class B2BBaseAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    /** Customer Tag */
    protected static final String B2B_CAN_CHANGE_ADDRESS = "B2B_CAN_CHANGE_ADDRESS";

    /** Customer Tag */
    protected static final String B2B_ORDERS_NEED_APPROVAL = "B2B_ORDERS_NEED_APPROVAL";

    /** Customer Tag */
    protected static final String B2B_ORDER_LIMIT = "B2B_ORDER_LIMIT";
    
    /** Customer Tag */
    protected static final String B2B_AGGREGATE_ORDER_LIMIT = "B2B_AGGREGATE_ORDER_LIMIT";

    /** Customer Tag */
    protected static final String B2B_VIEW_SIBLING_ORDERS = "B2B_VIEW_SIBLING_ORDERS";

    /**
     * Called to determine whether the current logged in customer is a B2B admin user
     * 
     * @param kkAppEng
     *            the kkAppEng
     * @return Returns true if the logged in customer is a B2B admin user
     */
    protected boolean isB2BAdmin(KKAppEng kkAppEng)
    {
        CustomerIf currentCust = kkAppEng.getCustomerMgr().getCurrentCustomer();
        if (currentCust.getType() == CustomerMgr.CUST_TYPE_B2B_COMPANY_ADMIN)
        {
            return true;
        }
        log.info("Customer with id = " + currentCust.getId() + " is not a B2B Admin customer.");
        return false;
    }

    /**
     * Utility to get a tag value as a BigDecimal
     * 
     * @param value
     *            the value
     * @return Returns the BigDecimal value of the tag
     * @throws KKException
     *            an unexpected KKException exception
     */
    public BigDecimal getBigDecimalFromString(String value) throws KKException
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            BigDecimal val = new BigDecimal(value);
            return val;
        } catch (Exception e)
        {
            log.warn(value + " cannot be converted to a BigDecimal");
            return null;
        }
    }

    /**
     * Utility to get a tag value as a boolean
     * 
     * @param value
     *            the value
     * @param defaultValue
     *            This is returned if the value cannot be converted to a boolean
     * @return Returns the boolean value of the tag
     * @throws KKException
     *            an unexpected KKException exception
     */
    public boolean getBooleanFromString(String value, boolean defaultValue) throws KKException
    {
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            Boolean val = Boolean.valueOf(value);
            return val;
        } catch (Exception e)
        {
            log.warn(value + " cannot be converted to a boolean");
            return defaultValue;
        }
    }

}
