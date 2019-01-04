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
package com.konakart.bl.modules.others.facebooklogin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.konakart.app.CustomerRegistration;
import com.konakart.app.CustomerTag;
import com.konakart.app.EmailOptions;
import com.konakart.app.ExternalLoginResult;
import com.konakart.app.KKException;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.CustomerRegistrationIf;
import com.konakart.appif.CustomerTagIf;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.ExternalLoginInputIf;
import com.konakart.appif.ExternalLoginResultIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.others.ExternalLoginInterface;
import com.konakart.blif.ConfigurationMgrIf;
import com.konakart.blif.CustomerMgrIf;
import com.konakart.blif.CustomerTagMgrIf;
import com.konakart.blif.SecurityMgrIf;

/**
 * 
 * Used to verify through a Facebook service that the customer identified by the Facebook access
 * token is logged in.
 * 
 */
public class FacebookLogin extends BaseModule implements ExternalLoginInterface
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(FacebookLogin.class);

    private String urlTemplate;

    private boolean available;

    /**
     * Constructor
     * 
     * @param eng
     * @throws KKException
     */
    public FacebookLogin(KKEngIf eng) throws KKException
    {
        super.init(eng);
        try
        {
            ConfigurationMgrIf configMgr = getConfigMgr();
            urlTemplate = configMgr.getConfigurationValue(/* checkReturnByApi */false,
                    "MODULE_OTHER_FACEBOOK_LOGIN_URL");
            available = configMgr.getConfigurationValueAsBool(/* checkReturnByApi */false,
                    "MODULE_OTHER_FACEBOOK_LOGIN_STATUS", false);
        } catch (Exception e)
        {
            throw new KKException("Exception in constructor of FacebookLogin:", e);
        }
    }

    /**
     * Called to validate a Facebook access token.
     * 
     * @param loginInfo
     *            Object containing login information. The Facebook access token is passed in
     *            custom1. If the email address is passed in, then it is compared with the address
     *            returned by Facebook and an error is thrown if they don't match..
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
            ret.setMessage(
                    "Facebook URL template defined by configuration variable MODULE_OTHER_FACEBOOK_LOGIN_URL is empty");
            if (log.isWarnEnabled())
            {
                log.warn(
                        "Facebook URL template defined by configuration variable MODULE_OTHER_FACEBOOK_LOGIN_URL is empty");
            }
            return ret;
        }

        /* Add the access token to the URL substituting the string {TOKEN} */
        String urlString = urlTemplate.replaceAll("\\{TOKEN\\}", accessToken);

        /* Get the result from Facebook */
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode != 200)
        {
            ret.setError(true);
            ret.setMessage(
                    "Response Code " + responseCode + " from Facebook. Could not validate login.");
            if (log.isWarnEnabled())
            {
                log.warn("Response Code " + responseCode
                        + " from Facebook. Could not validate login.");
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

        /* Process the response. Get the pure JSON string from the response. */
        String resp = respSb.toString();
        int startIndex = resp.indexOf("KK(") + 3;
        int endIndex = resp.indexOf(");");
        if (startIndex < 0 || endIndex < 0)
        {
            ret.setError(true);
            ret.setMessage("Badly formed response from Facebook = " + resp);
            if (log.isWarnEnabled())
            {
                log.warn("Badly formed response from Facebook = " + resp);
            }
            return ret;
        }

        /* Get a JSON object from the JSON string */
        String json = resp.substring(startIndex, endIndex);
        JSONObject jsonObj;
        try
        {
            jsonObj = new JSONObject(json);
        } catch (Exception e)
        {
            ret.setError(true);
            ret.setMessage("Badly formed json from Facebook = " + json);
            if (log.isWarnEnabled())
            {
                log.warn("Badly formed json from Facebook = " + json);
            }
            return ret;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Response from Facebook = ");
            log.debug(jsonObj.toString(1));
        }

        /*
         * Attempt to get the email from the Facebook reply. If not successful we assume that the
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
         * Compare the email address passed into the method with the one returned from Facebook.
         * They should match.
         */
        if (loginInfo.getEmailAddr() != null
                && loginInfo.getEmailAddr().compareToIgnoreCase(email) != 0)
        {
            ret.setError(true);
            ret.setMessage("Email address passed in as a parameter " + loginInfo.getEmailAddr()
                    + " does not match email address sent back from Facebook " + email);
            if (log.isDebugEnabled())
            {
                log.debug("Email address passed in as a parameter " + loginInfo.getEmailAddr()
                        + " does not match email address sent back from Facebook " + email);
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

            /* gender could be "male", "female", missing or something else */
            String gender = null;
            try
            {
                gender = (String) jsonObj.get("gender");
            } catch (Exception e)
            {
                // may be missing
            }

            /* Set KK gender values */
            if (gender == null)
            {
                cr.setNoGender(true);
            } else if (gender.equalsIgnoreCase("male"))
            {
                cr.setGender("m");
            } else if (gender.equalsIgnoreCase("female"))
            {
                cr.setGender("f");
            } else
            {
                cr.setGender("x");
            }

            /* get first name */
            String firstName = null;
            try
            {
                firstName = (String) jsonObj.get("first_name");
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
                lastName = (String) jsonObj.get("last_name");
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

            cr.setNoBirthDate(true);
            cr.setNoAddress(true);
            cr.setNoTelephone(true);

            /*
             * Setting noPassword to true means that the customer will not receive the KonaKart
             * password. However after logging in with Facebook on his account page he has a link
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
            
            // Set Facebook Id tag
            setFacebookIdTag(custId, (String)jsonObj.get("id"));

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

        // Set Facebook Id tag
        setFacebookIdTag(customer.getId(), (String)jsonObj.get("id"));

        return ret;
    }

    /**
     * Store the Facebook Id of the customer logging in
     * 
     * @param customerId
     * @param fbId
     */
    private void setFacebookIdTag(int customerId, String fbId)
    {
        try
        {
            if (fbId == null || fbId.length() == 0)
            {
                return;
            }
            CustomerTagMgrIf custTagMgr = getCustTagMgr();
            CustomerTagIf tag = custTagMgr.getCustomerTag(customerId, "FACEBOOK_LOGIN_ID",
                    /* getValue */true);
            if (tag != null && tag.getValue() != null && fbId.equals(tag.getValue()))
            {
                // Tag value is already set so return
                return;
            }

            CustomerTag ct = new CustomerTag();
            ct.setName("FACEBOOK_LOGIN_ID");
            ct.setValue(fbId);
            custTagMgr.insertCustomerTag(customerId, ct, /* ignoreSetByAPI */ true);

        } catch (Exception e)
        {
            log.debug("Exception setting the Facebook Id customer tag", e);
        }
    }

    /**
     * @return Returns true if the service is available
     */
    public boolean isAvailable()
    {
        return available;
    }

}
