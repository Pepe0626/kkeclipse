//
// (c) 2018 DS Data Systems UK Ltd, All rights reserved.
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
package com.konakart.bl.modules.others;

import com.konakart.app.ExternalMailInput;
import com.konakart.app.ExternalMailResult;
import com.konakart.app.KKException;

/**
 * Interface for external mail modules
 * 
 */
public interface ExternalMailInterface
{
    /**
     * Called to send an email through an external email service
     * 
     * @param input
     *            Object containing information required for sending the email
     * @return Returns a ExternalMailResult object with information regarding the outcome of the
     *         send mail attempt
     * @throws Exception an unexpected exception
     */
    public ExternalMailResult sendExternalMail(ExternalMailInput input) throws Exception;

    /**
     * Determines whether the module is available
     * 
     * @return True or False
     * @throws KKException
     *            an unexpected KKException exception
     */
    public boolean isAvailable() throws KKException;

}
