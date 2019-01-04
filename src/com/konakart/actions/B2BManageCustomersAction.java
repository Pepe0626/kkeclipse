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
import com.konakart.app.KKException;
import com.konakart.appif.CustomerIf;
import com.konakartadmin.app.AdminCustomer;
import com.konakartadmin.app.AdminGetCustomerOptions;

/**
 * Opens a page where a B2B Administrator can manage his users
 */
public class B2BManageCustomersAction extends B2BBaseAction
{
    private static final long serialVersionUID = 1L;

    private AdminCustomer[] users;

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

            kkAppEng.getNav().add(kkAppEng.getMsg("header.user.admin"), request);

            // Ensure that the current customer is of the correct type
            if (!isB2BAdmin(kkAppEng))
            {
                return ERROR;
            }

            // Get the children of this customer
            CustomerIf currentCust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            AdminGetCustomerOptions options = new AdminGetCustomerOptions();
            options.setGetChildren(true);
            AdminCustomer currentCustAdmin = kkAppEng.getAdminEng().getCustomerForIdWithOptions(
                    kkAppEng.getSessionId(), currentCust.getId(), options);
            if (currentCustAdmin == null)
            {
                throw new KKException(
                        "Cannot find Admin Customer with id = " + currentCust.getId());
            }

            users = currentCustAdmin.getChildren();

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }

    }

    /**
     * @return the users
     */
    public AdminCustomer[] getUsers()
    {
        return users;
    }

    /**
     * @param users
     *            the users to set
     */
    public void setUsers(AdminCustomer[] users)
    {
        this.users = users;
    }

}
