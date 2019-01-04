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

<s:set scope="request" var="delCust" value="delCust"/> 
<%com.konakartadmin.app.AdminCustomer delCust = (com.konakartadmin.app.AdminCustomer)(request.getAttribute("delCust")); %>


 <h1 id="page-title"><kk:msg  key="delete.b2bcustomer.title"/></h1>			
 <div class="content-area rounded-corners">
	<div class="form-section">
		<div class="del-cust-header">
			<div class="del-cust-explanation">
				<kk:msg  key="delete.b2bcustomer.areyousure"/>
			</div>
			<div class="del-cust-cust">
				<%if (delCust != null){ %>
					<%=delCust.getFirstName()%>&nbsp;<%=delCust.getLastName()%>,&nbsp;<%=delCust.getEmailAddr()%>
				<% } %>
			</div>
		</div>
	</div>
	<div class="form-buttons-wide">
		<a href='<%="B2BDeleteCustomerSubmit.action?custId="+delCust.getId()%>'  id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.delete"/></span></a>
		<a href="B2BManageCustomers.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
	</div>
 </div>


