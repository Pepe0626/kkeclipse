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

import com.konakart.app.DataDescriptor;
import com.konakart.app.KKException;
import com.konakart.appif.CategoryIf;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.appif.ManufacturerIf;
import com.konakart.appif.ProductIf;
import com.konakart.appif.ProductsIf;
import com.konakart.appif.ReviewIf;
import com.konakart.appif.ReviewsIf;

/**
 * Used to provide an XML interface to the engine
 */
public class XMLUtils
{
    /** */
    public static final String EXCEPTION_START = "<kkException><message><";

    /** */
    public static final String EXCEPTION_END = "></message></kkException>";

    private static final String RESP_START = "<kkResponse>";

    private static final String RESP_END = "</kkResponse>";

    /**
     * Serializes the object into an XML string.
     * 
     * @param eng
     *            the eng
     * @param input
     *            the input
     * @return Returns an XML string
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public static String getXML(KKAppEng eng, Object input) throws KKException, KKAppException
    {

        if (input == null)
        {
            return RESP_START + RESP_END;
        } else if (input instanceof CategoryIf[])
        {
            StringBuffer sb = new StringBuffer();
            getCatArray((CategoryIf[]) input, sb);
            return RESP_START + sb.toString() + RESP_END;
        } else if (input instanceof ProductsIf)
        {
            StringBuffer sb = new StringBuffer();
            getProducts(eng, (ProductsIf) input, sb);
            return RESP_START + sb.toString() + RESP_END;
        } else if (input instanceof ReviewsIf)
        {
            StringBuffer sb = new StringBuffer();
            getReviews(eng, (ReviewsIf) input, sb);
            return RESP_START + sb.toString() + RESP_END;
        } else if (input instanceof ProductIf)
        {
            StringBuffer sb = new StringBuffer();
            getProduct(eng, (ProductIf) input, sb);
            return RESP_START + sb.toString() + RESP_END;
        } else if (input instanceof ManufacturerIf)
        {
            StringBuffer sb = new StringBuffer();
            getManufacturer(eng, (ManufacturerIf) input, sb);
            return RESP_START + sb.toString() + RESP_END;
        } else if (input instanceof ManufacturerIf[])
        {
            StringBuffer sb = new StringBuffer();
            getManufacturers(eng, (ManufacturerIf[]) input, sb);
            return RESP_START + sb.toString() + RESP_END;
        }

        return getException("Unhandled return type :" + input.getClass().getName());

    }

    private static void getCatArray(CategoryIf[] catArray, StringBuffer sb)
    {
        for (int i = 0; i < catArray.length; i++)
        {
            CategoryIf cat = catArray[i];
            sb.append("<Category>");
            sb.append("<id>");
            sb.append(cat.getId());
            sb.append("</id>");
            if (cat.getImage() != null)
            {
                sb.append("<image>");
                sb.append(cat.getImage());
                sb.append("</image>");
            }
            if (cat.getName() != null)
            {
                sb.append("<name>");
                sb.append(cat.getName());
                sb.append("</name>");
            }
            sb.append("<numberOfProducts>");
            sb.append(cat.getNumberOfProducts());
            sb.append("</numberOfProducts>");
            sb.append("<parentId>");
            sb.append(cat.getParentId());
            sb.append("</parentId>");
            sb.append("<sortOrder>");
            sb.append(cat.getSortOrder());
            sb.append("</sortOrder>");
            if (cat.getChildren() != null)
            {
                getCatArray(cat.getChildren(), sb);
            }
            sb.append("</Category>");
        }
    }

    private static void getProduct(KKAppEng eng, ProductIf product, StringBuffer sb)
            throws KKException, KKAppException
    {
        sb.append(getProductXML(eng, product));
    }

    private static void getManufacturer(KKAppEng eng, ManufacturerIf manu, StringBuffer sb)
            throws KKException, KKAppException
    {
        sb.append(getManufacturerXML(eng, manu));
    }

    private static void getProducts(KKAppEng eng, ProductsIf products, StringBuffer sb)
            throws KKException, KKAppException
    {
        BigDecimal minPriceExTax = new BigDecimal(0), maxPriceExTax = new BigDecimal(0);
        BigDecimal minPriceIncTax = new BigDecimal(0), maxPriceIncTax = new BigDecimal(0);

        sb.append("<Products>");
        sb.append("<totalNumProducts>");
        sb.append(products.getTotalNumProducts());
        sb.append("</totalNumProducts>");

        if (products.getProductArray() != null)
        {
            for (int i = 0; i < products.getProductArray().length; i++)
            {
                ProductIf prod = products.getProductArray()[i];
                sb.append(getProductXML(eng, prod));

                /*
                 * Update the maximum and minimum prices (with and without tax)
                 */
                // With tax
                BigDecimal priceIncTax = null;
                if (prod.getSpecialPriceIncTax() != null)
                {
                    priceIncTax = prod.getSpecialPriceIncTax();
                } else if (prod.getPriceIncTax() != null)
                {
                    priceIncTax = prod.getPriceIncTax();
                }
                if (priceIncTax != null)
                {
                    if (i == 0)
                    {
                        minPriceIncTax = priceIncTax;
                        maxPriceIncTax = priceIncTax;
                    } else
                    {
                        if (priceIncTax.compareTo(minPriceIncTax) < 0)
                        {
                            minPriceIncTax = priceIncTax;
                        } else if (priceIncTax.compareTo(maxPriceIncTax) > 0)
                        {
                            maxPriceIncTax = priceIncTax;
                        }
                    }
                }
                // Without tax
                BigDecimal priceExTax = null;
                if (prod.getSpecialPriceExTax() != null)
                {
                    priceExTax = prod.getSpecialPriceExTax();
                } else if (prod.getPriceExTax() != null)
                {
                    priceExTax = prod.getPriceExTax();
                }
                if (priceExTax != null)
                {
                    if (i == 0)
                    {
                        minPriceExTax = priceExTax;
                        maxPriceExTax = priceExTax;
                    } else
                    {
                        if (priceExTax.compareTo(minPriceExTax) < 0)
                        {
                            minPriceExTax = priceExTax;
                        } else if (priceExTax.compareTo(maxPriceExTax) > 0)
                        {
                            maxPriceExTax = priceExTax;
                        }
                    }
                }

            }
        }
        sb.append("<minPriceIncTax>");
        sb.append(minPriceIncTax);
        sb.append("</minPriceIncTax>");
        sb.append("<maxPriceIncTax>");
        sb.append(maxPriceIncTax);
        sb.append("</maxPriceIncTax>");
        sb.append("<minPriceExTax>");
        sb.append(minPriceExTax);
        sb.append("</minPriceExTax>");
        sb.append("<maxPriceExTax>");
        sb.append(maxPriceExTax);
        sb.append("</maxPriceExTax>");
        sb.append("</Products>");
    }

    private static void getManufacturers(KKAppEng eng, ManufacturerIf[] manufacturers,
            StringBuffer sb) throws KKException, KKAppException
    {

        if (manufacturers != null)
        {
            for (int i = 0; i < manufacturers.length; i++)
            {
                ManufacturerIf manu = manufacturers[i];
                sb.append(getManufacturerXML(eng, manu));

            }
        }
    }

    private static void getReviews(KKAppEng eng, ReviewsIf reviews, StringBuffer sb)
            throws KKException, KKAppException
    {
        sb.append("<Reviews>");
        sb.append("<totalNumReviews>");
        sb.append(reviews.getTotalNumReviews());
        sb.append("</totalNumReviews>");

        if (reviews.getReviewArray() != null)
        {
            for (int i = 0; i < reviews.getReviewArray().length; i++)
            {
                ReviewIf rev = reviews.getReviewArray()[i];
                sb.append(getReviewXML(eng, rev));
            }
        }
        sb.append("</Reviews>");
    }

    private static StringBuffer getProductXML(KKAppEng eng, ProductIf prod) throws KKException,
            KKAppException
    {

        StringBuffer sb = new StringBuffer();
        sb.append("<Product>");

        sb.append("<id>");
        sb.append(prod.getId());
        sb.append("</id>");
        sb.append("<quantity>");
        sb.append(prod.getQuantity());
        sb.append("</quantity>");
        sb.append("<manufacturerId>");
        sb.append(prod.getManufacturerId());
        sb.append("</manufacturerId>");
        sb.append("<categoryId>");
        sb.append(prod.getCategoryId());
        sb.append("</categoryId>");
        sb.append("<viewedCount>");
        sb.append(prod.getViewedCount());
        sb.append("</viewedCount>");
        sb.append("<numberReviews>");
        sb.append(prod.getNumberReviews());
        sb.append("</numberReviews>");
        sb.append("<taxClassId>");
        sb.append(prod.getTaxClassId());
        sb.append("</taxClassId>");
        sb.append("<ordered>");
        sb.append(prod.getOrdered());
        sb.append("</ordered>");

        if (prod.getModel() != null)
        {
            sb.append("<model><![CDATA[");
            sb.append(prod.getModel());
            sb.append("]]></model>");
        }
        if (prod.getImageDir() != null && prod.getUuid() != null)
        {
            sb.append("<image><![CDATA[");
            sb.append(eng.getProdImage(prod, KKAppEng.IMAGE_MEDIUM));
            sb.append("]]></image>");
        }
        if (prod.getName() != null)
        {
            sb.append("<name><![CDATA[");
            sb.append(prod.getName());
            sb.append("]]></name>");
        }
        if (prod.getManufacturerName() != null)
        {
            sb.append("<manufacturerName><![CDATA[");
            sb.append(prod.getManufacturerName());
            sb.append("]]></manufacturerName>");
        }
        if (prod.getDescription() != null)
        {
            sb.append("<description><![CDATA[");
            sb.append(prod.getDescription());
            sb.append("]]></description>");
        }
        if (prod.getUrl() != null)
        {
            sb.append("<url><![CDATA[");
            sb.append(prod.getUrl());
            sb.append("]]></url>");
        }

        if (prod.getPriceExTax() != null)
        {
            sb.append("<priceExTax>");
            sb.append(eng.formatPrice(prod.getPriceExTax()));
            sb.append("</priceExTax>");
            /*
             * Create an unformatted price. If the special price is present, we take that, otherwise
             * we take the normal price.
             */
            sb.append("<unformattedPriceExTax>");
            if (prod.getSpecialPriceExTax() != null)
            {
                sb.append(prod.getSpecialPriceExTax());
            } else
            {
                sb.append(prod.getPriceExTax());
            }
            sb.append("</unformattedPriceExTax>");
        }
        if (prod.getSpecialPriceExTax() != null)
        {
            sb.append("<specialPriceExTax>");
            sb.append(eng.formatPrice(prod.getSpecialPriceExTax()));
            sb.append("</specialPriceExTax>");
        }
        if (prod.getPriceIncTax() != null)
        {
            sb.append("<priceIncTax>");
            sb.append(eng.formatPrice(prod.getPriceIncTax()));
            sb.append("</priceIncTax>");
            /*
             * Create an unformatted price. If the special price is present, we take that, otherwise
             * we take the normal price.
             */
            sb.append("<unformattedPriceIncTax>");
            if (prod.getSpecialPriceIncTax() != null)
            {
                sb.append(prod.getSpecialPriceIncTax());
            } else
            {
                sb.append(prod.getPriceIncTax());
            }
            sb.append("</unformattedPriceIncTax>");
        }
        if (prod.getSpecialPriceIncTax() != null)
        {
            sb.append("<specialPriceIncTax>");
            sb.append(eng.formatPrice(prod.getSpecialPriceIncTax()));
            sb.append("</specialPriceIncTax>");
        }

        if (prod.getWeight() != null)
        {
            sb.append("<weight>");
            sb.append(prod.getWeight());
            sb.append("</weight>");
        }

        if (prod.getDateAvailable() != null)
        {
            sb.append("<dateAvailable>");
            sb.append(eng.getDateAsString(prod.getDateAvailable()));
            sb.append("</dateAvailable>");
        }

        if (prod.getDateAdded() != null)
        {
            sb.append("<dateAdded>");
            sb.append(eng.getDateAsString(prod.getDateAdded()));
            sb.append("</dateAdded>");
        }

        sb.append("<status>");
        sb.append(prod.getStatus());
        sb.append("</status>");

        // Add a tag used by the UI to display or not display the product
        sb.append("<visible>true</visible>");

        /*
         * private ManufacturerIf manufacturer; private OptionIf[] opts;
         */

        sb.append("</Product>");

        return sb;
    }

    private static StringBuffer getReviewXML(KKAppEng eng, ReviewIf rev) throws KKException,
            KKAppException
    {

        StringBuffer sb = new StringBuffer();
        sb.append("<Review>");

        sb.append("<id>");
        sb.append(rev.getId());
        sb.append("</id>");
        sb.append("<languageId>");
        sb.append(rev.getLanguageId());
        sb.append("</languageId>");
        sb.append("<rating>");
        sb.append(rev.getRating());
        sb.append("</rating>");
        sb.append("<timesRead>");
        sb.append(rev.getTimesRead());
        sb.append("</timesRead>");
        sb.append("<customerId>");
        sb.append(rev.getCustomerId());
        sb.append("</customerId>");
        sb.append("<productId>");
        sb.append(rev.getProductId());
        sb.append("</productId>");

        if (rev.getCustomerName() != null)
        {
            sb.append("<customerName><![CDATA[");
            sb.append(rev.getCustomerName());
            sb.append("]]></customerName>");
        }
        if (rev.getReviewText() != null)
        {
            sb.append("<reviewText><![CDATA[");
            sb.append(rev.getReviewText());
            sb.append("]]></reviewText>");
        }
        if (rev.getLanguageName() != null)
        {
            sb.append("<languageName><![CDATA[");
            sb.append(rev.getLanguageName());
            sb.append("]]></languageName>");
        }

        if (rev.getAverageRating() != null)
        {
            sb.append("<averageRating>");
            sb.append(rev.getAverageRating());
            sb.append("</averageRating>");
        }

        if (rev.getDateAdded() != null)
        {
            sb.append("<dateAdded>");
            sb.append(eng.getDateAsString(rev.getDateAdded()));
            sb.append("</dateAdded>");
        }

        /*
         * private ProductIf product;
         */

        sb.append("</Review>");

        return sb;
    }

    private static StringBuffer getManufacturerXML(KKAppEng eng, ManufacturerIf manu)
            throws KKException, KKAppException
    {

        StringBuffer sb = new StringBuffer();
        sb.append("<Manufacturer>");

        sb.append("<id>");
        sb.append(manu.getId());
        sb.append("</id>");
        sb.append("<urlClicked>");
        sb.append(manu.getUrlClicked());
        sb.append("</urlClicked>");

        if (manu.getName() != null)
        {
            sb.append("<name><![CDATA[");
            sb.append(manu.getName());
            sb.append("]]></name>");
        }
        if (manu.getImage() != null)
        {
            sb.append("<image><![CDATA[");
            sb.append(manu.getImage());
            sb.append("]]></image>");
        }
        if (manu.getUrl() != null)
        {
            sb.append("<url><![CDATA[");
            sb.append(manu.getUrl());
            sb.append("]]></url>");
        }

        if (manu.getLastClick() != null)
        {
            sb.append("<lastClick>");
            sb.append(eng.getDateAsString(manu.getLastClick()));
            sb.append("</lastClick>");
        }

        /*
         * private ProductIf product;
         */

        sb.append("</Manufacturer>");

        return sb;
    }

    /**
     * Generate a DataDescriptor object for XML data in this format:
     * 
     * <pre>
     * {@code
     * <DataDescriptor>
     *     <limit>1000</limit>
     *     <offset>0</offset>
     *     <orderBy>ORDER_BY_NAME_ASCENDING</orderBy>
     *     <orderBy_1></orderBy_1>
     * </DataDescriptor>
     * }
     * </pre>
     * 
     * @param dataDescXML
     *            the dataDescXML
     * @return A DataDescriptor object
     */
    public static DataDescriptorIf getDataDesc(String dataDescXML)
    {
        DataDescriptor dd = new DataDescriptor();
        if (dataDescXML == null)
        {
            return dd;
        }

        String limit = getValue(dataDescXML, "limit");
        if (limit != null)
        {
            dd.setLimit(new Integer(limit).intValue());
        }
        String offset = getValue(dataDescXML, "offset");
        if (offset != null)
        {
            dd.setOffset(new Integer(offset).intValue());
        }
        String orderBy = getValue(dataDescXML, "orderBy");
        if (orderBy != null)
        {
            dd.setOrderBy(orderBy);
        }
        String orderBy1 = getValue(dataDescXML, "orderBy_1");
        if (orderBy1 != null)
        {
            dd.setOrderBy_1(orderBy1);
        }

        return dd;
    }

    private static String getValue(String xml, String elementName)
    {
        String startTag = "<" + elementName + ">";
        String endTag = "</" + elementName + ">";
        int start, end;

        start = xml.indexOf(startTag) + startTag.length();
        end = xml.indexOf(endTag);
        if (start > 0 && end > 0 && end > start)
        {
            return xml.substring(start, end);
        }

        return null;
    }

    private static String getException(String message)
    {
        return EXCEPTION_START + message + EXCEPTION_END;
    }
}
