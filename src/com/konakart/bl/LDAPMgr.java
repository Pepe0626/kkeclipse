//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.bl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.KKException;
import com.konakart.appif.KKEngIf;
import com.konakart.blif.LDAPMgrIf;

/**
 * Called when LDAP is enabled to use LDAP for authenticating the user
 */
public class LDAPMgr extends BaseMgr implements LDAPMgrIf
{
    /** the log */
    protected static Log log = LogFactory.getLog(LDAPMgr.class);

    /**
     * Constructor used when instantiated by the application engine
     * 
     * @param eng
     *            Application engine
     * @throws Exception an unexpected exception
     */
    public LDAPMgr(KKEngIf eng) throws Exception
    {
        super.init(eng, log);

        if (log.isDebugEnabled())
        {
            if (eng != null && eng.getEngConf() != null && eng.getEngConf().getStoreId() != null)
            {
                log.debug("LDAPMgr instantiated by application engine for store id = "
                        + eng.getEngConf().getStoreId());
            }
        }
    }

    /**
     * Called if the LDAP module is installed and active. This method should return:
     * <ul>
     * <li>A negative number in order for the login attempt to fail. The KonaKart login() method
     * will return a null sessionId</li>
     * <li>Zero to signal that this method is not implemented. The KonaKart login() method will
     * perform the credential check.</li>
     * <li>A positive number for the login attempt to pass. The KonaKart login() will not check
     * credentials, and will log in the customer, returning a valid session id.</li>
     * </ul>
     * This method may need to be modified slightly depending on the structure of your LDAP. The
     * example works when importing the exampleData.ldif file in the LDAP module jar:
     * 
     * <pre>
     * {@code
     * dn: cn=Robert Smith,ou=people,dc=example,dc=com
     * objectclass: inetOrgPerson
     * cn: Robert Smith
     * cn: Robert J Smith
     * cn: bob smith
     * sn: smith
     * uid: rjsmith
     * userpassword: rJsmitH
     * carlicense: HISCAR 123
     * homephone: 555-111-2222
     * mail: r.smith@example.com
     * mail: rsmith@example.com
     * mail: bob.smith@example.com
     * description: swell guy
     * }
     * </pre>
     * 
     * The code attempts to connect to LDAP using the username, password and URL in the
     * configuration variables set when the module was installed through the admin app.
     * <p>
     * After having connected, the person object is searched for using the email address of the
     * user. If found we use the "cn" attribute and the password of the user to attempt to bind to
     * LDAP. If the bind is successful, we return a positive number which means that authentication
     * was successful.
     * 
     * @param emailAddr
     *            The user name required to log in
     * @param password
     *            The log in password
     * @return Returns an integer
     * @throws KKException
     *            an unexpected KKException exception
     */
    public int checkCredentials(String emailAddr, String password) throws KKException
    {
        try
        {
            // Read Config Variables
            String ldapUserName = getConfigMgr().getConfigurationValue(false,
                    "MODULE_OTHER_LDAP_USER_NAME");
            String ldapPassword = getConfigMgr().getConfigurationValue(false,
                    "MODULE_OTHER_LDAP_PASSWORD");
            String url = getConfigMgr().getConfigurationValue(false, "MODULE_OTHER_LDAP_URL");
            String personDN = getConfigMgr().getConfigurationValue(false,
                    "MODULE_OTHER_LDAP_PERSON_DN");

            // Instantiate and call code in core class used by app and admin engines
            LDAPMgrCore core = new LDAPMgrCore();
            core.setLdapPassword(ldapPassword);
            core.setLdapUserName(ldapUserName);
            core.setPersonDN(personDN);
            core.setUrl(url);
            return core.checkCredentials(emailAddr, password);
        } catch (Exception e)
        {
            throw new KKException(e);
        }
    }

}
