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
 * APSIS PollInterface object
 */
public class PollInterface
{
    private String operationGuid = null;

    private String pollURL = null;

    public String toString()
    {
        String str = "operationGuid = " + getOperationGuid();
        str += " pollURL = " + getPollURL();

        return str;
    }
    
    /**
     * @return the operationGuid
     */
    public String getOperationGuid()
    {
        return operationGuid;
    }

    /**
     * @param operationGuid the operationGuid to set
     */
    @JsonProperty("OperationGuid")
    public void setOperationGuid(String operationGuid)
    {
        this.operationGuid = operationGuid;
    }

    /**
     * @return the pollURL
     */
    public String getPollURL()
    {
        return pollURL;
    }

    /**
     * @param pollURL the pollURL to set
     */
    @JsonProperty("PollURL")
    public void setPollURL(String pollURL)
    {
        this.pollURL = pollURL;
    }
}
