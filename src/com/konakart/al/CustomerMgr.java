//
// (c) 2006 DS Data Systems UK Ltd, All rights reserved.
//
// DS Data Systems and KonaKart and their respective logos, are 
// trademarks of DS Data Systems UK Ltd. All rights reserved.
//
// The information in this document is the proprietary property of
// DS Data Systems UK Ltd. and is protected by English copyright law,
// the laws of foreign jurisdictions, and international treaties,
// as applicable. No part of this document may be reproduced,
// transmitted, transcribed, transferred, modified, published, or
// translated into any language, in any form or by any means, for
// any purpose other than expressly permitted by DS Data Systems UK Ltd.
// in writing.
//
package com.konakart.al;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;

import com.konakart.actions.BaseAction;
import com.konakart.app.AddToWishListOptions;
import com.konakart.app.Customer;
import com.konakart.app.CustomerSearch;
import com.konakart.app.CustomerTag;
import com.konakart.app.EditCustomerOptions;
import com.konakart.app.EmailOptions;
import com.konakart.app.ExternalLoginResult;
import com.konakart.app.KKException;
import com.konakart.app.LoginInput;
import com.konakart.app.LoginResult;
import com.konakart.appif.AddressIf;
import com.konakart.appif.BasketIf;
import com.konakart.appif.CatalogIf;
import com.konakart.appif.CountryIf;
import com.konakart.appif.CustomerIf;
import com.konakart.appif.CustomerRegistrationIf;
import com.konakart.appif.CustomerSearchIf;
import com.konakart.appif.CustomerTagIf;
import com.konakart.appif.EditCustomerOptionsIf;
import com.konakart.appif.EmailOptionsIf;
import com.konakart.appif.ExternalLoginInputIf;
import com.konakart.appif.FetchProductOptionsIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.LoginResultIf;
import com.konakart.appif.OrderIf;
import com.konakart.appif.ProductIf;
import com.konakart.appif.StoreIf;
import com.konakart.appif.VendorIf;
import com.konakart.appif.WishListIf;
import com.konakart.appif.WishListsIf;
import com.konakart.appif.ZoneIf;
import com.konakart.bl.ConfigConstants;
import com.konakart.util.KKConstants;

/**
 * Contains methods to manage customer details and login / logout.
 */
