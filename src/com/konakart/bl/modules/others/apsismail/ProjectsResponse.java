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
 * APSIS ProjectsResponse object
 */
public class ProjectsResponse extends Response
{
    private Project[] result = null;

    public String toString()
    {
        String str = super.toString();

        str += "\n\t\t result      >";
        
        if (result != null)
        {
            for (Project pro : getResult())
            {
            str += "\n\t\t   " + pro.toString(); 
            }
        }
        
        return str;
    }
    
    /**
     * @return the result
     */
    public Project[] getResult()
    {
        return result;
    }

    /**
     * @param result
     *            the result to set
     */
    @JsonProperty("Result")
    public void setResult(Project[] result)
    {
        this.result = result;
    }
}
