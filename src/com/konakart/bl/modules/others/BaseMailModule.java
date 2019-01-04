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
//
package com.konakart.bl.modules.others;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import com.konakart.app.Customer;
import com.konakart.app.ExternalMailInput;
import com.konakart.app.ExternalMailResult;
import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.modules.BaseModule;
import com.konakart.util.KKConstants;
import com.konakartadmin.app.AdminCustomer;
import com.konakartadmin.app.AdminOrder;
import com.konakartadmin.app.KKAdminException;
import com.konakartadmin.appif.KKAdminIf;
import com.konakartadmin.bl.AdminMgrFactory;
import com.konakartadmin.blif.AdminConfigurationMgrIf;

/**
 * Base class for all external mail modules.
 */
public class BaseMailModule extends BaseModule
{
    /** the log */
    private static Log log = LogFactory.getLog(BaseMailModule.class);

    private KKAdminIf adminEng;

    private AdminMgrFactory adminMgrFactory;

    /** KonaKart API name */
    protected static final String sendNewPassword1 = "sendNewPassword1";

    /** KonaKart API name */
    protected static final String sendWelcomeEmail1 = "sendWelcomeEmail1";

    /** KonaKart API name */
    protected static final String sendOrderConfirmationEmail1 = "sendOrderConfirmationEmail1";

    /** KonaKart API name */
    protected static final String sendTemplateEmailToCustomer1 = "sendTemplateEmailToCustomer1";

    /** KonaKart API name */
    protected static final String sendStockReorderEmail = "sendStockReorderEmail";

    /** KonaKartAdmin API name */
    protected static final String resetCustomerPassword = "resetCustomerPassword";

    /** KonaKartAdmin API name */
    protected static final String sendStatusChangeEmailForState = "sendStatusChangeEmailForState";

    /** KonaKartAdmin API name */
    protected static final String sendTemplateEmail = "sendTemplateEmail";

    /** KonaKartAdmin API name */
    protected static final String sendEmail = "sendEmail";

    /** API called to send the mail */
    protected String konakartAPI;

    /** From address in string format */
    protected String fromAddressStr;

    /** From name */
    protected String fromName;

    /** Reply to address in string format */
    protected String replyToAddressStr;

    /** Reply to name */
    protected String replyToName;

    /** Blind copy addresses */
    protected String[] bccEmailsStrArray = null;

    /** Debug when set to true */
    protected boolean debugEmail = false;

    /** Blind copy activated when true */
    protected boolean doBlindCopy = true;

    /** Velocity Context */
    protected VelocityContext context;

    /** Store Owner */
    protected String storeOwner;

    /** Email address of store owner */
    protected String storeOwnerEmailAddr;

    /** Name of store */
    protected String storeName;

    /** Id of store */
    protected String storeId;

    /** Image base URL */
    protected String imageBaseUrl;

    /** Velocity template base directory */
    protected String templateBase;

    /** Prices displayed with tax if true */
    protected boolean displayPricesWithTax;

    /** Name of customer */
    protected String customerName;

    /** Application customer object */
    protected Customer appCustomer;

    /** Admin customer object */
    protected AdminCustomer adminCustomer;

    /** Subject of email */
    protected String emailSubject;

    /** Body of email */
    protected String emailBody;

    /** Destination email address */
    protected String emailAddr;

    /** country code - it's really locale - eg: "en", "es", "de" etc */
    protected String countryCode;

    /** template name */
    protected String templateName;

    /** Email message */
    protected String message;

    /** Application order object */
    protected Order appOrder;

    /** Application vendor order objects when in multi-vendor mode */
    protected Order[] vendorOrders;

    /** Admin order object */
    protected AdminOrder adminOrder;

    /** New password */
    protected String newPassword;

    /** Length of time in minutes before password expires */
    protected int expiryMins = 0;

    /** Content type */
    protected String contentType;

    /** When true, email is sent asynchronously */
    protected boolean isAsync = true;

    /** Full attachment file name */
    protected String fullAttachmentFilename;