public class CustomerMgr extends BaseMgr
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(CustomerMgr.class);

    // Hash map for storing tag information
    private HashMap<String, CustomerTagIf> tagMap = new HashMap<String, CustomerTagIf>();

    /* The currently selected address object for editing or deleting */
    private AddressIf selectedAddr;

    // Current logged in user
    private CustomerIf currentCustomer = null;

    // Current vendor when in multi-vendor mode
    private VendorIf currentVendor = null;

    // Current store when in multi-vendor mode
    private StoreIf currentStore = null;

    // Currently selected country
    private CountryIf selectedCountry;

    // Array of zones that a customer can choose from when registering. They depend on the selected
    // country.
    private ZoneIf[] selectedZones;

    // Used to store information on how login was performed
    private String loginType;

    /** Used for loginType */
    public static final String FACEBOOK = "FACEBOOK";

    /** Used for loginType */
    public static final String GOOGLEPLUS = "GOOGLEPLUS";

    /** Used for loginType */
    public static final String PAYPAL = "PAYPAL";

    /** Used for loginType */
    public static final String LIFERAY = "LIFERAY";

    // True when a customer hasn't added a name yet
    private boolean noName = false;

    // True when a customer hasn't added a gender yet
    private boolean noGender = false;

    // True when a customer hasn't created an address yet
    private boolean noAddress = false;

    // True when a customer hasn't added a telephone number yet
    private boolean noTelephone = false;

    // True when a customer hasn't added a date of birth yet
    private boolean noBirthDate = false;

    // Set to true if customer tag functionality is installed
    private boolean customerTagsAvailable = true;

    // When false the customer cannot add or update addresses
    private boolean canChangeAddress = true;

    // The catalog key used for this customer
    private String catalogKey = null;

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     */
    protected CustomerMgr(KKEngIf eng, KKAppEng kkAppEng)
    {
        this.eng = eng;
        this.kkAppEng = kkAppEng;
        /* Set the selected country which also sets the selected zones. */
        String countryIdStr = kkAppEng.getConfig("STORE_COUNTRY");
        if (countryIdStr != null)
        {
            int id = 0;
            try
            {
                id = Integer.parseInt(countryIdStr);
                // Set selected country and zones
                setSelectedCountry(kkAppEng.getCountry(id));
            } catch (NumberFormatException e)
            {
                log.warn("The configuration variable STORE_COUNTRY has an invalid value - "
                        + countryIdStr + ". It should be the numeric id of the country.");
            } catch (KKException e)
            {
                log.warn("Problem calling getCountry()", e);
            }
        }
    }

    /**
     * Ensures that the selectedAddr attribute is populated. The addrId should match the id of one
     * of the addresses of the current customer.
     * 
     * @param addrId
     *            Address Id of one of the addresses of the current customer
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void setSelectedAddrFromId(int addrId) throws KKAppException, KKException
    {
        if (currentCustomer == null)
        {
            throw new KKAppException("You need to be logged in, in order to select an address.");
        }

        if (currentCustomer.getAddresses() == null)
        {
            populateCurrentCustomerAddresses(/* force */true);
        }

        if (currentCustomer.getAddresses() == null)
        {
            throw new KKAppException("The current customer has no addresses.");
        }

        AddressIf addrFound = null;

        for (int i = 0; i < currentCustomer.getAddresses().length; i++)
        {
            AddressIf addr = currentCustomer.getAddresses()[i];
            if (addr.getId() == addrId)
            {
                addrFound = addr;
                break;
            }
        }

        if (addrFound != null)
        {
            selectedAddr = addrFound;
        } else
        {
            throw new KKAppException(
                    "The address referenced by id = " + addrId + " could not be found");
        }
    }

    /**
     * Call the engine to edit the customer address.
     * 
     * @param addr
     *            The address object to be edited
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void editCustomerAddress(AddressIf addr) throws KKException, KKAppException
    {
        eng.editCustomerAddress(kkAppEng.getSessionId(), addr);
        // Make sure we get the updated address from the DB
        populateCurrentCustomerAddresses(/* force */true);
    }

    /**
     * Call the engine to delete the customer address.
     * 
     * @param addrId
     *            The address Id of the address to be deleted.
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void deleteAddressFromCustomer(int addrId) throws KKException, KKAppException
    {
        eng.deleteAddressFromCustomer(kkAppEng.getSessionId(), addrId);
        // Make sure we get the updated address from the DB
        populateCurrentCustomerAddresses(/* force */true);
    }

    /**
     * Call the engine to create a new customer address which is added to the addresses of the
     * current customer.
     * 
     * @param addr
     *            The address to be added
     * @return Returns the id of the address object
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int addAddressToCustomer(AddressIf addr) throws KKException, KKAppException
    {
        int addrId = eng.addAddressToCustomer(kkAppEng.getSessionId(), addr);
        // Make sure we get the updated addresses from the DB
        populateCurrentCustomerAddresses(/* force */true);
        return addrId;
    }

    /**
     * Returns the currently selected address.
     * 
     * @return Returns the selectedAddr.
     */
    public AddressIf getSelectedAddr()
    {
        return selectedAddr;
    }

    /**
     * Sets the selected address to the one passed in as a parameter.
     * 
     * @param selectedAddr
     *            The selectedAddr to set.
     */
    public void setSelectedAddr(AddressIf selectedAddr)
    {
        this.selectedAddr = selectedAddr;
    }

    /**
     * Fetch the product notifications for a customer and language and set them on the customer
     * object. Each item has a product object which isn't however fully populated.
     * 
     * We set an empty array rather than null because an empty array indicates that there are no
     * notifications rather than null, which indicates that we haven't read them from the DB yet.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void fetchProductNotificationsPerCustomer() throws KKException
    {
        if (currentCustomer != null)
        {
            ProductIf[] pArray = eng.getProductNotificationsPerCustomerWithOptions(
                    kkAppEng.getSessionId(), kkAppEng.getLangId(), kkAppEng.getFetchProdOptions());
            if (pArray == null)
            {
                pArray = new ProductIf[0];
            }
            currentCustomer.setProductNotifications(pArray);
        }
    }

    /**
     * Add the product notification to the currently logged in customer.
     * 
     * @param productId
     *            The id of the product to be added
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void addProductNotificationsToCustomer(int productId) throws KKException
    {
        if (currentCustomer != null)
        {
            eng.addProductNotificationToCustomer(kkAppEng.getSessionId(), productId);
            // Refresh the list
            fetchProductNotificationsPerCustomer();
        }
    }

    /**
     * Delete the product notification from the customer's list of notifications.
     * 
     * @param productId
     *            The id of the product to be removed
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void deleteProductNotificationsFromCustomer(int productId) throws KKException
    {
        if (currentCustomer != null)
        {
            eng.deleteProductNotificationFromCustomer(kkAppEng.getSessionId(), productId);
            // Refresh the list
            fetchProductNotificationsPerCustomer();
        }
    }

    /**
     * Calls the engine to edit the current customer's locale
     * 
     * @param locale
     *            The new locale
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void editCustomerLocale(String locale) throws KKException
    {
        if (currentCustomer != null && currentCustomer.getId() > -1)
        {
            currentCustomer.setLocale(locale);
            editCustomer(currentCustomer);
        }
    }

    /**
     * Calls the engine to update the customer data with the data passed in as a parameter.
     * 
     * @param cust
     *            The Customer object to be edited
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void editCustomer(CustomerIf cust) throws KKException
    {
        EditCustomerOptionsIf options = new EditCustomerOptions();
        options.setUsernameUnique(true);
        eng.editCustomerWithOptions(kkAppEng.getSessionId(), cust, options);

        // Update currentCustomer with the latest data. Making sure we don't lose the basket or the
        // wish list or orders
        BasketIf[] basketItems = currentCustomer.getBasketItems();
        WishListIf[] wishLists = currentCustomer.getWishLists();
        OrderIf[] orders = currentCustomer.getOrders();
        AddressIf[] addresses = currentCustomer.getAddresses();
        setCurrentCustomer(eng.getCustomer(kkAppEng.getSessionId()));
        currentCustomer.setBasketItems(basketItems);
        currentCustomer.setWishLists(wishLists);
        currentCustomer.setOrders(orders);
        currentCustomer.setAddresses(addresses);
    }

    /**
     * Ensures that the currentCustomer object has his default address and array of addresses
     * populated
     * 
     * @param force
     *            If set to true the addresses will be refreshed even if they already exist
     * @return Returns the customer with populated addresses
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * 
     */
    public CustomerIf populateCurrentCustomerAddresses(boolean force)
            throws KKException, KKAppException
    {

        if (currentCustomer == null)
        {
            throw new KKAppException("The user is not logged in");
        }

        if (force || currentCustomer.getAddresses() == null
                || currentCustomer.getDefaultAddr() == null)
        {
            AddressIf[] addresses = eng.getAddressesPerCustomer(kkAppEng.getSessionId());
            if (addresses == null || addresses.length == 0)
            {
                throw new KKAppException(
                        "The current user has no addresses set. All registered users should have at least one address set.");
            }
            // The first address in the array is always the default one
            currentCustomer.setDefaultAddr(addresses[0]);
            currentCustomer.setAddresses(addresses);

            /* If the customer hasn't added an address yet then set NoAddress to true */
            if (addresses[0].getCity().length() < 2 && addresses[0].getStreetAddress().length() < 2)
            {
                setNoAddress(true);
            } else
            {
                setNoAddress(false);
            }
        }

        return currentCustomer;
    }

    /**
     * We create a customer object for a guest. We give it a negative id which will never be used by
     * a real customer. The reason we do this is so we can reuse all for the logic for the customer
     * cart even for a guest without having to create new logic in order to store a guest cart.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected void createGuest() throws KKException
    {

        /* get an id for the customer object. This temporary id is negative. */
        int id = kkAppEng.getEng().getTempCustomerId();

        /* Remove any existing basket items */
        eng.removeBasketItemsPerCustomer(kkAppEng.getSessionId(), id);

        /* Create the customer object */
        CustomerIf cust = new Customer();
        cust.setId(id);
        cust.setGlobalProdNotifier(0);
        setCurrentCustomer(cust);
    }

    /**
     * Returns the current customer. If the id of the current customer is negative, this means that
     * the customer hasn't logged in yet and that it is a temporary object used so that the customer
     * can still create basket items.
     * 
     * @return Current Customer
     */
    public CustomerIf getCurrentCustomer()
    {
        return currentCustomer;
    }

    /**
     * Register a new customer.
     * 
     * @param cr
     *            The CustomerRegistration object
     * @return Returns the id of the new customer
     * @throws KKException
     *            an unexpected KKException exception
     */
    public int registerCustomer(CustomerRegistrationIf cr) throws KKException
    {
        cr.setLocale(kkAppEng.getLocale());
        int customerId = eng.registerCustomer(cr);
        return customerId;
    }

    /**
     * Method used when a customer is allowed to checkout without registering..
     * 
     * @param cr
     *            The CustomerRegistration object
     * @return Returns the id of the new customer
     * @throws KKException
     *            an unexpected KKException exception
     */
    public int forceRegisterCustomer(CustomerRegistrationIf cr) throws KKException
    {
        cr.setLocale(kkAppEng.getLocale());
        /*
         * Uncomment the line below if you want to allow registered customers to checkout without
         * logging in, by creating a temporary customer.
         */
        // cr.setAllowMultipleRegistrations(true);
        int customerId = eng.forceRegisterCustomer(cr);
        return customerId;
    }

    /**
     * Login and if successful, set the current customer object. If before login, the customer had
     * placed items in the basket, these items are not lost once the customer logs in. The session
     * id is returned but it is also stored by the client engine so that it is used automatically by
     * the client engine when it has to communicate with the server engine.
     * 
     * @param loginUsername
     *            The user id used for login (email or username)
     * @param password
     *            The password
     * @return Return a LoginResult object containing the session id if successful
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public LoginResultIf login(String loginUsername, String password)
            throws KKException, KKAppException
    {
        /*
         * We only keep the affiliate id for the first login. If the customer logs in again or a new
         * customer logs in to the same engine we don't use the affiliate id.
         */
        if (this.kkAppEng.getSessionId() != null)
        {
            // Set affiliate id to null
            kkAppEng.setAffiliateId(null);
        }

        LoginResultIf ret = new LoginResult();
        try
        {
            if (this.kkAppEng.getVersionInt() < 8030000)
            {
                String sessionId = eng.login(loginUsername, password);
                ret.setSessionId(sessionId);
            } else
            {
                /*
                 * If the emailAddr isn't a valid email then we assume that the customer is using
                 * his username to login
                 */
                EmailValidator ev = EmailValidator.getInstance();
                if (ev.isValid(loginUsername))
                {
                    ret = eng.loginWithOptions(new LoginInput(loginUsername, password));
                } else
                {
                    LoginInput li = new LoginInput();
                    li.setUsername(loginUsername);
                    li.setPassword(password);
                    ret = eng.loginWithOptions(li);
                }
            }
            this.kkAppEng.setSessionId(ret.getSessionId());
        } catch (KKException e)
        {
            if (e.getCode() == KKException.KK_STORE_DELETED
                    || e.getCode() == KKException.KK_STORE_DISABLED
                    || e.getCode() == KKException.KK_STORE_UNDER_MAINTENANCE)
            {
                throw e;
            }
            log.debug(e.getMessage());
            return ret;
        }

        afterLogin(/* clearAffiliateId */false, password);

        return ret;
    }

    /**
     * Login using a login module (such as Facebook login) and if successful, set the current
     * customer object. If before login, the customer had placed items in the basket, these items
     * are not lost once the customer logs in. The session id is returned but it is also stored by
     * the client engine so that it is used automatically by the client engine when it has to
     * communicate with the server engine.
     * 
     * @param loginInfo
     *            the loginInfo
     * @param loginType
     *            the loginType
     * 
     * @return Return the session id
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public String externalLogin(ExternalLoginInputIf loginInfo, String loginType)
            throws KKException, KKAppException
    {
        /*
         * We only keep the affiliate id for the first login. If the customer logs in again or a new
         * customer logs in to the same engine we don't use the affiliate id.
         */
        if (this.kkAppEng.getSessionId() != null)
        {
            // Set affiliate id to null
            kkAppEng.setAffiliateId(null);
        }

        try
        {
            this.kkAppEng.setSessionId(null);
            ExternalLoginResult ret = (ExternalLoginResult) eng.externalLogin(loginInfo);
            if (ret != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(ret.toString());
                }
                this.kkAppEng.setSessionId(ret.getSessionId());
            }
        } catch (KKException e)
        {
            if (e.getCode() == KKException.KK_STORE_DELETED
                    || e.getCode() == KKException.KK_STORE_DISABLED
                    || e.getCode() == KKException.KK_STORE_UNDER_MAINTENANCE)
            {
                throw e;
            }
            log.debug(e.getMessage());
            this.loginType = loginType;
            return null;
        }

        afterLogin(/* clearAffiliateId */false, /* password */null);

        this.loginType = loginType;

        return this.kkAppEng.getSessionId();
    }

    /**
     * Login for the customer identified by customerId and if successful, set the current customer
     * object. The session id is returned but it is also stored by the client engine so that it is
     * used automatically by the client engine when it has to communicate with the server engine.
     * The adminSession must be a valid session belonging to an administrator.
     * 
     * @param adminSession
     *            Valid session belonging to an administrator
     * @param customerId
     *            Id of the customer being logged in
     * @return The session id
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public String loginByAdmin(String adminSession, int customerId)
            throws KKException, KKAppException
    {
        try
        {
            this.kkAppEng.setSessionId(eng.loginByAdmin(adminSession, customerId));
        } catch (KKException e)
        {
            log.debug(e.getMessage());
            logout();
            return null;
        }

        afterLogin(/* clearAffiliateId */true, /* password */null);

        return this.kkAppEng.getSessionId();
    }

    /**
     * This method is used to enter the store-front application using the session of a logged in
     * user.
     * 
     * @param sessionId
     *            the sessionId
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void loginBySession(String sessionId) throws KKException, KKAppException
    {
        this.kkAppEng.setSessionId(sessionId);

        afterLogin(/* clearAffiliateId */true, /* password */null);
    }

    /**
     * Private method to do some housekeeping after a successful login.
     * 
     * @param clearAffiliateId
     *            the clearAffiliateId
     * @param password
     *            the password
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void afterLogin(boolean clearAffiliateId, String password)
            throws KKException, KKAppException
    {
        loginType = null;

        if (this.kkAppEng.getSessionId() != null)
        {
            // Set Admin User to null
            kkAppEng.setAdminUser(null);

            // Set the punch out object to null
            kkAppEng.setPunchoutDetails(null);

            // Clear the Affiliate Id
            if (clearAffiliateId)
            {
                kkAppEng.setAffiliateId(null);
            }

            CustomerIf guest = null;
            // Save the current guest id
            if (currentCustomer != null)
            {
                guest = new Customer();
                guest.setId(currentCustomer.getId());
            }

            // Get a customer object from the login process
            setCurrentCustomer(eng.getCustomer(this.kkAppEng.getSessionId()));

            // Ensure that the customer's locale is set. If the customer was created through the
            // admin app, it will not be set.
            if (currentCustomer != null)
            {
                if (currentCustomer.getLocale() == null || (currentCustomer.getLocale() != null
                        && !currentCustomer.getLocale().equals(this.kkAppEng.getLocale())))
                {
                    editCustomerLocale(kkAppEng.getLocale());
                }
            }

            // Add any items from the guest basket to the permanent basket only if the guest id is <
            // 0. If we do a login when we are already logged in, the guest would be a real customer
            // and we would double the quantity of the basket
            if (guest != null && guest.getId() < 0)
            {
                eng.mergeBasketsWithOptions(this.kkAppEng.getSessionId(), guest.getId(),
                        this.kkAppEng.getBasketMgr().getAddToBasketOptions());
                eng.removeBasketItemsPerCustomer(null, guest.getId());

                String tagsEnabled = kkAppEng.getConfig(ConfigConstants.ENABLE_CUSTOMER_CART_TAGS);
                if (tagsEnabled != null && tagsEnabled.equalsIgnoreCase("true"))
                {
                    CustomerTag ct = new CustomerTag();
                    ct.setName(BaseAction.TAG_CART_TOTAL);
                    ct.setValue("0");
                    eng.insertCustomerTagForGuest(guest.getId(), ct);

                    ct.setName(BaseAction.TAG_PRODUCTS_IN_CART);
                    ct.setValue("");
                    eng.insertCustomerTagForGuest(guest.getId(), ct);

                    ct.setName(BaseAction.TAG_PRODUCTS_TIME_IN_CART);
                    ct.setValue("");
                    eng.insertCustomerTagForGuest(guest.getId(), ct);
                }

                if (kkAppEng.getWishListMgr().allowWishListWhenNotLoggedIn())
                {
                    eng.mergeWishListsWithOptions(this.kkAppEng.getSessionId(), guest.getId(),
                            kkAppEng.getLangId(),
                            kkAppEng.getWishListMgr().getAddToWishListOptions());

                    // Now delete the wish lists of the temporary customer
                    CustomerSearchIf search = new CustomerSearch();
                    search.setTmpId(guest.getId());
                    WishListsIf wishLists = eng.searchForWishLists(null, null, search);
                    if (wishLists != null && wishLists.getWishListArray() != null)
                    {
                        AddToWishListOptions opts = new AddToWishListOptions();
                        opts.setCustomerId(guest.getId());
                        for (int i = 0; i < wishLists.getWishListArray().length; i++)
                        {
                            WishListIf wl = wishLists.getWishListArray()[i];
                            eng.deleteWishListWithOptions(null, wl.getId(), opts);
                        }
                    }
                }
            }

            // Refresh the data relevant to the customer such as his basket and recent orders and
            // content
            refreshCustomerCachedData();

            // Populate the customer's addresses
            if (currentCustomer != null)
            {
                populateCurrentCustomerAddresses(/* force */false);
            }

            // Get an admin app session id if the user is a B2B Admin user
            if (currentCustomer != null && password != null && currentCustomer
                    .getType() == com.konakart.bl.CustomerMgr.CUST_TYPE_B2B_COMPANY_ADMIN)
            {
                try
                {
                    String adminSessionId = kkAppEng.getAdminEng()
                            .login(currentCustomer.getEmailAddr(), password);
                    kkAppEng.setSessionId(adminSessionId);
                } catch (Exception e)
                {
                    log.warn("Unable to log B2B Company Admin User "
                            + currentCustomer.getEmailAddr() + " into the Admin App", e);
                }
            }

            // Call the callout class method where custom code can be placed
            new KKAppEngCallouts().afterLogin(getKkAppEng());
        }
    }

    /**
     * Normally called after a login to get and cache customer relevant data such as the customer's
     * basket, the customer's orders and the customer's order history. If this method isn't called,
     * then the UI will not show updated data.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void refreshCustomerCachedData() throws KKException, KKAppException
    {
        // Get the tag information for this customer
        getCustomerTags(this.kkAppEng.getSessionId());

        // Get the customer's basket from the DB
        this.kkAppEng.getBasketMgr().getBasketItemsPerCustomer();

        // Populate the customer's orders array with the last three orders he made
        this.kkAppEng.getOrderMgr().populateCustomerOrders();

        // Get the digital downloads for this customer
        this.kkAppEng.getProductMgr().fetchDigitalDownloads();

        // Get wish lists for this customer
        String wishListEnabled = kkAppEng.getConfig(ConfigConstants.ENABLE_WISHLIST);
        if (wishListEnabled != null && wishListEnabled.equalsIgnoreCase("TRUE"))
        {
            this.kkAppEng.getWishListMgr().fetchCustomersWishLists();
        }

        // Remove various cached values
        this.kkAppEng.getOrderMgr().setCouponCode(null);
        this.kkAppEng.getOrderMgr().setGiftCertCode(null);
        this.kkAppEng.getOrderMgr().setRewardPoints(0);

        // Remove cached content since after logging in content may be filtered by expressions
        this.kkAppEng.getContentMgr().refreshCaches();
    }

    /**
     * Get the customer tags for this customer and add them to the hash map
     * 
     * @param sessionId
     *            the sessionId
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected void getCustomerTags(String sessionId) throws KKException
    {
        if (customerTagsAvailable)
        {
            try
            {
                tagMap.clear();
                CustomerTagIf[] tags = eng.getCustomerTags(sessionId);
                for (int i = 0; i < tags.length; i++)
                {
                    CustomerTagIf tag = tags[i];
                    tagMap.put(tag.getName(), tag);
                }

                /*
                 * Set a boolean used by the JSPs to show / hide links that allow the customer to
                 * add and edit addresses
                 */
                if (getTagValueAsBool(KKConstants.B2B_CAN_CHANGE_ADDRESS, true) == false)
                {
                    canChangeAddress = false;
                } else
                {
                    canChangeAddress = true;
                }

                /*
                 * Sets the catalog for this customer
                 */
                setCatalogKeyForCustomer();

            } catch (KKException e)
            {
                if (e.getCode() == KKException.KK_FUNCTIONALITY_NOT_INSTALLED)
                {
                    customerTagsAvailable = false;
                    return;
                }
                throw e;
            }
        }
    }

    /**
     * Set the catalog key whenever the customer logs in if tags are enabled and the customer has a
     * B2B_CATALOG_KEY customer tag containing the catalog key which is different to the current
     * key.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected void setCatalogKeyForCustomer() throws KKException
    {
        if (customerTagsAvailable)
        {
            catalogKey = getTagValueAsString(KKConstants.B2B_CATALOG_KEY);
            if (catalogKey == null || catalogKey.length() == 0)
            {
                // See if a default catalog has been defined
                catalogKey = kkAppEng.getConfig("DEFAULT_CATALOG_KEY");
            }
            
            if (catalogKey != null && catalogKey.length() > 0
                    && this.kkAppEng.getFetchProdOptions() != null && !Objects.equals(catalogKey,
                            this.kkAppEng.getFetchProdOptions().getCatalogId()))
            {

                CatalogIf cat = eng.getCatalogPerKey(catalogKey);
                if (cat != null)
                {
                    this.kkAppEng.getFetchProdOptions().setCatalogId(catalogKey);
                    this.kkAppEng.getFetchProdOptions()
                            .setUseExternalPrice(cat.isUseCatalogPrices());
                    this.kkAppEng.getFetchProdOptions()
                            .setUseExternalQuantity(cat.isUseCatalogQuantities());

                    // Ensure that everything is refreshed
                    this.kkAppEng.setFetchProdOptions(this.kkAppEng.getFetchProdOptions());

                    // Create a category structure with product counts matching the catalog
                    this.kkAppEng.getCategoryMgr().createPrivateCategoryStructures();
                }
            }
        }
    }

    /**
     * When a customer logs out we set the catalog key back to the default value
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected void resetCatalogKey() throws KKException
    {
        if (customerTagsAvailable)
        {
            FetchProductOptionsIf currentOptions = this.kkAppEng.getFetchProdOptions();
            FetchProductOptionsIf defaultOptions = this.kkAppEng.getFetchProdOptionsDefault();
            if (currentOptions != null && defaultOptions != null && !Objects
                    .equals(currentOptions.getCatalogId(), defaultOptions.getCatalogId()))
            {
                currentOptions.setCatalogId(defaultOptions.getCatalogId());
                currentOptions.setUseExternalPrice(defaultOptions.isUseExternalPrice());
                currentOptions.setUseExternalQuantity(defaultOptions.isUseExternalQuantity());
            }
            // Ensure that everything is refreshed
            this.kkAppEng.setFetchProdOptions(currentOptions);

            // Reset the private category structures
            this.kkAppEng.getCategoryMgr().resetPrivateCategoryStructures();
        }
    }

    /**
     * Returns the customer tag for the key. Null is returned if no tag exists.
     * 
     * @param key
     *            the key
     * @param fromDB
     *            Bypass the cache and get the tag value from the database
     * @return Returns the customer tag for the key. Null is returned if no tag exists.
     * @throws KKException
     *            an unexpected KKException exception
     */
    public CustomerTagIf getTag(String key, boolean fromDB) throws KKException
    {
        if (key == null)
        {
            return null;
        }
        if (fromDB)
        {
            CustomerTagIf tag = eng.getCustomerTag(kkAppEng.getSessionId(), key);
            if (tag != null)
            {
                tagMap.put(tag.getName(), tag);
            }
            return tag;
        }
        return tagMap.get(key);
    }

    /**
     * Returns the tag value as a Boolean. The default value is returned if the tag doesn't exist or
     * if the tag value cannot be converted to a boolean.
     * 
     * @param key
     *            the key
     * @param def
     *            Default value
     * @return Returns the tag value as a Boolean
     * @throws KKException
     *            an unexpected KKException exception
     */
    public Boolean getTagValueAsBool(String key, Boolean def) throws KKException
    {
        return getTagValueAsBool(key, def, /* fromDB */false);
    }

    /**
     * Returns the tag value as a Boolean. The default value is returned if the tag doesn't exist or
     * if the tag value cannot be converted to a boolean.
     * 
     * @param key
     *            the key
     * @param def
     *            Default value
     * @param fromDB
     *            Bypass the cache and get the tag value from the database
     * @return Returns the tag value as a Boolean
     * @throws KKException
     *            an unexpected KKException exception
     */
    public Boolean getTagValueAsBool(String key, Boolean def, boolean fromDB) throws KKException
    {
        CustomerTagIf tag = getTag(key, fromDB);
        if (tag == null)
        {
            return def;
        }
        try
        {
            Boolean val = Boolean.valueOf(tag.getValue());
            return val;
        } catch (Exception e)
        {
            log.warn("Customer tag - " + key + " is not a boolean. Value is - " + tag.getValue());
            return def;
        }
    }

    /**
     * Returns the tag value as a BigDecimal. Null is returned if the tag doesn't exist or if the
     * value cannot be converted to a BigDecimal.
     * 
     * @param key
     *            the key
     * @return Returns the tag value as a BigDecimal
     * @throws KKException
     *            an unexpected KKException exception
     */
    public BigDecimal getTagValueAsBigDecimal(String key) throws KKException
    {
        return getTagValueAsBigDecimal(key, /* fromDB */false);
    }

    /**
     * Returns the tag value as a BigDecimal. Null is returned if the tag doesn't exist or if the
     * value cannot be converted to a BigDecimal.
     * 
     * @param key
     *            the key
     * @param fromDB
     *            Bypass the cache and get the tag value from the database
     * @return Returns the tag value as a BigDecimal
     * @throws KKException
     *            an unexpected KKException exception
     */
    public BigDecimal getTagValueAsBigDecimal(String key, boolean fromDB) throws KKException
    {
        CustomerTagIf tag = getTag(key, fromDB);
        if (tag == null)
        {
            return null;
        }
        try
        {
            BigDecimal val = new BigDecimal(tag.getValue());
            return val;
        } catch (Exception e)
        {
            log.warn(
                    "Customer tag - " + key + " is not a BigDecimal. Value is - " + tag.getValue());
            return null;
        }
    }

    /**
     * Returns the tag value as a String. Null is returned if the tag doesn't exist.
     * 
     * @param key
     *            the key
     * @return Returns the tag value as a String
     * @throws KKException
     *            an unexpected KKException exception
     */
    public String getTagValueAsString(String key) throws KKException
    {
        return getTagValueAsString(key, /* fromDB */false);
    }

    /**
     * Returns the tag value as a String. Null is returned if the tag doesn't exist.
     * 
     * @param key
     *            the key
     * @param fromDB
     *            Bypass the cache and get the tag value from the database
     * @return Returns the tag value as a String
     * @throws KKException
     *            an unexpected KKException exception
     */
    public String getTagValueAsString(String key, boolean fromDB) throws KKException
    {
        CustomerTagIf tag = getTag(key, fromDB);
        if (tag == null)
        {
            return null;
        }

        return tag.getValue();
    }

    /**
     * Log-off and reset some variables. A guest customer is created and becomes the current
     * customer.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void logout() throws KKException
    {
        this.kkAppEng.logout();

        // Remove various cached values
        this.kkAppEng.getOrderMgr().setCouponCode(null);
        this.kkAppEng.getOrderMgr().setGiftCertCode(null);
        this.kkAppEng.getOrderMgr().setRewardPoints(0);
        this.kkAppEng.getContentMgr().refreshCaches();

        createGuest();

        // Set Admin User to null
        kkAppEng.setAdminUser(null);

        // Set affiliate id to null
        kkAppEng.setAffiliateId(null);

        // Reset the catalog key in case it was changed when the user logged in
        resetCatalogKey();
    }

    /**
     * Calls the engine to change the current password with the new one.
     * 
     * @param currentPassword
     *            The current password
     * @param newPassword
     *            The new password
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void changePassword(String currentPassword, String newPassword) throws KKException
    {
        eng.changePassword(kkAppEng.getSessionId(), currentPassword, newPassword);
    }

    /**
     * Calls the engine to send a new password to the user.
     * 
     * @param emailAddr
     *            The email address where the new password will be sent
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void sendNewPassword(String emailAddr) throws KKException
    {
        EmailOptionsIf options = new EmailOptions();
        options.setCountryCode(kkAppEng.getLocale().substring(0, 2));
        options.setTemplateName(com.konakart.bl.EmailMgr.NEW_PASSWORD_TEMPLATE);
        eng.sendNewPassword1(emailAddr, options);
    }

    /**
     * @return the selectedCountry
     */
    public CountryIf getSelectedCountry()
    {
        return selectedCountry;
    }

    /**
     * This also sets the selected zones if the country has an array of zones.
     * 
     * @param selectedCountry
     *            the selectedCountry to set
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void setSelectedCountry(CountryIf selectedCountry) throws KKException
    {
        if (selectedCountry != null && (this.selectedCountry == null
                || this.selectedCountry.getId() != selectedCountry.getId()))
        {
            // Get a new list of selected zones
            this.selectedZones = eng.getZonesPerCountry(selectedCountry.getId());
        } else if (selectedCountry == null)
        {
            this.selectedZones = null;
        }
        this.selectedCountry = selectedCountry;
    }

    /**
     * Sets the selected country from its id. This also sets the selected zones if the country has
     * an array of zones.
     * 
     * @param countryId
     *            the countryId
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void setSelectedCountry(int countryId) throws KKException
    {
        CountryIf country = eng.getCountry(countryId);
        setSelectedCountry(country);
    }

    /**
     * @return the selectedZones
     */
    public ZoneIf[] getSelectedZones()
    {
        return selectedZones;
    }

    /**
     * @param selectedZones
     *            the selectedZones to set
     */
    public void setSelectedZones(ZoneIf[] selectedZones)
    {
        this.selectedZones = selectedZones;
    }

    /**
     * Used to store information on how login was performed
     * 
     * @return the loginType
     */
    public String getLoginType()
    {
        return loginType;
    }

    /**
     * Used to store information on how login was performed
     * 
     * @param loginType
     *            the loginType to set
     */
    public void setLoginType(String loginType)
    {
        this.loginType = loginType;
    }

    /**
     * True when a customer hasn't created an address yet
     * 
     * @return the noAddress
     */
    public boolean isNoAddress()
    {
        return noAddress;
    }

    /**
     * True when a customer hasn't created an address yet
     * 
     * @param noAddress
     *            the noAddress to set
     */
    public void setNoAddress(boolean noAddress)
    {
        this.noAddress = noAddress;
    }

    /**
     * True when a customer hasn't added a telephone number yet
     * 
     * @return the noTelephone
     */
    public boolean isNoTelephone()
    {
        return noTelephone;
    }

    /**
     * True when a customer hasn't added a telephone number yet
     * 
     * @param noTelephone
     *            the noTelephone to set
     */
    public void setNoTelephone(boolean noTelephone)
    {
        this.noTelephone = noTelephone;
    }

    /**
     * @param currentCustomer
     *            the currentCustomer to set
     */
    public void setCurrentCustomer(CustomerIf currentCustomer)
    {
        this.currentCustomer = currentCustomer;
        if (currentCustomer != null)
        {
            if (currentCustomer.getTelephoneNumber() != null
                    && currentCustomer.getTelephoneNumber().length() > 1)
            {
                noTelephone = false;
            } else
            {
                noTelephone = true;
            }
            if (currentCustomer.getFirstName() != null
                    && currentCustomer.getFirstName().length() > 1
                    && currentCustomer.getLastName() != null
                    && currentCustomer.getLastName().length() > 1)
            {
                noName = false;
            } else
            {
                noName = true;
            }
            if (currentCustomer.getGender() != null && currentCustomer.getGender()
                    .equals(com.konakart.bl.CustomerMgr.DEFAULT_STRING))
            {
                noGender = true;
            } else
            {
                noGender = false;
            }

            if (currentCustomer.getBirthDate() != null && (currentCustomer.getBirthDate().get(
                    Calendar.YEAR) == com.konakart.bl.CustomerMgr.DEFAULT_DOB.get(Calendar.YEAR)))
            {
                noBirthDate = true;
            } else
            {
                noBirthDate = false;
            }
        }
    }

    /**
     * True when a customer hasn't added a name yet
     * 
     * @return the noName
     */
    public boolean isNoName()
    {
        return noName;
    }

    /**
     * True when a customer hasn't added a name yet
     * 
     * @param noName
     *            the noName to set
     */
    public void setNoName(boolean noName)
    {
        this.noName = noName;
    }

    /**
     * True when a customer hasn't added a gender yet
     * 
     * @return the noGender
     */
    public boolean isNoGender()
    {
        return noGender;
    }

    /**
     * True when a customer hasn't added a gender yet
     * 
     * @param noGender
     *            the noGender to set
     */
    public void setNoGender(boolean noGender)
    {
        this.noGender = noGender;
    }

    /**
     * True when a customer hasn't added a date of birth yet
     * 
     * @return the noBirthDate
     */
    public boolean isNoBirthDate()
    {
        return noBirthDate;
    }

    /**
     * True when a customer hasn't added a date of birth yet
     * 
     * @param noBirthDate
     *            the noBirthDate to set
     */
    public void setNoBirthDate(boolean noBirthDate)
    {
        this.noBirthDate = noBirthDate;
    }

    /**
     * Set to true if customer tag functionality is installed
     * 
     * @return the customerTagsAvailable
     */
    public boolean isCustomerTagsAvailable()
    {
        return customerTagsAvailable;
    }

    /**
     * @return the canChangeAddress
     */
    public boolean isCanChangeAddress()
    {
        return canChangeAddress;
    }

    /**
     * @param canChangeAddress
     *            the canChangeAddress to set
     */
    public void setCanChangeAddress(boolean canChangeAddress)
    {
        this.canChangeAddress = canChangeAddress;
    }

    /**
     * The catalog key used for this customer
     * 
     * @return the catalogKey
     */
    public String getCatalogKey()
    {
        return catalogKey;
    }

    /**
     * The catalog key used for this customer
     * 
     * @param catalogKey
     *            the catalogKey to set
     */
    public void setCatalogKey(String catalogKey)
    {
        this.catalogKey = catalogKey;
    }

    /**
     * @return the currentVendor
     */
    public VendorIf getCurrentVendor()
    {
        return currentVendor;
    }

    /**
     * @param currentVendor
     *            the currentVendor to set
     */
    public void setCurrentVendor(VendorIf currentVendor)
    {
        this.currentVendor = currentVendor;
    }

    /**
     * @return the currentStore
     */
    public StoreIf getCurrentStore()
    {
        return currentStore;
    }

    /**
     * @param currentStore
     *            the currentStore to set
     */
    public void setCurrentStore(StoreIf currentStore)
    {
        this.currentStore = currentStore;
    }

}
