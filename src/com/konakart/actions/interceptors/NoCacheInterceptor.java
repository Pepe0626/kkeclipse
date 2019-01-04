//
// (c) 2014 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.actions.interceptors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * To stop pages being cached by the browser
 */
public class NoCacheInterceptor implements Interceptor
{
    protected Log log = LogFactory.getLog(LoggingInterceptor.class);

    private static final long serialVersionUID = 1L;

    @Override
    public void destroy()
    {
    }

    @Override
    public void init()
    {
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception
    {
        try
        {
            HttpServletRequest request = ServletActionContext.getRequest();
            // Do check to stop this happening in a portlet
            if (request != null && request.getMethod() != null)
            {
                ServletActionContext.getResponse().setHeader("Cache-Control", "no-cache, no-store");
                ServletActionContext.getResponse().setHeader("Pragma", "no-cache");
                ServletActionContext.getResponse().setHeader("Expires",
                        "Wed, 11 Jan 1984 05:00:00 GMT");
                if (log.isDebugEnabled())
                {
                    log.debug("NoCaheInterceptor set the Headers in the Response");
                }
            }

        } catch (Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("NoCacheInterceptor exception", e);
            }
        }
        return invocation.invoke();
    }
}
