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

/**
 * Utility class to trim strings so that they may be presented on the UI.
 * 
 */
public class TrimString
{
    private static int DEFAULT_LENGTH_END = 15;

    private static int DEFAULT_LENGTH_MIDDLE = 15;

    /**
     * Characters at the end of the trim
     */
    public static String END_TRIM = "...";

    /**
     * Returns the string trimmed to the default length
     * 
     * @param in
     *            the in
     * @return The string trimmed to the default length
     */
    public static String trimEnd(String in)
    {
        return trimEnd(in, DEFAULT_LENGTH_END);
    }

    /**
     * Returns the string trimmed at the end to the length passed in as a parameter.
     * 
     * @param in
     *            the in
     * @param length
     *            the length
     * @return The trimmed string
     */
    public static String trimEnd(String in, int length)
    {
        if (in == null)
        {
            return null;
        }
        
        if (in.length() > length)
        {
            return(in.substring(0, length) + END_TRIM);
        }
        
        return in;
    }

    /**
     * Returns the string trimmed in the middle to the default length.
     * 
     * @param in
     *            the in
     * @return The parameter trimmed to the default length
     */
    public static String trimMiddle(String in)
    {
        return trimMiddle(in, DEFAULT_LENGTH_MIDDLE);
    }

    /**
     * Returns the string trimmed in the middle to the length passed in as a parameter.
     * 
     * @param in
     *            the in
     * @param length
     *            the length
     * @return The trimmed string
     */
    public static String trimMiddle(String in, int length)
    {

        if (in == null)
        {
            return null;
        }

        if (in.length() > length)
        {
            String firstHalf = in.substring(0, length / 2);
            String secondHalf = in.substring(in.length() - length / 2);
            return (firstHalf + END_TRIM + secondHalf);
        }

        return in;
    }
}
