//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.al;

import java.util.ArrayList;
import java.util.List;

import com.konakart.app.Option;
import com.konakart.appif.OptionIf;

/**
 * 
 * This class is a container class for an array of option values. The class contains the id of the
 * option and the id of the value that has been selected from the array of available values.
 */
public class ProdOptionContainer
{
    private String name;

    private String code;

    private String id;

    private int selectedValueId;

    private String quantity;

    private List<ProdOption> optValues;

    private String custom1;

    private String custom2;

    private String type = Integer.toString(Option.TYPE_SIMPLE);

    /**
     * Constructor
     * 
     * @param id
     *            the id
     * @param name
     *            the name
     * @param code 
     *            the code
     * @param custom1
     *            the custom1
     * @param custom2
     *            the custom2
     */
    public ProdOptionContainer(int id, String name, String code, String custom1, String custom2)
    {
        this.name = name;
        this.code = code;
        this.id = new String(new Integer(id).toString());
        this.custom1 = custom1;
        this.custom2 = custom2;
        optValues = new ArrayList<ProdOption>();
    }

    /**
     * Constructor
     * 
     * @param opt
     *            the opt
     */
    public ProdOptionContainer(OptionIf opt)
    {
        this.name = opt.getName();
        this.code = opt.getCode();
        this.id = new String(new Integer(opt.getId()).toString());
        this.custom1 = opt.getOptionCustom1();
        this.custom2 = opt.getOptionCustom2();
        this.type = Integer.toString(opt.getType());
        optValues = new ArrayList<ProdOption>();
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Returns the optValues.
     */
    public List<ProdOption> getOptValues()
    {
        return optValues;
    }

    /**
     * @param optValues
     *            The optValues to set.
     */
    public void setOptValues(List<ProdOption> optValues)
    {
        this.optValues = optValues;
    }

    /**
     * @return Returns the selectedValueId.
     */
    public int getSelectedValueId()
    {
        return selectedValueId;
    }

    /**
     * @param selectedValueId
     *            The selectedValueId to set.
     */
    public void setSelectedValueId(int selectedValueId)
    {
        this.selectedValueId = selectedValueId;
    }

    /**
     * @return Returns the id.
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the custom1
     */
    public String getCustom1()
    {
        return custom1;
    }

    /**
     * @param custom1
     *            the custom1 to set
     */
    public void setCustom1(String custom1)
    {
        this.custom1 = custom1;
    }

    /**
     * @return the custom2
     */
    public String getCustom2()
    {
        return custom2;
    }

    /**
     * @param custom2
     *            the custom2 to set
     */
    public void setCustom2(String custom2)
    {
        this.custom2 = custom2;
    }

    /**
     * @return the quantity
     */
    public String getQuantity()
    {
        return quantity;
    }

    /**
     * @param quantity
     *            the quantity to set
     */
    public void setQuantity(String quantity)
    {
        this.quantity = quantity;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the code
     */
    public String getCode()
    {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code)
    {
        this.code = code;
    }

}
