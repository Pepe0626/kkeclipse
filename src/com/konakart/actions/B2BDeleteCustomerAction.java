//
// (c) 2006-2017 DS Data Systems UK Ltd, All rights reserved.
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
import com.konakartadmin.app.AdminCustomer;

/**
 * Gets called before delete b2b customer page.
 */
public class B2BDeleteCustomerAction extends B2BBaseAction
{
    private static final long serialVersionUID = 1L;

    private AdminCustomer delCust = null;

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

            String delCustIdStr = request.getParameter("custId");
            int delCustId = 0;
            try
            {
                delCustId = Integer.parseInt(delCustIdStr);
            } catch (Exception e)
            {
                return "B2BManageCustomers";
            }

            // Get the customer we need to delete from the admin eng
            CustomerIf currentCust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            delCust = kkAppEng.getAdminEng().getCustomerForIdWithOptions(kkAppEng.getSessionId(),
                    delCustId, null);
            if (delCust == null)
            {
                log.info("Customer with id = " + delCustId + " could not be found.");
                return "B2BManageCustomers";
            }

            if (delCust.getParentId() != currentCust.getId())
            {
                log.info("Customer with id = " + delCustId
                        + " is not a child of the current customer.");
                return "B2BManageCustomers";
            }

            kkAppEng.getNav().add(kkAppEng.getMsg("header.delete.b2bcustomer"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the delCust
     */
    public AdminCustomer getDelCust()
    {
        return delCust;
    }

    /**
     * @param delCust
     *            the delCust to set
     */
    public void setDelCust(AdminCustomer delCust)
    {
        this.delCust = delCust;
    }
}
