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

import com.konakart.appif.NameValueIf;

/**
 * 
 * This class contains attributes for controlling the punch out process
 */
public class PunchOut
{

    private String username;

    private String password;

    private String returnURL;

    private String ociVersion;

    private String returnTarget;

    private String message;

    private NameValueIf[] parmArray;

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the returnURL
     */
    public String getReturnURL()
    {
        return returnURL;
    }

    /**
     * @param returnURL
     *            the returnURL to set
     */
    public void setReturnURL(String returnURL)
    {
        this.returnURL = returnURL;
    }

    /**
     * @return the ociVersion
     */
    public String getOciVersion()
    {
        return ociVersion;
    }

    /**
     * @param ociVersion
     *            the ociVersion to set
     */
    public void setOciVersion(String ociVersion)
    {
        this.ociVersion = ociVersion;
    }

    /**
     * @return the returnTarget
     */
    public String getReturnTarget()
    {
        return returnTarget;
    }

    /**
     * @param returnTarget
     *            the returnTarget to set
     */
    public void setReturnTarget(String returnTarget)
    {
        this.returnTarget = returnTarget;
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * @return the parmArray
     */
    public NameValueIf[] getParmArray()
    {
        return parmArray;
    }

    /**
     * @param parmArray
     *            the parmArray to set
     */
    public void setParmArray(NameValueIf[] parmArray)
    {
        this.parmArray = parmArray;
    }

}
