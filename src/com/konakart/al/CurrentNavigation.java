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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

//import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Object used by the header JSP to show the current navigation within the application.
 */
public class CurrentNavigation
{

    private static final String separator = ";";

    private List<String> navList;

    /**
     * Constructor
     */
    public CurrentNavigation()
    {
        navList = new ArrayList<String>();
    }

    /**
     * Constructor
     * 
     * @param _navList
     *            the _navList
     * 
     */
    public CurrentNavigation(List<String> _navList)
    {
        navList = _navList;
    }

    /**
     * Returns a clone of this object
     */
    public CurrentNavigation clone()
    {
        ArrayList<String> cloneNavList = new ArrayList<String>();
        if (navList != null && navList.size() > 0)
        {
            cloneNavList = new ArrayList<String>(navList);
        }
        return new CurrentNavigation(cloneNavList);
    }

    /**
     * 
     */
    public void clear()
    {
        navList.clear();
    }

    /**
     * @param element
     *            the element
     */
    public void add(String element)
    {
        if (element == null)
        {
            return;
        }
        if (navList.size() == 0)
        {
            this.set(element);
        }
        String lastEntry = navList.get(navList.size() - 1);
        if (!lastEntry.equals(element))
        {
            navList.add(element);
        }
        removeDuplicates();
    }

    /**
     * @param element
     *            the element
     * @param request
     *            the request
     */
    public void add(String element, HttpServletRequest request)
    {
        if (element == null)
        {
            return;
        }
        if (navList.size() == 0)
        {
            this.set(element, request);
        }
        String lastEntry = navList.get(navList.size() - 1);
        if (!getName(lastEntry).equals(element))
        {
            navList.add(element + separator + getRequestUrl(request));
        }
        removeDuplicates();
    }

    /**
     * @param element
     *            the element
     * @param url
     *            the url
     */
    public void add(String element, String url)
    {
        if (element == null)
        {
            return;
        }
        if (navList.size() == 0)
        {
            this.set(element, url);
        }
        String lastEntry = navList.get(navList.size() - 1);
        if (!getName(lastEntry).equals(element))
        {
            navList.add(element + separator + url);
        }
        removeDuplicates();
    }

    /**
     * @param request
     * @return A string containing struts action plus parameters
     */
    private String getRequestUrl(HttpServletRequest request)
    {
        String servletPath = request.getServletPath();

        // Remove initial forward slash
        if (servletPath != null && servletPath.length() > 1 && servletPath.startsWith("/"))
        {
            servletPath = servletPath.substring(1);
        }
        /*
         * We convert LoginSubmit to LogIn so that you don't see the login parameters on the browser
         * command line when you click on the bread crumb
         */
        if (servletPath != null && servletPath.contains("LoginSubmit"))
        {
            return "LogIn.action";
        }

        if (request.getMethod() != null && request.getMethod().equalsIgnoreCase("post"))
        {
            return servletPath;
        }

        // Get the parameters
        StringBuffer parms = new StringBuffer();
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements())
        {
            String paramName = en.nextElement();
            paramName = StringEscapeUtils.escapeHtml4(paramName);

            // Ignore password and user
            if (paramName.equalsIgnoreCase("password") || paramName.equalsIgnoreCase("user"))
            {
                continue;
            }
            String paramValue = request.getParameter(paramName);
            paramValue = StringEscapeUtils.escapeHtml4(paramValue);
            if (parms.length() > 0)
            {
                parms.append("&");
            } else
            {
                parms.append("?");
            }
            parms.append(paramName);
            parms.append("=");
            parms.append(paramValue);
        }
        return servletPath + parms.toString();
    }

    /**
     * @param entry
     * @return Returns the name part of the entry in the list
     */
    private String getName(String entry)
    {
        if (entry != null)
        {
            String[] strArray = entry.split(separator);
            return strArray[0];
        }
        return "";
    }

    /**
     * @param element
     *            the element
     */
    public void set(String element)
    {
        navList.clear();
        if (element == null)
        {
            return;
        }
        navList.add(element);
    }

    /**
     * @param element
     *            the element
     * @param request
     *            the request
     */
    public void set(String element, HttpServletRequest request)
    {
        navList.clear();
        if (element == null)
        {
            return;
        }
        if (request.getMethod() != null && request.getMethod().equals("GET"))
        {
            navList.add(element + separator + getRequestUrl(request));
        } else
        {
            navList.add(element);
        }
    }

    /**
     * @param element
     *            the element
     * @param url
     *            the url
     */
    public void set(String element, String url)
    {
        navList.clear();
        if (element == null)
        {
            return;
        }
        navList.add(element + separator + url);
    }

    /**
     * Get an array of strings that define the current navigation. i.e.
     * 
     * {@code
     *     Top >> Catalog >> Product Details
     * }
     * 
     * @return Array of strings
     */
    public String[] getNavigation()
    {

        String[] sArray = new String[navList.size()];
        int i = 0;
        for (Iterator<String> iter = navList.iterator(); iter.hasNext();)
        {
            String s = iter.next();
            sArray[i++] = s;
        }
        return sArray;
    }

    /**
     * Remove duplicates from the navList. If we see a duplicate we delete it and all of the other
     * entries up to the first occurrence.
     */
    private void removeDuplicates()
    {
        if (navList.size() > 1)
        {
            int i = 0;
            int lastGoodEntry = 0;
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            for (Iterator<String> iterator = navList.iterator(); iterator.hasNext();)
            {
                String entry = iterator.next();
                String[] entryArray = entry.split(separator);
                if (map.get(entryArray[0]) == null)
                {
                    map.put(entryArray[0], new Integer(i));
                    i++;
                } else
                {
                    lastGoodEntry = map.get(entryArray[0]).intValue();
                    break;
                }
            }
            if (lastGoodEntry > 0)
            {
                while (navList.size() > lastGoodEntry + 1)
                {
                    navList.remove(navList.size() - 1);
                }
            }
        }
    }

}
