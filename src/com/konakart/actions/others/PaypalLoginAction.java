//
// (c) 2017 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.actions.others;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.konakart.actions.BaseAction;
import com.konakart.al.CustomerMgr;

/**
 * Gets called by PayPal after the customer has logged in using the PayPal popup.
 */
public class PaypalLoginAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private String loginToken;

    private String loginType;

    private String code;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        try
        {
            /*
             * We set loginToken and loginType and return. PaypalReturnBody.jsp takes these values
             * and posts them to LoginSubmitAction
             */
            loginToken = code;
            loginType = CustomerMgr.PAYPAL;
            return SUCCESS;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the loginToken
     */
    public String getLoginToken()
    {
        return loginToken;
    }

    /**
     * @param loginToken
     *            the loginToken to set
     */
    public void setLoginToken(String loginToken)
    {
        this.loginToken = loginToken;
    }

    /**
     * @return the loginType
     */
    public String getLoginType()
    {
        return loginType;
    }

    /**
     * @param loginType
     *            the loginType to set
     */
    public void setLoginType(String loginType)
    {
        this.loginType = loginType;
    }

    /**
     * @return the code
     */
    public String getCode()
    {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(String code)
    {
        this.code = code;
    }

}
