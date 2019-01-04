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
 * APSIS EmailSending object
 */
public class DEMDataField
{
    private String key = null;

    private String value = null;

    public String toString()
    {
        String str = "\n\t\t key               = " + getKey();
        str += "\n\t\t value             = " + getValue();

        return str;
    }

    /**
     * @return the key
     */
    @JsonProperty("Key")
    public String getKey()
    {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    @JsonProperty("Key")
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * @return the value
     */
    @JsonProperty("Value")
    public String getValue()
    {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    @JsonProperty("Value")
    public void setValue(String value)
    {
        this.value = value;
    }
}
