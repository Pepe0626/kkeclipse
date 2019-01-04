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
 * APSIS Project object
 */
public class Project
{
    private int projectId = -1;

    private String projectGuid = null;

    private int newsletterId = -1;

    private String newsletterName = null;

    public String toString()
    {
        String str = "projectId = " + getProjectId();
        str += " projectGuid = " + getProjectGuid();
        str += " newsletterId = " + getNewsletterId();
        str += " newsletterName = " + getNewsletterName();

        return str;
    }

    /**
     * @return the projectId
     */
    public int getProjectId()
    {
        return projectId;
    }

    /**
     * @param projectId
     *            the projectId to set
     */
    @JsonProperty("ProjectId")
    public void setProjectId(int projectId)
    {
        this.projectId = projectId;
    }

    /**
     * @return the projectGuid
     */
    public String getProjectGuid()
    {
        return projectGuid;
    }

    /**
     * @param projectGuid
     *            the projectGuid to set
     */
    @JsonProperty("ProjectGuid")
    public void setProjectGuid(String projectGuid)
    {
        this.projectGuid = projectGuid;
    }

    /**
     * @return the newsletterId
     */
    public int getNewsletterId()
    {
        return newsletterId;
    }

    /**
     * @param newsletterId
     *            the newsletterId to set
     */
    @JsonProperty("NewsletterId")
    public void setNewsletterId(int newsletterId)
    {
        this.newsletterId = newsletterId;
    }

    /**
     * @return the newsletterName
     */
    public String getNewsletterName()
    {
        return newsletterName;
    }

    /**
     * @param newsletterName
     *            the newsletterName to set
     */
    @JsonProperty("NewsletterName")
    public void setNewsletterName(String newsletterName)
    {
        this.newsletterName = newsletterName;
    }
}
