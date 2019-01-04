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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.konakart.app.Category;
import com.konakart.app.CategoryTreeOptions;
import com.konakart.app.KKException;
import com.konakart.app.ProductSearch;
import com.konakart.appif.CategoryIf;
import com.konakart.appif.CategoryTreeOptionsIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.LanguageIf;
import com.konakart.appif.ProductsIf;
import com.konakart.bl.ConfigConstants;

/**
 * Contains methods to get the category tree and manage the current category.
 */
public class CategoryMgr extends BaseMgr
{
    /**
     * The <code>Log</code> instance for this application.
     */
    private Log log = LogFactory.getLog(CategoryMgr.class);

    // Category Information
    private CategoryIf currentCat;

    /** The object containing all of the static data for this instance of the Category Manager */
    private StaticData sd = null;

    /** Hash Map that contains the static data */
    private static Map<String, StaticData> staticDataHM = Collections
            .synchronizedMap(new HashMap<String, StaticData>());

    /**
     * A local rather than static category map when a customer is using a catalog id which may
     * modify the number of products per category
     */
    private HashMap<Integer, CategoryIf> privateCatMap = null;

    /**
     * A local rather than static category map when a customer is using a catalog id which may
     * modify the number of products per category
     */
    private CategoryIf[] privateCatTree = null;

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected CategoryMgr(KKEngIf eng, KKAppEng kkAppEng) throws KKException, KKAppException
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
     * @throws KKAppException
     *            an unexpected KKAppException exception
     * 
     */
    public void refreshConfigs() throws KKException, KKAppException
    {
        if (log.isDebugEnabled())
        {
            log.debug("refreshConfigs() for CategoryMgr");
        }

        sd.setShowCounts(false);
        String showCountsStr = kkAppEng.getConfig(ConfigConstants.SHOW_COUNTS);
        if (showCountsStr != null && showCountsStr.equalsIgnoreCase("true"))
        {
            sd.setShowCounts(true);
        }

        refreshCaches();
    }

    /**
     * Refreshes the all categories list. It forces a fetch of a new category tree for each language
     * from the server.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public void refreshCaches() throws KKException, KKAppException
    {
        if (log.isDebugEnabled())
        {
            log.debug("refreshCaches() for CategoryMgr");
        }

        // Get a new cat tree from the server for each language
        fetchCategoryTree();

        // Now the manager is ready for action
        sd.setMgrReady(true);
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
     * Fetch the category tree from the engine for all languages
     * 
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    protected void fetchCategoryTree() throws KKException, KKAppException
    {
        // Save the category tree
        Map<Integer, CategoryIf[]> treeMap = Collections
                .synchronizedMap(new HashMap<Integer, CategoryIf[]>());

        // Save a hash map of categories to make them easy to find
        Map<Integer, HashMap<Integer, CategoryIf>> mapMap = Collections
                .synchronizedMap(new HashMap<Integer, HashMap<Integer, CategoryIf>>());

        LanguageIf[] langs = eng.getAllLanguages();

        if (langs != null && langs.length > 0)
        {
            CategoryTreeOptionsIf options = new CategoryTreeOptions();
            options.setGetProductCounts(true);
            if (kkAppEng.getFetchProdOptions() != null)
            {
                options.setCatalogId(kkAppEng.getFetchProdOptions().getCatalogId());
            }

            for (int i = 0; i < langs.length; i++)
            {
                LanguageIf lang = langs[i];
                options.setLanguageId(lang.getId());
                CategoryIf[] catArray = eng.getCategoryTreeWithOptions(options);

                /*
                 * fill miscellaneous items for the top set of categories since are used to save
                 * banner info in default storefront. May be removed if they aren't being used for
                 * this.
                 */
                if (catArray != null)
                {
                    for (int j = 0; j < catArray.length; j++)
                    {
                        CategoryIf cat = catArray[j];
                        CategoryIf fullCat = eng.getCategory(cat.getId(), lang.getId());
                        cat.setMiscItems(fullCat.getMiscItems());
                    }
                }
                treeMap.put(lang.getId(), catArray);

