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
 * ApsisTransactionalEmailBody object
 */
public class ApsisTransactionalEmailBody
{
    private String email = null;

    private String name = null;

    private String format = "HTML";

    private String ExternalId = null;

    private String countryCode = null;

    private String phoneNumber = null;

    private String DDHtml = null;

    private String sendingType = "t";

    private DEMDataField[] demDataFields = null;

    private TransactionalAttachment[] attachments = null;

    public String toString()
    {
        String str = "\n\t\t email             = " + getEmail();

        str += "\n\t\t name              = " + getName();

        if (getFormat() != null)
        {
            str += "\n\t\t format            = " + getFormat();
        }
        if (getCountryCode() != null)
        {
            str += "\n\t\t Country Code      = " + getCountryCode();
        }
        if (getPhoneNumber() != null)
        {
            str += "\n\t\t Phone number      = " + getPhoneNumber();
        }

        str += "\n\t\t sending type      = " + getSendingType();

        if (getDDHtml() != null)
        {
            str += "\n\t\t DDHtml            = " + getDDHtml();
        }

        if (getDemDataFields() != null)
        {
            str += "\n\t\t DemData           >";
            for (DEMDataField dd : getDemDataFields())
            {
                str += dd.toString();
            }
        }
        if (getAttachments() != null)
        {
            str += "\n\t\t Attachments       >";
            for (TransactionalAttachment ta : getAttachments())
            {
                str += ta.toString();
            }
        }

        return str;
    }

    /**
     * @return the email
     */
    @JsonProperty("Email")
    public String getEmail()
    {
        return email;
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email)
    {
        this.email = email;
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
     * @return the format
     */
    @JsonProperty("Format")
    public String getFormat()
    {
        return format;
    }

    /**
     * @param format
     *            the format to set
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @return the externalId
     */
    @JsonProperty("ExternalId")
    public String getExternalId()
    {
        return ExternalId;
    }

    /**
     * @param externalId
     *            the externalId to set
     */
    public void setExternalId(String externalId)
    {
        ExternalId = externalId;
    }

    /**
     * @return the countryCode
     */
    @JsonProperty("CountryCode")
    public String getCountryCode()
    {
        return countryCode;
    }

    /**
     * @param countryCode
     *            the countryCode to set
     */
    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }

    /**
     * @return the phoneNumber
     */
    @JsonProperty("PhoneNumber")
    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    /**
     * @param phoneNumber
     *            the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return the dDHtml
     */
    @JsonProperty("DDHtml")
    public String getDDHtml()
    {
        return DDHtml;
    }

    /**
     * @param dDHtml
     *            the dDHtml to set
     */
    public void setDDHtml(String dDHtml)
    {
        DDHtml = dDHtml;
    }

    /**
     * @return the sendingType
     */
    public String getSendingType()
    {
        return sendingType;
    }

    /**
     * @param sendingType
     *            the sendingType to set
     */
    public void setSendingType(String sendingType)
    {
        this.sendingType = sendingType;
    }

    /**
     * @return the demDataFields
     */
    @JsonProperty("DemDataFields")
    public DEMDataField[] getDemDataFields()
    {
        return demDataFields;
    }

    /**
     * @param demDataFields
     *            the demDataFields to set
     */
    public void setDemDataFields(DEMDataField[] demDataFields)
    {
        this.demDataFields = demDataFields;
    }

    /**
     * @return the attachments
     */
    @JsonProperty("Attachments")
    public TransactionalAttachment[] getAttachments()
    {
        return attachments;
    }

    /**
     * @param attachments
     *            the attachments to set
     */
    public void setAttachments(TransactionalAttachment[] attachments)
    {
        this.attachments = attachments;
    }
}
