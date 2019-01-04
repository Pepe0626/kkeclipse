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
 * TransactionalAttachment object
 */
public class TransactionalAttachment
{
    private String name = null;

    private String content = null;

    private String contentType = null;

    public String toString()
    {
        String str = "\n\t\t name              = " + getName();

        if (getContent() == null)
        {
            str += "\n\t\t content           = null";
        } else
        {
            str += "\n\t\t content           = "
                    + getContent().substring(0, Math.min(20, getContent().length() - 1)) + "...";
        }
        str += "\n\t\t contentType       = " + getContentType();

        return str;
    }

    /**
     * @return the name
     */
    @JsonProperty("Name")
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the content
     */
    @JsonProperty("Content")
    public String getContent()
    {
        return content;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(String content)
    {
        this.content = content;
    }

    /**
     * @return the contentType
     */
    @JsonProperty("ContentType")
    public String getContentType()
    {
        return contentType;
    }

    /**
     * @param contentType
     *            the contentType to set
     */
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }
}
