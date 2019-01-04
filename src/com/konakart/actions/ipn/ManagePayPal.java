//
// (c) 2018 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is free software; you can redistribute 
// it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This software is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package com.konakart.actions.ipn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.al.KKAppEng;
import com.konakart.app.EmailOptions;
import com.konakart.app.IpnHistory;
import com.konakart.app.KKException;
import com.konakart.app.NameValue;
import com.konakart.app.OrderUpdate;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.IpnHistoryIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.appif.SSOTokenIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.bl.modules.payment.paypal.Paypal;

/**
 * This class is an Action class for what to do when a payment notification callback is received
 * from PayPal.
 */
public class ManagePayPal implements Runnable
{
    /**
     * The <code>Log</code> instance for this application.
     */
    protected Log log = LogFactory.getLog(ManagePayPal.class);

    // Module name must be the same as the class name although it can be all in lowercase in order
    // to remain compatible with osCommerce.
    private static String code = "paypal";

    // PayPal constants
    private static final String payment_status = "payment_status";

    private static final String txn_id = "txn_id";

    private static final String mc_gross = "mc_gross";

    private static final String completed = "Completed";

    // Return codes and descriptions
    private static final int RET0 = 0;

    private static final String RET0_DESC = "Transaction OK";

    private static final int RET4 = -4;

    private static final String RET4_DESC = "There has been an unexpected exception. Please look at the log. ";

    // Order history comments. These comments are associated with the order.
    private static final String ORDER_HISTORY_COMMENT_OK = "PayPal payment successful. PayPal TransactionId = ";

    private static final String ORDER_HISTORY_COMMENT_KO = "PayPal payment not successful. PayPal Payment Status = ";

    private static final String ORDER_HISTORY_COMMENT_NO_PAY_MATCH = "PayPal payment not successful. The amount paid is less than the amount requested. PayPal TransactionId = ";

    private KKAppEng kkAppEng = null;

    private SSOTokenIf token = null;

    private StringBuffer verifyPayload;

    private ArrayList<NameValue> parmList;

    /**
     * Constructor
     * 
     * @param _kkAppEng
     * @param _token
     * @param _verifyPayload
     * @param _parmList
     */
    public ManagePayPal(KKAppEng _kkAppEng, SSOTokenIf _token, StringBuffer _verifyPayload,
            ArrayList<NameValue> _parmList)
    {
        kkAppEng = _kkAppEng;
        token = _token;
        verifyPayload = _verifyPayload;
        parmList = _parmList;
    }

