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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * APSIS MailingList object
 */
public class MailingList
{
    private int id = -1; // id of the mailinglist

    private String name = null; // name of the mailinglist

    private Integer folderId = null; // folder id

    private String created = null; // creation date and time (UTC)
    private Date createdDate = null; // creation date and time (UTC)

    private String updated = null; // creation date and time (UTC)
    private Date updatedDate = null; // update date and time (UTC)

    private String fromName = null; // name of the sender

    private String fromEmail = null; // email of the sender

    private String description = null; // description of the mailinglist

    private String characterSet = null; // the character set of the mailinglist

    private Boolean hidden = null; // a value indicating whether mailinglist is hidden

    public String toString()
    {
        String str = "\n\t\t Id          = " + getId();
        str += "\n\t\t name        = " + getName();
        str += "\n\t\t folderId    = " + getFolderId();
        str += "\n\t\t fromName    = " + getFromName();
        str += "\n\t\t fromEmail   = " + getFromEmail();
        str += "\n\t\t description = " + getDescription();
        str += "\n\t\t hidden      = " + getHidden();
        str += "\n\t\t CharSet     = " + getCharacterSet();
        str += "\n\t\t Created     = " + getCreated() + " = " + getCreatedDate();
        str += "\n\t\t Updated     = " + getUpdated() + " = " + getUpdatedDate();

        return str;
    }

    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    @JsonProperty("Id")
    public void setId(int id)
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
     * @return the folderId
     */
    public Integer getFolderId()
    {
        return folderId;
    }

    /**
     * @param folderId
     *            the folderId to set
     */
    @JsonProperty("FolderId")
    public void setFolderId(Integer folderId)
    {
        this.folderId = folderId;
    }

    /**
     * @return the created
     */
    public String getCreated()
    {
        return created;
    }

    /**
     * @param created
     *            the created to set
     */
    @JsonProperty("Created")
    public void setCreated(String created)
    {
        this.created = created;
        String utc = created.replaceAll("\\D", "");
        long date = Long.parseLong(utc);  
        setCreatedDate(new Date(date));
    }

    /**
     * @return the updated
     */
    public String getUpdated()
    {
        return updated;
    }

    /**
     * @param updated
     *            the updated to set
     */
    @JsonProperty("Updated")
    public void setUpdated(String updated)
    {
        this.updated = updated;
        String utc = updated.replaceAll("\\D", "");
        long date = Long.parseLong(utc);  
        setUpdatedDate(new Date(date));
    }

    /**
     * @return the fromName
     */
    public String getFromName()
    {
        return fromName;
    }

    /**
     * @param fromName
     *            the fromName to set
     */
    @JsonProperty("FromName")
    public void setFromName(String fromName)
    {
        this.fromName = fromName;
    }

    /**
     * @return the fromEmail
     */
    public String getFromEmail()
    {
        return fromEmail;
    }

    /**
     * @param fromEmail
     *            the fromEmail to set
     */
    @JsonProperty("FromEmail")
    public void setFromEmail(String fromEmail)
    {
        this.fromEmail = fromEmail;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    @JsonProperty("Description")
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the characterSet
     */
    public String getCharacterSet()
    {
        return characterSet;
    }

    /**
     * @param characterSet
     *            the characterSet to set
     */
    @JsonProperty("CharacterSet")
    public void setCharacterSet(String characterSet)
    {
        this.characterSet = characterSet;
    }

    /**
     * @return the hidden
     */
    public Boolean getHidden()
    {
        return hidden;
    }

    /**
     * @param hidden
     *            the hidden to set
     */
    @JsonProperty("Hidden")
    public void setHidden(Boolean hidden)
    {
        this.hidden = hidden;
    }

    /**
     * @return the createdDate
     */
    public Date getCreatedDate()
    {
        return createdDate;
    }

    /**
     * @param createdDate the createdDate to set
     */
    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    /**
     * @return the updatedDate
     */
    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    /**
     * @param updatedDate the updatedDate to set
     */
    public void setUpdatedDate(Date updatedDate)
    {
        this.updatedDate = updatedDate;
    }
}
