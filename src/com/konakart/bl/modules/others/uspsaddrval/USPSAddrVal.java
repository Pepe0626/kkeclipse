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
package com.konakart.bl.modules.others.uspsaddrval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.konakart.app.AddrValidationResult;
import com.konakart.app.Address;
import com.konakart.app.KKException;
import com.konakart.appif.AddrValidationResultIf;
import com.konakart.appif.AddressIf;
import com.konakart.appif.KKEngIf;
import com.konakart.bl.modules.BaseModule;
import com.konakart.bl.modules.others.AddrValidationInterface;
import com.konakart.util.PrettyXmlPrinter;

/**
 * 
 * Used to validate US based addresses. It uses the service made available by USPS.
 * 
 */
public class USPSAddrVal extends BaseModule implements AddrValidationInterface
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(USPSAddrVal.class);

    /* CDATA control characters */
    private static final String cDataStart = "<![CDATA[";

    private static final String cDataEnd = "]]>";

    private String url;

    private String userId;

    private boolean available;

    private String addrTemplate = null;

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @throws KKException
     *            an unexpected KKException exception
     */
    public USPSAddrVal(KKEngIf eng) throws KKException
    {
        super.init(eng);
        url = eng.getConfigurationValue("MODULE_OTHER_USPS_ADDR_VAL_URL");
        userId = eng.getConfigurationValue("MODULE_OTHER_USPS_ADDR_VAL_USERID");
        available = eng.getConfigurationValueAsBool("MODULE_OTHER_USPS_ADDR_VAL_STATUS", false);
    }

    /**
     * Called to validate an address
     * 
     * @param addr
     *            the addr
     * @return Returns an AddrValidationResult object
     * @throws Exception an unexpected exception
     */
    public AddrValidationResultIf validateAddress(AddressIf addr) throws Exception
    {
        if (addr == null)
        {
            return null;
        }
        String req = getRequestURL(addr);
        String resp = sendRequest(req);
        AddrValidationResultIf ret = getRetAddr(resp);

        // Create a formatted address
        ret.getAddr().setAddressFormatTemplate(getAddrTemplate(addr.getAddressFormatId()));
        ((Address) ret.getAddr()).createFormattedAddresses();
        ret.getAddr().setFormattedAddress(removeCData(ret.getAddr().getFormattedAddress()));

        return ret;
    }

    /**
     * Send the request to USPS and get back a result
     * 
     * @param request
     * @return Return the result from USPS
     * @throws IOException
     */
    private String sendRequest(String request) throws IOException
    {

        BufferedReader rd = null;
        StringBuffer sbReply = new StringBuffer();
        HttpURLConnection conn = null;
        try
        {
            URL url = new URL(request);

            // Send data
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(60000); // Timeout after a minute

            // Get the response
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null)
            {
                sbReply.append(line);
            }
            rd.close();
        } catch (Exception e)
        {
            if (conn != null)
            {
                int respCode = conn.getResponseCode();
                String respMsg = conn.getResponseMessage();
                log.error(
                        "Exception received from USPS Address validation. \nHTTP Response Code = "
                                + respCode + "\nResponse Message =" + respMsg, e);
            } else
            {
                log.error("Exception received from USPS Address validation.", e);
            }

            if (rd != null)
            {
                rd.close();
            }
        }

        if (log.isDebugEnabled())
        {
            try
            {
                log.debug("Response from USPS =\n" + PrettyXmlPrinter.printXml(sbReply.toString()));
            } catch (Exception e)
            {
                log.error("Problem parsing the XML response from USPS", e);
                log.debug(sbReply.toString());
            }
        }

        return sbReply.toString();
    }

    /**
     * Constructs the request
     * 
     * @param addr
     * @return Returns the request in XML format
     * @throws UnsupportedEncodingException
     */
    private String getRequestURL(AddressIf addr) throws UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();

        // <AddressValidateRequest USERID="yourID">
        sb.append("<AddressValidateRequest USERID=\"");
        sb.append(userId);
        sb.append("\"><Address ID=\"0\">");
        // <FirmName>
        sb.append("<FirmName>");
        sb.append((addr.getCompany() == null) ? "" : addr.getCompany());
        sb.append("</FirmName>");
        // <Address1>
        sb.append("<Address1>");
        //sb.append((addr.getStreetAddress1());
        sb.append("</Address1>");
        // <Address2>
        sb.append("<Address2>");
        sb.append(addr.getStreetAddress());
        sb.append("</Address2>");
        // <City>
        sb.append("<City>");
        sb.append(addr.getCity());
        sb.append("</City>");
        // <State>
        sb.append("<State>");
        sb.append(addr.getState());
        sb.append("</State>");
        // <Zip5>
        sb.append("<Zip5>");
        if (addr.getPostcode() != null && addr.getPostcode().length() >= 5)
        {
            sb.append(addr.getPostcode().substring(0, 5));
        }
        sb.append("</Zip5>");
        // <Zip4>
        sb.append("<Zip4>");
        if (addr.getPostcode() != null && addr.getPostcode().length() == 10)
        {
            sb.append(addr.getPostcode().substring(6, 10));
        }
        sb.append("</Zip4>");
        sb.append("</Address></AddressValidateRequest>");

        if (log.isDebugEnabled())
        {
            try
            {
                log.debug("Request Sent to USPS at " + url + "?API=Verify&XML=" + "\n"
                        + PrettyXmlPrinter.printXml(sb.toString()));
            } catch (Exception e)
            {
                log.error("Problem parsing the XML to send to USPS", e);
                log.debug(sb.toString());
            }
        }

        String fullURL = url + "?API=Verify&XML=" + URLEncoder.encode(sb.toString(), "UTF-8");

        return fullURL;
    }

    /**
     * Constructs an address object from the return XML
     * 
     * @param retXML
     * @return Returns an address object
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws KKException
     */
    private AddrValidationResultIf getRetAddr(String retXML) throws ParserConfigurationException,
            SAXException, IOException, KKException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(retXML)));
        AddrValidationResultIf ret = new AddrValidationResult();
        Address addr = new Address();
        ret.setAddr(addr);
        traverse(doc, ret);
        return ret;
    }

    /**
     * Based on the node type we decide what to do
     * 
     * @param cNode
     * @param addr
     * @throws KKException
     */
    private void traverse(Node cNode, AddrValidationResultIf ret) throws KKException
    {
        switch (cNode.getNodeType())
        {
        case Node.DOCUMENT_NODE:
            processChildren(cNode.getChildNodes(), ret);
            break;
        case Node.ELEMENT_NODE:
            if (cNode.getNodeName().equalsIgnoreCase("Error"))
            {
                ret.setError(true);
            }
            processChildren(cNode.getChildNodes(), ret);
            break;
        case Node.CDATA_SECTION_NODE:
        case Node.TEXT_NODE:
            if (!cNode.getNodeValue().trim().equals(""))
            {
                processNode(cNode, ret);
            }
            break;
        }
    }

    /**
     * Process the child nodes of the node passed in as a parameter
     * 
     * @param nList
     * @param addr
     * @throws KKException
     */
    private void processChildren(NodeList nList, AddrValidationResultIf ret) throws KKException
    {
        if (nList.getLength() != 0)
        {
            for (int i = 0; i < nList.getLength(); i++)
                traverse(nList.item(i), ret);
        }
    }

    /**
     * Process the individual node
     * 
     * @param cNode
     * @param addr
     * @throws KKException
     */
    private void processNode(Node cNode, AddrValidationResultIf ret) throws KKException
    {
        String name = cNode.getParentNode().getNodeName();
        if (name.equals("FirmName"))
        {
            ret.getAddr().setCompany(cNode.getNodeValue());
        } else if (name.equals("Address1"))
        {
            // Not used
        } else if (name.equals("Address2"))
        {
            ret.getAddr().setStreetAddress(cNode.getNodeValue());
        } else if (name.equals("City"))
        {
            ret.getAddr().setCity(cNode.getNodeValue());
        } else if (name.equals("State"))
        {
            ret.getAddr().setState(cNode.getNodeValue());
        } else if (name.equals("Zip5"))
        {
            if (ret.getAddr().getPostcode() == null)
            {
                ret.getAddr().setPostcode(cNode.getNodeValue());
            } else
            {
                ret.getAddr().setPostcode(cNode.getNodeValue() + "-" + ret.getAddr().getPostcode());
            }
        } else if (name.equals("Zip4"))
        {
            if (ret.getAddr().getPostcode() == null)
            {
                ret.getAddr().setPostcode(cNode.getNodeValue());
            } else
            {
                ret.getAddr().setPostcode(ret.getAddr().getPostcode() + "-" + cNode.getNodeValue());
            }
        } else if (name.equals("ReturnText"))
        {
            ret.setMessage(cNode.getNodeValue());
        } else if (name.equals("Description"))
        {
            ret.setMessage(cNode.getNodeValue());
        }
    }

    /**
     * @return Returns true if the service is available
     */
    public boolean isAvailable()
    {
        return available;
    }

    /**
     * @param addressFormatId
     * @return the addrTemplate
     * @throws KKException
     */
    private String getAddrTemplate(int addressFormatId) throws KKException
    {
        if (addrTemplate == null)
        {
            addrTemplate = getEng().getAddressFormatTemplate(addressFormatId);
        }
        return addrTemplate;
    }

    /**
     * Returns the string passed in as inString without the CData information. i.e. It removes
     * '<![CDATA[' from the start of the string and ']]>' from the end of the string
     * 
     * @param inString
     * @return inString without the CDATA info
     */
    private String removeCData(String inString)
    {
        String stringIn = inString;

        if (stringIn == null || stringIn.length() < cDataStart.length() + cDataEnd.length())
        {
            return stringIn;
        }

        if (stringIn.substring(0, cDataStart.length()).equals(cDataStart))
        {
            stringIn = stringIn.substring(cDataStart.length());
        }

        if (stringIn.substring(stringIn.length() - cDataEnd.length(), stringIn.length()).equals(
                cDataEnd))
        {
            stringIn = stringIn.substring(0, stringIn.length() - cDataEnd.length());
        }
        return stringIn;
    }

}
