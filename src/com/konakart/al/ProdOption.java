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

import java.math.BigDecimal;

import com.konakart.app.KKException;
import com.konakart.app.Option;
import com.konakart.appif.OptionIf;

/**
 * 
 * A ProdOptionContainer class contains an array of these ProdOption classes. The purpose of this
 * class is to contain all of the value information for a product option including the price of the
 * value.(e.g. Option = memory, Value = 32MB, 64MB etc.). The attributes sign and formatted values
 * are calculated in this class in order to not have to implement the logic in the JSP
 */
public class ProdOption
{

    private int id;

    private String code;
    
    private String value;

    private BigDecimal priceExTax;

    private BigDecimal priceIncTax;

    private String sign;

    private String formattedValueExTax;

    private String formattedValueIncTax;

    /**
     * custom attribute field
     */
    private String attrCustom1;

    private String attrCustom2;

    /**
     * custom option field
     */
    private String optionCustom1;

    private String optionCustom2;

    /**
     * custom option value field
     */
    private String optionValCustom1;

    private String optionValCustom2;

    /**
     * Constructor
     * 
     * @param id
     *            the id
     * @param code 
     *            the code
     * @param value
     *            the value
     * @param priceExTax
     *            the priceExTax
     * @param priceIncTax
     *            the priceIncTax
     * @param attrCustom1
     *            Custom field that can take a unique value whenever the option is connected to a
     *            product.
     * @param attrCustom2
     *            Custom field that can take a unique value whenever the option is connected to a
     *            product.
     * @param optionCustom1
     *            Custom field, the value of which is defined when the option is created.
     * @param optionCustom2
     *            Custom field, the value of which is defined when the option is created.
     * @param optionValCustom1
     *            Custom field, the value of which is defined when the option value is created.
     * @param optionValCustom2
     *            Custom field, the value of which is defined when the option value is created.
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public ProdOption(int id, String code, String value, BigDecimal priceExTax, BigDecimal priceIncTax,
            String attrCustom1, String attrCustom2, String optionCustom1, String optionCustom2,
            String optionValCustom1, String optionValCustom2, KKAppEng kkAppEng)
            throws KKException, KKAppException
    {

        this.code = code;
        this.value = value;
        this.id = id;
        this.priceExTax = priceExTax;
        this.priceIncTax = priceIncTax;
        this.attrCustom1 = attrCustom1;
        this.attrCustom2 = attrCustom2;
        this.optionCustom1 = optionCustom1;
        this.optionCustom2 = optionCustom2;
        this.optionValCustom1 = optionValCustom1;
        this.optionValCustom2 = optionValCustom2;

        createFormattedValues(Option.TYPE_SIMPLE, kkAppEng);
    }

    /**
     * Constructor
     * 
     * @param opt
     *            the opt
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public ProdOption(OptionIf opt, KKAppEng kkAppEng) throws KKException, KKAppException
    {
        this.code = opt.getValueCode();
        this.value = opt.getValue();
        this.id = opt.getValueId();
        this.priceExTax = opt.getPriceExTax();
        this.priceIncTax = opt.getPriceIncTax();
        this.attrCustom1 = opt.getAttrCustom1();
        this.attrCustom2 = opt.getAttrCustom2();
        this.optionCustom1 = opt.getOptionCustom1();
        this.optionCustom2 = opt.getOptionCustom2();
        this.optionValCustom1 = opt.getOptionValCustom1();
        this.optionValCustom2 = opt.getOptionValCustom2();

        createFormattedValues(opt.getType(), kkAppEng);
    }

    private void createFormattedValues(int type, KKAppEng kkAppEng) throws KKAppException
    {

        BigDecimal zero = new BigDecimal(0);

        if (type == Option.TYPE_VARIABLE_QUANTITY)
        {
            if (priceExTax.compareTo(zero) == -1)
            {
                // - sign is put in by formatter
                sign = new String("");
            } else
            {
                sign = new String("+");
            }

            if (priceExTax.compareTo(zero) == 0)
            {
                formattedValueExTax = new String(value);
            } else
            {
                formattedValueExTax = new String("(" + sign + kkAppEng.formatPrice(priceExTax)
                        + " / " + value + ")");
            }

            if (priceIncTax.compareTo(zero) == 0)
            {
                formattedValueIncTax = new String(value);
            } else
            {
                formattedValueIncTax = new String("(" + sign + kkAppEng.formatPrice(priceIncTax)
                        + " / " + value + ")");
            }
        } else
        {
            if (priceExTax.compareTo(zero) == -1)
            {
                // - sign is put in by formatter
                sign = new String("");
            } else
            {
                sign = new String("+");
            }

            if (priceExTax.compareTo(zero) == 0)
            {
                formattedValueExTax = new String(value);
            } else
            {
                formattedValueExTax = new String(value + " (" + sign
                        + kkAppEng.formatPrice(priceExTax) + ")");
            }

            if (priceIncTax.compareTo(zero) == 0)
            {
                formattedValueIncTax = new String(value);
            } else
            {
                formattedValueIncTax = new String(value + " (" + sign
                        + kkAppEng.formatPrice(priceIncTax) + ")");
            }
        }

    }

    /**
     * @return Returns the id.
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * @return Returns the value.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return Returns the sign.
     */
    public String getSign()
    {
        return sign;
    }

