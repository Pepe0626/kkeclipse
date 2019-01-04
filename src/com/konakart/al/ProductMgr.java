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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.Category;
import com.konakart.app.CustomerTag;
import com.konakart.app.DataDescConstants;
import com.konakart.app.DataDescriptor;
import com.konakart.app.FetchDigitalDownloadOptions;
import com.konakart.app.FetchTagGroupOptions;
import com.konakart.app.KKBeanCopier;
import com.konakart.app.KKException;
import com.konakart.app.Manufacturer;
import com.konakart.app.ManufacturerSearch;
import com.konakart.app.ProductSearch;
import com.konakart.app.Products;
import com.konakart.app.SuggestedSpellingOptions;
import com.konakart.app.Tag;
import com.konakart.app.TagGroup;
import com.konakart.appif.CategoryIf;
import com.konakart.appif.DataDescriptorIf;
import com.konakart.appif.DigitalDownloadIf;
import com.konakart.appif.FetchDigitalDownloadOptionsIf;
import com.konakart.appif.FetchTagGroupOptionsIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.KKFacetIf;
import com.konakart.appif.KKPriceFacetIf;
import com.konakart.appif.ManufacturerIf;
import com.konakart.appif.ManufacturerSearchIf;
import com.konakart.appif.NameNumberIf;
import com.konakart.appif.OptionIf;
import com.konakart.appif.ProductIf;
import com.konakart.appif.ProductSearchIf;
import com.konakart.appif.ProductsIf;
import com.konakart.appif.PromotionIf;
import com.konakart.appif.SuggestedSpellingItemIf;
import com.konakart.appif.SuggestedSpellingOptionsIf;
import com.konakart.appif.TagGroupIf;
import com.konakart.appif.TagIf;
import com.konakart.bl.ConfigConstants;

/**
 * Contains methods to fetch and manage lists of products and keeps track of the current selected
 * product.
 */
public class ProductMgr extends BaseMgr
{

    private static final ProductIf[] emptyProdArray = new ProductIf[0];

    private static final ManufacturerIf[] emptyManuArray = new ManufacturerIf[0];

    private static final CategoryIf[] emptyCatArray = new CategoryIf[0];

    /**
     * Used for navigating product sets. It is the string required to pass to the
     * navigateCurrentProducts() as the <code>navdir</code> attribute when navigating forwards.
     */
    public static final String navNext = "next";

    /**
     * Used for navigating product sets. It is the string required to pass to the
     * navigateCurrentProducts() as the <code>navdir</code> attribute when navigating backwards.
     */
    public static final String navBack = "back";

    /**
     * Used for navigating product sets. It is the string required to pass to the
     * navigateCurrentProducts() as the <code>navdir</code> attribute when navigating to the start.
     */
    public static final String navStart = "start";

    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(ProductMgr.class);

    private ProductIf[] currentProducts;

    private ProductIf[] alsoPurchased;

    private ProductIf[] newProducts;

    private ProductIf[] specials;

    private ProductIf[] customProducts1;

    private ProductIf[] customProducts2;

    private ProductIf[] customProducts3;

    private ProductIf[] allRelatedProducts;

    private ProductIf[] upSellProducts;

    private ProductIf[] crossSellProducts;

    private ProductIf[] accessories;

    private ProductIf[] dependentProducts;

    private ProductIf[] bundledProducts;

    private ProductIf[] viewedProducts;

    private DigitalDownloadIf[] digitalDownloads;

    private ManufacturerIf[] currentManufacturers;

    private CategoryIf[] currentCategories;

    private ManufacturerIf selectedManufacturer;

    private CategoryIf selectedCategory;

    private ProductIf selectedProduct;

    private CategoryIf selectedProductCategory;

    private List<ProdOptionContainer> selectedProductOptions;

    private TagGroupIf[] currentTagGroups;

    private int newProdsCatId;

    private BigDecimal maxPrice;

    private BigDecimal minPrice;

    private BigDecimal taxMultiplier;

    private KKPriceFacetIf[] priceFacets;

    private SuggestedSpellingItemIf[] suggestedSpellings;

    private String suggestedSpellingSearch;

    private HashMap<Integer, TagIf> tagMap = new HashMap<Integer, TagIf>();

    /*
     * This value is set to true when a customer attempts to navigate an old page of products which
     * he got to using the browser back button.
     */
    private boolean expiredResultSet = false;

    // Data descriptor saved on the session for filtering and paging result sets
    private DataDescriptorIf dataDesc = new DataDescriptor();

    // Product Search saved on the session for filtering and paging result sets
    private ProductSearchIf prodSearch = new ProductSearch();

    // Keeps track of whether the result set contains specials
    private boolean showingSpecials = false;

    // Number of filters selected
    private int numSelectedFilters = 0;

    // Price filter active
    private boolean priceFilter = false;

    private int totalNumberOfProducts;

    private int currentOffset;

    private int currentPage;

    private int showNext;

    private int showBack;

    private ArrayList<Integer> pageList = new ArrayList<Integer>();

    private String defaultOrderBy = DataDescConstants.ORDER_BY_TIMES_VIEWED;

    private long prodTimestamp = 0;

    // Max rows defined by the user
    private int maxProdRowsUser = 0;

    // Total number of pages in a result set
    private int numPages = 0;

    /** The object containing all of the static data for this instance of the Category Manager */
    private StaticData sd = null;

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    // mutex
    private static String mutex = "prodContainerMutex";

    // Used to store search data for previous searches
    private static int SEARCH_MAP_SIZE = 10;

    // Used to store search data
    private LinkedHashMap<Long, SearchData> searchDataMap = new LinkedHashMap<Long, SearchData>(
            SEARCH_MAP_SIZE)
    {
        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(Map.Entry<Long, SearchData> eldest)
        {
            return size() > SEARCH_MAP_SIZE - 1;
        }
    };

    /**
     * Constructor. Should only be instantiated by Konakart.
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *             an unexpected KKException exception
     */
    protected ProductMgr(KKEngIf eng, KKAppEng kkAppEng) throws KKException
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
     * Puts the Product Manager back into it's original state with no products selected
     * 
     * @throws KKException
     *             an unexpected KKException exception
     * 
     */
    public void reset() throws KKException
    {
        currentProducts = emptyProdArray;
        currentManufacturers = emptyManuArray;
        currentCategories = emptyCatArray;
        currentTagGroups = null;
        selectedManufacturer = new Manufacturer();
        selectedCategory = new Category();
        selectedProductCategory = new Category();
        selectedProduct = null;
        selectedProductOptions = new ArrayList<ProdOptionContainer>();
        currentOffset = 0;
        showNext = 0;
        showBack = 0;
        numSelectedFilters = 0;
        priceFilter = false;
        suggestedSpellings = null;
        suggestedSpellingSearch = null;
    }

    /**
     * Refreshes the following;
     * <ul>
     * <li>All manufacturer list</li>
     * <li>Active promotion list</li>
     * </ul>
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void refreshCaches() throws KKException
    {
        try
        {
            synchronized (mutex)
            {
                this.initAllManufacturers();
                this.initActivePromotions();
            }

            // Now the manager is ready for action
            sd.setMgrReady(true);
        } catch (KKException kke)
        {
            log.warn("Exception refreshing caches on store " + this.kkAppEng.getStoreId());
            kke.printStackTrace();
        }
    }

    /**
     * Is the Manager Ready?
     * 
     * @return true if this manager is ready for work, otherwise returns false.
     */
    public boolean isMgrReady()
    {
        return sd.isMgrReady();
    }

