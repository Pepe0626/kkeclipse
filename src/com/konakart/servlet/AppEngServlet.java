//
// (c) 2006-2015 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.servlet;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.al.KKAppEng;
import com.konakart.app.EngineConfig;
import com.konakart.app.KKException;
import com.konakart.appif.EngineConfigIf;
import com.konakart.bl.KKEngInitialization;
import com.konakart.bl.KKServletContextListener;
import com.konakart.bl.LoggingMgr;
import com.konakart.blif.KKServletContextListenerIf;
import com.konakart.util.KKConstants;

/**
 * Instance of KonaKartApp engine started by application server on startup.
 */
public class AppEngServlet extends HttpServlet
{
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 1L;

    protected static Log log = LogFactory.getLog(AppEngServlet.class);

    private String propertiesPath = null;

    private String appPropertiesPath = null;

    private static int mode = 0;

    private boolean customersShared = false;

    private boolean productsShared = false;

    private boolean categoriesShared = false;

    private boolean portlet = false;

    /** Default StoreId as defined in the konakart.properties file **/
    private static String defaultStoreId = null;

    /**
     * @throws RemoteException
     *            an unexpected RemoteException exception
     * @throws MalformedURLException
     *            an unexpected MalformedURLException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public AppEngServlet() throws RemoteException, MalformedURLException, KKException
    {
        // beware... you cannot access the ServletConfig in this constructor - see init() below
    }

    /**
     * Initialise the servlet.
     * 
     * @param config
     *            the config
     * @throws ServletException
     *            an unexpected ServletException exception
     */
    public void init(ServletConfig config) throws ServletException
    {
        // very important to call super.init() here
        super.init(config);

        /*
         * Start Logging
         */
        new LoggingMgr().init();

        if (config != null)
        {
            KKEngInitialization initializer = new KKEngInitialization();
            mode = initializer.getEngineMode();
            defaultStoreId = initializer.getDefaultStoreId();
            customersShared = initializer.isCustomersShared();
            productsShared = initializer.isProductsShared();
            categoriesShared = initializer.isCategoriesShared();

            propertiesPath = config.getInitParameter("propertiesPath");
            if (propertiesPath == null || propertiesPath.length() == 0)
            {
                propertiesPath = initializer.getKKEngPropertiesFile();
                log.error("The propertiesPath parameter set in web.xml file for "
                        + "KonaKartAppEngineServlet must be set. A value of " + propertiesPath
                        + " will be used to instantiate the engine");
            }

            appPropertiesPath = config.getInitParameter("appPropertiesPath");
            if (appPropertiesPath == null || appPropertiesPath.length() == 0)
            {
                appPropertiesPath = KKConstants.KONAKART_APP_PROPERTIES_FILE;
                log.error("The appPropertiesPath parameter set in web.xml file for "
                        + "KonaKartAppEngineServlet must be set. A value of " + appPropertiesPath
                        + " will be used to instantiate the engine");
            }

            try
            {
                portlet = Boolean.valueOf(config.getInitParameter("portlet"));
            } catch (Exception e)
            {
            }

            if (log.isInfoEnabled())
            {
                String msg = "\n\nKKAppServlet Configuration:";
                msg += "\n\t defaultStoreId           = " + defaultStoreId;
                msg += "\n\t EngineMode               = " + mode;
                msg += "\n\t Customers Shared         = " + customersShared;
                msg += "\n\t Products Shared          = " + productsShared;
                msg += "\n\t Categories Shared        = " + categoriesShared;
                msg += "\n\t portlet                  = " + portlet;
                msg += "\n\t KKAppEngPropertiesFile   = " + appPropertiesPath;
                msg += "\n\t KKEngPropertiesFile      = " + propertiesPath;
                msg += "\n";

                log.info(msg);
            }

            /*
             * if (productsShared && !customersShared) { log.error("Illegal Mode Specified.  " +
             * "If you specify shared products you must also specify shared customers");
             * log.error("Setting customersShared Mode to true - may cause problems");
             * customersShared = true; }
             */

            if (categoriesShared && !productsShared)
            {
                log.error("Illegal Mode Specified.  "
                        + "If you specify shared categories you must also specify shared products");
                log.error("Setting productsShared Mode to true - may cause problems");
                productsShared = true;
            }

            // Instantiate an EngineConfig object
            EngineConfigIf engConf = new EngineConfig();
            engConf.setPropertiesFileName(propertiesPath);
            engConf.setAppPropertiesFileName(appPropertiesPath);
            engConf.setStoreId(defaultStoreId);
            engConf.setCustomersShared(customersShared);
            engConf.setProductsShared(productsShared);
            engConf.setCategoriesShared(categoriesShared);
            engConf.setMode(mode);
            engConf.setAppEngPortlet(portlet);

            try
            {
                KKAppEng eng = new KKAppEng(engConf);
                if (log.isDebugEnabled())
                {
                    log.debug("KKAppEng " + eng.getKkVersion() + " started with this config:\n"
                            + KKAppEng.getEngConf().toString());
                }
            } catch (Exception e)
            {
                log.error(
                        "Exception attempting to start the KonaKart Client Engine from a servlet",
                        e);
            }

        } else
        {
            log.error("Cannot start the KonaKartAppEngineServlet since there are no configuration parameters.");
        }

        if (log.isInfoEnabled())
        {
            String status = "AppEngServlet initialised : mode " + mode;

            if (customersShared)
            {
                status += " CUS-S";
            }

            if (productsShared)
            {
                status += " PRO-S";
            }

            if (categoriesShared)
            {
                status += " CAT-S";
            }

            log.info(status + " for store '" + defaultStoreId + "'");
        }

        // Register this thread with KKThreadManager
        try
        {
            KKServletContextListenerIf kkContextListener = KKServletContextListener.get();
            String threadName = this.getServletName() + " - " + getClass().getName();
            if (kkContextListener != null)
            {
                kkContextListener.registerThread(Thread.currentThread(), this.getServletName()
                        + " - " + getClass().getName());
                if (log.isInfoEnabled())
                {
                    log.info("Registered " + threadName + " for shutdown");
                }
            } else
            {
                if (log.isWarnEnabled())
                {
                    log.warn("Thread " + threadName
                            + " not registered for shutdown - no kkContextListener found");
                }
            }
        } catch (Throwable e)
        {
            if (log.isWarnEnabled())
            {
                log.warn("Thread not registered for shutdown - " + e.getMessage());
            }
        }
    }

    /**
     * @return true if MultiStore
     */
    public static boolean isMultiStore()
    {
        return (mode == KKConstants.MODE_MULTI_STORE_SHARED_DB || mode == KKConstants.MODE_MULTI_STORE_NON_SHARED_DB);
    }

    /**
     * @return the defaultStoreId
     */
    public static String getDefaultStoreId()
    {
        return defaultStoreId;
    }

    /**
     * @param defaultStoreId
     *            the defaultStoreId to set
     */
    public static void setDefaultStoreId(String defaultStoreId)
    {
        AppEngServlet.defaultStoreId = defaultStoreId;
    }
}
