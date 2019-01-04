//
// (c) 2016 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.bl.modules.others.liferaylogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.ExternalLoginResult;
import com.konakart.app.KKException;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.ExternalLoginInputIf;
import com.konakart.appif.ExternalLoginResultIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.others.ExternalLoginInterface;
import com.konakart.blif.ConfigurationMgrIf;
import com.konakart.blif.CustomerMgrIf;
import com.konakart.blif.SecurityMgrIf;

/**
 * Used for Liferay SSO. It is called to login a Liferay user after it has been confirmed that the
 * user is logged into Liferay.
 */
public class LiferayLogin extends BaseModule implements ExternalLoginInterface
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(LiferayLogin.class);

    private boolean available;

    /**
     * Constructor
     * 
     * @param eng
     * @throws KKException
     */
    public LiferayLogin(KKEngIf eng) throws KKException
    {
        super.init(eng);
        try
        {
            ConfigurationMgrIf configMgr = getConfigMgr();
            available = configMgr.getConfigurationValueAsBool(/* checkReturnByApi */false,
                    "MODULE_OTHER_LIFERAY_LOGIN_STATUS", false);
        } catch (Exception e)
        {
            throw new KKException("Exception in constructor of LiferayLogin:", e);
        }
    }

    /**
     * Called to login a Liferay user.
     * 
     * @param loginInfo
     *            Object containing the email address of the customer to login
     * @return Returns a LoginValidationResult object with information regarding the success of the
     *         login attempt. The KonaKart sessionId is returned in this object if the login is
     *         successful.
     * @throws Exception
     */
    public ExternalLoginResultIf externalLogin(ExternalLoginInputIf loginInfo) throws Exception
    {
        ExternalLoginResult ret = new ExternalLoginResult();

        if (loginInfo == null)
        {
            ret.setError(true);
            ret.setMessage("Parameter passed to validateLogin() is null");
            if (log.isDebugEnabled())
            {
                log.debug("Parameter passed to validateLogin() is null");
            }
            return ret;
        }

        if (loginInfo.getEmailAddr() == null || loginInfo.getEmailAddr().length() == 0)
        {
            ret.setError(true);
            ret.setMessage("emailAddr passed to validateLogin() is empty");
            if (log.isDebugEnabled())
            {
                log.debug("emailAddr passed to validateLogin() is empty");
            }
            return ret;
        }

        /* Get the customer */
        SecurityMgrIf secMgr = getSecMgr();
        CustomerMgrIf custMgr = getCustMgr();
        CustomerIf customer = custMgr.getCustomerForEmail(loginInfo.getEmailAddr());
        if (customer == null)
        {
            ret.setError(true);
            ret.setMessage("customer not found for email - " + loginInfo.getEmailAddr());
            if (log.isDebugEnabled())
            {
                log.debug("customer not found for email - " + loginInfo.getEmailAddr());
            }
            return ret;
        }

        // Create a session id
        String sessionId = secMgr.login(customer.getId());
        ret.setSessionId(sessionId);
        return ret;
    }

    /**
     * @return Returns true if the service is available
     */
    public boolean isAvailable()
    {
        return available;
    }
}
