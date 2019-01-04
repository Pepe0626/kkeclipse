<%--
//
// (c) 2017 DS Data Systems UK Ltd, All rights reserved.
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
--%>
<%@include file="Taglibs.jsp" %>

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");  %>
<s:set scope="request" var="delAddr" value="delAddr"/> 
<%com.konakartadmin.app.AdminAddress delAddr = (com.konakartadmin.app.AdminAddress)(request.getAttribute("delAddr")); %>
<s:set scope="request" var="b2bCustId" value="b2bCustId"/> 
<%int b2bCustId = (Integer)(request.getAttribute("b2bCustId")); %>

 				<h1 id="page-title"><kk:msg  key="delete.addr.book.title"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
			    		<form action="B2BDeleteAddressSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
			    			<input type="hidden" value="<s:property value="addrId" />" name="addrId"/>
			    			<input type="hidden" value="<s:property value="b2bCustId" />" name="b2bCustId"/>
							<div class="form-section">
								<div class="addr-book-header">
									<div class="addr-book-explanation">
										<kk:msg  key="delete.addr.book.areyousure"/>
									</div>
									<div class="addr-book-addr">
										<%=kkEng.removeCData(delAddr.getFormattedAddress())%>
									</div>
								</div>
							</div>
							<div class="form-buttons-wide">
								<a onclick="document.getElementById('form1').submit();"  id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.delete"/></span></a>
								<a href="B2BAddressBook.action?custId=<%=b2bCustId%>" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


