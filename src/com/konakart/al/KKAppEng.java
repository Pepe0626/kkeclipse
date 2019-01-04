//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.al;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.EngineConfig;
import com.konakart.app.KKBeanCopier;
import com.konakart.app.KKException;
import com.konakart.app.ProductSearch;
import com.konakart.appif.CountryIf;
import com.konakart.appif.CurrencyIf;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.EngineConfigIf;
import com.konakart.appif.FetchProductOptionsIf;
import com.konakart.appif.KKConfigurationIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.LanguageIf;
import com.konakart.appif.NameValueIf;
import com.konakart.appif.OptionIf;
import com.konakart.appif.ProductIf;
import com.konakart.appif.StoreIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.KKServletContextListener;
import com.konakart.blif.KKServletContextListenerIf;
import com.konakart.liferay.SingleSignOnIf;
import com.konakart.servlet.AppEngServlet;
import com.konakart.util.KKCodeDescription;
import com.konakart.util.KKConstants;
import com.konakart.util.PropertiesUtils;
import com.konakart.util.PropertyFileFinder;
import com.konakart.util.PropertyFileNames;
import com.konakartadmin.app.AdminEngineConfig;
import com.konakartadmin.appif.KKAdminIf;
import com.konakartadmin.bl.KKAdminInitialization;

/**
 * This is the entry point for all method calls to the KonaKart client engine. The idea behind the
 * client engine is that each application user instantiates a copy and continues to use the same
 * copy which keeps state for the user. The Struts Actions call methods to retrieve data from the
 * server engine or perform transactions, whereas the JSPs normally call methods to display data
 * which has been cached by the client engine.<br>
 * <br>
 * This object can be used to get an instance of one of the manager objects such as the
 * CustomerMgr() and the CategoryMgr(). It can also be used to get an instance of the server engine
 * using the getEng() method. Examples of how to use these calls can be obtained from the Struts
 * Action Classes which are shipped in source code format.
 */
public class KKAppEng
{
    /** log */
    protected Log log = LogFactory.getLog(KKAppEng.class);

    /*
     * Static final data
     */

    /** Key used to store the KonaKart object within the session. */
    public static final String KONAKART_KEY = "konakartKey";

    /** CDATA control characters */
    private static final String cDataStart = "<![CDATA[";

    /** CDATA control characters */
    private static final String cDataEnd = "]]>";

    /** Image size */
    public static final int IMAGE_BIG = 1;

    /** Image size */
    public static final int IMAGE_MEDIUM = 2;

    /** Image size */
    public static final int IMAGE_SMALL = 3;

    /** Image size */
    public static final int IMAGE_TINY = 4;

    /*
     * Static data
     */

    /**
     * Configuration parameter passed when the engine is instantiated. We keep a static version
     * since all information except for storeId is common for all instances of the engine.
     */
    private static EngineConfigIf engConf = null;

    /** mutex */
    private static String mutex = "konakartMutex";

    /** Used to read the properties from the properties file */
    private static Configuration konakartAppConfig = null;

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = new HashMap<String, StaticData>();

    /** Used to make sure we don't set it off twice */
    private static boolean setOffUpdateThread = false;

    /**
     * Thread which loops forever to update the config variables when needed. We keep track of them
     * so that we can eventually shut them down gracefully
     */
    private static List<ConfigCacheUpdater> updateThreadList = Collections
            .synchronizedList(new ArrayList<ConfigCacheUpdater>());

    /*
     * Non Static data
     */

    /** local mutex */
    public Object checkoutConfirmationSubmitMutex = new Object();

    /** local mutex */
    public Object checkoutMutex = new Object();

    /** The HTTP Session */
    HttpSession session = null;

    /**
     * Used to keep track of where we are. Header.jsp uses it to display our current position within
     * the application. i.e. Top >> Catalog >> Checkout
     */
    private CurrentNavigation nav = new CurrentNavigation();

    /** Constant for searching all products */
    private int SEARCH_ALL = ProductSearch.SEARCH_ALL;

    /** Manages the categories */
    private CategoryMgr categoryMgr = null;

    /** Manages the content */
    private ContentMgr contentMgr = null;

    /** Manages the products */
    private ProductMgr productMgr = null;

    /** Manages the reviews */
    private ReviewMgr reviewMgr = null;

    /** Manages the orders */
    private OrderMgr orderMgr = null;

    /** Manages the basket */
    private BasketMgr basketMgr = null;

    /** Manages the customers */
    private CustomerMgr customerMgr = null;

    /** Manages the customer's wish lists */
    private WishListMgr wishListMgr = null;

    /** Manages the customer tags */
    private CustomerTagMgr customerTagMgr = null;

    /** Manages the reward points */
    private RewardPointMgr rewardPointMgr = null;

    /** Manages the number of products a customer can buy */
    private QuotaMgr quotaMgr = null;

    /** User Currency */
    private CurrencyIf userCurrency = null;

    /** User Currency Formatter */
    private DecimalFormat userCurrencyFormatter = null;

    /**
     * Contains an instance of the class used to gain access to server side engine for the
     * application
     */
    private KKEngIf eng = null;

    /**
     * Instance of the Admin Eng
     */
    private KKAdminIf adminEng = null;

    /**
     * Hash map to store engines in multi-vendor mode
     */
    private HashMap<String, EngineData> engMap;

    /** Keep a copy of the current language for the KonaKart engine */
    private int langId = -1;

    /** Session Id returned after logging in */
    private String sessionId;

    /** Used to store information related to checking the validity of the KonaKart session */
    private SessionCheckData sessionCheckData;

    /**
     * If set, after logging in we go to this page, because the user attempted to do something for
     * which he had to log in, so we need to return to where he was
     */
    private String forwardAfterLogin = null;

    /** Locale */
    private String locale = null;

    private Locale localeObj = null;

    private Collator myCollator = null;

    /** XMLOverHTTP response */
    private String XMLOverHTTPResp = "";

    /** Title of page */
    private String pageTitle = "";

    /** Meta Description */
    private String metaDescription = "";

    /** Meta Keywords */
    private String metaKeywords = "";

    /** List of meta tags */
    private ArrayList<String> metaList = new ArrayList<String>();

    /** Custom variables */
    private String custom1 = null;

    /** Custom variables */
    private String custom2 = null;

    /** Custom variables */
    private String custom3 = null;

    /** Custom variables */
    private String custom4 = null;

    /** Custom variables */
    private String custom5 = null;

    /** Custom variables used to hold data temporarily for passing to included JSPs */
    private String customTemp1 = null;

    /** Custom object */
    private Object customObj = null;

    /** Custom Map */
    private Map<String, String> customMap = null;

    /** Hash map used to store objects on KKAppEng */
    private HashMap<String, Object> objMap = new HashMap<String, Object>();

    /** The StoreInfo for this instance of the engine */
    private StoreInfo storeInfo = null;

    /** The object containing all of the static data for this instance of the client engine */
    private StaticData sd = null;

    /** Set to true if KonaKart Cookie functionality is installed */
    private boolean kkCookieEnabled = false;

    /**
     * An options object that could for example point this customer to a special catalog. This is
     * the default object set when the engine is created in KKAppEngCallouts
     */
    private FetchProductOptionsIf fetchProdOptionsDefault = null;

    /** An options object that could for example point this customer to a special catalog */
    private FetchProductOptionsIf fetchProdOptions = null;

    /**
     * An options object that could for example point this customer to a special catalog. getImages
     * set to false.
     */
    private FetchProductOptionsIf fetchProdOptionsNoImages = null;

    /** The administrator who has logged for the customer */
    private CustomerIf adminUser = null;

    /** Set to true when running as a portlet */
    private boolean portlet = false;

    /** The portlet context path */
    private String portletContextPath = null;

    /** KonaKart version - the version of the engine that this KKAppEng is pointing to */
    private String kkVersion = null;

    /** The Id of an affiliate partner */
    private String affiliateId = null;

    /** Information required for performing punch out. Normally null. */
    private PunchOut punchoutDetails;

    /** Default StoreId as defined in the web.xml for the Servlet **/
    // public static String defaultStoreIdFromWebXml = null;

    /** Used for sizing the page tiles */
    private String contentClass = "wide";

    /** Base URL set by JSP. e.g. http://localhost:8780/konakart/ **/
    private String base;

    /** Set to true when the customer agrees to accept cookies */
    private boolean agreedCookies = false;

    /** Token used to prevent XSRF attacks */
    private String xsrfToken;

    /** Content images directory name */
    private String contentImagesDir = null;

    /**
     * Valid values are null, CustomerMgr.FACEBOOK, CustomerMgr.PAYPAL and CustomerMgr.GOOGLEPLUS
     */
    private String loginType;

    /** Class for Single Sign On when using Liferay (Enterprise Only) */
    private SingleSignOnIf liferaySSO = null;

    /** Number of times we've attempted to instantiate the liferaySSO class */
    private int liferaySSOInstantiateAttempts = 0;

    /** The parent category id used for filtering searches */
    private int searchParentCategoryId = -1;

    /**
     * Constructor called by a servlet that creates an AppEng instance on startup.
     * 
     * @param engConf
     *            An EngineConfigIf object containing information to configure the engine at
     *            startup.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws InvocationTargetException
     *            an unexpected InvocationTargetException exception
     * @throws IllegalAccessException
     *            an unexpected IllegalAccessException exception
     * @throws InstantiationException
     *            an unexpected InstantiationException exception
     * @throws ClassNotFoundException
     *            an unexpected ClassNotFoundException exception
     * @throws IllegalArgumentException
     *            an unexpected IllegalArgumentException exception
     */
    public KKAppEng(EngineConfigIf engConf)
            throws KKException, KKAppException, IllegalArgumentException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException
    {
        initFromServlet(engConf);
    }

    /**
     * Method that starts an update thread for a store that gets created dynamically through the
     * admin app
     * 
     * @param info
     * @throws KKAppException
     * @throws KKException
     */
    private void startUpdateThread(StoreInfo info) throws KKAppException, KKException
    {
        synchronized (mutex)
        {
            boolean found = false;
            if (info != null && info.getStoreId() != null && updateThreadList != null)
            {
                for (Iterator<ConfigCacheUpdater> iterator = updateThreadList.iterator(); iterator
                        .hasNext();)
                {
                    ConfigCacheUpdater ccu = iterator.next();
                    if (ccu.getStoreInfo() != null && ccu.getStoreInfo().getStoreId() != null)
                    {
                        if (ccu.getStoreInfo().getStoreId().equals(info.getStoreId()))
                        {
                            if (log.isInfoEnabled())
                            {
                                log.info("Found update thread for store " + info.getStoreId()
                                        + " in thread list.");
                            }
                            found = true;
                            break;
                        }
                    }
                }
                if (!found)
                {
                    log.info("Start Cache Update Thread for " + info.getStoreId());
                    ConfigCacheUpdater cacheUpdateThread = new ConfigCacheUpdater(this);
                    cacheUpdateThread.setStoreInfo(new StoreInfo(info.getStoreId()));
                    cacheUpdateThread.start();
                    registerUpdaterThread(cacheUpdateThread, info.getStoreId());
                }
            }
        }
    }

