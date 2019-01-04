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
 * APSIS Response object
 */
public class Response
{
    private int code = -1;

    private String message = null;

    public String toString()
    {
        String str = "\n\t\t code          = " + getCode();

        if (getMessage() != null)
        {
            str += "\n\t\t message       = " + getMessage();
        }
       
        return str;
    }

    /**
     * @return the code
     */
    public int getCode()
    {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    @JsonProperty("Code")
    public void setCode(int code)
    {
        this.code = code;
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
    @JsonProperty("Message")
    public void setMessage(String message)
    {
        this.message = message;
    }
}
