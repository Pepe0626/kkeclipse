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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.konakart.app.DataDescriptor;
import com.konakart.app.FetchRewardPointOptions;
import com.konakart.app.KKException;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.appif.FetchRewardPointOptionsIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.RewardPointIf;
import com.konakart.appif.RewardPointsIf;
import com.konakart.bl.ConfigConstants;

/**
 * Contains methods to manage Reward Points
 */
public class RewardPointMgr extends BaseMgr
{
    // Result set navigation constants
    private static final String navNext = "next";

    private static final String navBack = "back";

    private static final String navStart = "start";

    // Data descriptor info
    private DataDescriptorIf dataDesc = new DataDescriptor();

    private int currentOffset;

    private int currentPage;

    private int showNext;

    private int showBack;

    // Total number of pages in a result set
    private int numPages = 0;

    private ArrayList<Integer> pageList = new ArrayList<Integer>();

    /** The current reward point array */
    private RewardPointIf[] currentRewardPoints;

    /** Empty reward point array */
    private final RewardPointIf[] emptyRewardPointArray = new RewardPointIf[0];

    /** The total number of reward points */
    private int totalNumberOfRewardPoints;

    /** The object containing all of the static data for this instance of the RewardPoint Manager */
    private StaticData sd = null;

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected RewardPointMgr(KKEngIf eng, KKAppEng kkAppEng) throws KKException
    {
        this.eng = eng;
        this.kkAppEng = kkAppEng;
        sd = staticDataHM.get(kkAppEng.getStoreId());
        if (sd == null)
        {
            sd = new StaticData();
            staticDataHM.put(kkAppEng.getStoreId(), sd);
        }
        this.reset();
    }

    /**
     * Puts the RewardPointMgr object back into it's original state
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * 
     */
    public void reset() throws KKException
    {
        currentRewardPoints = emptyRewardPointArray;
        currentOffset = 0;
        showNext = 0;
        showBack = 0;
        this.initDataDesc();
    }

    /**
     * Returns the number of reward points available to the customer. Uses a cached version if
     * subsequent calls are in close succession.
     * 
     * @return Returns the number of reward points available to the customer
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int pointsAvailable() throws KKException, KKAppException
    {
        int REFRESH_MILLIS = 5000;

        checkEnabled();
        if (kkAppEng.getSessionId() == null)
        {
            return 0;
        }
        long time = System.currentTimeMillis();
        if (time - sd.getPointsTime() < REFRESH_MILLIS)
        {
            return sd.getPoints();
        }
        int points;
        try
        {
            points = eng.pointsAvailable(kkAppEng.getSessionId());
        } catch (Exception e)
        {
            // If not logged in, return 0
            return 0;
        }
        sd.setPoints(points);
        sd.setPointsTime(time);
        return points;
    }

    /**
     * Adds a number of reward points to the customer's total
     * 
     * @param points
     *            the points
     * @param code
     *            An optional code to categorize the reason for awarding the points
     * @param description
     *            An optional description describing why the points were awarded
     * @return Returns the new number of points available for spending
     * @throws Exception an unexpected exception
     */
    public int addPoints(int points, String code, String description) throws Exception
    {
        checkEnabled();
        if (kkAppEng.getSessionId() == null)
        {
            return 0;
        }

        int newPoints;
        try
        {
            newPoints = eng.addPoints(kkAppEng.getSessionId(), points, code, description);
        } catch (Exception e)
        {
            // If not logged in, return 0
            return 0;
        }
        sd.setPoints(newPoints);
        sd.setPointsTime(System.currentTimeMillis());
        return newPoints;
    }

