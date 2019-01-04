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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.al.KKAppEng.EngineData;
import com.konakart.app.AddToBasketOptions;
import com.konakart.app.Basket;
import com.konakart.app.CreateOrderOptions;
import com.konakart.app.DataDescConstants;
import com.konakart.app.DataDescriptor;
import com.konakart.app.EmailOptions;
import com.konakart.app.FetchOrderOptions;
import com.konakart.app.KKException;
import com.konakart.app.Order;
import com.konakart.app.OrderSearch;
import com.konakart.app.OrderStatusHistory;
import com.konakart.app.OrderTotal;
import com.konakart.app.OrderUpdate;
import com.konakart.app.SaveOrderOptions;
import com.konakart.appif.AddToBasketOptionsIf;
import com.konakart.appif.AddressIf;
import com.konakart.appif.BasketIf;
import com.konakart.appif.CreateOrderOptionsIf;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.FetchOrderOptionsIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.OrderProductIf;
import com.konakart.appif.OrderSearchIf;
import com.konakart.appif.OrderStatusHistoryIf;
import com.konakart.appif.OrderTotalIf;
import com.konakart.appif.OrderUpdateIf;
import com.konakart.appif.OrdersIf;
import com.konakart.appif.PaymentDetailsIf;
import com.konakart.appif.ProductIf;
import com.konakart.appif.ShippingQuoteIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.util.KKConstants;
import com.konakart.util.Utils;

/**
 * Contains methods to manage all stages of order creation.
 */
