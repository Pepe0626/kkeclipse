package com.konakart.al;

import com.konakart.appif.BasketIf;
import com.konakart.appif.KKEngIf;
import com.konakart.appif.OptionIf;
import com.konakart.appif.ProductIf;

/**
 * Manager for managing customer quotas. A quota may be set for a product so that a customer cannot
 * buy more than x many products in a certain time frame or within a single purchase etc.<br>
 * This manager can be customized to add quota management logic pertinent to your business.
 * 
 */
public class QuotaMgr extends BaseMgr
{
    /**
     * The <code>Log</code> instance for this application.
     */
    // private Log log = LogFactory.getLog(ProductMgr.class);

    protected KKAppEng kkAppEng = null;

    // Max quantity of a product that can be added to cart
    private static final int MAX_NUM_PRODUCTS = 1000000;

    private int maxNumProducts = MAX_NUM_PRODUCTS;

    /**
     * Constructor
     * 
     * @param eng
     *            the eng
     * @param kkAppEng
     *            the kkAppEng
     */
    public QuotaMgr(KKEngIf eng, KKAppEng kkAppEng)
    {
        this.eng = eng;
        this.kkAppEng = kkAppEng;
    }

    /**
     * Returns the number of products that can be added to the cart for this customer.
     * 
     * @param productId
     *            Id of product being added to cart
     * @param opts
     *            Options
     * @return Returns the number of buyable products
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int canAddToBasket(int productId, OptionIf[] opts) throws KKAppException
    {
        return getQuotaForProduct(productId, opts) - getCurrentBasketQty(productId, opts);
    }

    /**
     * Returns the number of products that can be added to the cart for this customer.
     * 
     * @param product
     *            Product being added to cart
     * @param opts
     *            Options
     * @return Returns the number of buyable products.
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int canAddToBasket(ProductIf product, OptionIf[] opts) throws KKAppException
    {
        return getQuotaForProduct(product, opts) - getCurrentBasketQty(product.getId(), opts);
    }

    /**
     * Return the current quantity in the cart
     * 
     * @param productId
     * @param opts
     *            Options If not null, the array will be used to find a basket item with identical
     *            options
     * @return Return the current quantity in the cart
     */
    private int getCurrentBasketQty(int productId, OptionIf[] opts)
    {
        BasketIf[] items = kkAppEng.getCustomerMgr().getCurrentCustomer().getBasketItems();
        if (items != null && items.length > 0)
        {
            int qty = 0;
            for (int i = 0; i < items.length; i++)
            {
                BasketIf item = items[i];
                if (item.getProductId() == productId)
                {
                    if (opts != null)
                    {
                        int found = 0;
                        if (item.getOpts() != null && item.getOpts().length == opts.length)
                        {
                            for (int j = 0; j < opts.length; j++)
                            {
                                OptionIf opt = opts[j];
                                for (int k = 0; k < item.getOpts().length; k++)
                                {
                                    OptionIf itemOpt = item.getOpts()[k];
                                    if (itemOpt.getValueId() == opt.getAttrId()
                                            && itemOpt.getId() == opt.getId())
                                    {
                                        found++;
                                    }
                                }
                            }
                            if (found == item.getOpts().length)
                            {
                                return item.getQuantity();
                            }
                        }
                    } else
                    {
                        /*
                         * The quota does not have option granularity. There may be many products in
                         * the cart with identical product ids.
                         */
                        qty += item.getQuantity();
                    }
                }
            }
            return qty;
        }
        return 0;
    }

    /**
     * You may customize this method if the quota varies per product and is contained in a product
     * custom field or is retrieved from a service.
     * 
     * @param productId
     *            the productId
     * @param opts
     *            Options
     * @return Return the maximum quantity that can be ordered for this product
     */
    public int getQuotaForProduct(int productId, OptionIf[] opts)
    {
        return maxNumProducts;
    }

    /**
     * You may customize this method if the quota varies per product and is contained in a product
     * custom field or is retrieved from a service.
     * 
     * @param product
     *            the product
     * @param opts
     *            Options
     * @return Return the maximum quantity that can be ordered for this product
     */
    public int getQuotaForProduct(ProductIf product, OptionIf[] opts)
    {
        return maxNumProducts;
    }

}
