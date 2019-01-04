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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.konakart.actions.BaseAction;
import com.konakart.app.AddToWishListOptions;
import com.konakart.app.CustomerSearch;
import com.konakart.app.CustomerTag;
import com.konakart.app.DataDescConstants;
import com.konakart.app.DataDescriptor;
import com.konakart.app.KKException;
import com.konakart.app.WishList;
import com.konakart.appif.AddToWishListOptionsIf;
import com.konakart.appif.CustomerSearchIf;
import com.konakart.appif.CustomerTagIf;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.WishListIf;
import com.konakart.appif.WishListItemIf;
import com.konakart.appif.WishListItemsIf;
import com.konakart.appif.WishListsIf;
import com.konakart.bl.ConfigConstants;

/**
 * Contains methods to manage Wish Lists
 */
public class WishListMgr extends BaseMgr
{
    /** Defines the type of wish list */
    public static final int WISH_LIST_TYPE = 0;

    /** Defines the type of wish list */
    public static final int WEDDING_LIST_TYPE = 1;

    /** Defines the type of wish list */
    public static final int BIRTHDAY_LIST_TYPE = 2;

    // Result set navigation constants
    private static final String navNext = "next";

    private static final String navBack = "back";

    private static final String navStart = "start";

    // Order By Constants
    private static final String obPriority = "obPriority";

    /** The current wish list */
    private WishListIf currentWishList;

    /** The total number of wish lists */
    private int totalNumberOfWishLists;

    /** The total number of wish list items */
    private int totalNumberOfWishListItems;

    /** Empty wish list array */
    private final WishListIf[] emptyWishListArray = new WishListIf[0];

    /** Empty wish list item array */
    private final WishListItemIf[] emptyWishListItemArray = new WishListItemIf[0];

    /** The current wish list array */
    private WishListIf[] currentWishLists;

    /** The current wish list item array */
    private WishListItemIf[] currentWishListItems;

    // Data descriptor info
    private DataDescriptorIf dataDesc = new DataDescriptor();

    private DataDescriptorIf dataDescItems = new DataDescriptor();

    // Customer Search object
    private CustomerSearchIf customerSearch = new CustomerSearch();

    private int currentWishListOffset;

    private int currentItemOffset;

    private int currentWishListPage;

    private int currentItemPage;

    private int wishListShowNext;

    private int wishListShowBack;

    private int itemShowNext;

    private int itemShowBack;

    // Total number of pages in a result set
    private int numWishListPages = 0;

    private int numItemPages = 0;

    private ArrayList<Integer> wishListPageList = new ArrayList<Integer>();

    private ArrayList<Integer> itemPageList = new ArrayList<Integer>();

    // Hash Map to contain tags
    private HashMap<String, Boolean> tagMap = new HashMap<String, Boolean>();

    /** The object containing all of the static data for this instance of the WishList Manager */
    private StaticData sd = null;

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected WishListMgr(KKEngIf eng, KKAppEng kkAppEng) throws KKException
    {
        this.eng = eng;
        this.kkAppEng = kkAppEng;
        sd = staticDataHM.get(kkAppEng.getStoreId());
        if (sd == null)
        {
            sd = new StaticData();
            staticDataHM.put(kkAppEng.getStoreId(), sd);
        }
        this.reset();
    }

