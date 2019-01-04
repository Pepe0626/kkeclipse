//
// (c) 2017 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.appif.CustomerIf;
import com.konakartadmin.app.AdminAddress;
import com.konakartadmin.app.AdminAddressSearch;
import com.konakartadmin.app.AdminAddressSearchResult;
import com.konakartadmin.app.AdminCustomer;

/**
 * Opens a page where a B2B customer admin can view and change entries in the address book of one of
 * his managed users.
 */
public class B2BAddressBookAction extends B2BBaseAction
{
    private static final long serialVersionUID = 1L;

    private String maxEntries;

    private int b2bCustId;

    private String b2bCustName;

    private AdminAddress[] addresses;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            int custId;

            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            // Check to see whether the user is logged in
            custId = this.loggedIn(request, response, kkAppEng, "B2BManageCustomers");

            if (custId < 0)
            {
                return KKLOGIN;
            }

            // Ensure we are using the correct protocol. Redirect if not.
            String redirForward = checkSSL(kkAppEng, request, custId, /* forceSSL */false);
            if (redirForward != null)
            {
                setupResponseForSSLRedirect(response, redirForward);
                return null;
            }

            // Ensure that the current customer is of the correct type
            if (!isB2BAdmin(kkAppEng))
            {
                return "B2BManageCustomers";
            }

            // b2bCustId may already be set
            if (b2bCustId <= 0)
            {               
                String custIdStr = request.getParameter("custId");
                try
                {
                    b2bCustId = Integer.parseInt(custIdStr);
                } catch (Exception e)
                {
                    return "B2BManageCustomers";
                }
            }

            // Get the customer we need to edit from the admin eng
            CustomerIf currentCust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            AdminCustomer b2bCust = kkAppEng.getAdminEng()
                    .getCustomerForIdWithOptions(kkAppEng.getSessionId(), b2bCustId, null);
            if (b2bCust == null)
            {
                log.info("Customer with id = " + b2bCustId + " could not be found.");
                return "B2BManageCustomers";
            }
            b2bCustName = b2bCust.getFirstName() + " " + b2bCust.getLastName();

            if (b2bCust.getParentId() != currentCust.getId())
            {
                log.info("Customer with id = " + b2bCustId
                        + " is not a child of the current customer.");
                return "B2BManageCustomers";
            }

            // Get the addresses. Put primary address first.
            AdminAddressSearch search = new AdminAddressSearch();
            search.setCustomerId(b2bCustId);
            search.setFormatAddresses(true);
            AdminAddressSearchResult ret = kkAppEng.getAdminEng()
                    .getAddresses(kkAppEng.getSessionId(), search, 0, 1000);
            if (ret != null && ret.getAddresses() != null && ret.getAddresses().length > 0)
            {
                addresses = new AdminAddress[ret.getAddresses().length];
                int index = 1;
                for (int i = 0; i < ret.getAddresses().length; i++)
                {
                    AdminAddress addr = ret.getAddresses()[i];
                    if (addr.getIsPrimary())
                    {
                        addresses[0] = addr;
                    } else
                    {
                        addresses[index++] = addr;
                    }
                }
            }

            kkAppEng.getNav().add(kkAppEng.getMsg("header.address.book"), request);

            // populate the attribute to display in the JSP
            maxEntries = kkAppEng.getConfig("MAX_ADDRESS_BOOK_ENTRIES");

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the maxEntries
     */
    public String getMaxEntries()
    {
        return maxEntries;
    }

    /**
     * @param maxEntries
     *            the maxEntries to set
     */
    public void setMaxEntries(String maxEntries)
    {
        this.maxEntries = maxEntries;
    }

    /**
     * @return the b2bCustId
     */
    public int getB2bCustId()
    {
        return b2bCustId;
    }

    /**
     * @param b2bCustId
     *            the b2bCustId to set
     */
    public void setB2bCustId(int b2bCustId)
    {
        this.b2bCustId = b2bCustId;
    }

    /**
     * @return the b2bCustName
     */
    public String getB2bCustName()
    {
        return b2bCustName;
    }

    /**
     * @param b2bCustName
     *            the b2bCustName to set
     */
    public void setB2bCustName(String b2bCustName)
    {
        this.b2bCustName = b2bCustName;
    }

    /**
     * @return the addresses
     */
    public AdminAddress[] getAddresses()
    {
        return addresses;
    }

    /**
     * @param addresses
     *            the addresses to set
     */
    public void setAddresses(AdminAddress[] addresses)
    {
        this.addresses = addresses;
    }

}