                // map of cats
                HashMap<Integer, CategoryIf> hm = new HashMap<Integer, CategoryIf>();
                addCatsToMap(catArray, hm);
                mapMap.put(lang.getId(), hm);
            }
        }
        sd.setCatsTreeMap(treeMap);
        sd.setCatsMapMap(mapMap);
    }

    /**
     * Recursive method to add all categories to the map
     * 
     * @param cats
     * @param hm
     */
    private void addCatsToMap(CategoryIf[] cats, HashMap<Integer, CategoryIf> hm)
    {
        if (cats != null)
        {
            for (int i = 0; i < cats.length; i++)
            {
                CategoryIf cat = cats[i];
                if (cat != null)
                {
                    if (cat.getChildren() != null)
                    {
                        addCatsToMap(cat.getChildren(), hm);
                    }
                    hm.put(cat.getId(), cat);
                }
            }
        }
    }

    /**
     * Puts the Category Manager back into it's original state with no categories selected
     */
    public void reset()
    {
        currentCat = getNewCat();
    }

    /**
     * Sets <code>currentCat</code>. If the selected category is a leaf node then the ProductMgr
     * object is updated with the relative product and manufacturer information for the category.
     * i.e. The manufacturers and products for that category are fetched from the server.
     * 
     * @param catId
     *            The category id of the selected category
     * @param ps
     *            productSearch object
     * @return Return the number of products found. Return a negative number if it is a category
     *         with child categories but no products of its own.
     * @throws KKException
     *            an unexpected KKException exception
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int setCurrentCatAndUpdateProducts(int catId, ProductSearch ps) throws KKException,
            KKAppException
    {
        // Create a new catMenuList
        List<CategoryIf> catMenu = setCurrentCat(catId);

        ProductsIf prods = kkAppEng.getProductMgr().fetchProducts(null, ps, null, catMenu);

        if (currentCat.getChildren() != null && currentCat.getChildren().length > 0)
        {
            // Return -1 if this is a parent cat with no prods of its own
            return -1;
        }

        if (prods != null && prods.getProductArray() != null)
        {
            return prods.getProductArray().length;
        }

        return 0;
    }

    /**
     * It returns the category from a hash map
     * 
     * @param catId
     *            the catId
     * @return A category object
     */
    public CategoryIf getCatFromId(int catId)
    {
        HashMap<Integer, CategoryIf> catMap = getCategoryMap();
        return catMap.get(catId);
    }

    /**
     * Creates a category menu list so that the facets tile can display the correct information. We
     * do not go and get any products since this is being taken care of elsewhere.
     * 
     * @param catId
     *            The numeric id of the current category
     * @return Returns a list of categories to display as a category menu
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public List<CategoryIf> setCurrentCat(int catId) throws KKAppException
    {
        currentCat = getCatFromId(catId);
        if (currentCat == null)
        {
            // This should never happen
            currentCat = getNewCat();
            return null;
        }

        return getCatMenuList(currentCat);
    }

    /**
     * Method that creates a new category menu list for a selected category. It displays the
     * children of that category, itself and the parent hierarchy. It doesn't display the siblings.
     * 
     * @param selectedCat
     *            the selectedCat
     * 
     * @return Returns a list of categories to display as a category menu
     */
    public List<CategoryIf> getCatMenuList(CategoryIf selectedCat)
    {
        return getCatMenuList(selectedCat, /* getChildren */true);
    }

    /**
     * Method that creates a new category menu list for a selected category. It displays the
     * children of that category, itself and the parent hierarchy. It doesn't display the siblings.
     * 
     * @param _selectedCat
     *            the _selectedCat
     * @param getChildren
     *            Displays children when set
     * 
     * @return Returns a list of categories to display as a category menu
     */
    public List<CategoryIf> getCatMenuList(CategoryIf _selectedCat, boolean getChildren)
    {
        CategoryIf selectedCat = _selectedCat;
        if (selectedCat == null)
        {
            return null;
        }

        // Save the numProds and then get a category from tree that has hierarchy
        int numProds = selectedCat.getNumberOfProducts();
        selectedCat = getCatFromId(selectedCat.getId());

        List<CategoryIf> catMenuList = new ArrayList<CategoryIf>();
        if (selectedCat != null)
        {
            if (selectedCat.getParent() == null)
            {
                CategoryIf cat = cloneCatForMenuList(selectedCat);
                cat.setLevel(0);
                cat.setNumberOfProducts(numProds);
                if (getChildren)
                    cat.setSelected(true);
                catMenuList.add(cat);
            } else
            {
                List<CategoryIf> tmpList = new ArrayList<CategoryIf>();
                CategoryIf topCat = selectedCat;
                tmpList.add(topCat);
                while (topCat.getParent() != null)
                {
                    topCat = topCat.getParent();
                    tmpList.add(topCat);
                }
                int j = 0;
                for (int i = tmpList.size() - 1; i > -1; i--)
                {
                    CategoryIf cat = cloneCatForMenuList(tmpList.get(i));
                    int level = j++;
                    cat.setLevel(level);
                    catMenuList.add(cat);
                    if (i == 0)
                    {
                        if (getChildren)
                            cat.setSelected(true);
                        cat.setNumberOfProducts(numProds);
                    } else
                    {
                        cat.setNumberOfProducts(-1);
                    }
                }
            }
            if (selectedCat.getChildren() != null && getChildren)
            {
                for (int i = 0; i < selectedCat.getChildren().length; i++)
                {
                    CategoryIf cat = selectedCat.getChildren()[i];
                    catMenuList.add(cloneCatForMenuList(cat));
                }
            }
        }

        return catMenuList;
    }

    /**
     * Method that creates a new category menu list for an array of selected categories which may be
     * returned as facets after a search. It displays the categories and the parent hierarchy. It
     * doesn't display the siblings or the children
     * 
     * @param catArray
     *            the catArray
     * 
     * @return Returns a list of categories to display as a category menu
     */
    public List<CategoryIf> getCatMenuList(CategoryIf[] catArray)
    {
        if (catArray == null || catArray.length == 0)
        {
            return null;
        }

        List<CategoryIf> menuList = null;
        for (int i = catArray.length - 1; i > -1; i--)
        {
            CategoryIf cat = catArray[i];
            boolean added = false;
            if (menuList == null)
            {
                menuList = getCatMenuList(cat, /* getChildren */false);
            } else
            {
                List<CategoryIf> newList = getCatMenuList(cat,/* getChildren */false);
                // Traverse new menu from child to parent
                for (int j = newList.size() - 1; j > -1; j--)
                {
                    CategoryIf newCat = newList.get(j);
                    // Determine whether cat exists in menuList
                    for (int k = 0; k < menuList.size(); k++)
                    {
                        CategoryIf menuCat = menuList.get(k);
                        if (menuCat.getId() == newCat.getId())
                        {
                            // If we find a match we add all cats downstream from match
                            int index = k + 1;
                            if (newList.size() >= j + 2)
                            {
                                for (int l = j + 1; l < newList.size(); l++)
                                {
                                    CategoryIf catToAdd = newList.get(l);
                                    menuList.add(index++, catToAdd);
                                }
                            }
                            added = true;
                            break;
                        }
                    }
                    /*
                     * Break if added otherwise try next in the menu
                     */
                    if (added)
                    {
                        break;
                    }
                }
                if (!added)
                {
                    for (int m = 0; m < newList.size(); m++)
                    {
                        CategoryIf catToAdd = newList.get(m);
                        menuList.add(catToAdd);
                    }
                }

            }
        }
        return menuList;
    }

    /**
     * Determine whether to show the number of products per category. Normally called directly by
     * the UI.
     * 
     * @return Returns true if we should show counts. Otherwise returns false.
     */
    public boolean isShowCounts()
    {
        return sd.isShowCounts();
    }

    /**
     * @return Return a new category object
     */
    private CategoryIf getNewCat()
    {
        CategoryIf cat = new Category();
        cat.setName("");
        cat.setImage("");
        cat.setId(-1);
        return cat;
    }

    /**
     * Returns a static list of drop list elements. This list contains all of the categories and is
     * fetched once from the engine and then cached.
     * 
     * @return Returns the static list of drop list elements.
     */
    // public DropListElement[] getAllCatsDropList()
    // {
    // return CategoryMgr.allCategories;
    // }

    /**
     * Returns the category tree (i.e. Categories in a hierarchical tree)
     * 
     * @return Returns an array of top level categories
     */
    public CategoryIf[] getCats()
    {
        if (sd.getCatsTreeMap() == null)
        {
            log.warn("Categories not yet set up");
            return null;
        }

        CategoryIf[] cats = getCategoryTree();

        if (cats == null)
        {
            log.warn("Categories not yet set up for language " + kkAppEng.getLangId());
            return null;
        }

        if (cats.length == 0)
        {
            if (log.isDebugEnabled())
            {
                log.debug("No Categories found for language " + kkAppEng.getLangId());
            }
        } else
        {
            if (log.isDebugEnabled())
            {
                log.debug(cats.length + " Categories found for language " + kkAppEng.getLangId());
            }
        }

        return cats;
    }

    /**
     * Returns the category that is currently selected in the UI.
     * 
     * @return Returns the currentCat.
     */
    public CategoryIf getCurrentCat()
    {
        return currentCat;
    }

    /**
     * Get an array of drop list elements in the correct language
     * 
     */
    private DropListElement[] getAllCategoryDropList()
    {
        CategoryIf[] catTree = getCats();
        // Instantiate list of drop list elements
        if (catTree != null)
        {
            ArrayList<DropListElement> allCatsList = new ArrayList<DropListElement>();
            for (int i = 0; i < catTree.length; i++)
            {
                DropListElement dle = new DropListElement(catTree[i].getId(), catTree[i].getName());
                allCatsList.add(dle);
                recurseChildren(allCatsList, catTree[i]);
            }

            int i = 0;
            DropListElement[] allCatsDl = new DropListElement[allCatsList.size()];
            for (Iterator<DropListElement> iter = allCatsList.iterator(); iter.hasNext();)
            {
                DropListElement dle = iter.next();
                allCatsDl[i++] = dle;
            }
            return allCatsDl;
        }
        return new DropListElement[0];
    }

    /**
     * Called by getAllCategoryDropList()
     * 
     * @param allCatsList
     * @param cat
     */
    private void recurseChildren(ArrayList<DropListElement> allCatsList, CategoryIf cat)
    {
        CategoryIf[] children = cat.getChildren();
        if (children != null)
        {
            for (int i = 0; i < children.length; i++)
            {
                DropListElement dle = new DropListElement(children[i].getId(), "->"
                        + children[i].getName());
                allCatsList.add(dle);
                recurseChildren(allCatsList, children[i]);
            }
        }
    }

    /**
     * This is only used for advanced product search so we can create it every time in the correct
     * language.
     * 
     * @return Returns the static list of drop list elements.
     */
    public DropListElement[] getAllCatsDropList()
    {
        return getAllCategoryDropList();
    }

    /**
     * Clone a category for the category menu list so that we have a private copy where we can set
     * private information such as whether the category is selected or not.
     * 
     * @param catIn
     * @return Return a clone of catIn
     */
    private CategoryIf cloneCatForMenuList(CategoryIf catIn)
    {
        CategoryIf catOut = new Category();
        catOut.setChildren(catIn.getChildren());
        catOut.setId(catIn.getId());
        catOut.setImage(catIn.getImage());
        catOut.setName(catIn.getName());
        catOut.setNumberOfProducts(catIn.getNumberOfProducts());
        catOut.setParentId(catIn.getParentId());
        catOut.setSortOrder(catIn.getSortOrder());
        catOut.setParent(catIn.getParent());
        catOut.setLevel(catIn.getLevel());
        catOut.setCustom1(catIn.getCustom1());
        catOut.setCustom2(catIn.getCustom2());
        catOut.setCustom3(catIn.getCustom3());
        catOut.setInvisible(catIn.isInvisible());
        catOut.setDescription(catIn.getDescription());
        catOut.setMiscItems(catIn.getMiscItems());
        return catOut;
    }

    /**
     * When a customer logs in, we may assign a catalog to the customer in a B2B scenario. The
     * catalog may exclude a number of products and so the category product counts will be different
     * to the global category trees. Therefore what we do is to create and store private category
     * structures for this customer.
     * 
     * @throws KKException
     *            an unexpected KKException exception
     */
    protected void createPrivateCategoryStructures() throws KKException
    {
        HashMap<Integer, CategoryIf> globalCatMap = getCategoryMap();

        CategoryTreeOptionsIf options = new CategoryTreeOptions();
        options.setGetProductCounts(true);
        if (kkAppEng.getFetchProdOptions() != null)
        {
            options.setCatalogId(kkAppEng.getFetchProdOptions().getCatalogId());
        }
        options.setLanguageId(kkAppEng.getLangId());
        privateCatTree = eng.getCategoryTreeWithOptions(options);

        /*
         * fill miscellaneous items for the top set of categories since are used to save banner info
         * in default storefront. May be removed if they aren't being used for this.
         */
        if (privateCatTree != null)
        {
            for (int j = 0; j < privateCatTree.length; j++)
            {
                CategoryIf cat = privateCatTree[j];
                CategoryIf fullCat = globalCatMap.get(cat.getId());
                cat.setMiscItems(fullCat.getMiscItems());
            }
        }

        // map of cats
        privateCatMap = new HashMap<Integer, CategoryIf>();
        addCatsToMap(privateCatTree, privateCatMap);
    }

    /**
     * Set the private category structures to null when the customer logs out
     */
    protected void resetPrivateCategoryStructures()
    {
        privateCatTree = null;
        privateCatMap = null;
    }

    /**
     * Returns the category tree. It either returns the global static tree or the private tree if
     * available.
     * 
     * @return Returns the category tree
     */
    protected CategoryIf[] getCategoryTree()
    {
        if (privateCatTree != null)
        {
            return privateCatTree;
        }
        return sd.getCatsTreeMap().get(kkAppEng.getLangId());
    }

    /**
     * Returns the category map. It either returns the global static map or the private map if
     * available.
     * 
     * @return Returns the category map
     */
    protected HashMap<Integer, CategoryIf> getCategoryMap()
    {
        if (privateCatMap != null)
        {
            return privateCatMap;
        }
        return sd.getCatsMapMap().get(kkAppEng.getLangId());
    }

    /**
     * Used to store the static data of this manager
     */
    private class StaticData
    {
        // Hash map to contain a category tree for each language
        Map<Integer, CategoryIf[]> catsTreeMap = Collections
                .synchronizedMap(new HashMap<Integer, CategoryIf[]>());

        Map<Integer, HashMap<Integer, CategoryIf>> catsMapMap = Collections
                .synchronizedMap(new HashMap<Integer, HashMap<Integer, CategoryIf>>());

        // Static config variables
        boolean showCounts = false;

        // is the manager ready?
        boolean mgrReady = false;

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
         * @return the showCounts
         */
        public boolean isShowCounts()
        {
            return showCounts;
        }

        /**
         * @param showCounts
         *            the showCounts to set
         */
        public void setShowCounts(boolean showCounts)
        {
            this.showCounts = showCounts;
        }

        /**
         * @return the catsTreeMap
         */
        public Map<Integer, CategoryIf[]> getCatsTreeMap()
        {
            return catsTreeMap;
        }

        /**
         * @param catsTreeMap
         *            the catsTreeMap to set
         */
        public void setCatsTreeMap(Map<Integer, CategoryIf[]> catsTreeMap)
        {
            this.catsTreeMap = catsTreeMap;
        }

        /**
         * @return the catsMapMap
         */
        public Map<Integer, HashMap<Integer, CategoryIf>> getCatsMapMap()
        {
            return catsMapMap;
        }

        /**
         * @param catsMapMap
         *            the catsMapMap to set
         */
        public void setCatsMapMap(Map<Integer, HashMap<Integer, CategoryIf>> catsMapMap)
        {
            this.catsMapMap = catsMapMap;
        }
    }

}