    /** Friendly attachment file name */
    protected String friendlyAttachmentName;

    /** The attachment should be deleted after send when true */
    protected boolean deleteAttachmentAfterSend = false;

    /** Set for sendStockReorderEmail */
    protected String productName;

    /** Set for sendStockReorderEmail */
    protected int productQuantity;

    /** Set for sendStockReorderEmail */
    protected int productId;

    /** Set for sendStockReorderEmail */
    protected String sku;

    /**
     * @return the adminEng
     */
    public KKAdminIf getAdminEng()
    {
        return adminEng;
    }

    /**
     * Init method called from the super class when called from the Admin engine
     * 
     * @param adminEng
     *            the adminEng
     */
    protected void init(KKAdminIf adminEng)
    {
        this.adminEng = adminEng;
        adminMgrFactory = new AdminMgrFactory(this.adminEng);
    }

    /**
     * @param adminEng
     *            the adminEng to set
     */
    public void setAdminEng(KKAdminIf adminEng)
    {
        this.adminEng = adminEng;
    }

    /**
     * Return the storeId
     * 
     * @return Return the storeId
     * @throws KKException
     *             an unexpected exception from the KonaKart engine
     */
    protected String getStoreId() throws KKException
    {
        if (getEng() != null)
        {
            return super.getStoreId();
        }

        if (adminEng == null)
        {
            throw new KKException("This manager has been instantiated with KKAdmin set to null");
        }

        try
        {
            if (adminEng.getEngConf() == null || adminEng.getEngConf().getStoreId() == null)
            {
                return KKConstants.KONAKART_DEFAULT_STORE_ID;
            }
            return adminEng.getEngConf().getStoreId();
        } catch (KKAdminException kkae)
        {
            throw new KKException(kkae.getMessage());
        }
    }

    /**
     * Called by the actual mail module to perform common tasks such as validating the input and
     * setting local configuration variables
     * 
     * @param input
     *            the input
     * @return Returns null
     * @throws Exception
     *             an unexpected exception
     */
    public ExternalMailResult sendExternalMail(ExternalMailInput input) throws Exception
    {
        // Validate input
        validateExternalMailInput(input);

        // Set local configuration values used to send the mail, from the input data
        setConfigsFromInput(input);

        // Display debug information
        if (log.isDebugEnabled())
        {
            log.debug(getMailDebugInfo());
        }

        return null;
    }