    public void run()
    {
        if (log.isDebugEnabled())
        {
            log.debug("*********** PayPal Callback Thread");
        }

        String paymentStatus = null, txnId = null;

        // Create these outside of try / catch since they are needed in the case of a general
        // exception
        IpnHistoryIf ipnHistory = new IpnHistory();
        ipnHistory.setOrderId(-1);
        ipnHistory.setModuleCode(code);

        String sessionId = null;

        try
        {
            /*
             * Get a session id for the customer and use it to initialise kkAppEng
             */
            NameValue nvToken = new NameValue(token.getSecretKey(), token.getSecretKey());
            PaymentDetailsIf pd = kkAppEng.getEng().getPaymentDetailsCustom(/* sessionId */null,
                    "paypal", /* parameters */new NameValue[]
                    { nvToken });
            if (pd == null || pd.getCustom1() == null || pd.getCustom1().length() == 0)
            {
                throw new KKException(
                        "Unexpected reply from PayPal payment module. Unable to retrieve session id");
            }

            // Log in the user
            kkAppEng.getCustomerMgr().loginBySession(pd.getCustom1());
            sessionId = pd.getCustom1();

            try
            {
                // Get the order id from custom1
                int orderId = Integer.parseInt(token.getCustom1());
                ipnHistory.setOrderId(orderId);
            } catch (Exception e)
            {
                throw new KKException("The SSOToken does not contain an order id in custom1");
            }

            BigDecimal orderTotal = null;
            try
            {
                // Get the order total from custom2
                orderTotal = new BigDecimal(token.getCustom2());
            } catch (Exception e)
            {
                throw new KKException("The SSOToken does not contain an order total in custom2");
            }

            boolean testMode = kkAppEng.getConfigAsBoolean(Paypal.MODULE_PAYMENT_PAYPAL_TEST_MODE,
                    true);
            String response = postVerificationData(verifyPayload, testMode);

            if (log.isDebugEnabled())
            {
                log.debug("Verify response  = " + response);
            }

            if (response == null || !response.equalsIgnoreCase("VERIFIED"))
            {
                ipnHistory.setGatewayFullResponse(verifyPayload.toString());
                throw new KKException("The IPN data received from PayPal did not verify");
            }

            // See if we need to send an email, by looking at the configuration
            String sendEmailsConfig = kkAppEng.getConfig(ConfigConstants.SEND_EMAILS);
            boolean sendEmail = false;
            if (sendEmailsConfig != null && sendEmailsConfig.equalsIgnoreCase("true"))
            {
                sendEmail = true;
            }

            // Process the parameters sent in the callback
            boolean amountPaidMatch = false;
            StringBuffer sb = new StringBuffer();
            for (Iterator<NameValue> iterator = parmList.iterator(); iterator.hasNext();)
            {
                NameValue nv = iterator.next();
                if (sb.length() > 0)
                {
                    sb.append("\n");
                }
                sb.append(nv.getName());
                sb.append(" = ");
                sb.append(nv.getValue());

                // Capture important variables so that we can determine whether the transaction
                // was successful or not
                if (nv.getName() != null)
                {
                    if (nv.getName().equalsIgnoreCase(ManagePayPal.payment_status))
                    {
                        paymentStatus = nv.getValue();
                    } else if (nv.getName().equalsIgnoreCase(ManagePayPal.txn_id))
                    {
                        txnId = nv.getValue();
                    } else if (nv.getName().equalsIgnoreCase(ManagePayPal.mc_gross))
                    {
                        try
                        {
                            ipnHistory.setTxAmount(new BigDecimal(nv.getValue()));
                        } catch (Exception e)
                        {
                            // Try again in case it uses a comma instead of a decimal point
                            if (nv.getValue() != null)
                            {
                                nv.setValue(nv.getValue().replaceAll(",", "."));
                                try
                                {
                                    ipnHistory.setTxAmount(new BigDecimal(nv.getValue()));
                                } catch (Exception e1)
                                {
                                    log.error(
                                            "mc_gross returned from PayPal cannot be converted to a BigDecimal : "
                                                    + nv.getValue());
                                }
                            }
                        }

                        /*
                         * Verify that the amount paid matches the orderTotal retrieved from the SSO
                         * token
                         */
                        if (ipnHistory.getTxAmount() == null
                                || !orderTotal.equals(ipnHistory.getTxAmount()))
                        {
                            log.error("Amount paid received by PayPal = " + ipnHistory.getTxAmount()
                                    + " doesn't match the amount of the order = "
                                    + orderTotal.toString());
                        } else
                        {
                            amountPaidMatch = true;
                        }
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("PayPal CallBack data:\n");
                log.debug(sb.toString());
            }

            /*
             * Discard duplicate messages with the same transaction id and payment status
             */
            if (txnId != null)
            {
                IpnHistoryIf[] ipnHistoryArray = kkAppEng.getEng().getIpnHistory(sessionId,
                        ipnHistory.getOrderId());
                if (ipnHistoryArray != null && ipnHistoryArray.length > 0)
                {
                    for (int i = 0; i < ipnHistoryArray.length; i++)
                    {
                        IpnHistoryIf ipn = ipnHistoryArray[i];

                        String previousTxnId = ipn.getGatewayTransactionId();
                        String previousStatus = ipn.getGatewayResult();
                        if (compare(previousTxnId, txnId) && compare(previousStatus, paymentStatus))
                        {
                            log.error("Received a duplicate IPN from PayPal for\nOrder ID = "
                                    + ipnHistory.getOrderId() + "\nTransactionId = " + txnId
                                    + "\nPayment Status = " + paymentStatus);
                            return;
                        }
                    }
                }
            }

            // Fill more details of the IPN history class
            ipnHistory.setGatewayResult(paymentStatus);
            ipnHistory.setGatewayFullResponse(sb.toString());
            ipnHistory.setGatewayTransactionId(txnId);

            // If successful, we update the inventory as well as changing the state of the
            // order.
            OrderUpdateIf updateOrder = new OrderUpdate();
            updateOrder.setUpdatedById(kkAppEng.getActiveCustId());

            String comment = null;
            if (paymentStatus != null && paymentStatus.equalsIgnoreCase(completed)
                    && !amountPaidMatch)
            {
                String msg = " - FAILED: received " + ipnHistory.getTxAmount() + " instead of "
                        + orderTotal;
                ipnHistory.setKonakartResultDescription(RET0_DESC + msg);
                comment = ORDER_HISTORY_COMMENT_NO_PAY_MATCH + txnId;
                kkAppEng.getEng().updateOrder(sessionId, ipnHistory.getOrderId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, sendEmail, comment,
                        updateOrder);
                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, ipnHistory.getOrderId(), /* success */
                            false);
                }
            } else if (paymentStatus != null && paymentStatus.equalsIgnoreCase(completed))
            {
                comment = ORDER_HISTORY_COMMENT_OK + txnId;
                kkAppEng.getEng().updateOrder(sessionId, ipnHistory.getOrderId(),
                        com.konakart.bl.OrderMgr.PAYMENT_RECEIVED_STATUS, sendEmail, comment,
                        updateOrder);
                // If the order payment was approved we update the inventory
                kkAppEng.getEng().updateInventory(sessionId, ipnHistory.getOrderId());
                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, ipnHistory.getOrderId(), /* success */
                            true);
                }
                ipnHistory.setKonakartResultDescription(RET0_DESC);
            } else
            {
                comment = ORDER_HISTORY_COMMENT_KO + paymentStatus;
                kkAppEng.getEng().updateOrder(sessionId, ipnHistory.getOrderId(),
                        com.konakart.bl.OrderMgr.PAYMENT_DECLINED_STATUS, sendEmail, comment,
                        updateOrder);
                if (sendEmail)
                {
                    sendOrderConfirmationMail(kkAppEng, ipnHistory.getOrderId(), /* success */
                            false);
                }
                ipnHistory.setKonakartResultDescription(RET0_DESC);
            }

