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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.KKException;
import com.konakart.appif.KKConfigurationIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.util.ExceptionUtils;
import com.konakart.util.KKConstants;

/**
 * Thread which runs forever. At a regular interval it checks to see whether the cache of
 * configuration variables needs updating and updates it if required.
 */
public class ConfigCacheUpdater extends Thread
{
    /**
     * The <code>Log</code> instance for this class.
     */
    private Log log = LogFactory.getLog(ConfigCacheUpdater.class);

    /**
     * Default sleep time in millis (30 secs)
     */
    private long SLEEP_MILLIS_DEFAULT = 30 * 1000L;

    /**
     * Sleep time in millis
     */
    private long sleepMillis = SLEEP_MILLIS_DEFAULT;

    /**
     * Last time the config variable was modified
     */
    private Calendar lastModified = null;

    /*
     * Variable to decide whether to keep looping
     */
    private boolean loop = true;

    /**
     * Client Engine Instance
     */
    private KKAppEng clientEng = null;

    /**
     * Master Client Engine Instance. The instance that starts this thread.
     */
    private KKAppEng masterClientEng = null;

    /**
     * Configuration parameter passed when the engine is instantiated to determine which store the
     * engine is for
     */
    private StoreInfo storeInfo = null;

    /**
     * The storeId as a string used for reporting. Set to "Default store" if
     * storeInfo().getStoreId() == null
     */
    private String storeIdStr = null;

    /**
     * Constructor
     * 
     * @param masterClientEng
     *            The AppEng that started the thread
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * 
     */
    public ConfigCacheUpdater(KKAppEng masterClientEng) throws KKAppException, KKException
    {
        if (masterClientEng == null)
        {
            throw new KKAppException("The thread must be passed a non null KKAppEng object");
        }
        this.masterClientEng = masterClientEng;
    }

    /**
     * @return Returns an instant of the client engine
     * @throws KKAppException
     * @throws KKException
     */
    private KKAppEng getClientEng() throws KKAppException, KKException
    {
        if (clientEng == null)
        {
            clientEng = new KKAppEng(storeInfo, null);
            clientEng.refreshCachedData();
        }
        return clientEng;
    }

    /**
     * Set the interval for checking the configuration flag
     * 
     * @throws KKException
     * @throws KKAppException
     */
    private void setCheckInterval() throws KKAppException, KKException
    {
        int checkIntervalSecs = getClientEng().getConfigAsInt(
                ConfigConstants.CLIENT_CONFIG_CACHE_CHECK_SECS);

        if (checkIntervalSecs != KKConstants.NOT_SET)
        {
            sleepMillis = checkIntervalSecs * 1000L;
        } else
        {
            log.warn("Warning: The variable CLIENT_CONFIG_CACHE_CHECK_SECS has not been set "
                    + " so the default value of " + sleepMillis + "ms will be used.");
        }

        if (log.isDebugEnabled())
        {
            log.debug("ConfigCacheUpdater Sleep Time in Millis = " + sleepMillis);
        }
    }

    /**
     * Stop the current thread from looping
     */
    public void stopLooping()
    {
        loop = false;
    }

    /**
     * This thread runs forever. At a regular interval it checks to see whether the cache of
     * configuration variables needs updating and updates it if required.
     */
    public void run()
    {
        if (storeInfo == null)
        {
            log.error("ConfigCacheUpdater thread has been started without using the setStoreInfo()"
                    + " method to set a valid StoreInfo before starting the thread");
            return;
        }

        // This string is used for reporting purposes
        storeIdStr = (storeInfo.getStoreId() == null) ? "Default Store" : storeInfo.getStoreId();

        try
        {
            // Set the interval for checking the configuration flag (sleepMillis)
            setCheckInterval();

            // Initialize the last modified variable
            initLastModified();
        } catch (KKAppException e2)
        {
            if (e2.getThrowables() != null)
            {
                for (int i = 0; i < e2.getThrowables().length; i++)
                {
                    Throwable t = e2.getThrowables()[i];
                    if (t.getClass().getName().equals("com.konakart.app.KKException"))
                    {
                        if (handleException1((KKException) (t)) == false)
                        {
                            break;
                        }
                        return;
                    }
                }
            }
            log.error(ExceptionUtils.exceptionToString(e2));
        } catch (KKException e1)
        {
            if (handleException1(e1) == false)
            {
                log.error(ExceptionUtils.exceptionToString(e1));
            } else
            {
                return;
            }
        } catch (Exception e)
        {
            log.error(ExceptionUtils.exceptionToString(e));
        }

        if (log.isInfoEnabled())
        {
            log.info("Started a ConfigCacheUpdater thread for store id " + storeIdStr);
        }

        while (loop)
        {
            try
            {
                Thread.sleep(sleepMillis);

                if (updateConfigs())
                {
                    if (log.isInfoEnabled())
                    {
                        log.info("Refreshing Config Variables for storeId "
                                + getClientEng().getStoreId());
                    }

                    // Get the new configuration values for the client which are stored in a hash
                    // map
                    getClientEng().refreshCachedData();

                    // The check interval may have changed. The following method may change the
                    // value of "sleepMillis"
                    setCheckInterval();
                }

            } catch (InterruptedException e1)
            {
                loop = false;
                log.info("Stopping ConfigCacheUpdater thread for storeId " + storeIdStr);
            } catch (KKException e2)
            {
                handleException(e2);
            } catch (KKAppException e3)
            {
                boolean foundException = false;
                if (e3.getThrowables() != null)
                {
                    for (int i = 0; i < e3.getThrowables().length; i++)
                    {
                        Throwable t = e3.getThrowables()[i];
                        if (t.getClass().getName().equals("com.konakart.app.KKException"))
                        {
                            handleException((KKException) (t));
                            foundException = true;
                            break;
                        }
                    }
                }
                if (!foundException)
                {
                    KKAppException e1 = new KKAppException(
                            "There has been an exception in the ConfigCacheUpdater", e3);
                    log.error(ExceptionUtils.exceptionToString(e1));
                }
            } catch (Exception e)
            {
                KKAppException e1 = new KKAppException(
                        "There has been an exception in the ConfigCacheUpdater", e);
                log.error(ExceptionUtils.exceptionToString(e1));
            }
        }
    }

