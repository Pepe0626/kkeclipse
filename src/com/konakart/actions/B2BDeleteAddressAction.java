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
import com.konakartadmin.app.AdminAddress;
import com.konakartadmin.app.AdminCustomer;

/**
 * Gets called before the B2B delete address page.
 */
public class B2BDeleteAddressAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    private int addrId;

    private AdminAddress delAddr;

    private int b2bCustId;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            // Check to see whether the user is logged in
            int custId = this.loggedIn(request, response, kkAppEng, "B2BAddressBook");
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

            String addrIdStr = request.getParameter("addrId");
            try
            {
                addrId = Integer.parseInt(addrIdStr);
            } catch (Exception e)
            {
                log.info("Invalid addrId parameter = " + addrIdStr);
                return "B2BAddressBook";
            }

            // Get the address
            delAddr = kkAppEng.getAdminEng().getAddressById(kkAppEng.getSessionId(), addrId);
            if (delAddr == null)
            {
                log.info("Address with id = " + addrId + " could not be found.");
                return "B2BAddressBook";
            }
            b2bCustId = delAddr.getCustomerId();

            CustomerIf currentCust = kkAppEng.getCustomerMgr().getCurrentCustomer();
            AdminCustomer addrCust = kkAppEng.getAdminEng().getCustomerForIdWithOptions(
                    kkAppEng.getSessionId(), delAddr.getCustomerId(), null);
            if (addrCust == null)
            {
                log.info("Customer with id = " + delAddr.getCustomerId() + " could not be found.");
                return "B2BAddressBook";
            }

            if (addrCust.getParentId() != currentCust.getId())
            {
                log.info("Customer with id = " + addrCust.getId()
                        + " is not a child of the current customer.");
                return "B2BAddressBook";
            }

            kkAppEng.getNav().add(kkAppEng.getMsg("header.delete.entry"), request);

            return SUCCESS;

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }

    /**
     * @return the addrId
     */
    public int getAddrId()
    {
        return addrId;
    }

    /**
     * @param addrId
     *            the addrId to set
     */
    public void setAddrId(int addrId)
    {
        this.addrId = addrId;
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
     * @return the delAddr
     */
    public AdminAddress getDelAddr()
    {
        return delAddr;
    }

    /**
     * @param delAddr
     *            the delAddr to set
     */
    public void setDelAddr(AdminAddress delAddr)
    {
        this.delAddr = delAddr;
    }

}
