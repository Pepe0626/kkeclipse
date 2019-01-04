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
 * APSIS CreateSubscribersResponse object
 */
public class CreateSubscribersResponse 
{
    private Integer[] createdSubscriberIds = null;

    private Integer[] failedCreatedSubscribers = null;

    public String toString()
    {
        String str = "\n\t\t CreatedSubscriberIds >";
        
        if (createdSubscriberIds != null)
        {
            for (Integer id : getCreatedSubscriberIds())
            {
            str += "\n\t\t     " + id; 
            }
        }
        
        str += "\n\t\t FailedCreatedSubscribers >";
        
        if (failedCreatedSubscribers != null)
        {
            for (Integer id : getFailedCreatedSubscribers())
            {
            str += "\n\t\t     " + id; 
            }
        }
        
        return str;
    }

    /**
     * @return the createdSubscriberIds
     */
    public Integer[] getCreatedSubscriberIds()
    {
        return createdSubscriberIds;
    }

    /**
     * @param createdSubscriberIds the createdSubscriberIds to set
     */
    @JsonProperty("CreatedSubscriberIds")
    public void setCreatedSubscriberIds(Integer[] createdSubscriberIds)
    {
        this.createdSubscriberIds = createdSubscriberIds;
    }

    /**
     * @return the failedCreatedSubscribers
     */
    public Integer[] getFailedCreatedSubscribers()
    {
        return failedCreatedSubscribers;
    }

    /**
     * @param failedCreatedSubscribers the failedCreatedSubscribers to set
     */
    @JsonProperty("FailedCreatedSubscribers")
    public void setFailedCreatedSubscribers(Integer[] failedCreatedSubscribers)
    {
        this.failedCreatedSubscribers = failedCreatedSubscribers;
    }
}
