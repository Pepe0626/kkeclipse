package com.konakart.actions.interceptors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * Used to force a Post and not allow a Get
 * 
 */
public class ForceAPostInterceptor implements Interceptor
{

    private static final long serialVersionUID = 1L;

    protected Log log = LogFactory.getLog(ForceAPostInterceptor.class);

    public String intercept(ActionInvocation invocation) throws Exception
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (request != null && request.getMethod() != null
                && !request.getMethod().equalsIgnoreCase("POST"))
        {
            return "Welcome";
        }
        String result = invocation.invoke();
        return result;
    }

    public void destroy()
    {
        log.debug("Destroying ForceAPostInterceptor...");
    }

    public void init()
    {
        log.debug("Initializing ForceAPostInterceptor...");
    }
}
