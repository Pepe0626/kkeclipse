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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.KKException;

/**
 * Called when LDAP is enabled to use LDAP for authenticating the user
 */
public class LDAPMgrCore
{
    /** the log */
    protected static Log log = LogFactory.getLog(LDAPMgrCore.class);

    protected String ldapUserName = null;

    protected String ldapPassword = null;

    protected String url = null;

    protected String personDN = null;

    /**
     * Constructor
     */
    public LDAPMgrCore()
    {
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
     * @throws Exception an unexpected exception unexpected exception
     */
    public int checkCredentials(String emailAddr, String password) throws Exception
    {
        DirContext ctx = null;

        try
        {
            Hashtable<String, String> environment = new Hashtable<String, String>();

            if (log.isDebugEnabled())
            {
                log.debug("LDAP connection URL                          =   " + url);
                log.debug("LDAP user name                               =   " + ldapUserName);
                log.debug("LDAP person object distinguished name (DN)   =   " + personDN);
            }

            if (ldapUserName == null)
            {
                throw new KKException(
                        "Cannot access LDAP because the MODULE_OTHER_LDAP_USER_NAME configuration variable hasn't been set.");
            }
            if (ldapPassword == null)
            {
                throw new KKException(
                        "Cannot access LDAP because the MODULE_OTHER_LDAP_PASSWORD configuration variable hasn't been set.");
            }
            if (url == null)
            {
                throw new KKException(
                        "Cannot access LDAP because the MODULE_OTHER_LDAP_URL configuration variable hasn't been set.");
            }
            if (personDN == null)
            {
                throw new KKException(
                        "Cannot validate through LDAP because the MODULE_OTHER_LDAP_PERSON_DN (Distinguished Name of Person Object) configuration variable hasn't been set.");
            }

            environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            environment.put(Context.SECURITY_AUTHENTICATION, "simple");
            environment.put(Context.PROVIDER_URL, url);
            environment.put(Context.SECURITY_PRINCIPAL, ldapUserName);
            environment.put(Context.SECURITY_CREDENTIALS, ldapPassword);

            /*
             * connect to LDAP using the credentials and connection string from the configuration
             * variables
             */
            try
            {
                ctx = new InitialDirContext(environment);
            } catch (Exception e)
            {
                log.error("Cannot connect to LDAP", e);
                return -1;
            }

            /* Specify the search filter on the eMail address */
            String filter = "(mail=" + emailAddr + ")";

            /*
             * limit returned attributes to those we care about. In this case we only require the
             * "cn" attribute which we will use to attempt to bind the user in order to validate his
             * password
             */
            String[] attrIDs =
            { "cn" };
            SearchControls ctls = new SearchControls();
            ctls.setReturningAttributes(attrIDs);
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            /* Search for objects using filter and controls */
            NamingEnumeration<SearchResult> answer = ctx.search(personDN, filter, ctls);

            /* close the connection */
            ctx.close();

            if (answer == null || !answer.hasMore())
            {
                return -1;
            }

            SearchResult sr = answer.next();
            Attributes attrs = sr.getAttributes();
            String cn = attrs.get("cn").toString();
            if (log.isDebugEnabled())
            {
                log.debug("cn of user with eMail (" + emailAddr + ") is " + cn);
            }

            /*
             * cn could be in the format "cn: Peter Smith, Pete Smith, Smithy" so we need to capture
             * just the first entry
             */
            if (cn != null)
            {
                if (cn.contains(","))
                {
                    cn = cn.split(",")[0];
                    if (cn.contains(":"))
                    {
                        cn = cn.split(":")[1];
                    }
                } else if (cn.contains(":"))
                {
                    cn = cn.split(":")[1];
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("Cleaned cn of user with eMail (" + emailAddr + ") is " + cn);
            }

            /* Now we try to bind as the user */
            String userName = "cn=" + cn + "," + personDN;

            if (log.isDebugEnabled())
            {
                log.debug("LDAP user name of user with eMail (" + emailAddr + ") is " + userName);
            }

            /* Bind as the user */
            environment.put(Context.SECURITY_PRINCIPAL, userName);
            environment.put(Context.SECURITY_CREDENTIALS, password);
            try
            {
                ctx = new InitialDirContext(environment);
            } catch (Exception e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Could not bind user " + userName);
                }
                return -1;
            }
            ctx.close();
            if (log.isDebugEnabled())
            {
                log.debug("user with eMail (" + emailAddr
                        + ") was successfully authenticated using LDAP");
            }
            return 1;
        } finally
        {
            if (ctx != null)
            {
                try
                {
                    ctx.close();
                } catch (NamingException e)
                {
                    log.error("Received an exception while closing the LDAP DirContext", e);
                }
            }
        }
    }

    /**
     * @return the ldapUserName
     */
    public String getLdapUserName()
    {
        return ldapUserName;
    }

    /**
     * @param ldapUserName
     *            the ldapUserName to set
     */
    public void setLdapUserName(String ldapUserName)
    {
        this.ldapUserName = ldapUserName;
    }

    /**
     * @return the ldapPassword
     */
    public String getLdapPassword()
    {
        return ldapPassword;
    }

    /**
     * @param ldapPassword
     *            the ldapPassword to set
     */
    public void setLdapPassword(String ldapPassword)
    {
        this.ldapPassword = ldapPassword;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return the personDN
     */
    public String getPersonDN()
    {
        return personDN;
    }

    /**
     * @param personDN
     *            the personDN to set
     */
    public void setPersonDN(String personDN)
    {
        this.personDN = personDN;
    }

}
