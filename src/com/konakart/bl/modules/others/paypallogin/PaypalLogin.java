//
// (c) 2017 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.bl.modules.others.paypallogin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
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
import com.konakart.db.KKBasePeer;
import com.konakart.db.KKCriteria;
import com.konakart.om.BaseCountriesPeer;
import com.workingdogs.village.Record;

/**
 * 
 * Used to verify through a PayPal service that the customer identified by the PayPal access token
 * is logged in an dto retrieve information about that user
 * 
 */
public class PaypalLogin extends BaseModule implements ExternalLoginInterface
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(PaypalLogin.class);

    private String clientId;

    private String secretKey;

    private String sandboxURL;

    private String liveURL;

    private boolean useSandbox;

    private boolean available;

    private String returnURL;

    private String tokenService = "/v1/identity/openidconnect/tokenservice?grant_type=authorization_code&code={0}&redirect_uri={1}";

    private String userInfo = "/v1/identity/openidconnect/userinfo/?schema=openid";

    private String TOKEN_SERVICE_NAME = "tokenService";

    private String USER_INFO_NAME = "userinfo";

    /**
     * Constructor
     * 
     * @param eng
     * @throws KKException
     */
    public PaypalLogin(KKEngIf eng) throws KKException
    {
        super.init(eng);
        try
        {
            ConfigurationMgrIf configMgr = getConfigMgr();
            clientId = configMgr.getConfigurationValue(/* checkReturnByApi */false,
                    "MODULE_OTHER_PAYPAL_CLIENT_ID");
            secretKey = configMgr.getConfigurationValue(/* checkReturnByApi */false,
                    "MODULE_OTHER_PAYPAL_SECRET_KEY");
            sandboxURL = configMgr.getConfigurationValue(/* checkReturnByApi */false,
                    "MODULE_OTHER_PAYPAL_LOGIN_SANDBOX_URL");
            liveURL = configMgr.getConfigurationValue(/* checkReturnByApi */false,
                    "MODULE_OTHER_PAYPAL_LOGIN_LIVE_URL");
            useSandbox = configMgr.getConfigurationValueAsBool(/* checkReturnByApi */false,
                    "MODULE_OTHER_PAYPAL_LOGIN_RUN_IN_SANDBOX", true);
            available = configMgr.getConfigurationValueAsBool(/* checkReturnByApi */false,
                    "MODULE_OTHER_PAYPAL_LOGIN_STATUS", false);
            returnURL = configMgr.getConfigurationValue(/* checkReturnByApi */false,
                    "MODULE_OTHER_PAYPAL_LOGIN_RETURN_URL");
        } catch (Exception e)
        {
            throw new KKException("Exception in constructor of PaypalLogin:", e);
        }
    }

    /**
     * Called to grant a token from a PayPal authorization Code
     * 
     * @param loginInfo
     *            Object containing login information. The PayPal authorization code is passed in
     *            custom1. If the email address is passed in, then it is compared with the address
     *            returned by PayPal and an error is thrown if they don't match..
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
            ret.setMessage("Parameter passed to externalLogin() is null");
            if (log.isDebugEnabled())
            {
                log.debug("Parameter passed to externalLogin() is null");
            }
            return ret;
        }

        String authCode = loginInfo.getCustom1();

        if (authCode == null || authCode.length() == 0)
        {
            ret.setError(true);
            ret.setMessage("authCode passed to externalLogin() is empty");
            if (log.isDebugEnabled())
            {
                log.debug("authCode passed to externalLogin() is empty");
            }
            return ret;
        }

        if (clientId == null || clientId.length() == 0)
        {
            ret.setError(true);
            ret.setMessage("PayPal Client Id defined by configuration variable MODULE_OTHER_PAYPAL_CLIENT_ID is empty");
            if (log.isWarnEnabled())
            {
                log.warn("PayPal Client Id defined by configuration variable MODULE_OTHER_PAYPAL_CLIENT_ID is empty");
            }
            return ret;
        }

        if (secretKey == null || secretKey.length() == 0)
        {
            ret.setError(true);
            ret.setMessage("PayPal Secret Key defined by configuration variable MODULE_OTHER_PAYPAL_SECRET_KEY is empty");
            if (log.isWarnEnabled())
            {
                log.warn("PayPal Secret Key defined by configuration variable MODULE_OTHER_PAYPAL_SECRET_KEY is empty");
            }
            return ret;
        }

        if (returnURL == null || returnURL.length() == 0)
        {
            ret.setError(true);
            ret.setMessage("PayPal return URL defined by configuration variable MODULE_OTHER_PAYPAL_LOGIN_RETURN_URL is empty");
            if (log.isWarnEnabled())
            {
                log.warn("PayPal return URL defined by configuration variable MODULE_OTHER_PAYPAL_LOGIN_RETURN_URL is empty");
            }
            return ret;
        }

        if (useSandbox)
        {
            if (sandboxURL == null || sandboxURL.length() == 0)
            {
                ret.setError(true);
                ret.setMessage("PayPal Sandbox API Endpoint defined by configuration variable MODULE_OTHER_PAYPAL_LOGIN_SANDBOX_URL is empty");
                if (log.isWarnEnabled())
                {
                    log.warn("PayPal Sandbox API Endpoint defined by configuration variable MODULE_OTHER_PAYPAL_LOGIN_SANDBOX_URL is empty");
                }
                return ret;
            }
        } else
        {
            if (liveURL == null || liveURL.length() == 0)
            {
                ret.setError(true);
                ret.setMessage("PayPal Live API Endpoint defined by configuration variable MODULE_OTHER_PAYPAL_LOGIN_LIVE_URL is empty");
                if (log.isWarnEnabled())
                {
                    log.warn("PayPal Live API Endpoint defined by configuration variable MODULE_OTHER_PAYPAL_LOGIN_LIVE_URL is empty");
                }
                return ret;
            }
        }

        /* create the URL string depending on the mode we are in */
        String urlString;
        if (useSandbox)
        {
            urlString = sandboxURL + tokenService;
        } else
        {
            urlString = liveURL + tokenService;
        }

        // Add parameter info
        urlString = urlString.replace("{0}", authCode);
        urlString = urlString.replace("{1}", returnURL);

        /* Call PayPal to get a token from the suthorization code */
        JSONObject jsonObjTokenService = callPayPalAPI(urlString, ret, TOKEN_SERVICE_NAME,/* accessToken */
                null);
        if (ret.isError())
        {
            return ret;
        }

        // Ensure that we have an access_token
        String accessToken = null;
        try
        {
            accessToken = (String) jsonObjTokenService.get("access_token");
        } catch (Exception e)
        {
            ret.setError(true);
            ret.setMessage("access_token missing from reply from PayPal tokenservice");
            return ret;
        }

        if (accessToken == null || accessToken.length() == 0)
        {
            ret.setError(true);
            ret.setMessage("access_token missing from reply from PayPal tokenservice");
            return ret;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Access Token from PayPal = " + accessToken);
        }

        // Get the user information from the token
        if (useSandbox)
        {
            urlString = sandboxURL + userInfo;
        } else
        {
            urlString = liveURL + userInfo;
        }
        /* Call PayPal to get user info */
        JSONObject jsonObjUserInfo = callPayPalAPI(urlString, ret, USER_INFO_NAME, accessToken);
        if (ret.isError())
        {
            return ret;
        }

        /* Get the email address */
        String emailAddr = getFromJson(jsonObjUserInfo, "email");

        /* Check that email exists */
        if (emailAddr == null || emailAddr.length() == 0)
        {
            ret.setError(true);
            ret.setMessage("Email address not received from PayPal");
            if (log.isDebugEnabled())
            {
                log.debug("Email address not received from PayPal");
            }
            return ret;
        }

        /*
         * Compare the email address passed into the method with the one returned from PayPal. They
         * should match.
         */
        if (loginInfo.getEmailAddr() != null
                && loginInfo.getEmailAddr().compareToIgnoreCase(emailAddr) != 0)
        {
            ret.setError(true);
            ret.setMessage("Email address passed in as a parameter " + loginInfo.getEmailAddr()
                    + " does not match email address sent back from PayPal " + emailAddr);
            if (log.isDebugEnabled())
            {
                log.debug("Email address passed in as a parameter " + loginInfo.getEmailAddr()
                        + " does not match email address sent back from PayPal " + emailAddr);
            }
            return ret;
        }

        /* Set the email address on the return object */
        ret.setEmailAddr(emailAddr);

        /* Determine whether we need to register the customer */
        SecurityMgrIf secMgr = getSecMgr();
        CustomerMgrIf custMgr = getCustMgr();
        CustomerIf customer = custMgr.getCustomerForEmail(emailAddr);
        if (customer == null)
        {
            CustomerRegistrationIf cr = new CustomerRegistration();
            cr.setEmailAddr(emailAddr);
            cr.setEnabled(true);

            /* gender could be "male", "female", missing or something else */
            // TODO not sure of the format of gender from PayPal
            String gender = getFromJson(jsonObjUserInfo, "gender");

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
            String firstName = getFromJson(jsonObjUserInfo, "given_name");
            if (firstName == null || firstName.length() == 0)
            {
                cr.setNoName(true);
            } else
            {
                cr.setFirstName(firstName);
            }

            /* get last name */
            String lastName = getFromJson(jsonObjUserInfo, "family_name");
            if (lastName == null || lastName.length() == 0)
            {
                cr.setNoName(true);
            } else
            {
                cr.setLastName(lastName);
            }

            /* get telephone number */
            String telephoneNumber = getFromJson(jsonObjUserInfo, "phone_number");
            if (telephoneNumber == null || telephoneNumber.length() == 0)
            {
                cr.setNoTelephone(true);
            } else
            {
                cr.setTelephoneNumber(telephoneNumber);
            }

            /* get locale */
            String locale = getFromJson(jsonObjUserInfo, "locale");
            cr.setLocale(locale);

            /* get DOB */
            String birthDateStr = getFromJson(jsonObjUserInfo, "birthday"); // YYYY-MM-DD
            if (birthDateStr != null)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                Date d = null;
                try
                {
                    d = sdf.parse(birthDateStr);
                } catch (Exception e)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Cannot parse Birth Date : " + birthDateStr);
                    }
                }
                if (d != null)
                {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(d);
                    cr.setBirthDate(gc);
                } else
                {
                    cr.setNoBirthDate(true);
                }
            } else
            {
                cr.setNoBirthDate(true);
            }

            /* Get the address information */
            JSONObject jsonObjAddr = null;
            try
            {
                jsonObjAddr = jsonObjUserInfo.getJSONObject("address");
            } catch (Exception e1)
            {
            }
            if (jsonObjAddr == null)
            {
                cr.setNoAddress(true);
            } else
            {
                String streetAddress = getFromJson(jsonObjAddr, "street_address");
                String city = getFromJson(jsonObjAddr, "locality");
                String state = getFromJson(jsonObjAddr, "region");
                String postcode = getFromJson(jsonObjAddr, "postal_code");
                String country = getFromJson(jsonObjAddr, "country");
                int countryId = validateAddress(streetAddress, city, state, postcode, country);
                if (countryId > -1)
                {
                    cr.setStreetAddress(streetAddress);
                    cr.setCity(city);
                    cr.setPostcode(postcode);
                    cr.setCountryId(countryId);
                    cr.setState(state);
                } else
                {
                    if (log.isWarnEnabled())
                    {
                        log.warn("Could not validate address for PayPal user "
                                + emailAddr
                                + " so registered in KonaKart without address. Address from PayPal was\n"
                                + jsonObjAddr.toString());
                    }
                    cr.setNoAddress(true);
                }
            }

            /*
             * Setting noPassword to true means that the customer will not receive the KonaKart
             * password. However after logging in with PayPal on his account page he has a link that
             * he can click to get a password to be sent to his email address. If alternatively you
             * want to send the password in the welcome email, then you must set noPassword to false
             * and add the generated password to the CustomerRegistration object. e.g.
             */
            // String password = getSecMgr().getRandomPassword(0);
            // cr.setPassword(password);
            // cr.setNoPassword(false);
            cr.setNoPassword(true);

            // Register the customer and get the customer Id
            int custId;
            try
            {
                custId = custMgr.registerCustomer(cr);
            } catch (Exception e)
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Attempted to register with CustomerRegistration object\n"
                            + cr.toString() + " but received exception " + e.getMessage());
                }
                // Try again with no address
                cr.setStreetAddress(null);
                cr.setCity(null);
                cr.setPostcode(null);
                cr.setCountryId(0);
                cr.setState(null);
                cr.setNoAddress(true);
                if (log.isWarnEnabled())
                {
                    log.warn("Attempting again to register with CustomerRegistration object"
                            + cr.toString());
                }
                custId = custMgr.registerCustomer(cr);
            }

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
            ret.setMessage("Customer " + emailAddr + " is not enabled");
            if (log.isDebugEnabled())
            {
                log.debug("Customer " + emailAddr + " is not enabled");
            }
            return ret;
        }

        // Create a session id
        String sessionId = secMgr.login(customer.getId());
        ret.setSessionId(sessionId);
        return ret;
    }

    /**
     * Validate the address from PayPal before attempting to register the customer in KonaKart
     * 
     * @param streetAddress
     * @param city
     * @param state
     * @param postcode
     * @param country
     * @return Returns the country id if the address validates. Otherwise a negative number.
     * @throws Exception
     */
    private int validateAddress(String streetAddress, String city, String state, String postcode,
            String country) throws Exception
    {
        try
        {
            if (streetAddress == null || streetAddress.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("streetAddress is missing");
                }
                return -1;
            }
            if (postcode == null || postcode.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("postcode is missing");
                }
                return -1;
            }
            if (city == null || city.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("city is missing");
                }
                return -1;
            }
            if (country == null || country.length() != 2)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("country is missing or not ISO2 Code");
                }
                return -1;
            }
            int countryId = getCountryPerISO2(country);
            if (countryId < 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Unable to find country for ISO2 Code: " + country);
                }
                return -1;
            }

            if (state == null || state.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("state is missing");
                }
                return -1;
            }
            if (!getCustMgr().doesZoneExist(state, countryId))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("The zone " + state + " is invalid for a country with id = "
                            + countryId);
                }
                return -1;
            }

            return countryId;
        } catch (Exception e)
        {
            if (log.isWarnEnabled())
            {
                log.warn("Exception while validating address from PayPal", e);
            }
            return -1;
        }
    }

    /**
     * Returns the country id for the country referenced by the Iso2Code
     * 
     * @param iso2Code
     * @return Returns the country id for the country. -1 If the country isn't found.
     * @throws Exception
     */
    public int getCountryPerISO2(String iso2Code) throws Exception
    {
        KKCriteria c = getNewCriteria(isMultiStoreShareCustomersOrProducts());
        c.addSelectColumn(BaseCountriesPeer.COUNTRIES_ID);
        c.add(BaseCountriesPeer.COUNTRIES_ISO_CODE_2, iso2Code);
        List<Record> rows = KKBasePeer.doSelect(c);
        if (rows.size() == 0)
        {
            return -1;
        }
        return rows.get(0).getValue(1).asInt();
    }

    /**
     * Returns the value of an attribute from a JSONobject
     * 
     * @param obj
     * @param attrName
     * @return
     */
    private String getFromJson(JSONObject obj, String attrName)
    {
        try
        {
            String val = obj.getString(attrName);
            if (log.isDebugEnabled())
            {
                log.debug("Value of " + attrName + " = " + val);
            }
            return val;
        } catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Calls PayPal and returns a JSON object
     * 
     * @param urlString
     * @param ret
     * @param serviceName
     * @param accessToken
     * @return Returns a JSON object
     * @throws Exception
     */
    private JSONObject callPayPalAPI(String urlString, ExternalLoginResult ret, String serviceName,
            String accessToken) throws Exception
    {

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        if (log.isDebugEnabled())
        {
            log.debug("Sending 'GET' request to URL : " + url);
        }

        if (serviceName.equals(TOKEN_SERVICE_NAME))
        {
            String userpass = clientId + ":" + secretKey;
            String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
            conn.setRequestProperty("Authorization", basicAuth);
        } else if (serviceName.equals(USER_INFO_NAME))
        {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        } else
        {
            throw new KKException("Unknown service name : " + serviceName);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200)
        {
            ret.setError(true);
            ret.setMessage("Response Code " + responseCode
                    + " from PayPal. Could not validate login.");
            if (log.isWarnEnabled())
            {
                log.warn("Response Code " + responseCode
                        + " from PayPal. Could not validate login.");
            }
            return null;
        }

        if (log.isDebugEnabled())
        {
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
            log.debug("Raw response from PayPal " + serviceName + " : " + respSb);
        }

        /* Get a JSON object from the JSON string */
        JSONObject jsonObj;
        try
        {
            jsonObj = new JSONObject(respSb.toString());
        } catch (Exception e)
        {
            ret.setError(true);
            ret.setMessage("Badly formed json from PayPal " + serviceName + " = " + respSb);
            if (log.isWarnEnabled())
            {
                log.warn("Badly formed json from PayPal " + serviceName + " = " + respSb);
            }
            return null;
        }

        return jsonObj;

    }

    /**
     * @return Returns true if the service is available
     */
    public boolean isAvailable()
    {
        return available;
    }

}