    /**
     * Set local configuration values from configuration variables
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    protected void setConfigs() throws KKException
    {
        fromAddressStr = getConfigurationValue(ConfigConstants.EMAIL_FROM);
        if (fromAddressStr == null)
        {
            throw new KKException(
                    "An email cannot be sent because the EMAIL_FROM configuration has not been set.");
        }
        setFromAddressStr(fromAddressStr);

        replyToAddressStr = getConfigurationValue(ConfigConstants.EMAIL_REPLY_TO);
        if (replyToAddressStr == null)
        {
            replyToAddressStr = fromAddressStr;
        }
        setReplyToAddressStr(replyToAddressStr);

        debugEmail = getConfigurationValueAsBool(ConfigConstants.DEBUG_EMAIL_SESSIONS, false);

        String bccEmails = getConfigurationValue(ConfigConstants.SEND_EXTRA_EMAILS_TO);
        if (bccEmails != null && bccEmails.length() > 0)
        {
            bccEmailsStrArray = bccEmails.split(";");
        }
    }

    /**
     * Set local configuration values used to send the mail, from the input data
     * 
     * @param input
     *            the input
     * @throws Exception
     *             an unexpected exception
     */
    protected void setConfigsFromInput(ExternalMailInput input) throws Exception
    {
        // Instantiate some attributes
        konakartAPI = input.getKonakartAPI();
        context = input.getContext();
        if (context != null)
        {
            storeOwner = (String) context.get("storeOwner");
            storeOwnerEmailAddr = (String) context.get("storeOwnerEmailAddr");
            storeName = (String) context.get("storeName");
            storeId = (String) context.get("storeId");
            imageBaseUrl = (String) context.get("imageBaseUrl");
            templateBase = (String) context.get("templateBase");
            displayPricesWithTax = ((Boolean) context.get("displayPricesWithTax")).booleanValue();
            customerName = (String) context.get("customerName");
            appCustomer = (Customer) context.get("customer");
            adminCustomer = (AdminCustomer) context.get("cust");
        }
        if (customerName == null && adminCustomer != null)
        {
            customerName = getFormattedCustomerName(adminCustomer);
        }
        if (input.getEmailTemplate() != null)
        {
            emailSubject = input.getEmailTemplate().getSubject();
            emailBody = input.getEmailTemplate().getBody();
        }
        emailAddr = input.getEmailAddr();
        if (input.getAppOptions() != null)
        {
            countryCode = input.getAppOptions().getCountryCode();
            templateName = input.getAppOptions().getTemplateName();
        }
        if (input.getAdminOptions() != null)
        {
            countryCode = input.getAdminOptions().getCountryCode();
            templateName = input.getAdminOptions().getTemplateName();
        }
        if (input.getAdminEmailData() != null)
        {
            emailAddr = input.getAdminEmailData().getToAddress();
            emailSubject = input.getAdminEmailData().getSubject();
            emailBody = input.getAdminEmailData().getMessage();
            // Some mail providers don't accept an empty message
            if (emailBody == null || emailBody.length() == 0)
            {
                emailBody = " ";
            }
            doBlindCopy = input.getAdminEmailData().isSendBlindCopies();
        }

        // Override values from config variables
        if (input.getAppOptions() != null)
        {
            /*
             * Mail sent from Application
             */
            if (input.getAppOptions().getFromAddress() != null)
            {
                setFromAddressStr(input.getAppOptions().getFromAddress());
            } else if (input.getAppOptions().getReplyToAddress() != null)
            {
                setReplyToAddressStr(input.getAppOptions().getReplyToAddress());
            } else if (input.getAppOptions().getBccEmails() != null
                    && input.getAppOptions().getBccEmails().length() > 0)
            {
                bccEmailsStrArray = input.getAppOptions().getBccEmails().split(";");
            }

            if (input.getAppOptions().getContentType() == KKConstants.EMAIL_AS_HTML)
            {
                contentType = "text/html";
            } else if (input.getAppOptions().getContentType() == KKConstants.EMAIL_AS_PLAIN_TEXT)
            {
                contentType = "text/plain";
            } else
            {
                log.warn("Unrecognised Email ContentType: "
                        + input.getAppOptions().getContentType());
                contentType = "text/html";
            }

            fullAttachmentFilename = input.getAppOptions().getFullAttachmentFilename();
            friendlyAttachmentName = input.getAppOptions().getFriendlyAttachmentName();
            deleteAttachmentAfterSend = input.getAppOptions().isDeleteAttachmentAfterSend();
        } else if (input.getAdminEmailData() != null)
        {
            /*
             * Mail sent using admin API call sendEmail
             */
            if (input.getAdminEmailData().getFromAddress() != null)
            {
                setFromAddressStr(input.getAdminEmailData().getFromAddress());
            } else if (input.getAdminEmailData().getReplyToAddress() != null)
            {
                setReplyToAddressStr(input.getAdminEmailData().getReplyToAddress());
            } else if (input.getAdminEmailData().getBccEmails() != null
                    && input.getAdminEmailData().getBccEmails().length() > 0)
            {
                bccEmailsStrArray = input.getAdminEmailData().getBccEmails().split(";");
            }

            if (input.getAdminEmailData().getContentType() == KKConstants.EMAIL_AS_HTML)
            {
                contentType = "text/html";
            } else if (input.getAdminEmailData()
                    .getContentType() == KKConstants.EMAIL_AS_PLAIN_TEXT)
            {
                contentType = "text/plain";
            } else
            {
                log.warn("Unrecognised Email ContentType: "
                        + input.getAdminEmailData().getContentType());
                contentType = "text/html";
            }

            if (input.getAdminEmailData().getSynchronousity() == KKConstants.EMAIL_ASYNCHRONOUS)
            {
                isAsync = true;
            } else if (input.getAdminEmailData()
                    .getSynchronousity() == KKConstants.EMAIL_SYNCHRONOUS)
            {
                isAsync = false;
            }
        } else if (input.getAdminOptions() != null)
        {
            /*
             * Mail sent from Admin App
             */
            if (input.getAdminOptions().getFromAddress() != null)
            {
                setFromAddressStr(input.getAdminOptions().getFromAddress());
            } else if (input.getAdminOptions().getReplyToAddress() != null)
            {
                setReplyToAddressStr(input.getAdminOptions().getReplyToAddress());
            } else if (input.getAdminOptions().getBccEmails() != null
                    && input.getAdminOptions().getBccEmails().length() > 0)
            {
                bccEmailsStrArray = input.getAdminOptions().getBccEmails().split(";");
            }

            if (input.getAdminOptions().getContentType() == KKConstants.EMAIL_AS_HTML)
            {
                contentType = "text/html";
            } else if (input.getAdminOptions().getContentType() == KKConstants.EMAIL_AS_PLAIN_TEXT)
            {
                contentType = "text/plain";
            } else
            {
                log.warn("Unrecognised Email ContentType: "
                        + input.getAdminOptions().getContentType());
                contentType = "text/html";
            }

            if (input.getAdminOptions().getSynchronousity() == KKConstants.EMAIL_ASYNCHRONOUS)
            {
                isAsync = true;
            } else if (input.getAdminOptions().getSynchronousity() == KKConstants.EMAIL_SYNCHRONOUS)
            {
                isAsync = false;
            }

            fullAttachmentFilename = input.getAdminOptions().getFullAttachmentFilename();
            friendlyAttachmentName = input.getAdminOptions().getFriendlyAttachmentName();
            deleteAttachmentAfterSend = input.getAdminOptions().isDeleteAttachmentAfterSend();
        }

        /*
         * Get specific data depending on type of mail being sent
         */
        if (input.getType() == ExternalMailInput.MAIL_FROM_APP)
        {

            if (input.getKonakartAPI().equals(sendTemplateEmailToCustomer1))
            {
                message = (String) context.get("message");

            } else if (input.getKonakartAPI().equals(sendOrderConfirmationEmail1))
            {
                appOrder = (Order) context.get("order");
                vendorOrders = (Order[]) context.get("vendorOrders");
            } else if (input.getKonakartAPI().equals(sendStockReorderEmail))
            {
                productName = (String) context.get("productName");
                productQuantity = ((Integer) context.get("productQuantity")).intValue();
                productId = ((Integer) context.get("productId")).intValue();
                sku = (String) context.get("sku");
            } else if (input.getKonakartAPI().equals(sendWelcomeEmail1))
            {

            } else if (input.getKonakartAPI().equals(sendNewPassword1))
            {
                doBlindCopy = false;
                newPassword = (String) context.get("newPassword");
                expiryMins = ((Integer) context.get("expiryMins")).intValue();
            } else
            {
                throw new KKException("Unknwown KonaKart API - " + input.getKonakartAPI());
            }

        } else if (input.getType() == ExternalMailInput.MAIL_FROM_ADMIN)
        {
            // Special case that doesn't use a Velocity Context
            if (input.getKonakartAPI().equals(sendEmail))
            {

            } else if (input.getKonakartAPI().equals(resetCustomerPassword))
            {
                doBlindCopy = false;
                newPassword = (String) context.get("newPassword");
                expiryMins = ((Integer) context.get("expiryMins")).intValue();
            } else if (input.getKonakartAPI().equals(sendStatusChangeEmailForState))
            {
                adminOrder = (AdminOrder) context.get("order");
            } else if (input.getKonakartAPI().equals(sendTemplateEmail))
            {
            } else
            {
                throw new KKException("Unknwown KonaKart Admin API - " + input.getKonakartAPI());
            }
        }
    }