    /**
     * Removes the update thread from the static list for the storeId passed in as a parameter
     * 
     * @param storeId
     *            the storeId
     */
    protected void removeFromUpdateThreadList(String storeId)
    {
        synchronized (mutex)
        {
            if (updateThreadList != null && storeId != null)
            {
                for (Iterator<ConfigCacheUpdater> iterator = updateThreadList.iterator(); iterator
                        .hasNext();)
                {
                    ConfigCacheUpdater ccu = iterator.next();
                    if (ccu.getStoreInfo() != null && ccu.getStoreInfo().getStoreId() != null
                            && ccu.getStoreInfo().getStoreId().equals(storeId))
                    {
                        if (log.isInfoEnabled())
                        {
                            log.info("Removed store : " + storeId + " from update thread list.");
                        }
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Method called by constructors.
     * 
     * @param _engConf
     * @throws KKException
     * @throws KKAppException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void initFromServlet(EngineConfigIf _engConf)
            throws KKException, KKAppException, IllegalArgumentException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException
    {
        synchronized (mutex)
        {
            // Call the startup hook
            new KKAppEngCallouts().beforeStartup(this);

            KKAppEng.engConf = _engConf;

            if (engConf == null)
            {
                throw new KKException(
                        "The EngineConfiguration object for the App Engine is null and so the engine cannot be instantiated");
            }

            // Look up any property file definitions that might override those provided in the
            // EngineConfig (which in some cases are those defined in the the web.xml)

            PropertyFileNames pfn = new PropertyFileNames();
            engConf.setAppPropertiesFileName(
                    pfn.getFileName(PropertyFileNames.KONAKART_APPENG_CLIENT_PROPERTIES_FILE,
                            engConf.getAppPropertiesFileName()));
            engConf.setPropertiesFileName(
                    pfn.getFileName(PropertyFileNames.KONAKART_APPENG_SERVER_PROPERTIES_FILE,
                            engConf.getPropertiesFileName()));

            if (log.isInfoEnabled())
            {
                log.info("Initialising KKAppEng : "
                        + KKCodeDescription.engineModeToString(engConf.getMode()));
            }

            /*
             * Read the properties file
             */
            if (konakartAppConfig == null)
            {
                if (log.isInfoEnabled())
                {
                    log.info("Initialising KKAppEng from " + engConf.getAppPropertiesFileName());
                }
                readPropertiesFile(engConf.getAppPropertiesFileName());
            }

            /*
             * Instantiate a server engine and figure out how many stores it supports. We start an
             * update thread per store that checks for changes in configuration variables and
             * updates them if changes have been made
             */
            if (!setOffUpdateThread)
            {
                if (eng == null)
                {
                    StoreInfo _storeInfo = new StoreInfo();
                    _storeInfo.setStoreId(engConf.getStoreId());

                    eng = getAServerEngineInstance(_storeInfo);
                }

                // Set the engine id on the static engConf
                EngineConfigIf lConf = eng.getEngConf();
                if (lConf != null)
                {
                    KKAppEng.engConf.setEngineId(lConf.getEngineId());
                }

                String[] storeIds = eng.getStoreIds();

                if (storeIds == null || storeIds.length == 0)
                {
                    log.info("Start 1 Cache Update Thread for " + engConf.getStoreId());
                    ConfigCacheUpdater cacheUpdateThread = new ConfigCacheUpdater(this);
                    cacheUpdateThread.setStoreInfo(new StoreInfo(engConf.getStoreId()));
                    cacheUpdateThread.start();
                    registerUpdaterThread(cacheUpdateThread, engConf.getStoreId());
                } else
                {
                    log.info("Start " + storeIds.length + " Cache Update Threads");
                    for (int i = 0; i < storeIds.length; i++)
                    {
                        log.info("Start Cache Update Thread for " + storeIds[i]);
                        ConfigCacheUpdater cacheUpdateThread = new ConfigCacheUpdater(this);
                        cacheUpdateThread.setStoreInfo(new StoreInfo(storeIds[i]));
                        cacheUpdateThread.start();
                        registerUpdaterThread(cacheUpdateThread, storeIds[i]);
                    }
                }
                setOffUpdateThread = true;
            }

            // Call the startup hook
            new KKAppEngCallouts().afterStartup(this);
        }
    }

    private void registerUpdaterThread(ConfigCacheUpdater cacheUpdateThread, String storeId)
    {
        synchronized (mutex)
        {
            updateThreadList.add(cacheUpdateThread);
        }
        // Register this thread with KKThreadManager
        try
        {
            KKServletContextListenerIf kkContextListener = KKServletContextListener.get();
            String threadName = "Storefront-Cache-Updater - " + storeId;
            if (kkContextListener != null)
            {
                kkContextListener.registerThread(cacheUpdateThread, threadName);
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
     * Read the properties file and instantiate konakartAppConfig
     * 
     * @param propertiesPath
     * @throws KKAppException
     */
    private void readPropertiesFile(String propertiesPath) throws KKAppException
    {
        try
        {
            /*
             * Find the properties file which is guaranteed to return the URL of the properties file
             * or throw an exception. It can't send out an error message since Log4j hasn't been
             * initialised yet. In this case the properties file should be called
             * konakart_app.properties.
             */
            URL configFileURL = PropertyFileFinder.findPropertiesURL(propertiesPath);
            log.info("kkAppEng using configuration file: " + configFileURL);
            Configuration conf = PropertiesUtils.getConfiguration(configFileURL);
            
            if (conf.isEmpty())
            {
                throw new KKAppException("The configuration file: " + propertiesPath
                        + " appears to contain no keys");
            }

            // Look for properties that are in the "konakart.app" namespace.
            Configuration subConf = conf.subset("konakart.app");
            if (subConf == null || subConf.isEmpty())
            {
                log.error("The konakart.app section in the properties file is missing. "
                        + "You must add at least one property to resolve this problem. "
                        + "e.g. konakart.app.engineclass=com.konakart.app.KKEng");
                return;
            }
            konakartAppConfig = subConf;

        } catch (Exception e)
        {
            throw new KKAppException(e);
        }
    }

    /**
     * Constructor for engine created when a user creates a new session. This is called in
     * com.konakart.actions.BaseAction .
     * 
     * @param session
     *            HttpSession
     * 
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public KKAppEng(HttpSession session) throws KKAppException
    {
        this.session = session;
        StoreInfo info = new StoreInfo();
        init(info);
    }

    /**
     * Constructor for engine created when a user creates a new session. This is called in
     * com.konakart.actions.BaseAction . The StoreId can be specified for multi store usage.
     * 
     * @param storeInfo
     *            An object containing information about the store being accessed
     * @param session
     *            HttpSession
     * 
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public KKAppEng(StoreInfo storeInfo, HttpSession session) throws KKAppException
    {
        if (storeInfo == null)
        {
            throw new KKAppException("The StoreInfo can not be null");
        }
        this.session = session;
        init(storeInfo);
    }

    /**
     * Common private method for initialising
     * 
     * @param _storeInfo
     * @throws KKAppException
     */
    private void init(StoreInfo _storeInfo) throws KKAppException
    {
        // Call the startup hook
        new KKAppEngCallouts().beforeStartup(this);

        if (_storeInfo == null)
        {
            throw new KKAppException(
                    "The StoreInfo object cannot be set to null when instantiating KKAppEng");
        }
        this.storeInfo = _storeInfo;

        // Instantiate the engine
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Entering KKAppEng Constructor()");
            }

            if (konakartAppConfig == null)
            {
                /*
                 * If Struts is being used, this class should already have been instantiated as a
                 * servlet and so the properties file should already have been read. If we aren't
                 * using Struts (i.e. Running the GWT code through Eclipse) then the servlet won't
                 * have been instantiated so we try and read the properties file before throwing an
                 * exception.
                 */
                readPropertiesFile("konakart_app.properties");
                if (konakartAppConfig == null)
                {
                    throw new KKAppException("No properties have been found for the application. "
                            + "Please ensure that the properties file konakart_app.properties exists");
                }
            }

            // Find the storeId to use

            if (log.isInfoEnabled())
            {
                log.info(
                        "AppEngServlet.getDefaultStoreId() = " + AppEngServlet.getDefaultStoreId());
                log.info("Default StoreId                   = "
                        + getStoreId(AppEngServlet.getDefaultStoreId()));
                log.info("StoreInfo StoreId                 = " + storeInfo.getStoreId());
            }

            String defaultStore = getStoreId(AppEngServlet.getDefaultStoreId());

            // Get the Static Data object for this store and store it globally
            sd = staticDataHM.get(getStoreId(defaultStore));
            if (sd == null)
            {
                sd = new StaticData();
                sd.setStoreId(getStoreId(defaultStore));
                staticDataHM.put(getStoreId(defaultStore), sd);
            }

            // if storeinfo has a null storeId set it to the default storeId
            if (storeInfo.getStoreId() == null)
            {
                storeInfo.setStoreId(getStoreId(defaultStore));
            }

            // Get an engine instance
            eng = getAServerEngineInstance(storeInfo);

            if (log.isDebugEnabled())
            {
                log.debug("Got Eng Instance for " + storeInfo.getStoreId());
            }

            // Setup default language.
            LanguageIf defaultLang = eng.getDefaultLanguage();
            if (defaultLang != null && defaultLang.getLocale() != null
                    && defaultLang.getLocale().length() > 0)
            {
                this.setLocale(defaultLang.getLocale());
            } else if (defaultLang != null && defaultLang.getCode() != null)
            {
                this.setLocale(defaultLang.getCode(), defaultLang.getCode().toUpperCase());
            }

            if (log.isDebugEnabled())
            {
                if (defaultLang != null && defaultLang.getLocale() != null)
                {
                    log.debug("Default locale for " + storeInfo.getStoreId() + " = "
                            + defaultLang.getLocale());
                } else if (defaultLang != null && defaultLang.getCode() != null)
                {
                    log.debug("Default language for " + storeInfo.getStoreId() + " = "
                            + defaultLang.getCode());
                }
            }

            // Set flag to define whether the storefront is running as a portlet
            if (engConf != null)
            {
                this.setPortlet(engConf.isAppEngPortlet());
            }

            // Instantiate the category manager
            categoryMgr = new CategoryMgr(eng, this);
            // Instantiate the content manager
            contentMgr = new ContentMgr(eng, this);
            // Instantiate the product manager
            productMgr = new ProductMgr(eng, this);
            // Instantiate the review manager
            reviewMgr = new ReviewMgr(eng, this);
            // Instantiate the order manager
            orderMgr = new OrderMgr(eng, this);
            // Instantiate the basket manager
            basketMgr = new BasketMgr(eng, this);
            // Instantiate the customer manager
            customerMgr = new CustomerMgr(eng, this);
            // Instantiate the wish list manager
            wishListMgr = new WishListMgr(eng, this);
            // Instantiate the customer tag manager
            customerTagMgr = new CustomerTagMgr(eng, this);
            // Instantiate the reward point manager
            rewardPointMgr = new RewardPointMgr(eng, this);
            // Instantiate the quota manager
            quotaMgr = new QuotaMgr(eng, this);

            log.debug("Instantiated managers for " + storeInfo.getStoreId());

            // Setup default currency
            setupCurrency();

            // Setup the engine version
            setupVersion();

            // Refresh the caches if they have never been refreshed
            firstTimeRefresh();

            // Create a guest customer
            customerMgr.createGuest();

            // Ensure that we have an update thread running
            if (eng.getEngConf() != null)
            {
                int mode = eng.getEngConf().getMode();
                if (mode == EngineConfig.MODE_MULTI_STORE_NON_SHARED_DB
                        || mode == EngineConfig.MODE_MULTI_STORE_SHARED_DB)
                {
                    startUpdateThread(storeInfo);
                }
            }

            // Check to see whether KKCookie functionality is installed
            checkKKCookieInstalled();

            // Call the startup hook
            new KKAppEngCallouts().afterStartup(this);

            log.debug("Leaving KKAppEng constructor");

        } catch (Exception e)
        {
            throw new KKAppException(e);
        }

    }

    /**
     * Get an instance of the server engine which will either be a full engine or a SOAP stub
     * 
     * @param _storeInfo
     *            a StreInfo object that defines how to create an engine
     * 
     * @return Returns an instance of the server engine
     * @throws ClassNotFoundException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     * @throws KKAppException
     */
    private KKEngIf getAServerEngineInstance(StoreInfo _storeInfo)
            throws ClassNotFoundException, IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException, KKAppException
    {
        if (_storeInfo == null)
        {
            throw new KKAppException("The parameter storeInfo cannot be set to null");
        }

        String engineClassName = konakartAppConfig.getString("engineclass");

        Class<?> engineClass = Class.forName(engineClassName);

        KKEngIf lEng = null;
        Constructor<?>[] constructors = engineClass.getConstructors();
        Constructor<?> engConstructor = null;
        if (constructors != null && constructors.length > 0)
        {
            for (int i = 0; i < constructors.length; i++)
            {
                Constructor<?> constructor = constructors[i];
                Class<?>[] parmTypes = constructor.getParameterTypes();
                if (parmTypes != null && parmTypes.length == 1)
                {
                    String parmName = parmTypes[0].getName();
                    if (parmName != null && parmName.equals("com.konakart.appif.EngineConfigIf"))
                    {
                        engConstructor = constructor;
                    }
                }
            }
        }

        if (engConstructor != null)
        {
            // Create a copy of the static engine conf.
            EngineConfigIf conf = getEngConfCopy();
            if (_storeInfo.getStoreId() != null)
            {
                conf.setStoreId(_storeInfo.getStoreId());
            }

            lEng = (KKEngIf) engConstructor.newInstance(conf);
            if (log.isDebugEnabled())
            {
                log.debug("Called EngineConfigIf constructor for instantiating KKEng for store "
                        + conf.getStoreId() + " in "
                        + KKCodeDescription.engineModeToString(conf.getMode()) + " mode");
            }
        } else
        {
            lEng = (KKEngIf) engineClass.newInstance();
            if (log.isDebugEnabled())
            {
                log.debug("Called empty constructor for instantiating KKEng");
            }
        }

        log.info("Engine used by application is " + engineClassName + " for store id "
                + _storeInfo.getStoreId());

        return lEng;
    }

    /**
     * Get an instance of the Admin engine
     * 
     * @return Returns an instance of the server engine
     * @throws ClassNotFoundException
     *            an unexpected ClassNotFoundException exception
     * @throws InvocationTargetException
     *            an unexpected InvocationTargetException exception
     * @throws IllegalAccessException
     *            an unexpected IllegalAccessException exception
     * @throws InstantiationException
     *            an unexpected InstantiationException exception
     * @throws IllegalArgumentException
     *            an unexpected IllegalArgumentException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public KKAdminIf getAnAdminEngineInstance()
            throws ClassNotFoundException, IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException, KKAppException
    {
        if (storeInfo == null)
        {
            throw new KKAppException("StoreInfo cannot be set to null");
        }

        KKAdminInitialization initializer = new KKAdminInitialization();
        AdminEngineConfig adminEngConfig = new AdminEngineConfig();
        adminEngConfig.setStoreId(storeInfo.getStoreId());
        adminEngConfig.setCategoriesShared(initializer.isCategoriesShared());
        adminEngConfig.setCustomersShared(initializer.isCustomersShared());
        adminEngConfig.setMode(initializer.getEngineMode());
        adminEngConfig.setProductsShared(initializer.isProductsShared());

        String kkAdminEngineName = konakartAppConfig.getString("adminengineclass");
        Class<?> engineClass = Class.forName(kkAdminEngineName);
        KKAdminIf kkAdmin = null;
        Constructor<?>[] constructors = engineClass.getConstructors();
        Constructor<?> engConstructor = null;
        if (constructors != null && constructors.length > 0)
        {
            for (int i = 0; i < constructors.length; i++)
            {
                Constructor<?> constructor = constructors[i];
                Class<?>[] parmTypes = constructor.getParameterTypes();
                if (parmTypes != null && parmTypes.length == 1)
                {
                    String parmName = parmTypes[0].getName();
                    if (parmName != null
                            && parmName.equals("com.konakartadmin.app.AdminEngineConfig"))
                    {
                        engConstructor = constructor;
                    }
                }
            }
        }

        if (engConstructor != null)
        {
            if (log.isInfoEnabled())
            {
                log.info("Create: " + kkAdminEngineName
                        + " using AdminEngineConfig constructor for store '"
                        + storeInfo.getStoreId() + "'");
                if (log.isDebugEnabled())
                {
                    log.debug(adminEngConfig.toString());
                }
            }
            kkAdmin = (KKAdminIf) engConstructor.newInstance(adminEngConfig);
            if (log.isInfoEnabled())
            {
                log.info("Created " + kkAdminEngineName
                        + " using AdminEngineConfig constructor for store '"
                        + storeInfo.getStoreId() + "'");
            }
        } else
        {
            if (log.isInfoEnabled())
            {
                log.info("Create: " + kkAdminEngineName + " using default constructor");
            }
            kkAdmin = (KKAdminIf) engineClass.newInstance();
            if (log.isInfoEnabled())
            {
                log.info("Created " + kkAdminEngineName + " using default constructor");
            }
        }

        if (log.isInfoEnabled())
        {
            String propsFileName = KKConstants.KONAKARTADMIN_PROPERTIES_FILE;
            if (adminEngConfig.getPropertiesFileName() != null)
            {
                propsFileName = adminEngConfig.getPropertiesFileName();
            }

            log.info("Created Admine Engine: " + kkAdminEngineName + " using properties in '"
                    + propsFileName + "'");
        }

        return kkAdmin;
    }

    /**
     * Creates a copy of the static EngineConfig object
     * 
     * @return Returns a copy of the static EngineConfig object
     */
    private EngineConfigIf getEngConfCopy()
    {
        EngineConfigIf conf = new EngineConfig();
        conf.setPropertiesFileName(engConf.getPropertiesFileName());
        conf.setAppPropertiesFileName(engConf.getAppPropertiesFileName());
        conf.setMode(engConf.getMode());
        conf.setStoreId(engConf.getStoreId());
        conf.setCustomersShared(engConf.isCustomersShared());
        conf.setProductsShared(engConf.isProductsShared());
        conf.setCategoriesShared(engConf.isCategoriesShared());
        return conf;
    }

    /**
     * Refresh the cached data. It is the ConfigCacheUpdater that calls this method when the
     * configuration variables have been changed. At the start of the day to do the first
     * initialization it is also called from firstTimeRefresh();
     * 
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     * 
     */
    public void refreshCachedData() throws KKException, KKAppException
    {
        if (log.isInfoEnabled())
        {
            log.info("Refreshing cached data for store id " + getStoreId());
        }

        /*
         * Read and cache the config variables
         */
        refreshConfigCache();

        // Get the SSL parameters from the configuration values
        setupSSLParms();

        // Get the store front base paths from the configuration values
        setupStoreFrontBasePaths();

        // Get the stock warn level
        setupStockData();

        // See whether wish list functionality is enabled
        setupWishListData();

        // Are we using Solr
        setupSolrData();

        // Get the Digital Download parameters from the configuration values
        setupDigitalDownloads();

        // Get the validation values from the configuration values
        setupValidation();

        // Setup the default currency and the formatter
        setupCurrency();

        // Setup the engine version
        setupVersion();

        // Refresh all manager caches
        refreshAllManagerConfigs();

        // Message catalogs
        refreshMsgResources();

        // Call the callout
        new KKAppEngCallouts().afterRefreshCaches(this);

        sd.setCachesFilled(true);

        if (log.isInfoEnabled())
        {
            log.info("Leaving refreshConfigs() for " + getStoreId());
        }
    }

    /**
     * Refresh the caches only if they haven't been refreshed yet
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    protected void firstTimeRefresh() throws KKException, KKAppException
    {
        if (sd == null || !sd.isCachesFilled())
        {
            synchronized (mutex)
            {
                /*
                 * If we've been waiting and someone else has done the refresh, then get out
                 */
                if (sd == null || !sd.isCachesFilled())
                {
                    log.debug("Refreshing client engine caches for first time");
                    refreshCachedData();
                    log.debug("Finished refreshing client engine caches for first time");
                }
            }
        }
    }

    /**
     * Refresh all manager caches
     * 
     * @throws KKException
     * @throws KKAppException
     */
    private void refreshAllManagerConfigs() throws KKException, KKAppException
    {
        if (log.isInfoEnabled())
        {
            log.info("Refreshing all Mgr configs for store id " + getStoreId());
        }

        // Refresh all managers
        categoryMgr.refreshConfigs();
        contentMgr.refreshConfigs();
        productMgr.refreshConfigs();
        reviewMgr.refreshConfigs();
        orderMgr.refreshConfigs();
        // basketMgr.refreshConfigs();
        // customerMgr.refreshConfigs();
        wishListMgr.refreshConfigs();
        // customerTagMgr.refreshConfigs();
        rewardPointMgr.refreshConfigs();
    }

    /**
     * Refresh the cache of config variables. Only called by refreshAllClientConfigs()
     * 
     * @throws KKException
     */
    private void refreshConfigCache() throws KKException
    {
        synchronized (mutex)
        {
            // Get the config info from the engine
            KKConfigurationIf[] configArray = eng.getConfigurations();

            if (configArray != null && configArray.length > 0)
            {
                for (int i = 0; i < configArray.length; i++)
                {
                    KKConfigurationIf conf = configArray[i];
                    sd.getConfigMap().put(conf.getKey(), conf.getValue());
                }

                /*
                 * Run the following code only if the cached map has more config variables than what
                 * we read from the engine. In this case we need to remove some config variables
                 * from the map.
                 */
                if (configArray.length < sd.getConfigMap().size())
                {
                    // Need to remove one or more configs from the map
                    // Create a temp map with keys of configs in DB
                    HashMap<String, String> tmpMap = new HashMap<String, String>();
                    for (int i = 0; i < configArray.length; i++)
                    {
                        KKConfigurationIf config = configArray[i];
                        tmpMap.put(config.getKey(), "");
                    }

                    // Iterate through the cache and add missing configs to list
                    ArrayList<String> tmpList = new ArrayList<String>();
                    Iterator<String> it = sd.getConfigMap().keySet().iterator();
                    for (Iterator<String> iterator = it; iterator.hasNext();)
                    {
                        String key = iterator.next();
                        if (tmpMap.get(key) == null)
                        {
                            tmpList.add(key);
                        }
                    }

                    // Remove configs in list from cache
                    for (Iterator<String> iterator = tmpList.iterator(); iterator.hasNext();)
                    {
                        String key = iterator.next();
                        sd.getConfigMap().remove(key);
                    }
                }
            }
        }
    }

    /**
     * Puts the Konakart object back into it's original state with no categories or products
     * selected.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void reset() throws KKException
    {
        categoryMgr.reset();
        productMgr.reset();
        reviewMgr.reset();
    }

    /**
     * Splits the locale string at the underscore and calls
     * <code>public void setLocale(String lowerCaseCode, String upperCaseCode)</code>
     * 
     * @param locale
     *            Locale which should be in the format en_GB
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void setLocale(String locale) throws KKException, KKAppException
    {
        if (locale == null)
        {
            log.warn("Cannot set locale to null");
            return;
        }
        String[] localArray = locale.split("_");
        if (localArray.length == 2)
        {
            // Set the engine locale to the saved locale
            setLocale(localArray[0], localArray[1]);
        } else
        {
            log.warn("Locale has an invalid format :" + locale);
        }
    }

    /**
     * Used to change the locale of the application from the default locale. The category tree
     * already exists for all locales and all calls to the engine use the new locale so that product
     * descriptions etc. are in the correct language. For the case of en_GB, the lower case code
     * would be "en" and the upper case code would be "GB".
     * 
     * @param lowerCaseCode
     *            This is the two letter country code in lower case characters (i.e. it, en etc.)
     * @param upperCaseCode
     *            This is the two letter country code in upper case characters (i.e. IT, GB etc.)
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void setLocale(String lowerCaseCode, String upperCaseCode)
            throws KKException, KKAppException
    {
        LanguageIf lang = eng.getLanguagePerCode(lowerCaseCode);
        if (lang == null)
        {
            throw new KKAppException(
                    "Cannot find a language in the database for code = " + lowerCaseCode);
        }

        langId = lang.getId();

        // Set the locale
        locale = lowerCaseCode + "_" + upperCaseCode;
        localeObj = new Locale(lowerCaseCode, upperCaseCode);

        // Set the Collator
        setMyCollator(Collator.getInstance(localeObj));
    }

    /**
     * Get the current locale in String format (lowerCaseCode_upperCaseCode). For example en_GB.
     * 
     * @return Returns the current locale in String format
     */
    public String getLocale()
    {
        return locale;
    }

    /**
     * Get the current locale as a locale object
     * 
     * @return Returns the current locale as a locale object
     */
    public Locale getLocaleObj()
    {
        return localeObj;
    }

    /**
     * Returns the configuration value for the key passed in as a parameter. If the key is not
     * found, then null is returned.
     * 
     * @param key
     *            the key
     * @return Returns Configuration value
     */
    public String getConfig(String key)
    {
        return getConfig(key, true);
    }

    /**
     * Returns the configuration value for the key passed in as a parameter. If the key is not
     * found, then null is returned.
     * 
     * @param key
     *            the key
     * @param tryEngIfNotInCache
     *            Call the Engine if can't find the variable in the cache
     * @return Returns Configuration value
     */
    public String getConfig(String key, boolean tryEngIfNotInCache)
    {
        boolean warnIfMissing = true;
        return getConfig(key, tryEngIfNotInCache, warnIfMissing);
    }

    /**
     * Returns the configuration value for the key passed in as a parameter. If the key is not
     * found, then null is returned.
     * 
     * @param key
     *            the key
     * @param tryEngIfNotInCache
     *            Call the Engine if can't find the variable in the cache
     * @param warnIfMissing
     *            if true a warning message is logged if the configuration key isn't found
     * @return Returns Configuration value
     */
    public String getConfig(String key, boolean tryEngIfNotInCache, boolean warnIfMissing)
    {
        if (key != null)
        {
            if (log.isDebugEnabled())
            {
                if (sd == null)
                {
                    log.debug("KKAppEng.getConfig(" + key + ") failed - sd = null");
                } else if (sd.getConfigMap() == null)
                {
                    log.debug("KKAppEng.getConfig(" + key + ") failed - sd.getConfigMap() = null");
                } else
                {
                    log.debug("KKAppEng.getConfig(" + key + ") = " + sd.getConfigMap().get(key)
                            + " (" + sd.getStoreId() + ")");
                }
            }

            if (tryEngIfNotInCache && !sd.getConfigMap().containsKey(key))
            {
                log.info("KKAppEng Configuration Map does not contain " + key
                        + ". Getting value from engine.");
                String val = null;
                try
                {
                    // Try getting directly from engine since cache may not yet have been filled
                    val = getEng().getConfigurationValue(key);
                } catch (Exception e)
                {
                    if (e.getMessage().contains("return by API"))
                    {
                        if (log.isWarnEnabled())
                        {
                            log.warn(e.getMessage());
                        }
                    } else
                    {
                        log.warn("Problem calling getConfigurationValue() with key " + key, e);
                    }
                }
                if (val == null)
                {
                    if (warnIfMissing)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.warn(
                                    "The database does not contain the configuration variable with key = "
                                            + key + " .");
                        }
                    } else
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug(
                                    "The database does not contain the configuration variable with key = "
                                            + key + " .");
                        }
                    }
                }
                return val;
            }

            return sd.getConfigMap().get(key);
        }
        return null;
    }

    /**
     * Returns the configuration value as a boolean for the key passed in as a parameter. If the key
     * is not found, then the default value is returned.
     * 
     * @param key
     *            the configuration key to look up
     * @param def
     *            the default value which is returned if the key is null or the key isn't found
     * @return Returns Configuration key value as a boolean if found, or the default value is
     *         returned
     */
    public boolean getConfigAsBoolean(String key, boolean def)
    {
        String configValue = getConfig(key);

        if (configValue == null)
        {
            return def;
        }

        if (configValue.equalsIgnoreCase("TRUE") || configValue.equalsIgnoreCase("ON"))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns the configuration value as a boolean for the key passed in as a parameter. If the key
     * is not found, then the default value is returned.
     * 
     * @param key
     *            the configuration key to look up
     * @param def
     *            the default value which is returned if the key is null or the key isn't found
     * @param tryEngIfNotInCache
     *            Call the Engine if can't find the variable in the cache
     * @return Returns Configuration key value as a boolean if found, or the default value is
     *         returned
     */
    public boolean getConfigAsBoolean(String key, boolean def, boolean tryEngIfNotInCache)
    {
        boolean warnIfMissing = false;
        return getConfigAsBoolean(key, def, tryEngIfNotInCache, warnIfMissing);
    }

    /**
     * Returns the configuration value as a boolean for the key passed in as a parameter. If the key
     * is not found, then the default value is returned.
     * 
     * @param key
     *            the configuration key to look up
     * @param def
     *            the default value which is returned if the key is null or the key isn't found
     * @param tryEngIfNotInCache
     *            Call the Engine if can't find the variable in the cache
     * @param warnIfMissing
     *            if true a warning message is logged if the configuration key isn't found
     * @return Returns Configuration key value as a boolean if found, or the default value is
     *         returned
     */
    public boolean getConfigAsBoolean(String key, boolean def, boolean tryEngIfNotInCache,
            boolean warnIfMissing)
    {
        String configValue = getConfig(key, tryEngIfNotInCache, warnIfMissing);

        if (configValue == null)
        {
            return def;
        }

        if (configValue.equalsIgnoreCase("TRUE") || configValue.equalsIgnoreCase("ON"))
        {
            return true;
        }

        return false;
    }

    /**
     * Returns the configuration value as an integer for the key passed in as a parameter. If the
     * key is not found, then the default int parameter is returned.
     * 
     * @param key
     *            the configuration key to look up
     * @param def
     *            the default value
     * @return Returns Configuration key value as an integer or the default value is returned if the
     *         key is not found.
     * @throws KKException
     *             if there is a problem converting the configuration value into an integer
     */
    public int getConfigAsInt(String key, int def) throws KKException
    {
        int ret = getConfigAsInt(key);
        if (ret == KKConstants.NOT_SET)
        {
            return def;
        }
        return ret;
    }

    /**
     * Returns the configuration value as an integer for the key passed in as a parameter. If the
     * key is not found, then KKConstants.NOT_SET is returned.
     * 
     * @param key
     *            the configuration key to look up
     * @return Returns Configuration key value as an integer or KKConstants.NOT_SET is returned if
     *         the key is not found.
     * @throws KKException
     *             if there is a problem converting the configuration value into an integer
     */
    public int getConfigAsInt(String key) throws KKException
    {
        int intValue = KKConstants.NOT_SET;
        String value = getConfig(key);

        if (value == null)
        {
            return intValue;
        }

        try
        {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e)
        {
            // This is a mis-configuration
            String warnMsg = "Could not convert the " + key + " configuration parameter with value "
                    + value + " into an integer";
            if (log.isWarnEnabled())
            {
                log.warn(warnMsg);
            }

            throw new KKException(warnMsg, e);
        }

        return intValue;
    }

    /**
     * Returns the value of a property from the konakart_app.properties file. Within the properties
     * file the "key" value is prepended by "konakart.app" so for example to read the property
     * "konakart.app.engineclass" you need to pass in a key of "engineclass".
     * 
     * @param key
     *            the key
     * @return Returns the value of the property or null if the key is null or the key's value isn't
     *         found
     */
    public String getProperty(String key)
    {
        if (key == null)
        {
            return null;
        }
        if (konakartAppConfig != null)
        {
            return konakartAppConfig.getString(key);
        }
        return null;
    }

    /**
     * Returns the value of a property from the konakart_app.properties file. Within the properties
     * file the "key" value is prepended by "konakart.app" so for example to read the property
     * "konakart.app.main.page.hide.recently.viewed" you need to pass in a key of
     * "main.page.hide.recently.viewed".
     * 
     * @param key
     *            the key
     * @param def
     *            The default value
     * @return Returns the boolean value of the property or the default value of "def" if the
     *         property doesn't exist or isn't "true" or "false"
     * 
     */
    public boolean getPropertyAsBoolean(String key, boolean def)
    {
        String val = getProperty(key);
        if (val == null)
        {
            return def;
        }
        if (val.equalsIgnoreCase("true"))
        {
            return true;
        } else if (val.equalsIgnoreCase("false"))
        {
            return false;
        }
        return def;
    }

    /**
     * Instantiates the default currency object and its formatter. Also instantiate an array of
     * available currencies. Note that this is called by an update thread and so userCurrency and
     * userCurrencyFormatter are null.
     * 
     * @throws KKException
     * @throws KKAppException
     */
    private void setupCurrency() throws KKException, KKAppException
    {
        // Set the default currency
        sd.setDefaultCurrency(eng.getDefaultCurrency());

        if (sd.getDefaultCurrency() == null)
        {
            // If this is a Multi-Store environment, include the storeId in the exception
            if (eng.getEngConf().getMode() == EngineConfig.MODE_MULTI_STORE_SHARED_DB)
            {
                throw new KKAppException("A default currency cannot be found for store '"
                        + eng.getEngConf().getStoreId() + "'");
            }

            throw new KKAppException("A default currency cannot be found");
        }
        sd.setCurrencyFormatter(getFormatter(sd.getDefaultCurrency()));

        // Get available currencies and sort them to put the default currency at the top of the list
        CurrencyIf[] currencies = eng.getAllCurrencies();
        if (currencies == null || currencies.length == 0)
        {
            throw new KKAppException("No currencies can be found in the system");
        }

        CurrencyIf[] sortedCurrencies = new CurrencyIf[currencies.length];
        int index = 1;
        for (int i = 0; i < currencies.length; i++)
        {
            CurrencyIf c = currencies[i];

            // Put the default currency on top
            if (c.getCode().equalsIgnoreCase(sd.getDefaultCurrency().getCode()))
            {
                sortedCurrencies[0] = c;
            } else
            {
                if (index < sortedCurrencies.length)
                {
                    sortedCurrencies[index] = c;
                    index++;
                }
            }
            sd.getCurrencyHM().put(c.getCode(), c);
        }
        sd.setCurrencies(sortedCurrencies);
    }

    /**
     * Instantiates the engine version if it is null
     * 
     * @throws KKException
     * @throws KKAppException
     */
    private void setupVersion() throws KKException, KKAppException
    {
        if (sd.getVersionStr() != null)
        {
            return;
        }

        String verStr = eng.getKonaKartVersion();
        if (verStr == null || verStr.length() == 0)
        {
            log.warn("The KonaKart version cannot be read."
                    + " This may impact the functionality of the KonaKart engine");
            return;
        }

        String[] verArray = verStr.split("\\.");
        StringBuffer sb = new StringBuffer();
        if (verArray.length != 4)
        {
            log.warn("The KonaKart version (" + verStr + ") should be in the form N.N.N.N."
                    + " This may impact the functionality of the KonaKart engine");
        }

        for (int i = 0; i < verArray.length; i++)
        {
            String v = verArray[i];
            if (v != null)
            {
                if (v.length() == 0)
                {
                    sb.append("00");
                } else if (v.length() == 1)
                {
                    sb.append("0");
                    sb.append(v);
                } else
                {
                    sb.append(v);
                }
            }
        }

        try
        {
            int verInt = Integer.parseInt(sb.toString());
            sd.setVersionStr(verStr);
            sd.setVersionInt(verInt);
            if (log.isDebugEnabled())
            {
                log.debug("KonaKart Version = " + sd.getVersionStr());
                log.debug("KonaKart Version Integer = " + sd.getVersionInt());
            }
        } catch (NumberFormatException e)
        {
            log.warn("The KonaKart version (" + verStr + ") should be in the form N.N.N.N."
                    + " This may impact the functionality of the KonaKart engine");
        }
    }

    /**
     * @return the versionStr
     */
    public String getVersionStr()
    {
        return sd.getVersionStr();
    }

    /**
     * @return the versionInt
     */
    public int getVersionInt()
    {
        return sd.getVersionInt();
    }

    /**
     * Sets up the form validation values from the configuration properties
     * 
     */
    private void setupValidation()
    {

        if (getConfig(ConfigConstants.ENTRY_CITY_MIN_LENGTH) != null)
        {
            sd.setCityMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_CITY_MIN_LENGTH)).intValue());
        } else
        {
            sd.setCityMinLength(3);
        }

        if (getConfig(ConfigConstants.ENTRY_COMPANY_MIN_LENGTH) != null)
        {
            sd.setCompanyMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_COMPANY_MIN_LENGTH)).intValue());
        } else
        {
            sd.setCompanyMinLength(2);
        }

        if (getConfig(ConfigConstants.ENTRY_FIRST_NAME_MIN_LENGTH) != null)
        {
            sd.setFirstNameMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_FIRST_NAME_MIN_LENGTH)).intValue());
        } else
        {
            sd.setFirstNameMinLength(2);
        }

        if (getConfig(ConfigConstants.ENTRY_LAST_NAME_MIN_LENGTH) != null)
        {
            sd.setLastNameMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_LAST_NAME_MIN_LENGTH)).intValue());
        } else
        {
            sd.setLastNameMinLength(2);
        }

        if (getConfig(ConfigConstants.ENTRY_PASSWORD_MIN_LENGTH) != null)
        {
            sd.setPasswordMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_PASSWORD_MIN_LENGTH)).intValue());
        } else
        {
            sd.setPasswordMinLength(6);
        }

        if (getConfig(ConfigConstants.ENTRY_POSTCODE_MIN_LENGTH) != null)
        {
            sd.setPostCodeMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_POSTCODE_MIN_LENGTH)).intValue());
        } else
        {
            sd.setPostCodeMinLength(4);
        }

        if (getConfig(ConfigConstants.ENTRY_STATE_MIN_LENGTH) != null)
        {
            sd.setStateMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_STATE_MIN_LENGTH)).intValue());
        } else
        {
            sd.setStateMinLength(2);
        }

        if (getConfig(ConfigConstants.ENTRY_STREET_ADDRESS1_MIN_LENGTH) != null)
        {
            sd.setStreetAddr1MinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_STREET_ADDRESS1_MIN_LENGTH))
                            .intValue());
        } else
        {
            sd.setStreetAddr1MinLength(5);
        }

        if (getConfig(ConfigConstants.ENTRY_STREET_ADDRESS_MIN_LENGTH) != null)
        {
            sd.setStreetAddrMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_STREET_ADDRESS_MIN_LENGTH))
                            .intValue());
        } else
        {
            sd.setStreetAddrMinLength(5);
        }

        if (getConfig(ConfigConstants.ENTRY_TELEPHONE_MIN_LENGTH) != null)
        {
            sd.setTelephoneMinLength(
                    new Integer(getConfig(ConfigConstants.ENTRY_TELEPHONE_MIN_LENGTH)).intValue());
        } else
        {
            sd.setTelephoneMinLength(3);
        }

        if (getConfig(ConfigConstants.MAX_ADDRESS_BOOK_ENTRIES) != null)
        {
            sd.setMaxAddressBookEntries(
                    new Integer(getConfig(ConfigConstants.MAX_ADDRESS_BOOK_ENTRIES)).intValue());
        } else
        {
            sd.setMaxAddressBookEntries(10);
        }

    }

    /**
     * Sets up the SSL parameters from the configuration properties
     */
    private void setupSSLParms()
    {
        log.info("Setting SSL parameters for " + sd.getStoreId());

        if (getConfig(ConfigConstants.SSL_PORT_NUMBER) != null)
        {
            sd.setSslPort(getConfig(ConfigConstants.SSL_PORT_NUMBER));
        } else
        {
            sd.setSslPort("443");
        }

        if (getConfig(ConfigConstants.STANDARD_PORT_NUMBER) != null)
        {
            sd.setStandardPort(getConfig(ConfigConstants.STANDARD_PORT_NUMBER));
        } else
        {
            sd.setStandardPort("80");
        }

        if (getConfig(ConfigConstants.ENABLE_SSL) != null)
        {
            String enableSSLString = getConfig(ConfigConstants.ENABLE_SSL);
            if (enableSSLString.equalsIgnoreCase("true"))
            {
                sd.setEnableSSL(true);
            } else
            {
                sd.setEnableSSL(false);
            }
        } else
        {
            sd.setEnableSSL(false);
        }

        if (getConfig(ConfigConstants.SSL_BASE_URL) != null
                && getConfig(ConfigConstants.SSL_BASE_URL).length() > 0)
        {
            sd.setSslBaseUrl(getConfig(ConfigConstants.SSL_BASE_URL));
        } else
        {
            sd.setSslBaseUrl(null);
        }
    }

    /**
     * Sets up the store front base paths from the configuration properties
     */
    private void setupStoreFrontBasePaths()
    {
        if (getConfig(ConfigConstants.STORE_FRONT_BASE) != null)
        {
            sd.setStoreBase(getConfig(ConfigConstants.STORE_FRONT_BASE));
        } else
        {
            sd.setStoreBase("/konakart");
        }
        if (getConfig(ConfigConstants.STORE_FRONT_IMG_BASE) != null)
        {
            sd.setImageBase(getConfig(ConfigConstants.STORE_FRONT_IMG_BASE));
        } else
        {
            sd.setImageBase("/konakart/images");
        }
        if (getConfig(ConfigConstants.STORE_FRONT_SCRIPT_BASE) != null)
        {
            sd.setScriptBase(getConfig(ConfigConstants.STORE_FRONT_SCRIPT_BASE));
        } else
        {
            sd.setScriptBase("/konakart/script");
        }
        if (getConfig(ConfigConstants.STORE_FRONT_STYLE_BASE) != null)
        {
            sd.setStyleBase(getConfig(ConfigConstants.STORE_FRONT_STYLE_BASE));
        } else
        {
            sd.setStyleBase("/konakart/styles");
        }
    }

    /**
     * Sets up the static variable that defines whether Solr is being used
     */
    private void setupSolrData()
    {
        String useSolrStr = getConfig(ConfigConstants.USE_SOLR_SEARCH);
        boolean useSolr = false;
        if (useSolrStr != null && useSolrStr.equalsIgnoreCase("TRUE"))
        {
            useSolr = true;
        }
        sd.setUseSolr(useSolr);

        if (useSolr)
        {
            boolean getSpellingSuggestions = getConfigAsBoolean(
                    ConfigConstants.SOLR_ENABLE_SPELLING_SUGGESTION, false);
            sd.setGetSpellingSuggestions(getSpellingSuggestions);
        } else
        {
            sd.setGetSpellingSuggestions(false);
        }
    }

    /**
     * Sets up the stock warning level from the configuration properties
     */
    private void setupStockData()
    {
        try
        {
            sd.setStockWarnLevel(getConfigAsInt(ConfigConstants.STOCK_WARN_LEVEL));

        } catch (KKException e)
        {
            log.warn("STOCK_WARN_LEVEL contains an invalid value", e);
        }
    }

    /**
     * Sets up the is wish list enabled boolean from the configuration properties
     */
    private void setupWishListData()
    {
        sd.setWishListEnabled(getConfigAsBoolean(ConfigConstants.ENABLE_WISHLIST, false));
    }

    /**
     * Sets up the digital download parameters from the configuration properties
     */
    private void setupDigitalDownloads()
    {
        if (getConfig(ConfigConstants.DD_BASE_PATH) != null)
        {
            sd.setDdbasePath(getConfig(ConfigConstants.DD_BASE_PATH));
        } else
        {
            sd.setDdbasePath("");
        }

        if (getConfig(ConfigConstants.DD_DOWNLOAD_AS_ATTACHMENT) != null
                && getConfig(ConfigConstants.DD_DOWNLOAD_AS_ATTACHMENT).equalsIgnoreCase("false"))
        {
            sd.setDdAsAttachment(false);
        } else
        {
            sd.setDdAsAttachment(true);
        }
    }

    /**
     * Get a DecimalFormat object for a currency
     * 
     * @param currency
     * @return Returns a DecimalFormat object for the given currency
     * @throws KKException
     */
    private DecimalFormat getFormatter(CurrencyIf currency)
    {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();

        if (currency.getDecimalPoint() != null && currency.getDecimalPoint().length() > 0)
        {
            char decimalSeparator = currency.getDecimalPoint().charAt(0);
            dfs.setDecimalSeparator(decimalSeparator);
        }

        if (currency.getThousandsPoint() != null && currency.getThousandsPoint().length() > 0)
        {
            char groupingSeparator = currency.getThousandsPoint().charAt(0);
            dfs.setGroupingSeparator(groupingSeparator);
        }

        NumberFormat nFormat = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat dFormat = (DecimalFormat) nFormat;
        dFormat.setDecimalFormatSymbols(dfs);
        dFormat.setMaximumFractionDigits(new Integer(currency.getDecimalPlaces()).intValue());
        dFormat.setMinimumFractionDigits(new Integer(currency.getDecimalPlaces()).intValue());
        return dFormat;
    }

    /**
     * Used to create a string in order to visualize a price. It ensures that the decimal places,
     * the thousands separator and the currency symbol are correct. Uses the default currency.
     * 
     * @param number
     *            to be formatted
     * @return The formatted price
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public String formatPrice(BigDecimal number) throws KKAppException
    {
        return formatPrice(number, null);
    }

    /**
     * Used to create a string in order to visualise a price. It ensures that the decimal places,
     * the thousands separator and the currency symbol are correct. An entry must exist in the
     * database for the currency code passed in as a parameter.
     * 
     * @param numberToFormat
     *            number to be formatted
     * @param currencyCode
     *            Three letter currency code (USD, GBP, EUR etc.)
     * @return The formatted price
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public String formatPrice(BigDecimal numberToFormat, String currencyCode) throws KKAppException
    {
        if (numberToFormat == null)
        {
            return "null";
        }

        CurrencyIf currency = null;
        DecimalFormat formatter = null;
        BigDecimal number = numberToFormat;

        /*
         * Note that getUserCurrency() and getUserCurrencyFormatter() return the default currency
         * and formatter if the user ones are set to null.
         */
        if (currencyCode != null && currencyCode.equals(getUserCurrency().getCode()))
        {
            currency = getUserCurrency();
            formatter = getUserCurrencyFormatter();
        } else if (currencyCode == null)
        {
            currency = getUserCurrency();
            formatter = getUserCurrencyFormatter();
        } else
        {
            try
            {
                currency = getCurrency(currencyCode);
            } catch (KKException e)
            {
                throw new KKAppException(
                        "A currency cannot be found for currency code = " + currencyCode, e);
            }
            if (currency == null)
            {
                throw new KKAppException(
                        "A currency cannot be found for currency code = " + currencyCode);
            }
            formatter = getFormatter(currency);
        }

        /*
         * If the currency is passed in the method, then we don't do a conversion since this is used
         * where we know exactly what the currency is. If the currencyCode == null, this normally
         * means to use the default currency. However if the user currency is different to the
         * default currency then we need to make a conversion and use the user currency.
         */
        if (currencyCode == null && userCurrency != null
                && userCurrency.getId() != sd.getDefaultCurrency().getId()
                && userCurrencyFormatter != null)
        {
            BigDecimal conversionRate = userCurrency.getValue();
            number = number.multiply(conversionRate);
        }

        formatter.setRoundingMode(RoundingMode.HALF_UP);
        String ret = formatter.format(number);
        if (currency.getSymbolLeft() != null && currency.getSymbolLeft().length() > 0)
        {
            ret = currency.getSymbolLeft() + ret;
        }
        if (currency.getSymbolRight() != null && currency.getSymbolRight().length() > 0)
        {
            ret = ret + currency.getSymbolRight();
        }
        return ret;
    }

    /**
     * Converts the input using the conversion rate of the user currency.
     * 
     * @param numberToConvert
     *            number to be converted
     * @return The converted price
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public BigDecimal convertPrice(BigDecimal numberToConvert) throws KKAppException
    {
        if (numberToConvert == null)
        {
            return null;
        }

        CurrencyIf userCurr = getUserCurrency();

        // No conversion if using default currency
        if (userCurr.getId() == sd.getDefaultCurrency().getId())
        {
            return numberToConvert;
        }

        BigDecimal conversionRate = userCurr.getValue();
        if (conversionRate == null)
        {
            return null;
        }

        BigDecimal convertedNum = numberToConvert.multiply(conversionRate);

        return convertedNum;
    }

    /**
     * Used to get an instance of the CategoryMgr.
     * 
     * @return Returns the CategoryMgr.
     */
    public CategoryMgr getCategoryMgr()
    {
        return categoryMgr;
    }

    /**
     * Used to get an instance of the ProductMgr.
     * 
     * @return Returns the ProductMgr.
     */
    public ProductMgr getProductMgr()
    {
        return productMgr;
    }

    /**
     * Used to get an instance of the QuotaMgr.
     * 
     * @return Returns the QuotaMgr.
     */
    public QuotaMgr getQuotaMgr()
    {
        return quotaMgr;
    }

    /**
     * Get the Analytics Code from the message catalog if Analytics are enabled
     * 
     * @return the analytics code in the message catalog if analytics are enabled, or return empty
     *         string if not.
     */
    public String getAnalyticsCode()
    {
        String defaultCode = "";
        boolean analyticsEnabled = getConfigAsBoolean(ConfigConstants.ENABLE_ANALYTICS, false);

        if (analyticsEnabled)
        {
            if (log.isDebugEnabled())
            {
                log.debug("analytics enabled");
            }
            return getMsgWithDefault("analytics.code", defaultCode);
        }

        if (log.isDebugEnabled())
        {
            log.debug("analytics not enabled");
        }

        return defaultCode;
    }

    /**
     * Get a message from the message catalog for the current locale.
     * 
     * @param key
     *            the key
     * @return Return the message indexed by the key parameter
     */
    public String getMsg(String key)
    {
        if (key == null)
        {
            return key;
        }

        String msg = null;
        if (sd.isUseDBMsgs())
        {
            HashMap<String, String> hm = sd.getDbMsgMap().get(getLocale());
            if (hm != null)
            {
                msg = hm.get(key);
            } else
            {
                log.warn("Resource hash map for locale " + getLocale() + " could not be found.");
            }
        } else
        {
            ResourceBundle bundle = sd.getBundleMap().get(getLocale());
            if (bundle != null)
            {
                try
                {
                    msg = bundle.getString(key);
                } catch (Exception e)
                {
                    msg = null;
                }
            } else
            {
                log.warn("Resource bundle for locale " + getLocale() + " could not be found.");
            }
        }

        if (msg == null)
        {
            log.warn("Message with key = #" + key + "# not found for locale " + getLocale());
            return key;
        }

        return msg;
    }

    /**
     * Get a message from the message catalog for the current locale. If the message isn't found, a
     * default value is returned.
     * 
     * @param key
     *            the key
     * @param def
     *            the def
     * @return Return the message indexed by the key parameter or the default value
     */
    public String getMsgWithDefault(String key, String def)
    {

        String msg = getMsg(key);
        if (msg == null)
        {
            return def;
        } else if (msg.equals(key))
        {
            return def;
        }
        return msg;
    }

    /**
     * Get a message from the message catalog for the current locale.
     * 
     * @param key
     *            the key
     * @param arg0
     *            Parameter to replace place holder {0}
     * @return Return the message indexed by the key parameter
     */
    public String getMsg(String key, String arg0)
    {
        String template = getMsg(key);
        if (template == null)
        {
            return null;
        }

        if (arg0 != null)
        {
            template = template.replace("{0}", arg0);
        }

        return template;
    }

    /**
     * Get a message from the message catalog for the current locale.
     * 
     * @param key
     *            the key
     * @param arg0
     *            Parameter to replace place holder {0}
     * @param arg1
     *            Parameter to replace place holder {1}
     * @return Return the message indexed by the key parameter
     */
    public String getMsg(String key, String arg0, String arg1)
    {
        String template = getMsg(key);
        if (template == null)
        {
            return null;
        }

        if (arg0 != null)
        {
            template = template.replace("{0}", arg0);
        }

        if (arg1 != null)
        {
            template = template.replace("{1}", arg1);
        }

        return template;
    }

    /**
     * Get a message from the message catalog for the current locale.
     * 
     * @param key
     *            the key
     * @param arg0
     *            Parameter to replace place holder {0}
     * @param arg1
     *            Parameter to replace place holder {1}
     * @param arg2
     *            Parameter to replace place holder {2}
     * @return Return the message indexed by the key parameter
     */
    public String getMsg(String key, String arg0, String arg1, String arg2)
    {
        String template = getMsg(key);
        if (template == null)
        {
            return null;
        }

        if (arg0 != null)
        {
            template = template.replace("{0}", arg0);
        }

        if (arg1 != null)
        {
            template = template.replace("{1}", arg1);
        }

        if (arg2 != null)
        {
            template = template.replace("{2}", arg2);
        }

        return template;
    }

    /**
     * Get a message from the message catalog for the current locale.
     * 
     * @param key
     *            the key
     * @param args
     *            Array of parameters to add to message in place holders {0}, {1} etc
     * @return Return the message indexed by the key parameter
     */
    public String getMsg(String key, String[] args)
    {
        String template = getMsg(key);
        if (template == null)
        {
            return null;
        }

        if (args != null && args.length > 0)
        {
            for (int i = 0; i < args.length; i++)
            {
                String arg = args[i];
                template = template.replace(("{" + i + "}"), arg);
            }
        }

        return template;
    }

    /**
     * Get the current language Id.
     * 
     * @return Returns the langId.
     */
    public int getLangId()
    {
        return langId;
    }

    /**
     * Set the languageId with the id passed in as a parameter.
     * 
     * @param langId
     *            The langId to set.
     */
    public void setLangId(int langId)
    {
        this.langId = langId;
    }

    /**
     * Used to get an instance of the ReviewMgr.
     * 
     * @return Returns the ReviewMgr.
     */
    public ReviewMgr getReviewMgr()
    {
        return reviewMgr;
    }

    /**
     * This is a constant used for searching all products.
     * 
     * @return Returns the sEARCH_ALL.
     */
    public int getSEARCH_ALL()
    {
        return SEARCH_ALL;
    }

    /**
     * Returns the session id of the currently logged in customer.
     * 
     * @return Returns the sessionId.
     */
    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * The forwardAfterLogin is stored for the cases where the user wants to perform some action
     * such as writing a review, which requires him to be logged in. Here we store the forward to
     * the "write review" page so that after the login we can open this page up automatically.
     * 
     * @return Returns the forwardAfterLogin.
     */
    public String getForwardAfterLogin()
    {
        return forwardAfterLogin;
    }

    /**
     * The forwardAfterLogin is stored for the cases where the user wants to perform some action
     * such as writing a review, which requires him to be logged in. Here we store the forward to
     * the "write review" page so that after the login we can open this page up automatically.
     * 
     * @param forwardAfterLogin
     *            The forwardAfterLogin to set.
     */
    public void setForwardAfterLogin(String forwardAfterLogin)
    {
        this.forwardAfterLogin = forwardAfterLogin;
    }

    /**
     * Returns an instance of the KonaKart server engine so that methods may be called directly on
     * it.
     * 
     * @return Returns a KonaKart server engine
     */
    public KKEngIf getEng()
    {
        return eng;
    }

    /**
     * The date template defined in the message catalog (date.format) is used to format the date.
     * 
     * @param date
     *            the date
     * @return Returns a date as a formatted string
     */
    public String getDateAsString(Calendar date)
    {
        if (date == null)
        {
            return "Invalid Date";
        }
        return getDateAsString(date.getTime());
    }

    /**
     * The date template defined in the message catalog (date.format) is used to format the date.
     * 
     * @param date
     *            the date
     * @return Returns a date as a formatted string
     */
    public String getDateAsString(Date date)
    {
        if (date == null)
        {
            return "Invalid Date";
        }

        String dateTemplate = getMsgWithDefault("date.format", "dd/MM/yyyy");
        SimpleDateFormat sdf = new SimpleDateFormat(dateTemplate);
        return sdf.format(date);
    }

    /**
     * The date time template defined in the message catalog (date.time.format) is used to format
     * the date.
     * 
     * @param date
     *            the date
     * @return Returns a date time as a formatted string
     */
    public String getDateTimeAsString(Calendar date)
    {
        if (date == null)
        {
            return "Invalid Date";
        }

        String dateTemplate = getMsgWithDefault("date.time.format", "dd/MM/yyyy HH:mm");
        SimpleDateFormat sdf = new SimpleDateFormat(dateTemplate);
        return sdf.format(date.getTime());
    }

    /**
     * The date template defined in the message catalog (date.format) is used to get the current
     * date and time.
     * 
     * @return Returns the current date as a formatted string
     */
    public String getNowAsString()
    {
        String dateTemplate = getMsgWithDefault("date.format", "dd/MM/yyyy");
        SimpleDateFormat sdf = new SimpleDateFormat(dateTemplate);
        return sdf.format(new Date().getTime());
    }

    /**
     * Returns the string passed in as stringIn without the CData information. i.e. It removes
     * '&lt;![CDATA[' from the start of the string and ']]&gt;' from the end of the string.
     * 
     * @param stringIn
     *            the stringIn
     * @return stringIn without the CDATA info
     */
    public String removeCData(String stringIn)
    {
        if (stringIn == null || stringIn.length() < cDataStart.length() + cDataEnd.length())
        {
            return stringIn;
        }

        String stringOut = stringIn;
        String origStringIn = stringIn;

        if (origStringIn.substring(0, cDataStart.length()).equals(cDataStart))
        {
            stringOut = origStringIn.substring(cDataStart.length());
            origStringIn = stringOut;
        }

        if (origStringIn.substring(origStringIn.length() - cDataEnd.length(), origStringIn.length())
                .equals(cDataEnd))
        {
            stringOut = origStringIn.substring(0, origStringIn.length() - cDataEnd.length());
        }

        return stringOut;
    }

    /**
     * Log-off and reset some variables.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void logout() throws KKException
    {
        if (getSessionId() != null)
        {
            eng.logout(getSessionId());
            setSessionId(null);
        }
        getSessionCheckData().logout();
    }

    /**
     * Used to get an instance of the OrderMgr.
     * 
     * @return Returns the OrderMgr.
     */
    public OrderMgr getOrderMgr()
    {
        return orderMgr;
    }

    /**
     * Form Validation: Minimum length of value for city
     * 
     * @return Returns the minimum length of value for city
     */
    public int getCityMinLength()
    {
        return sd.getCityMinLength();
    }

    /**
     * Form Validation: Minimum length of value for company
     * 
     * @return Returns the minimum length of value for company
     */
    public int getCompanyMinLength()
    {
        return sd.getCompanyMinLength();
    }

    /**
     * Form Validation: Minimum length of value for first name
     * 
     * @return Returns the minimum length of value for first name
     */
    public int getFirstNameMinLength()
    {
        return sd.getFirstNameMinLength();
    }

    /**
     * Form Validation: Minimum length of value for last name
     * 
     * @return Returns the minimum length of value for last name
     */
    public int getLastNameMinLength()
    {
        return sd.getLastNameMinLength();
    }

    /**
     * Form Validation: Minimum length of value for password
     * 
     * @return Returns the minimum length of value for password
     */
    public int getPasswordMinLength()
    {
        return sd.getPasswordMinLength();
    }

    /**
     * Form Validation: Minimum length of value for post code
     * 
     * @return Returns the minimum length of value for post code
     */
    public int getPostCodeMinLength()
    {
        return sd.getPostCodeMinLength();
    }

    /**
     * Form Validation: Minimum length of value for state
     * 
     * @return Returns the minimum length of value for state
     */
    public int getStateMinLength()
    {
        return sd.getStateMinLength();
    }

    /**
     * Form Validation: Minimum length of value for street address
     * 
     * @return Returns the minimum length of value for street address
     */
    public int getStreetAddrMinLength()
    {
        return sd.getStreetAddrMinLength();
    }

    /**
     * Form Validation: Minimum length of value for street address 1
     * 
     * @return Returns the minimum length of value for street address 1
     */
    public int getStreetAddr1MinLength()
    {
        return sd.getStreetAddr1MinLength();
    }

    /**
     * Form Validation: Minimum length of value for telephone
     * 
     * @return Returns the minimum length of value for telephone
     */
    public int getTelephoneMinLength()
    {
        return sd.getTelephoneMinLength();
    }

    /**
     * The maximum number of address book entries allowed
     * 
     * @return Returns the maximum number of address book entries allowed
     */
    public int getMaxAddressBookEntries()
    {
        return sd.getMaxAddressBookEntries();
    }

    /**
     * The StoreBase is set by a configuration parameter.
     * 
     * @return Returns the storeBase.
     */
    public String getStoreBase()
    {
        return sd.getStoreBase();
    }

    /**
     * The ImageBase is set by a configuration parameter.
     * 
     * @return Returns the imageBase.
     */
    public String getImageBase()
    {
        if (getPortletContextPath() == null)
        {
            return sd.getImageBase();
        }
        return getPortletContextPath() + sd.getImageBase();
    }

    /**
     * Returns the product image base
     * 
     * @param prod
     *            Product object
     * @return Returns the product image base.
     */
    public String getProdImageBase(ProductIf prod)
    {
        if (prod == null)
        {
            return null;
        }
        if (getPortletContextPath() == null)
        {
            return sd.getImageBase() + prod.getImageDir() + prod.getUuid();
        }
        return getPortletContextPath() + sd.getImageBase() + prod.getImageDir() + prod.getUuid();
    }

    /**
     * Returns the product image extension
     * 
     * @param prod
     *            Product object
     * @return Returns the product image extension.
     */
    public String getProdImageExtension(ProductIf prod)
    {
        if (prod == null)
        {
            return null;
        }
        String ret = null;
        if (prod.getImage() != null)
        {
            ret = FilenameUtils.getExtension(prod.getImage());
        } else if (prod.getImage2() != null)
        {
            ret = FilenameUtils.getExtension(prod.getImage2());
        } else if (prod.getImage3() != null)
        {
            ret = FilenameUtils.getExtension(prod.getImage3());
        } else if (prod.getImage4() != null)
        {
            ret = FilenameUtils.getExtension(prod.getImage4());
        }
        if (ret == null || ret.length() == 0)
        {
            return "";
        }
        return "." + ret;
    }

    /**
     * Returns the product image based on a size parameter passed in and the selected options
     * 
     * @param prod
     *            the prod
     * @param opts
     *            the opts
     * @param size
     *            the size
     * @return Returns the product image based on a size parameter passed in and the selected
     *         options
     */
    public String getProdImage(ProductIf prod, OptionIf[] opts, int size)
    {
        if (opts == null || opts.length == 0 || prod == null || prod.getImages() == null
                || prod.getImages().getImgNames() == null
                || prod.getImages().getImgNames().length == 0)
        {
            return getProdImage(prod, size);
        }

        ArrayList<String> combinedCodes = new ArrayList<String>();
        for (int i = 0; i < opts.length; i++)
        {
            OptionIf opt = opts[i];
            if (opt.getCode() != null && opt.getCode().length() > 0 && opt.getValueCode() != null
                    && opt.getValueCode().length() > 0)
            {
                String searchString = "_" + opt.getCode() + "_" + opt.getValueCode();
                switch (size)
                {
                case IMAGE_BIG:
                    combinedCodes.add(searchString + "_1_big");
                case IMAGE_MEDIUM:
                    combinedCodes.add(searchString + "_1_medium");
                case IMAGE_SMALL:
                    combinedCodes.add(searchString + "_1_small");
                case IMAGE_TINY:
                    combinedCodes.add(searchString + "_1_tiny");
                default:
                    combinedCodes.add(searchString + "_1_big");
                }
            }
        }

        // Check to see whether any of the options have codes
        if (combinedCodes.isEmpty())
        {
            return getProdImage(prod, size);
        }

        // Search for an image and return the first one we find
        for (int i = prod.getImages().getImgNames().length - 1; i > -1; i--)
        {
            String name = prod.getImages().getImgNames()[i];
            for (Iterator<String> iterator = combinedCodes.iterator(); iterator.hasNext();)
            {
                String combinedCode = iterator.next();
                if (name != null && name.contains(combinedCode))
                {
                    return getImageBase() + name;
                }

            }
        }

        return getProdImage(prod, size);
    }

    /**
     * Returns the product image based on a size parameter passed in
     * 
     * @param prod
     *            the prod
     * @param size
     *            the size
     * @return Returns the product image based on a size parameter passed in
     */
    public String getProdImage(ProductIf prod, int size)
    {
        switch (size)
        {
        case IMAGE_BIG:
            return getProdImageBase(prod) + "_1_big" + getProdImageExtension(prod);
        case IMAGE_MEDIUM:
            return getProdImageBase(prod) + "_1_medium" + getProdImageExtension(prod);
        case IMAGE_SMALL:
            return getProdImageBase(prod) + "_1_small" + getProdImageExtension(prod);
        case IMAGE_TINY:
            return getProdImageBase(prod) + "_1_tiny" + getProdImageExtension(prod);
        default:
            return getProdImageBase(prod) + "_1_big" + getProdImageExtension(prod);
        }
    }

    /**
     * The ScriptBase is set by a configuration parameter.
     * 
     * @return Returns the scriptBase.
     */
    public String getScriptBase()
    {
        return sd.getScriptBase();
    }

    /**
     * The StyleBase is set by a configuration parameter.
     * 
     * @return Returns the styleBase.
     */
    public String getStyleBase()
    {
        return sd.getStyleBase();
    }

    /**
     * Warn when the stock level of a product gets below this value.
     * 
     * @return Return the stock warning level
     */
    public int getStockWarnLevel()
    {
        return sd.getStockWarnLevel();
    }

    /**
     * Returns true if wish list functionality is enabled
     * 
     * @return Returns true if wish list functionality is enabled
     */
    public boolean isWishListEnabled()
    {
        return sd.isWishListEnabled();
    }

    /**
     * Returns the available currencies in the system
     * 
     * @return Return an array of currency objects
     */
    public CurrencyIf[] getCurrencies()
    {
        if (sd.getDefaultCurrency().getCode().equalsIgnoreCase(getUserCurrency().getCode()))
        {
            return sd.getCurrencies();
        }

        /*
         * Otherwise we have to put the user selected currency on top
         */
        CurrencyIf[] sortedCurrencies = new CurrencyIf[sd.getCurrencies().length];
        int index = 1;
        for (int i = 0; i < sd.getCurrencies().length; i++)
        {
            CurrencyIf c = sd.getCurrencies()[i];
            if (c.getCode().equalsIgnoreCase(getUserCurrency().getCode()))
            {
                sortedCurrencies[0] = c;
            } else
            {
                if (index < sortedCurrencies.length)
                {
                    sortedCurrencies[index] = c;
                    index++;
                }
            }
        }
        return sortedCurrencies;
    }

    /**
     * Returns the currency object for the currency code. Attempts to retrieve it from the cache
     * before making an engine API call.
     * 
     * @param currencyCode
     *            the currencyCode
     * 
     * @return Returns the currency object for the currency code
     * @throws KKException
     *            an unexpected KKException exception
     */
    public CurrencyIf getCurrency(String currencyCode) throws KKException
    {
        if (currencyCode == null)
        {
            return null;
        }

        CurrencyIf ret = sd.getCurrencyHM().get(currencyCode);
        if (ret == null)
        {
            return eng.getCurrency(currencyCode);
        }

        return ret;
    }

    /**
     * Called by the UI to determine whether to display prices with tax.
     * 
     * @return True if we should display prices with tax
     */
    public boolean displayPriceWithTax()
    {
        String value = getConfig(ConfigConstants.DISPLAY_PRICE_WITH_TAX);
        if (value != null && value.equalsIgnoreCase("false"))
        {
            return false;
        }
        return true;
    }

    /**
     * Used to get an instance of the BasketMgr.
     * 
     * @return Returns the basketMgr.
     */
    public BasketMgr getBasketMgr()
    {
        return basketMgr;
    }

    /**
     * Used to get an instance of the CustomerMgr.
     * 
     * @return Returns the customerMgr.
     */
    public CustomerMgr getCustomerMgr()
    {
        return customerMgr;
    }

    /**
     * Used to get an instance of the ContentMgr.
     * 
     * @return Returns the contentMgr.
     */
    public ContentMgr getContentMgr()
    {
        return contentMgr;
    }

    /**
     * Set the sessionId with the string passed in as a parameter.
     * 
     * @param sessionId
     *            The sessionId to set.
     */
    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    /**
     * XMLOverHTTPResp is used to temporary store an XML response.
     * 
     * @return Returns the xMLOverHTTPResp.
     */
    public String getXMLOverHTTPResp()
    {
        return XMLOverHTTPResp;
    }

    /**
     * XMLOverHTTPResp is used to temporary store an XML response.
     * 
     * @param overHTTPResp
     *            The xMLOverHTTPResp to set.
     */
    public void setXMLOverHTTPResp(String overHTTPResp)
    {
        XMLOverHTTPResp = overHTTPResp;
    }

    /**
     * Whether SSL is enabled or not is determined by the ENABLE_SSL configuration variable.
     * 
     * @return Returns the enableSSL.
     */
    public boolean isEnableSSL()
    {
        return sd.isEnableSSL();
    }

    /**
     * Whether SSL is enabled or not is determined by the ENABLE_SSL configuration variable.
     * 
     * @param enableSSL
     *            The enableSSL to set.
     */
    public void setEnableSSL(boolean enableSSL)
    {
        sd.setEnableSSL(enableSSL);
    }

    /**
     * The standard port "443" is used unless it is overridden by the SSL_PORT_NUMBER configuration
     * variable.
     * 
     * @return Returns the sslPort.
     */
    public String getSslPort()
    {
        return sd.getSslPort();
    }

    /**
     * The standard port "443" is used unless it is overridden by the SSL_PORT_NUMBER configuration
     * variable.
     * 
     * @param sslPort
     *            The sslPort to set.
     */
    public void setSslPort(String sslPort)
    {
        sd.setSslPort(sslPort);
    }

    /**
     * The standard port "80" is used unless it is overridden by the STANDARD_PORT_NUMBER
     * configuration.
     * 
     * @return Returns the standardPort.
     */
    public String getStandardPort()
    {
        return sd.getStandardPort();
    }

    /**
     * The standard port "80" is used unless it is overridden by the STANDARD_PORT_NUMBER
     * configuration.
     * 
     * @param standardPort
     *            The standardPort to set.
     */
    public void setStandardPort(String standardPort)
    {
        sd.setStandardPort(standardPort);
    }

    /**
     * Used by the JSPs to set the content of the title tag. The value is determined based on a
     * template which is in the Messages.properties file. The template can be different depending on
     * whether we are viewing a single product or a list of products for a manufacturer, or a list
     * of products within a category.
     * 
     * @return Returns the pageTitle.
     */
    public String getPageTitle()
    {
        return pageTitle;
    }

    /**
     * Used by the JSPs to set the content of the title tag. The value is determined based on a
     * template which is in the Messages.properties file. The template can be different depending on
     * whether we are viewing a single product or a list of products for a manufacturer, or a list
     * of products within a category.
     * 
     * @param pageTitle
     *            The pageTitle to set.
     */
    public void setPageTitle(String pageTitle)
    {
        this.pageTitle = pageTitle;
    }

    /**
     * Used by the JSPs to set the content of the meta description tag. The value is determined
     * based on a template which is in the Messages.properties file. The template can be different
     * depending on whether we are viewing a single product or a list of products for a
     * manufacturer, or a list of products within a category.
     * 
     * @return Returns the metaDescription.
     */
    public String getMetaDescription()
    {
        return metaDescription;
    }

    /**
     * Used by the JSPs to set the content of the meta description tag. The value is determined
     * based on a template which is in the Messages.properties file. The template can be different
     * depending on whether we are viewing a single product or a list of products for a
     * manufacturer, or a list of products within a category.
     * 
     * @param metaDescription
     *            The metaDescription to set.
     */
    public void setMetaDescription(String metaDescription)
    {
        this.metaDescription = metaDescription;
    }

    /**
     * Used by the JSPs to set the content of the meta keywords tag. The value is determined based
     * on a template which is in the Messages.properties file.
     * 
     * @return Returns the metaKeywords.
     */
    public String getMetaKeywords()
    {
        return metaKeywords;
    }

    /**
     * Used by the JSPs to set the content of the meta keywords tag. The value is determined based
     * on a template which is in the Messages.properties file.
     * 
     * @param metaKeywords
     *            The metaKeywords to set.
     */
    public void setMetaKeywords(String metaKeywords)
    {
        this.metaKeywords = metaKeywords;
    }

    /**
     * Returns true if KonaKart is configured to use Solr
     * 
     * @return Returns true if KonaKart is configured to use Solr
     */
    public boolean isUseSolr()
    {
        return sd.isUseSolr();
    }

    /**
     * Returns true if KonaKart is configured to retrieve spelling suggestions
     * 
     * @return Returns true if KonaKart is configured to retrieve spelling suggestions
     */
    public boolean isGetSpellingSuggestions()
    {
        return sd.isGetSpellingSuggestions();
    }

    /**
     * The default condition is that the sslBaseUrl isn't used unless the SSL_BASE_URL configuration
     * variable is set. Normally just the port is changed to switch between SSL and non SSL. However
     * in some cases SSL communication may need a different URL.
     * 
     * @return Returns the sslBaseUrl.
     */
    public String getSslBaseUrl()
    {
        return sd.getSslBaseUrl();
    }

    /**
     * @return Returns the ddbasePath.
     */
    public String getDdbasePath()
    {
        return sd.getDdbasePath();
    }

    /**
     * The value of the Digital Download base path is read from the DD_BASE_PATH configuration
     * variable. The base path is prepended to the file path in order to get the full path of the
     * file.
     * 
     * @param ddbasePath
     *            The ddbasePath to set.
     */
    public void setDdbasePath(String ddbasePath)
    {
        sd.setDdbasePath(ddbasePath);
    }

    /**
     * The value is read from the DD_DOWNLOAD_AS_ATTACHMENT configuration variable. When set to
     * true, the digital download product is downloaded as an attachment that can be saved to disk.
     * When set to false, the digital download product may be viewed within the browser.
     * 
     * @return Returns the ddAsAttachment.
     */
    public boolean isDdAsAttachment()
    {
        return sd.isDdAsAttachment();
    }

    /**
     * The value is read from the DD_DOWNLOAD_AS_ATTACHMENT configuration variable. When set to
     * true, the digital download product is downloaded as an attachment that can be saved to disk.
     * When set to false, the digital download product may be viewed within the browser.
     * 
     * @param ddAsAttachment
     *            The ddAsAttachment to set.
     */
    public void setDdAsAttachment(boolean ddAsAttachment)
    {
        sd.setDdAsAttachment(ddAsAttachment);
    }

    /**
     * Can be used for any custom logic
     * 
     * @return Returns the custom1.
     */
    public String getCustom1()
    {
        return custom1;
    }

    /**
     * Can be used for any custom logic
     * 
     * @param custom1
     *            The custom1 to set.
     */
    public void setCustom1(String custom1)
    {
        this.custom1 = custom1;
    }

    /**
     * Can be used for any custom logic
     * 
     * @return Returns the custom2.
     */
    public String getCustom2()
    {
        return custom2;
    }

    /**
     * Can be used for any custom logic
     * 
     * @param custom2
     *            The custom2 to set.
     */
    public void setCustom2(String custom2)
    {
        this.custom2 = custom2;
    }

    /**
     * Can be used for any custom logic
     * 
     * @return Returns the custom3.
     */
    public String getCustom3()
    {
        return custom3;
    }

    /**
     * Can be used for any custom logic
     * 
     * @param custom3
     *            The custom3 to set.
     */
    public void setCustom3(String custom3)
    {
        this.custom3 = custom3;
    }

    /**
     * Can be used for any custom logic
     * 
     * @return Returns the custom4.
     */
    public String getCustom4()
    {
        return custom4;
    }

    /**
     * Can be used for any custom logic
     * 
     * @param custom4
     *            The custom4 to set.
     */
    public void setCustom4(String custom4)
    {
        this.custom4 = custom4;
    }

    /**
     * Can be used for any custom logic
     * 
     * @return Returns the custom5.
     */
    public String getCustom5()
    {
        return custom5;
    }

    /**
     * Can be used for any custom logic
     * 
     * @param custom5
     *            The custom5 to set.
     */
    public void setCustom5(String custom5)
    {
        this.custom5 = custom5;
    }

    /**
     * Custom variables used to hold data temporarily for passing to included JSPs
     * 
     * @return the customTemp1
     */
    public String getCustomTemp1()
    {
        return customTemp1;
    }

    /**
     * Custom variables used to hold data temporarily for passing to included JSPs
     * 
     * @param customTemp1
     *            the customTemp1 to set
     */
    public void setCustomTemp1(String customTemp1)
    {
        this.customTemp1 = customTemp1;
    }

    /**
     * @return Returns the engConf.
     */
    public static EngineConfigIf getEngConf()
    {
        return engConf;
    }

    /**
     * Return the storeId. If the storeInfo object has a non-null storeId we return the storeId
     * stored in that object. If the storeInfo object is null or its storeId is null we return a
     * default storeId. The default storeId is the one provided as an input (could be from
     * konakart_app.properties) or the cdefault store constant if the storeId passed in is null.
     * 
     * @param defaultStoreId
     *            default storeId
     * 
     * @return Return the storeId
     */
    public String getStoreId(String defaultStoreId)
    {
        String defStore = defaultStoreId;
        if (defStore == null)
        {
            defStore = KKConstants.KONAKART_DEFAULT_STORE_ID;
        }

        if (storeInfo == null)
        {
            return defStore;
        } else if (storeInfo.getStoreId() == null)
        {
            return defStore;
        } else
        {
            return storeInfo.getStoreId();
        }
    }

    /**
     * Return the storeId
     * 
     * @return Return the storeId
     */
    public String getStoreId()
    {
        return getStoreId(KKConstants.KONAKART_DEFAULT_STORE_ID);
    }

    /**
     * Returns the updateThreadList.
     * 
     * @return Returns the updateThreadList.
     */
    public static List<ConfigCacheUpdater> getUpdateThreadList()
    {
        return updateThreadList;
    }

    /**
     * Returns the wishListMgr.
     * 
     * @return Returns the wishListMgr.
     */
    public WishListMgr getWishListMgr()
    {
        return wishListMgr;
    }

    /**
     * A custom object that can be attached to the KonaKart client engine to store custom data.
     * 
     * @return the customObj
     */
    public Object getCustomObj()
    {
        return customObj;
    }

    /**
     * A custom object that can be attached to the KonaKart client engine to store custom data.
     * 
     * @param customObj
     *            the customObj to set
     */
    public void setCustomObj(Object customObj)
    {
        this.customObj = customObj;
    }

    /**
     * Returns the user's currency which may be different to the default currency
     * 
     * @return Returns a currency object
     */
    public CurrencyIf getUserCurrency()
    {
        if (userCurrency == null)
        {
            return sd.getDefaultCurrency();
        }
        return userCurrency;
    }

    /**
     * Returns the default currency
     * 
     * @return Returns a currency object
     */
    public CurrencyIf getDefaultCurrency()
    {
        return sd.getDefaultCurrency();
    }

    /**
     * Returns the user's currency formatter which may be different to the default currency
     * formatter
     * 
     * @return Returns a DecimalFormat object
     */
    public DecimalFormat getUserCurrencyFormatter()
    {
        if (userCurrencyFormatter == null)
        {
            return sd.getCurrencyFormatter();
        }
        return userCurrencyFormatter;
    }

    /**
     * Used to set the user's currency. The currency object is looked up from the code.
     * 
     * @param currencyCode
     *            The 3 letter currency code (i.e. USD, EUR ..)
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void setUserCurrency(String currencyCode) throws KKException
    {
        if (currencyCode == null)
        {
            return;
        }

        /*
         * If the user's currency is set to the default currency then set userCurrency and
         * userCurrencyFormatter to null so that the default one is picked up.
         */
        if (currencyCode.equalsIgnoreCase(sd.getDefaultCurrency().getCode()))
        {
            userCurrency = null;
            userCurrencyFormatter = null;
            return;
        }

        CurrencyIf curr = getCurrency(currencyCode);
        if (curr != null)
        {
            userCurrency = curr;
            userCurrencyFormatter = getFormatter(curr);
        }
    }

    /**
     * Return All Countries with the names set from the message catalog. If any are set from the
     * message catalog the list is ordered by name before returning.
     * 
     * @return the countries for the current locale
     * @throws KKException
     *            an unexpected KKException exception
     */
    public CountryIf[] getAllCountries() throws KKException
    {
        // If we have them cached we just return them
        CountryIf[] countries = sd.getCountryListsHM().get(getLocale());
        if (countries != null)
        {
            return countries;
        }
        synchronized (mutex)
        {
            /*
             * Try again just in case another thread just fetched the countries
             */
            countries = sd.getCountryListsHM().get(getLocale());
            if (countries != null)
            {
                return countries;
            }

            // We'll have to look them up...
            countries = getEng().getAllCountries();

            // If the "Use Msg Cat for Country Names" config variable isn't set we use the DB list.
            // Before returning we add the countries to a hash map.
            if (!getConfigAsBoolean(KKConstants.CONF_KEY_USE_MSG_CAT_FOR_COUNTRY_NAMES, false))
            {
                HashMap<Integer, CountryIf> countryMap = new HashMap<Integer, CountryIf>();
                for (int c = 0; c < countries.length; c++)
                {
                    CountryIf ctry = countries[c];
                    countryMap.put(new Integer(ctry.getId()), ctry);
                }

                // Save this list and hash map for the current locale
                sd.getCountryListsHM().put(getLocale(), countries);
                sd.getCountryMapsHM().put(getLocale(), countryMap);

                return countries;
            }

            // Now we go through the country list from the database and replace the name with the
            // name
            // from the message catalog (if it exists). We also add them to a hash map so that they
            // can
            // be quickly looked up by id.
            HashMap<Integer, CountryIf> countryMap = new HashMap<Integer, CountryIf>();
            for (int c = 0; c < countries.length; c++)
            {
                CountryIf ctry = countries[c];
                if (ctry.getMsgCatKey() != null)
                {
                    ctry.setName(getMsgWithDefault(ctry.getMsgCatKey(), ctry.getName()));
                }
                countryMap.put(new Integer(ctry.getId()), ctry);
            }

            // Now we need to order the country list because in another language the order might be
            // different

            Arrays.sort(countries, countryComparer);

            // Save this list and hash map for the current locale
            sd.getCountryListsHM().put(getLocale(), countries);
            sd.getCountryMapsHM().put(getLocale(), countryMap);

            return countries;
        }

    }

    private Comparator<CountryIf> countryComparer = new Comparator<CountryIf>()
    {
        public int compare(CountryIf X, CountryIf Y)
        {
            return getMyCollator().compare(X.getName(), Y.getName());
        }
    };

    /**
     * Returns the country referenced by the id for the current locale.
     * 
     * @param countryId
     *            Numeric id of country
     * 
     * @return Returns the country referenced by the id for the current locale
     * @throws KKException
     *            an unexpected KKException exception
     */
    public CountryIf getCountry(int countryId) throws KKException
    {
        if (!sd.isCachesFilled())
        {
            // Return it from the engine
            return eng.getCountry(countryId);
        }

        // If we have them cached we just look up from the map
        HashMap<Integer, CountryIf> countryMap = sd.getCountryMapsHM().get(getLocale());
        if (countryMap != null)
        {
            return countryMap.get(new Integer(countryId));
        }

        // get countries for locale (also creates hash map)
        getAllCountries();

        countryMap = sd.getCountryMapsHM().get(getLocale());
        if (countryMap == null)
        {
            throw new KKException("Unable to create Hash Map of countries");
        }
        return countryMap.get(new Integer(countryId));
    }

    /**
     * Get the active customer Id. This could be an admin user or the current customer
     * 
     * @return the Id of the current customer or admin user
     */
    public int getActiveCustId()
    {
        if (getAdminUser() != null)
        {
            // Admin User is set so we'll use this as the current customer

            return getAdminUser().getId();
        }

        if (getCustomerMgr().getCurrentCustomer() != null)
        {
            return getCustomerMgr().getCurrentCustomer().getId();
        }

        return 0;
    }

    /**
     * @return the customMap
     */
    public Map<String, String> getCustomMap()
    {
        if (customMap == null)
        {
            customMap = Collections.synchronizedMap(new HashMap<String, String>());
        }
        return customMap;
    }

    /**
     * Set a Custom config in the Custom Map on the session
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void setCustomConfig(String key, String value)
    {
        getCustomMap().put(key, value);
    }

    /**
     * Get a Custom config value from the Custom Map on the session
     * 
     * @param key
     *            key of the Custom config value
     * @return the value found in the Custom Map or null if not found
     */
    public String getCustomConfig(String key)
    {
        return getCustomConfig(key, false);
    }

    /**
     * Get a Custom config value from the Custom Map on the session
     * 
     * @param key
     *            key of the Custom config value
     * @param remove
     *            if true the key is removed from the Custom Config map after being retrieved
     * @return the value found in the Custom Map or null if not found
     */
    public String getCustomConfig(String key, boolean remove)
    {
        String keyValue = getCustomMap().get(key);
        getCustomMap().remove(key);

        return keyValue;
    }

    /**
     * Get a Custom config value as a boolean from the Custom Map on the session
     * 
     * @param key
     *            key of the Custom config value
     * @param def
     *            the default value if the key isn't present
     * @return the value as a boolean found in the Custom Map or the default value if not found
     */
    public boolean getCustomConfigAsBool(String key, boolean def)
    {
        String conf = getCustomMap().get(key);

        if (conf == null)
        {
            return def;
        }

        return Boolean.parseBoolean(conf);
    }

    /**
     * If KK Cookie functionality is installed it sets the kkCookieEnabled boolean to TRUE
     */
    private void checkKKCookieInstalled()
    {
        try
        {
            getEng().getCookie("aaaa", "bbbb");
        } catch (KKException e)
        {
            kkCookieEnabled = false;
            log.debug("KK Cookie functionality NOT installed");
            return;
        }
        log.debug("KK Cookie functionality IS installed");
        kkCookieEnabled = true;
    }

    /**
     * Determines whether The KonaKart Cookie functionality is installed.
     * 
     * @return the kkCookieEnabled
     */
    public boolean isKkCookieEnabled()
    {
        return kkCookieEnabled;
    }

    /**
     * An options object that could for example point this customer to a special catalog
     * 
     * @return the fetchProdOptions
     */
    public FetchProductOptionsIf getFetchProdOptions()
    {
        return fetchProdOptions;
    }

    /**
     * An options object that could for example point this customer to a special catalog
     * 
     * @param fetchProdOptions
     *            the fetchProdOptions to set
     */
    public void setFetchProdOptions(FetchProductOptionsIf fetchProdOptions)
    {
        this.fetchProdOptions = fetchProdOptions;
        FetchProductOptionsIf fpo = new KKBeanCopier().cloneFetchProductOptions(fetchProdOptions);
        fpo.setGetImages(false);
        this.setFetchProdOptionsNoImages(fpo);
    }

    /**
     * @return Returns the customerTagMgr
     */
    public CustomerTagMgr getCustomerTagMgr()
    {
        return customerTagMgr;
    }

    /**
     * @return Returns the rewardPointsMgr
     */
    public RewardPointMgr getRewardPointMgr()
    {
        return rewardPointMgr;
    }

    /**
     * @return the myCollator
     */
    public Collator getMyCollator()
    {
        if (myCollator != null)
        {
            return myCollator;
        }

        return Collator.getInstance();
    }

    /**
     * @param myCollator
     *            the myCollator to set
     */
    public void setMyCollator(Collator myCollator)
    {
        this.myCollator = myCollator;
    }

    /**
     * The administrator who has logged for the customer
     * 
     * @return the adminUser
     */
    public CustomerIf getAdminUser()
    {
        return adminUser;
    }

    /**
     * The administrator who has logged for the customer
     * 
     * @param adminUser
     *            the adminUser to set
     */
    public void setAdminUser(CustomerIf adminUser)
    {
        this.adminUser = adminUser;
    }

    /**
     * Set to true when running as a portlet
     * 
     * @return the portlet
     */
    public boolean isPortlet()
    {
        return portlet;
    }

    /**
     * Set to true when running as a portlet
     * 
     * @param portlet
     *            the portlet to set
     */
    public void setPortlet(boolean portlet)
    {
        this.portlet = portlet;
    }

    /**
     * @return the kkVersion
     */
    public String getKkVersion()
    {
        if (this.kkVersion == null)
        {
            if (eng != null)
            {
                try
                {
                    this.kkVersion = eng.getKonaKartVersion();
                } catch (KKException e)
                {
                    // no recovery possible
                    return "";
                }
            } else
            {
                return "";
            }
        }

        return this.kkVersion;
    }

    /**
     * The Id of an affiliate partner
     * 
     * @return the affiliateId
     */
    public String getAffiliateId()
    {
        return affiliateId;
    }

    /**
     * The Id of an affiliate partne
     * 
     * @param affiliateId
     *            the affiliateId to set
     */
    public void setAffiliateId(String affiliateId)
    {
        this.affiliateId = affiliateId;
    }

    /**
     * Information required for performing punch out. Normally null.
     * 
     * @return the punchoutDetails
     */
    public PunchOut getPunchoutDetails()
    {
        return punchoutDetails;
    }

    /**
     * Information required for performing punch out. Normally null.
     * 
     * @param punchoutDetails
     *            the punchoutDetails to set
     */
    public void setPunchoutDetails(PunchOut punchoutDetails)
    {
        this.punchoutDetails = punchoutDetails;
    }

    /**
     * @return the session
     */
    public HttpSession getSession()
    {
        return session;
    }

    /**
     * @param session
     *            the session to set
     */
    public void setSession(HttpSession session)
    {
        this.session = session;
    }

    /**
     * Refresh the message resources - Called from RefreshCachedData()
     * 
     * @throws KKException
     */
    private void refreshMsgResources() throws KKException
    {
        boolean useDB = false;
        String useDBStr = getConfig(ConfigConstants.USE_DB_FOR_MESSAGES);
        if (useDBStr != null && useDBStr.equalsIgnoreCase("true"))
        {
            useDB = true;
        }
        sd.setUseDBMsgs(useDB);

        LanguageIf[] langs = eng.getAllLanguages();
        if (langs == null || langs.length == 0)
        {
            throw new KKException("No languages present in the KonaKart database");
        }

        for (int i = 0; i < langs.length; i++)
        {
            LanguageIf lang = langs[i];
            if (lang.getLocale() == null || lang.getLocale().length() == 0)
            {
                log.warn("The language " + lang.getName()
                        + " does not have a local defined. Message resource bundle for this language could not be created.");
                return;
            }
            String[] codes = lang.getLocale().split("_");
            if (codes.length != 2)
            {
                log.warn("The language " + lang.getName()
                        + " does not have a local defined in the correct format (lang_COUNTRY). Message resource bundle for this language could not be created.");
                return;
            }
            ResourceBundle bundle = null;
            if (useDB)
            {
                NameValueIf[] msgArray = getEng().getMessages(KKConstants.MSG_TYP_APPLICATION,
                        lang.getLocale());
                if (msgArray == null || msgArray.length == 0)
                {
                    log.warn("No application database messages found for locale  "
                            + lang.getLocale());
                } else
                {
                    HashMap<String, String> hm = new HashMap<String, String>();
                    for (int j = 0; j < msgArray.length; j++)
                    {
                        NameValueIf nv = msgArray[j];
                        hm.put(nv.getName(), nv.getValue());
                    }
                    sd.getDbMsgMap().put(lang.getLocale(), hm);
                }
            } else
            {
                try
                {
                    bundle = ResourceBundle.getBundle("Messages", new Locale(codes[0], codes[1]));
                } catch (Exception e)
                {
                    log.error("Problem creating resource bundle for language " + lang.getLocale(),
                            e);
                }
                if (bundle != null)
                {
                    sd.getBundleMap().put(lang.getLocale(), bundle);
                }
            }
        }
    }

    /**
     * Used to keep track of where we are. Header.jsp uses it to display our current position within
     * the application. i.e. Top &gt;&gt; Catalog &gt;&gt; Checkout
     * 
     * @return the nav
     */
    public CurrentNavigation getNav()
    {
        return nav;
    }

    /**
     * Used to keep track of where we are. Header.jsp uses it to display our current position within
     * the application. i.e. Top &gt;&gt; Catalog &gt;&gt; Checkout
     * 
     * @param nav
     *            the nav to set
     */
    public void setNav(CurrentNavigation nav)
    {
        this.nav = nav;
    }

    /**
     * @return the contentClass
     */
    public String getContentClass()
    {
        return contentClass;
    }

    /**
     * @param contentClass
     *            the contentClass to set
     */
    public void setContentClass(String contentClass)
    {
        this.contentClass = contentClass;
    }

    /**
     * @return the portletContextPath
     */
    public String getPortletContextPath()
    {
        return portletContextPath;
    }

    /**
     * @param portletContextPath
     *            the portletContextPath to set
     */
    public void setPortletContextPath(String portletContextPath)
    {
        this.portletContextPath = portletContextPath;
    }

    /**
     * Hash map used to store objects on KKAppEng
     * 
     * @return the objMap
     */
    public HashMap<String, Object> getObjMap()
    {
        return objMap;
    }

    /**
     * Hash map used to store objects on KKAppEng
     * 
     * @param objMap
     *            the objMap to set
     */
    public void setObjMap(HashMap<String, Object> objMap)
    {
        this.objMap = objMap;
    }

    /**
     * Returns true if the store is in multi-vendor mode
     * 
     * @return Returns true if the store is in multi-vendor mode
     */
    public boolean isMultiVendor()
    {
        boolean multiVendorMode = getConfigAsBoolean(ConfigConstants.MULTI_VENDOR_MODE, false);
        if (multiVendorMode)
        {
            if (eng.getEngConf() != null)
            {
                int mode = eng.getEngConf().getMode();
                boolean sharedProducts = eng.getEngConf().isProductsShared();
                if (!(mode == EngineConfig.MODE_MULTI_STORE_SHARED_DB && sharedProducts))
                {
                    log.error(
                            "Multi Vendor Mode cannot be activated unless you are sharing products in multi-store mode");
                    return false;
                }
            }
        }

        return multiVendorMode;
    }

    /**
     * Determines whether the storefront application should display links to a store details page
     * 
     * @return Returns true if the links should be displayed
     */
    public boolean isMultiVendorStoreLinks()
    {
        return (getConfigAsBoolean(ConfigConstants.MULTI_VENDOR_STORE_LINKS, true));
    }

    /**
     * Returns true if the Enterprise version of KonaKart is being used
     * 
     * @return Returns true if the Enterprise version of KonaKart is being used
     */
    public boolean isEnterprise()
    {
        String engId = engConf.getEngineId();
        if (engId != null && engId.equals(KKConstants.MODE_ENTERPRISE))
        {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the Business version of KonaKart is being used
     * 
     * @return Returns true if the Business version of KonaKart is being used
     */
    public boolean isBusiness()
    {
        String engId = engConf.getEngineId();
        if (engId != null && engId.equals(KKConstants.MODE_BUSINESS))
        {
            return true;
        }
        return false;
    }

    /**
     * Returns true if a customer is required to login in order to use the storefront application
     * 
     * @return Returns true if a customer is required to login
     */
    public boolean isForceLogin()
    {
        boolean forceLogin = getConfigAsBoolean(ConfigConstants.APP_FORCE_LOOGIN, false);
        return forceLogin;
    }

    /**
     * Returns an engine instance for a store id. The instance is cached in a hash map and returned
     * from the cache in future calls.
     * 
     * @param storeId
     *            the storeId
     * @return Returns an EngineData instance for a store id.
     * @throws IllegalArgumentException
     *            an unexpected IllegalArgumentException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws ClassNotFoundException
     *            an unexpected ClassNotFoundException exception
     * @throws InstantiationException
     *            an unexpected InstantiationException exception
     * @throws IllegalAccessException
     *            an unexpected IllegalAccessException exception
     * @throws InvocationTargetException
     *            an unexpected InvocationTargetException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public EngineData getStoreEng(String storeId)
            throws IllegalArgumentException, KKAppException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException, KKException
    {
        if (engMap == null)
        {
            engMap = new HashMap<String, EngineData>();
        }
        EngineData engData = engMap.get(storeId);
        if (engData == null)
        {
            StoreInfo si = new StoreInfo();
            si.setStoreId(storeId);
            KKEngIf storeEng = getAServerEngineInstance(si);
            engData = new EngineData();
            engMap.put(storeId, engData);
            engData.setEng(storeEng);
            StoreIf store = storeEng.getStore();
            if (store != null)
            {
                engData.setStoreName(store.getStoreName());
            }
        }
        return engData;
    }

    /**
     * Returns true if the module referenced by the class name is a tax module
     * 
     * @param className
     *            the className
     * @return Returns true if the module referenced by the class name is a tax module
     */
    public boolean isTaxModule(String className)
    {
        if (className != null && className.length() > 5 && className.startsWith("ot_tax"))
        {
            return true;
        }
        return false;
    }

    /**
     * Helper method that returns true if a module provides a discount. Typically used for
     * formatting the order total in the UI.
     * 
     * @param module
     *            the module
     * @return Returns true if the module provides a discount
     */
    public boolean isDiscountModule(String module)
    {
        if (module != null && (module.equals("ot_product_discount")
                || module.equals("ot_total_discount") || module.equals("ot_total_discount_admin")
                || module.equals("ot_buy_x_get_y_free") || module.equals("ot_gift_certificate")
                || module.equals("ot_redeem_points") || module.equals("ot_shipping_discount")))
        {
            return true;
        }
        return false;
    }

    /**
     * Base URL set by JSP. e.g. http://localhost:8780/konakart/
     * 
     * @return the base
     */
    public String getBase()
    {
        return base;
    }

    /**
     * Base URL set by JSP. e.g. http://localhost:8780/konakart/
     * 
     * @param base
     *            the base to set
     */
    public void setBase(String base)
    {
        this.base = base;
    }

    /**
     * Set to true when the customer agrees to accept cookies
     * 
     * @return the agreedCookies
     */
    public boolean isAgreedCookies()
    {
        return agreedCookies;
    }

    /**
     * Set to true when the customer agrees to accept cookies
     * 
     * @param agreedCookies
     *            the agreedCookies to set
     */
    public void setAgreedCookies(boolean agreedCookies)
    {
        this.agreedCookies = agreedCookies;
    }

    /**
     * @return the xsrfToken
     */
    public String getXsrfToken()
    {
        return xsrfToken;
    }

    /**
     * @param xsrfToken
     *            the xsrfToken to set
     */
    public void setXsrfToken(String xsrfToken)
    {
        this.xsrfToken = xsrfToken;
    }

    /**
     * Used to store information related to checking the validity of the KonaKart session
     * 
     * @return the sessionCheckData
     */
    public SessionCheckData getSessionCheckData()
    {
        if (sessionCheckData == null)
        {
            sessionCheckData = new SessionCheckData();
        }
        return sessionCheckData;
    }

    /**
     * Used to store information related to checking the validity of the KonaKart session
     * 
     * @param sessionCheckData
     *            the sessionCheckData to set
     */
    public void setSessionCheckData(SessionCheckData sessionCheckData)
    {
        this.sessionCheckData = sessionCheckData;
    }

    /**
     * @return the contentImagesDir
     */
    public String getContentImagesDir()
    {
        if (contentImagesDir == null)
        {
            String contentDir = getProperty("content.images.dir");
            if (contentDir == null)
            {
                contentDir = "content";
            }

            contentImagesDir = contentDir;
        }
        return contentImagesDir;
    }

    /**
     * Used to store information related to checking the validity of the KonaKart session
     * 
     */
    public class SessionCheckData
    {
        private int CHECK_MILLIS = 10 * 60 * 1000;

        private int custId = 0;

        private long checkTime = 0;

        /**
         * Called when the customer logs off
         */
        public void logout()
        {
            custId = 0;
            checkTime = 0;
        }

        /**
         * Returns true if need to check the validity of the session on the server
         * 
         * @return Returns true if need to check the validity of the session on the server
         */
        public boolean needToCheck()
        {
            long currentTime = System.currentTimeMillis();
            if (currentTime - checkTime > CHECK_MILLIS)
            {
                return true;
            }
            return false;
        }

        /**
         * @return the custId
         */
        public int getCustId()
        {
            return custId;
        }

        /**
         * @param custId
         *            the custId to set
         */
        public void setCustId(int custId)
        {
            this.custId = custId;
        }

        /**
         * @return the checkTime
         */
        public long getCheckTime()
        {
            return checkTime;
        }

        /**
         * @param checkTime
         *            the checkTime to set
         */
        public void setCheckTime(long checkTime)
        {
            this.checkTime = checkTime;
        }
    }

    /**
     * Used to store engine information in the hash map
     * 
     */
    public class EngineData
    {
        private KKEngIf engine;

        private String storeName;

        /**
         * @return the engine
         */
        public KKEngIf getEng()
        {
            return engine;
        }

        /**
         * @param engine
         *            the engine to set
         */
        public void setEng(KKEngIf engine)
        {
            this.engine = engine;
        }

        /**
         * @return the storeName
         */
        public String getStoreName()
        {
            return storeName;
        }

        /**
         * @param storeName
         *            the storeName to set
         */
        public void setStoreName(String storeName)
        {
            this.storeName = storeName;
        }
    }

    /**
     * Used to store the static data of this manager
     */
    private class StaticData
    {
        // Map containing the configuration variables
        Map<String, String> configMap = new HashMap<String, String>();

        // Map containing the message resource bundles
        Map<String, ResourceBundle> bundleMap = new HashMap<String, ResourceBundle>();

        // Map containing the maps for each language containing DB messages resource bundles
        Map<String, HashMap<String, String>> dbMsgMap = new HashMap<String, HashMap<String, String>>();

        // storeId - also the key of this cached object
        String storeId;

        // Form Validation values
        int cityMinLength, companyMinLength;

        int firstNameMinLength, lastNameMinLength, passwordMinLength;

        int postCodeMinLength, stateMinLength, streetAddrMinLength;

        int streetAddr1MinLength, telephoneMinLength;

        // Maximum number of addresses in address book
        int maxAddressBookEntries;

        // Stock warn level
        int stockWarnLevel = 10;

        // Is wish list enabled
        boolean wishListEnabled = false;

        // Base path for the store front application
        String storeBase = "/konakart";

        // Base path for images in the store front application
        String imageBase = "/konakart/images";

        // Base path for scripts in the store front application
        String scriptBase = "/konakart/script";

        // Base path for style sheets in the store front application
        String styleBase = "/konakart/styles";

        // SSL redirection properties
        String sslPort = "443";

        String standardPort = "80";

        String sslBaseUrl;

        boolean enableSSL = false;

        boolean useDBMsgs = false;

        boolean useSolr = false;

        boolean getSpellingSuggestions = false;

        // Digital download base path
        String ddbasePath;

        // Digital download as an attachment
        boolean ddAsAttachment;

        // Default currency
        CurrencyIf defaultCurrency;

        // Currencies
        CurrencyIf[] currencies;

        /** Hash Map that contains the currencies */
        Map<String, CurrencyIf> currencyHM = Collections
                .synchronizedMap(new HashMap<String, CurrencyIf>());

        // Analytics Flag
        // String analyticsEnabled;

        // Default currency formatter
        DecimalFormat currencyFormatter;

        /** Hash Map that contains the ordered country lists for each locale */
        Map<String, CountryIf[]> countryListsHM = Collections
                .synchronizedMap(new HashMap<String, CountryIf[]>());

        /** Hash Map that contains a hash map of countries for each locale */
        Map<String, HashMap<Integer, CountryIf>> countryMapsHM = Collections
                .synchronizedMap(new HashMap<String, HashMap<Integer, CountryIf>>());

        // To solve race conditions at start-up. This is set when we've filled the caches for the
        // first time
        boolean cachesFilled = false;

        // Engine version in string format
        String versionStr = null;

        // Engine version in int format
        int versionInt = 0;

        /**
         * @return Returns the sslPort.
         */
        public String getSslPort()
        {
            return sslPort;
        }

        /**
         * @param sslPort
         *            The sslPort to set.
         */
        public void setSslPort(String sslPort)
        {
            this.sslPort = sslPort;
        }

        /**
         * @return Returns the standardPort.
         */
        public String getStandardPort()
        {
            return standardPort;
        }

        /**
         * @param standardPort
         *            The standardPort to set.
         */
        public void setStandardPort(String standardPort)
        {
            this.standardPort = standardPort;
        }

        /**
         * @return Returns the sslBaseUrl.
         */
        public String getSslBaseUrl()
        {
            return sslBaseUrl;
        }

        /**
         * @param sslBaseUrl
         *            The sslBaseUrl to set.
         */
        public void setSslBaseUrl(String sslBaseUrl)
        {
            this.sslBaseUrl = sslBaseUrl;
        }

        /**
         * @return Returns the enableSSL.
         */
        public boolean isEnableSSL()
        {
            return enableSSL;
        }

        /**
         * @param enableSSL
         *            The enableSSL to set.
         */
        public void setEnableSSL(boolean enableSSL)
        {
            this.enableSSL = enableSSL;
        }

        /**
         * @return Returns the ddbasePath.
         */
        public String getDdbasePath()
        {
            return ddbasePath;
        }

        /**
         * @param ddbasePath
         *            The ddbasePath to set.
         */
        public void setDdbasePath(String ddbasePath)
        {
            this.ddbasePath = ddbasePath;
        }

        /**
         * @return Returns the ddAsAttachment.
         */
        public boolean isDdAsAttachment()
        {
            return ddAsAttachment;
        }

        /**
         * @param ddAsAttachment
         *            The ddAsAttachment to set.
         */
        public void setDdAsAttachment(boolean ddAsAttachment)
        {
            this.ddAsAttachment = ddAsAttachment;
        }

        /**
         * @return Returns the defaultCurrency.
         */
        public CurrencyIf getDefaultCurrency()
        {
            return defaultCurrency;
        }

        /**
         * @param defaultCurrency
         *            The defaultCurrency to set.
         */
        public void setDefaultCurrency(CurrencyIf defaultCurrency)
        {
            this.defaultCurrency = defaultCurrency;
        }

        /**
         * @return Returns the currencyFormatter.
         */
        public DecimalFormat getCurrencyFormatter()
        {
            return currencyFormatter;
        }

        /**
         * @param currencyFormatter
         *            The currencyFormatter to set.
         */
        public void setCurrencyFormatter(DecimalFormat currencyFormatter)
        {
            this.currencyFormatter = currencyFormatter;
        }

        /**
         * @return Returns the configMap.
         */
        public Map<String, String> getConfigMap()
        {
            return configMap;
        }

        /**
         * @return the storeId
         */
        protected String getStoreId()
        {
            return storeId;
        }

        /**
         * @param storeId
         *            the storeId to set
         */
        protected void setStoreId(String storeId)
        {
            this.storeId = storeId;
        }

        /**
         * @return the currencies
         */
        public CurrencyIf[] getCurrencies()
        {
            return currencies;
        }

        /**
         * @param currencies
         *            the currencies to set
         */
        public void setCurrencies(CurrencyIf[] currencies)
        {
            this.currencies = currencies;
        }

        /**
         * @return the countryListsHM
         */
        public Map<String, CountryIf[]> getCountryListsHM()
        {
            return countryListsHM;
        }

        /**
         * @return the imageBase
         */
        public String getImageBase()
        {
            return imageBase;
        }

        /**
         * @param imageBase
         *            the imageBase to set
         */
        public void setImageBase(String imageBase)
        {
            this.imageBase = imageBase;
        }

        /**
         * @return the scriptBase
         */
        public String getScriptBase()
        {
            return scriptBase;
        }

        /**
         * @param scriptBase
         *            the scriptBase to set
         */
        public void setScriptBase(String scriptBase)
        {
            this.scriptBase = scriptBase;
        }

        /**
         * @return the styleBase
         */
        public String getStyleBase()
        {
            return styleBase;
        }

        /**
         * @param styleBase
         *            the styleBase to set
         */
        public void setStyleBase(String styleBase)
        {
            this.styleBase = styleBase;
        }

        /**
         * @return the storeBase
         */
        public String getStoreBase()
        {
            return storeBase;
        }

        /**
         * @param storeBase
         *            the storeBase to set
         */
        public void setStoreBase(String storeBase)
        {
            this.storeBase = storeBase;
        }

        /**
         * @return the countryMapsHM
         */
        public Map<String, HashMap<Integer, CountryIf>> getCountryMapsHM()
        {
            return countryMapsHM;
        }

        /**
         * @return the cityMinLength
         */
        public int getCityMinLength()
        {
            return cityMinLength;
        }

        /**
         * @param cityMinLength
         *            the cityMinLength to set
         */
        public void setCityMinLength(int cityMinLength)
        {
            this.cityMinLength = cityMinLength;
        }

        /**
         * @return the companyMinLength
         */
        public int getCompanyMinLength()
        {
            return companyMinLength;
        }

        /**
         * @param companyMinLength
         *            the companyMinLength to set
         */
        public void setCompanyMinLength(int companyMinLength)
        {
            this.companyMinLength = companyMinLength;
        }

        /**
         * @return the firstNameMinLength
         */
        public int getFirstNameMinLength()
        {
            return firstNameMinLength;
        }

        /**
         * @param firstNameMinLength
         *            the firstNameMinLength to set
         */
        public void setFirstNameMinLength(int firstNameMinLength)
        {
            this.firstNameMinLength = firstNameMinLength;
        }

        /**
         * @return the lastNameMinLength
         */
        public int getLastNameMinLength()
        {
            return lastNameMinLength;
        }

        /**
         * @param lastNameMinLength
         *            the lastNameMinLength to set
         */
        public void setLastNameMinLength(int lastNameMinLength)
        {
            this.lastNameMinLength = lastNameMinLength;
        }

        /**
         * @return the passwordMinLength
         */
        public int getPasswordMinLength()
        {
            return passwordMinLength;
        }

        /**
         * @param passwordMinLength
         *            the passwordMinLength to set
         */
        public void setPasswordMinLength(int passwordMinLength)
        {
            this.passwordMinLength = passwordMinLength;
        }

        /**
         * @return the postCodeMinLength
         */
        public int getPostCodeMinLength()
        {
            return postCodeMinLength;
        }

        /**
         * @param postCodeMinLength
         *            the postCodeMinLength to set
         */
        public void setPostCodeMinLength(int postCodeMinLength)
        {
            this.postCodeMinLength = postCodeMinLength;
        }

        /**
         * @return the stateMinLength
         */
        public int getStateMinLength()
        {
            return stateMinLength;
        }

        /**
         * @param stateMinLength
         *            the stateMinLength to set
         */
        public void setStateMinLength(int stateMinLength)
        {
            this.stateMinLength = stateMinLength;
        }

        /**
         * @return the streetAddrMinLength
         */
        public int getStreetAddrMinLength()
        {
            return streetAddrMinLength;
        }

        /**
         * @param streetAddrMinLength
         *            the streetAddrMinLength to set
         */
        public void setStreetAddrMinLength(int streetAddrMinLength)
        {
            this.streetAddrMinLength = streetAddrMinLength;
        }

        /**
         * @return the streetAddr1MinLength
         */
        public int getStreetAddr1MinLength()
        {
            return streetAddr1MinLength;
        }

        /**
         * @param streetAddr1MinLength
         *            the streetAddr1MinLength to set
         */
        public void setStreetAddr1MinLength(int streetAddr1MinLength)
        {
            this.streetAddr1MinLength = streetAddr1MinLength;
        }

        /**
         * @return the telephoneMinLength
         */
        public int getTelephoneMinLength()
        {
            return telephoneMinLength;
        }

        /**
         * @param telephoneMinLength
         *            the telephoneMinLength to set
         */
        public void setTelephoneMinLength(int telephoneMinLength)
        {
            this.telephoneMinLength = telephoneMinLength;
        }

        /**
         * @return the stockWarnLevel
         */
        public int getStockWarnLevel()
        {
            return stockWarnLevel;
        }

        /**
         * @param stockWarnLevel
         *            the stockWarnLevel to set
         */
        public void setStockWarnLevel(int stockWarnLevel)
        {
            this.stockWarnLevel = stockWarnLevel;
        }

        /**
         * @return the bundleMap
         */
        public Map<String, ResourceBundle> getBundleMap()
        {
            return bundleMap;
        }

        /**
         * @return the dbMsgMap
         */
        public Map<String, HashMap<String, String>> getDbMsgMap()
        {
            return dbMsgMap;
        }

        /**
         * @return the useDBMsgs
         */
        public boolean isUseDBMsgs()
        {
            return useDBMsgs;
        }

        /**
         * @param useDBMsgs
         *            the useDBMsgs to set
         */
        public void setUseDBMsgs(boolean useDBMsgs)
        {
            this.useDBMsgs = useDBMsgs;
        }

        /**
         * @return the cachesFilled
         */
        public boolean isCachesFilled()
        {
            return cachesFilled;
        }

        /**
         * @param cachesFilled
         *            the cachesFilled to set
         */
        public void setCachesFilled(boolean cachesFilled)
        {
            this.cachesFilled = cachesFilled;
        }

        /**
         * @return the useSolr
         */
        public boolean isUseSolr()
        {
            return useSolr;
        }

        /**
         * @param useSolr
         *            the useSolr to set
         */
        public void setUseSolr(boolean useSolr)
        {
            this.useSolr = useSolr;
        }

        /**
         * @return the maxAddressBookEntries
         */
        public int getMaxAddressBookEntries()
        {
            return maxAddressBookEntries;
        }

        /**
         * @param maxAddressBookEntries
         *            the maxAddressBookEntries to set
         */
        public void setMaxAddressBookEntries(int maxAddressBookEntries)
        {
            this.maxAddressBookEntries = maxAddressBookEntries;
        }

        /**
         * @return the wishListEnabled
         */
        public boolean isWishListEnabled()
        {
            return wishListEnabled;
        }

        /**
         * @param wishListEnabled
         *            the wishListEnabled to set
         */
        public void setWishListEnabled(boolean wishListEnabled)
        {
            this.wishListEnabled = wishListEnabled;
        }

        /**
         * @return the getSpellingSuggestions
         */
        public boolean isGetSpellingSuggestions()
        {
            return getSpellingSuggestions;
        }

        /**
         * @param getSpellingSuggestions
         *            the getSpellingSuggestions to set
         */
        public void setGetSpellingSuggestions(boolean getSpellingSuggestions)
        {
            this.getSpellingSuggestions = getSpellingSuggestions;
        }

        /**
         * @return the versionStr
         */
        public String getVersionStr()
        {
            return versionStr;
        }

        /**
         * @param versionStr
         *            the versionStr to set
         */
        public void setVersionStr(String versionStr)
        {
            this.versionStr = versionStr;
        }

        /**
         * @return the versionInt
         */
        public int getVersionInt()
        {
            return versionInt;
        }

        /**
         * @param versionInt
         *            the versionInt to set
         */
        public void setVersionInt(int versionInt)
        {
            this.versionInt = versionInt;
        }

        /**
         * @return the currencyHM
         */
        public Map<String, CurrencyIf> getCurrencyHM()
        {
            return currencyHM;
        }
    }

    /**
     * An options object that could for example point this customer to a special catalog. getImages
     * set to false.
     * 
     * @return the fetchProdOptionsNoImages
     */
    public FetchProductOptionsIf getFetchProdOptionsNoImages()
    {
        return fetchProdOptionsNoImages;
    }

    /**
     * An options object that could for example point this customer to a special catalog. getImages
     * set to false.
     * 
     * @param fetchProdOptionsNoImages
     *            the fetchProdOptionsNoImages to set
     */
    public void setFetchProdOptionsNoImages(FetchProductOptionsIf fetchProdOptionsNoImages)
    {
        this.fetchProdOptionsNoImages = fetchProdOptionsNoImages;
    }

    /**
     * Valid values are null, CustomerMgr.FACEBOOK, CustomerMgr.PAYPAL and CustomerMgr.GOOGLEPLUS
     * 
     * @return the loginType
     */
    public String getLoginType()
    {
        return loginType;
    }

    /**
     * Valid values are null, CustomerMgr.FACEBOOK, CustomerMgr.PAYPAL and CustomerMgr.GOOGLEPLUS
     * 
     * @param loginType
     *            the loginType to set
     */
    public void setLoginType(String loginType)
    {
        this.loginType = loginType;
    }

    /**
     * @return the fetchProdOptionsDefault
     */
    public FetchProductOptionsIf getFetchProdOptionsDefault()
    {
        return fetchProdOptionsDefault;
    }

    /**
     * @param fetchProdOptionsDefault
     *            the fetchProdOptionsDefault to set
     */
    public void setFetchProdOptionsDefault(FetchProductOptionsIf fetchProdOptionsDefault)
    {
        FetchProductOptionsIf fpo = new KKBeanCopier()
                .cloneFetchProductOptions(fetchProdOptionsDefault);
        this.fetchProdOptionsDefault = fpo;
    }

    /**
     * @return the liferaySSO
     */
    public SingleSignOnIf getLiferaySSO()
    {
        if (liferaySSO == null)
        {
            if (liferaySSOInstantiateAttempts++ > 0)
            {
                return null;
            }

            String moduleClassName = getConfig("MODULE_OTHER_LIFERAY_LOGIN_CLASS");
            if (moduleClassName == null || moduleClassName.length() == 0)
            {
                return null;
            }

            // Attempt to instantiate the SSO class
            try
            {
                Class<?> ssoClass = Class.forName("com.konakart.liferay.SingleSignOn");
                liferaySSO = (SingleSignOnIf) ssoClass.newInstance();
            } catch (Exception e)
            {
                log.debug("Could not instantiate com.konakart.liferay.SingleSignOn");
            }

        }
        return liferaySSO;
    }

    /**
     * @return the metaList
     */
    public ArrayList<String> getMetaList()
    {
        return metaList;
    }

    /**
     * @param metaList
     *            the metaList to set
     */
    public void setMetaList(ArrayList<String> metaList)
    {
        this.metaList = metaList;
    }

    /**
     * @return the searchParentCategoryId
     */
    public int getSearchParentCategoryId()
    {
        return searchParentCategoryId;
    }

    /**
     * @param searchParentCategoryId
     *            the searchParentCategoryId to set
     */
    public void setSearchParentCategoryId(int searchParentCategoryId)
    {
        this.searchParentCategoryId = searchParentCategoryId;
    }

    /**
     * Removes single quotes from the string by substituting them with double quotes. Currently used
     * to prepare strings to send to Google Analytics because a string like De'Longhi for product
     * name causes an exception in the JS on the browser. Substituting with &quot; doesn't really
     * work because Google shows &quot; in the dashboard. Also unicode or escaping doesn't work
     * because the JS still sees it as a single quote.
     * 
     * @param in
     *            the in
     * @return Returns the string with single quotes replaced by double quotes
     */
    public String removeSingleQuotes(String in)
    {
        if (in == null)
        {
            return "";

        }
        return in.replaceAll("'", "\"");
    }

    /**
     * @return the adminEng
     * 
     * @throws Exception an unexpected exception
     */
    public KKAdminIf getAdminEng() throws Exception
    {
        if (adminEng == null)
        {
            adminEng = getAnAdminEngineInstance();
        }
        return adminEng;
    }

    /**
     * @param adminEng
     *            the adminEng to set
     */
    public void setAdminEng(KKAdminIf adminEng)
    {
        this.adminEng = adminEng;
    }

}
