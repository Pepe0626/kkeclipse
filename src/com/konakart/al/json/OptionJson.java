//
// (c) 2013 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.al.json;

/**
 * Simplified Option Object to send required data back to the browser in JSON format
 */
public class OptionJson
{

    /**
     * name
     */
    private String name;

    /**
     * value
     */
    private String value;

    /**
     * type of option
     */
    private int type;

    /**
     * quantity
     */
    private int quantity;

    /**
     * formattedCustPrice
     */
    private String formattedCustPrice="";

    /**
     * customerText
     */
    private String customerText="";

    /**
     * Constructor
     */
    public OptionJson()
    {
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
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return the type
     */
    public int getType()
    {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(int type)
    {
        this.type = type;
    }

    /**
     * @return the quantity
     */
    public int getQuantity()
    {
        return quantity;
    }

    /**
     * @param quantity
     *            the quantity to set
     */
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    /**
     * @return the formattedCustPrice
     */
    public String getFormattedCustPrice()
    {
        return formattedCustPrice;
    }

    /**
     * @param formattedCustPrice
     *            the formattedCustPrice to set
     */
    public void setFormattedCustPrice(String formattedCustPrice)
    {
        this.formattedCustPrice = formattedCustPrice;
    }

    /**
     * @return the customerText
     */
    public String getCustomerText()
    {
        return customerText;
    }

    /**
     * @param customerText
     *            the customerText to set
     */
    public void setCustomerText(String customerText)
    {
        this.customerText = customerText;
    }

}