            ipnHistory.setKonakartResultId(RET0);
            kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);

            return;

        } catch (Exception e)
        {
            try
            {
                if (sessionId != null)
                {
                    String msg = "";
                    if (e.getClass().getName().equalsIgnoreCase("com.konakart.app.KKException"))
                    {
                        msg = e.getMessage();
                    }
                    ipnHistory.setKonakartResultDescription(RET4_DESC + msg);
                    ipnHistory.setKonakartResultId(RET4);
                    if (kkAppEng != null)
                    {
                        kkAppEng.getEng().saveIpnHistory(sessionId, ipnHistory);
                    }
                }
            } catch (KKException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return;
        } finally
        {
            if (sessionId != null && kkAppEng != null)
            {
                try
                {
                    kkAppEng.getEng().logout(sessionId);
                } catch (KKException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends a request to PayPal to verify that the IPN message actually came from PayPal
     * 
     * @param payload
     * @param testMode
     * @return Returns the PayPal response
     * @throws IOException
     */
    public String postVerificationData(StringBuffer payload, boolean testMode) throws IOException
    {
        String urlStr = null;
        if (testMode)
        {
            urlStr = "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr";
        } else
        {
            urlStr = "https://ipnpb.paypal.com/cgi-bin/webscr";
        }
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        PrintWriter out = new PrintWriter(connection.getOutputStream());

        // Send the message
        out.print(payload.toString());
        out.close();

        // Get back the response
        StringBuffer respSb = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = in.readLine();

        while (line != null)
        {
            respSb.append(line);
            line = in.readLine();
        }

        in.close();

        return respSb.toString();
    }

    /**
     * Utility to compare strings that could be null
     * 
     * @param str1
     *            String 1
     * @param str2
     *            String 2
     * @return true if the Strings are the same
     */
    private boolean compare(String str1, String str2)
    {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

    /**
     * Send an order confirmation eMail. The template used is different if the order is successful
     * or not.
     * 
     * @param kkAppEng
     *            the kkAppEng
     * @param orderId
     *            the orderId
     * @param success
     *            the success
     * @throws KKException
     *             an unexpected KKException exception
     */
    protected void sendOrderConfirmationMail(KKAppEng kkAppEng, int orderId, boolean success)
            throws KKException
    {
        String countryCode = kkAppEng.getLocale().substring(0, 2);

        sendOrderConfirmationMail(kkAppEng.getEng(), kkAppEng.getSessionId(), countryCode, orderId,
                success);
    }

    /**
     * Send an order confirmation eMail. The template used is different if the order is successful
     * or not.
     * 
     * @param eng
     *            the eng
     * @param sessionId
     *            the sessionId
     * @param countryCode
     *            the countryCode
     * @param orderId
     *            the orderId
     * @param success
     *            the success
     * @throws KKException
     *             an unexpected KKException exception
     */
    protected void sendOrderConfirmationMail(KKEngIf eng, String sessionId, String countryCode,
            int orderId, boolean success) throws KKException
    {
        EmailOptionsIf options = new EmailOptions();

        // Default behaviour is not to create or attach the PDF invoice
        options.setCreateInvoice(false);
        options.setAttachInvoice(false);

        options.setCountryCode(countryCode);

        if (success)
        {
            options.setTemplateName("OrderConfPaymentSuccess");

            // Attach the invoice to the confirmation email (Enterprise Only). Defaults to false.
            // options.setAttachInvoice(true);

            // Create the invoice (if not already present) for attaching to the confirmation email
            // (Enterprise Only). Defaults to false.
            // options.setCreateInvoice(true);

        } else
        {
            options.setTemplateName("OrderConfPaymentFailure");
        }

        eng.sendOrderConfirmationEmail1(sessionId, orderId, /* langIdForOrder */
                -1, options);
    }

}
