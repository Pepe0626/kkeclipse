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
 * ApsisTransactionalEmail object
 */
public class ApsisTransactionalEmail
{
    private int projectId = -1;

    private boolean allowInactiveProjects = false;

    private ApsisTransactionalEmailBody body = null;

    public String toString()
    {
        String str = "\n\t\t projectId         = " + getProjectId();

        str += "\n\t\t allowInactive?    = " + isAllowInactiveProjects();

        if (getBody() != null)
        {
            str += getBody().toString();
        }

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
    public void setProjectId(int projectId)
    {
        this.projectId = projectId;
    }

    /**
     * @return the allowInactiveProjects
     */
    public boolean isAllowInactiveProjects()
    {
        return allowInactiveProjects;
    }

    /**
     * @param allowInactiveProjects
     *            the allowInactiveProjects to set
     */
    public void setAllowInactiveProjects(boolean allowInactiveProjects)
    {
        this.allowInactiveProjects = allowInactiveProjects;
    }

    /**
     * @return the body
     */
    public ApsisTransactionalEmailBody getBody()
    {
        return body;
    }

    /**
     * @param body
     *            the body to set
     */
    public void setBody(ApsisTransactionalEmailBody body)
    {
        this.body = body;
    }
}
