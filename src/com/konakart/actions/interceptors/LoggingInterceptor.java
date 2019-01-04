package com.konakart.actions.interceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * Used to figure out how long each struts action is taking
 * 
 */
public class LoggingInterceptor implements Interceptor
{

    private static final long serialVersionUID = 1L;

    protected Log log = LogFactory.getLog(LoggingInterceptor.class);

    public String intercept(ActionInvocation invocation) throws Exception
    {
        String className = null;
        long startTime = 0;
        if (log.isDebugEnabled())
        {
            className = invocation.getAction().getClass().getName();
            startTime = System.currentTimeMillis();
            log.debug("Before calling action: " + className);
        }
        String result = invocation.invoke();
        if (log.isDebugEnabled())
        {
            long endTime = System.currentTimeMillis();
            log.debug("After calling action: " + className + " Time taken: "
                    + (endTime - startTime) + " ms");
        }
        return result;
    }

    public void destroy()
    {
        log.debug("Destroying LoggingInterceptor...");
    }

    public void init()
    {
        log.debug("Initializing LoggingInterceptor...");
    }
}
