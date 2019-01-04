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
package com.konakart.bl.modules.others.sendgridmail;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.ExternalMailInput;
import com.konakart.app.ExternalMailResult;
import com.konakart.app.KKException;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.others.BaseMailModule;
import com.konakart.bl.modules.others.ExternalMailInterface;
import com.konakartadmin.app.KKAdminException;
import com.konakartadmin.appif.KKAdminIf;
import com.sendgrid.Attachments;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Personalization;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

/**
 * Used to send mails using SendGrid. The mails may be sent either from the App Engine or the Admin
 * Engine.
 */
public class SendGridMail extends BaseMailModule implements ExternalMailInterface
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(SendGridMail.class);

    // API key required by GridMail to send a mail
    private String apiKey = null;

    // SendGrid mail object used to send the mail by SendGrid
    private Mail sendGridMail;

    /** Send Grid Constant */
    protected final static String MODULE_OTHER_SENDGRID_MAIL_API_KEY = "MODULE_OTHER_SENDGRID_MAIL_API_KEY";

    /**
     * Constructor when called from App Eng
     * 
     * @param appEng
     * @throws KKException
     */
    public SendGridMail(KKEngIf appEng) throws KKException
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
     * @throws KKAdminException an unexpected exception in the KonaKart Admin engine
     */
    public SendGridMail(KKAdminIf adminEng) throws KKAdminException
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
            throw new KKAdminException(e);
        }
    }

    /**
     * Set configuration variables. Here we just set the SendGrid specific variables. Others are set
     * in the super implementation.
     * 
     * @throws KKException
     */
    protected void setConfigs() throws KKException
    {
        super.setConfigs();
        apiKey = getConfigurationValue(MODULE_OTHER_SENDGRID_MAIL_API_KEY);
        if (apiKey == null || apiKey.length() == 0)
        {
            throw new KKException("The SendGrid API key has not been set.");
        }
    }

    /**
     * Called to send a mail
     */
    public ExternalMailResult sendExternalMail(ExternalMailInput input) throws Exception
    {
        // Call super to validate the input and set local configuration variables
        super.sendExternalMail(input);

        // Create a SendGrid mail object
        sendGridMail = createSendGridMail(input);

        // Send the mail using SendGrid
        if (isAsync)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Sending SendGrid Mail Asynchronously");
            }
            EmailSender es = new EmailSender(this);
            new Thread(es).start();
        } else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Sending SendGrid Mail Synchronously");
            }
            return sendMail();
        }

        return null;
    }

    /**
     * Creates and returns a SendGrid mail object
     * 
     * @param input
     * @return Returns a SendGrid mail object
     * @throws IOException
     */
    private Mail createSendGridMail(ExternalMailInput input) throws IOException
    {
        Mail sendGridMail = new Mail();
        Email from = new Email(fromAddressStr);
        if (fromName != null)
        {
            from.setName(fromName);
        }
        sendGridMail.setFrom(from);
        sendGridMail.setSubject(emailSubject);
        Content content = new Content(contentType, emailBody);
        sendGridMail.addContent(content);

        if (fullAttachmentFilename != null)
        {
            Attachments attachments = new Attachments();
            attachments.setType(getContentType());
            attachments.setFilename(friendlyAttachmentName);
            attachments.setContent(getEncodedFileContent());
            sendGridMail.addAttachments(attachments);
        }

        if (replyToAddressStr != null && replyToAddressStr.length() > 0)
        {
            Email replyTo = new Email(replyToAddressStr);
            if (replyToName != null)
            {
                replyTo.setName(replyToName);
            }
            sendGridMail.setReplyTo(replyTo);
        }

        /*
         * Define the to and bcc addresses in a personalization
         */
        Personalization personalization = new Personalization();
        Email to = new Email(emailAddr);
        if (customerName != null)
        {
            to.setName(customerName);
        }
        personalization.addTo(to);

        if (bccEmailsStrArray != null && bccEmailsStrArray.length > 0 && doBlindCopy)
        {
            for (int i = 0; i < bccEmailsStrArray.length; i++)
            {
                String bcc = bccEmailsStrArray[i];
                personalization.addBcc(new Email(bcc));
            }
        }

        /*
         * Personalizations are where you would add substitutions
         */
        // personalization.addSubstitution("%name%", "Example User");
        // personalization.addSubstitution("%city%", "Riverside");

        // Add the personalization to the mail
        sendGridMail.addPersonalization(personalization);

        if (log.isDebugEnabled())
        {
            log.debug("SendGrid Mail:\n" + sendGridMail.buildPretty());
        }

        /*
         * Note that personalizations can also be used for adding substitutions
         */
        return sendGridMail;
    }

    /**
     * Sends a mail using SendGrid
     * 
     * @return Returns an ExternalMailResult object
     * 
     * @throws Exception
     */
    public ExternalMailResult sendMail() throws Exception
    {
        if (sendGridMail == null)
        {
            throw new KKException(
                    "The SendGrid Mail object must not be null when sendMail() is called");
        }
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(sendGridMail.build());
        Response response = sg.api(request);

        if (log.isDebugEnabled())
        {
            StringBuffer str = new StringBuffer();
            str.append("SendGrid Email Response:").append("\n");
            str.append("Status Code   = ").append(response.getStatusCode()).append("\n");
            str.append("Headers       = ").append("\n").append(response.getHeaders()).append("\n");
            str.append("Body          = ").append("\n").append(response.getBody()).append("\n");
            log.debug(str.toString());
        }

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
        return isAvailable("MODULE_OTHER_SENDGRID_MAIL_STATUS");
    }

    /**
     * @return the sendGridMail
     */
    public Mail getSendGridMail()
    {
        return sendGridMail;
    }
}
