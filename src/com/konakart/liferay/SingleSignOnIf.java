//
// (c) 2016 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.liferay;

import com.konakart.al.KKAppEng;
import com.konakart.al.KKAppException;

/**
 * Single Sign On class used to retrieve customer information from Liferay. If the customer already
 * exists within KonaKart, he is logged in using the Liferay Login module. If he doesn't already
 * exist then he is registered before being logged in.
 */
public interface SingleSignOnIf
{
    /**
     * The following method provides an example of how you can get user information from Liferay in
     * order to provide SSO
     * 
     * @param kkAppEng
     *            the kkAppEng
     * @return Returns the customer id
     * @throws KKAppException
     *            an unexpected KKAppException exception
     */
    public int getCustomerId(KKAppEng kkAppEng) throws KKAppException;
}
