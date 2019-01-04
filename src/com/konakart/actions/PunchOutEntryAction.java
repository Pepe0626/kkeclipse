//
// (c) 2012 DS Data Systems UK Ltd, All rights reserved.
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

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.PunchOut;

/**
 * Gets called by the ERP system to start a KonaKart session for the corporate buyer. URL that can
 * be used for testing:
 * <pre>
 * {@code
 * http://localhost:8780/konakart/PunchoutEntry.action
 *                ?username=doe@konakart.com
 *                &password=password
 *                &HOOK_URL=http://www.hashemian.com/tools/form-post-tester.php
 *                &OCI_VERSION=4.0
 *                &OPI_VERSION=1.0
 *                &returntarget=_parent
 * }
 * </pre>
 */
public class PunchOutEntryAction extends BaseAction
{

    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            /*
             * When debugging get all parameters
             */
            if (log.isDebugEnabled())
            {
                StringBuffer sb = new StringBuffer();
                sb.append("\n");
                Enumeration<?> en = request.getParameterNames();
                while (en.hasMoreElements())
                {
                    String paramName = (String) en.nextElement();
                    String paramValue = request.getParameter(paramName);
                    if (sb.length() > 0)
                    {
                        sb.append("\n");
                    }
                    sb.append(paramName);
                    sb.append(" = ");
                    sb.append(paramValue);
                }
                sb.append("\n");
                log.debug("Punch Out data received from ERP:");
                log.debug(sb.toString());
            }

            // Get an engine
            KKAppEng kkAppEng = this.getKKAppEng(request, response);
            kkAppEng.setPunchoutDetails(null);

            /*
             * Check input parameters
             */
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String hookUrl = request.getParameter("HOOK_URL");

            if (username == null || username.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("PunchOutEntryAction called with no username parameter");
                }
                return SUCCESS;
            }

            if (password == null || password.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("PunchOutEntryAction called with no password parameter");
                }
                return SUCCESS;
            }

            if (hookUrl == null || hookUrl.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("PunchOutEntryAction called with no HOOK_URL parameter");
                }
                return SUCCESS;
            }

            // Check the credentials
            String sessionId = kkAppEng.getEng().login(username, password);
            if (sessionId == null || sessionId.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("PunchOutEntryAction called with invalid login credentials");
                }
                return SUCCESS;
            }
            kkAppEng.getEng().logout(sessionId);

            // Create a punch out object and attach it to the app eng
            PunchOut po = new PunchOut();
            po.setUsername(username);
            po.setPassword(password);
            po.setReturnURL(hookUrl);
            po.setOciVersion(request.getParameter("OCI_VERSION"));
            po.setReturnTarget(request.getParameter("returntarget"));
            kkAppEng.setPunchoutDetails(po);

            return SUCCESS;
        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }
}
