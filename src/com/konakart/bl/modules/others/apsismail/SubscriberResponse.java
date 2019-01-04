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
import com.konakart.util.Utils;

/**
 * APSIS SubscriberResponse object
 */
public class SubscriberResponse
{
    private String id = null;

    private String name = null;

    private String email = null;

    private DEMDataField[] demographicData = null;

    private String externalId = null;

    public String toString()
    {
        String str = "\n\t\t id             = " + getId();
        str += "\n\t\t Name           = " + getName();
        str += "\n\t\t Email          = " + getEmail();
        str += "\n\t\t ExternalId     = " + getExternalId();
      
        if (getDemographicData() != null)
        {
            str += "\n\t\t DemographicData  > ";
            int maxKey = 0;
            for (DEMDataField ddf : getDemographicData())
            {
                if (ddf.getKey().length() > maxKey)
                {
                    maxKey = ddf.getKey().length();
                }
            }
            for (DEMDataField ddf : getDemographicData())
            {
                str += "\n\t\t                " + Utils.padRight(ddf.getKey(), maxKey) + " = "
                        + ddf.getValue();
            }
        }

        return str;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    @JsonProperty("Id")
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    @JsonProperty("Name")
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * @param email
     *            the email to set
     */
    @JsonProperty("Email")
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * @return the demographicData
     */
    public DEMDataField[] getDemographicData()
    {
        return demographicData;
    }

    /**
     * @param demographicData the demographicData to set
     */
    @JsonProperty("DemographicData")
    public void setDemographicData(DEMDataField[] demographicData)
    {
        this.demographicData = demographicData;
    }

    /**
     * @return the externalId
     */
    public String getExternalId()
    {
        return externalId;
    }

    /**
     * @param externalId
     *            the externalId to set
     */
    @JsonProperty("ExternalId")
    public void setExternalId(String externalId)
    {
        this.externalId = externalId;
    }
}
