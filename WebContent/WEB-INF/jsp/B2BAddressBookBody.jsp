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
<s:set scope="request" var="b2bCustName" value="b2bCustName"/> 
<%String b2bCustName = (String)(request.getAttribute("b2bCustName")); %>
<s:set scope="request" var="b2bCustId" value="b2bCustId"/> 
<%int b2bCustId = (Integer)(request.getAttribute("b2bCustId")); %>
<s:set scope="request" var="addresses" value="addresses"/> 
<%com.konakartadmin.app.AdminAddress[] addresses = (com.konakartadmin.app.AdminAddress[])(request.getAttribute("addresses")); %>


 				<h1 id="page-title"><kk:msg  key="b2b.addresses.body.title" arg0="<%=b2bCustName%>"/></h1>		
	    		<div class="content-area rounded-corners">
		    		<div>
		    			<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    		    		
						<s:if test="hasActionMessages()">
						   <div class="messageStackSuccess">  
						        <s:iterator value="actionMessages">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>		    
			    		<form action="EditCustomerSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
							<div class="form-section">
								<div class="form-section-title">
									<h3><kk:msg  key="address.book.body.primaryaddress"/></h3>								
								</div>
								<div class="addr-book-header">
									<div class="addr-book-explanation">
										<kk:msg  key="b2b.addresses.body.primary.explanation" arg0="<%=b2bCustName%>"/>
									</div>
									<div class="addr-book-addr">
										<%if (addresses != null && addresses.length > 0){ %>
											<%=kkEng.removeCData(addresses[0].getFormattedAddress())%>
										<% } %>
									</div>
								</div>
							</div>
							<div class="form-section">
								<div class="form-section-title no-margin">
									<h3><kk:msg  key="address.book.body.addressbookentries"/><a href='<%="B2BNewAddress.action?custId="+b2bCustId%>'  class="button-medium new-addr-button small-rounded-corners"><span><kk:msg  key="address.book.body.newaddress"/></span></a></h3>									
								</div>
								<%if (addresses != null && addresses.length > 0){ %>
									<% for (int i = 0; i < addresses.length; i++){ %>
										<% com.konakartadmin.app.AdminAddress addr = addresses[i];%>
										<div class="select-addr-section <%=(i%2==0)?"even":"odd"%>">
											<div class="select-addr">
												<%if (i == 0){ %>
													<span class="primary-addr-label">(<kk:msg  key="address.book.body.primaryaddress"/>)</span><br>
												<% } %>											
												<%=kkEng.removeCData(addr.getFormattedAddress())%>
											</div>
											<div class="select-addr-buttons">
												<a href='<%="B2BEditAddress.action?addrId="+addr.getId()%>' class="button-medium small-rounded-corners">
													<span><kk:msg  key="common.edit"/></span>
												</a>&nbsp;
												<%if (i != 0){ %>
													<a href='<%="B2BDeleteAddress.action?addrId="+addr.getId()%>' class="button-medium small-rounded-corners">
														<span><kk:msg  key="common.delete"/></span>
													</a>											
												<% } %>											
											</div>
										</div>
									<% } %>
								<% } %>
							</div>
							<div class="form-buttons-wide">
								<a href="B2BManageCustomers.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


