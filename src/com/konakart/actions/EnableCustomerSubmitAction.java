//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is free software; you can redistribute 
// it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This software is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.app.EmailOptions;
import com.konakart.app.KKException;
import com.konakart.appif.EmailOptionsIf;

/**
 * Gets called normally from an eMail where a customer clicks on a link to confirm his registration.
 */
public class EnableCustomerSubmitAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String emailAddr;

    private String message;

    private String closePanelAction;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            /*
             * Check input parameters
             */
            String secretKey = request.getParameter("key");
            if (secretKey == null || secretKey.length() == 0)
            {
                log.debug("EnableCustomerSubmitAction called with no key parameter");
                return WELCOME;
            }

            if (log.isDebugEnabled())
            {
                log.debug("EnableCustomerSubmitAction called with key = " + secretKey + ", m = "
                        + request.getParameter("m"));
            }

            /* Attempt to enable the customer */
            try
            {
                kkAppEng.getEng().enableCustomer(secretKey);
            } catch (KKException e)
            {
                log.debug("enableCustomer() not successful. Exception message :" + e.getMessage());
                return WELCOME;
            }

            // Send a welcome email
            try
            {
                String custIdStr = request.getParameter("i");
                int custId = Integer.parseInt(custIdStr);
                EmailOptionsIf options = new EmailOptions();
                options.setCountryCode(kkAppEng.getLocale().substring(0, 2));
                options.setTemplateName(com.konakart.bl.EmailMgr.WELCOME_TEMPLATE);
                kkAppEng.getEng().sendWelcomeEmail1(custId, options);
            } catch (Exception e)
            {
                log.warn("Unable to send Welcome email to customer", e);
            }

            /* We pass email to login page so that it's already filled in */
            emailAddr = request.getParameter("m");

            message = kkAppEng.getMsg("double.opt.in.ret.registration.message", getEmailAddr());
            closePanelAction = "LogIn.action";

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the emailAddr
     */
    public String getEmailAddr()
    {
        return emailAddr;
    }

    /**
     * @param emailAddr
     *            the emailAddr to set
     */
    public void setEmailAddr(String emailAddr)
    {
        this.emailAddr = emailAddr;
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * @return the closePanelAction
     */
    public String getClosePanelAction()
    {
        return closePanelAction;
    }

    /**
     * @param closePanelAction
     *            the closePanelAction to set
     */
    public void setClosePanelAction(String closePanelAction)
    {
        this.closePanelAction = closePanelAction;
    }
}
