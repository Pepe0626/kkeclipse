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

package com.konakartadmin.modules.others.messengerplugin;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.OtherModule;

/**
 * Facebook Messenger Plugin module
 */
public class MessengerPlugin extends OtherModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_MESSENGER_PLUGIN");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_OTHER_MESSENGER_PLUGIN_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "MessengerPlugin";
    }

    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "MESSENGER_PLUGIN";
    }

    /**
     * @return an array of configuration values for this module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[5];
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
                /* title */"Messenger Plugin Status",
                /* key DO NOT CHANGE (Used by Engines to determine whether active or not) */"MODULE_OTHER_MESSENGER_PLUGIN_STATUS",
                /* value */"true",
                /* description */"If set to false, the Messenger Plugin module will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now,
                /* returnByApi */true);

        // 2
        configs[i] = new KKConfiguration(
                /* title */"Sort order of display",
                /* key */"MODULE_OTHER_MESSENGER_PLUGIN_SORT_ORDER",
                /* value */"0",
                /* description */"Sort Order of Messenger Plugin module on the UI. Lowest is displayed first.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
        /* title */"Facebook App ID",
        /* key */"MODULE_OTHER_MESSENGER_PLUGIN_FACEBOOK_APP_ID",
        /* value */"XXXXXXXXXXXXXXXXX",
        /* description */"Facebook App ID.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* returnByApi */true);

        // 4
        configs[i] = new KKConfiguration(
        /* title */"Facebook Page ID",
        /* key */"MODULE_OTHER_MESSENGER_PLUGIN_FACEBOOK_PAGE_ID",
        /* value */"XXXXXXXXXXXXXXXXX",
        /* description */"Facebook Page ID.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now,
        /* returnByApi */true);

        // 5
        configs[i] = new KKConfiguration(
                /* title */"Minimized",
                /* key */"MODULE_OTHER_MESSENGER_PLUGIN_MINIMIZED",
                /* value */"true",
                /* description */"If set to false, the Messenger Plugin module will not be minimized by default",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true', 'false')",
                /* dateAdd */now,
                /* returnByApi */true);


        return configs;
    }
}