//
// (c) 2018 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.bl.modules.others.apsismail;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * APSIS PollResponse object
 */
public class PollResponse 
{
    private String dataUrl = null;
    private String message = null;
    private String state = null;
    private String stateName = null;

    public String toString()
    {
        String str = "\n\t\t DataUrl      = " + getDataUrl();
        str += "\n\t\t Message      = " + getMessage();
        str += "\n\t\t State        = " + getState();
        str += "\n\t\t StateName    = " + getStateName();
        
        return str;
    }

    /**
     * @return the dataUrl
     */
    public String getDataUrl()
    {
        return dataUrl;
    }

    /**
     * @param dataUrl the dataUrl to set
     */
    @JsonProperty("DataUrl")
    public void setDataUrl(String dataUrl)
    {
        this.dataUrl = dataUrl;
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message the message to set
     */
    @JsonProperty("Message")
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * @return the state
     */
    public String getState()
    {
        return state;
    }

    /**
     * @param state the state to set
     */
    @JsonProperty("State")
    public void setState(String state)
    {
        this.state = state;
    }

    /**
     * @return the stateName
     */
    public String getStateName()
    {
        return stateName;
    }

    /**
     * @param stateName the stateName to set
     */
    @JsonProperty("StateName")
    public void setStateName(String stateName)
    {
        this.stateName = stateName;
    }
}
