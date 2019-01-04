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

package com.konakart.actions.ipn;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.konakart.actions.gateways.BaseGatewayAction;
import com.konakart.al.KKAppEng;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.appif.SSOTokenIf;

/**
 * This class is an Action class for what to do when a payment notification callback is received
 * from PayPal.
 */
public class PayPalAction extends BaseGatewayAction
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(PayPalAction.class);

    private static final long serialVersionUID = 1L;

    private static final String ENCODING = "windows-1252";

    // PayPal constants
    private static final String custom = "custom";

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        if (log.isDebugEnabled())
        {
            log.debug("*********** PayPal Callback");
        }

        if (request == null)
        {
            return null;
        }

        try
        {

            /*
             * Create a StringBuffer of data passed to the thread that will be used to validate the
             * IPN message received by PayPal
             */
            Map<String, String[]> ipnInMap = request.getParameterMap();
            StringBuffer verifyPayload = new StringBuffer("cmd=_notify-validate");
            if (ipnInMap != null)
            {
                String[] encodingParam = ipnInMap.get("charset");
                String encoding = encodingParam != null && encodingParam.length > 0
                        ? encodingParam[0] : ENCODING;
                for (Map.Entry<String, String[]> entry : ipnInMap.entrySet())
                {
                    String name = entry.getKey();
                    String[] value = entry.getValue();
                    verifyPayload.append("&").append(name).append("=")
                            .append(URLEncoder.encode(value[0], encoding));
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("Verify Payload = " + verifyPayload);
            }

            /*
             * Get the UUID from the request so that we can look up the SSO Token
             */
            String uuid = request.getParameter(PayPalAction.custom);
            if (uuid == null)
            {
                throw new KKException(
                        "The callback from PayPal did not contain the 'custom' parameter.");
            }

            // Get an instance of the KonaKart engine
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            /*
             * Don't delete the token since may be required by other callbacks
             */
            SSOTokenIf token = kkAppEng.getEng().getSSOToken(uuid, /* deleteToken */false);
            if (token == null)
            {
                throw new KKException("The SSOToken from the PayPal callback is null");
            }

            /*
             * Create an ArrayList of name value pairs to pass to the thread
             */
            ArrayList<NameValue> parmList = new ArrayList<NameValue>();
            Enumeration<?> en = request.getParameterNames();
            while (en.hasMoreElements())
            {
                String paramName = (String) en.nextElement();
                String paramValue = request.getParameter(paramName);
                NameValue nv = new NameValue(paramName, paramValue);
                parmList.add(nv);
            }

            /*
             * Call a thread to verify the IPN and to save the data from PayPal
             */
            ManagePayPal mpp = new ManagePayPal(kkAppEng, token, verifyPayload, parmList);
            new Thread(mpp).start();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