    /**
     * Initialise the array of drop list elements containing all of the manufacturers
     * 
     * @throws KKException
     * 
     */
    private void initAllManufacturers() throws KKException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Creating All Manufacturers List");
        }

        // Instantiate list of drop list elements
        ManufacturerIf[] manuArray = eng.getAllManufacturers();

        if (manuArray == null)
        {
            sd.setAllManufacturers(new DropListElement[0]);
            return;
        }

        // Set the global array
        sd.setAllManuArray(manuArray);

        DropListElement[] dleArray = new DropListElement[manuArray.length];

        for (int i = 0; i < manuArray.length; i++)
        {
            DropListElement dle = new DropListElement(manuArray[i].getId(), manuArray[i].getName());
            dleArray[i] = dle;
        }

        sd.setAllManufacturers(dleArray);
    }

    /**
     * Creates and returns a new data descriptor object with a zero offset and max rows set. It is
     * set with the default Order By.
     * 
     */
    private DataDescriptorIf createDataDesc()
    {
        // We always get an extra row in order to determine whether to show the
        // next link
        DataDescriptorIf dd = new DataDescriptor();
        dd.setLimit(getMaxDisplaySearchResults() + 1);
        dd.setOffset(0);
        dd.setOrderBy(defaultOrderBy);
        return dd;
    }

    /**
     * Initialize a list of active promotions
     * 
     * @throws KKException
     * 
     */
    private void initActivePromotions() throws KKException
    {
        PromotionIf[] activePromotions = eng.getAllPromotions();
        sd.setActivePromotions(activePromotions);
        if (activePromotions != null && activePromotions.length > 0)
        {
            for (int i = 0; i < activePromotions.length; i++)
            {
                PromotionIf prom = activePromotions[i];
                sd.getActivePromotionMap().put(new Integer(prom.getId()), prom);
            }
        }
    }

    /**
     * Retrieves the products that match the criteria in a ProductSearch object and puts them in the
     * <code>currentProducts</code> array. The following attributes of the ProductMgr are set by
     * this method:
     * <ul>
     * <li>prodSearch</li>
     * <li>dataDesc</li>
     * <li>showingSpecials</li>
     * <li>expiredResultSet</li>
     * <li>prodTimestamp</li>
     * <li>currentTagGroups</li>
     * <li>currentProducts</li>
     * <li>totalNumberOfProducts</li>
     * <li>currentCategories</li>
     * <li>currentManufacturers</li>
     * <li>selectedCategory</li>
     * <li>selectedManufacturer</li>
     * </ul>
     * 
     * @param dd
     *            Used to control the data offset, limit the number of items returned and set the
     *            sort order
     * @param ps
     *            Contains information used to search the catalog for a product
     * @return Returns a Products object
     * @throws KKException
     *             an unexpected KKException exception
     * @throws KKAppException
     *             an unexpected KKAppException exception
     */
    public ProductsIf fetchProducts(DataDescriptorIf dd, ProductSearchIf ps)
            throws KKException, KKAppException
    {
        return fetchProducts(dd, ps, null, null);
    }

    /**
     * Retrieves the products that match the criteria in a ProductSearch object and puts them in the
     * <code>currentProducts</code> array. After some pre-processing this method calls one of the
     * following kkEng calls;
     * <ul>
     * <li>searchForProductsWithOptions()</li>
     * <li>getSpecialsPerCategory()</li>
     * <li>getAllSpecials()</li>
     * </ul>
     * The following attributes of the ProductMgr are set by this method:
     * <ul>
     * <li>prodSearch</li>
     * <li>dataDesc</li>
     * <li>showingSpecials</li>
     * <li>expiredResultSet</li>
     * <li>prodTimestamp</li>
     * <li>currentTagGroups</li>
     * <li>currentProducts</li>
     * <li>totalNumberOfProducts</li>
     * <li>currentCategories</li>
     * <li>currentManufacturers</li>
     * <li>selectedCategory</li>
     * <li>selectedManufacturer</li>
     * </ul>
     * 
     * @param dd
     *            Used to control the data offset, limit the number of items returned and set the
     *            sort order
     * @param ps
     *            Contains information used to search the catalog for a product
     * @param options
     *            Contains options for the method. It may be set to null when the method is used to
     *            fetch a fresh result set of products. The options are:
     *            <ul>
     *            <li>Type of operation. (GET, NAVIGATE, FILTER or SORT). Defaults to GET.</li>
     *            <li>An option to instruct the method to call the engine with
     *            getSpecialsPerCategory() rather than searchForProductsWithOptions()</li>
     *            <li>An option to instruct the method to display the reduced number of facets
     *            returned from Solr (based on the filter search results) rather than the complete
     *            list.</li>
     *            </ul>
     * @param catMenu
     *            The category menu passed from the Category manager
     * @return Returns a Products object
     * @throws KKException
     *             an unexpected KKException exception
     * @throws KKAppException
     *             an unexpected KKAppException exception
     */
    public ProductsIf fetchProducts(DataDescriptorIf dd, ProductSearchIf ps, ProductFetch options,
            List<CategoryIf> catMenu) throws KKException, KKAppException

    {
        this.prodSearch = (ps == null) ? new ProductSearch() : ps;
        this.dataDesc = (dd == null) ? createDataDesc() : dd;
        this.showingSpecials = (options == null) ? false : options.isGetSpecials();
        boolean setCustomFacets = (options == null) ? true : options.isSetCustomFacets();
        int operation = (options == null) ? ProductFetch.OP_GET : options.getOperation();

        // Set the page to display
        if (this.dataDesc.getOffset() == 0)
        {
            setDataDescOffset(navStart);
        }

        // Reset the expired flag
        expiredResultSet = false;

        // Reset the filter count
        if (operation == ProductFetch.OP_GET)
        {
            numSelectedFilters = 0;
            priceFilter = false;
        }

        // Reset the current categories
        currentCategories = emptyCatArray;

        if (this.prodSearch.getCategoryId() > 0)
        {
            // Get tag groups only for a fresh get. Not for a navigation or filter.
            if (operation == ProductFetch.OP_GET)
            {
                fetchTagGroupsPerCategory(this.prodSearch.getCategoryId());
            }
            TagGroupIf[] selectedTagGroups = getTagGroupFilter();
            this.prodSearch.setTagGroups(selectedTagGroups);
            if (selectedTagGroups != null && selectedTagGroups.length > 0 && kkAppEng.isUseSolr())
            {
                this.prodSearch.setReturnCustomFacets(true);
            } else
            {
                this.prodSearch.setReturnCustomFacets(false);
            }
        } else
        {
            currentTagGroups = null;
            this.prodSearch.setTagGroups(null);
        }

        // If Solr is enabled we get Solr to return facets
        if (kkAppEng.isUseSolr())
        {
            this.prodSearch.setForceUseSolr(true);
        }

        ProductsIf prods = null;
        if (this.showingSpecials)
        {
            if (this.prodSearch.getCategoryId() > -1)
            {
                prods = eng.getSpecialsPerCategory(kkAppEng.getSessionId(), dataDesc,
                        this.prodSearch.getCategoryId(), this.prodSearch.isSearchInSubCats(),
                        kkAppEng.getLangId());
            } else
            {
                prods = eng.getAllSpecials(kkAppEng.getSessionId(), dataDesc, kkAppEng.getLangId());
            }
        } else
        {
            prods = eng.searchForProductsWithOptions(kkAppEng.getSessionId(), dataDesc,
                    this.prodSearch, kkAppEng.getLangId(), kkAppEng.getFetchProdOptionsNoImages());
        }

        if (prods != null)
        {
            currentProducts = prods.getProductArray();
            totalNumberOfProducts = prods.getTotalNumProducts();
            manageCurrentCategories(prods.getCategoryFacets(), catMenu, operation, this.prodSearch);

            if ((operation == ProductFetch.OP_GET
                    || operation == ProductFetch.OP_FILTER && setCustomFacets))
            {
                if (kkAppEng.isUseSolr())
                {
                    maxPrice = prods.getMaxPrice();
                    minPrice = prods.getMinPrice();
                    taxMultiplier = prods.getTaxMultiplier();
                    priceFacets = prods.getPriceFacets();
                    if (priceFilter && priceFacets != null && priceFacets.length == 1)
                    {
                        priceFacets[0].setSelected(true);
                    }

                    if (prods.getManufacturerFacets() != null
                            && prods.getManufacturerFacets().length > 0)
                    {
                        currentManufacturers = prods.getManufacturerFacets();
                        if (this.prodSearch.getManufacturerId() > -1)
                        {
                            for (int i = 0; i < currentManufacturers.length; i++)
                            {
                                ManufacturerIf manu = currentManufacturers[i];
                                if (manu.getId() == this.prodSearch.getManufacturerId())
                                {
                                    manu.setSelected(true);
                                }
                            }
                        }
                        if (this.prodSearch.getManufacturerIds() != null
                                && this.prodSearch.getManufacturerIds().length > 0)
                        {
                            for (int i = 0; i < currentManufacturers.length; i++)
                            {
                                ManufacturerIf manu = currentManufacturers[i];
                                for (int j = 0; j < prodSearch.getManufacturerIds().length; j++)
                                {
                                    int id = prodSearch.getManufacturerIds()[j];
                                    if (manu.getId() == id)
                                    {
                                        manu.setSelected(true);
                                        break;
                                    }
                                }
                            }
                        }
                    } else
                    {
                        /*
                         * Only reset if we were meant to get some back from Solr but didn't
                         */
                        if (this.prodSearch.isReturnManufacturerFacets())
                        {
                            currentManufacturers = emptyManuArray;
                        }
                    }
                }
                if (this.prodSearch.isReturnCustomFacets())
                {
                    setCustomFacets(prods);
                }
            }
        }

        if (currentProducts == null || currentProducts.length == 0)
        {
            currentProducts = emptyProdArray;
        }

        if (this.prodSearch.getCategoryId() < 0)
        {
            /*
             * If no category id was specified we reset the selected category and tag groups
             */
            selectedCategory = new Category();
            currentTagGroups = null;
        } else
        {
            selectedCategory = kkAppEng.getCategoryMgr()
                    .getCatFromId(this.prodSearch.getCategoryId());
            selectedCategory = (selectedCategory == null) ? new Category() : selectedCategory;
        }

        if (this.prodSearch.getManufacturerId() < 0)
        {
            /*
             * If no manufacturer id was specified we reset the selected manufacturer
             */
            selectedManufacturer = new Manufacturer();
        } else
        {
            selectedManufacturer = kkAppEng.getEng()
                    .getManufacturer(this.prodSearch.getManufacturerId(), kkAppEng.getLangId());
            selectedManufacturer = (selectedManufacturer == null) ? new Manufacturer()
                    : selectedManufacturer;
        }

        if (!kkAppEng.isUseSolr() && operation == ProductFetch.OP_GET)
        {
            /*
             * We got products and there was a category id constraint so if not using Solr we need
             * to fetch the manufacturers for the products in the result set
             */
            if (this.prodSearch.getCategoryId() > -1)
            {
                fetchManufacturersPerCategory(this.prodSearch.getCategoryId());
            } else
            {
                currentManufacturers = emptyManuArray;
            }

            if (this.prodSearch.getManufacturerId() > -1)
            {
                CategoryIf[] catArray = eng.getCategoriesPerManufacturer(
                        this.prodSearch.getManufacturerId(), kkAppEng.getLangId());
                manageCurrentCategories(catArray, catMenu, operation, this.prodSearch);
            }
        }

        this.setBackAndNext();

        /*
         * Generates a timestamp and saves a clone of the DataDescriptor and ProductSearch and tag
         * groups
         */
        generateTimestamp();

        return prods;
    }

    /**
     * Manages the current categories to display
     * 
     * @param catsFound
     * @param catMenu
     * @param operation
     * @param ps
     */
    private void manageCurrentCategories(CategoryIf[] catsFound, List<CategoryIf> catMenu,
            int operation, ProductSearchIf search)
    {
        synchronized (mutex)
        {
            CategoryMgr catMgr = kkAppEng.getCategoryMgr();
            List<CategoryIf> catMenuList = null;
            // Don't use Solr facets if doing a get by category
            if (catsFound != null && catsFound.length > 0
                    && !(operation == ProductFetch.OP_GET && search.getCategoryId() > -1))
            {
                catMenuList = catMgr.getCatMenuList(catsFound);
                if (search.getCategoryId() > -1)
                {
                    for (Iterator<CategoryIf> iterator = catMenuList.iterator(); iterator
                            .hasNext();)
                    {
                        CategoryIf cat = iterator.next();
                        if (cat.getId() == search.getCategoryId())
                        {
                            cat.setSelected(true);
                            break;
                        }
                    }
                }
            } else if (catMenu != null && catMenu.size() > 0)
            {
                catMenuList = catMenu;
            } else if (search.getCategoryId() > -1)
            {
                catMenuList = catMgr.getCatMenuList(catMgr.getCatFromId(search.getCategoryId()));
            }

            if (catMenuList != null && catMenuList.size() > 0)
            {
                CategoryIf[] catMenuArray = catMenuList.toArray(new CategoryIf[0]);
                currentCategories = catMenuArray;
            }
        }
    }

    /**
     * Check to see whether the time stamp has expired
     * 
     * @param timestamp
     * @return Return true if expired
     * @throws KKException
     */
    private boolean hasTimestampExpired(long timestamp) throws KKException
    {
        if (timestamp != prodTimestamp)
        {
            SearchData sd = searchDataMap.get(new Long(timestamp));
            if (sd != null)
            {
                this.dataDesc = sd.getDd();
                this.prodSearch = sd.getPs();
                this.currentOffset = sd.getCurrentOffset();
                this.currentPage = sd.getCurrentPage();
                this.currentTagGroups = sd.getTagGroups();
                tagMap.clear();
                if (currentTagGroups != null)
                {
                    for (int i = 0; i < currentTagGroups.length; i++)
                    {
                        TagGroupIf tg = currentTagGroups[i];
                        if (tg != null && tg.getTags() != null)
                        {
                            for (int j = 0; j < tg.getTags().length; j++)
                            {
                                TagIf tag = tg.getTags()[j];
                                tagMap.put(new Integer(tag.getId()), tag);
                            }
                        }
                    }
                }
                this.currentManufacturers = sd.getCurrentManufacturers();
                kkAppEng.setNav(sd.getNav());
                if (kkAppEng.isUseSolr())
                {
                    minPrice = sd.getMinPrice();
                    maxPrice = sd.getMaxPrice();
                    taxMultiplier = sd.getTaxMultiplier();
                    priceFacets = sd.getPriceFacets();
                }
            } else
            {
                expiredResultSet = true;
                reset();
                return true;
            }
        }
        return false;
    }

    /**
     * Method used to filter an existing product result set. The result set may be filtered by
     * <ul>
     * <li>Category</li>
     * <li>Manufacturer</li>
     * <li>Price</li>
     * <li>Tag or Facet</li>
     * </ul>
     * 
     * @param filter
     *            Object containing the allowed filters
     * @param timestamp
     *            Indicates when the result set was created. If it doesn't match the timestamp of
     *            the current result set, the operation is aborted.
     * @return Returns a Products object
     * @throws KKException
     *             an unexpected KKException exception
     * @throws KKAppException
     *             an unexpected KKAppException exception
     */
    public ProductsIf filterProducts(ProductFilter filter, long timestamp)
            throws KKException, KKAppException
    {
        if (hasTimestampExpired(timestamp))
        {
            return new Products();
        }

        setDataDescOffset(navStart);

        if (this.prodSearch == null || this.dataDesc == null || filter == null)
        {
            return null;
        }

        if (filter.getTagId() > -1)
        {
            /*
             * See whether the tag was already selected, and toggle the selection. All tags are
             * stored in a hash map.
             */
            TagIf tag = tagMap.get(new Integer(filter.getTagId()));
            if (tag != null)
            {
                if (tag.isSelected())
                {
                    // Reset selected
                    tag.setSelected(false);
                    numSelectedFilters--;
                } else
                {
                    // Set selected
                    tag.setSelected(true);
                    numSelectedFilters++;
                }
            }
        } else if (filter.getCategoryId() > -1
                || filter.getCategoryId() == ProductSearch.SEARCH_ALL)
        {
            prodSearch.setCategoryId(filter.getCategoryId());
        } else if (filter.getManufacturerId() > -1
                || filter.getManufacturerId() == ProductSearch.SEARCH_ALL)
        {
            ArrayList<Integer> manuIdList = new ArrayList<Integer>();
            for (int i = 0; i < currentManufacturers.length; i++)
            {
                ManufacturerIf manu = currentManufacturers[i];
                if (manu.getId() == filter.getManufacturerId())
                {
                    if (manu.isSelected())
                    {
                        manu.setSelected(false);
                        numSelectedFilters--;
                    } else
                    {
                        manu.setSelected(true);
                        numSelectedFilters++;
                    }
                }
                if (manu.isSelected())
                {
                    manuIdList.add(manu.getId());
                }
            }

            int[] manufacturerIds = new int[manuIdList.size()];
            int i = 0;
            for (Iterator<Integer> iterator = manuIdList.iterator(); iterator.hasNext();)
            {
                Integer id = iterator.next();
                manufacturerIds[i++] = id.intValue();
            }
            prodSearch.setManufacturerIds(manufacturerIds);
        } else if (filter.getPriceFrom() != null || filter.getPriceTo() != null)
        {
            if (priceFacets != null && priceFacets.length > 0)
            {
                for (int i = 0; i < priceFacets.length; i++)
                {
                    KKPriceFacetIf pf = priceFacets[i];
                    if (filter.getPriceFrom() != null
                            && filter.getPriceFrom().compareTo(pf.getLowerLimit()) == 0)
                    {
                        if (pf.isSelected())
                        {
                            pf.setSelected(false);
                            prodSearch.setPriceFrom(null);
                            prodSearch.setPriceTo(null);
                            priceFilter = false;
                        } else
                        {
                            pf.setSelected(true);
                            prodSearch.setPriceFrom(filter.getPriceFrom());
                            // Simulate < behavior for price to rather than <=
                            prodSearch.setPriceTo(
                                    filter.getPriceTo().subtract(new BigDecimal("0.001")));
                            priceFilter = true;
                        }
                    } else
                    {
                        pf.setSelected(false);
                    }
                }
            } else
            {
                // Using slider
                prodSearch.setPriceFrom(filter.getPriceFrom());
                prodSearch.setPriceTo(filter.getPriceTo());
                if (minPrice != null && maxPrice != null && filter.getPriceFrom() != null
                        && filter.getPriceTo() != null)
                {
                    if (filter.getPriceFrom().compareTo(minPrice) == 0
                            && filter.getPriceTo().compareTo(maxPrice) == 0)
                    {
                        priceFilter = false;
                    } else
                    {
                        priceFilter = true;
                    }
                }
            }
        } else if (filter.isRemoveAllTags())
        {
            // Set the tags to "not selected"
            if (currentTagGroups != null)
            {
                for (int i = 0; i < currentTagGroups.length; i++)
                {
                    TagGroupIf tg = currentTagGroups[i];
                    if (tg != null && tg.getTags() != null)
                    {
                        for (int j = 0; j < tg.getTags().length; j++)
                        {
                            TagIf tag = tg.getTags()[j];
                            tag.setSelected(false);
                        }
                    }
                }
            }

            if (priceFacets != null && priceFacets.length > 0)
            {
                for (int i = 0; i < priceFacets.length; i++)
                {
                    KKPriceFacetIf pf = priceFacets[i];
                    pf.setSelected(false);
                }
            }

            for (int i = 0; i < currentManufacturers.length; i++)
            {
                currentManufacturers[i].setSelected(false);
            }
            this.prodSearch.setManufacturerIds(null);

            for (int i = 0; i < currentCategories.length; i++)
            {
                currentCategories[i].setSelected(false);
            }
            this.prodSearch.setCategoryIds(null);

            this.prodSearch.setPriceFrom(null);
            this.prodSearch.setPriceTo(null);

            numSelectedFilters = 0;
            priceFilter = false;
        }

        ProductFetch options = new ProductFetch();
        options.setOperation(ProductFetch.OP_FILTER);
        if (sd.isUseSolrDynamicFacets())
        {
            options.setSetCustomFacets(true);
        } else
        {
            options.setSetCustomFacets(false);
        }
        ProductsIf prods = fetchProducts(this.dataDesc, this.prodSearch, options, null);

        return prods;
    }

    /**
     * The method is called to retrieve a list of manufacturers that may be used to filter the
     * result set. Only used when not using Solr. <code>currentManufacturers</code> is set with the
     * results.
     * 
     * @param catId
     *            The id of the selected category
     * @return The number of manufacturers found
     * @throws KKException
     *             an unexpected KKException exception
     */
    public int fetchManufacturersPerCategory(int catId) throws KKException
    {
        ManufacturerSearchIf search = new ManufacturerSearch();
        search.setCategoryId(catId);
        if (kkAppEng.getFetchProdOptions() != null)
        {
            search.setCatalogId(kkAppEng.getFetchProdOptions().getCatalogId());
        }
        DataDescriptorIf dataDesc = new DataDescriptor();
        dataDesc.setOrderBy(DataDescConstants.ORDER_BY_NAME_ASCENDING);
        currentManufacturers = eng.getManufacturers(dataDesc, search, kkAppEng.getLangId());
        return currentManufacturers.length;
    }

    /**
     * This method is called to change the order of a list of products in the currentProducts array.
     * The orderBy parameter can take a range of values. The valid orderBy values are:
     * <ul>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_NAME_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_NAME_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_PRICE_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_PRICE_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_DATE_ADDED</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_DATE_ADDED_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_DATE_ADDED_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_TIMES_VIEWED</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_TIMES_VIEWED_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_TIMES_VIEWED_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_TIMES_ORDERED</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_TIMES_ORDERED_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_TIMES_ORDERED_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_RATING_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_RATING_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_ID</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_MANUFACTURER</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM1_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM1_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM2_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM2_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM3_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM3_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM4_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM4_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM5_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM5_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM6_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM6_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM7_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM7_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM8_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM8_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM9_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM9_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM10_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM10_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM1INT_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM1INT_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM2INT_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM2INT_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM1DEC_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM1DEC_DESCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM2DEC_ASCENDING</li>
     * <li>com.konakart.app.DataDescConstants.ORDER_BY_CUSTOM2DEC_DESCENDING</li>
     * </ul>
     * The timestamp should match the timestamp of when the result set was created. Otherwise it
     * indicates that the customer, using the back button has navigated back to an old page of
     * search results for which we no longer have the result set. If it doesn't match, the operation
     * is aborted.
     * 
     * @param orderBy
     *            The order by parameter
     * @param timestamp
     *            The time stamp when the result set was created
     * @return Returns a Products object
     * @throws KKException
     *             an unexpected KKException exception
     * @throws KKAppException
     *             an unexpected KKAppException exception
     */
    public ProductsIf orderCurrentProds(String orderBy, long timestamp)
            throws KKException, KKAppException
    {
        if (hasTimestampExpired(timestamp))
        {
            return new Products();
        }

        setDataDescOffset(navStart);

        if (orderBy != null)
        {
            dataDesc.setOrderBy(orderBy);
        }
        ProductFetch options = new ProductFetch();
        options.setOperation(ProductFetch.OP_SORT);
        options.setGetSpecials(this.showingSpecials);

        ProductsIf prods = fetchProducts(this.dataDesc, this.prodSearch, options, null);

        return prods;
    }

    /**
     * This method is called to navigate through a list of products when the list is longer than
     * maxRows. The currentProducts array is updated with the new products.
     * <p>
     * <code>navDir</code> can take the following values which are retrieved using getter methods on
     * the ProductMgr instance:
     * <ul>
     * <li>navNext</li>
     * <li>navBack</li>
     * <li>navStart</li>
     * </ul>
     * The timestamp should match the timestamp of when the result set was created. Otherwise it
     * indicates that the customer, using the back button has navigated back to an old page of
     * search results for which we no longer have the result set. If it doesn't match, the operation
     * is aborted.
     * 
     * 
     * @param navDir
     *            The navigation direction.
     * @param timestamp
     *            The time stamp when the result set was created
     * @return Returns a Products object
     * @throws KKException
     *             an unexpected KKException exception
     * @throws KKAppException
     *             an unexpected KKAppException exception
     */
    public ProductsIf navigateCurrentProducts(String navDir, long timestamp)
            throws KKException, KKAppException
    {
        if (hasTimestampExpired(timestamp))
        {
            return new Products();
        }

        setDataDescOffset(navDir);

        ProductFetch options = new ProductFetch();
        options.setOperation(ProductFetch.OP_SORT);
        options.setGetSpecials(this.showingSpecials);

        ProductsIf prods = fetchProducts(this.dataDesc, this.prodSearch, options, null);

        return prods;
    }

    /**
     * Set the custom facets from the search result
     * 
     * @param prods
     */
    private void setCustomFacets(ProductsIf prods)
    {
        if (prods.getCustomFacets() != null && prods.getCustomFacets().length > 0)
        {
            HashMap<String, String> selectedTagMap = new HashMap<String, String>();
            if (this.prodSearch.getTagGroups() != null)
            {
                for (int i = 0; i < this.prodSearch.getTagGroups().length; i++)
                {
                    TagGroupIf tg = this.prodSearch.getTagGroups()[i];
                    if (tg.getTags() != null)
                    {
                        for (int j = 0; j < tg.getTags().length; j++)
                        {
                            TagIf t = tg.getTags()[j];
                            if (t.isSelected())
                            {
                                selectedTagMap.put(t.getName(), "");
                            }
                        }
                    }
                }
            }

            /*
             * If we are using Solr we create the tag groups and tags from the facet information
             * returned by Solr
             */
            tagMap.clear();

            int tagId = 0;
            currentTagGroups = new TagGroup[prods.getCustomFacets().length];
            for (int i = 0; i < prods.getCustomFacets().length; i++)
            {
                KKFacetIf facet = prods.getCustomFacets()[i];
                TagGroup tg = new TagGroup();
                tg.setId(i);
                tg.setName(facet.getName());
                tg.setFacetNumber(facet.getNumber());
                if (facet.getValues() != null && facet.getValues().length > 0)
                {
                    Tag[] tags = new Tag[facet.getValues().length];
                    for (int j = 0; j < facet.getValues().length; j++)
                    {
                        NameNumberIf nm = facet.getValues()[j];
                        Tag tag = new Tag();
                        tag.setId(tagId++);
                        tag.setName(nm.getName());
                        tag.setNumProducts(nm.getNumber());
                        if (selectedTagMap.get(tag.getName()) != null)
                        {
                            tag.setSelected(true);
                        }
                        tagMap.put(new Integer(tag.getId()), tag);
                        tags[j] = tag;
                    }
                    tg.setTags(tags);
                }
                currentTagGroups[i] = tg;
            }
        }
    }

    /**
     * Called when a new set of products has been fetched into currentProducts in order to correctly
     * set the back and next buttons
     * 
     */
    private void setBackAndNext()
    {

        pageList = getPages( /* currentPage */
                currentPage);

        // We always attempt to fetch back maxRows + 1
        if (currentProducts.length > getMaxDisplaySearchResults())
        {
            this.showNext = 1;
        } else
        {
            this.showNext = 0;
        }
        if (currentOffset > 0)
        {
            this.showBack = 1;
        } else
        {
            this.showBack = 0;
        }
    }

    /**
     * Based on the action we are being asked to perform and the current offset, we set the new
     * offset before going to the engine to ask for more products.
     * 
     * @param action
     * @throws KKAppException
     */
    private void setDataDescOffset(String action) throws KKAppException
    {
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
            currentOffset = 0;
            currentPage = 1;
        } else if (action.equals(navNext))
        {
            currentOffset += getMaxDisplaySearchResults();
            currentPage = (currentOffset / getMaxDisplaySearchResults()) + 1;
        } else if (action.equals(navBack))
        {
            currentOffset -= getMaxDisplaySearchResults();
            if (currentOffset < 0)
            {
                currentOffset = 0;
            }
            currentPage = (currentOffset / getMaxDisplaySearchResults()) + 1;
        } else if (requestedPage > 0)
        {
            currentOffset = getMaxDisplaySearchResults() * (requestedPage - 1);
            currentPage = requestedPage;
        } else if (requestedPage <= 0)
        {
            currentOffset = 0;
            currentPage = 1;
        }
        dataDesc.setOffset(currentOffset);
    }

    /**
     * Get an array list of pages to show
     * 
     * @param _currentPage
     * @return Returns an array List of pages to show
     */
    private ArrayList<Integer> getPages(int _currentPage)
    {
        numPages = totalNumberOfProducts / getMaxDisplaySearchResults();
        if (totalNumberOfProducts % getMaxDisplaySearchResults() != 0)
        {
            numPages++;
        }

        pageList.clear();

        return getPages(_currentPage, numPages, sd.getMaxPageLinks(), pageList);

    }

    /**
     * This method is called when a user selects a product in order to retrieve more details for
     * that product. If the product has options that can be selected by the user, then the list of
     * selectedProductOptions is populated so that the JSP can easily render the drop lists for the
     * user to make selections.
     * 
     * The selectedProduct and selectedManufacturer objects are updated.
     * 
     * @param prodId
     *            The id of the product
     * @throws KKException
     *             an unexpected KKException exception
     * @throws KKAppException
     *             an unexpected KKAppException exception
     */
    public void fetchSelectedProduct(int prodId) throws KKException, KKAppException
    {
        prodTimestamp = 0;
        selectedProduct = eng.getProductWithOptions(kkAppEng.getSessionId(), prodId,
                kkAppEng.getLangId(), kkAppEng.getFetchProdOptions());
        if (selectedProduct == null)
        {
            return;
        }

        // Oracle returns it as null
        if (selectedProduct.getUrl() == null)
        {
            selectedProduct.setUrl("");
        }

        selectedManufacturer = eng.getManufacturer(selectedProduct.getManufacturerId(),
                kkAppEng.getLangId());
        if (selectedManufacturer == null)
        {
            selectedManufacturer = new Manufacturer();
        }
        selectedProduct.setManufacturer(selectedManufacturer);

        // If the product has options, then we must create a data structure that
        // is easy to display on the JSP page
        if (selectedProduct.getOpts() != null)
        {
            this.selectedProductOptions.clear();
            HashMap<Integer, ProdOptionContainer> containerMap = new HashMap<Integer, ProdOptionContainer>();
            int optId = -1;
            ProdOptionContainer poc = null;
            for (int i = 0; i < selectedProduct.getOpts().length; i++)
            {
                OptionIf opt = selectedProduct.getOpts()[i];
                if (opt.getId() != optId)
                {
                    poc = containerMap.get(opt.getId());
                    if (poc == null)
                    {
                        // If the new id doesn't match the previous id in the
                        // list and it doesn't exist in the map, it means that we are starting a
                        // new option and so must create a new ProdOptionContainer.
                        poc = new ProdOptionContainer(opt);
                        this.selectedProductOptions.add(poc);
                        containerMap.put(opt.getId(), poc);
                    }
                }
                optId = opt.getId();
                ProdOption po = new ProdOption(opt, kkAppEng);
                if (poc != null)
                {
                    poc.getOptValues().add(po);
                }
            }
        }

        // Get all categories for the product and put them in a hash map
        CategoryIf[] cats = eng.getCategoriesPerProduct(prodId, kkAppEng.getLangId());
        HashMap<Integer, CategoryIf> catMap = new HashMap<Integer, CategoryIf>();
        if (cats != null)
        {
            for (int i = 0; i < cats.length; i++)
            {
                CategoryIf cat = cats[i];
                catMap.put(new Integer(cat.getId()), cat);
            }
        }

        /*
         * Set the current category for the selected product so that the category tile shows the
         * correct navigation. Don't set the category if the current category is already a valid
         * category that the product belongs to. The reason for this is that the product may belong
         * to multiple categories and we don't want to navigate to it from one category and then
         * change the category tile to point to the wrong category.
         */
        CategoryIf cat = null;
        if (kkAppEng.getCategoryMgr().getCurrentCat() != null)
        {
            cat = catMap.get(new Integer(kkAppEng.getCategoryMgr().getCurrentCat().getId()));
        }
        if (cat == null)
        {
            kkAppEng.getCategoryMgr().setCurrentCat(selectedProduct.getCategoryId());
        }
        selectedProductCategory = kkAppEng.getCategoryMgr().getCurrentCat();
    }

    /**
     * Returns the number of products in the currentProducts array.
     * 
     * @return Returns the number of products in the currentProducts array.
     */
    public int getNumberOfProducts()
    {
        // We attempt to fetch 1 more record than the number in maxRows so that
        // we can determine whether to show the next button. However, the JSP
        // should only show the number of records displayed which is limited to
        // maxRows within the JSP itself
        if (currentProducts.length == getMaxDisplaySearchResults() + 1)
        {
            return getMaxDisplaySearchResults();
        }
        return currentProducts.length;
    }

    /**
     * Populates the alsoPurchasedArray array. It uses the selectedProduct as the reference product.
     * The product description is not fetched.
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void fetchAlsoPurchasedArray() throws KKException
    {
        fetchAlsoPurchasedArray(/* fillDescription */false);
    }

    /**
     * Populates the alsoPurchasedArray array. It uses the selectedProduct as the reference product.
     * 
     * @param fillDescription
     *            When set to true, the product description is also fetched.
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void fetchAlsoPurchasedArray(boolean fillDescription) throws KKException
    {
        if (selectedProduct == null)
        {
            return;
        }

        DataDescriptorIf lDataDesc = new DataDescriptor();
        lDataDesc.setLimit(sd.getMaxDispAlsoPurchased());
        lDataDesc.setOffset(0);
        lDataDesc.setOrderBy(defaultOrderBy);
        lDataDesc.setFillDescription(fillDescription);
        alsoPurchased = eng.getAlsoPurchasedWithOptions(kkAppEng.getSessionId(), lDataDesc,
                selectedProduct.getId(), kkAppEng.getLangId(),
                kkAppEng.getFetchProdOptionsNoImages());
    }

    /**
     * Fetches the related products from the DB. It uses the selectedProduct as the reference
     * product. The product description is not fetched. The following arrays are populated with the
     * results :
     * <ul>
     * <li>allRelatedProducts</li>
     * <li>upSellProducts</li>
     * <li>crossSellProducts</li>
     * <li>accessories</li>
     * <li>dependentProducts</li>
     * <li>bundledProducts</li>
     * </ul>
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void fetchRelatedProducts() throws KKException
    {
        fetchRelatedProducts(/* fillDescription */false);
    }

    /**
     * Fetches the related products from the DB. It uses the selectedProduct as the reference
     * product. The following arrays are populated with the results :
     * <ul>
     * <li>allRelatedProducts</li>
     * <li>upSellProducts</li>
     * <li>crossSellProducts</li>
     * <li>accessories</li>
     * <li>dependentProducts</li>
     * <li>bundledProducts</li>
     * </ul>
     * 
     * @param fillDescription
     *            When set to true, the product description is also fetched.
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void fetchRelatedProducts(boolean fillDescription) throws KKException
    {
        List<ProductIf> upSellList = null, crossSellList = null, accessoryList = null,
                dependentList = null;

        int upSellNum = 0, crossSellNum = 0, accessoryNum = 0, dependentNum = 0;

        if (selectedProduct == null)
        {
            return;
        }

        // Set current arrays to null
        allRelatedProducts = null;
        upSellProducts = null;
        crossSellProducts = null;
        accessories = null;
        dependentProducts = null;
        bundledProducts = null;

        DataDescriptorIf lDataDesc = new DataDescriptor();
        lDataDesc.setOrderBy(defaultOrderBy);
        // We set a high limit since we are asking for all related products in one go. In order to
        // set the correct limit we'd have to make 4 separate calls to the engine so we do it this
        // way which should perform better. The limit is checked further down when we add the
        // products to the arrays.
        lDataDesc.setLimit(100);
        lDataDesc.setOffset(0);
        lDataDesc.setFillDescription(fillDescription);

        // If product is a bundle, then get products within the bundle
        if (selectedProduct.getType() == com.konakart.bl.ProductMgr.BUNDLE_PRODUCT_TYPE
                || selectedProduct
                        .getType() == com.konakart.bl.ProductMgr.FREE_SHIPPING_BUNDLE_PRODUCT_TYPE)
        {
            ProductsIf bundle = eng.getRelatedProductsWithOptions(kkAppEng.getSessionId(),
                    lDataDesc, selectedProduct.getId(),
                    com.konakart.bl.ProductMgr.BUNDLED_PRODUCT_RELATIONSHIP, kkAppEng.getLangId(),
                    kkAppEng.getFetchProdOptionsNoImages());
            if (bundle != null)
            {
                bundledProducts = bundle.getProductArray();
                selectedProduct.setBundledProducts(bundledProducts);
            }
        }

        // Get merchandising products
        ProductsIf relatedProds = eng.getRelatedProductsWithOptions(kkAppEng.getSessionId(),
                lDataDesc, selectedProduct.getId(), com.konakart.bl.ProductMgr.ALL_RELATIONSHIPS,
                kkAppEng.getLangId(), kkAppEng.getFetchProdOptionsNoImages());

        if (relatedProds.getProductArray() != null)
        {
            for (int i = 0; i < relatedProds.getProductArray().length; i++)
            {
                ProductIf prod = relatedProds.getProductArray()[i];
                if (prod.getProdRelationTypeArray() != null)
                {
                    for (int j = 0; j < prod.getProdRelationTypeArray().length; j++)
                    {
                        int relType = prod.getProdRelationTypeArray()[j];
                        switch (relType)
                        {
                        case com.konakart.bl.ProductMgr.UP_SELL:
                            if (upSellList == null)
                            {
                                upSellList = new ArrayList<ProductIf>();
                            }
                            if (upSellNum++ < sd.getMaxDispUpSellProds())
                            {
                                upSellList.add(prod);
                            }
                            break;
                        case com.konakart.bl.ProductMgr.CROSS_SELL:
                            if (crossSellList == null)
                            {
                                crossSellList = new ArrayList<ProductIf>();
                            }

                            if (crossSellNum++ < sd.getMaxDispCrossSellProds())
                            {
                                crossSellList.add(prod);
                            }
                            break;
                        case com.konakart.bl.ProductMgr.ACCESSORY:
                            if (accessoryList == null)
                            {
                                accessoryList = new ArrayList<ProductIf>();
                            }

                            if (accessoryNum++ < sd.getMaxDispAccessories())
                            {
                                accessoryList.add(prod);
                            }
                            break;
                        case com.konakart.bl.ProductMgr.DEPENDENT_ITEM:
                            if (dependentList == null)
                            {
                                dependentList = new ArrayList<ProductIf>();
                            }

                            if (dependentNum++ < sd.getMaxDispDependentProds())
                            {
                                dependentList.add(prod);
                            }
                            break;

                        default:
                            break;
                        }
                    }
                }
            }

            // Create the arrays
            int count = 0;
            if (upSellList != null && upSellList.size() > 0)
            {
                upSellProducts = new ProductIf[upSellList.size()];
                int i = 0;
                for (Iterator<ProductIf> iter = upSellList.iterator(); iter.hasNext();)
                {
                    ProductIf prod = iter.next();
                    upSellProducts[i++] = prod;
                    count++;
                }
            }
            if (crossSellList != null && crossSellList.size() > 0)
            {
                crossSellProducts = new ProductIf[crossSellList.size()];
                int i = 0;
                for (Iterator<ProductIf> iter = crossSellList.iterator(); iter.hasNext();)
                {
                    ProductIf prod = iter.next();
                    crossSellProducts[i++] = prod;
                    count++;
                }
            }
            if (accessoryList != null && accessoryList.size() > 0)
            {
                accessories = new ProductIf[accessoryList.size()];
                int i = 0;
                for (Iterator<ProductIf> iter = accessoryList.iterator(); iter.hasNext();)
                {
                    ProductIf prod = iter.next();
                    accessories[i++] = prod;
                    count++;
                }
            }
            if (dependentList != null && dependentList.size() > 0)
            {
                dependentProducts = new ProductIf[dependentList.size()];
                int i = 0;
                for (Iterator<ProductIf> iter = dependentList.iterator(); iter.hasNext();)
                {
                    ProductIf prod = iter.next();
                    dependentProducts[i++] = prod;
                    count++;
                }
            }

            if (count > 0)
            {
                int index = 0;
                allRelatedProducts = new ProductIf[count];
                if (upSellProducts != null)
                {
                    for (int i = 0; i < upSellProducts.length; i++)
                    {
                        ProductIf prod = upSellProducts[i];
                        allRelatedProducts[index++] = prod;
                    }
                }
                if (crossSellProducts != null)
                {
                    for (int i = 0; i < crossSellProducts.length; i++)
                    {
                        ProductIf prod = crossSellProducts[i];
                        allRelatedProducts[index++] = prod;
                    }
                }
                if (accessories != null)
                {
                    for (int i = 0; i < accessories.length; i++)
                    {
                        ProductIf prod = accessories[i];
                        allRelatedProducts[index++] = prod;
                    }
                }
                if (dependentProducts != null)
                {
                    for (int i = 0; i < dependentProducts.length; i++)
                    {
                        ProductIf prod = dependentProducts[i];
                        allRelatedProducts[index++] = prod;
                    }
                }
            }
        }
    }

    /**
     * Get the latest products added to the catalog for the category whose id is passed in as a
     * parameter. The newProducts array is populated. The maximum number of products returned is
     * determined by a configuration variable.
     * 
     * @param categoryId
     *            The id of the selected category
     * @param fillDescription
     *            When set to true, the product description is also fetched.
     * @param forceRefresh
     *            Will fetch the products even if they are already cached
     * @return Returns the newProducts array
     * @throws KKException
     *             an unexpected KKException exception
     */
    public ProductIf[] fetchNewProductsArray(int categoryId, boolean fillDescription,
            boolean forceRefresh) throws KKException
    {
        if (!forceRefresh && newProducts != null && categoryId == newProdsCatId)
        {
            return newProducts;
        }

        newProdsCatId = categoryId;
        DataDescriptorIf lDataDesc = new DataDescriptor();
        lDataDesc.setLimit(sd.getMaxDispNewProds());
        lDataDesc.setOffset(0);
        lDataDesc.setOrderBy(DataDescConstants.ORDER_BY_DATE_ADDED);
        lDataDesc.setFillDescription(fillDescription);

        ProductSearch ps = new ProductSearch();
        ps.setSearchInSubCats(true);
        if (categoryId != com.konakart.bl.ProductMgr.DONT_INCLUDE)
        {
            ps.setCategoryId(categoryId);
        }
        if (kkAppEng.isUseSolr())
        {
            ps.setForceUseSolr(true);
        }
        ProductsIf prods = eng.searchForProductsWithOptions(kkAppEng.getSessionId(), lDataDesc, ps,
                kkAppEng.getLangId(), kkAppEng.getFetchProdOptionsNoImages());
        if (prods != null)
        {
            newProducts = prods.getProductArray();
        } else
        {
            newProducts = new ProductIf[0];
        }

        return newProducts;
    }

    /**
     * Get the on-sale products added to the catalog for the category whose id is passed in as a
     * parameter. The specials array is populated. The maximum number of products returned is
     * determined by a configuration variable.
     * 
     * @param categoryId
     *            The id of the selected category
     * @param searchInSubCats
     *            When true the sub categories will be searched
     * @param fillDescription
     *            When set to true, the product description is also fetched.
     * @param forceRefresh
     *            Will fetch the products even if they are already cached
     * @return Returns the specials array
     * @throws KKException
     *             an unexpected KKException exception
     */
    public ProductIf[] fetchSpecialsArray(int categoryId, boolean searchInSubCats,
            boolean fillDescription, boolean forceRefresh) throws KKException
    {
        if (!forceRefresh && specials != null)
        {
            return specials;
        }
        DataDescriptorIf lDataDesc = new DataDescriptor();
        lDataDesc.setLimit(sd.getMaxDispSpecials());
        lDataDesc.setOffset(0);
        lDataDesc.setOrderBy(DataDescConstants.ORDER_BY_DATE_ADDED);
        lDataDesc.setFillDescription(fillDescription);

        ProductsIf prods = eng.getSpecialsPerCategory(kkAppEng.getSessionId(), lDataDesc,
                categoryId, searchInSubCats, kkAppEng.getLangId());

        if (prods != null)
        {
            specials = prods.getProductArray();
        } else
        {
            specials = new ProductIf[0];
        }

        return specials;
    }

    /**
     * You can supply your own ProductSearch and DataDescriptor objects to return a custom array of
     * objects that will be stored in the session.
     * 
     * @param _prodSearch
     *            The ProductSearch object that will be used to return the products
     * @param _dataDesc
     *            The DataDescriptor object that will be used to return the products
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void fetchCustomProducts1Array(ProductSearchIf _prodSearch, DataDescriptorIf _dataDesc)
            throws KKException
    {
        ProductsIf prods = eng.searchForProductsWithOptions(kkAppEng.getSessionId(), _dataDesc,
                _prodSearch, kkAppEng.getLangId(), kkAppEng.getFetchProdOptionsNoImages());

        if (prods != null)
        {
            customProducts1 = prods.getProductArray();
        }
    }

    /**
     * You can supply your own ProductSearch and DataDescriptor objects to return a custom array of
     * objects that will be stored in the session.
     * 
     * @param _prodSearch
     *            The ProductSearch object that will be used to return the products
     * @param _dataDesc
     *            The DataDescriptor object that will be used to return the products
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void fetchCustomProducts2Array(ProductSearchIf _prodSearch, DataDescriptorIf _dataDesc)
            throws KKException
    {
        ProductsIf prods = eng.searchForProductsWithOptions(kkAppEng.getSessionId(), _dataDesc,
                _prodSearch, kkAppEng.getLangId(), kkAppEng.getFetchProdOptionsNoImages());

        if (prods != null)
        {
            customProducts2 = prods.getProductArray();
        }
    }

    /**
     * You can supply your own ProductSearch and DataDescriptor objects to return a custom array of
     * objects that will be stored in the session.
     * 
     * @param _prodSearch
     *            The ProductSearch object that will be used to return the products
     * @param _dataDesc
     *            The DataDescriptor object that will be used to return the products
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void fetchCustomProducts3Array(ProductSearchIf _prodSearch, DataDescriptorIf _dataDesc)
            throws KKException
    {
        ProductsIf prods = eng.searchForProductsWithOptions(kkAppEng.getSessionId(), _dataDesc,
                _prodSearch, kkAppEng.getLangId(), kkAppEng.getFetchProdOptionsNoImages());

        if (prods != null)
        {
            customProducts3 = prods.getProductArray();
        }
    }

    /**
     * If a customer is logged in, it returns true if he is already being notified about the
     * selected product. Otherwise it returns false.
     * 
     * If not logged in, it always returns false.
     * 
     * @return true or false
     * @throws KKException
     *             an unexpected KKException exception
     */
    public boolean isCurrentProductANotification() throws KKException
    {
        if (kkAppEng.getCustomerMgr().getCurrentCustomer() != null && getSelectedProduct() != null)
        {
            if (kkAppEng.getCustomerMgr().getCurrentCustomer().getId() < 0)
            {
                return false;
            }

            // Ensure that the notifications have been instantiated
            if (kkAppEng.getCustomerMgr().getCurrentCustomer().getProductNotifications() == null)
            {
                kkAppEng.getCustomerMgr().fetchProductNotificationsPerCustomer();
            }

            if (kkAppEng.getCustomerMgr().getCurrentCustomer().getProductNotifications() != null)
            {
                for (int i = 0; i < kkAppEng.getCustomerMgr().getCurrentCustomer()
                        .getProductNotifications().length; i++)
                {
                    ProductIf p = kkAppEng.getCustomerMgr().getCurrentCustomer()
                            .getProductNotifications()[i];
                    if (p.getId() == getSelectedProduct().getId())
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * The method is called to get the digital downloads from the DB for this user. The
     * digitalDownloads array is populated with the result.
     * 
     * @return The number of Digital Downloads found
     * @throws KKException
     *             an unexpected KKException exception
     */
    public DigitalDownloadIf[] fetchDigitalDownloads() throws KKException
    {
        FetchDigitalDownloadOptionsIf options = new FetchDigitalDownloadOptions();
        options.setPopulateProducts(true);
        digitalDownloads = eng.getDigitalDownloadsWithOptions(kkAppEng.getSessionId(),
                kkAppEng.getLangId(), options, kkAppEng.getFetchProdOptionsNoImages());
        return digitalDownloads;
    }

    /**
     * The method is called to populate the <code>currentTagGroups</code> array. If there are any
     * tag groups associated with this category, the tags will be displayed on the UI to allow the
     * customer to refine his product search.
     * 
     * @param catId
     *            The id of the selected category
     * @return The number of tag groups found
     * @throws KKException
     *             an unexpected KKException exception
     */
    public int fetchTagGroupsPerCategory(int catId) throws KKException
    {
        tagMap.clear();

        if (kkAppEng.isUseSolr())
        {
            FetchTagGroupOptionsIf options = new FetchTagGroupOptions();
            options.setPopulateTags(false);
            currentTagGroups = eng.getTagGroupsPerCategoryWithOptions(catId, kkAppEng.getLangId(),
                    options);
        } else
        {
            currentTagGroups = eng.getTagGroupsPerCategory(catId, /* getProdCount */true,
                    kkAppEng.getLangId());

            if (currentTagGroups != null)
            {
                for (int i = 0; i < currentTagGroups.length; i++)
                {
                    TagGroupIf tg = currentTagGroups[i];
                    if (tg != null && tg.getTags() != null)
                    {
                        for (int j = 0; j < tg.getTags().length; j++)
                        {
                            TagIf tag = tg.getTags()[j];
                            tagMap.put(new Integer(tag.getId()), tag);
                        }
                    }
                }
            }

        }
        return (currentTagGroups == null) ? 0 : currentTagGroups.length;

    }

    /**
     * Get an array of TagGroups with the tags to filter by. The code executed is different
     * depending on whether Solr is being used to do the faceted searching or not. In the case of
     * Solr we detect which tags are selected and add the tag name to the constraint array of the
     * tag group. When not using Solr we add an array of selected tags to the tag group.
     * 
     * @return Returns an array of TagGroups with the tags to filter by
     * @throws KKException
     *             an unexpected KKException exception
     */
    public TagGroupIf[] getTagGroupFilter() throws KKException
    {
        if (kkAppEng.isUseSolr())
        {
            if (currentTagGroups != null)
            {
                for (int i = 0; i < currentTagGroups.length; i++)
                {
                    TagGroupIf tg = currentTagGroups[i];
                    if (tg != null && tg.getTags() != null)
                    {
                        tg.setFacetConstraint(null);
                        tg.setFacetConstraints(null);
                        ArrayList<String> constraintList = new ArrayList<String>();
                        for (int j = 0; j < tg.getTags().length; j++)
                        {
                            TagIf tag = tg.getTags()[j];
                            if (tag != null && tag.isSelected())
                            {
                                constraintList.add(tag.getName());
                            }
                        }
                        if (constraintList.size() > 0)
                        {
                            String[] constraintArray = new String[0];
                            constraintArray = constraintList.toArray(constraintArray);
                            tg.setFacetConstraints(constraintArray);
                        }
                    }
                }
                return currentTagGroups;
            }
        } else
        {
            ArrayList<TagGroupIf> retTagGroupList = new ArrayList<TagGroupIf>();
            if (currentTagGroups != null)
            {
                for (int i = 0; i < currentTagGroups.length; i++)
                {
                    TagGroupIf tg = currentTagGroups[i];
                    if (tg != null && tg.getTags() != null)
                    {
                        ArrayList<TagIf> tagList = new ArrayList<TagIf>();
                        for (int j = 0; j < tg.getTags().length; j++)
                        {
                            TagIf tag = tg.getTags()[j];
                            if (tag != null && tag.isSelected())
                            {
                                tagList.add(tag);
                            }
                        }
                        if (tagList.size() > 0)
                        {
                            TagGroupIf retTagGroup = new TagGroup();
                            retTagGroup.setTags(tagList.toArray(new TagIf[0]));
                            retTagGroupList.add(retTagGroup);
                        }
                    }
                }
            }
            if (retTagGroupList.size() > 0)
            {
                return retTagGroupList.toArray(new TagGroupIf[0]);
            }
        }
        return null;
    }

    /**
     * This method only returns valid data when customer tags are active. It interrogates the
     * <code>PRODUCTS_TIME_VIEWED</code> tag to get the ids of the products recently viewed by the
     * customer. It then retrieves the products and sorts them to show the most recently viewed
     * product first. The <code>viewedProducts</code> attribute of the ProductMgr is set with the
     * array of products. The number of products returned depends on the number of product ids
     * stored by the <code>PRODUCTS_TIME_VIEWED</code> tag.
     * 
     * @param fillDescription
     *            If set, the product description is returned
     * @return Returns an array of recently viewed products
     * @throws KKException
     *             an unexpected KKException exception
     * @throws KKAppException
     *             an unexpected KKAppException exception
     */
    public ProductIf[] fetchRecentlyViewedProductsArray(boolean fillDescription)
            throws KKException, KKAppException
    {
        int[] prodIds = new int[0];
        ProductIf[] prodArray = null;
        String prodsViewed = kkAppEng.getCustomerTagMgr().getCustomerTagValue("PRODUCTS_TIME_VIEWED");
        if (prodsViewed != null && prodsViewed.length() > 0)
        {
            CustomerTag ct = new CustomerTag();
            ct.setValue(prodsViewed);
            ct.setType(CustomerTag.MULTI_INT_PLUS_TIME_TYPE);
            prodIds = ct.getValueAsIntArray();
            if (prodIds.length > 0)
            {
                DataDescriptorIf dataDescr = new DataDescriptor();
                dataDescr.setFillDescription(fillDescription);
                prodArray = eng.getProductsFromIdsWithOptions(kkAppEng.getSessionId(), dataDescr,
                        prodIds, kkAppEng.getLangId(), kkAppEng.getFetchProdOptionsNoImages());
                // Sort the array so last viewed product is displayed first
                HashMap<Integer, ProductIf> hm = new HashMap<Integer, ProductIf>();
                for (int i = 0; i < prodArray.length; i++)
                {
                    ProductIf prod = prodArray[i];
                    hm.put(new Integer(prod.getId()), prod);
                }
                int j = 0;
                for (int i = prodIds.length - 1; i > -1; i--)
                {
                    int prodId = prodIds[i];
                    ProductIf prod = hm.get(new Integer(prodId));
                    if (prod != null)
                    {
                        prodArray[j++] = hm.get(new Integer(prodId));
                    }
                }
            }
        }
        viewedProducts = prodArray;
        return viewedProducts;
    }

    /**
     * Determine whether to allow a customer to checkout if product isn't in stock.
     * 
     * @return Returns true if we should allow checkout. Otherwise returns false.
     */
    public boolean isNoStockAllowCheckout()
    {
        return sd.isNoStockAllowCheckout();
    }

    /**
     * Determine whether to check to see if product is in stock
     * 
     * @return Returns true if we should check stock. Otherwise returns false.
     */
    public boolean isStockCheck()
    {
        return sd.isStockCheck();
    }

    /**
     * Update the number of times that the product has been viewed
     * 
     * @param productId
     *            the productId
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void updateProductViewedCount(int productId) throws KKException
    {
        eng.updateProductViewedCount(productId, kkAppEng.getLangId());
    }

    /**
     * Returns the static list of all manufacturers
     * 
     * @return Returns the static list of all manufacturers
     */
    public DropListElement[] getAllManuDropList()
    {
        return sd.getAllManufacturers();
    }

    /**
     * Returns the currentManufacturers.
     * 
     * @return Returns the currentManufacturers.
     */
    public ManufacturerIf[] getCurrentManufacturers()
    {
        return currentManufacturers;
    }

    /**
     * Sets the currentManufacturers. It is normally set automatically by the application.
     * 
     * @param currentManufacturers
     *            the currentManufacturers to set
     */
    public void setCurrentManufacturers(ManufacturerIf[] currentManufacturers)
    {
        this.currentManufacturers = currentManufacturers;
    }

    /**
     * Returns the length of the currentManufacturers array
     * 
     * @return Returns the length of the currentManufacturers array
     */
    public int getCurrentManufacturersLength()
    {
        return currentManufacturers.length;
    }

    /**
     * Returns the currentProducts.
     * 
     * @return Returns the currentProducts.
     */
    public ProductIf[] getCurrentProducts()
    {
        return currentProducts;
    }

    /**
     * Allows you to set the currentProducts array.
     * 
     * @param currentProducts
     *            the currentProducts to set
     */
    public void setCurrentProducts(ProductIf[] currentProducts)
    {
        this.currentProducts = currentProducts;
    }

    /**
     * Returns the selectedCategory.
     * 
     * @return Returns the selectedCategory.
     */
    public CategoryIf getSelectedCategory()
    {
        return selectedCategory;
    }

    /**
     * Sets the selectedCategory. It is normally set automatically by the application.
     * 
     * @param selectedCategory
     *            the selectedCategory to set
     */
    public void setSelectedCategory(CategoryIf selectedCategory)
    {
        this.selectedCategory = selectedCategory;
    }

    /**
     * Show the back button if set to 1. Don't show the back button if set to 0.
     * 
     * @return Returns the showBack.
     */
    public int getShowBack()
    {
        return showBack;
    }

    /**
     * Show the back button if set to 1. Don't show the back button if set to 0. It is normally set
     * automatically by the application.
     * 
     * @param showBack
     *            the showBack to set
     */
    public void setShowBack(int showBack)
    {
        this.showBack = showBack;
    }

    /**
     * Show the next button if set to 1. Don't show the next button if set to 0.
     * 
     * @return Returns the showNext.
     */
    public int getShowNext()
    {
        return showNext;
    }

    /**
     * Show the next button if set to 1. Don't show the next button if set to 0. It is normally set
     * automatically by the application.
     * 
     * @param showNext
     *            the showNext to set
     */
    public void setShowNext(int showNext)
    {
        this.showNext = showNext;
    }

    /**
     * Maximum number of products to show in a list. The value returned comes from the configuration
     * variable and does not take into account the user preferences.
     * 
     * @return Returns the maxRows.
     */
    public int getMaxRows()
    {
        return sd.getMaxProdRows();
    }

    /**
     * The current offset in the list of products.
     * 
     * @return Returns the currentOffset.
     */
    public int getCurrentOffset()
    {
        return currentOffset;
    }

    /**
     * The current offset in the list of products. It is normally set automatically by the
     * application.
     * 
     * @param currentOffset
     *            the currentOffset to set
     */
    public void setCurrentOffset(int currentOffset)
    {
        this.currentOffset = currentOffset;
    }

    /**
     * Returns the selectedProduct.
     * 
     * @return Returns the selectedProduct.
     */
    public ProductIf getSelectedProduct()
    {
        return selectedProduct;
    }

    /**
     * Sets the selectedProduct. It is normally set automatically by the application.
     * 
     * @param selectedProduct
     *            the selectedProduct to set
     */
    public void setSelectedProduct(ProductIf selectedProduct)
    {
        this.selectedProduct = selectedProduct;
    }

    /**
     * Returns the selectedProductOptions.
     * 
     * @return Returns the selectedProductOptions.
     */
    public List<ProdOptionContainer> getSelectedProductOptions()
    {
        return selectedProductOptions;
    }

    /**
     * Sets the selectedProductOptions. It is normally set automatically by the application.
     * 
     * @param selectedProductOptions
     *            the selectedProductOptions to set
     */
    public void setSelectedProductOptions(List<ProdOptionContainer> selectedProductOptions)
    {
        this.selectedProductOptions = selectedProductOptions;
    }

    /**
     * Returns the currentCategories.
     * 
     * @return Returns the currentCategories.
     */
    public CategoryIf[] getCurrentCategories()
    {
        return currentCategories;
    }

    /**
     * Sets the currentCategories. It is normally set automatically by the application.
     * 
     * @param currentCategories
     *            the currentCategories to set
     */
    public void setCurrentCategories(CategoryIf[] currentCategories)
    {
        this.currentCategories = currentCategories;
    }

    /**
     * Returns the length of the currentCategories array
     * 
     * @return Returns the length of the currentCategories array
     */
    public int getCurrentCategoriesLength()
    {
        return currentCategories.length;
    }

    /**
     * Returns the selectedManufacturer.
     * 
     * @return Returns the selectedManufacturer.
     */
    public ManufacturerIf getSelectedManufacturer()
    {
        return selectedManufacturer;
    }

    /**
     * Sets the selectedManufacturer. It is normally set automatically by the application.
     * 
     * @param selectedManufacturer
     *            the selectedManufacturer to set
     */
    public void setSelectedManufacturer(ManufacturerIf selectedManufacturer)
    {
        this.selectedManufacturer = selectedManufacturer;
    }

    /**
     * Returns the totalNumberOfProducts which was set as a result of a search.
     * 
     * @return Returns the totalNumberOfProducts.
     */
    public int getTotalNumberOfProducts()
    {
        return totalNumberOfProducts;
    }

    /**
     * The totalNumberOfProducts is normally set automatically as a result of a search.
     * 
     * @param totalNumberOfProducts
     *            the totalNumberOfProducts to set
     */
    public void setTotalNumberOfProducts(int totalNumberOfProducts)
    {
        this.totalNumberOfProducts = totalNumberOfProducts;
    }

    /**
     * Returns the alsoPurchased array of products.
     * 
     * @return Returns the alsoPurchased array of products.
     */
    public ProductIf[] getAlsoPurchased()
    {
        return alsoPurchased;
    }

    /**
     * Sets the alsoPurchased array of products. It is normally set automatically by the
     * application.
     * 
     * @param alsoPurchased
     *            the alsoPurchased to set
     */
    public void setAlsoPurchased(ProductIf[] alsoPurchased)
    {
        this.alsoPurchased = alsoPurchased;
    }

    /**
     * Returns an array of products containing the new products.
     * 
     * @return Returns an array of products containing the new products.
     */
    public ProductIf[] getNewProducts()
    {
        return newProducts;
    }

    /**
     * Allows you to set the newProducts array
     * 
     * @param newProducts
     *            the newProducts to set
     */
    public void setNewProducts(ProductIf[] newProducts)
    {
        this.newProducts = newProducts;
    }

    /**
     * Returns an array containing all of the manufacturers.
     * 
     * @return Returns an array containing all of the manufacturers.
     */
    public ManufacturerIf[] getAllManuArray()
    {
        return sd.getAllManuArray();
    }

    /**
     * Returns the accessories.
     * 
     * @return Returns the accessories.
     */
    public ProductIf[] getAccessories()
    {
        return accessories;
    }

    /**
     * Sets the accessories array. It is normally set automatically by the application.
     * 
     * @param accessories
     *            the accessories to set
     */
    public void setAccessories(ProductIf[] accessories)
    {
        this.accessories = accessories;
    }

    /**
     * Returns the crossSellProducts.
     * 
     * @return Returns the crossSellProducts.
     */
    public ProductIf[] getCrossSellProducts()
    {
        return crossSellProducts;
    }

    /**
     * Sets the crossSellProducts. It is normally set automatically by the application.
     * 
     * @param crossSellProducts
     *            the crossSellProducts to set
     */
    public void setCrossSellProducts(ProductIf[] crossSellProducts)
    {
        this.crossSellProducts = crossSellProducts;
    }

    /**
     * Returns the dependentProducts.
     * 
     * @return Returns the dependentProducts.
     */
    public ProductIf[] getDependentProducts()
    {
        return dependentProducts;
    }

    /**
     * Sets the dependentProducts array. It is normally set automatically by the application.
     * 
     * @param dependentProducts
     *            the dependentProducts to set
     */
    public void setDependentProducts(ProductIf[] dependentProducts)
    {
        this.dependentProducts = dependentProducts;
    }

    /**
     * Returns the upSellProducts.
     * 
     * @return Returns the upSellProducts.
     */
    public ProductIf[] getUpSellProducts()
    {
        return upSellProducts;
    }

    /**
     * Sets the upSellProducts. It is normally set automatically by the application.
     * 
     * @param upSellProducts
     *            the upSellProducts to set
     */
    public void setUpSellProducts(ProductIf[] upSellProducts)
    {
        this.upSellProducts = upSellProducts;
    }

    /**
     * Returns the digitalDownloads.
     * 
     * @return Returns the digitalDownloads.
     */
    public DigitalDownloadIf[] getDigitalDownloads()
    {
        return digitalDownloads;
    }

    /**
     * @param digitalDownloads
     *            The digitalDownloads to set.
     */
    public void setDigitalDownloads(DigitalDownloadIf[] digitalDownloads)
    {
        this.digitalDownloads = digitalDownloads;
    }

    /**
     * Returns the current data descriptor object.
     * 
     * @return Returns the dataDesc.
     */
    public DataDescriptorIf getDataDesc()
    {
        return dataDesc;
    }

    /**
     * Sets the current data descriptor object.
     * 
     * @param dataDesc
     *            The dataDesc to set.
     */
    public void setDataDesc(DataDescriptorIf dataDesc)
    {
        this.dataDesc = dataDesc;
    }

    /**
     * Used to set the default OrderBy used by many of the API calls to fetch products. The value
     * must be an attribute of com.konakart.app.DataDescConstants such as ORDER_BY_NAME_ASCENDING,
     * ORDER_BY_MANUFACTURER etc. It is used by:
     * <ul>
     * <li>fetchProductsPerCategory</li>
     * <li>fetchAllSpecials</li>
     * <li>searchForProducts</li>
     * <li>fetchProductsPerManufacturer</li>
     * <li>fetchAlsoPurchasedArray</li>
     * <li>fetchRelatedProducts</li>
     * </ul>
     * 
     * The default value is DataDescConstants.ORDER_BY_PRICE_ASCENDING.
     * 
     * @return Returns the defaultOrderBy.
     */
    public String getDefaultOrderBy()
    {
        return defaultOrderBy;
    }

    /**
     * Used to set the default OrderBy used by many of the API calls to fetch products. The value
     * must be an attribute of com.konakart.app.DataDescConstants such as ORDER_BY_NAME_ASCENDING,
     * ORDER_BY_MANUFACTURER etc. It is used by:
     * <ul>
     * <li>fetchProductsPerCategory</li>
     * <li>fetchAllSpecials</li>
     * <li>searchForProducts</li>
     * <li>fetchProductsPerManufacturer</li>
     * <li>fetchAlsoPurchasedArray</li>
     * <li>fetchRelatedProducts</li>
     * </ul>
     * 
     * The default value is DataDescConstants.ORDER_BY_PRICE_ASCENDING.
     * 
     * @param defaultOrderBy
     *            The defaultOrderBy to set.
     */
    public void setDefaultOrderBy(String defaultOrderBy)
    {
        this.defaultOrderBy = defaultOrderBy;
    }

    /**
     * Returns the bundledProducts.
     * 
     * @return Returns the bundledProducts.
     */
    public ProductIf[] getBundledProducts()
    {
        return bundledProducts;
    }

    /**
     * Sets the bundled products array. It is normally set automatically by the application.
     * 
     * @param bundledProducts
     *            the bundledProducts to set
     */
    public void setBundledProducts(ProductIf[] bundledProducts)
    {
        this.bundledProducts = bundledProducts;
    }

    /**
     * If there are any tag groups associated with the current category, the tags will be displayed
     * on the UI to allow the customer to refine his product search.
     * 
     * @return Returns the currentTagGroups.
     */
    public TagGroupIf[] getCurrentTagGroups()
    {
        return currentTagGroups;
    }

    /**
     * If there are any tag groups associated with the current category, the tags will be displayed
     * on the UI to allow the customer to refine his product search.
     * 
     * It is normally set automatically by the application.
     * 
     * @param currentTagGroups
     *            the currentTagGroups to set
     */
    public void setCurrentTagGroups(TagGroupIf[] currentTagGroups)
    {
        this.currentTagGroups = currentTagGroups;
    }

    /**
     * Returns the current ProductSearch object.
     * 
     * @return the prodSearch
     */
    public ProductSearchIf getProdSearch()
    {
        return prodSearch;
    }

    /**
     * Sets the current ProductSearch object.
     * 
     * @param prodSearch
     *            the prodSearch to set
     */
    public void setProdSearch(ProductSearchIf prodSearch)
    {
        this.prodSearch = prodSearch;
    }

    /**
     * @return the customProducts1
     */
    public ProductIf[] getCustomProducts1()
    {
        return customProducts1;
    }

    /**
     * @param customProducts1
     *            the customProducts1 to set
     */
    public void setCustomProducts1(ProductIf[] customProducts1)
    {
        this.customProducts1 = customProducts1;
    }

    /**
     * @return the customProducts2
     */
    public ProductIf[] getCustomProducts2()
    {
        return customProducts2;
    }

    /**
     * @param customProducts2
     *            the customProducts2 to set
     */
    public void setCustomProducts2(ProductIf[] customProducts2)
    {
        this.customProducts2 = customProducts2;
    }

    /**
     * @return the customProducts3
     */
    public ProductIf[] getCustomProducts3()
    {
        return customProducts3;
    }

    /**
     * @param customProducts3
     *            the customProducts3 to set
     */
    public void setCustomProducts3(ProductIf[] customProducts3)
    {
        this.customProducts3 = customProducts3;
    }

    /**
     * Used to set a user defined maximum number of search results displayed. When set to a number
     * greater than zero, this value is used instead of the value in the configuration variable
     * MAX_DISPLAY_SEARCH_RESULTS.
     * 
     * @param num
     *            the num
     */
    public void setMaxDisplaySearchResults(int num)
    {
        if (getMaxDisplaySearchResults() != num)
        {
            currentOffset = 0;
        }
        maxProdRowsUser = num;
    }

    /**
     * Used to get the maximum number of search results to display. The value is customizable by the
     * customer.
     * 
     * @return Returns the maximum number of search results to display
     */
    public int getMaxDisplaySearchResults()
    {
        if (maxProdRowsUser > 0)
        {
            return maxProdRowsUser;
        }
        return sd.getMaxProdRows();
    }

    /**
     * A timestamp for when the last product search was done. Used to detect whether a result set is
     * old when paging.
     * 
     * @return the prodTimestamp
     */
    public long getProdTimestamp()
    {
        return prodTimestamp;
    }

    /**
     * A timestamp for when the last product search was done. Used to detect whether a result set is
     * old when paging.
     * 
     * @param prodTimestamp
     *            the prodTimestamp to set
     */
    public void setProdTimestamp(long prodTimestamp)
    {
        this.prodTimestamp = prodTimestamp;
    }

    /**
     * @return the pageList
     */
    public ArrayList<Integer> getPageList()
    {
        return pageList;
    }

    /**
     * @param pageList
     *            the pageList to set
     */
    public void setPageList(ArrayList<Integer> pageList)
    {
        this.pageList = pageList;
    }

    /**
     * @return the currentPage
     */
    public int getCurrentPage()
    {
        return currentPage;
    }

    /**
     * @param currentPage
     *            the currentPage to set
     */
    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }

    /**
     * Number of pages within a result set
     * 
     * @return the numPages
     */
    public int getNumPages()
    {
        return numPages;
    }

    /**
     * Number of pages within a result set
     * 
     * @param numPages
     *            the numPages to set
     */
    public void setNumPages(int numPages)
    {
        this.numPages = numPages;
    }

    /**
     * Returns an array of active promotions from the cache
     * 
     * @return Returns an array of active promotions from the cache
     */
    public PromotionIf[] getAllPromotions()
    {
        return sd.getActivePromotions();
    }

    /**
     * Returns a Hash Map containing the active promotions
     * 
     * @return Returns a Hash Map containing the active promotions
     */
    public Map<Integer, PromotionIf> getPromotionMap()
    {
        return sd.getActivePromotionMap();
    }

    /**
     * Refresh the configuration variables. This is called automatically at a regular interval.
     * 
     * @throws KKException
     *             an unexpected KKException exception
     */
    public void refreshConfigs() throws KKException
    {
        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_SEARCH_RESULTS) != null)
        {
            sd.setMaxProdRows(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_SEARCH_RESULTS))
                            .intValue());
        } else
        {
            sd.setMaxProdRows(7);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_PAGE_LINKS) != null)
        {
            sd.setMaxPageLinks(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_PAGE_LINKS))
                            .intValue());
        } else
        {
            sd.setMaxPageLinks(5);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_ALSO_PURCHASED) != null)
        {
            sd.setMaxDispAlsoPurchased(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_ALSO_PURCHASED))
                            .intValue());
        } else
        {
            sd.setMaxDispAlsoPurchased(6);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_UP_SELL) != null)
        {
            sd.setMaxDispUpSellProds(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_UP_SELL))
                            .intValue());
        } else
        {
            sd.setMaxDispUpSellProds(6);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_CROSS_SELL) != null)
        {
            sd.setMaxDispCrossSellProds(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_CROSS_SELL))
                            .intValue());
        } else
        {
            sd.setMaxDispCrossSellProds(6);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_ACCESSORIES) != null)
        {
            sd.setMaxDispAccessories(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_ACCESSORIES))
                            .intValue());
        } else
        {
            sd.setMaxDispAccessories(6);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_DEPENDENT_PRODUCTS) != null)
        {
            sd.setMaxDispDependentProds(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_DEPENDENT_PRODUCTS))
                            .intValue());
        } else
        {
            sd.setMaxDispDependentProds(6);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_NEW_PRODUCTS) != null)
        {
            sd.setMaxDispNewProds(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_NEW_PRODUCTS))
                            .intValue());
        } else
        {
            sd.setMaxDispNewProds(9);
        }

        if (kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_SPECIALS) != null)
        {
            sd.setMaxDispSpecials(
                    new Integer(kkAppEng.getConfig(ConfigConstants.MAX_DISPLAY_SPECIALS))
                            .intValue());
        } else
        {
            sd.setMaxDispSpecials(9);
        }

        String noStockAllowCheckoutStr = kkAppEng.getConfig(ConfigConstants.STOCK_ALLOW_CHECKOUT);
        if (noStockAllowCheckoutStr != null && noStockAllowCheckoutStr.equalsIgnoreCase("true"))
        {
            sd.setNoStockAllowCheckout(true);
        } else
        {
            sd.setNoStockAllowCheckout(false);
        }

        String stockCheckStr = kkAppEng.getConfig(ConfigConstants.STOCK_CHECK);
        if (stockCheckStr != null && stockCheckStr.equalsIgnoreCase("true"))
        {
            sd.setStockCheck(true);
        } else
        {
            sd.setStockCheck(false);
        }

        String setUseSolrDynamicFacetsStr = kkAppEng.getConfig(ConfigConstants.SOLR_DYNAMIC_FACETS);
        if (setUseSolrDynamicFacetsStr != null
                && setUseSolrDynamicFacetsStr.equalsIgnoreCase("true"))
        {
            sd.setUseSolrDynamicFacets(true);
        } else
        {
            sd.setUseSolrDynamicFacets(false);
        }

        // Refresh cached lists
        refreshCaches();
    }

    /**
     * @return the recently viewedProducts
     */
    public ProductIf[] getViewedProducts()
    {
        return viewedProducts;
    }

    /**
     * @param viewedProducts
     *            the recently viewedProducts to set
     */
    public void setViewedProducts(ProductIf[] viewedProducts)
    {
        this.viewedProducts = viewedProducts;
    }

    /**
     * @return the specials
     */
    public ProductIf[] getSpecials()
    {
        return specials;
    }

    /**
     * @param specials
     *            the specials to set
     */
    public void setSpecials(ProductIf[] specials)
    {
        this.specials = specials;
    }

    /**
     * @return the expiredResultSet
     */
    public boolean isExpiredResultSet()
    {
        return expiredResultSet;
    }

    /**
     * @param expiredResultSet
     *            the expiredResultSet to set
     */
    public void setExpiredResultSet(boolean expiredResultSet)
    {
        this.expiredResultSet = expiredResultSet;
    }

    /**
     * @return the numSelectedFilters
     */
    public int getNumSelectedFilters()
    {
        return numSelectedFilters;
    }

    /**
     * @param numSelectedFilters
     *            the numSelectedFilters to set
     */
    public void setNumSelectedFilters(int numSelectedFilters)
    {
        this.numSelectedFilters = numSelectedFilters;
    }

    /**
     * @return the maxPrice
     */
    public BigDecimal getMaxPrice()
    {
        return maxPrice;
    }

    /**
     * @param maxPrice
     *            the maxPrice to set
     */
    public void setMaxPrice(BigDecimal maxPrice)
    {
        this.maxPrice = maxPrice;
    }

    /**
     * @return the minPrice
     */
    public BigDecimal getMinPrice()
    {
        return minPrice;
    }

    /**
     * @param minPrice
     *            the minPrice to set
     */
    public void setMinPrice(BigDecimal minPrice)
    {
        this.minPrice = minPrice;
    }

    /**
     * @return the taxMultiplier
     */
    public BigDecimal getTaxMultiplier()
    {
        return taxMultiplier;
    }

    /**
     * @param taxMultiplier
     *            the taxMultiplier to set
     */
    public void setTaxMultiplier(BigDecimal taxMultiplier)
    {
        this.taxMultiplier = taxMultiplier;
    }

    /**
     * @return the priceFilter
     */
    public boolean isPriceFilter()
    {
        return priceFilter;
    }

    /**
     * @param priceFilter
     *            the priceFilter to set
     */
    public void setPriceFilter(boolean priceFilter)
    {
        this.priceFilter = priceFilter;
    }

    /**
     * @return the priceFacets
     */
    public KKPriceFacetIf[] getPriceFacets()
    {
        return priceFacets;
    }

    /**
     * @param priceFacets
     *            the priceFacets to set
     */
    public void setPriceFacets(KKPriceFacetIf[] priceFacets)
    {
        this.priceFacets = priceFacets;
    }

    /**
     * @return the allRelatedProducts
     */
    public ProductIf[] getAllRelatedProducts()
    {
        return allRelatedProducts;
    }

    /**
     * @param allRelatedProducts
     *            the allRelatedProducts to set
     */
    public void setAllRelatedProducts(ProductIf[] allRelatedProducts)
    {
        this.allRelatedProducts = allRelatedProducts;
    }

    /**
     * Generates a timestamp and saves a clone of the DataDescriptor and ProductSearch
     */
    private void generateTimestamp()
    {
        prodTimestamp = System.currentTimeMillis();
        KKBeanCopier copier = new KKBeanCopier();
        TagGroupIf[] clonedTagGroups = null;
        if (currentTagGroups != null && currentTagGroups.length > 0)
        {
            clonedTagGroups = new TagGroupIf[currentTagGroups.length];
            for (int i = 0; i < currentTagGroups.length; i++)
            {
                TagGroupIf tg = currentTagGroups[i];
                clonedTagGroups[i] = copier.cloneTagGroup(tg);
            }
        }

        ManufacturerIf[] clonedManufacturers = emptyManuArray;
        if (currentManufacturers != null && currentManufacturers.length > 0)
        {
            clonedManufacturers = new ManufacturerIf[currentManufacturers.length];
            for (int i = 0; i < currentManufacturers.length; i++)
            {
                ManufacturerIf manu = currentManufacturers[i];
                clonedManufacturers[i] = copier.cloneManufacturer(manu);
            }
        }

        SearchData sd = new SearchData(copier.cloneProductSearch(this.prodSearch),
                copier.cloneDataDescriptor(this.dataDesc), clonedTagGroups, clonedManufacturers,
                currentOffset, currentPage);
        if (kkAppEng.isUseSolr())
        {
            KKPriceFacetIf[] priceFacetsClone = null;
            if (priceFacets != null)
            {
                priceFacetsClone = new KKPriceFacetIf[priceFacets.length];
                for (int i = 0; i < priceFacets.length; i++)
                {
                    KKPriceFacetIf pf = priceFacets[i];
                    priceFacetsClone[i] = copier.cloneKKPriceFacet(pf);
                }
            }
            sd.setSolrPriceData(minPrice, maxPrice, taxMultiplier, priceFacetsClone);
        }
        searchDataMap.put(new Long(prodTimestamp), sd);
    }

    /**
     * Called from the JSP to the save the current navigation
     */
    public void saveNavigationForTimestamp()
    {
        SearchData sd = searchDataMap.get(prodTimestamp);
        if (sd != null && sd.getNav() == null)
        {
            CurrentNavigation clonedNavigation = null;
            CurrentNavigation currentNavigation = kkAppEng.getNav();
            if (currentNavigation != null)
            {
                clonedNavigation = currentNavigation.clone();
            }
            sd.setNav(clonedNavigation);
        }
    }

    /**
     * An array of SuggestedSpellingItems is returned for search text. This method is useful for
     * prompting a customer with a list of alternative spellings contained within the catalog if a
     * search returns no results.
     * 
     * @param searchText
     *            the searchText
     * @return Returns an array of suggested spelling items
     * @throws KKException
     *             an unexpected KKException exception
     */
    public SuggestedSpellingItemIf[] getSuggestedSpellingItems(String searchText) throws KKException
    {
        SuggestedSpellingOptionsIf options = new SuggestedSpellingOptions();
        options.setLanguageCode(kkAppEng.getLocale().substring(0, 2));
        options.setSearchText(searchText);
        options.setCatalogId(kkAppEng.getFetchProdOptions().getCatalogId());
        SuggestedSpellingItemIf[] items = eng.getSuggestedSpellingItems(kkAppEng.getSessionId(),
                options);
        this.setSuggestedSpellingSearch(searchText);
        this.setSuggestedSpellings(items);

        return items;
    }

    /**
     * @return the suggestedSpellings
     */
    public SuggestedSpellingItemIf[] getSuggestedSpellings()
    {
        return suggestedSpellings;
    }

    /**
     * @param suggestedSpellings
     *            the suggestedSpellings to set
     */
    public void setSuggestedSpellings(SuggestedSpellingItemIf[] suggestedSpellings)
    {
        this.suggestedSpellings = suggestedSpellings;
    }

    /**
     * @return the suggestedSpellingSearch
     */
    public String getSuggestedSpellingSearch()
    {
        return suggestedSpellingSearch;
    }

    /**
     * @param suggestedSpellingSearch
     *            the suggestedSpellingSearch to set
     */
    public void setSuggestedSpellingSearch(String suggestedSpellingSearch)
    {
        this.suggestedSpellingSearch = suggestedSpellingSearch;
    }

    /**
     * Used to store search data for previous searches
     * 
     */
    private class SearchData
    {

        private ProductSearchIf ps;

        private DataDescriptorIf dd;

        private TagGroupIf[] tagGroups;

        private ManufacturerIf[] currentManufacturers;

        private CurrentNavigation nav;

        private BigDecimal maxPrice;

        private BigDecimal minPrice;

        private BigDecimal taxMultiplier;

        private KKPriceFacetIf[] priceFacets;

        private int currentOffset;

        private int currentPage;

        /**
         * Constructor
         * 
         * @param _ps
         * @param _dd
         * @param _tagGroups
         * @param _currentManufacturers
         * @param _currentOffset
         * @param _currentPage
         */
        public SearchData(ProductSearchIf _ps, DataDescriptorIf _dd, TagGroupIf[] _tagGroups,
                ManufacturerIf[] _currentManufacturers, int _currentOffset, int _currentPage)
        {
            this.ps = _ps;
            this.dd = _dd;
            this.tagGroups = _tagGroups;
            this.currentManufacturers = _currentManufacturers;
            this.currentOffset = _currentOffset;
            this.currentPage = _currentPage;
        }

        public void setSolrPriceData(BigDecimal _minPrice, BigDecimal _maxPrice,
                BigDecimal _taxMultiplier, KKPriceFacetIf[] _priceFacets)
        {
            this.minPrice = _minPrice;
            this.maxPrice = _maxPrice;
            this.taxMultiplier = _taxMultiplier;
            this.priceFacets = _priceFacets;
        }

        /**
         * @return the ps
         */
        public ProductSearchIf getPs()
        {
            return ps;
        }

        /**
         * @return the dd
         */
        public DataDescriptorIf getDd()
        {
            return dd;
        }

        /**
         * @return the tagGroups
         */
        public TagGroupIf[] getTagGroups()
        {
            return tagGroups;
        }

        /**
         * @return the currentManufacturers
         */
        public ManufacturerIf[] getCurrentManufacturers()
        {
            return currentManufacturers;
        }

        /**
         * @return the nav
         */
        public CurrentNavigation getNav()
        {
            return nav;
        }

        /**
         * @param nav
         *            the nav to set
         */
        public void setNav(CurrentNavigation nav)
        {
            this.nav = nav;
        }

        /**
         * @return the maxPrice
         */
        public BigDecimal getMaxPrice()
        {
            return maxPrice;
        }

        /**
         * @param maxPrice
         *            the maxPrice to set
         */
        @SuppressWarnings("unused")
        public void setMaxPrice(BigDecimal maxPrice)
        {
            this.maxPrice = maxPrice;
        }

        /**
         * @return the minPrice
         */
        public BigDecimal getMinPrice()
        {
            return minPrice;
        }

        /**
         * @param minPrice
         *            the minPrice to set
         */
        @SuppressWarnings("unused")
        public void setMinPrice(BigDecimal minPrice)
        {
            this.minPrice = minPrice;
        }

        /**
         * @return the taxMultiplier
         */
        public BigDecimal getTaxMultiplier()
        {
            return taxMultiplier;
        }

        /**
         * @param taxMultiplier
         *            the taxMultiplier to set
         */
        @SuppressWarnings("unused")
        public void setTaxMultiplier(BigDecimal taxMultiplier)
        {
            this.taxMultiplier = taxMultiplier;
        }

        /**
         * @return the priceFacets
         */
        public KKPriceFacetIf[] getPriceFacets()
        {
            return priceFacets;
        }

        /**
         * @param priceFacets
         *            the priceFacets to set
         */
        @SuppressWarnings("unused")
        public void setPriceFacets(KKPriceFacetIf[] priceFacets)
        {
            this.priceFacets = priceFacets;
        }

        /**
         * @param ps
         *            the ps to set
         */
        @SuppressWarnings("unused")
        public void setPs(ProductSearchIf ps)
        {
            this.ps = ps;
        }

        /**
         * @param dd
         *            the dd to set
         */
        @SuppressWarnings("unused")
        public void setDd(DataDescriptorIf dd)
        {
            this.dd = dd;
        }

        /**
         * @param tagGroups
         *            the tagGroups to set
         */
        @SuppressWarnings("unused")
        public void setTagGroups(TagGroupIf[] tagGroups)
        {
            this.tagGroups = tagGroups;
        }

        /**
         * @param currentManufacturers
         *            the currentManufacturers to set
         */
        @SuppressWarnings("unused")
        public void setCurrentManufacturers(ManufacturerIf[] currentManufacturers)
        {
            this.currentManufacturers = currentManufacturers;
        }

        /**
         * @return the currentOffset
         */
        public int getCurrentOffset()
        {
            return currentOffset;
        }

        /**
         * @param currentOffset
         *            the currentOffset to set
         */
        @SuppressWarnings("unused")
        public void setCurrentOffset(int currentOffset)
        {
            this.currentOffset = currentOffset;
        }

        /**
         * @return the currentPage
         */
        public int getCurrentPage()
        {
            return currentPage;
        }

        /**
         * @param currentPage
         *            the currentPage to set
         */
        @SuppressWarnings("unused")
        public void setCurrentPage(int currentPage)
        {
            this.currentPage = currentPage;
        }
    }

    /**
     * Used to store the static data of this manager
     */
    private class StaticData
    {
        // is the manager ready?
        boolean mgrReady = false;

        int maxProdRows = 7;

        int maxPageLinks = 5;;

        int maxDispAlsoPurchased = 6;

        int maxDispNewProds = 9;

        int maxDispSpecials = 9;

        int maxDispUpSellProds = 6;

        int maxDispCrossSellProds = 6;

        int maxDispAccessories = 6;

        int maxDispDependentProds = 6;

        boolean noStockAllowCheckout = false;

        boolean stockCheck = false;

        boolean useSolrDynamicFacets = false;

        // Used to select a manufacturer from a drop list
        DropListElement[] allManufacturers = null;

        ManufacturerIf[] allManuArray;

        // Hash map to contain active promotions that can be referenced by id
        Map<Integer, PromotionIf> activePromotionMap = Collections
                .synchronizedMap(new HashMap<Integer, PromotionIf>());

        PromotionIf[] activePromotions;

        /**
         * @return Returns the maxProdRows.
         */
        public int getMaxProdRows()
        {
            if (maxProdRowsUser > 0)
            {
                return maxProdRowsUser;
            }
            return maxProdRows;
        }

        /**
         * @param maxProdRows
         *            The maxProdRows to set.
         */
        public void setMaxProdRows(int maxProdRows)
        {
            this.maxProdRows = maxProdRows;
        }

        /**
         * @return Returns the maxDispAlsoPurchased.
         */
        public int getMaxDispAlsoPurchased()
        {
            return maxDispAlsoPurchased;
        }

        /**
         * @param maxDispAlsoPurchased
         *            The maxDispAlsoPurchased to set.
         */
        public void setMaxDispAlsoPurchased(int maxDispAlsoPurchased)
        {
            this.maxDispAlsoPurchased = maxDispAlsoPurchased;
        }

        /**
         * @return Returns the maxDispNewProds.
         */
        public int getMaxDispNewProds()
        {
            return maxDispNewProds;
        }

        /**
         * @param maxDispNewProds
         *            The maxDispNewProds to set.
         */
        public void setMaxDispNewProds(int maxDispNewProds)
        {
            this.maxDispNewProds = maxDispNewProds;
        }

        /**
         * @return Returns the maxDispUpSellProds.
         */
        public int getMaxDispUpSellProds()
        {
            return maxDispUpSellProds;
        }

        /**
         * @param maxDispUpSellProds
         *            The maxDispUpSellProds to set.
         */
        public void setMaxDispUpSellProds(int maxDispUpSellProds)
        {
            this.maxDispUpSellProds = maxDispUpSellProds;
        }

        /**
         * @return Returns the maxDispCrossSellProds.
         */
        public int getMaxDispCrossSellProds()
        {
            return maxDispCrossSellProds;
        }

        /**
         * @param maxDispCrossSellProds
         *            The maxDispCrossSellProds to set.
         */
        public void setMaxDispCrossSellProds(int maxDispCrossSellProds)
        {
            this.maxDispCrossSellProds = maxDispCrossSellProds;
        }

        /**
         * @return Returns the maxDispAccessories.
         */
        public int getMaxDispAccessories()
        {
            return maxDispAccessories;
        }

        /**
         * @param maxDispAccessories
         *            The maxDispAccessories to set.
         */
        public void setMaxDispAccessories(int maxDispAccessories)
        {
            this.maxDispAccessories = maxDispAccessories;
        }

        /**
         * @return Returns the maxDispDependentProds.
         */
        public int getMaxDispDependentProds()
        {
            return maxDispDependentProds;
        }

        /**
         * @param maxDispDependentProds
         *            The maxDispDependentProds to set.
         */
        public void setMaxDispDependentProds(int maxDispDependentProds)
        {
            this.maxDispDependentProds = maxDispDependentProds;
        }

        /**
         * @return Returns the allManufacturers.
         */
        public DropListElement[] getAllManufacturers()
        {
            return allManufacturers;
        }

        /**
         * @param allManufacturers
         *            The allManufacturers to set.
         */
        public void setAllManufacturers(DropListElement[] allManufacturers)
        {
            this.allManufacturers = allManufacturers;
        }

        /**
         * @return Returns the allManuArray.
         */
        public ManufacturerIf[] getAllManuArray()
        {
            return allManuArray;
        }

        /**
         * @param allManuArray
         *            The allManuArray to set.
         */
        public void setAllManuArray(ManufacturerIf[] allManuArray)
        {
            this.allManuArray = allManuArray;
        }

        /**
         * @return the mgrReady
         */
        public boolean isMgrReady()
        {
            return mgrReady;
        }

        /**
         * @param mgrReady
         *            the mgrReady to set
         */
        public void setMgrReady(boolean mgrReady)
        {
            this.mgrReady = mgrReady;
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

        /**
         * @return the activePromotions
         */
        public PromotionIf[] getActivePromotions()
        {
            return activePromotions;
        }

        /**
         * @param activePromotions
         *            the activePromotions to set
         */
        public void setActivePromotions(PromotionIf[] activePromotions)
        {
            this.activePromotions = activePromotions;
        }

        /**
         * @return the activePromotionMap
         */
        public Map<Integer, PromotionIf> getActivePromotionMap()
        {
            return activePromotionMap;
        }

        /**
         * @param activePromotionMap
         *            the activePromotionMap to set
         */
        @SuppressWarnings("unused")
        public void setActivePromotionMap(Map<Integer, PromotionIf> activePromotionMap)
        {
            this.activePromotionMap = activePromotionMap;
        }

        /**
         * @return the noStockAllowCheckout
         */
        public boolean isNoStockAllowCheckout()
        {
            return noStockAllowCheckout;
        }

        /**
         * @param noStockAllowCheckout
         *            the noStockAllowCheckout to set
         */
        public void setNoStockAllowCheckout(boolean noStockAllowCheckout)
        {
            this.noStockAllowCheckout = noStockAllowCheckout;
        }

        /**
         * @return the stockCheck
         */
        public boolean isStockCheck()
        {
            return stockCheck;
        }

        /**
         * @param stockCheck
         *            the stockCheck to set
         */
        public void setStockCheck(boolean stockCheck)
        {
            this.stockCheck = stockCheck;
        }

        /**
         * @return the maxDispSpecials
         */
        public int getMaxDispSpecials()
        {
            return maxDispSpecials;
        }

        /**
         * @param maxDispSpecials
         *            the maxDispSpecials to set
         */
        public void setMaxDispSpecials(int maxDispSpecials)
        {
            this.maxDispSpecials = maxDispSpecials;
        }

        /**
         * @return the useSolrDynamicFacets
         */
        public boolean isUseSolrDynamicFacets()
        {
            return useSolrDynamicFacets;
        }

        /**
         * @param useSolrDynamicFacets
         *            the useSolrDynamicFacets to set
         */
        public void setUseSolrDynamicFacets(boolean useSolrDynamicFacets)
        {
            this.useSolrDynamicFacets = useSolrDynamicFacets;
        }
    }

    /**
     * @return the selectedProductCategory
     */
    public CategoryIf getSelectedProductCategory()
    {
        return selectedProductCategory;
    }

    /**
     * @param selectedProductCategory
     *            the selectedProductCategory to set
     */
    public void setSelectedProductCategory(CategoryIf selectedProductCategory)
    {
        this.selectedProductCategory = selectedProductCategory;
    }
}