    /**
     * Create a formatted string of debug information
     * 
     * @return Returns debug information
     */
    protected String getMailDebugInfo()
    {
        StringBuffer str = new StringBuffer();
        str.append("Email Details:").append("\n");
        str.append("konakartAPI            = ").append(konakartAPI).append("\n");
        str.append("storeOwner             = ").append(storeOwner).append("\n");
        str.append("storeOwnerEmailAddr    = ").append(storeOwnerEmailAddr).append("\n");
        str.append("storeName              = ").append(storeName).append("\n");
        str.append("storeId                = ").append(storeId).append("\n");
        str.append("imageBaseUrl           = ").append(imageBaseUrl).append("\n");
        str.append("templateBase           = ").append(templateBase).append("\n");
        str.append("displayPricesWithTax   = ").append(displayPricesWithTax).append("\n");
        str.append("emailSubject           = ").append(emailSubject).append("\n");
        str.append("destinationEmailAddr   = ").append(emailAddr).append("\n");
        str.append("fromAddress            = ").append(fromAddressStr).append("\n");
        str.append("fromAddressName        = ").append(fromName).append("\n");
        str.append("replyToAddress         = ").append(replyToAddressStr).append("\n");
        str.append("replyToAddressName     = ").append(replyToName).append("\n");
        if (bccEmailsStrArray != null && bccEmailsStrArray.length > 0)
        {
            for (int i = 0; i < bccEmailsStrArray.length; i++)
            {
                String bccEmail = bccEmailsStrArray[i];
                str.append("bccEmail               = ").append(bccEmail).append("\n");
            }
        }
        str.append("contentType            = ").append(contentType).append("\n");
        str.append("doBlindCopy            = ").append(doBlindCopy).append("\n");
        str.append("fullAttachmentFilename = ").append(fullAttachmentFilename).append("\n");
        str.append("isAsync                = ").append(isAsync).append("\n");
        str.append("countryCode            = ").append(countryCode).append("\n");
        str.append("templateName           = ").append(templateName).append("\n");
        str.append("customerName           = ").append(customerName).append("\n");
        if (newPassword != null)
        {
            str.append("newPassword            = ").append("YES").append("\n");
            str.append("expiryMins             = ").append(expiryMins).append("\n");
        }
        if (productName != null)
        {
            str.append("productName            = ").append(productName).append("\n");
            str.append("productQuantity        = ").append(productQuantity).append("\n");
            str.append("productId              = ").append(productId).append("\n");
            str.append("sku                    = ").append(sku).append("\n");
        }

        str.append("emailBody              = ").append(emailBody).append("\n");
        if (appCustomer != null)
        {
            str.append("customer        = ").append(appCustomer.toString()).append("\n");
        } else if (adminCustomer != null)
        {
            str.append("customer        = ").append(adminCustomer.toString()).append("\n");
        } else
        {
            str.append("customer is null").append("\n");
        }
        if (appOrder != null)
        {
            str.append("order           = ").append(appOrder.toString()).append("\n");
        } else if (adminOrder != null)
        {
            str.append("order           = ").append(adminOrder.toString()).append("\n");
        } else
        {
            str.append("order is null").append("\n");
        }
        if (vendorOrders != null && vendorOrders.length > 0)
        {
            for (int i = 0; i < vendorOrders.length; i++)
            {
                Order vOrder = vendorOrders[i];
                str.append("vendor order    = ").append(vOrder.toString()).append("\n");
            }
        }
        return str.toString();
    }

