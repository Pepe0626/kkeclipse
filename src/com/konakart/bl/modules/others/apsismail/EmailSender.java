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
package com.konakart.bl.modules.others.apsismail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to send an email in a separate thread
 */
public class EmailSender implements Runnable
{
    /** the log */
    protected static Log log = LogFactory.getLog(EmailSender.class);

    private ApsisMail apsisMail;

    /**
     * Constructor
     * @param _apsisMail 
     */
    public EmailSender(ApsisMail _apsisMail)
    {
        this.apsisMail = _apsisMail;
    }

    public void run()
    {
        try
        {
            apsisMail.sendMail();
        } catch (Exception e)
        {
//            if (apsisMail.getSendGridMail() != null)
//            {
//                try
//                {
//                    log.warn("Problem Sending email:\n" + apsisMail.getApsisMail().buildPretty(),
//                            e);
//                } catch (IOException e1)
//                {
//                    log.warn("Problem Sending email", e);
//                }
//            } else
            {
                log.warn("Problem Sending email", e);
            }
        }
    }
}
