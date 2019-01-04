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

import com.konakart.appif.KKEngIf;

/**
 * All managers extend BaseMgr.
 */
public class BaseMgr
{
    // Contains an instance of the class used to gain access to server side
    // engine for the application
    protected KKEngIf eng = null;

    // Contains an instance of the class used by the actions to call methods. This class is used by
    // the Containers to get access to the other containers.
    protected KKAppEng kkAppEng = null;

    /**
     * @return Returns the eng.
     */
    public KKEngIf getEng()
    {
        return eng;
    }

    /**
     * @param eng
     *            The eng to set.
     */
    public void setEng(KKEngIf eng)
    {
        this.eng = eng;
    }

    /**
     * @return Returns the kkAppEng.
     */
    public KKAppEng getKkAppEng()
    {
        return kkAppEng;
    }

    /**
     * @param kkAppEng
     *            The kkAppEng to set.
     */
    public void setKkAppEng(KKAppEng kkAppEng)
    {
        this.kkAppEng = kkAppEng;
    }

    /**
     * Is the Manager Ready? This is overwritten wherever required.
     * 
     * @return true if this manager is ready for work, otherwise returns false.
     */
    public boolean isMgrReady()
    {
        return true;
    }

    /**
     * Get an array list of pages to show
     * 
     * @param _currentPage
     *            the _currentPage
     * @param numPages 
     *            the numPages
     * @param _maxPagesToShow 
     *            the _maxPagesToShow
     * @param pageList 
     *            the pageList
     * @return Returns an array List of pages to show
     */
    protected ArrayList<Integer> getPages(int _currentPage, int numPages, int _maxPagesToShow,
            ArrayList<Integer> pageList)
    {
        int currentPage = _currentPage;
        int maxPagesToShow = _maxPagesToShow;
        pageList.clear();

        // Ensure that currentPage is valid
        if (currentPage > numPages)
        {
            currentPage = numPages;
        }

        if (currentPage < 1)
        {
            currentPage = 1;
        }

        // Need to show at least 3 pages
        if (maxPagesToShow < 3)
        {
            maxPagesToShow = 3;
        }

        // ensure that we need to show an odd number of pages
        if (maxPagesToShow % 2 == 0)
        {
            maxPagesToShow++;
        }

        int pagesEitherSide = maxPagesToShow / 2;

        // Add pages before current page
        for (int i = pagesEitherSide; i > 0; i--)
        {
            pageList.add(new Integer(currentPage - i));
        }

        // Add current page
        pageList.add(new Integer(currentPage));

        // Add pages after current page
        for (int i = 0; i < pagesEitherSide; i++)
        {
            pageList.add(new Integer(currentPage + (i + 1)));
        }

        // If page numbers are < 1 remove them from start of list and add to end
        while (pageList.get(0).intValue() < 1)
        {
            int max = pageList.get(pageList.size() - 1).intValue();
            pageList.remove(0);
            if (max < numPages)
            {
                pageList.add(new Integer(max + 1));
            }
        }

        // If page numbers are > max allowed remove them from end of list and add to start
        while (pageList.size() > 0 && pageList.get(pageList.size() - 1).intValue() > numPages)
        {
            pageList.remove(pageList.size() - 1);
            if (pageList.size() > 0 && pageList.get(0).intValue() > 1)
            {
                pageList.add(0, new Integer(pageList.get(0).intValue() - 1));
            }
        }

        return pageList;
    }
}
