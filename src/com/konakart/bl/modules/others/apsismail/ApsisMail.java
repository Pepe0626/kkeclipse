//
// (c) 2018 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.bl.modules.others.apsismail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konakart.app.ExternalMailInput;
import com.konakart.app.ExternalMailResult;
import com.konakart.app.KKException;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.NameValueIf;
import com.konakart.bl.modules.others.BaseMailModule;
import com.konakart.bl.modules.others.ExternalMailInterface;
import com.konakart.util.ExceptionUtils;
import com.konakart.util.JavaUtils;
import com.konakart.util.PropertiesUtils;
import com.konakart.util.PropertyFileFinder;
import com.konakartadmin.app.AdminNameValue;
import com.konakartadmin.app.KKAdminException;
import com.konakartadmin.appif.KKAdminIf;

/**
 * Used to send mails using APSIS. The mails may be sent either from the App Engine or the Admin
 * Engine.
 */
public class ApsisMail extends BaseMailModule implements ExternalMailInterface
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(ApsisMail.class);

    // APSIS Transactional mail object used to send the mail
    private ApsisTransactionalEmail apsisMail;

    /**
     * JSON Data Mapping Object
     */
    private ObjectMapper mapper = null; // can reuse, share globally

    /** APSIS API KEY Constant Key */
    protected final static String MODULE_OTHER_APSIS_MAIL_API_KEY = "MODULE_OTHER_APSIS_MAIL_API_KEY";

    /** APSIS URL Constant Key */
    protected final static String MODULE_OTHER_APSIS_MAIL_URL = "MODULE_OTHER_APSIS_MAIL_URL";

    /** APSIS Mapping Filename Constant Key */
    protected final static String MODULE_OTHER_APSIS_MAIL_MAPPING_FILENAME = "MODULE_OTHER_APSIS_MAIL_MAPPING_FILENAME";

    /** APSIS Connect Timeout Constant Key */
    protected final static String MODULE_OTHER_APSIS_MAIL_CONNECT_TIMEOUT = "MODULE_OTHER_APSIS_MAIL_CONNECT_TIMEOUT";

    /** APSIS Read Timeout Constant Key */
    protected final static String MODULE_OTHER_APSIS_MAIL_READ_TIMEOUT = "MODULE_OTHER_APSIS_MAIL_READ_TIMEOUT";

    /** APSIS Max Retries Constant Key */
    protected final static String MODULE_OTHER_APSIS_MAIL_MAX_RETRIES = "MODULE_OTHER_APSIS_MAIL_MAX_RETRIES";

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // connectTimeoutMS default
    protected final static int connectTimeoutMS_DEFAULT = 5000;

    // readTimeoutMS default
    protected final static int readTimeoutMS_DEFAULT = 30000;

    // maxRetries default
    protected final static int maxRetries_DEFAULT = 2;

    /**
     * Constructor when called from App Eng
     * 
     * @param appEng
     * @throws KKException
     */
    public ApsisMail(KKEngIf appEng) throws KKException
    {
        if (appEng == null)
        {
            throw new KKException("KonaKart engine must not be null");
        }
        super.init(appEng);
        setConfigs();
    }

    /**
     * Constructor when called from Admin Eng
     * 
     * @param adminEng
     * @throws KKAdminException
     *             an unexpected exception in the KonaKart Admin engine
     */
    public ApsisMail(KKAdminIf adminEng) throws KKAdminException
    {
        if (adminEng == null)
        {
            throw new KKAdminException("KonaKart admin engine must not be null");
        }
        super.init(adminEng);
        try
        {
            setConfigs();
        } catch (KKException e)
        {
            log.warn("Exception thrown in setConfigs()\n" + ExceptionUtils.exceptionToString(e));
            throw new KKAdminException(e);
        }
    }

    /**
     * Set configuration variables. Here we just set the APSIS specific variables. Others are set in
     * the super implementation.
     * 
     * @throws KKException
     */
    protected void setConfigs() throws KKException
    {
        boolean putStaticData = false;

        super.setConfigs();
        
        StaticData staticData = staticDataHM.get(getStoreId());
        if (staticData == null)
        {
            staticData = new StaticData();
            putStaticData = true;
        } else
        {
            if (!updateStaticVariablesNow(staticData.getLastUpdatedMS()))
            {
                return;
            }
        }

        staticData.setApiKey(getConfigurationValue(MODULE_OTHER_APSIS_MAIL_API_KEY));
        if (staticData.getApiKey() == null || staticData.getApiKey().length() == 0)
        {
            log.warn("The APSIS API key has not been set in " + getStoreId());
        }

        staticData.setApsisUrl(getConfigurationValue(MODULE_OTHER_APSIS_MAIL_URL));
        if (staticData.getApsisUrl() == null || staticData.getApsisUrl().length() == 0)
        {
            log.warn("The APSIS URL has not been set in " + getStoreId());
        }

        staticData.setApsisMappingFilename(
                getConfigurationValue(MODULE_OTHER_APSIS_MAIL_MAPPING_FILENAME));
        if (staticData.getApsisMappingFilename() == null
                || staticData.getApsisMappingFilename().length() == 0)
        {
            log.warn("The APSIS Mapping Filename has not been set in " + getStoreId());
        }

        String confStr = getConfigurationValue(MODULE_OTHER_APSIS_MAIL_CONNECT_TIMEOUT);
        if (confStr == null || confStr.length() == 0)
        {
            log.warn("The APSIS Connect Timeout has not been set in " + getStoreId());
            staticData.setConnectTimeoutMS(connectTimeoutMS_DEFAULT);
        } else
        {
            try
            {
                staticData.setConnectTimeoutMS(Integer.parseInt(confStr));
            } catch (NumberFormatException e)
            {
                log.warn("The APSIS Connect Timeout parameter (" + confStr
                        + ") cannot be converted to an Integer", e);
            }
        }

        confStr = getConfigurationValue(MODULE_OTHER_APSIS_MAIL_READ_TIMEOUT);
        if (confStr == null || confStr.length() == 0)
        {
            log.warn("The APSIS Read Timeout has not been set in " + getStoreId());
            staticData.setReadTimeoutMS(readTimeoutMS_DEFAULT);
        } else
        {
            try
            {
                staticData.setReadTimeoutMS(Integer.parseInt(confStr));
            } catch (NumberFormatException e)
            {
                log.warn("The APSIS Read Timeout parameter (" + confStr
                        + ") cannot be converted to an Integer", e);
            }
        }

        confStr = getConfigurationValue(MODULE_OTHER_APSIS_MAIL_MAX_RETRIES);
        if (confStr == null || confStr.length() == 0)
        {
            log.warn("The APSIS Max Retries has not been set in " + getStoreId());
            staticData.setMaxRetries(maxRetries_DEFAULT);
        } else
        {
            try
            {
                staticData.setMaxRetries(Integer.parseInt(confStr));
            } catch (NumberFormatException e)
            {
                log.warn("The APSIS Max Retries parameter (" + confStr
                        + ") cannot be converted to an Integer", e);
            }
        }

        if (staticData.getApsisUrl() != null && staticData.getApsisUrl().length() > 0
                && staticData.getApiKey() != null && staticData.getApiKey().length() > 0)
        {
            getApsisMailProperties(staticData);
            try
            {
                getProjectIds(staticData);
            } catch (KKException kke)
            {
                // We logged it earlier so we'll just write a warning message
                log.warn("Problem getting the APSIS Project Ids but continuing");
            }
        }

        if (log.isInfoEnabled())
        {
            if (log.isDebugEnabled())
            {
                log.debug(JavaUtils.dumpAllStackTraces(".*JavaUtils.dumpAllStackTraces.*",
                        getDebugStackTraceExclusions()));
            }
            String staticD = "Configuration data for APSIS Mail on " + getStoreId();
            staticD += "\n\t\t\t Enabled?           = " + isAvailable();
            staticD += "\n\t\t\t SortOrder          = " + staticData.getSortOrder();
            staticD += "\n\t\t\t API Key            = " + staticData.getApiKey();
            staticD += "\n\t\t\t APSIS URL          = " + staticData.getApsisUrl();
            staticD += "\n\t\t\t APSIS Mapping File = " + staticData.getApsisMappingFilename();
            staticD += "\n\t\t\t Connect Timeout    = " + staticData.getConnectTimeoutMS();
            staticD += "\n\t\t\t Read Timeout       = " + staticData.getReadTimeoutMS();
            staticD += "\n\t\t\t Maximum retries    = " + staticData.getMaxRetries();
            staticD += "\n\t\t\t LastUpdated        = " + staticData.getLastUpdatedMS();
            log.info(staticD);
        }

        staticData.setLastUpdatedMS(System.currentTimeMillis());

        if (putStaticData)
        {
            staticDataHM.put(getStoreId(), staticData);
        }
    }

    private void getApsisMailProperties(StaticData staticData) throws KKException
    {
        try
        {
            URL mappingPropertiesURL = PropertyFileFinder
                    .findPropertiesURL(staticData.getApsisMappingFilename());
            staticData.setApsisMappingProperties(
                    PropertiesUtils.getConfiguration(mappingPropertiesURL));

            log.info("Read APSIS Mapping Properties:" + mappingPropertiesURL.toExternalForm()
                    + "\nContaining: \n" + PropertiesUtils
                            .configurationToString(staticData.getApsisMappingProperties()));
        } catch (Exception e)
        {
            log.warn("Problem reading APSIS Mapping Properties for store " + getStoreId() + " : "
                    + ExceptionUtils.throwableToString(e));
            return;
        }
    }

    private void getProjectIds(StaticData staticData) throws KKException
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Get APSIS Project Ids");
            }
            String rawResponse = postRequest(staticData, "GET",
                    staticData.getApsisUrl() + "v1/transactional/projects", null);
            if (log.isDebugEnabled())
            {
                log.debug("rawResponse:\n" + rawResponse);
            }

            ProjectsResponse response = getMapper().readValue(rawResponse, ProjectsResponse.class);

            // checkForException(response);

            if (log.isDebugEnabled())
            {
                log.debug("Response result: " + response.toString());
            }

            int mapped = 0;
            int projects = 0;

            if (response.getResult() != null)
            {
                projects = response.getResult().length;
            }

            if (response.getCode() == 1)
            {
                Configuration templates = staticData.getApsisMappingProperties().subset("template");
                for (Iterator<String> iterator = templates.getKeys(); iterator.hasNext();)
                {
                    String template = iterator.next();
                    String ApsisNewsletter = templates.getString(template);
                    for (Project proj : response.getResult())
                    {
                        if (proj.getNewsletterName().startsWith(ApsisNewsletter))
                        {
                            log.info("Map '" + template + "' > '" + proj.getNewsletterName()
                                    + "' ProjectId " + proj.getProjectId());
                            staticData.getTemplateToApsisProjectMap().put(template, proj);
                            mapped++;
                        }
                    }
                }

                if (log.isInfoEnabled())
                {
                    log.info("APSIS Mail: " + projects + " Projects found and " + mapped
                            + " were mapped");
                }
            }

        } catch (Exception e)
        {
            log.warn("Problem getting projects from APSIS\n" + ExceptionUtils.throwableToString(e));
            throw new KKException("Problem getting projects from APSIS", e);
        }
    }

    /**
     * Called to send a mail
     */
    public ExternalMailResult sendExternalMail(ExternalMailInput input) throws Exception
    {
        // Call super to validate the input and set local configuration variables
        super.sendExternalMail(input);

        StaticData staticData = getStaticDataForStore();

        staticDataHM.get(getStoreId());
        if (staticData == null)
        {
            throw new KKException("StaticData for Apsis Mail is null for store " + getStoreId());
        }

        // Create an ApsisTransactionalEmail object
        apsisMail = createApsisTransactionalEmail(staticData, input);

        // Send the mail using APSIS
        if (isAsync)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Sending APSIS Mail Asynchronously");
            }
            EmailSender es = new EmailSender(this);
            new Thread(es).start();
        } else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Sending APSIS Mail Synchronously");
            }
            return sendMail();
        }

        return null;
    }

    private StaticData getStaticDataForStore() throws KKException
    {
        StaticData staticData = staticDataHM.get(getStoreId());
        if (staticData == null)
        {
            throw new KKException("StaticData for Apsis Mail is null for store " + getStoreId());
        }
        return staticData;
    }

    /**
     * Creates and returns a APSIS mail object
     * 
     * @param input
     * @return Returns a APSIS mail object
     * @throws IOException
     * @throws KKException
     */
    private ApsisTransactionalEmail createApsisTransactionalEmail(StaticData staticData,
            ExternalMailInput input) throws IOException, KKException
    {
        ApsisTransactionalEmail apMail = new ApsisTransactionalEmail();

        String myTemplate = templateName;

        if (countryCode != null)
        {
            myTemplate += "_" + countryCode;
        }

        Project proj = staticData.getTemplateToApsisProjectMap().get(myTemplate);

        if (proj == null)
        {
            throw new KKException(
                    "Can't identify the project from the template '" + myTemplate + "'");
        }

        apMail.setProjectId(proj.getProjectId());
        apMail.setAllowInactiveProjects(false);
        ApsisTransactionalEmailBody body = new ApsisTransactionalEmailBody();
        body.setCountryCode(countryCode);
        body.setSendingType("t");

        List<DEMDataField> demDataList = new ArrayList<DEMDataField>();

        DEMDataField demData = new DEMDataField();
        demData.setKey("orderNo");
        if (appOrder != null)
        {
            demData.setValue(appOrder.getOrderNumber());
        } else if (adminOrder != null)
        {
            demData.setValue(adminOrder.getOrderNumber());
        }
        if (demData.getValue() != null)
        {
            demDataList.add(demData);
        }

        demData = new DEMDataField();
        demData.setKey("deliveryPostcode");
        if (appOrder != null)
        {
            demData.setValue(appOrder.getDeliveryPostcode());
        } else if (adminOrder != null)
        {
            demData.setValue(adminOrder.getDeliveryPostcode());
        }
        if (demData.getValue() != null)
        {
            demDataList.add(demData);
        }

        demData = new DEMDataField();
        demData.setKey("deliveryStreetAddress");
        if (appOrder != null)
        {
            demData.setValue(appOrder.getDeliveryStreetAddress());
        } else if (adminOrder != null)
        {
            demData.setValue(adminOrder.getDeliveryStreetAddress());
        }
        if (demData.getValue() != null)
        {
            demDataList.add(demData);
        }

        demData = new DEMDataField();
        demData.setKey("storeOwner");
        demData.setValue(storeOwner);
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("storeName");
        demData.setValue(storeName);
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("imageBaseUrl");
        demData.setValue(imageBaseUrl);
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("customerName");
        demData.setValue(customerName);
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("newPassword");
        demData.setValue(newPassword);
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("expiryMins");
        demData.setValue(String.valueOf(expiryMins));
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("productName");
        demData.setValue(productName);
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("productQuantity");
        demData.setValue(String.valueOf(productQuantity));
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("productId");
        demData.setValue(String.valueOf(productId));
        demDataList.add(demData);

        demData = new DEMDataField();
        demData.setKey("sku");
        demData.setValue(sku);
        demDataList.add(demData);

        if (input.getAppOptions() != null && input.getAppOptions().getCustomAttrs() != null)
        {
            for (NameValueIf nameValue : input.getAppOptions().getCustomAttrs())
            {
                demData = new DEMDataField();
                demData.setKey(nameValue.getName());
                demData.setValue(nameValue.getValue());
                demDataList.add(demData);
            }
        }

        if (input.getAdminOptions() != null && input.getAdminOptions().getCustomAttrs() != null)
        {
            for (AdminNameValue nameValue : input.getAdminOptions().getCustomAttrs())
            {
                demData = new DEMDataField();
                demData.setKey(nameValue.getName());
                demData.setValue(nameValue.getValue());
                demDataList.add(demData);
            }
        }

        body.setDemDataFields(demDataList.toArray(new DEMDataField[0]));

        if (fullAttachmentFilename != null)
        {
            TransactionalAttachment[] attachments = new TransactionalAttachment[1];
            TransactionalAttachment attachment = new TransactionalAttachment();
            attachment.setContentType(getContentType());
            attachment.setName(friendlyAttachmentName);
            attachment.setContent(getEncodedFileContent());
            attachments[0] = attachment;

            body.setAttachments(attachments);
        }

        body.setEmail(emailAddr);
        if (customerName != null)
        {
            body.setName(customerName);
        }

        if (templateName.equals("OrderConfReceived"))
        {
            int startIdx = emailBody.indexOf("<table");
            startIdx = emailBody.indexOf("<table", startIdx + 1);
            startIdx = emailBody.indexOf("<table", startIdx + 1);
            startIdx = emailBody.indexOf("<table", startIdx + 1);
            int endIndex = emailBody.indexOf("</body>");
            String orderDetails = emailBody.substring(startIdx, endIndex);
            body.setDDHtml(orderDetails);
        }

        apMail.setBody(body);

        if (log.isDebugEnabled())
        {
            log.debug("Apsis Mail:" + apMail.toString());
        }

        return apMail;
    }

    /**
     * Sends a mail using APSIS
     * 
     * @return Returns an ExternalMailResult object
     * 
     * @throws Exception
     */
    public ExternalMailResult sendMail() throws Exception
    {
        log.warn("Send APSIS mail");

        if (apsisMail == null)
        {
            throw new KKException(
                    "The apsisMail object must not be null when sendMail() is called");
        }

        log.warn("Send the Email using APSIS");

        String request = "v1/transactional/projects/" + apsisMail.getProjectId()
                + "/sendEmail?allowInactiveProjects=" + apsisMail.isAllowInactiveProjects();

        log.info("request : " + request);

        String params = getMapper().writeValueAsString(apsisMail.getBody());

        log.info("params :\n" + params);
        log.info("params :\n" + prettyJson(params));

        StaticData staticData = getStaticDataForStore();
        String rawResponse = postRequest(staticData, "POST", staticData.getApsisUrl() + request,
                params);

        log.info("Raw Response  :\n" + rawResponse);
        log.info("JSON Response :\n" + prettyJson(rawResponse));

        TransactionalMailResponse response = getMapper().readValue(rawResponse,
                TransactionalMailResponse.class);

        log.info("Java Response :" + response.toString());

        // Deletes the attachment file if present and if instructed to do so
        deleteAttachmentFile();

        ExternalMailResult ret = new ExternalMailResult();
        ret.setError(false);
        return ret;
    }

    /**
     * @return Returns true if the service is available
     */
    public boolean isAvailable() throws KKException
    {
        return isAvailable("MODULE_OTHER_APSIS_MAIL_STATUS");
    }

    /**
     * @param staticData
     * @param requestMethod
     * @param request
     * @param params
     * @return the response
     * @throws Exception
     */
    public String postRequest(StaticData staticData, String requestMethod, String request,
            String params) throws Exception
    {
        int maxAttempts = staticData.getMaxRetries();
        return postRequest(staticData, requestMethod, request, params, maxAttempts);
    }

    /**
     * @param staticData
     * @param requestMethod
     * @param request
     * @param params
     * @param maxAttempts
     * @return the response
     * @throws Exception
     */
    public String postRequest(StaticData staticData, String requestMethod, String request,
            String params, int maxAttempts) throws Exception
    {
        String response = null;
        int attempt = 1;

        if (log.isDebugEnabled())
        {
            log.debug(
                    requestMethod + " Request: (Raw)\n" + request + (params != null ? params : ""));
        }

        // Check the APSIS set-up data is OK... throw an exception if not
        checkApsisSetup(staticData);

        while (response == null && attempt <= maxAttempts)
        {
            try
            {
                long startTime = System.currentTimeMillis();

                HttpURLConnection conn = (HttpURLConnection) (new URL(request)).openConnection();

                log.info("Opened connection to : " + conn.getURL().toString());

                // Set connection parameters.
                conn.setConnectTimeout(staticData.getConnectTimeoutMS());
                conn.setReadTimeout(staticData.getReadTimeoutMS());

                if (requestMethod.equals("POST") || requestMethod.equals("DELETE"))
                {
                    conn.setDoInput(true);
                }
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Accept-Encoding", "gzip,deflate");

                // Add authorization header
                String authKey = staticData.getApiKey() + ":";
                String encodedkey = "Basic " + Base64.encodeBase64String(authKey.getBytes());
                log.debug("Authorization: " + encodedkey);
                conn.setRequestProperty("Authorization", encodedkey);

                conn.setRequestMethod(requestMethod);

                log.debug("Request Properties:\n" + conn.getRequestProperties());

                if (params != null)
                {
                    OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),
                            "utf-8");
                    {
                        out.write(params);
                    }
                    out.flush();
                    out.close();
                }

                int responseCode = conn.getResponseCode();
                log.info("Connection Response Code : " + responseCode);

                if (responseCode >= 400 && responseCode <= 500)
                {
                    // Read response from the error stream.
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), "utf-8"));
                    String temp = null;
                    while ((temp = in.readLine()) != null)
                    {
                        if (response == null)
                        {
                            response = temp + "\n";
                        } else
                        {
                            response += temp + "\n";
                        }
                    }
                    in.close();
                } else
                {
                    // Read response from the input stream.
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String temp = null;
                    while ((temp = in.readLine()) != null)
                    {
                        if (response == null)
                        {
                            response = temp + "\n";
                        } else
                        {
                            response += temp + "\n";
                        }
                    }
                    in.close();

                    if (log.isDebugEnabled())
                    {
                        log.debug("APSIS Response (Raw):\n" + response);
                        log.debug("APSIS Response (JSON):\n" + prettyJson(response));
                    }
                }

                for (Entry<String, List<String>> header : conn.getHeaderFields().entrySet())
                {
                    log.debug(header.getKey() + "=" + header.getValue());
                }

                long endTime = System.currentTimeMillis();

                log.info(request + " took " + (endTime - startTime) + " ms (attempt " + attempt
                        + ")");

            } catch (Exception e)
            {
                log.warn(ExceptionUtils.exceptionToString(e));

                if (e instanceof java.net.SocketTimeoutException || e instanceof KKException)
                {
                    if (attempt == maxAttempts)
                    {
                        log.warn("Giving up with " + request + " call after " + maxAttempts
                                + " attempts");
                        throw e;
                    }
                    log.info("Try again");
                    attempt++;
                } else
                {
                    throw e;
                }
            }
        }

        return response;
    }

    /**
     * Make some checks to confirm the setup is OK to post messages
     * 
     * @param staticData
     *            the APSIS Mail StaticData for the store
     * @throws KKException
     *             if the set-up isn't sufficient to call APSIS.
     */
    protected void checkApsisSetup(StaticData staticData) throws KKException
    {
        if (staticData == null)
        {
            throw new KKException("APSIS Mail staticData is null");
        }

        if (staticData.getApiKey() == null || staticData.getApiKey().length() < 1)
        {
            throw new KKException("APSIS Mail API Key is not defined");
        }

        if (staticData.getApsisUrl() == null || staticData.getApsisUrl().length() < 1)
        {
            throw new KKException("APSIS Mail URL is not defined");
        }
    }

    /**
     * To pretty-print a JSON String
     * 
     * @param uglyJsonString
     *            the raw JSON String
     * @return the pretty version of the input
     * @throws KKException
     *             if there is a problem printing the JSON
     */
    public String prettyJson(String uglyJsonString) throws KKException
    {
        if (uglyJsonString == null)
        {
            return "null";
        }

        if (uglyJsonString.length() == 0)
        {
            return "";
        }

        try
        {
            JsonNode json = getMapper().readValue(uglyJsonString, JsonNode.class);
            return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e)
        {
            log.warn("Problem pretty-printing:\n" + uglyJsonString);
            log.warn(ExceptionUtils.exceptionToString(e));
            throw new KKException("Problem printing JSON", e);
        }
    }

    /**
     * @return the mapper
     */
    public ObjectMapper getMapper()
    {
        if (mapper == null)
        {
            mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_NULL);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mapper;
    }

    /**
     * @return An array of mailing lists
     * @throws KKException
     */
    public MailingList[] getMailingLists() throws KKException
    {
        StaticData staticData = getStaticDataForStore();
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Get APSIS Mailing Lists");
            }

            // Needed to add a " " as a parameter otherwise this POST with no real body fails
            String rawResponse = postRequest(staticData, "POST",
                    staticData.getApsisUrl() + "mailinglists/v2/all", "");
            // String rawResponse = postRequest(staticData, "GET", "v1/mailinglists/1/999", null);
            if (log.isDebugEnabled())
            {
                log.debug("rawResponse:\n" + rawResponse);
            }

            MailingListResponse response = getMapper().readValue(rawResponse,
                    MailingListResponse.class);

            // checkForException(response);

            if (log.isDebugEnabled())
            {
                log.debug("Response result: " + response.toString());
            }

            int mLists = 0;

            if (response.getResult() != null)
            {
                mLists = response.getResult().length;
            }

            if (log.isInfoEnabled())
            {
                log.info("APSIS Mail: " + mLists + " Mailing Lists found");
            }

            return response.getResult();

        } catch (Exception e)
        {
            log.warn(ExceptionUtils.throwableToString(e));
            throw new KKException("Problem getting projects from APSIS", e);
        }
    }

    /**
     * @param allDemographics
     * @return An array of Subscribers
     * @throws KKException
     */
    public SubscriberResponse[] getAllSubscribers(boolean allDemographics) throws KKException
    {
        StaticData staticData = getStaticDataForStore();
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Get All Subscribers (Queued)");
            }

            // Needed to add a " " as a parameter otherwise this POST with no real body fails
            String rawResponse = postRequest(staticData, "POST",
                    staticData.getApsisUrl() + "v1/subscribers/all",
                    "{\"AllDemographics\": " + allDemographics + ",\"FieldNames\": []}");
            // String rawResponse = postRequest(staticData, "GET", "v1/mailinglists/1/999", null);
            if (log.isDebugEnabled())
            {
                log.debug("rawResponse:\n" + rawResponse);
            }

            QueuedResponse response = getMapper().readValue(rawResponse, QueuedResponse.class);

            // checkForException(response);

            if (log.isDebugEnabled())
            {
                log.debug("Response result: " + response.toString());
            }

            if (response.getCode() != 1)
            {
                throw new KKException(
                        "Problem getting Subscribers from APSIS: " + response.toString());
            }

            String pollURL = response.getResult().getPollURL();

            String rawDataResponse = pollForResults(pollURL, "Retrieve Subscribers", staticData);

            SubscriberResponse[] subscribers = getMapper().readValue(rawDataResponse,
                    SubscriberResponse[].class);

            SubscribersResponse subscribersResponse = new SubscribersResponse();
            subscribersResponse.setSubscribers(subscribers);

            if (log.isInfoEnabled())
            {
                log.info("Response from Retrieving Data: " + subscribersResponse.toString());
            }

            if (log.isInfoEnabled())
            {
                log.info(subscribers.length + " APSIS Subscribers found");
            }

            return subscribers;

        } catch (Exception e)
        {
            log.warn(ExceptionUtils.throwableToString(e));
            throw new KKException("Problem getting Subscribers from APSIS", e);
        }
    }

    /**
     * @param subscribersToCreate
     * @param mailingListId
     * @return a CreateSubscribersResponse object
     * @throws KKException
     */
    public CreateSubscribersResponse createSubscribers(List<Subscriber> subscribersToCreate,
            int mailingListId) throws KKException
    {
        // List<String> subscriberIdsList = new ArrayList<String>();
        StaticData staticData = getStaticDataForStore();
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Create Subscribers (Queued)");
            }

            Subscriber[] subsArray = subscribersToCreate
                    .toArray(new Subscriber[subscribersToCreate.size()]);
            String params = getMapper().writeValueAsString(subsArray);

            log.info("params :\n" + prettyJson(params));

            int maxRetries = 4;

            String rawResponse = postRequest(staticData, "POST", staticData.getApsisUrl()
                    + "/v1/subscribers/mailinglist/" + mailingListId + "/queue", params,
                    maxRetries);
            // String rawResponse = postRequest(staticData, "GET", "v1/mailinglists/1/999", null);
            if (log.isDebugEnabled())
            {
                log.debug("rawResponse:\n" + rawResponse);
            }

            QueuedResponse response = getMapper().readValue(rawResponse, QueuedResponse.class);

            // checkForException(response);

            if (log.isDebugEnabled())
            {
                log.debug("Response result: " + response.toString());
            }

            if (response.getCode() != 1)
            {
                throw new KKException(
                        "Problem creating/updating Subscribers on APSIS: " + response.toString());
            }

            String pollURL = response.getResult().getPollURL();

            String rawDataResponse = pollForResults(pollURL, "Creation/Update of Subscribers",
                    staticData);

            CreateSubscribersResponse respResults = getMapper().readValue(rawDataResponse,
                    CreateSubscribersResponse.class);

            if (log.isInfoEnabled())
            {
                log.info("Response from Creation/Update of Subscribers Retrieving Data: "
                        + respResults.toString());
            }

            if (log.isInfoEnabled())
            {
                if (respResults.getCreatedSubscriberIds() == null)
                {
                    log.info("No APSIS Subscribers Created/Updated");
                } else
                {
                    log.info(respResults.getCreatedSubscriberIds().length
                            + " APSIS Subscribers Created/Updated");
                }

                if (respResults.getFailedCreatedSubscribers() == null)
                {
                    log.info("No APSIS Subscriber Creations/Updates Failed");
                } else
                {
                    log.info(respResults.getFailedCreatedSubscribers().length
                            + " APSIS Subscriber Creations/Updates Failed");
                }
            }

            return respResults;

        } catch (Exception e)
        {
            log.warn(ExceptionUtils.throwableToString(e));
            throw new KKException("Problem creating/updating Subscribers on APSIS", e);
        }
    }

    /**
     * @param pollURL
     * @param pollDataDesc
     * @param staticData
     * @return a raw data response
     * @throws Exception
     */
    public String pollForResults(String pollURL, String pollDataDesc, StaticData staticData)
            throws Exception
    {
        if (log.isInfoEnabled())
        {
            log.info("Poll '" + pollURL + "' for the results");
        }

        long startTime = System.currentTimeMillis();
        long timeNowMs = startTime;
        long maxWaitMs = 1000L * 60 * 60; // one hour
        maxWaitMs = 1000L * 60 * 10; // two minutes
        long maxTimeMs = startTime + maxWaitMs;
        long sleepTimeMs = 1000L * 10; // start at 10 secs
        long sleepIncMs = 1000L * 10; // start at 10 secs

        PollResponse pollResponse = null;
        int attempt = 0;
        while (timeNowMs < maxTimeMs)
        {
            attempt++;
            if (log.isInfoEnabled())
            {
                if (attempt == 1)
                {
                    log.info("Poll for " + pollDataDesc);
                } else
                {
                    log.info("Poll for " + pollDataDesc + " " + (timeNowMs - startTime) / 1000
                            + " seconds elapsed");
                }
            }
            try
            {
                String rawPollResponse = postRequest(staticData, "GET", pollURL, null);

                if (log.isInfoEnabled())
                {
                    log.info("Response from Poll for " + pollDataDesc + " after "
                            + (timeNowMs - startTime) / 1000 + " seconds:\n" + rawPollResponse);
                }

                try
                {
                    pollResponse = getMapper().readValue(rawPollResponse, PollResponse.class);
                } catch (JsonParseException jpe)
                {
                    log.warn("Could not parse JSON repsonse due to " + jpe.getMessage());
                    pollResponse = null;
                }

                if (pollResponse != null)
                {
                    if (log.isInfoEnabled())
                    {
                        log.info("Response from Poll for " + pollDataDesc + " : "
                                + pollResponse.toString());
                    }

                    if (pollResponse.getState().equals("2") && pollResponse.getDataUrl() != null)
                    {
                        break;
                    }
                }
            } catch (Exception e)
            {
                log.warn("Problem polling for results at APSIS - " + e.getMessage());
                if (log.isDebugEnabled())
                {
                    log.debug(ExceptionUtils.exceptionToString(e));
                }
            }
            Thread.sleep(sleepTimeMs);
            sleepTimeMs += sleepIncMs;
            timeNowMs = System.currentTimeMillis();
        }

        if (pollResponse == null)
        {
            String msg = "Problem parsing JSON returned for " + pollDataDesc + " after "
                    + (timeNowMs - startTime) / 1000 + " seconds";
            throw new KKException(msg);
        }

        if (!pollResponse.getState().equals("2"))
        {
            String msg = "Gave up waiting for " + pollDataDesc + " to complete after "
                    + (timeNowMs - startTime) / 1000 + " seconds - poll state is "
                    + pollResponse.getState();
            throw new KKException(msg);
        }

        if (pollResponse.getDataUrl() == null)
        {
            String msg = "Gave up waiting for " + pollDataDesc + " to complete after "
                    + (timeNowMs - startTime) / 1000 + " seconds - dataURL is null";
            throw new KKException(msg);
        }

        if (log.isInfoEnabled())
        {
            log.info("Now retrieve the datafile for " + pollDataDesc + " at "
                    + pollResponse.getDataUrl());
        }

        String rawDataResponse = postRequest(staticData, "GET", pollResponse.getDataUrl(), null);

        if (log.isInfoEnabled())
        {
            log.info("Response from " + pollDataDesc + " Retrieving Data: " + rawDataResponse);
        }

        return rawDataResponse;
    }

    /**
     * @return the number of subscribers removed
     * @throws KKException
     */
    public int removeAllSubscribers() throws KKException
    {
        int removeCount = 0;
        StaticData staticData = getStaticDataForStore();
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Remove All Subscribers (Queued)");
            }

            boolean allDemographics = false;
            SubscriberResponse[] subscribers = getAllSubscribers(allDemographics);

            if (subscribers == null || subscribers.length == 0)
            {
                return removeCount;
            }

            List<Integer> subscribersToDelete = new ArrayList<Integer>();

            for (SubscriberResponse sub : subscribers)
            {
                removeCount += removeSubscriber(sub.getId(), subscribersToDelete, staticData);
            }

            // Flush out any left to remove...

            removeCount += removeSubscribers(subscribersToDelete, staticData);

            if (log.isInfoEnabled())
            {
                log.info(removeCount + " APSIS Subscribers deleted (total)");
            }

            return removeCount;

        } catch (Exception e)
        {
            log.warn(ExceptionUtils.throwableToString(e));
            throw new KKException("Problem removing Subscribers from APSIS", e);
        }
    }

    protected int removeSubscriber(String id, List<Integer> subscribersToDelete,
            StaticData staticData) throws Exception
    {
        int REMOVE_CHUNK_SIZE = 500;

        subscribersToDelete.add(Integer.valueOf(id));

        if (subscribersToDelete.size() == REMOVE_CHUNK_SIZE)
        {
            return removeSubscribers(subscribersToDelete, staticData);
        }

        return 0;
    }

    protected int removeSubscribers(List<Integer> subscribersToDelete, StaticData staticData)
            throws Exception
    {
        String params = "[";
        int subCount = 0;
        for (Integer subId : subscribersToDelete)
        {
            if (subCount > 0)
            {
                params += ",";
            }
            params += subId;
            subCount++;
        }
        params += "]";

        String rawResponse = postRequest(staticData, "DELETE",
                staticData.getApsisUrl() + "subscribers/v2/id", params);

        if (log.isDebugEnabled())
        {
            log.debug("rawResponse:\n" + rawResponse);
        }

        QueuedResponse response = getMapper().readValue(rawResponse, QueuedResponse.class);

        // checkForException(response);

        if (log.isDebugEnabled())
        {
            log.debug("Response result: " + response.toString());
        }

        if (response.getCode() != 1)
        {
            throw new KKException(
                    "Problem getting Remove All Subscribers from APSIS: " + response.toString());
        }

        String pollURL = response.getResult().getPollURL();

        String rawDataResponse = pollForResults(pollURL, "Remove All Subscribers", staticData);

        Integer[] results = getMapper().readValue(rawDataResponse, Integer[].class);

        if (log.isInfoEnabled())
        {
            log.info("Response from Retrieving Data: " + results[0]);
        }

        subscribersToDelete.clear();
        return results[0];
    }

    /**
     * Used to store the static data of this module
     */
    protected class StaticData
    {
        private int sortOrder = -1;

        // API key required to send a mail
        private String apiKey = null;

        // APSIS Service URL
        private String apsisUrl = null;

        // APSIS Mapping filename
        private String apsisMappingFilename = null;

        /**
         * KK Template Name to APSIS Project Map
         */
        private HashMap<String, Project> templateToApsisProjectMap = null;

        /**
         * Mapping Properties
         */
        private PropertiesConfiguration apsisMappingProperties = null;

        // connectTimeoutMS
        private int connectTimeoutMS = connectTimeoutMS_DEFAULT;

        // readTimeoutMS
        private int readTimeoutMS = readTimeoutMS_DEFAULT;

        // maxRetries
        private int maxRetries = maxRetries_DEFAULT;

        // lastUpdatedMS
        private long lastUpdatedMS = -1;

        /**
         * @return the lastUpdatedMS
         */
        public long getLastUpdatedMS()
        {
            return lastUpdatedMS;
        }

        /**
         * @param lastUpdatedMS
         *            the lastUpdatedMS to set
         */
        public void setLastUpdatedMS(long lastUpdatedMS)
        {
            this.lastUpdatedMS = lastUpdatedMS;
        }

        /**
         * @return the sortOrder
         */
        public int getSortOrder()
        {
            return sortOrder;
        }

        /**
         * @param sortOrder
         *            the sortOrder to set
         */
        public void setSortOrder(int sortOrder)
        {
            this.sortOrder = sortOrder;
        }

        /**
         * @return the apiKey
         */
        public String getApiKey()
        {
            return apiKey;
        }

        /**
         * @param apiKey
         *            the apiKey to set
         */
        public void setApiKey(String apiKey)
        {
            this.apiKey = apiKey;
        }

        /**
         * @return the apsisUrl
         */
        public String getApsisUrl()
        {
            return apsisUrl;
        }

        /**
         * @param apsisUrl
         *            the apsisUrl to set
         */
        public void setApsisUrl(String apsisUrl)
        {
            this.apsisUrl = apsisUrl;
        }

        /**
         * @return the templateToApsisProjectMap
         */
        public HashMap<String, Project> getTemplateToApsisProjectMap()
        {
            if (templateToApsisProjectMap == null)
            {
                templateToApsisProjectMap = new HashMap<String, Project>();
            }
            return templateToApsisProjectMap;
        }

        /**
         * @param templateToApsisProjectMap
         *            the templateToApsisProjectMap to set
         */
        public void setTemplateToApsisProjectMap(HashMap<String, Project> templateToApsisProjectMap)
        {
            this.templateToApsisProjectMap = templateToApsisProjectMap;
        }

        /**
         * @return the apsisMappingProperties
         */
        public PropertiesConfiguration getApsisMappingProperties()
        {
            if (apsisMappingProperties == null)
            {
                apsisMappingProperties = new PropertiesConfiguration();
            }
            return apsisMappingProperties;
        }

        /**
         * @param apsisMappingProperties
         *            the apsisMappingProperties to set
         */
        public void setApsisMappingProperties(PropertiesConfiguration apsisMappingProperties)
        {
            this.apsisMappingProperties = apsisMappingProperties;
        }

        /**
         * @return the apsisMappingFilename
         */
        public String getApsisMappingFilename()
        {
            return apsisMappingFilename;
        }

        /**
         * @param apsisMappingFilename
         *            the apsisMappingFilename to set
         */
        public void setApsisMappingFilename(String apsisMappingFilename)
        {
            this.apsisMappingFilename = apsisMappingFilename;
        }

        /**
         * @return the connectTimeoutMS
         */
        public int getConnectTimeoutMS()
        {
            return connectTimeoutMS;
        }

        /**
         * @param connectTimeoutMS
         *            the connectTimeoutMS to set
         */
        public void setConnectTimeoutMS(int connectTimeoutMS)
        {
            this.connectTimeoutMS = connectTimeoutMS;
        }

        /**
         * @return the readTimeoutMS
         */
        public int getReadTimeoutMS()
        {
            return readTimeoutMS;
        }

        /**
         * @param readTimeoutMS
         *            the readTimeoutMS to set
         */
        public void setReadTimeoutMS(int readTimeoutMS)
        {
            this.readTimeoutMS = readTimeoutMS;
        }

        /**
         * @return the maxRetries
         */
        public int getMaxRetries()
        {
            return maxRetries;
        }

        /**
         * @param maxRetries
         *            the maxRetries to set
         */
        public void setMaxRetries(int maxRetries)
        {
            this.maxRetries = maxRetries;
        }
    }
}