    /**
     * Called to determine whether the module is available.Works regardless of whether module is
     * being called by Admin or App eng.
     * 
     * @param configKey
     *            the configKey
     * @return Returns true or false
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    protected boolean isAvailable(String configKey) throws KKException
    {
        boolean available = getConfigurationValueAsBool(configKey, false);

        if (log.isDebugEnabled())
        {
            log.debug(configKey + " Available? " + available);
        }

        return available;
    }

    /**
     * @return Returns the AdminConfigurationMgr.
     * @throws Exception
     *             an unexpected exception
     */
    protected AdminConfigurationMgrIf getAdminConfigMgr() throws Exception
    {
        return adminMgrFactory.getAdminConfigMgr(/* createNew */false);
    }

    /**
     * Common validation code for the input object
     * 
     * @param input
     *            the input
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void validateExternalMailInput(ExternalMailInput input) throws KKException
    {
        if (input == null)
        {
            throw new KKException("Input object cannot be set to null");
        }
        if (input.getKonakartAPI() == null)
        {
            throw new KKException("KonaKart API definition cannot be set to null");
        }
        if (input.getKonakartAPI().equals(sendEmail))
        {
            if (input.getAdminEmailData() == null)
            {
                throw new KKException("AdminEmailData cannot be set to null");
            }
            return;
        }
        if (input.getContext() == null)
        {
            throw new KKException("Context cannot be set to null");
        }
        if (input.getEmailTemplate() == null)
        {
            throw new KKException("Email Template cannot be set to null");
        }
        if (input.getType() == ExternalMailInput.MAIL_FROM_APP)
        {
            if (input.getAppEng() == null)
            {
                throw new KKException("appEng cannot be set to null");
            }
            if (input.getAppOptions() == null)
            {
                throw new KKException("AppOptions cannot be set to null");
            }
        } else if (input.getType() == ExternalMailInput.MAIL_FROM_ADMIN)
        {
            if (input.getAdminEng() == null)
            {
                throw new KKException("adminEng cannot be set to null");
            }
            if (input.getKonakartAPI().equals(sendEmail) && input.getAdminEmailData() == null)
            {
                throw new KKException("adminEmailData cannot be set to null");
            }
        } else
        {
            throw new KKException("Unknown mail type - " + input.getType());
        }
    }

    /**
     * Returns the customer name formatted by the template in the configuration variable
     * 
     * @param cust
     *            the cust
     * @return Returns the formatted customer name
     * @throws Exception
     *             an unexpected exception
     */
    public String getFormattedCustomerName(AdminCustomer cust) throws Exception
    {
        if (cust == null)
        {
            return null;
        }
        String template = getConfigurationValue(ConfigConstants.NAME_FORMAT_TEMPLATE);
        String name = com.konakart.util.Utils.formatName(template, cust.getFirstName(),
                cust.getLastName());
        return name;
    }

