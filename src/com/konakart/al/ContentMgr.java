//
// (c) 2015 DS Data Systems UK Ltd, All rights reserved.
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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.ContentSearch;
import com.konakart.app.DataDescriptor;
import com.konakart.app.KKException;
import com.konakart.appif.ContentIf;
import com.konakart.appif.ContentSearchIf;
import com.konakart.appif.ContentSearchResultIf;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.util.KKConstants;

/**
 * Contains methods to manage the Content.
 */
public class ContentMgr extends BaseMgr
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(ContentMgr.class);

    // Content Map containing the static content for logged in users
    private HashMap<String, ContentIf[]> userContentMap = new HashMap<String, ContentIf[]>();

    // Hash Map that contains the static content for all users not logged in
    private static Map<String, ContentIf[]> staticContentMap = Collections
            .synchronizedMap(new HashMap<String, ContentIf[]>());

    // Time cache should have been refreshed
    private static Date refreshTime;

    // Time cache was last refreshed
    private Date lastRefreshTime;

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected ContentMgr(KKEngIf eng, KKAppEng kkAppEng) throws KKException, KKAppException
    {
        this.eng = eng;
        this.kkAppEng = kkAppEng;
    }

    /**
     * Clear the content map. It clears the static map and sets a static date variable which
     * instructs the non static user specific content maps to be cleared.
     * 
     */
    public void refreshConfigs()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Refresh static content map for ContentMgr");
        }
        staticContentMap.clear();
        refreshTime = new Date();
    }

    /**
     * Refreshes the non static content map containing user specific content.
     */
    public void refreshCaches()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Refresh local content map for ContentMgr");
        }
        userContentMap.clear();
        lastRefreshTime = new Date();
    }

    /**
     * Determines whether we need to refresh the content cache
     * 
     * @return Returns true if we should refresh the cache
     */
    private boolean needToRefresh()
    {
        if (refreshTime == null)
        {
            return false;
        }
        if (lastRefreshTime == null)
        {
            return true;
        }
        if (lastRefreshTime.before(refreshTime))
        {
            return true;
        }
        return false;
    }

    /**
     * Based on a configuration variable decides whether content enabled. It returns true if
     * enabled. Otherwise it returns false.
     * 
     * @return Returns true if content is enabled
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public boolean isEnabled() throws KKAppException
    {
        String enabled = kkAppEng.getConfig(ConfigConstants.ENABLE_KONAKART_CONTENT);
        if (enabled != null && enabled.equalsIgnoreCase("true"))
        {
            return true;
        }
        return false;
    }

    /**
     * Based on a configuration variable decides whether content caching is enabled. It returns true
     * if content should be cached otherwise false.
     * 
     * @return Returns true if content should be cached
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public boolean isCacheContent() throws KKAppException
    {
        String caching = kkAppEng.getConfig(ConfigConstants.CACHE_CONTENT);
        if (caching != null && caching.equalsIgnoreCase("true"))
        {
            return true;
        }
        return false;
    }

    /**
     * Get Content by Type and Key
     * 
     * @param maxItems
     *            maximum number of items of the specified contentType to return. If your content
     *            has expressions defined you may not get maxItems content records back because some
     *            may not pass the expression tests. In these cases you should add a higher maxItems
     *            value to ensure you always retrieve the required number of content items. (The
     *            problem is that the expressions are executed on the maxItems records returned from
     *            the database so if any of those fail the expression tests the number returned will
     *            be reduced by the number that fail the tests.
     * @param contentTypeId
     *            Content type id identifying the content
     * @param searchKey
     *            Search key identifying the content
     * @return A number of content items matching the type id and searchKey
     */
    public ContentIf[] getContentForTypeAndKey(int maxItems, int contentTypeId, String searchKey)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Calling getContentForTypeAndKey with key =  " + searchKey + " and type = "
                    + contentTypeId);
        }
        ContentSearchIf search = getContentSearch();
        search.setSearchKey(searchKey);
        search.setContentTypeId(contentTypeId);
        return getContent(maxItems, search);
    }

    /**
     * Get Content by Id
     * 
     * @param maxItems
     *            maximum number of items of the specified contentType to return. If your content
     *            has expressions defined you may not get maxItems content records back because some
     *            may not pass the expression tests. In these cases you should add a higher maxItems
     *            value to ensure you always retrieve the required number of content items. (The
     *            problem is that the expressions are executed on the maxItems records returned from
     *            the database so if any of those fail the expression tests the number returned will
     *            be reduced by the number that fail the tests.
     * @param contentId
     *            Content id identifying the content
     * @return A number of content items matching the contentId
     */
    public ContentIf[] getContentForId(int maxItems, int contentId)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Calling getContentForId with id =  " + contentId);
        }

        ContentSearchIf search = getContentSearch();
        search.setContentId(contentId);
        return getContent(maxItems, search);
    }

    /**
     * Get Content by Type
     * 
     * @param maxItems
     *            maximum number of items of the specified contentType to return. If your content
     *            has expressions defined you may not get maxItems content records back because some
     *            may not pass the expression tests. In these cases you should add a higher maxItems
     *            value to ensure you always retrieve the required number of content items. (The
     *            problem is that the expressions are executed on the maxItems records returned from
     *            the database so if any of those fail the expression tests the number returned will
     *            be reduced by the number that fail the tests.
     * @param contentTypeId
     *            Content type id identifying the content
     * @return A number of content items matching the contentTypeId
     */
    public ContentIf[] getContentForType(int maxItems, int contentTypeId)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Calling getContentForType with id =  " + contentTypeId);
        }

        ContentSearchIf search = getContentSearch();
        search.setContentTypeId(contentTypeId);
        return getContent(maxItems, search);
    }

    /**
     * @param maxItems
     *            maximum number of items of the specified contentType to return. If your content
     *            has expressions defined you may not get maxItems content records back because some
     *            may not pass the expression tests. In these cases you should add a higher maxItems
     *            value to ensure you always retrieve the required number of content items. (The
     *            problem is that the expressions are executed on the maxItems records returned from
     *            the database so if any of those fail the expression tests the number returned will
     *            be reduced by the number that fail the tests.
     * @param search
     *            Content search object containing constraints to search for the content
     * @return A number of content items matching the ContentSearch object
     */
    public ContentIf[] getContent(int maxItems, ContentSearchIf search)
    {
        if (search == null)
        {
            return null;
        }

        boolean isLoggedIn = (kkAppEng.getSessionId() == null) ? false : true;
        ContentIf[] content = null;
        String contentMapKey = null;
        boolean cacheContent;
        try
        {
            cacheContent = isCacheContent();
        } catch (KKAppException kkae)
        {
            // Very unexpected at this stage
            kkae.printStackTrace();
            cacheContent=false;
        }

        if (cacheContent)
        {
            if (log.isDebugEnabled())
            {
                if (!isLoggedIn)
                {
                    log.debug("Using static content map");
                } else
                {
                    log.debug("Using customer instance of content map");
                }
            }

            contentMapKey = getContentMapKey(search);

            if (isLoggedIn)
            {
                if (needToRefresh())
                {
                    refreshCaches();
                } else
                {
                    content = userContentMap.get(contentMapKey);
                }
            } else
            {
                content = staticContentMap.get(contentMapKey);
            }
        }

        try
        {
            if (content == null)
            {
                DataDescriptorIf dd = new DataDescriptor();
                dd.setLimit(maxItems);
                dd.setOffset(0);

                ContentSearchResultIf result = eng.getContents(kkAppEng.getSessionId(), search, dd);

                if (result != null)
                {
                    content = result.getContents();
                    if (cacheContent && contentMapKey != null)
                    {
                        if (isLoggedIn)
                        {
                            userContentMap.put(contentMapKey, content);
                        } else
                        {
                            staticContentMap.put(contentMapKey, content);
                        }
                    }
                    if (log.isDebugEnabled())
                    {
                        log.debug(content.length + " Content items found in database");
                    }
                }
            } else
            {
                if (log.isDebugEnabled())
                {
                    log.debug(content.length + " Content items found in cache");
                }
            }
        } catch (KKException e)
        {
            // No attempt to recover
            e.printStackTrace();
        }

        if (log.isDebugEnabled())
        {
            if (content != null)
            {
                String retStr = "Returning:";
                for (ContentIf cont : content)
                {
                    retStr += "\n\t\t\t\t" + cont.getContentId() + ") ";
                    if (cont.getDescription() != null && cont.getDescription().getName1() != null)
                    {
                        retStr += cont.getDescription().getName1() + " ";
                    }
                    if (cont.getClickUrl() != null)
                    {
                        retStr += cont.getClickUrl();
                    }
                }
                log.debug(retStr);
            } else
            {
                log.debug("Returning null content");
            }
        }

        return content;
    }

    /**
     * @param search
     * @return Returns a key for the content map
     */
    private String getContentMapKey(ContentSearchIf search)
    {
        int engLanguage = kkAppEng.getLangId();
        String contentId = (search.getContentId() > -1) ? Integer.toString(search.getContentId())
                : "";
        String contentTypeId = (search.getContentTypeId() > -1) ? Integer.toString(search
                .getContentTypeId()) : "";
        String searchKey = (search.getSearchKey() != null) ? search.getSearchKey() : "";
        return contentId + "~" + contentTypeId + "~" + searchKey + "~" + engLanguage + "~"
                + kkAppEng.getStoreId();
    }

    /**
     * @return Returns a partially filled content search object
     */
    private ContentSearchIf getContentSearch()
    {
        ContentSearchIf search = new ContentSearch();
        search.setEvaluateExpressions(true);
        search.setRetrieveDescriptions(true);
        search.setEnabled(true);
        search.setSearchKeyRule(KKConstants.SEARCH_EXACT);
        search.setLanguageId(kkAppEng.getLangId());
        return search;
    }
}