    /**
     * Handles an exception before entering the endless loop.
     * 
     * @param e
     * @return Returns true if the exception was caused by a store not being available
     */
    private boolean handleException1(KKException e)
    {
        boolean found = false;
        if (e.getCode() == KKException.KK_STORE_NOT_FOUND)
        {
            log.warn("Not starting Config Cache Updater for store " + storeIdStr
                    + " because it cannot be found.");
            found = true;
        }
        if (e.getCode() == KKException.KK_STORE_DELETED)
        {
            log.warn("Not starting Config Cache Updater for store " + storeIdStr
                    + " because it has been deleted.");
            found = true;
        }
        if (e.getCode() == KKException.KK_STORE_DISABLED)
        {
            log.warn("Not starting Config Cache Updater for store " + storeIdStr
                    + " because it has been disabled");
            found = true;
        }
        if (e.getCode() == KKException.KK_STORE_UNDER_MAINTENANCE)
        {
            log.warn("Not starting Config Cache Updater for store " + storeIdStr
                    + " because it is under maintenance.");
            found = true;
        }

        if (found)
        {
            masterClientEng.removeFromUpdateThreadList(storeInfo.getStoreId());
        }
        return found;
    }

    /**
     * Handles an exception after entering the endless loop
     * 
     * @param e
     */
    private void handleException(KKException e)
    {
        if (e.getCode() == KKException.KK_STORE_DELETED)
        {
            loop = false;
            log.warn("Stopping ConfigCacheUpdater thread for storeId " + storeIdStr
                    + " since this store has been deleted");

            masterClientEng.removeFromUpdateThreadList(storeInfo.getStoreId());

        } else if (e.getCode() == KKException.KK_STORE_DISABLED
                || e.getCode() == KKException.KK_STORE_NOT_FOUND
                || e.getCode() == KKException.KK_STORE_UNDER_MAINTENANCE)
        {
            log.debug("Config Cache Updater threw exception for storeId " + storeIdStr
                    + ". The KKException code was " + e.getCode());
        } else
        {
            KKAppException appE = new KKAppException(
                    "There has been an exception in the ConfigCacheUpdater", e);

            // We get very occassional deadlock exceptions under MS SQL and DB2 - not a serious
            // "ERROR" so we just report it here and hope the next time it tries the transaction
            // there will be no deadlock.
            if (e.getMessage().contains("deadlocked") && e.getMessage().contains("Rerun")
                    || e.getMessage().contains("DB2 SQL Error: SQLCODE=-911, SQLSTATE=40001"))
            {
                log.warn("Deadlock detected; Config Cache Update will be re-tried");
                if (log.isInfoEnabled())
                {
                    log.info(ExceptionUtils.exceptionToString(appE));
                }
            } else
            {
                log.error(ExceptionUtils.exceptionToString(appE));
            }
        }
    }

    /**
     * Reads a config variable and decides whether or not to do the update based on whether the
     * config variable was modified since last time it was read. No updates are executed if the
     * store is not found, deleted, under maintenance, or disabled.
     * 
     * @return Returns a boolean
     * @throws KKException
     * @throws KKAppException
     */
    private boolean updateConfigs() throws KKException, KKAppException
    {
        KKConfigurationIf ret;

        ret = getClientEng().getEng().getConfigurationNoCache(
                ConfigConstants.CLIENT_CONFIG_CACHE_CHECK_FLAG);

        if (ret == null)
        {
            throw new KKException(
                    "Not able to find the CLIENT_CONFIG_CACHE_CHECK_FLAG configuration variable which is compulsory.");
        }

        if (ret.getLastModified() == null)
        {
            return false;
        }

        if (lastModified == null)
        {
            // Should never be set to null since initLastModified() will have been called
            lastModified = ret.getLastModified();
            return true;
        } else if (lastModified.compareTo(ret.getLastModified()) != 0)
        {
            lastModified = ret.getLastModified();
            return true;
        }

        return false;
    }

    /**
     * Initializes the lastModified variable at startup
     * 
     * @throws KKException
     * @throws KKAppException
     */
    private void initLastModified() throws KKException, KKAppException
    {
        KKConfigurationIf ret = getClientEng().getEng().getConfigurationNoCache(
                ConfigConstants.CLIENT_CONFIG_CACHE_CHECK_FLAG);

        if (ret == null)
        {
            throw new KKException(
                    "Not able to find the CLIENT_CONFIG_CACHE_CHECK_FLAG configuration variable which is compulsory.");
        }

        if (ret.getLastModified() == null)
        {
            // Can occur for a new database
            lastModified = new GregorianCalendar();
        } else
        {
            lastModified = ret.getLastModified();
        }
    }

    /**
     * @return Returns the storeInfo.
     */
    public StoreInfo getStoreInfo()
    {
        return storeInfo;
    }

    /**
     * @param storeInfo
     *            The storeInfo to set.
     */
    public void setStoreInfo(StoreInfo storeInfo)
    {
        this.storeInfo = storeInfo;
    }

    /**
     * @param clientEng
     *            the clientEng to set
     */
    public void setClientEng(KKAppEng clientEng)
    {
        this.clientEng = clientEng;
    }

}
