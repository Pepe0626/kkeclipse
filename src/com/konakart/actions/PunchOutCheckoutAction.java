//
// (c) 2012 DS Data Systems UK Ltd, All rights reserved.
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

import org.apache.commons.codec.binary.Base64;
import org.apache.struts2.ServletActionContext;

import com.konakart.al.KKAppEng;
import com.konakart.al.PunchOut;
import com.konakart.app.CreateOrderOptions;
import com.konakart.app.PunchOutOptions;
import com.konakart.appif.CreateOrderOptionsIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.PunchOutOptionsIf;
import com.konakart.appif.PunchOutResultIf;

/**
 * Gets called to send an XML message back to the ERP system
 */
public class PunchOutCheckoutAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    public String execute()
    {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        try
        {
            // Get an engine
            KKAppEng kkAppEng = this.getKKAppEng(request, response);

            /*
             * Check that we have a punch out object
             */
            if (kkAppEng.getPunchoutDetails() == null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("PunchOutCheckoutAction called but engine doesn't have a PunchOut object");
                }
                return WELCOME;
            }

            PunchOut po = kkAppEng.getPunchoutDetails();

            // Check the credentials
            String sessionId = kkAppEng.getEng().login(po.getUsername(), po.getPassword());
            if (sessionId == null || sessionId.length() == 0)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("PunchOutCheckoutAction called with invalid login credentials");
                }
                return WELCOME;
            }

            // Create the order from the basket items
            CreateOrderOptionsIf options = new CreateOrderOptions();
            options.setUseDefaultCustomer(false);

            // Add extra info to the options
            if (kkAppEng.getFetchProdOptions() != null)
            {
                options.setPriceDate(kkAppEng.getFetchProdOptions().getPriceDate());
                options.setCatalogId(kkAppEng.getFetchProdOptions().getCatalogId());
                options.setUseExternalPrice(kkAppEng.getFetchProdOptions().isUseExternalPrice());
                options.setUseExternalQuantity(kkAppEng.getFetchProdOptions()
                        .isUseExternalQuantity());
            }

            // Create the order
            OrderIf order = kkAppEng.getEng().createOrderWithOptions(sessionId,
                    kkAppEng.getCustomerMgr().getCurrentCustomer().getBasketItems(), options,
                    kkAppEng.getLangId());

            // Get the punch out message from the order
            PunchOutOptionsIf poOpts = new PunchOutOptions();
            poOpts.setStandard(PunchOutOptions.OCI_HTML);
            poOpts.setVersion("4.0");

            PunchOutResultIf ret = kkAppEng.getEng().getPunchOutMessage(sessionId, order, poOpts);

            if (poOpts.getStandard().equals(PunchOutOptions.OCI_XML))
            {
                // Encode the message using Base64 encoding
                po.setMessage(Base64.encodeBase64String(ret.getRetString().getBytes()));
                if (log.isDebugEnabled())
                {
                    log.debug("Encoded Punchout Message:");
                    log.debug(po.getMessage());
                }
            } else if (poOpts.getStandard().equals(PunchOutOptions.OCI_HTML))
            {
                po.setParmArray(ret.getRetParms());
            }

            // Logout
            kkAppEng.getEng().logout(sessionId);

            // Remove items from cart
            kkAppEng.getEng().removeBasketItemsPerCustomer(null,
                    kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
            kkAppEng.getBasketMgr().getBasketItemsPerCustomer();

            // Redirect back to the ERP system with the punch out message
            // Redirect back to the ERP system with the punch out message
            if (poOpts.getStandard().equals(PunchOutOptions.OCI_XML))
            {
                return "Punchout_OCI_XML";
            }
            return "Punchout_OCI_HTML";

        } catch (Exception e)
        {
            return super.handleException(request, e);
        }
    }
}
