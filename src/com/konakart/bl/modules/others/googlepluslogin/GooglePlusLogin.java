//
// (c) 2015 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.bl.modules.others.googlepluslogin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.konakart.app.CustomerRegistration;
import com.konakart.app.EmailOptions;
import com.konakart.app.ExternalLoginResult;
import com.konakart.app.KKException;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.CustomerRegistrationIf;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.ExternalLoginInputIf;
import com.konakart.appif.ExternalLoginResultIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.others.ExternalLoginInterface;
import com.konakart.blif.ConfigurationMgrIf;
import com.konakart.blif.CustomerMgrIf;
import com.konakart.blif.SecurityMgrIf;

/**
 * 
 * Used to verify through a Google+ service that the customer identified by the Google+ access
 * token is logged in.
 * 
 */
public class GooglePlusLogin extends BaseModule implements ExternalLoginInterface
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(GooglePlusLogin.class);

    private String urlTemplate;

    private boolean available;

    /**
     * Constructor
     * 
     * @param eng
     * @throws KKException
     */
    public GooglePlusLogin(KKEngIf eng) throws KKException
    {
        super.init(eng);
        try
        {
            ConfigurationMgrIf configMgr = getConfigMgr();
            urlTemplate = configMgr.getConfigurationValue(/*checkReturnByApi*/false,"MODULE_OTHER_GOOGLEPLUS_LOGIN_URL");
            available = configMgr.getConfigurationValueAsBool(/*checkReturnByApi*/false,"MODULE_OTHER_GOOGLEPLUS_LOGIN_STATUS", false);
        } catch (Exception e)
        {
            throw new KKException("Exception in constructor of GooglePlusLogin:",e);
        }
    }

    /**
     * Called to validate a Google+ access token.
     * 
     * @param loginInfo
     *            Object containing login information. The Google+ access token is passed in
     *            custom1. If the email address is passed in, then it is compared with the address
     *            returned by Google+ and an error is thrown if they don't match..
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

        String accessToken = loginInfo.getCustom1();

        if (accessToken == null || accessToken.length() == 0)
        {
            ret.setError(true);
            ret.setMessage("accessToken passed to validateLogin() is empty");
            if (log.isDebugEnabled())
            {
                log.debug("accessToken passed to validateLogin() is empty");
            }
            return ret;
        }

        if (urlTemplate == null || urlTemplate.length() == 0)
        {
            ret.setError(true);
            ret.setMessage("Google+ URL template defined by configuration variable MODULE_OTHER_GOOGLEPLUS_LOGIN_URL is empty");
            if (log.isWarnEnabled())
            {
                log.warn("Google+ URL template defined by configuration variable MODULE_OTHER_GOOGLEPLUS_LOGIN_URL is empty");
            }
            return ret;
        }

        /* Add the access token to the URL substituting the string {TOKEN} */
        String urlString = urlTemplate.replaceAll("\\{TOKEN\\}", accessToken);

        /* Get the result from Google+ */
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode != 200)
        {
            ret.setError(true);
            ret.setMessage("Response Code " + responseCode
                    + " from Google+. Could not validate login.");
            if (log.isWarnEnabled())
            {
                log.warn("Response Code " + responseCode
                        + " from Google+. Could not validate login.");
            }
            return ret;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Sending 'GET' request to URL : " + url);
            log.debug("Response Code : " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        /* Get the response */
        StringBuffer respSb = new StringBuffer();
        String line = in.readLine();
        while (line != null)
        {
            respSb.append(line);
            line = in.readLine();
        }
        in.close();
        
        if (log.isDebugEnabled())
        {
            log.debug("Raw Response from Google+ = ");
            log.debug(respSb);
        }



        /* Get a JSON object from the JSON string */
        JSONObject jsonObj;
        try
        {
            jsonObj = new JSONObject(respSb.toString());
        } catch (Exception e)
        {
            ret.setError(true);
            ret.setMessage("Badly formed json from Google+ = " + respSb);
            if (log.isWarnEnabled())
            {
                log.warn("Badly formed json from Google+ = " + respSb);
            }
            return ret;
        }

        if (log.isDebugEnabled())
        {
            log.debug("JSON response from Google+ = ");
            log.debug(jsonObj.toString(1));
        }

        /*
         * Attempt to get the email from the Google+ reply. If not successful we assume that the
         * validation didn't work out
         */
        String email = null;
        try
        {
            email = (String) jsonObj.get("email");
        } catch (Exception e)
        {
            ret.setError(true);
            return ret;
        }

        /*
         * Compare the email address passed into the method with the one returned from Google+.
         * They should match.
         */
        if (loginInfo.getEmailAddr() != null
                && loginInfo.getEmailAddr().compareToIgnoreCase(email) != 0)
        {
            ret.setError(true);
            ret.setMessage("Email address passed in as a parameter " + loginInfo.getEmailAddr()
                    + " does not match email address sent back from Google+ " + email);
            if (log.isDebugEnabled())
            {
                log.debug("Email address passed in as a parameter " + loginInfo.getEmailAddr()
                        + " does not match email address sent back from Google+ " + email);
            }
            return ret;
        }

        /* Set the email address on the return object */
        ret.setEmailAddr(email);

        /* Determine whether we need to register the customer */
        SecurityMgrIf secMgr = getSecMgr();
        CustomerMgrIf custMgr = getCustMgr();
        CustomerIf customer = custMgr.getCustomerForEmail(email);
        if (customer == null)
        {
            CustomerRegistrationIf cr = new CustomerRegistration();
            cr.setEmailAddr(email);
            cr.setEnabled(true);

            /* get first name */
            String firstName = null;
            try
            {
                firstName = (String) jsonObj.get("given_name");
            } catch (Exception e)
            {
                // may be missing
            }
            if (firstName == null || firstName.length() == 0)
            {
                cr.setNoName(true);
            } else
            {
                cr.setFirstName(firstName);
            }

            /* get last name */
            String lastName = null;
            try
            {
                lastName = (String) jsonObj.get("family_name");
            } catch (Exception e)
            {
                // may be missing
            }
            if (lastName == null || lastName.length() == 0)
            {
                cr.setNoName(true);
            } else
            {
                cr.setLastName(lastName);
            }

            cr.setNoGender(true);
            cr.setNoBirthDate(true);
            cr.setNoAddress(true);
            cr.setNoTelephone(true);

            /*
             * Setting noPassword to true means that the customer will not receive the KonaKart
             * password. However after logging in with Google+ on his account page he has a link
             * that he can click to get a password to be sent to his email address. If alternatively
             * you want to send the password in the welcome email, then you must set noPassword to
             * false and add the generated password to the CustomerRegistration object. e.g.
             */
            // String password = getSecMgr().getRandomPassword(0);
            // cr.setPassword(password);
            // cr.setNoPassword(false);
            cr.setNoPassword(true);

            // Register the customer and get the customer Id
            int custId = custMgr.registerCustomer(cr);

            /*
             * Send a welcome email. If you have a password and want to send it in the welcome
             * email, you can add it to the EmailOptions object in the customAttrs array.
             */
            EmailOptionsIf options = new EmailOptions();
            options.setCountryCode(getLangMgr().getDefaultLanguage().getCode());
            options.setTemplateName(com.konakart.bl.EmailMgr.WELCOME_TEMPLATE);
            getEmailMgr().sendWelcomeEmail1(custId, options);

            // Create a session id
            String sessionId = secMgr.login(custId);
            ret.setSessionId(sessionId);
            return ret;
        }

        if (!customer.isEnabled())
        {
            ret.setError(true);
            ret.setMessage("Customer " + email + " is not enabled");
            if (log.isDebugEnabled())
            {
                log.debug("Customer " + email + " is not enabled");
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