public class OrderMgr extends BaseMgr
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(OrderMgr.class);

    // Result set navigation constants
    private static final String navNext = "next";

    private static final String navBack = "back";

    private static final String navStart = "start";

    // Order Information

    private final OrderIf[] emptyOrderArray = new OrderIf[0];

    private OrderIf[] currentOrders;

    private OrderIf selectedOrder;

    private OrderIf checkoutOrder;

    private int totalNumberOfOrders;

    private ShippingQuoteIf[] shippingQuotes;

    private HashMap<String, ShippingQuoteIf[]> vendorShippingQuoteMap = new HashMap<String, ShippingQuoteIf[]>();

    private PaymentDetailsIf[] paymentDetailsArray;

    // Data descriptor info
    private DataDescriptorIf dataDesc = new DataDescriptor();

    private int currentOffset;

    private int currentPage;

    private int showNext;

    private int showBack;

    private ArrayList<Integer> pageList = new ArrayList<Integer>();

    // Server name and port for saving the order
    private String hostAndPort;

    // Boolean to pass to GWT one page checkout code
    private boolean useCheckoutOrder = false;

    // Coupon code entered by customer
    private String couponCode = null;

    // Gift Certificate code entered by customer
    private String giftCertCode = null;

    // Reward Points entered by customer
    private int rewardPoints = 0;

    // Max rows defined by the user
    private int maxRowsUser = 0;

    // Total number of pages in a result set
    private int numPages = 0;

    /** The object containing all of the static data for this instance of the Category Manager */
    private StaticData sd = null;

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     */
    protected OrderMgr(KKEngIf eng, KKAppEng kkAppEng)
    {
        this.eng = eng;
        this.kkAppEng = kkAppEng;
        sd = staticDataHM.get(kkAppEng.getStoreId());
        if (sd == null)
        {
            sd = new StaticData();
            staticDataHM.put(kkAppEng.getStoreId(), sd);
        }
        this.reset();
    }

    /**
     * Is the Manager Ready?
     * 
     * @return true if this manager is ready for work, otherwise returns false.
     */
    public boolean isMgrReady()
    {
        return sd.isMgrReady();
    }

    /**
     * Refresh the configuration variables. This is called automatically at a regular interval.
     * 
     */
    public void refreshConfigs()
    {
        if (log.isDebugEnabled())
        {
            log.debug("OrderMgr refreshConfigs");
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_PAGE_LINKS) != null)
        {
            sd.setMaxPageLinks(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_PAGE_LINKS))
                            .intValue());
        } else
        {
            sd.setMaxPageLinks(5);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_ORDER_HISTORY) != null)
        {
            sd.setMaxRows(new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_ORDER_HISTORY))
                    .intValue());
        } else
        {
            sd.setMaxRows(10);
        }

        String conf = kkAppEng.getConfig(ConfigConstants.SEND_EMAILS);
        if (conf != null)
        {
            if (conf.equalsIgnoreCase("true"))
            {
                sd.setSendEmails(true);
            } else
            {
                sd.setSendEmails(false);
            }
        }

        conf = kkAppEng.getConfig(ConfigConstants.SEND_ORDER_CONF_EMAIL);
        if (conf != null)
        {
            if (conf.equalsIgnoreCase("true"))
            {
                sd.setSendOrderConfEmails(true);
            } else
            {
                sd.setSendOrderConfEmails(false);
            }
        }

        // Now the manager is ready for action
        sd.setMgrReady(true);
    }

    /**
     * Puts the OrderContainer object back into it's original state with no products selected
     */
    public void reset()
    {
        currentOrders = emptyOrderArray;
        selectedOrder = new Order();
        currentOffset = 0;
        showNext = 0;
        showBack = 0;
        currentPage = 1;
        this.initDataDesc();
    }

    /**
     * Initialise the data descriptor object with a zero offset and max rows set.
     * 
     */
    private void initDataDesc()
    {
        // We always get an extra row in order to determine whether to show the
        // next link
        dataDesc.setLimit(getPageSize() + 1);
        dataDesc.setOffset(0);
        dataDesc.setOrderBy(DataDescConstants.ORDER_BY_ID_DESCENDING);
    }

    /**
     * This method is called to navigate through a list of orders when the list is longer than
     * maxRows. The CurrentOrders array is updated with the new orders.<br>
     * <code>navDir</code> can take the following values which are retrieved using getter methods on
     * the OrderMgr instance:
     * <ul>
     * <li>getNavNext()</li>
     * <li>getNavBack()</li>
     * <li>getNavStart()</li>
     * </ul>
     * 
     * @param navDir
     *            the navDir
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void navigateCurrentOrders(String navDir) throws KKException, KKAppException
    {
        setDataDescOffset(navDir);

        fetchAllOrders();
    }

    /**
     * Calls the engine to get an array of OrderTotal objects which are added to the checkoutOrder.
     * 
     * @throws Exception an unexpected exception
     */
    public void populateCheckoutOrderWithOrderTotals() throws Exception
    {
        if (checkoutOrder != null)
        {
            if (log.isInfoEnabled())
            {
                log.info("Create/Update checkoutOrder by calling getOrderTotals for "
                        + checkoutOrder.getLifecycleId());
            }

            checkoutOrder = kkAppEng.getEng().getOrderTotals(checkoutOrder, kkAppEng.getLangId());

            if (log.isDebugEnabled())
            {
                String msg = "After calling getEng().getOrderTotals checkoutOrder: "
                        + checkoutOrder.getLifecycleId();
                if (checkoutOrder.getOrderTotals() == null)
                {
                    msg += " NULL OrderTotals";
                } else
                {
                    msg += " " + checkoutOrder.getOrderTotals().length + " OrderTotals";
                }
                log.debug(msg);
            }
        }

        // Populate vendor orders with order totals
        if (kkAppEng.isMultiVendor() && checkoutOrder.getVendorOrders() != null)
        {
            BigDecimal tax = null;
            int taxSortOrder = 0;
            ArrayList<Thread> threads = new ArrayList<Thread>();
            for (int i = 0; i < checkoutOrder.getVendorOrders().length; i++)
            {
                OrderIf order = checkoutOrder.getVendorOrders()[i];
                OrderTotalFetcher otFetcherThread = new OrderTotalFetcher(
                        kkAppEng.getStoreEng(order.getStoreId()).getEng(), checkoutOrder, i,
                        kkAppEng.getLangId());
                otFetcherThread.setDaemon(true);
                otFetcherThread.start();
                threads.add(otFetcherThread);
            }

            // Wait for threads to finish
            for (Thread thread : threads)
            {
                thread.join();
            }

            // Calculate the total amount of tax to put on the parent order
            for (int i = 0; i < checkoutOrder.getVendorOrders().length; i++)
            {
                OrderIf order = checkoutOrder.getVendorOrders()[i];
                if (order.getOrderTotals() != null && order.getOrderTotals().length > 0)
                {
                    for (int j = 0; j < order.getOrderTotals().length; j++)
                    {
                        OrderTotalIf ot = order.getOrderTotals()[j];
                        if (kkAppEng.isTaxModule(ot.getClassName()))
                        {
                            tax = (tax == null) ? new BigDecimal(0) : tax;
                            tax = tax.add(ot.getValue());
                            taxSortOrder = ot.getSortOrder();
                            break;
                        }
                    }
                }
            }

            /*
             * We need to add the total tax to the main order. If the main order has already had tax
             * added to it we must subtract the tax from the total. We add a new tax ot which is the
             * sum of taxes from all of the vendor orders and we add this tax to the total.
             */
            if (tax != null)
            {
                if (checkoutOrder.getOrderTotals() != null
                        && checkoutOrder.getOrderTotals().length > 0)
                {
                    boolean addedTax = false;
                    ArrayList<OrderTotalIf> otList = new ArrayList<OrderTotalIf>();
                    for (int i = 0; i < checkoutOrder.getOrderTotals().length; i++)
                    {
                        OrderTotalIf ot = checkoutOrder.getOrderTotals()[i];
                        if ((taxSortOrder <= ot.getSortOrder()) && !addedTax)
                        {
                            OrderTotalIf taxOT = new OrderTotal();
                            taxOT.setValue(tax);
                            taxOT.setTitle(kkAppEng.getMsg("common.tax") + ":");
                            taxOT.setClassName("ot_tax");
                            taxOT.setText(
                                    kkAppEng.formatPrice(tax, checkoutOrder.getCurrencyCode()));
                            taxOT.setSortOrder(taxSortOrder);
                            otList.add(taxOT);
                            addedTax = true;
                        }
                        if (ot.getClassName().equals("ot_total"))
                        {
                            if (checkoutOrder.getTax() != null)
                            {
                                ot.setValue(ot.getValue().subtract(checkoutOrder.getTax()));
                            }
                            ot.setValue(ot.getValue().add(tax));
                            ot.setText(kkAppEng.formatPrice(ot.getValue(),
                                    checkoutOrder.getCurrencyCode()));
                            otList.add(ot);
                        } else
                        {
                            otList.add(ot);
                        }
                    }
                    if (log.isInfoEnabled())
                    {
                        String msg = "Add OrderTotals to checkoutOrder (LifecycleId: "
                                + checkoutOrder.getLifecycleId() + " OrderNumber:"
                                + checkoutOrder.getOrderNumber() + ")";
                        if (otList.isEmpty())
                        {
                            msg += "\n\t\t No OrderTotals to add to checkoutOrder";
                        } else
                        {
                            int maxClassName = -1;
                            int maxTitle = -1;
                            for (OrderTotalIf ot : otList)
                            {
                                if (ot.getClassName() != null
                                        && ot.getClassName().length() > maxClassName)
                                {
                                    maxClassName = ot.getClassName().length();
                                }
                                if (ot.getTitle() != null && ot.getTitle().length() > maxTitle)
                                {
                                    maxTitle = ot.getTitle().length();
                                }
                            }
                            for (OrderTotalIf ot : otList)
                            {
                                msg += "\n\t\t "
                                        + Utils.padRight(ot.getClassName(), maxClassName + 2)
                                        + Utils.padRight(ot.getTitle(), maxTitle + 2)
                                        + ot.getText();
                            }
                        }
                        log.info(msg);
                    }
                    checkoutOrder.setOrderTotals(otList.toArray(new OrderTotalIf[otList.size()]));
                }
            }
        }
    }

    /**
     * Populates the currentCustomer object with the latest orders made.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * 
     */
    public void populateCustomerOrders() throws KKException
    {
        if (kkAppEng.getCustomerMgr().getCurrentCustomer() == null)
        {
            return;
        }
        DataDescriptorIf dd = new DataDescriptor();
        dd.setOffset(0);
        dd.setLimit(3);
        dd.setOrderBy(DataDescConstants.ORDER_BY_ID_DESCENDING);

        OrdersIf o = eng.searchForOrdersPerCustomer(kkAppEng.getSessionId(), dd, getOrderSearch(),
                kkAppEng.getLangId());

        kkAppEng.getCustomerMgr().getCurrentCustomer().setOrders(o.getOrderArray());
    }

    /**
     * It gets an array of orders sorted by date. They are written to the currentOrders array.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void fetchAllOrders() throws KKException
    {
        OrdersIf o = eng.searchForOrdersPerCustomer(kkAppEng.getSessionId(), dataDesc,
                getOrderSearch(), kkAppEng.getLangId());
        if (o != null)
        {
            currentOrders = o.getOrderArray();
            totalNumberOfOrders = o.getTotalNumOrders();
        }

        pageList = getPages( /* currentPage */
                currentPage);

        if (currentOrders != null)
        {
            // We always attempt to fetch back maxRows + 1
            if (currentOrders.length > getPageSize())
            {
                this.showNext = 1;
            } else
            {
                this.showNext = 0;
            }
        }

        if (currentOffset > 0)
        {
            this.showBack = 1;
        } else
        {
            this.showBack = 0;
        }
    }

    /**
     * Returns an OrderSearch object based on customer tag values
     * 
     * @return Returns an OrderSearch object based on customer tag values
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected OrderSearch getOrderSearch() throws KKException
    {
        CustomerMgr custMgr = kkAppEng.getCustomerMgr();
        if (!custMgr.isCustomerTagsAvailable())
        {
            return null;
        }
        boolean viewChildOrders = custMgr.getTagValueAsBool(KKConstants.B2B_VIEW_CHILD_ORDERS,
                false);
        boolean viewParentOrders = custMgr.getTagValueAsBool(KKConstants.B2B_VIEW_PARENT_ORDERS,
                false);
        boolean viewSiblingOrders = custMgr.getTagValueAsBool(KKConstants.B2B_VIEW_SIBLING_ORDERS,
                false);
        OrderSearch os = new OrderSearch();
        os.setIncludeParentCustomerOrders(viewParentOrders);
        os.setIncludeSiblingCustomerOrders(viewSiblingOrders);
        os.setIncludeChildrenCustomerOrders(viewChildOrders);
        os.setPopulateArchivedOrdersAttribute(true);

        String includeStatusIds = kkAppEng.getConfig(ConfigConstants.ORDER_DISPLAY_STATUS_IDS);
        if (includeStatusIds != null && includeStatusIds.length() > 0)
        {
            String[] statusIdStrArray = includeStatusIds.split(",");
            int[] statusIdIntArray = new int[statusIdStrArray.length];
            for (int i = 0; i < statusIdStrArray.length; i++)
            {
                String idStr = (statusIdStrArray[i]).trim();
                try
                {
                    int idInt = Integer.parseInt(idStr);
                    statusIdIntArray[i] = idInt;
                } catch (NumberFormatException e)
                {
                    log.warn("The order status Id " + idStr + " must be an integer");
                }
            }
            if (log.isDebugEnabled())
            {
                StringBuffer sb = new StringBuffer();
                sb.append("Only show customer orders with the following statuses: ");
                for (int i = 0; i < statusIdIntArray.length; i++)
                {
                    if (i > 0)
                    {
                        sb.append(",");
                    }
                    int id = statusIdIntArray[i];
                    sb.append(id);
                }
                log.debug(sb);
            }
            os.setIncludeStatusIds(statusIdIntArray);
        }
        String excludeStatusIds = kkAppEng
                .getConfig(ConfigConstants.ORDER_DISPLAY_EXCLUDE_STATUS_IDS);
        if (excludeStatusIds != null && excludeStatusIds.length() > 0)
        {
            String[] statusIdStrArray = excludeStatusIds.split(",");
            int[] statusIdIntArray = new int[statusIdStrArray.length];
            for (int i = 0; i < statusIdStrArray.length; i++)
            {
                String idStr = (statusIdStrArray[i]).trim();
                try
                {
                    int idInt = Integer.parseInt(idStr);
                    statusIdIntArray[i] = idInt;
                } catch (NumberFormatException e)
                {
                    log.warn("The order status Id " + idStr + " must be an integer");
                }
            }
            if (log.isDebugEnabled())
            {
                StringBuffer sb = new StringBuffer();
                sb.append("Do not show customer orders with the following statuses: ");
                for (int i = 0; i < statusIdIntArray.length; i++)
                {
                    if (i > 0)
                    {
                        sb.append(",");
                    }
                    int id = statusIdIntArray[i];
                    sb.append(id);
                }
                log.debug(sb);
            }
            os.setExcludeStatusIds(statusIdIntArray);
        }

        return os;
    }

    /**
     * Returns an FetchOrderOptions object based on customer tag values
     * 
     * @return Returns an FetchOrderOptions object based on customer tag values
     * @throws KKException
     *            an unexpected KKException exception
     */
    public FetchOrderOptionsIf getFetchOrderOptions() throws KKException
    {
        CustomerMgr custMgr = kkAppEng.getCustomerMgr();
        if (!custMgr.isCustomerTagsAvailable())
        {
            return null;
        }
        boolean viewChildOrders = custMgr.getTagValueAsBool(KKConstants.B2B_VIEW_CHILD_ORDERS,
                false);
        boolean viewParentOrders = custMgr.getTagValueAsBool(KKConstants.B2B_VIEW_PARENT_ORDERS,
                false);
        boolean viewSiblingOrders = custMgr.getTagValueAsBool(KKConstants.B2B_VIEW_SIBLING_ORDERS,
                false);
        FetchOrderOptions foo = new FetchOrderOptions();
        foo.setIncludeParentCustomerOrders(viewParentOrders);
        foo.setIncludeSiblingCustomerOrders(viewSiblingOrders);
        foo.setIncludeChildrenCustomerOrders(viewChildOrders);
        foo.setPopulateArchivedOrdersAttribute(true);
        return foo;
    }

    /**
     * Calls the engine to save the order. The state of the order is set before saving and an email
     * is sent if KonaKart has been configured to send emails.<br>
     * 
     * @deprecated Since version 2.2.0.6. hostAndPort is no longer used and the sending of email
     *             cannot be controlled from this method. Use saveOrder(boolean sendEmail) instead.
     * 
     * @param _hostAndPort
     *            (i.e. my_server:8970)
     * @return The id of the new order
     * @throws Exception an unexpected exception
     */
    @Deprecated
    public int saveOrder(String _hostAndPort) throws Exception
    {
        return saveOrder(true);
    }

    /**
     * Calls the engine to save the order. The state of the order is set before saving. If KonaKart
     * has been configured to send emails and sendEmail is set to true, an email is sent using a
     * template called <code>OrderConfReceived</code>.<br>
     * 
     * @deprecated Use <code>saveOrder(boolean sendEmail, EmailOptionsIf options)</code> instead.
     *             This method gives you more flexibility and allows you to specify whether you want
     *             to attach an invoice to the mail.
     * 
     * @param sendEmail
     *            Set to true to send an email to the customer
     * @return The id of the new order
     * @throws Exception an unexpected exception
     */
    @Deprecated
    public int saveOrder(boolean sendEmail) throws Exception
    {
        return saveOrder(sendEmail, null);
    }

    /**
     * Calls the engine to save the order. The state of the order is set before saving. If KonaKart
     * has been configured to send emails and sendEmail is set to true, an email is sent using the
     * options defined in the EmailOptions object passed in as a parameter. This object allows you
     * to set the template name, country code and decide whether an invoice should be attached to
     * the mail.
     * 
     * @param sendEmail
     *            Set to true to send an email to the customer
     * @param _options
     *            Options for the eMail. If left null, default values will be created.
     * @return The id of the new order
     * @throws Exception an unexpected exception
     */
    public int saveOrder(boolean sendEmail, EmailOptionsIf _options) throws Exception
    {
        EmailOptionsIf options = _options;
        if (checkoutOrder.getPaymentDetails() == null)
        {
            throw new KKAppException(
                    "The order cannot be saved since there are no payment details associated with the order.");
        }

        if (log.isWarnEnabled())
        {
            if (checkoutOrder == null)
            {
                log.warn("Attempt to save null checkoutOrder");
            } else
            {
                if (log.isInfoEnabled())
                {
                    if (checkoutOrder.getOrderTotals() == null)
                    {
                        log.info("Save Order (LifecycleId: " + checkoutOrder.getLifecycleId()
                                + " OrderNumber:" + checkoutOrder.getOrderNumber()
                                + ") ORDER TOTALS is NULL");
                    } else
                    {
                        log.info("Save Order (LifecycleId: " + checkoutOrder.getLifecycleId()
                                + " OrderNumber:" + checkoutOrder.getOrderNumber() + ") with "
                                + checkoutOrder.getOrderTotals().length + " order totals");
                    }
                }
            }
        }

        // Set the status on the OrderStatusHistory object in the StatusTrail array. There should
        // only be one object in the array at this point.
        if (checkoutOrder.getStatusTrail() != null && checkoutOrder.getStatusTrail().length > 0)
        {
            checkoutOrder.getStatusTrail()[0].setOrderStatusId(checkoutOrder.getStatus());
        }

        // Set the customer notification attribute on the OrderStatusHistory object in the
        // StatusTrail array. There should only be one object in the array at this point.
        if (sd.isSendEmails() && sd.isSendOrderConfEmails() && sendEmail)
        {
            checkoutOrder.getStatusTrail()[0].setCustomerNotified(true);
        }

        // Determine who is updating the order and set the UpdatedById field appropriately
        checkoutOrder.getStatusTrail()[0].setUpdatedById(getIdForUserUpdatingOrder(checkoutOrder));

        // Now we can save the order
        SaveOrderOptions saveOptions = getSaveOrderOptions(checkoutOrder);
        int orderId = eng.saveOrderWithOptions(kkAppEng.getSessionId(), checkoutOrder,
                kkAppEng.getLangId(), saveOptions);
        checkoutOrder.setId(orderId);

        // If we are editing an existing order we must archive the existing order
        archiveOrder(eng, saveOptions, /* refresh */true);

        // Save the vendor orders in multi-vendor mode
        StringBuffer bccList = null;
        if (kkAppEng.isMultiVendor() && checkoutOrder.getVendorOrders() != null)
        {
            for (int i = 0; i < checkoutOrder.getVendorOrders().length; i++)
            {
                OrderIf order = checkoutOrder.getVendorOrders()[i];

                order.setPaymentMethod(checkoutOrder.getPaymentMethod());
                OrderStatusHistoryIf[] oshArray = new OrderStatusHistory[]
                { new OrderStatusHistory() };
                order.setStatusTrail(oshArray);
                order.setParentId(orderId);
                KKEngIf eng = kkAppEng.getStoreEng(order.getStoreId()).getEng();
                saveOptions = getSaveOrderOptions(order);
                eng.saveOrderWithOptions(kkAppEng.getSessionId(), order, kkAppEng.getLangId(),
                        saveOptions);
                // If we are editing an existing order we must archive the existing order
                archiveOrder(eng, saveOptions, /* refresh */false);
                String bcc = eng.getConfigurationValue(ConfigConstants.SEND_EXTRA_EMAILS_TO);
                if (bcc != null && bcc.length() > 0)
                {
                    if (bccList == null)
                    {
                        bccList = new StringBuffer();
                    } else
                    {
                        bccList.append(";");
                    }
                    bccList.append(bcc);
                }
            }
        }

        // Update the latest customer orders
        populateCustomerOrders();

        // Send an email
        if (sd.isSendEmails() && sd.isSendOrderConfEmails() && sendEmail)
        {
            if (bccList != null)
            {
                // Dynamically bcc all store owners who have to deliver products for this order
                String bcc = eng.getConfigurationValue(ConfigConstants.SEND_EXTRA_EMAILS_TO);
                if (bcc != null && bcc.length() > 0)
                {
                    bccList.append(";");
                    bccList.append(bcc);
                }
                if (options == null)
                {
                    options = new EmailOptions();
                }
                options.setBccEmails(bccList.toString());
                if (log.isDebugEnabled())
                {
                    log.debug("List of blind copies on order confirmation email are : "
                            + options.getBccEmails());
                }
            }
            sendOrderConfirmationEmail(orderId, options);
        }

        return orderId;
    }

    /**
     * Utility to create a SaveOrderOptions object from an order where the id of the order to be
     * archived is stored in the archiveId attribute of the order. The archiveId of the order is set
     * to null before returning.
     * 
     * @param order
     *            the order
     * @return Returns a SaveOrderOptions object
     */
    protected SaveOrderOptions getSaveOrderOptions(OrderIf order)
    {
        SaveOrderOptions saveOptions = null;
        if (order.getArchiveId() != null)
        {
            int idOfOrderToBeArchived = -1;
            try
            {
                idOfOrderToBeArchived = Integer.parseInt(order.getArchiveId());
            } catch (Exception e)
            {
                log.warn("Order (" + order.getId() + ") archiveId contains a non integer value - "
                        + order.getArchiveId());
            }
            if (idOfOrderToBeArchived > 0)
            {
                saveOptions = new SaveOrderOptions();
                saveOptions.setIdOfOrderToBeArchived(idOfOrderToBeArchived);
            }
            order.setArchiveId(null);
        }
        return saveOptions;
    }

    /**
     * Utility to archive an order
     * 
     * @param _eng
     *            The engine to use
     * @param saveOptions
     *            the saveOptions
     * @param refresh
     *            If true we refresh the order that's just been archived
     * @throws Exception an unexpected exception
     */
    protected void archiveOrder(KKEngIf _eng, SaveOrderOptions saveOptions, boolean refresh)
            throws Exception
    {
        if (saveOptions != null && saveOptions.getIdOfOrderToBeArchived() > 0)
        {
            OrderUpdateIf updateOptions = new OrderUpdate();
            if (kkAppEng.getAdminUser() != null)
            {
                updateOptions.setUpdatedById(kkAppEng.getAdminUser().getId());
            } else
            {
                updateOptions.setUpdatedById(kkAppEng.getActiveCustId());
            }

            _eng.updateOrder(kkAppEng.getSessionId(), saveOptions.getIdOfOrderToBeArchived(),
                    com.konakart.bl.OrderMgr.ARCHIVED_STATUS, /* sendEmail */false, /* comment */
                    null, updateOptions);

            // Refresh the order
            if (refresh)
            {
                getOrder(saveOptions.getIdOfOrderToBeArchived(), /* force */true);
            }
        }
    }

    /**
     * Calls the engine to send an order confirmation email to the customer.
     * 
     * @param orderId
     * @throws KKException
     */
    private void sendOrderConfirmationEmail(int orderId, EmailOptionsIf _options) throws KKException
    {
        EmailOptionsIf options = _options;

        if (options == null)
        {
            options = new EmailOptions();
        }
        if (options.getCountryCode() == null)
        {
            options.setCountryCode(kkAppEng.getLocale().substring(0, 2));
        }
        if (options.getTemplateName() == null)
        {
            options.setTemplateName("OrderConfReceived");
        }

        eng.sendOrderConfirmationEmail1(kkAppEng.getSessionId(), orderId, kkAppEng.getLangId(),
                options);
    }

    /**
     * It attempts to get an order from the currentOrders array. If it can't find it there, then it
     * looks it up from the DB by making a call to the engine. The <code>selectedOrder</code>
     * attribute is set to the order.
     * 
     * @param orderId
     *            The id of the order to fetch.
     * @return Returns the order
     * @throws KKException
     *            an unexpected KKException exception
     */
    public OrderIf getOrder(int orderId) throws KKException
    {
        return getOrder(orderId, /* force */false);
    }

    /**
     * It attempts to get an order from the currentOrders array. If it can't find it there, then it
     * looks it up from the DB by making a call to the engine. The <code>selectedOrder</code>
     * attribute is set to the order.
     * 
     * @param orderId
     *            The id of the order to fetch.
     * @param force
     *            When true, order is always looked up from the DB
     * @return Returns the order
     * @throws KKException
     *            an unexpected KKException exception
     */
    public OrderIf getOrder(int orderId, boolean force) throws KKException
    {
        if (currentOrders != null && !force)
        {
            for (int i = 0; i < currentOrders.length; i++)
            {
                OrderIf currentOrder = currentOrders[i];
                if (currentOrder.getId() == orderId)
                {
                    if (kkAppEng.isMultiVendor() && currentOrder.getVendorOrders() == null)
                    {
                        currentOrder.setVendorOrders(getVendorOrders(orderId));
                    }
                    selectedOrder = currentOrder;
                    return selectedOrder;
                }
            }
        }

        // If we can't find the order in our array, we get it from the DB
        OrderIf o = eng.getOrderWithOptions(kkAppEng.getSessionId(), orderId, kkAppEng.getLangId(),
                getFetchOrderOptions());
        if (kkAppEng.isMultiVendor())
        {
            o.setVendorOrders(getVendorOrders(orderId));
        }

        selectedOrder = o;

        /*
         * If force == true we update the currentOrders array with the new order we looked up
         */
        if (currentOrders != null && force)
        {
            for (int i = 0; i < currentOrders.length; i++)
            {
                OrderIf currentOrder = currentOrders[i];
                if (currentOrder.getId() == orderId)
                {
                    currentOrders[i] = selectedOrder;
                    break;
                }
            }
        }

        return selectedOrder;
    }

    /**
     * Fetch the vendor orders from the database.
     * 
     * @param orderId
     *            the orderId
     * @return Returns an array of orders
     * @throws KKException
     *            an unexpected KKException exception
     */
    public OrderIf[] getVendorOrders(int orderId) throws KKException
    {
        OrderSearchIf search = new OrderSearch();
        search.setParentId(orderId);
        DataDescriptorIf dd = new DataDescriptor();
        OrdersIf orders = eng.searchForOrdersPerCustomer(kkAppEng.getSessionId(), dd, search,
                kkAppEng.getLangId());
        if (orders != null && orders.getOrderArray() != null && orders.getOrderArray().length > 0)
        {
            return orders.getOrderArray();
        }
        return null;
    }

    /**
     * Based on the action we are being asked to perform and the current offset, we set the new
     * offset before going to the engine to ask for more products.
     * 
     * @param action
     * @throws KKAppException
     */
    private void setDataDescOffset(String action) throws KKAppException
    {
        // We initialise the data desc if the number of rows we can view has changed
        if (getPageSize() != (dataDesc.getLimit() - 1))
        {
            initDataDesc();
            currentOffset = 0;
        }

        // Determine whether we've passed in a page number
        int requestedPage = -1;
        try
        {
            requestedPage = Integer.parseInt(action);
        } catch (NumberFormatException e)
        {
        }

        if (action.equals(navStart))
        {
            dataDesc.setOffset(0);
            currentOffset = 0;
            currentPage = 1;
        } else if (action.equals(navNext))
        {
            currentOffset += getPageSize();
            dataDesc.setOffset(currentOffset);
            currentPage = (currentOffset / getPageSize()) + 1;
        } else if (action.equals(navBack))
        {
            currentOffset -= getPageSize();
            if (currentOffset < 0)
            {
                currentOffset = 0;
            }
            dataDesc.setOffset(currentOffset);
            currentPage = (currentOffset / getPageSize()) + 1;
        } else if (requestedPage > 0)
        {
            currentOffset = getPageSize() * (requestedPage - 1);
            dataDesc.setOffset(currentOffset);
            currentPage = requestedPage;
        } else if (requestedPage <= 0)
        {
            currentOffset = 0;
            dataDesc.setOffset(currentOffset);
            currentPage = 1;
        } else
        {
            throw new KKAppException(
                    "The navigation direction parameter has an unrecognised value of " + action);
        }
    }

    /**
     * The number of orders in the currentOrders array.
     * 
     * @return The number of orders currently retrieved.
     */
    public int getNumberOfOrders()
    {
        // We attempt to fetch 1 more record than the number in maxRows so that
        // we can determine whether to show the next button. However, the JSP
        // should only show the number of records displayed which is limited to
        // maxRows within the JSP itself
        if (currentOrders.length == getPageSize() + 1)
        {
            return getPageSize();
        }
        return currentOrders.length;
    }

    /**
     * Creates a partially populated order object and sets <code>checkoutOrder</code> to this
     * object.
     * 
     * @return Returns the checkoutOrder
     * 
     * @throws Exception an unexpected exception
     */
    public OrderIf createCheckoutOrder() throws Exception
    {
        return createCheckoutOrderWithOptions(null);
    }

    /**
     * Creates a partially populated order object and sets <code>checkoutOrder</code> to this
     * object. It receives an options object as input in order to configure certain aspects of the
     * creation.
     * 
     * @param _options
     *            An object containing options for the method. It may be set to null.
     * @return Returns the checkoutOrder
     * 
     * @throws Exception an unexpected exception
     */
    public OrderIf createCheckoutOrderWithOptions(CreateOrderOptionsIf _options) throws Exception
    {
        if (log.isInfoEnabled())
        {
            log.info("create new checkoutOrder");
        }

        CreateOrderOptionsIf options = _options;
        if (options == null)
        {
            options = getCreateOrderOptions();
        } else
        {
            // Add extra info to the options
            if (kkAppEng.getFetchProdOptions() != null)
            {
                options.setPriceDate(kkAppEng.getFetchProdOptions().getPriceDate());
                options.setCatalogId(kkAppEng.getFetchProdOptions().getCatalogId());
                options.setUseExternalPrice(kkAppEng.getFetchProdOptions().isUseExternalPrice());
                options.setUseExternalQuantity(
                        kkAppEng.getFetchProdOptions().isUseExternalQuantity());
                options.setGetImages(kkAppEng.getFetchProdOptions().isGetImages());
            }

            if (options.getLocale() == null)
            {
                options.setLocale(kkAppEng.getLocale());
            }
        }

        BasketIf[] items = kkAppEng.getCustomerMgr().getCurrentCustomer().getBasketItems();

        // Save any products on the basket items and make them null so that we don't send too much
        // data
        HashMap<Integer, ProductIf> prodMap = new HashMap<Integer, ProductIf>();
        if (items != null && items.length > 0)
        {
            for (int i = 0; i < items.length; i++)
            {
                BasketIf b = items[i];
                prodMap.put(b.getId(), b.getProduct());
                b.setProduct(null);
            }
        }

        /*
         * If we are editing an existing order then we have to keep track of the id of the existing
         * order which has temporarily been saved in the archiveId attribute. We set this on the new
         * order.
         */
        // Archive id of main order
        int idOfOrderToBeArchived = -1;
        // Map to store archive ids of vendor orders
        HashMap<String, Integer> archiveIdMap = null;
        if (checkoutOrder != null && checkoutOrder.getArchiveId() != null)
        {
            try
            {
                idOfOrderToBeArchived = Integer.parseInt(checkoutOrder.getArchiveId());
            } catch (Exception e)
            {
                log.warn("CheckoutOrder (" + checkoutOrder.getId()
                        + ") archiveId contains a non integer value - "
                        + checkoutOrder.getArchiveId());
            }

            /*
             * Save the vendor order archive ids in a hash map indexed by the store id
             */
            if (kkAppEng.isMultiVendor())
            {
                archiveIdMap = new HashMap<String, Integer>();
                if (checkoutOrder.getVendorOrders() != null
                        && checkoutOrder.getVendorOrders().length > 0)
                {
                    for (int i = 0; i < checkoutOrder.getVendorOrders().length; i++)
                    {
                        OrderIf vo = checkoutOrder.getVendorOrders()[i];
                        try
                        {
                            int idOfVendorOrderToBeArchived = Integer.parseInt(vo.getArchiveId());
                            archiveIdMap.put(vo.getStoreId(), idOfVendorOrderToBeArchived);
                        } catch (Exception e)
                        {
                            log.warn("CheckoutOrder vendor order (" + vo.getId()
                                    + ") archiveId contains a non integer value - "
                                    + vo.getArchiveId());
                        }
                    }
                }
            }
        }

        if (log.isInfoEnabled())
        {
            log.info("Create checkoutOrder by calling createOrderWithOptions");
        }

        checkoutOrder = eng.createOrderWithOptions(kkAppEng.getSessionId(), items, options,
                kkAppEng.getLangId());

        if (idOfOrderToBeArchived > 0)
        {
            checkoutOrder.setArchiveId(Integer.toString(idOfOrderToBeArchived));
        }

        if (items != null && items.length > 0)
        {
            for (int i = 0; i < items.length; i++)
            {
                BasketIf b = items[i];
                b.setProduct(prodMap.get(b.getId()));
            }
        }

        if (kkAppEng.isMultiVendor() && items != null)
        {
            HashMap<String, ArrayList<BasketIf>> vendorProdMap = new HashMap<String, ArrayList<BasketIf>>();

            // Sort the basket items per store
            for (int i = 0; i < items.length; i++)
            {
                BasketIf b = items[i];
                if (b.getProduct() == null)
                {
                    log.warn("Basket id = " + b.getId() + " didn't have an attached product");
                    ProductIf prod = kkAppEng.getEng().getProduct(null, b.getProductId(),
                            kkAppEng.getLangId());
                    b.setProduct(prod);
                }
                if (b.getProduct().getStoreId() == null)
                {
                    log.warn("Product id = " + b.getProductId() + " has a null store id");
                }
                ArrayList<BasketIf> basketList = vendorProdMap.get(b.getProduct().getStoreId());
                if (basketList == null)
                {
                    basketList = new ArrayList<BasketIf>();
                    vendorProdMap.put(b.getProduct().getStoreId(), basketList);
                }
                basketList.add(b);
            }

            /*
             * We don't attach any vendor orders if the order only contains products belonging to
             * the current store. This happens when vendor stores are available to customers.
             */
            if (!(vendorProdMap.size() == 1 && vendorProdMap.get(kkAppEng.getStoreId()) != null))
            {
                Set<String> storeIdSet = vendorProdMap.keySet();
                OrderIf[] vendorOrders = new OrderIf[storeIdSet.size()];
                int i = 0;
                for (String storeId : storeIdSet)
                {
                    ArrayList<BasketIf> bListPerStore = vendorProdMap.get(storeId);
                    BasketIf[] bArrayPerStore = new BasketIf[bListPerStore.size()];
                    bListPerStore.toArray(bArrayPerStore);
                    // Get engine for this store
                    EngineData engData = kkAppEng.getStoreEng(storeId);
                    OrderIf vendorOrder = engData.getEng().createOrderWithOptions(
                            kkAppEng.getSessionId(), bArrayPerStore, options, kkAppEng.getLangId());
                    vendorOrder.setStoreId(storeId);
                    vendorOrder.setStoreName(engData.getStoreName());
                    if (archiveIdMap != null)
                    {
                        Integer archiveId = archiveIdMap.get(storeId);
                        if (archiveId != null)
                        {
                            vendorOrder.setArchiveId(Integer.toString(archiveId));
                        }
                    }
                    vendorOrders[i++] = vendorOrder;
                }

                // Attach the vendor orders to the checkout orders
                checkoutOrder.setVendorOrders(vendorOrders);
            }
        }

        if (checkoutOrder != null)
        {
            checkoutOrder.setCouponCode(getCouponCode());
            checkoutOrder.setGiftCertCode(getGiftCertCode());
            if (kkAppEng.getAdminUser() != null)
            {
                checkoutOrder.setCreator(kkAppEng.getAdminUser().getEmailAddr());
            }
            checkoutOrder.setAffiliateId(kkAppEng.getAffiliateId());
        }

        if (getKkAppEng().getRewardPointMgr().isEnabled())
        {
            int availablePoints = kkAppEng.getRewardPointMgr().pointsAvailable();
            if (rewardPoints > availablePoints)
            {
                rewardPoints = availablePoints;
            }
            checkoutOrder.setPointsRedeemed(rewardPoints);
        }

        if (log.isInfoEnabled())
        {
            log.info("Created checkoutOrder " + checkoutOrder.getLifecycleId());
        }

        return checkoutOrder;
    }

    /**
     * Gets an array of shipping quotes from the engine. The quotes are put into the shippingQuotes
     * array.
     * 
     * @throws Exception an unexpected exception
     */
    public void createShippingQuotes() throws Exception
    {
        if (kkAppEng.isMultiVendor() && checkoutOrder.getVendorOrders() != null
                && checkoutOrder.getVendorOrders().length > 0)
        {
            ArrayList<Thread> threads = new ArrayList<Thread>();
            for (int i = 0; i < checkoutOrder.getVendorOrders().length; i++)
            {
                OrderIf order = checkoutOrder.getVendorOrders()[i];

                ShippingQuoteFetcher quoteThread = new ShippingQuoteFetcher(
                        kkAppEng.getStoreEng(order.getStoreId()).getEng(), order,
                        vendorShippingQuoteMap, kkAppEng.getLangId());
                quoteThread.setDaemon(true);
                quoteThread.start();
                threads.add(quoteThread);
            }

            // Wait for threads to finish
            for (Thread thread : threads)
            {
                thread.join();
            }
        } else
        {
            shippingQuotes = eng.getShippingQuotes(checkoutOrder, kkAppEng.getLangId());
        }
    }

    /**
     * Gets an array of payment details from the engine. The details are put into the paymentDetails
     * array.
     * 
     * @return Returns the array of payment details
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    public PaymentDetailsIf[] createPaymentGatewayList() throws KKException
    {
        paymentDetailsArray = eng.getPaymentGateways(checkoutOrder, kkAppEng.getLangId());
        return paymentDetailsArray;
    }

    /**
     * Returns an int that describes the payment type of the checkoutOrder. Valid types are :
     * <ul>
     * <li>com.konakart.app.PaymenDetails.COD - Cash On Delivery</li>
     * <li>com.konakart.app.PaymenDetails.BROWSER_PAYMENT_GATEWAY - Pay through a payment gateway
     * using their UI</li>
     * <li>com.konakart.app.PaymenDetails.SERVER_PAYMENT_GATEWAY - Pay through a payment gateway
     * using KonaKart UI</li>
     * </ul>
     * It returns 0 if order is null or the paymentDetails object doesn't exist for the order.
     * 
     * @return The payment type
     */
    public int getPaymentType()
    {
        if (checkoutOrder != null && checkoutOrder.getPaymentDetails() != null)
        {
            return checkoutOrder.getPaymentDetails().getPaymentType();
        }
        return 0;
    }

    /**
     * The shipping Quote referenced by shippingCode is added to the checkout order.
     * 
     * @param shippingCode
     *            e.g. free, item, table, zones
     * @param description
     *            Used to make sure that the quotes match. In the case of dynamic lookups we call
     *            them fedex_1, fedex_2 etc and so fedex_1 may in one case be Overnight and in
     *            another case International Priority etc.
     */
    public void addShippingQuoteToOrder(String shippingCode, String description)
    {
        if (shippingQuotes != null && shippingQuotes.length > 0)
        {
            boolean found = false;
            for (int i = 0; i < shippingQuotes.length; i++)
            {
                ShippingQuoteIf quote = shippingQuotes[i];
                if (quote.getCode() != null && quote.getCode().equals(shippingCode))
                {
                    if (quote.getDescription() == null || description == null
                            || quote.getDescription().equals(description))
                    {
                        checkoutOrder.setShippingQuote(quote);
                        found = true;
                        break;
                    }
                }
            }
            // We haven't found it so we use the first in the list
            if (!found)
                checkoutOrder.setShippingQuote(shippingQuotes[0]);
        } else
        {
            checkoutOrder.setShippingQuote(null);
        }
    }

    /**
     * The shipping Quote referenced by shippingCode is added to the order.
     * 
     * @param shippingCode
     *            e.g. free, item, table, zones
     * @param order
     *            The vendor order
     * @param description
     *            Used to make sure that the quotes match. In the case of dynamic lookups we call
     *            them fedex_1, fedex_2 etc and so fedex_1 may in one case be Overnight and in
     *            another case International Priority etc.
     */
    public void addShippingQuoteToVendorOrder(String shippingCode, OrderIf order,
            String description)
    {
        ShippingQuoteIf[] quotes = vendorShippingQuoteMap.get(order.getStoreId());
        if (quotes != null && quotes.length > 0)
        {
            boolean found = false;
            for (int j = 0; j < quotes.length; j++)
            {
                ShippingQuoteIf quote = quotes[j];
                if (quote.getCode() != null && quote.getCode().equals(shippingCode))
                {
                    if (quote.getDescription() == null || description == null
                            || quote.getDescription().equals(description))
                    {
                        order.setShippingQuote(quote);
                        found = true;
                        break;
                    }
                }
            }
            // We haven't found it so we use the first in the list
            if (!found)
                order.setShippingQuote(quotes[0]);
        }
    }

    /**
     * The payment details object referenced by paymentCode is added to the order. If a sub code
     * exists (i.e. Global Collect) then the code consists of code~~subcode .
     * 
     * @param paymentCode
     *            The code of the payment module. e.g. cod, paypal, worldpay
     * @return Returns true if the payment details were added
     * @throws KKException
     *            an unexpected KKException exception
     */
    public boolean addPaymentDetailsToOrder(String paymentCode) throws KKException
    {
        if (paymentDetailsArray == null || paymentDetailsArray.length == 0)
        {
            createPaymentGatewayList();
        }

        if (paymentDetailsArray != null && paymentDetailsArray.length > 0)
        {
            // If present split the payment code into: Code and SubCode
            int dividerIdx = paymentCode.indexOf("~~");
            String code = paymentCode;
            String subCode = null;

            if (dividerIdx != -1)
            {
                code = paymentCode.substring(0, dividerIdx);
                subCode = paymentCode.substring(dividerIdx + 2);
            }

            if (log.isDebugEnabled())
            {
                log.debug("paymentCode = " + paymentCode + " => code = " + code + " subCode = "
                        + subCode);
            }

            for (int i = 0; i < paymentDetailsArray.length; i++)
            {
                PaymentDetailsIf pd = paymentDetailsArray[i];
                if (pd.getCode() != null && pd.getCode().equals(code))
                {
                    if (subCode == null
                            || (pd.getSubCode() != null && pd.getSubCode().equals(subCode)))
                    {
                        checkoutOrder.setPaymentDetails(pd);
                        checkoutOrder.setPaymentMethod(pd.getTitle());
                        checkoutOrder.setPaymentModuleCode(code);
                        checkoutOrder.setPaymentModuleSubCode(subCode);
                        if (log.isDebugEnabled())
                        {
                            log.debug("C) Set Payment Details on checkout order: "
                                    + checkoutOrder.getPaymentDetails().toString());
                        }
                        return true;
                    }
                }
            }
        } else
        {
            checkoutOrder.setPaymentDetails(null);
            checkoutOrder.setPaymentMethod(null);
            checkoutOrder.setPaymentModuleCode(null);
            checkoutOrder.setPaymentModuleSubCode(null);
        }
        return false;
    }

    /**
     * The address object should already exist in the list of addresses for the current customer. It
     * is set as the shipping address for the current checkoutOrder.
     * 
     * @param addrId
     *            The id of the address object
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void setCheckoutOrderShippingAddress(int addrId) throws KKException
    {

        if (kkAppEng.getCustomerMgr().getCurrentCustomer() == null
                || kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses() == null)
        {
            return;
        }

        AddressIf selectedAddr = null;
        for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer()
                .getAddresses().length; i++)
        {
            AddressIf addr = kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses()[i];
            if (addr.getId() == addrId)
            {
                selectedAddr = addr;
                break;
            }
        }

        if (selectedAddr == null)
        {
            return;
        }

        // We get the engine to change the address because the tax rates also need to be
        // recalculated

        if (log.isInfoEnabled())
        {
            log.info("Create/Update checkoutOrder by calling changeDeliveryAddress");
        }

        checkoutOrder = eng.changeDeliveryAddress(kkAppEng.getSessionId(), checkoutOrder,
                selectedAddr);
    }

    /**
     * The address object should already exist in the list of addresses for the current customer. It
     * is set as the billing address for the current checkoutOrder.
     * 
     * @param addrId
     *            The id of the address object
     */
    public void setCheckoutOrderBillingAddress(int addrId)
    {
        if (kkAppEng.getCustomerMgr().getCurrentCustomer() == null
                || kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses() == null)
        {
            return;
        }

        AddressIf selectedAddr = null;
        for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer()
                .getAddresses().length; i++)
        {
            AddressIf addr = kkAppEng.getCustomerMgr().getCurrentCustomer().getAddresses()[i];
            if (addr.getId() == addrId)
            {
                selectedAddr = addr;
                break;
            }
        }

        if (selectedAddr == null)
        {
            return;
        }

        checkoutOrder.setBillingName(selectedAddr.getFormattedName());
        checkoutOrder.setBillingCompany(selectedAddr.getCompany());
        checkoutOrder.setBillingStreetAddress(selectedAddr.getStreetAddress());
        checkoutOrder.setBillingStreetAddress1(selectedAddr.getStreetAddress1());
        checkoutOrder.setBillingSuburb(selectedAddr.getSuburb());
        checkoutOrder.setBillingCity(selectedAddr.getCity());
        checkoutOrder.setBillingPostcode(selectedAddr.getPostcode());
        checkoutOrder.setBillingState(selectedAddr.getState());
        checkoutOrder.setBillingCountry(selectedAddr.getCountryName());
        checkoutOrder.setBillingTelephone(selectedAddr.getTelephoneNumber());
        checkoutOrder.setBillingTelephone1(selectedAddr.getTelephoneNumber1());
        checkoutOrder.setBillingEmail(selectedAddr.getEmailAddr());
        checkoutOrder.setBillingFormattedAddress(selectedAddr.getFormattedAddress());
        checkoutOrder.setBillingAddrId(selectedAddr.getId());
    }

    /**
     * A new Order is created with the same products as a previous order which is passed in as a
     * parameter. The attribute <code>CheckoutOrder</code> is set to the new order. The same payment
     * and shipping providers are selected although the default billing and shipping addresses are
     * used since the original ones may no longer be valid.<br>
     * All of the products are placed in the cart and a KKNotInStockException is thrown if the stock
     * of any product is not sufficient for the order and KonaKart is configured to not allow
     * checkout for products that are not in stock.
     * 
     * @param orderId
     *            The id of the order to be repeated
     * @param addToCurrentBasket
     *            If set to true, the order items are added to the items in the current basket.
     *            Otherwise the current basket is cleared.
     * @param copyCustomFields
     *            If set, the custom fields of the orderProducts are copied to the custom fields of
     *            the basket items so that when the new order is created, the orderProducts of the
     *            new order have the same custom field data as the original order.
     * @param edit
     *            Set to true if the repeat order is called for an edit in which case the old order
     *            is archived
     * @throws KKNotInStockException
     *             Thrown if the stock of any product is not sufficient for the order and KonaKart
     *             is configured to not allow checkout for products that are not in stock.
     * @throws Exception an unexpected exception
     */
    public void repeatOrder(int orderId, boolean addToCurrentBasket, boolean copyCustomFields,
            boolean edit) throws KKNotInStockException, Exception
    {
        // Get the order which is then placed in selectedOrder
        getOrder(orderId);

        if (selectedOrder == null)
        {
            throw new KKAppException("The order with id = " + orderId + " cannot be found.");
        }

        repeatOrder(selectedOrder, addToCurrentBasket, copyCustomFields, edit);
    }

    /**
     * A new Order is created with the same products as a previous order which is passed in as a
     * parameter. The attribute <code>CheckoutOrder</code> is set to the new order. The same payment
     * and shipping providers are selected although the default billing and shipping addresses are
     * used since the original ones may no longer be valid.<br>
     * All of the products are placed in the cart and a KKNotInStockException is thrown if the stock
     * of any product is not sufficient for the order and KonaKart is configured to not allow
     * checkout for products that are not in stock.
     * 
     * @param order
     *            The order to be repeated
     * @param addToCurrentBasket
     *            If set to true, the order items are added to the items in the current basket.
     *            Otherwise the current basket is cleared.
     * @param copyCustomFields
     *            If set, the custom fields of the orderProducts are copied to the custom fields of
     *            the basket items so that when the new order is created, the orderProducts of the
     *            new order have the same custom field data as the original order.
     * @param edit
     *            Set to true if the repeat order is called for an edit in which case the old order
     *            is archived
     * @throws KKNotInStockException
     *             Thrown if the stock of any product is not sufficient for the order and KonaKart
     *             is configured to not allow checkout for products that are not in stock.
     * @throws Exception an unexpected exception
     */
    public void repeatOrder(OrderIf order, boolean addToCurrentBasket, boolean copyCustomFields,
            boolean edit) throws KKNotInStockException, Exception
    {
        // Get the current customer
        CustomerIf cust = kkAppEng.getCustomerMgr().getCurrentCustomer();

        if (order == null)
        {
            throw new KKAppException("The order cannot be set to Null.");
        }

        if (order.getOrderProducts() == null || order.getOrderProducts().length == 0)
        {
            throw new KKAppException(
                    "The order with id = " + order.getId() + " contains no products.");
        }

        // Clear the current basket
        if (!addToCurrentBasket)
        {
            eng.removeBasketItemsPerCustomer(kkAppEng.getSessionId(), 0);
        }

        // Create basket items from the order products of the order and add them to the basket
        boolean refresh;
        AddToBasketOptionsIf atbo = kkAppEng.getBasketMgr().getAddToBasketOptions();
        if (atbo == null)
        {
            atbo = new AddToBasketOptions();
        }
        atbo.setAllowMultipleEntriesForSameProduct(true);
        for (int i = 0; i < order.getOrderProducts().length; i++)
        {
            OrderProductIf op = order.getOrderProducts()[i];
            BasketIf b = new Basket();
            b.setQuantity(op.getQuantity());
            b.setProductId(op.getProductId());
            b.setOpts(op.getOpts());
            if (copyCustomFields)
            {
                b.setCustom1(op.getCustom1());
                b.setCustom2(op.getCustom2());
                b.setCustom3(op.getCustom3());
                b.setCustom4(op.getCustom4());
                b.setCustom5(op.getCustom5());
                b.setCustom6(op.getCustom6());
                b.setCustom7(op.getCustom7());
                b.setCustom8(op.getCustom8());
                b.setCustom9(op.getCustom9());
                b.setCustom10(op.getCustom10());
            }
            refresh = (i == order.getOrderProducts().length - 1) ? true : false;

            try
            {
                kkAppEng.getBasketMgr().addToBasketWithOptions(b, atbo, refresh);
            } catch (Exception e)
            {
                // An exception will occur if the product or option no longer exists
            }
        }

        if (cust.getBasketItems() == null || cust.getBasketItems().length == 0)
        {
            // We weren't able to add any items to the basket because they are all no longer
            // available
            throw new KKNotInStockException("None of the products in the order are available");
        }

        // Check to see whether we are trying to checkout an item that isn't in stock
        String stockAllowCheckout = kkAppEng.getConfig(ConfigConstants.STOCK_ALLOW_CHECKOUT);
        if (stockAllowCheckout != null && stockAllowCheckout.equalsIgnoreCase("false"))
        {
            // If required, we check to see whether the products are in stock
            BasketIf[] items = kkAppEng.getEng().updateBasketWithStockInfo(cust.getBasketItems());
            for (int i = 0; i < items.length; i++)
            {
                BasketIf basket = items[i];
                if (basket.getQuantity() > basket.getQuantityInStock())
                {
                    throw new KKNotInStockException("The product id = " + basket.getProductId()
                            + " is not in stock in sufficient quantity to satisfy the order");
                }
            }
        }

        // Create an order object and set checkoutOrder to this object
        CreateOrderOptionsIf options = new CreateOrderOptions();
        options.setBillingAddrId(order.getBillingAddrId());
        options.setCustomerAddrId(order.getCustomerAddrId());
        options.setDeliveryAddrId(order.getDeliveryAddrId());
        if (copyCustomFields)
        {
            options.setCopyBasketCustomFields(true);
        }
        createCheckoutOrderWithOptions(options);

        // Populate the shipping module information
        checkoutOrder.setShippingModuleCode(order.getShippingModuleCode());
        ShippingQuoteIf quote = null;
        if (!Utils.isBlank(checkoutOrder.getShippingModuleCode()))
        {
            try
            {
                quote = eng.getShippingQuote(checkoutOrder, checkoutOrder.getShippingModuleCode(),
                        kkAppEng.getLangId());
                if (quote != null && quote.getQuotes() != null && quote.getQuotes().length > 0)
                {
                    quote = quote.getQuotes()[0];
                }
            } catch (Exception e)
            {
                log.debug("Exception thrown attempting to get a shipping quote", e);
            }
        }

        if (quote == null)
        {
            /*
             * If the shipping module that was used for the order no longer exists, then use a
             * default shipping method which can always be changed by the customer in the
             * confirmation screen
             */
            createShippingQuotes();
            if (shippingQuotes != null && shippingQuotes.length > 0)
            {
                ShippingQuoteIf lQuote = shippingQuotes[0];
                if (lQuote.getQuotes() != null && lQuote.getQuotes().length > 0)
                {
                    lQuote = lQuote.getQuotes()[0];
                }

                checkoutOrder.setShippingModuleCode(lQuote.getCode());
                checkoutOrder.setShippingQuote(lQuote);
            }
        } else
        {
            checkoutOrder.setShippingQuote(quote);
        }

        // Populate the payment module information
        checkoutOrder.setPaymentModuleCode(order.getPaymentModuleCode());
        checkoutOrder.setPaymentModuleSubCode(order.getPaymentModuleSubCode());
        PaymentDetailsIf pd = null;
        if (!Utils.isBlank(order.getPaymentModuleCode()))
        {
            String modCode = checkoutOrder.getPaymentModuleCode();
            if (order.getPaymentModuleSubCode() != null)
            {
                modCode += "~~" + order.getPaymentModuleSubCode();
            }
            pd = eng.getPaymentGateway(checkoutOrder, modCode, kkAppEng.getLangId());

            if (log.isDebugEnabled())
            {
                log.debug("getPaymentGateway() returned found '" + pd.getTitle() + "' for "
                        + modCode);
            }
        }

        if (pd == null || !isSamePaymentModule(pd, order))
        {
            /*
             * If the payment module that was used for the order no longer exists, then use a
             * default payment method which can always be changed by the customer in the
             * confirmation screen
             */
            createPaymentGatewayList();
            if (paymentDetailsArray != null && paymentDetailsArray.length > 0
                    && paymentDetailsArray[0] != null)
            {
                checkoutOrder.setPaymentModuleCode(paymentDetailsArray[0].getCode());
                checkoutOrder.setPaymentModuleSubCode(paymentDetailsArray[0].getSubCode());
                checkoutOrder.setPaymentDetails(paymentDetailsArray[0]);
                checkoutOrder.setPaymentMethod(paymentDetailsArray[0].getTitle());
                if (log.isDebugEnabled())
                {
                    log.debug("A) Set Payment Details on checkout order: "
                            + checkoutOrder.getPaymentDetails().toString());
                }
            }
        } else
        {
            checkoutOrder.setPaymentDetails(pd);
            checkoutOrder.setPaymentMethod(pd.getTitle());
            if (log.isDebugEnabled())
            {
                log.debug("B) Set Payment Details on checkout order: "
                        + checkoutOrder.getPaymentDetails().toString());
            }
        }

        // Populate the comment
        OrderStatusHistoryIf osh = new OrderStatusHistory();
        osh.setComments("");
        osh.setUpdatedById(getIdForUserUpdatingOrder(checkoutOrder));
        OrderStatusHistoryIf[] oshArray = new OrderStatusHistoryIf[1];
        oshArray[0] = osh;
        checkoutOrder.setStatusTrail(oshArray);

        /*
         * Set the archiveId to temporarily keep track that this is an edited order and so we need
         * to archive the original order. We save the id of the original order.
         */
        if (edit)
        {
            checkoutOrder.setArchiveId(Integer.toString(order.getId()));
            if (kkAppEng.isMultiVendor() && checkoutOrder.getVendorOrders() != null
                    && checkoutOrder.getVendorOrders().length > 0 && order.getVendorOrders() != null
                    && order.getVendorOrders().length > 0)
            {
                HashMap<String, Integer> archiveIdMap = new HashMap<String, Integer>();
                // Store the current archive ids in the map
                for (int i = 0; i < order.getVendorOrders().length; i++)
                {
                    OrderIf vo = order.getVendorOrders()[i];
                    archiveIdMap.put(vo.getStoreId(), vo.getId());
                }
                // Set the archive id on the checkout vendor orders
                for (int i = 0; i < checkoutOrder.getVendorOrders().length; i++)
                {
                    OrderIf vo = checkoutOrder.getVendorOrders()[i];
                    int archiveId = archiveIdMap.get(vo.getStoreId());
                    vo.setArchiveId(Integer.toString(archiveId));
                }
            }
        }
    }

    /**
     * Return true if the order uses the specified payment module
     * 
     * @param pd
     *            Payment Details object
     * 
     * @param order
     *            the order
     * 
     * @return true if the order uses the specified Payment Details
     */
    protected boolean isSamePaymentModule(PaymentDetailsIf pd, OrderIf order)
    {
        if (!order.getPaymentModuleCode().equals(pd.getCode()))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Not the same : module codes different");
            }
            return false;
        }

        if (order.getPaymentModuleSubCode() == null && pd.getSubCode() == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("The same : module codes match and sub codes both null");
            }
            return true;
        }

        if (order.getPaymentModuleSubCode() == null || pd.getSubCode() == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Not the same : module code match but both sub codes are not null");
            }
            return false;
        }

        if (order.getPaymentModuleSubCode().equals(pd.getSubCode()))
        {
            if (log.isDebugEnabled())
            {
                log.debug("The same : module codes match and sub codes natch");
            }
            return true;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Not the same : module codes match but sub codes do not");
        }
        return false;
    }

    /**
     * Get the Id of the user who is updating this order
     * 
     * @param order
     *            the order being updated
     * @return a customerId of the person updating the order
     */
    public int getIdForUserUpdatingOrder(OrderIf order)
    {
        // Determine who is updating the order and set the UpdatedById field appropriately
        if (order.getCreator() != null)
        {
            // logged in as Administrator so get the admin Id from the KKAppEng
            return kkAppEng.getActiveCustId();
        }

        // Assume this is a normal using creating the order
        return order.getCustomerId();
    }

    /**
     * Update the product inventory based on the products sold in the order identified by orderId.
     * 
     * @param orderId
     *            The id of the order
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void updateInventory(int orderId) throws KKException, KKAppException
    {
        if (kkAppEng.isMultiVendor())
        {
            getOrder(orderId);
            if (selectedOrder == null || selectedOrder.getId() != orderId)
            {
                throw new KKAppException("Order with id = " + orderId + " cannot be found");
            }
            if (selectedOrder.getVendorOrders() != null
                    && selectedOrder.getVendorOrders().length > 0)
            {
                for (int i = 0; i < selectedOrder.getVendorOrders().length; i++)
                {
                    OrderIf order = selectedOrder.getVendorOrders()[i];
                    try
                    {
                        kkAppEng.getStoreEng(order.getStoreId()).getEng()
                                .updateInventoryWithOptions(kkAppEng.getSessionId(), order.getId(),
                                        getCreateOrderOptions());
                    } catch (Exception e)
                    {
                        throw new KKException("Problem updating inventory of vendor order", e);
                    }
                }
            } else
            {
                kkAppEng.getEng().updateInventoryWithOptions(kkAppEng.getSessionId(), orderId,
                        getCreateOrderOptions());
            }

        } else
        {
            kkAppEng.getEng().updateInventoryWithOptions(kkAppEng.getSessionId(), orderId,
                    getCreateOrderOptions());
        }

        // Refresh the category tree since some products may no longer be available
        kkAppEng.getCategoryMgr().fetchCategoryTree();
    }

    /**
     * Gets the currentOffset in the currentOrders array. Used to page through an array of orders.
     * 
     * @return Returns the currentOffset in the currentOrders array
     */
    public int getCurrentOffset()
    {
        return currentOffset;
    }

    /**
     * Maximum number of orders to show in a list.
     * 
     * @return Returns the maxRows
     */
    public int getMaxRows()
    {
        return sd.getMaxRows();
    }

    /**
     * Show the back button if set to 1. Don't show the back button if set to 0.
     * 
     * @return Returns the showBack.
     */
    public int getShowBack()
    {
        return showBack;
    }

    /**
     * Show the next button if set to 1. Don't show the next button if set to 0.
     * 
     * @return Returns the showNext.
     */
    public int getShowNext()
    {
        return showNext;
    }

    /**
     * navBack is a constant. It is the string required to pass to the navigateCurrentOrders() as
     * the <code>navdir</code> attribute when navigating backwards.
     * 
     * @return Returns the navBack.
     */
    public String getNavBack()
    {
        return navBack;
    }

    /**
     * navNext is a constant. It is the string required to pass to the navigateCurrentOrders() as
     * the <code>navdir</code> attribute when navigating forwards.
     * 
     * @return Returns the navNext.
     */
    public String getNavNext()
    {
        return navNext;
    }

    /**
     * navStart is a constant. It is the string required to pass to the navigateCurrentOrders() as
     * the <code>navdir</code> attribute when navigating to the start.
     * 
     * @return Returns the navStart.
     */
    public String getNavStart()
    {
        return navStart;
    }

    /**
     * Returns the selected order.
     * 
     * @return Returns the selectedOrder.
     */
    public OrderIf getSelectedOrder()
    {
        return selectedOrder;
    }

    /**
     * Used to determine the total number of orders.
     * 
     * @return Returns the totalNumberOfOrders.
     */
    public int getTotalNumberOfOrders()
    {
        return totalNumberOfOrders;
    }

    /**
     * Gets an array of current orders.
     * 
     * @return Returns the currentOrders.
     */
    public OrderIf[] getCurrentOrders()
    {
        return currentOrders;
    }

    /**
     * Returns the current checkout order.
     * 
     * @return Returns the checkoutOrder.
     */
    public OrderIf getCheckoutOrder()
    {
        return checkoutOrder;
    }

    /**
     * Sets the checkout order with the order passed in as a parameter.
     * 
     * @param checkoutOrder
     *            The checkoutOrder to set.
     */
    public void setCheckoutOrder(OrderIf checkoutOrder)
    {
        this.checkoutOrder = checkoutOrder;
    }

    /**
     * Gets an array of shipping quotes for the current order.
     * 
     * @return Returns the shippingQuotes.
     */
    public ShippingQuoteIf[] getShippingQuotes()
    {
        return shippingQuotes;
    }

    /**
     * Returns an array of payment details for the current order.
     * 
     * @return Returns the paymentDetailsArray.
     */
    public PaymentDetailsIf[] getPaymentDetailsArray()
    {
        return paymentDetailsArray;
    }

    /**
     * In the form servername:serverport
     * 
     * @return Returns the hostAndPort.
     */
    public String getHostAndPort()
    {
        return hostAndPort;
    }

    /**
     * In the form servername:serverport
     * 
     * @param hostAndPort
     *            The hostAndPort to set.
     */
    public void setHostAndPort(String hostAndPort)
    {
        this.hostAndPort = hostAndPort;
    }

    /**
     * Boolean passed to GWT one page checkout code to tell it to either create a new order from the
     * basket items or to use the current checkoutOrder as would be the case for repeat order.
     * 
     * @return Returns the useCheckoutOrder.
     */
    public boolean isUseCheckoutOrder()
    {
        return useCheckoutOrder;
    }

    /**
     * Boolean passed to GWT one page checkout code to tell it to either create a new order from the
     * basket items or to use the current checkoutOrder as would be the case for repeat order.
     * 
     * @param useCheckoutOrder
     *            The useCheckoutOrder to set.
     */
    public void setUseCheckoutOrder(boolean useCheckoutOrder)
    {
        this.useCheckoutOrder = useCheckoutOrder;
    }

    /**
     * Creates an CreateOrderOptionsIf based on the current FetchProductOptions stored in the
     * AppEng.
     * 
     * @return Returns an AddToBasketOptionsIf object
     */
    public CreateOrderOptionsIf getCreateOrderOptions()
    {
        CreateOrderOptionsIf coo = new CreateOrderOptions();
        coo.setUseWishListShippingAddr(true);
        if (kkAppEng.getFetchProdOptions() != null)
        {
            coo.setCatalogId(kkAppEng.getFetchProdOptions().getCatalogId());
            coo.setPriceDate(kkAppEng.getFetchProdOptions().getPriceDate());
            coo.setUseExternalPrice(kkAppEng.getFetchProdOptions().isUseExternalPrice());
            coo.setUseExternalQuantity(kkAppEng.getFetchProdOptions().isUseExternalQuantity());
            coo.setGetImages(kkAppEng.getFetchProdOptions().isGetImages());
        }
        coo.setLocale(kkAppEng.getLocale());
        return coo;
    }

    /**
     * Latest coupon code entered by the customer
     * 
     * @return the couponCode
     */
    public String getCouponCode()
    {
        return couponCode;
    }

    /**
     * Latest coupon code entered by the customer
     * 
     * @param couponCode
     *            the couponCode to set
     */
    public void setCouponCode(String couponCode)
    {
        this.couponCode = couponCode;
    }

    /**
     * Reward points entered by customer
     * 
     * @return the rewardPoints
     */
    public int getRewardPoints()
    {
        return rewardPoints;
    }

    /**
     * Reward points entered by customer
     * 
     * @param rewardPoints
     *            the rewardPoints to set
     */
    public void setRewardPoints(int rewardPoints)
    {
        this.rewardPoints = rewardPoints;
    }

    /**
     * @return the giftCertCode
     */
    public String getGiftCertCode()
    {
        return giftCertCode;
    }

    /**
     * @param giftCertCode
     *            the giftCertCode to set
     */
    public void setGiftCertCode(String giftCertCode)
    {
        this.giftCertCode = giftCertCode;
    }

    /**
     * Used to set a user defined maximum number of orders displayed on a page. When set to a number
     * greater than zero, this value is used instead of the value in the configuration variable
     * MAX_DISPLAY_ORDER_HISTORY.
     * 
     * @param num
     *            the num
     */
    public void setPageSize(int num)
    {
        if (getPageSize() != num)
        {
            initDataDesc();
            currentOffset = 0;
        }
        maxRowsUser = num;
    }

    /**
     * Used to get the maximum number of orders to display. The value is customizable by the
     * customer.
     * 
     * @return Returns the maximum number of orders to display
     */
    public int getPageSize()
    {
        if (maxRowsUser > 0)
        {
            return maxRowsUser;
        }
        return sd.getMaxRows();
    }

    /**
     * Get an array list of pages to show
     * 
     * @param _currentPage
     * @return Returns an array List of pages to show
     */
    private ArrayList<Integer> getPages(int _currentPage)
    {
        numPages = totalNumberOfOrders / getPageSize();
        if (totalNumberOfOrders % getPageSize() != 0)
        {
            numPages++;
        }

        pageList.clear();

        return getPages(_currentPage, numPages, sd.getMaxPageLinks(), pageList);
    }

    /**
     * @return the numPages
     */
    public int getNumPages()
    {
        return numPages;
    }

    /**
     * @param numPages
     *            the numPages to set
     */
    public void setNumPages(int numPages)
    {
        this.numPages = numPages;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage()
    {
        return currentPage;
    }

    /**
     * @param currentPage
     *            the currentPage to set
     */
    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }

    /**
     * @return the pageList
     */
    public ArrayList<Integer> getPageList()
    {
        return pageList;
    }

    /**
     * @param pageList
     *            the pageList to set
     */
    public void setPageList(ArrayList<Integer> pageList)
    {
        this.pageList = pageList;
    }

    /**
     * @return the vendorShippingQuoteMap
     */
    public HashMap<String, ShippingQuoteIf[]> getVendorShippingQuoteMap()
    {
        return vendorShippingQuoteMap;
    }

    /**
     * @param vendorShippingQuoteMap
     *            the vendorShippingQuoteMap to set
     */
    public void setVendorShippingQuoteMap(HashMap<String, ShippingQuoteIf[]> vendorShippingQuoteMap)
    {
        this.vendorShippingQuoteMap = vendorShippingQuoteMap;
    }

    /**
     * Used to store the static data of this manager
     */
    private class StaticData
    {
        int maxRows = 10;

        int maxPageLinks = 5;

        boolean sendEmails = false;

        boolean sendOrderConfEmails = false;

        // is the manager ready?
        boolean mgrReady = false;

        /**
         * @return Returns the maxRows.
         */
        public int getMaxRows()
        {
            return maxRows;
        }

        /**
         * @param maxRows
         *            The maxRows to set.
         */
        public void setMaxRows(int maxRows)
        {
            this.maxRows = maxRows;
        }

        /**
         * @return Returns the sendEmails.
         */
        public boolean isSendEmails()
        {
            return sendEmails;
        }

        /**
         * @param sendEmails
         *            The sendEmails to set.
         */
        public void setSendEmails(boolean sendEmails)
        {
            this.sendEmails = sendEmails;
        }

        /**
         * @return Returns the sendOrderConfEmails.
         */
        public boolean isSendOrderConfEmails()
        {
            return sendOrderConfEmails;
        }

        /**
         * @param sendOrderConfEmails
         *            The sendOrderConfEmails to set.
         */
        public void setSendOrderConfEmails(boolean sendOrderConfEmails)
        {
            this.sendOrderConfEmails = sendOrderConfEmails;
        }

        /**
         * @return the mgrReady
         */
        public boolean isMgrReady()
        {
            return mgrReady;
        }

        /**
         * @param mgrReady
         *            the mgrReady to set
         */
        public void setMgrReady(boolean mgrReady)
        {
            this.mgrReady = mgrReady;
        }

        /**
         * @return the maxPageLinks
         */
        public int getMaxPageLinks()
        {
            return maxPageLinks;
        }

        /**
         * @param maxPageLinks
         *            the maxPageLinks to set
         */
        public void setMaxPageLinks(int maxPageLinks)
        {
            this.maxPageLinks = maxPageLinks;
        }
    }

}