    /**
     * Refresh the configuration variables. This is called automatically at a regular interval.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * 
     */
    public void refreshConfigs() throws KKException
    {
        sd.setMaxRowsPrevious(sd.getMaxRows());
        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_GIFT_REGISTRIES) != null)
        {
            sd.setMaxRows(new Integer(kkAppEng
                    .getConfig(ConfigConstants.MAX_DISPLAY_GIFT_REGISTRIES)).intValue());
        } else
        {
            sd.setMaxRows(6);
        }
        sd.setMaxItemRowsPrevious(sd.getMaxItemRows());
        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_GIFT_REGISTRY_ITEMS) != null)
        {
            sd.setMaxItemRows(new Integer(kkAppEng
                    .getConfig(ConfigConstants.MAX_DISPLAY_GIFT_REGISTRY_ITEMS)).intValue());
        } else
        {
            sd.setMaxItemRows(20);
        }
    }

    /**
     * Puts the WishListMgr object back into it's original state with no wish lists selected.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * 
     */
    public void reset() throws KKException
    {
        currentWishLists = emptyWishListArray;
        currentWishListItems = emptyWishListItemArray;
        currentWishList = null;
        currentWishListOffset = 0;
        currentItemOffset = 0;
        wishListShowNext = 0;
        wishListShowBack = 0;
        itemShowNext = 0;
        itemShowBack = 0;
        this.initDataDesc();
        this.initDataDescItems();
    }

    /**
     * Initialize the data descriptor object with a zero offset and max rows set.
     * 
     */
    private void initDataDesc()
    {
        // We always get an extra row in order to determine whether to show the
        // next link
        dataDesc.setLimit(sd.getMaxRows() + 1);
        dataDesc.setOffset(0);
    }

    /**
     * Initialize the data descriptor object with a zero offset and max rows set.
     * 
     */
    private void initDataDescItems()
    {
        // We always get an extra row in order to determine whether to show the
        // next link
        dataDescItems.setLimit(sd.getMaxItemRows() + 1);
        dataDescItems.setOffset(0);
        dataDescItems.setOrderBy(DataDescConstants.ORDER_BY_PRIORITY_DESCENDING);
    }

    /**
     * Gets a fully populated wish list from the engine and populates the
     * <code>currentWishList</code> attribute with it.
     * 
     * @param wishListId
     *            the wishListId
     * @return Returns the WishList object
     * @throws KKException
     *            an unexpected KKException exception
     */
    public WishListIf fetchWishList(int wishListId) throws KKException
    {
        WishListIf wl = eng.getWishListWithItemsWithOptions(kkAppEng.getSessionId(), wishListId,
                kkAppEng.getLangId(), getAddToWishListOptions());
        currentWishList = wl;
        return wl;
    }

    /**
     * Gets an empty wish list from the engine and populates the <code>currentWishList</code>
     * attribute with it.
     * 
     * @param wishListId
     *            the wishListId
     * @return Returns the WishList object
     * @throws KKException
     *            an unexpected KKException exception
     */
    public WishListIf fetchWishListWithoutItems(int wishListId) throws KKException
    {
        WishListIf wl = eng.getWishListWithOptions(kkAppEng.getSessionId(), wishListId,
                getAddToWishListOptions());
        currentWishList = wl;
        return wl;
    }

    /**
     * Get the wish list for a customer and language and set them on the customer object of the
     * customerMgr. Each wish list may have an array of wish list items, each of which has a fully
     * populated product object.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void fetchCustomersWishLists() throws KKException, KKAppException
    {
        checkEnabled();
        if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null)
        {
            CustomerSearchIf custSearch = null;
            if (kkAppEng.getCustomerMgr().getCurrentCustomer().getId() < 0
                    && allowWishListWhenNotLoggedIn())
            {
                custSearch = new CustomerSearch();
                custSearch.setTmpId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
            }

            WishListsIf ret = eng.searchForWishLists(kkAppEng.getSessionId(), null, custSearch);
            if (ret != null && ret.getWishListArray() != null)
            {
                WishListIf[] retArray = new WishList[ret.getWishListArray().length];
                for (int i = 0; i < ret.getWishListArray().length; i++)
                {
                    // Populate each wish list with its items
                    WishListIf wl = ret.getWishListArray()[i];
                    wl = eng.getWishListWithItemsWithOptions(kkAppEng.getSessionId(), wl.getId(),
                            kkAppEng.getLangId(), getAddToWishListOptions());
                    retArray[i] = wl;
                }
                kkAppEng.getCustomerMgr().getCurrentCustomer().setWishLists(retArray);
                setCustomerTags(retArray);
            }
        }
    }

    /**
     * The Wish List item is removed from the database and so no longer appears in the customer's
     * wish list when <code>fetchCustomersWishLists()</code> is called.
     * 
     * @param item
     *            The wish list item to be removed
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void removeFromWishList(WishListItemIf item) throws KKException, KKAppException
    {
        checkEnabled();
        if (item != null && kkAppEng.getCustomerMgr().getCurrentCustomer() != null)
        {
            eng.removeFromWishListWithOptions(kkAppEng.getSessionId(), item.getId(),
                    getAddToWishListOptions());
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_PAGE_LINKS) != null)
        {
            sd.setMaxPageLinks(new Integer(kkAppEng
                    .getConfig(ConfigConstants.MAX_DISPLAY_PAGE_LINKS)).intValue());
        } else
        {
            sd.setMaxPageLinks(5);
        }
    }

    /**
     * Creates a new wish list which will appear when <code>fetchCustomersWishLists()</code> is
     * called.
     * 
     * @param wishList
     *            the wishList
     * @return Returns the id of the new Wish List
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int createWishList(WishListIf wishList) throws KKException, KKAppException
    {
        checkEnabled();
        int id = 0;
        if (wishList != null && kkAppEng.getCustomerMgr().getCurrentCustomer() != null)
        {
            id = eng.createWishListWithOptions(kkAppEng.getSessionId(), wishList,
                    getAddToWishListOptions());
        }
        return id;
    }

    /**
     * The WishListItem is added to one of the customer's wish lists. The chosen wish list is
     * determined by the wishListId attribute of the WishListItem object.<br>
     * If the wishListId attribute of the wishListItem is set to a negative number, then it will
     * automatically be added to the wish list of the current customer. If the current customer has
     * more than one wish list, then an exception will be thrown. In this case the wish list id must
     * be set correctly. If the current customer has no wish lists, then a wish list will
     * automatically be created.<br>
     * The new wish list item will appear on the UI only after
     * <code>fetchCustomersWishLists()</code> is called.
     * 
     * @param item
     *            the item
     * @return Returns the id of the new wish list item
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int addToWishList(WishListItemIf item) throws KKException, KKAppException
    {
        checkEnabled();
        int wliId = 0;

        if (item != null && kkAppEng.getCustomerMgr().getCurrentCustomer() != null)
        {
            if (item.getWishListId() < 0)
            {
                int wlId = -1;
                WishListIf[] wishLists = kkAppEng.getCustomerMgr().getCurrentCustomer()
                        .getWishLists();
                if (wishLists == null || wishLists.length == 0)
                {
                    // Create a wish list
                    WishList wl = new WishList();
                    wl.setPublicWishList(false);
                    wl.setListType(WISH_LIST_TYPE);
                    wlId = eng.createWishListWithOptions(kkAppEng.getSessionId(), wl,
                            getAddToWishListOptions());
                } else
                {
                    for (int i = 0; i < wishLists.length; i++)
                    {
                        // Try to find a wish list
                        WishListIf wl = wishLists[i];
                        if (wl.getListType() == WISH_LIST_TYPE)
                        {
                            wlId = wl.getId();
                            break;
                        }
                    }
                    // If can't find one then create one
                    if (wlId == -1)
                    {
                        // Create a wish list
                        WishListIf wl = new WishList();
                        wl.setPublicWishList(false);
                        wl.setListType(WISH_LIST_TYPE);
                        wlId = eng.createWishListWithOptions(kkAppEng.getSessionId(), wl,
                                getAddToWishListOptions());
                    }
                }

                // Set the wish list id of the wish list item
                item.setWishListId(wlId);
            }

            wliId = eng.addToWishListWithOptions(kkAppEng.getSessionId(), item,
                    getAddToWishListOptions());
        }
        return wliId;
    }

    /**
     * The wish list referenced by the wishListId parameter, and all of its items will be deleted.
     * If wishListId is set to a negative number all of the current customer's wish lists will be
     * deleted.<br>
     * The wish lists will disappear on the UI only after <code>fetchCustomersWishLists()</code> is
     * called.
     * 
     * @param wishListId
     *            the wishListId
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void deleteWishList(int wishListId) throws KKException, KKAppException
    {
        checkEnabled();
        if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null)
        {
            if (wishListId < 0)
            {
                WishListIf[] wishLists = kkAppEng.getCustomerMgr().getCurrentCustomer()
                        .getWishLists();
                if (wishLists != null && wishLists.length > 0)
                {
                    for (int i = 0; i < wishLists.length; i++)
                    {
                        WishListIf wl = wishLists[i];
                        eng.deleteWishListWithOptions(kkAppEng.getSessionId(), wl.getId(),
                                getAddToWishListOptions());
                    }
                }
            } else
            {
                eng.deleteWishListWithOptions(kkAppEng.getSessionId(), wishListId,
                        getAddToWishListOptions());
            }
        }
    }

    /**
     * The wish list passed in as a parameter will be edited.<br>
     * The edited wish list will only appear on the UI only after
     * <code>fetchCustomersWishLists()</code> is called.
     * 
     * @param wishList
     *            the wishList
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void editWishList(WishListIf wishList) throws KKException, KKAppException
    {
        checkEnabled();
        if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null && wishList != null)
        {
            eng.editWishListWithOptions(kkAppEng.getSessionId(), wishList,
                    getAddToWishListOptions());
        }
    }

    /**
     * Based on a configuration variable decides whether wish lists are enabled. Throws an exception
     * if not enabled.
     * 
     * @throws KKAppException
     * 
     */
    private void checkEnabled() throws KKAppException
    {
        if (isEnabled())
        {
            return;
        }
        throw new KKAppException(
                "Wish List functionality is not enabled. It can be enabled by setting the ENABLE_WISHLIST and/or ENABLE_GIFT_REGISTRY config variables to true.");
    }

    /**
     * Based on a configuration variable decides whether wish lists are enabled. It returns true if
     * they are enabled. Otherwise it returns false.
     * 
     * @return Returns true if wish lists are enabled
     * 
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * 
     */
    public boolean isEnabled() throws KKAppException
    {
        String enabled = kkAppEng.getConfig(ConfigConstants.ENABLE_WISHLIST);
        if (enabled != null && enabled.equalsIgnoreCase("true"))
        {
            return true;
        }
        enabled = kkAppEng.getConfig(ConfigConstants.ENABLE_GIFT_REGISTRY);
        if (enabled != null && enabled.equalsIgnoreCase("true"))
        {
            return true;
        }
        return false;
    }

    /**
     * Creates an AddToWishListOptionsIf based on the current FetchProductOptions stored in the
     * AppEng.
     * 
     * @return Returns an AddToWishListOptionsIf object
     */
    public AddToWishListOptionsIf getAddToWishListOptions()
    {
        AddToWishListOptionsIf atwlo = null;
        if (kkAppEng.getFetchProdOptions() != null)
        {
            atwlo = new AddToWishListOptions();
            atwlo.setCatalogId(kkAppEng.getFetchProdOptions().getCatalogId());
            atwlo.setPriceDate(kkAppEng.getFetchProdOptions().getPriceDate());
            atwlo.setUseExternalPrice(kkAppEng.getFetchProdOptions().isUseExternalPrice());
            atwlo.setGetImages(kkAppEng.getFetchProdOptions().isGetImages());
        }

        if (allowWishListWhenNotLoggedIn()
                && kkAppEng.getCustomerMgr().getCurrentCustomer() != null
                && kkAppEng.getCustomerMgr().getCurrentCustomer().getId() < 0)
        {
            if (atwlo == null)
            {
                atwlo = new AddToWishListOptions();
            }
            atwlo.setCustomerId(kkAppEng.getCustomerMgr().getCurrentCustomer().getId());
        }
        return atwlo;
    }

    /**
     * This method is called to navigate through a list of wish lists when the list is longer than
     * maxRows.<br>
     * <code>navDir</code> can take the following values which are retrieved using getter methods on
     * the WishListMgr instance:
     * <ul>
     * <li>getNavNext()</li>
     * <li>getNavBack()</li>
     * <li>getNavStart()</li>
     * </ul>
     * 
     * @param navDir
     *            The navigation direction
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void navigateCurrentWishLists(String navDir) throws KKException, KKAppException
    {
        setDataDescOffset(navDir);

        getWishLists();
    }

    /**
     * This method is called to navigate through a list of wish list items when the list is longer
     * than maxItemRows.<br>
     * <code>navDir</code> can take the following values which are retrieved using getter methods on
     * the WishListMgr instance:
     * <ul>
     * <li>getNavNext()</li>
     * <li>getNavBack()</li>
     * <li>getNavStart()</li>
     * </ul>
     * 
     * @param navDir
     *            The navigation direction
     * @throws Exception an unexpected exception
     */
    public void navigateCurrentWishListItems(String navDir) throws Exception
    {
        setDataDescItemsOffset(navDir);

        getWishListItems();
    }

    /**
     * The wish lists are fetched from the engine and put in the currentWishList array.
     * 
     * @param _customerSearch
     *            the _customerSearch
     * @return The number of wish lists retrieved
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int searchForWishLists(CustomerSearchIf _customerSearch) throws KKException,
            KKAppException
    {
        setDataDescOffset(navStart);
        this.customerSearch = _customerSearch;

        this.getWishLists();

        return currentWishLists.length;
    }

    /**
     * It gets an array of wish lists
     * 
     * @throws KKException
     * @throws KKAppException
     */
    private void getWishLists() throws KKException, KKAppException
    {
        WishListsIf wishLists = eng.searchForWishLists(kkAppEng.getSessionId(), dataDesc,
                customerSearch);
        if (wishLists != null)
        {
            currentWishLists = wishLists.getWishListArray();
            totalNumberOfWishLists = wishLists.getTotalNumWishLists();
        }

        wishListPageList = getWishListPages( /* currentPage */
        currentWishListPage);

        if (currentWishLists != null)
        {
            // We always attempt to fetch back maxRows + 1
            if (currentWishLists.length > sd.getMaxRows())
            {
                this.wishListShowNext = 1;
            } else
            {
                this.wishListShowNext = 0;
            }
        }

        if (currentWishListOffset > 0)
        {
            this.wishListShowBack = 1;
        } else
        {
            this.wishListShowBack = 0;
        }
    }

    /**
     * It gets an array of wish list items
     * 
     * @param wishListId
     * 
     * @throws Exception an unexpected exception
     */
    private void getWishListItems() throws Exception
    {

        if (currentWishList == null)
        {
            return;
        }

        WishListItemsIf wishListItems = eng.getWishListItemsWithOptions(kkAppEng.getSessionId(),
                dataDescItems, currentWishList.getId(), kkAppEng.getLangId(),
                getAddToWishListOptions());

        if (wishListItems != null)
        {
            currentWishListItems = wishListItems.getItemArray();
            totalNumberOfWishListItems = wishListItems.getTotalNumItems();
        }

        itemPageList = getWishListItemPages( /* currentPage */
        currentItemPage);

        if (currentWishListItems != null)
        {
            // We always attempt to fetch back maxRows + 1
            if (currentWishListItems.length > sd.getMaxItemRows())
            {
                this.itemShowNext = 1;
            } else
            {
                this.itemShowNext = 0;
            }
        }

        if (currentItemOffset > 0)
        {
            this.itemShowBack = 1;
        } else
        {
            this.itemShowBack = 0;
        }
    }

    /**
     * This method is called to change the order of a list of wishlist items in the
     * currentWishListItems array. The orderBy parameter can take a single value that can be
     * retrieved from the WishListMgr using a getter methods:
     * <ul>
     * <li>getObPriority() - Order by gift priority</li>
     * </ul>
     * 
     * @param orderBy
     *            The order by parameter
     * @throws Exception an unexpected exception
     */
    public void orderCurrentWishListItems(String orderBy) throws Exception
    {
        setDataDescItemsOffset(navStart);

        if (orderBy.equals(obPriority))
        {
            if (dataDescItems.getOrderBy() == DataDescConstants.ORDER_BY_PRIORITY_ASCENDING)
            {
                dataDescItems.setOrderBy(DataDescConstants.ORDER_BY_PRIORITY_DESCENDING);
            } else
            {
                dataDescItems.setOrderBy(DataDescConstants.ORDER_BY_PRIORITY_ASCENDING);
            }
        } else
        {
            throw new KKAppException("The orderBy parameter " + orderBy + " is not valid");
        }
        this.getWishListItems();
    }

    /**
     * Based on the action we are being asked to perform and the current offset, we set the new
     * offset before going to the engine to ask for more wish lists.
     * 
     * @param action
     * @throws KKAppException
     */
    private void setDataDescOffset(String action) throws KKAppException
    {
        // We initialize the data desc if the number of rows we can view has changed
        if (sd.getMaxRows() != sd.getMaxRowsPrevious())
        {
            initDataDesc();
            currentWishListOffset = 0;
            currentWishListPage = 1;
            sd.setMaxRowsPrevious(sd.getMaxRows());
        }

        // Determine whether we've passed in a page number
        int requestedPage = -1;
        try
        {
            requestedPage = Integer.parseInt(action);
        } catch (NumberFormatException e)
        {
        }

        if (action.equals(navStart))
        {
            dataDesc.setOffset(0);
            currentWishListOffset = 0;
            currentWishListPage = 1;
        } else if (action.equals(navNext))
        {
            currentWishListOffset += sd.getMaxRows();
            dataDesc.setOffset(currentWishListOffset);
            currentWishListPage = (currentWishListOffset / sd.getMaxRows()) + 1;
        } else if (action.equals(navBack))
        {
            currentWishListOffset -= sd.getMaxRows();
            if (currentWishListOffset < 0)
            {
                currentWishListOffset = 0;
            }
            dataDesc.setOffset(currentWishListOffset);
            currentWishListPage = (currentWishListOffset / sd.getMaxRows()) + 1;
        } else if (requestedPage > 0)
        {
            currentWishListOffset = sd.getMaxRows() * (requestedPage - 1);
            dataDesc.setOffset(currentWishListOffset);
            currentWishListPage = requestedPage;
        } else if (requestedPage <= 0)
        {
            currentWishListOffset = 0;
            dataDesc.setOffset(currentWishListOffset);
            currentWishListPage = 1;
        } else
        {
            throw new KKAppException(
                    "The navigation direction parameter has an unrecognised value of " + action);

        }
    }

    /**
     * Based on the action we are being asked to perform and the current offset, we set the new
     * offset before going to the engine to ask for more wish list items.
     * 
     * @param action
     * @throws KKAppException
     */
    private void setDataDescItemsOffset(String action) throws KKAppException
    {
        // We initialize the data desc if the number of rows we can view has changed
        if (sd.getMaxItemRows() != sd.getMaxItemRowsPrevious())
        {
            initDataDescItems();
            currentItemOffset = 0;
            currentItemPage = 1;
            sd.setMaxItemRowsPrevious(sd.getMaxItemRows());
        }

        // Determine whether we've passed in a page number
        int requestedPage = -1;
        try
        {
            requestedPage = Integer.parseInt(action);
        } catch (NumberFormatException e)
        {
        }

        if (action.equals(navStart))
        {
            dataDescItems.setOffset(0);
            currentItemOffset = 0;
            currentItemPage = 1;
        } else if (action.equals(navNext))
        {
            currentItemOffset += sd.getMaxItemRows();
            dataDescItems.setOffset(currentItemOffset);
            currentItemPage = (currentItemOffset / sd.getMaxItemRows()) + 1;
        } else if (action.equals(navBack))
        {
            currentItemOffset -= sd.getMaxItemRows();
            if (currentItemOffset < 0)
            {
                currentItemOffset = 0;
            }
            dataDescItems.setOffset(currentItemOffset);
            currentItemPage = (currentItemOffset / sd.getMaxItemRows()) + 1;
        } else if (requestedPage > 0)
        {
            currentItemOffset = sd.getMaxItemRows() * (requestedPage - 1);
            dataDescItems.setOffset(currentItemOffset);
            currentItemPage = requestedPage;
        } else if (requestedPage <= 0)
        {
            currentItemOffset = 0;
            dataDescItems.setOffset(currentItemOffset);
            currentItemPage = 1;
        } else
        {
            throw new KKAppException(
                    "The navigation direction parameter has an unrecognised value of " + action);

        }
    }

    /**
     * @return the currentWishList
     */
    public WishListIf getCurrentWishList()
    {
        return currentWishList;
    }

    /**
     * @param currentWishList
     *            the currentWishList to set
     */
    public void setCurrentWishList(WishListIf currentWishList)
    {
        this.currentWishList = currentWishList;
    }

    /**
     * Static string - navigate backwards. It is the string required to pass to the
     * navigateCurrentWishLists() or navigateCurrentWishListItems() as the <code>navdir</code>
     * attribute when navigating backwards.
     * 
     * @return Returns the navBack.
     */
    public String getNavBack()
    {
        return navBack;
    }

    /**
     * Static string - navigate forwards. It is the string required to pass to the
     * navigateCurrentWishLists() or navigateCurrentWishListItems() as the <code>navdir</code>
     * attribute when navigating forwards.
     * 
     * @return Returns the navNext.
     */
    public String getNavNext()
    {
        return navNext;
    }

    /**
     * Static string - navigate to the start. It is the string required to pass to the
     * navigateCurrentWishLists() or navigateCurrentWishListItems() as the <code>navdir</code>
     * attribute when navigating to the start.
     * 
     * @return Returns the navStart.
     */
    public String getNavStart()
    {
        return navStart;
    }

    /**
     * Get an array list of wish list pages to show
     * 
     * @param _currentPage
     * @return Returns an array List of pages to show
     */
    private ArrayList<Integer> getWishListPages(int _currentPage)
    {
        numWishListPages = totalNumberOfWishLists / getMaxRows();
        if (totalNumberOfWishLists % getMaxRows() != 0)
        {
            numWishListPages++;
        }

        wishListPageList.clear();

        return getPages(_currentPage, numWishListPages, sd.getMaxPageLinks(), wishListPageList);
    }

    /**
     * Get an array list of wish list item pages to show
     * 
     * @param _currentPage
     * @return Returns an array List of pages to show
     */
    private ArrayList<Integer> getWishListItemPages(int _currentPage)
    {
        numItemPages = totalNumberOfWishListItems / getMaxItemRows();
        if (totalNumberOfWishListItems % getMaxItemRows() != 0)
        {
            numItemPages++;
        }

        itemPageList.clear();

        return getPages(_currentPage, numItemPages, sd.getMaxPageLinks(), itemPageList);
    }

    /**
     * Returns the number of wish lists currently retrieved.
     * 
     * @return Returns the number of wish lists currently retrieved.
     */
    public int getNumberOfWishLists()
    {
        // We attempt to fetch 1 more record than the number in maxRows so that
        // we can determine whether to show the next button. However, the JSP
        // should only show the number of records displayed which is limited to
        // maxRows within the JSP itself
        if (currentWishLists.length == sd.getMaxRows() + 1)
        {
            return sd.getMaxRows();
        }
        return currentWishLists.length;
    }

    /**
     * Returns the number of wish list items currently retrieved.
     * 
     * @return Returns the number of wish list items currently retrieved.
     */
    public int getNumberOfWishListItems()
    {
        // We attempt to fetch 1 more record than the number in maxRows so that
        // we can determine whether to show the next button. However, the JSP
        // should only show the number of records displayed which is limited to
        // maxRows within the JSP itself
        if (currentWishListItems.length == sd.getMaxItemRows() + 1)
        {
            return sd.getMaxItemRows();
        }
        return currentWishListItems.length;
    }

    /**
     * If Customer wish list tags are enabled we set tags for the wish list items and the value of
     * the wish list total. We have to ensure that the customer tags exist in the database. We do
     * this once and store the result in a hash map.
     * 
     * @param wlArray
     * @throws KKException
     * @throws KKAppException
     */
    private void setCustomerTags(WishListIf[] wlArray) throws KKException, KKAppException
    {
        String enabled = kkAppEng.getConfig(ConfigConstants.ENABLE_CUSTOMER_WISHLIST_TAGS);
        if (enabled != null && enabled.equalsIgnoreCase("true"))
        {
            Boolean setProdsTimeInWishList = tagMap.get(BaseAction.TAG_PRODUCTS_TIME_IN_WISHLIST);
            Boolean setProdsInWishList = tagMap.get(BaseAction.TAG_PRODUCTS_IN_WISHLIST);
            Boolean setWishListTotal = tagMap.get(BaseAction.TAG_WISHLIST_TOTAL);
            if (setProdsInWishList == null)
            {
                CustomerTagIf ct = eng.getCustomerTag(null, BaseAction.TAG_PRODUCTS_IN_WISHLIST);
                if (ct == null)
                {
                    tagMap.put(BaseAction.TAG_PRODUCTS_IN_WISHLIST, new Boolean(false));
                    setProdsInWishList = new Boolean(false);
                } else
                {
                    tagMap.put(BaseAction.TAG_PRODUCTS_IN_WISHLIST, new Boolean(true));
                    setProdsInWishList = new Boolean(true);
                }
            }
            if (setProdsTimeInWishList == null)
            {
                CustomerTagIf ct = eng.getCustomerTag(null, BaseAction.TAG_PRODUCTS_TIME_IN_WISHLIST);
                if (ct == null)
                {
                    tagMap.put(BaseAction.TAG_PRODUCTS_TIME_IN_WISHLIST, new Boolean(false));
                    setProdsTimeInWishList = new Boolean(false);
                } else
                {
                    tagMap.put(BaseAction.TAG_PRODUCTS_TIME_IN_WISHLIST, new Boolean(true));
                    setProdsTimeInWishList = new Boolean(true);
                }
            }
            if (setWishListTotal == null)
            {
                CustomerTagIf ct = eng.getCustomerTag(null, BaseAction.TAG_WISHLIST_TOTAL);
                if (ct == null)
                {
                    tagMap.put(BaseAction.TAG_WISHLIST_TOTAL, new Boolean(false));
                    setWishListTotal = new Boolean(false);
                } else
                {
                    tagMap.put(BaseAction.TAG_WISHLIST_TOTAL, new Boolean(true));
                    setWishListTotal = new Boolean(true);
                }
            }

            WishListIf selectedWishList = null;
            if (setProdsInWishList)
            {
                if (wlArray != null && wlArray.length > 0)
                {
                    StringBuffer prodsSB = new StringBuffer();
                    for (int i = 0; i < wlArray.length; i++)
                    {
                        WishListIf wl = wlArray[i];
                        if (wl.getListType() == WISH_LIST_TYPE)
                        {
                            selectedWishList = wl;
                            for (int j = 0; j < wl.getWishListItems().length; j++)
                            {
                                WishListItemIf wlItem = wl.getWishListItems()[j];
                                if (j == 0)
                                {
                                    prodsSB.append(CustomerTag.DELIM);
                                }
                                prodsSB.append(wlItem.getProductId());
                                prodsSB.append(CustomerTag.DELIM);
                            }
                            break;
                        }
                    }
                    kkAppEng.getCustomerTagMgr().insertCustomerTag(
                            BaseAction.TAG_PRODUCTS_IN_WISHLIST, prodsSB.toString());
                }
            }
            if (setProdsTimeInWishList)
            {
                if (wlArray != null && wlArray.length > 0)
                {
                    StringBuffer prodsSB = new StringBuffer();
                    for (int i = 0; i < wlArray.length; i++)
                    {
                        WishListIf wl = wlArray[i];
                        if (wl.getListType() == WISH_LIST_TYPE)
                        {
                            selectedWishList = wl;
                            for (int j = 0; j < wl.getWishListItems().length; j++)
                            {
                                WishListItemIf wlItem = wl.getWishListItems()[j];
                                if (j == 0)
                                {
                                    prodsSB.append(CustomerTag.DELIM);
                                }
                                prodsSB.append(wlItem.getProductId());
                                prodsSB.append(" ");
                                prodsSB.append(System.currentTimeMillis());
                                prodsSB.append(CustomerTag.DELIM);
                            }
                            break;
                        }
                    }
                    kkAppEng.getCustomerTagMgr().insertCustomerTag(
                            BaseAction.TAG_PRODUCTS_TIME_IN_WISHLIST, prodsSB.toString());
                }
            }
            if (setWishListTotal && selectedWishList != null)
            {
                BigDecimal total = null;
                if (kkAppEng.displayPriceWithTax())
                {
                    total = selectedWishList.getFinalPriceIncTax();
                } else
                {
                    total = selectedWishList.getFinalPriceExTax();
                }
                if (total != null)
                {
                    CustomerTag ct = new CustomerTag();
                    ct.setValueAsBigDecimal(total);
                    ct.setName(BaseAction.TAG_WISHLIST_TOTAL);
                    kkAppEng.getCustomerTagMgr().insertCustomerTag(ct);
                }
            }
        }
    }

    /**
     * Returns true or false depending on whether wish lists are allowed for non logged in customers
     * 
     * @return Returns true or false depending on whether wish lists are allowed for non logged in
     *         customers
     */
    public boolean allowWishListWhenNotLoggedIn()
    {
        String allowWLStr = kkAppEng.getConfig(ConfigConstants.ALLOW_WISHLIST_WHEN_NOT_LOGGED_IN);
        boolean allowWLBool = false;
        if (allowWLStr != null && allowWLStr.equalsIgnoreCase("true"))
        {
            allowWLBool = true;
        }
        return allowWLBool;
    }

    /**
     * @return the totalNumberOfWishLists
     */
    public int getTotalNumberOfWishLists()
    {
        return totalNumberOfWishLists;
    }

    /**
     * @return the currentWishLists
     */
    public WishListIf[] getCurrentWishLists()
    {
        return currentWishLists;
    }

    /**
     * Maximum number of wish lists to show in a list.
     * 
     * @return Returns the maxRows.
     */
    public int getMaxRows()
    {
        return sd.getMaxRows();
    }

    /**
     * Maximum number of wish list items to show in a list.
     * 
     * @return Returns the maxRows.
     */
    public int getMaxItemRows()
    {
        return sd.getMaxItemRows();
    }

    /**
     * Used to store the static data of this manager
     */
    private class StaticData
    {
        // Static config variables
        int maxRows = 6;

        // Keeps previous value so that we can determine whether it has changed
        int maxRowsPrevious;

        // Max number of items to display
        int maxItemRows = 20;

        // Keeps previous value so that we can determine whether it has changed
        int maxItemRowsPrevious;

        int maxPageLinks;

        /**
         * @return Returns the maxRows.
         */
        public int getMaxRows()
        {
            return maxRows;
        }

        /**
         * @param maxRows
         *            The maxRows to set.
         */
        public void setMaxRows(int maxRows)
        {
            this.maxRows = maxRows;
        }

        /**
         * @return Returns the maxRowsPrevious.
         */
        public int getMaxRowsPrevious()
        {
            return maxRowsPrevious;
        }

        /**
         * @param maxRowsPrevious
         *            The maxRowsPrevious to set.
         */
        public void setMaxRowsPrevious(int maxRowsPrevious)
        {
            this.maxRowsPrevious = maxRowsPrevious;
        }

        /**
         * @return the maxItemRows
         */
        public int getMaxItemRows()
        {
            return maxItemRows;
        }

        /**
         * @param maxItemRows
         *            the maxItemRows to set
         */
        public void setMaxItemRows(int maxItemRows)
        {
            this.maxItemRows = maxItemRows;
        }

        /**
         * @return the maxItemRowsPrevious
         */
        public int getMaxItemRowsPrevious()
        {
            return maxItemRowsPrevious;
        }

        /**
         * @param maxItemRowsPrevious
         *            the maxItemRowsPrevious to set
         */
        public void setMaxItemRowsPrevious(int maxItemRowsPrevious)
        {
            this.maxItemRowsPrevious = maxItemRowsPrevious;
        }

        /**
         * @return the maxPageLinks
         */
        public int getMaxPageLinks()
        {
            return maxPageLinks;
        }

        /**
         * @param maxPageLinks
         *            the maxPageLinks to set
         */
        public void setMaxPageLinks(int maxPageLinks)
        {
            this.maxPageLinks = maxPageLinks;
        }

    }

    /**
     * @return the totalNumberOfWishListItems
     */
    public int getTotalNumberOfWishListItems()
    {
        return totalNumberOfWishListItems;
    }

    /**
     * @return the currentWishListItems
     */
    public WishListItemIf[] getCurrentWishListItems()
    {
        return currentWishListItems;
    }

    /**
     * @return the obPriority
     */
    public String getObPriority()
    {
        return obPriority;
    }

    /**
     * @return the numWishListPages
     */
    public int getNumWishListPages()
    {
        return numWishListPages;
    }

    /**
     * @return the numItemPages
     */
    public int getNumItemPages()
    {
        return numItemPages;
    }

    /**
     * @return the wishListPageList
     */
    public ArrayList<Integer> getWishListPageList()
    {
        return wishListPageList;
    }

    /**
     * @return the itemPageList
     */
    public ArrayList<Integer> getItemPageList()
    {
        return itemPageList;
    }

    /**
     * Show the next button if set to 1. Don't show the next button if set to 0.
     * 
     * @return the wishListShowNext
     */
    public int getWishListShowNext()
    {
        return wishListShowNext;
    }

    /**
     * Show the back button if set to 1. Don't show the back button if set to 0.
     * 
     * @return the wishListShowBack
     */
    public int getWishListShowBack()
    {
        return wishListShowBack;
    }

    /**
     * Show the next button if set to 1. Don't show the next button if set to 0.
     * 
     * @return the itemShowNext
     */
    public int getItemShowNext()
    {
        return itemShowNext;
    }

    /**
     * Show the back button if set to 1. Don't show the back button if set to 0.
     * 
     * @return the itemShowBack
     */
    public int getItemShowBack()
    {
        return itemShowBack;
    }

    /**
     * @return the currentWishListOffset
     */
    public int getCurrentWishListOffset()
    {
        return currentWishListOffset;
    }

    /**
     * @return the currentItemOffset
     */
    public int getCurrentItemOffset()
    {
        return currentItemOffset;
    }

    /**
     * @return the currentWishListPage
     */
    public int getCurrentWishListPage()
    {
        return currentWishListPage;
    }

    /**
     * @return the currentItemPage
     */
    public int getCurrentItemPage()
    {
        return currentItemPage;
    }

}