    /**
     * Works regardless of whether module is being called by Admin or App eng.
     * 
     * @param key
     *            the key
     * @return Returns the String value of the configuration variable
     * @throws KKException
     *             an unexpected KKException exception
     */
    protected String getConfigurationValue(String key) throws KKException
    {
        try
        {
            if (getEng() != null)
            {
                return getConfigMgr().getConfigurationValue(false, key);
            }
            return getAdminConfigMgr().getConfigurationValue(key);
        } catch (KKException kke)
        {
            throw kke;
        } catch (Exception e)
        {
            throw new KKException(e);
        }
    }

    /**
     * Works regardless of whether module is being called by Admin or App eng.
     * 
     * @param key
     *            the key
     * @param def
     *            the default
     * @return Returns the boolean value of the configuration variable
     * @throws KKException
     *             an unexpected KKException exception
     */
    protected boolean getConfigurationValueAsBool(String key, boolean def) throws KKException
    {
        try
        {
            if (getEng() != null)
            {
                return getConfigMgr().getConfigurationValueAsBool(false, key, def);
            }
            return getAdminConfigMgr().getConfigurationValueAsBool(key, def);
        } catch (KKException kke)
        {
            throw kke;
        } catch (Exception e)
        {
            throw new KKException(e);
        }
    }

