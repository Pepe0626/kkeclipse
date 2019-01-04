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

import com.konakart.app.CustomerTag;
import com.konakart.app.KKException;
import com.konakart.appif.CustomerTagIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.util.KKConstants;

/**
 * Contains methods to manage Customer Tags
 */
public class CustomerTagMgr extends BaseMgr
{
    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected CustomerTagMgr(KKEngIf eng, KKAppEng kkAppEng) throws KKException
    {
        this.eng = eng;
        this.kkAppEng = kkAppEng;
    }

    /**
     * Sets the <code>tagValue</code> for the tag called <code>tagName</code> for the logged in
     * customer or guest customer.
     * 
     * @param tagName
     *            The name of the customer tag
     * @param tagValue
     *            The value of the customer tag for this customer
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void insertCustomerTag(String tagName, String tagValue) throws KKAppException,
            KKException
    {
        if (!isEnabled())
        {
            return;
        }

        if (tagName != null)
        {
            CustomerTag ct = new CustomerTag();
            ct.setName(tagName);
            ct.setValue(tagValue);
            ct.setType(KKConstants.NOT_SET);

            if (kkAppEng.getSessionId() != null && kkAppEng.getSessionId().length() > 0)
            {
                eng.insertCustomerTag(kkAppEng.getSessionId(), ct);
            } else
            {
                eng.insertCustomerTagForGuest(kkAppEng.getCustomerMgr().getCurrentCustomer()
                        .getId(), ct);
            }
        }
    }

    /**
     * Sets the <code>tag</code> for the logged in customer or guest customer.
     * 
     * @param tag
     *            The customer tag with populated tagName and tagValue attributes
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void insertCustomerTag(CustomerTag tag) throws KKAppException, KKException
    {
        if (!isEnabled())
        {
            return;
        }

        if (tag != null && kkAppEng.getSessionId() != null && kkAppEng.getSessionId().length() > 0)
        {
            eng.insertCustomerTag(kkAppEng.getSessionId(), tag);
        }

        if (tag != null)
        {
            if (kkAppEng.getSessionId() != null && kkAppEng.getSessionId().length() > 0)
            {
                eng.insertCustomerTag(kkAppEng.getSessionId(), tag);
            } else
            {
                eng.insertCustomerTagForGuest(kkAppEng.getCustomerMgr().getCurrentCustomer()
                        .getId(), tag);
            }
        }
    }

    /**
     * Adds the <code>tagValue</code> for the tag called <code>tagName</code> for the logged in
     * customer or guest customer. This is only valid for customer tags of type
     * <code>com.konakart.app.CustomerTag.MULTI_INT_TYPE</code> or
     * <code>MULTI_INT_PLUS_TIME_TYPE</code>.
     * 
     * @param tagName
     *            The name of the customer tag
     * @param tagValue
     *            The value of the customer tag for this customer
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void addToCustomerTag(String tagName, int tagValue) throws KKAppException, KKException
    {
        if (!isEnabled())
        {
            return;
        }

        if (tagName != null && kkAppEng.getSessionId() != null
                && kkAppEng.getSessionId().length() > 0)
        {
            eng.addToCustomerTag(kkAppEng.getSessionId(), tagName, tagValue);
        } else
        {
            eng.addToCustomerTagForGuest(kkAppEng.getCustomerMgr().getCurrentCustomer().getId(),
                    tagName, tagValue);
        }
    }

    /**
     * An expression object is retrieved from the database and evaluated for the logged in customer
     * or guest customer. If the <code>expressionName</code> parameter is not set to null, then the
     * Expression is searched for by name. Otherwise it is searched for by the id contained in the
     * <code>expressionId</code> parameter.
     * 
     * @param expressionId
     *            the expressionId
     * @param expressionName
     *            the expressionName
     * @return Returns true or false
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public boolean evaluateExpression(int expressionId, String expressionName)
            throws KKAppException, KKException
    {
        if (!isEnabled())
        {
            return false;
        }

        if (kkAppEng.getSessionId() != null && kkAppEng.getSessionId().length() > 0)
        {
            return eng.evaluateExpression(kkAppEng.getSessionId(), expressionId, expressionName);
        }
        return eng.evaluateExpressionForGuest(kkAppEng.getCustomerMgr().getCurrentCustomer()
                .getId(), expressionId, expressionName);
    }

    /**
     * A string is returned containing the value of the customer tag referenced by the parameter
     * <code>tagName</code> for the logged in customer or guest customer.
     * 
     * @param tagName
     *            The name of the customer tag
     * @return Returns the value of the customer tag
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public String getCustomerTagValue(String tagName) throws KKAppException, KKException
    {
        if (!isEnabled())
        {
            return null;
        }

        if (tagName != null)
        {
            if (kkAppEng.getSessionId() != null && kkAppEng.getSessionId().length() > 0)
            {
                return eng.getCustomerTagValue(kkAppEng.getSessionId(), tagName);
            }
            return eng.getCustomerTagValueForGuest(kkAppEng.getCustomerMgr().getCurrentCustomer()
                    .getId(), tagName);
        }

        return null;
    }

    /**
     * A CustomerTag object is returned containing the value of the customer tag referenced by the
     * parameter <code>tagName</code> for the logged in customer or guest customer.
     * 
     * @param tagName
     *            The name of the customer tag
     * @return Returns the CustomerTag object
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public CustomerTagIf getCustomerTag(String tagName) throws KKAppException, KKException
    {
        if (!isEnabled())
        {
            return null;
        }

        if (tagName != null)
        {
            if (kkAppEng.getSessionId() != null && kkAppEng.getSessionId().length() > 0)
            {
                return eng.getCustomerTag(kkAppEng.getSessionId(), tagName);
            }
            return eng.getCustomerTagForGuest(kkAppEng.getCustomerMgr().getCurrentCustomer()
                    .getId(), tagName);
        }

        return null;
    }

    /**
     * Based on a configuration variable decides whether Customer Tags are enabled. It returns true
     * if they are enabled. Otherwise it returns false.
     * 
     * @return Returns true if wish lists are enabled
     * 
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * 
     */
    public boolean isEnabled() throws KKAppException
    {
        String enabled = kkAppEng.getConfig(ConfigConstants.ENABLE_CUSTOMER_TAGS);
        if (enabled != null && enabled.equalsIgnoreCase("true"))
        {
            return true;
        }
        return false;
    }

}
