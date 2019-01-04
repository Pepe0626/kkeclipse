package com.konakart.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.konakart.al.KKAppEng;
import com.konakart.app.KKException;

/**
 * Used to implement custom cookie code
 * 
 */
public class CustomCookieAction extends BaseAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Called from within BaseAction.java when a customer first accesses the application.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void manageCookiesOnEntry(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng) throws KKException
    {

    }

    /**
     * Called from LoginSubmitAction.java after a login
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void manageCookiesAfterLogin(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng) throws KKException
    {

    }

    /**
     * Called from LogoutAction.java after a logout.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @param kkAppEng
     *            the kkAppEng
     * @throws KKException
     *            an unexpected KKException exception
     */
    public void manageCookiesAfterLogout(HttpServletRequest request, HttpServletResponse response,
            KKAppEng kkAppEng) throws KKException
    {

    }

}