    /**
     * @param sign
     *            The sign to set.
     */
    public void setSign(String sign)
    {
        this.sign = sign;
    }

    /**
     * @return Returns the priceExTax.
     */
    public BigDecimal getPriceExTax()
    {
        return priceExTax;
    }

    /**
     * @param priceExTax
     *            The priceExTax to set.
     */
    public void setPriceExTax(BigDecimal priceExTax)
    {
        this.priceExTax = priceExTax;
    }

    /**
     * @return Returns the priceIncTax.
     */
    public BigDecimal getPriceIncTax()
    {
        return priceIncTax;
    }

    /**
     * @param priceIncTax
     *            The priceIncTax to set.
     */
    public void setPriceIncTax(BigDecimal priceIncTax)
    {
        this.priceIncTax = priceIncTax;
    }

    /**
     * @return Returns the formattedValueExTax.
     */
    public String getFormattedValueExTax()
    {
        return formattedValueExTax;
    }

    /**
     * @param formattedValueExTax
     *            The formattedValueExTax to set.
     */
    public void setFormattedValueExTax(String formattedValueExTax)
    {
        this.formattedValueExTax = formattedValueExTax;
    }

    /**
     * @return Returns the formattedValueIncTax.
     */
    public String getFormattedValueIncTax()
    {
        return formattedValueIncTax;
    }

    /**
     * @param formattedValueIncTax
     *            The formattedValueIncTax to set.
     */
    public void setFormattedValueIncTax(String formattedValueIncTax)
    {
        this.formattedValueIncTax = formattedValueIncTax;
    }

    /**
     * Custom field that can take a unique value whenever the option is connected to a product.
     * 
     * @return the attrCustom1
     */
    public String getAttrCustom1()
    {
        return attrCustom1;
    }

    /**
     * Custom field that can take a unique value whenever the option is connected to a product.
     * 
     * @param attrCustom1
     *            the attrCustom1 to set
     */
    public void setAttrCustom1(String attrCustom1)
    {
        this.attrCustom1 = attrCustom1;
    }

    /**
     * Custom field that can take a unique value whenever the option is connected to a product.
     * 
     * @return the attrCustom2
     */
    public String getAttrCustom2()
    {
        return attrCustom2;
    }

    /**
     * Custom field that can take a unique value whenever the option is connected to a product.
     * 
     * @param attrCustom2
     *            the attrCustom2 to set
     */
    public void setAttrCustom2(String attrCustom2)
    {
        this.attrCustom2 = attrCustom2;
    }

    /**
     * Custom field, the value of which is defined when the option is created.
     * 
     * @return the optionCustom1
     */
    public String getOptionCustom1()
    {
        return optionCustom1;
    }

    /**
     * Custom field, the value of which is defined when the option is created.
     * 
     * @param optionCustom1
     *            the optionCustom1 to set
     */
    public void setOptionCustom1(String optionCustom1)
    {
        this.optionCustom1 = optionCustom1;
    }

    /**
     * Custom field, the value of which is defined when the option is created.
     * 
     * @return the optionCustom2
     */
    public String getOptionCustom2()
    {
        return optionCustom2;
    }

    /**
     * Custom field, the value of which is defined when the option is created.
     * 
     * @param optionCustom2
     *            the optionCustom2 to set
     */
    public void setOptionCustom2(String optionCustom2)
    {
        this.optionCustom2 = optionCustom2;
    }

    /**
     * Custom field, the value of which is defined when the option value is created.
     * 
     * @return the optionValCustom1
     */
    public String getOptionValCustom1()
    {
        return optionValCustom1;
    }

    /**
     * Custom field, the value of which is defined when the option value is created.
     * 
     * @param optionValCustom1
     *            the optionValCustom1 to set
     */
    public void setOptionValCustom1(String optionValCustom1)
    {
        this.optionValCustom1 = optionValCustom1;
    }

    /**
     * Custom field, the value of which is defined when the option value is created.
     * 
     * @return the optionValCustom2
     */
    public String getOptionValCustom2()
    {
        return optionValCustom2;
    }

    /**
     * Custom field, the value of which is defined when the option value is created.
     * 
     * @param optionValCustom2
     *            the optionValCustom2 to set
     */
    public void setOptionValCustom2(String optionValCustom2)
    {
        this.optionValCustom2 = optionValCustom2;
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