    /**
     * Refresh the configuration variables. This is called automatically at a regular interval.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * 
     */
    public void refreshConfigs() throws KKException
    {
        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_REWARD_POINTS) != null)
        {
            sd.setMaxRows(new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_REWARD_POINTS))
                    .intValue());
        } else
        {
            sd.setMaxRows(20);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_PAGE_LINKS) != null)
        {
            sd.setMaxPageLinks(new Integer(kkAppEng
                    .getConfig(ConfigConstants.MAX_DISPLAY_PAGE_LINKS)).intValue());
        } else
        {
            sd.setMaxPageLinks(5);
        }
    }

    /**
     * This method is called to navigate through a list of reward point records when the list is
     * longer than maxRows.<br>
     * <code>navDir</code> can take the following values which are retrieved using getter methods on
     * the RewardPointMgr instance:
     * <ul>
     * <li>getNavNext()</li>
     * <li>getNavBack()</li>
     * <li>getNavStart()</li>
     * </ul>
     * 
     * @param navDir
     *            The navigation direction
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void navigateCurrentRewardPoints(String navDir) throws KKException, KKAppException
    {
        setDataDescOffset(navDir);

        getRewardPoints();
    }

    /**
     * Based on the action we are being asked to perform and the current offset, we set the new
     * offset before going to the engine to ask for more reward points.
     * 
     * @param action
     * @throws KKAppException
     */
    private void setDataDescOffset(String action) throws KKAppException
    {
        // Determine whether we've passed in a page number
        int requestedPage = -1;
        try
        {
            requestedPage = Integer.parseInt(action);
        } catch (NumberFormatException e)
        {
        }

        if (action.equals(navStart))
        {
            dataDesc.setOffset(0);
            currentOffset = 0;
            currentPage = 1;
        } else if (action.equals(navNext))
        {
            currentOffset += sd.getMaxRows();
            dataDesc.setOffset(currentOffset);
            currentPage = (currentOffset / sd.getMaxRows()) + 1;
        } else if (action.equals(navBack))
        {
            currentOffset -= sd.getMaxRows();
            if (currentOffset < 0)
            {
                currentOffset = 0;
            }
            dataDesc.setOffset(currentOffset);
            currentPage = (currentOffset / sd.getMaxRows()) + 1;
        } else if (requestedPage > 0)
        {
            currentOffset = sd.getMaxRows() * (requestedPage - 1);
            dataDesc.setOffset(currentOffset);
            currentPage = requestedPage;
        } else if (requestedPage <= 0)
        {
            currentOffset = 0;
            dataDesc.setOffset(currentOffset);
            currentPage = 1;
        } else
        {
            throw new KKAppException(
                    "The navigation direction parameter has an unrecognised value of " + action);
        }
    }

    /**
     * It gets an array of reward point transactions
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void getRewardPoints() throws KKException, KKAppException
    {
        RewardPointsIf rewardPoints = eng.getRewardPointsWithOptions(kkAppEng.getSessionId(), dataDesc, getFetchRewardPointOptions());

        if (rewardPoints != null)
        {
            currentRewardPoints = rewardPoints.getRewardPointArray();
            totalNumberOfRewardPoints = rewardPoints.getTotalNumRecords();
        }

        pageList = getPages( /* currentPage */
        currentPage);

        if (currentRewardPoints != null)
        {
            // We always attempt to fetch back maxRows + 1
            if (currentRewardPoints.length > sd.getMaxRows())
            {
                this.showNext = 1;
            } else
            {
                this.showNext = 0;
            }
        }

        if (currentOffset > 0)
        {
            this.showBack = 1;
        } else
        {
            this.showBack = 0;
        }
    }

    /**
     * Initialize the data descriptor object with a zero offset and max rows set.
     * 
     */
    private void initDataDesc()
    {
        // We always get an extra row in order to determine whether to show the
        // next link
        dataDesc.setLimit(sd.getMaxRows() + 1);
        dataDesc.setOffset(0);
    }

    /**
     * Based on a configuration variable decides whether reward points are enabled. Throws an
     * exception if not enabled.
     * 
     * @throws KKAppException
     * 
     */
    private void checkEnabled() throws KKAppException
    {
        if (isEnabled())
        {
            return;
        }
        throw new KKAppException(
                "Reward point functionality is not enabled. It can be enabled by setting the ENABLE_REWARD_POINTS config variables to true.");
    }

    /**
     * Returns the number of reward point records currently retrieved.
     * 
     * @return Returns the number of reward point records currently retrieved.
     */
    public int getNumberOfRewardPoints()
    {
        // We attempt to fetch 1 more record than the number in maxRows so that
        // we can determine whether to show the next button. However, the JSP
        // should only show the number of records displayed which is limited to
        // maxRows within the JSP itself
        if (currentRewardPoints.length == sd.getMaxRows() + 1)
        {
            return sd.getMaxRows();
        }
        return currentRewardPoints.length;
    }

    /**
     * @return the currentOffset
     */
    public int getCurrentOffset()
    {
        return currentOffset;
    }

    /**
     * Based on a configuration variable decides whether reward points are enabled. It returns true
     * if they are enabled. Otherwise it returns false.
     * 
     * @return Returns true if reward points are enabled
     * 
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * 
     */
    public boolean isEnabled() throws KKAppException
    {
        String enabled = kkAppEng.getConfig(ConfigConstants.ENABLE_REWARD_POINTS);
        if (enabled != null && enabled.equalsIgnoreCase("true"))
        {
            return true;
        }
        return false;
    }

    /**
     * Static string - navigate backwards. It is the string required to pass to the
     * navigateCurrentRewardPoints() as the <code>navdir</code> attribute when navigating backwards.
     * 
     * @return Returns the navBack.
     */
    public String getNavBack()
    {
        return navBack;
    }

    /**
     * Static string - navigate forwards. It is the string required to pass to the
     * navigateCurrentRewardPoints() as the <code>navdir</code> attribute when navigating forwards.
     * 
     * @return Returns the navNext.
     */
    public String getNavNext()
    {
        return navNext;
    }

    /**
     * Static string - navigate to the start. It is the string required to pass to the
     * navigateCurrentRewardPoints() as the <code>navdir</code> attribute when navigating to the
     * start.
     * 
     * @return Returns the navStart.
     */
    public String getNavStart()
    {
        return navStart;
    }

    /**
     * Show the back button if set to 1. Don't show the back button if set to 0.
     * 
     * @return Returns the showBack.
     */
    public int getShowBack()
    {
        return showBack;
    }

    /**
     * Show the next button if set to 1. Don't show the next button if set to 0.
     * 
     * @return Returns the showNext.
     */
    public int getShowNext()
    {
        return showNext;
    }

    /**
     * Get an array list of pages to show
     * 
     * @param _currentPage
     * @return Returns an array List of pages to show
     */
    private ArrayList<Integer> getPages(int _currentPage)
    {
        numPages = totalNumberOfRewardPoints / getMaxRows();
        if (totalNumberOfRewardPoints % getMaxRows() != 0)
        {
            numPages++;
        }

        pageList.clear();

        return getPages(_currentPage, numPages, sd.getMaxPageLinks(), pageList);
    }

    /**
     * @return the numPages
     */
    public int getNumPages()
    {
        return numPages;
    }

    /**
     * @param numPages
     *            the numPages to set
     */
    public void setNumPages(int numPages)
    {
        this.numPages = numPages;
    }

    /**
     * @return the totalNumberOfRewardPoints
     */
    public int getTotalNumberOfRewardPoints()
    {
        return totalNumberOfRewardPoints;
    }

    /**
     * Maximum number of reward point transactions to show in a list.
     * 
     * @return Returns the maxRows.
     */
    public int getMaxRows()
    {
        return sd.getMaxRows();
    }

    /**
     * @return the currentRewardPoints
     */
    public RewardPointIf[] getCurrentRewardPoints()
    {
        return currentRewardPoints;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage()
    {
        return currentPage;
    }

    /**
     * @param currentPage
     *            the currentPage to set
     */
    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }

    /**
     * @return the pageList
     */
    public ArrayList<Integer> getPageList()
    {
        return pageList;
    }
    
    /**
     * 
     * @return Returns the standard FetchRewardPointOptions
     */
    public FetchRewardPointOptionsIf getFetchRewardPointOptions(){
        FetchRewardPointOptionsIf options = new FetchRewardPointOptions();
        options.setFetchExpired(true);
        return options;
    }

    /**
     * Used to store the static data of this manager
     */
    private class StaticData
    {
        // number of reward points
        int points = 0;

        // time in ms when points was read
        long pointsTime = 0;

        // Static config variables
        int maxRows = 20;

        int maxPageLinks = 5;

        /**
         * @return the points
         */
        public int getPoints()
        {
            return points;
        }

        /**
         * @param points
         *            the points to set
         */
        public void setPoints(int points)
        {
            this.points = points;
        }

        /**
         * @return the pointsTime
         */
        public long getPointsTime()
        {
            return pointsTime;
        }

        /**
         * @param pointsTime
         *            the pointsTime to set
         */
        public void setPointsTime(long pointsTime)
        {
            this.pointsTime = pointsTime;
        }

        /**
         * @return the maxRows
         */
        public int getMaxRows()
        {
            return maxRows;
        }

        /**
         * @param maxRows
         *            the maxRows to set
         */
        public void setMaxRows(int maxRows)
        {
            this.maxRows = maxRows;
        }

        /**
         * @return the maxPageLinks
         */
        public int getMaxPageLinks()
        {
            return maxPageLinks;
        }

        /**
         * @param maxPageLinks
         *            the maxPageLinks to set
         */
        public void setMaxPageLinks(int maxPageLinks)
        {
            this.maxPageLinks = maxPageLinks;
        }

    }

}