    /**
     * Works regardless of whether module is being called by Admin or App eng.
     * 
     * @param key
     *            the key
     * @param def
     *            the def
     * @return Returns the int value of the configuration variable
     * @throws KKException
     *             an unexpected KKException exception
     */
    protected int getConfigurationValueAsIntWithDefault(String key, int def) throws KKException
    {
        try
        {
            if (getEng() != null)
            {
                return getConfigMgr().getConfigurationValueAsInt(false, key, def);
            }
            return getAdminConfigMgr().getConfigurationIntValueOrDefault(key, def);
        } catch (KKException kke)
        {
            throw kke;
        } catch (Exception e)
        {
            throw new KKException(e);
        }
    }

    /**
     * If it's in the format "name &lt;abc@defg.com&gt;" we set the name as well
     * 
     * @param fromAddressStr
     *            the fromAddressStr to set
     */
    public void setFromAddressStr(String fromAddressStr)
    {
        this.fromAddressStr = fromAddressStr;
        if (fromAddressStr != null)
        {
            int start = fromAddressStr.indexOf("<");
            if (start > 0)
            {
                int end = fromAddressStr.indexOf(">");
                String name = fromAddressStr.substring(0, start);
                String mail = fromAddressStr.substring(start + 1, end);
                this.fromAddressStr = mail;
                this.fromName = name;
            } else
            {
                this.fromName = null;
            }
        }
    }

    /**
     * @param replyToAddressStr
     *            the replyToAddressStr to set
     */
    public void setReplyToAddressStr(String replyToAddressStr)
    {
        this.replyToAddressStr = replyToAddressStr;
        if (replyToAddressStr != null)
        {
            int start = replyToAddressStr.indexOf("<");
            if (start > 0)
            {
                int end = replyToAddressStr.indexOf(">");
                String name = replyToAddressStr.substring(0, start);
                String mail = replyToAddressStr.substring(start + 1, end);
                this.replyToAddressStr = mail;
                this.replyToName = name;
            } else
            {
                this.replyToName = null;
            }
        }
    }

    /**
     * Deletes the attachment file
     */
    protected void deleteAttachmentFile()
    {
        try
        {
            if (deleteAttachmentAfterSend && fullAttachmentFilename != null)
            {
                File attFile = new File(fullAttachmentFilename);
                if (log.isDebugEnabled())
                {
                    log.debug("Delete attachment file: " + attFile.getCanonicalPath());
                }
                attFile.delete();
            }
        } catch (Exception e)
        {
            log.warn("Unable to delete " + fullAttachmentFilename + " : " + e.getMessage());
        }
    }

    /**
     * Get the encoded file content required to send the mail
     * 
     * @return Returns the encoded file content
     * @throws IOException
     *             an unexpected IOException exception
     */
    protected String getEncodedFileContent() throws IOException
    {
        if (fullAttachmentFilename == null)
        {
            return null;
        }
        Path path = Paths.get(fullAttachmentFilename);
        byte[] byteArray = Files.readAllBytes(path);
        Base64 x = new Base64();
        String encodedFileContent = x.encodeAsString(byteArray);
        return encodedFileContent;
    }

    /**
     * Get some common content types
     * 
     * @return Returns some common attachment content types
     */
    protected String getContentType()
    {
        if (fullAttachmentFilename == null)
        {
            return null;
        }
        String ext = FilenameUtils.getExtension(fullAttachmentFilename);
        if (ext == null)
        {
            return null;
        }
        if (ext.equalsIgnoreCase("pdf"))
        {
            return "application/pdf";
        } else if (ext.equalsIgnoreCase("zip"))
        {
            return "application/zip";
        } else if (ext.equalsIgnoreCase("jpg"))
        {
            return "image/jpeg";

        } else if (ext.equalsIgnoreCase("jpeg"))
        {
            return "image/jpeg";

        } else if (ext.equalsIgnoreCase("html"))
        {
            return "text/html";

        } else if (ext.equalsIgnoreCase("png"))
        {
            return "image/png";

        } else if (ext.equalsIgnoreCase("gif"))
        {
            return "image/gif";

        }
        log.warn("Content type not found for file extension: " + ext);
        return null;
    }

}
