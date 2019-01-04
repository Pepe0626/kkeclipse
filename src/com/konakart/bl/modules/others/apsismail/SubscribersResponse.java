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

/**
 * APSIS SubscribersResponse object
 */
public class SubscribersResponse 
{
    private SubscriberResponse[] subscribers = null;

    public String toString()
    {
        String str = null;
        
        if (getSubscribers() == null)
        {
            return str;
        }

        str = "";
        for (SubscriberResponse sub : getSubscribers())
        {
            str += "\n" + sub.toString();
        }
        
        return str;
    }

    /**
     * @return the subscribers
     */
    public SubscriberResponse[] getSubscribers()
    {
        return subscribers;
    }

    /**
     * @param subscribers the subscribers to set
     */
    public void setSubscribers(SubscriberResponse[] subscribers)
    {
        this.subscribers = subscribers;
    }
}
