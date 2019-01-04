//
// (c) 2004-2015 DS Data Systems UK Ltd, All rights reserved.
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

package com.konakartadmin.modules.others.ldap;

import java.util.Date;

import com.konakart.util.Utils;
import com.konakartadmin.app.KKConfiguration;
import com.konakartadmin.bl.KKAdminBase;
import com.konakartadmin.modules.OtherModule;

/**
 * LDAP module
 */
public class Ldap extends OtherModule
{
    /**
     * @return the config key stub
     */
    public String getConfigKeyStub()
    {
        if (configKeyStub == null)
        {
            setConfigKeyStub(super.getConfigKeyStub() + "_LDAP");
        }
        return configKeyStub;
    }

    public String getModuleTitle()
    {
        return getMsgs().getString("MODULE_OTHER_LDAP_TEXT_TITLE");
    }

    /**
     * @return the implementation filename
     */
    public String getImplementationFileName()
    {
        return "Ldap";
    }
    
    /**
     * @return the module code
     */
    public String getModuleCode()
    {
        return "ldap";
    }
    
    /**
     * @return an array of configuration values for this module
     */
    public KKConfiguration[] getConfigs()
    {
        if (configs == null)
        {
            configs = new KKConfiguration[8];
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
                /* title */"LDAP Status",
                /* key DO NOT CHANGE (Used by Engines to determine whether active or not) */"MODULE_OTHER_LDAP_STATUS",
                /* value */"true",
                /* description */"If set to false, the LDAP module will be unavailable",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"choice('true'='true','false'='false')",
                /* dateAdd */now);

        // 2
        configs[i] = new KKConfiguration(
        /* title */"Sort order of display",
        /* key */"MODULE_OTHER_LDAP_SORT_ORDER",
        /* value */"0",
        /* description */"Sort Order of LDAP module on the UI. Lowest is displayed first.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 3
        configs[i] = new KKConfiguration(
                /* title */"LDAP App Mgr Class Name",
                /* key */"MODULE_OTHER_LDAP_APP_MGR_CLASS",
                /* value */"com.konakart.bl.LDAPMgr",
                /* description */"LDAP Manager Class Name for App Engine.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        //4
        configs[i] = new KKConfiguration(
                /* title */"LDAP Admin Mgr Class Name",
                /* key */"MODULE_OTHER_LDAP_ADMIN_MGR_CLASS",
                /* value */"com.konakartadmin.bl.AdminLDAPMgr",
                /* description */"LDAP Manager Class Name for Admin Engine.",
                /* groupId */groupId,
                /* sortO */i++,
                /* useFun */"",
                /* setFun */"",
                /* dateAdd */now);

        // 5
        configs[i] = new KKConfiguration(
        /* title */"LDAP User Name",
        /* key */"MODULE_OTHER_LDAP_USER_NAME",
        /* value */"uid=admin,ou=system",
        /* description */"User Name for accessing LDAP.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 6
        configs[i] = new KKConfiguration(
        /* title */"LDAP Password",
        /* key */"MODULE_OTHER_LDAP_PASSWORD",
        /* value */"secret",
        /* description */"Password for accessing LDAP.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"password",
        /* dateAdd */now);

        // 7
        configs[i] = new KKConfiguration(
        /* title */"LDAP URL",
        /* key */"MODULE_OTHER_LDAP_URL",
        /* value */"ldap://localhost:10389",
        /* description */"URL for accessing LDAP.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        // 8
        configs[i] = new KKConfiguration(
        /* title */"Person Object DN",
        /* key */"MODULE_OTHER_LDAP_PERSON_DN",
        /* value */"ou=people,dc=example,dc=com",
        /* description */"Distinuished Name (DN) for accessing a person object.",
        /* groupId */groupId,
        /* sortO */i++,
        /* useFun */"",
        /* setFun */"",
        /* dateAdd */now);

        return configs;
    }
}