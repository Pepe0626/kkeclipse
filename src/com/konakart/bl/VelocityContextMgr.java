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
package com.konakart.bl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.konakart.app.KKException;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.KKEngIf;
import com.konakart.blif.VelocityContextMgrIf;

/**
 * The VelocityContextMgr - for Managing context Maps
 */
public class VelocityContextMgr extends BaseMgr implements VelocityContextMgrIf
{
    /** the log */
    protected static Log log = LogFactory.getLog(VelocityContextMgr.class);

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @throws Exception an unexpected exception
     */
    public VelocityContextMgr(KKEngIf eng) throws Exception
    {
        super.init(eng, log);
    }

    public void addToContext(VelocityContext context, KKEngIf eng, int contextType, int langId,
            EmailOptionsIf options, int customInt, String customString) throws KKException
    {
        // The default implementation does nothing except log the parameters

        if (log.isDebugEnabled())
        {
            log.debug("contextType: " + contextType + " (" + contextTypeToString(contextType)
                    + ") customInt: " + customInt + " customString: " + customString
                    + " languageId: " + langId);
        }

        /* Example */

        // Example if (contextType == TEMPLATE_MAIL_TO_CUST)
        // Example {
        // Example com.konakart.appif.CurrencyIf defCurrency = eng.getDefaultCurrency();
        // Example context.put("defaultCurrency", defCurrency);
        // Example }

        /* End of Example */
        
        // This can be removed; it's another example
        context.put("KonaKartVersion", eng.getKonaKartVersion());
    }

    /**
     * Just for diagnostic purposes to translate the contextType integer into a human-readable
     * String
     * 
     * @param contextType
     *            the contextType
     * @return a String representing the specified contextType
     */
    protected String contextTypeToString(int contextType)
    {
        switch (contextType)
        {
        case ORDER_CONFIRMATION_EMAIL:
            return "ORDER_CONFIRMATION_EMAIL";
        case TEMPLATE_MAIL_TO_CUST:
            return "TEMPLATE_MAIL_TO_CUST";
        case SEND_NEW_PASSWORD:
            return "SEND_NEW_PASSWORD";
        case STOCK_REORDER_EMAIL:
            return "STOCK_REORDER_EMAIL";
        case WELCOME_EMAIL:
            return "WELCOME_EMAIL";
        default:
            return "(" + contextType + ") UNDEFINED";
        }
    }
}
