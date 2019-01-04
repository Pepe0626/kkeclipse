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

package com.konakartadmin.modules.others.paypallogin;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.OtherModule;

/**
 * PayPal Login module
 */
public class PaypalLogin extends OtherModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_PAYPAL_LOGIN");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_OTHER_PAYPAL_LOGIN_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "PaypalLogin";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "paypal_login";
    }

    /**
     * @return an array of configuration values for this module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[10];
        }

        if (configs[0] != null && !Utils.isBlank(configs[0].getConfigurationKey()))
        {
            return configs;
        }

        Date now = KKAdminBase.getKonakartTimeStampDate();

        int i = 0;
        int groupId = 6;

        // 1
        configs[i] = new KKConfiguration(
                /* title */"PayPal Login Status",
                /* key DO NOT CHANGE (Used by Engines to determine whether active or not) */"MODULE_OTHER_PAYPAL_LOGIN_STATUS",
                /* value */"true",
                /* description */"If set to false, the PayPal Login module will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now,
                /* returnByApi */true);

        // 2
        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_OTHER_PAYPAL_LOGIN_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of PayPal Login module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
        /* title */"Client ID",
        /* key */"MODULE_OTHER_PAYPAL_CLIENT_ID",
        /* value */"XXXXXXXXXXXXXXXXX",
        /* description */"Client ID of PayPal App used for login.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* returnByApi */true);

        // 4
        configs[i] = new KKConfiguration(
        /* title */"Secret Key",
        /* key */"MODULE_OTHER_PAYPAL_SECRET_KEY",
        /* value */"XXXXXXXXXXXXXXXXX",
        /* description */"Secret Key of PayPal App used for login.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now,
        /* returnByApi */false);

        // 5
        configs[i] = new KKConfiguration(
        /* title */"PayPal Login Module Class Name",
        /* key */"MODULE_OTHER_PAYPAL_LOGIN_CLASS",
        /* value */"com.konakart.bl.modules.others.paypallogin.PaypalLogin",
        /* description */"PayPal Login module class.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* returnByApi */true);

        // 6
        configs[i] = new KKConfiguration(
        /* title */"Run in Sandbox",
        /* key */"MODULE_OTHER_PAYPAL_LOGIN_RUN_IN_SANDBOX",
        /* value */"true",
        /* description */"If set to false, the live PayPal system will be used",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"choice('true', 'false')",
        /* dateAdd */now,
        /* returnByApi */true);

        // 7
        configs[i] = new KKConfiguration(
        /* title */"PayPal Sandbox API Endpoint",
        /* key */"MODULE_OTHER_PAYPAL_LOGIN_SANDBOX_URL",
        /* value */"https://api.sandbox.paypal.com",
        /* description */"Endpoint for Sandbox PayPal REST APIs.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* returnByApi */true);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"PayPal Live API Endpoint",
        /* key */"MODULE_OTHER_PAYPAL_LOGIN_LIVE_URL",
        /* value */"https://api.paypal.com",
        /* description */"Endpoint for Live PayPal REST APIs.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* returnByApi */true);

        // 9
        configs[i] = new KKConfiguration(
        /* title */"Return URL",
        /* key */"MODULE_OTHER_PAYPAL_LOGIN_RETURN_URL",
        /* value */"http://localhost:8780/konakart/PaypalLogin.action",
        /* description */"Returned to this URL after PayPal login",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* returnByApi */true);

        // 10
        configs[i] = new KKConfiguration(
        /* title */"Scopes",
        /* key */"MODULE_OTHER_PAYPAL_LOGIN_SCOPES",
        /* value */"openid profile email address phone",
        /* description */"Defines the data that can be retrieved from PayPal",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* returnByApi */true);

        return configs;
    }
}