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

<s:set scope="request" var="users" value="users"/> 
<%com.konakartadmin.app.AdminCustomer[] users = (com.konakartadmin.app.AdminCustomer[])(request.getAttribute("users")); %>


 				<h1 id="page-title"><kk:msg  key="common.company.user.admin"/></h1>			
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
								<div class="form-section-title no-margin">
									<h3><kk:msg  key="b2buser.body.users"/><a href="B2BCustomerRegistration.action"  class="button-medium new-addr-button small-rounded-corners"><span><kk:msg  key="b2buser.body.add.user"/></span></a></h3>									
								</div>
								<%if (users != null && users.length > 0){ %>
									<% for (int i = 0; i < users.length; i++){ %>
										<% com.konakartadmin.app.AdminCustomer user = users[i];%>
										<div class="select-cust-section <%=(i%2==0)?"even":"odd"%>">
											<div class="select-cust">
												<%if (user.isEnabled()){ %>
													<img src="<%=kkEng.getImageBase()%>/green-circle.png" height="8px" width="8px">&nbsp;
												<% } else { %>
													<img src="<%=kkEng.getImageBase()%>/red-circle.png" height="8px" width="8px">&nbsp;
												<% } %>
												<%=user.getFirstName()%>&nbsp;<%=user.getLastName()%>,&nbsp;<%=user.getEmailAddr()%>
											</div>
											<div class="select-cust-buttons">
												<a href='<%="B2BCustomerRoles.action?custId="+user.getId()%>' class="button-medium small-rounded-corners">
													<span><kk:msg  key="common.roles"/></span>
												</a>&nbsp;
												<a href='<%="B2BEditCustomer.action?custId="+user.getId()%>' class="button-medium small-rounded-corners">
													<span><kk:msg  key="common.edit"/></span>
												</a>&nbsp;
												<a href='<%="B2BAddressBook.action?custId="+user.getId()%>' class="button-medium small-rounded-corners">
													<span><kk:msg  key="common.address.book"/></span>
												</a>&nbsp;
												<a href='<%="B2BDeleteCustomer.action?custId="+user.getId()%>' class="button-medium small-rounded-corners">
													<span><kk:msg  key="common.delete"/></span>
												</a>&nbsp;
												<%if (user.isEnabled()){ %>
													<a href='<%="B2BCustomerState.action?custId="+user.getId()+"&action=d"%>' class="button-medium small-rounded-corners">
														<span><kk:msg  key="common.disable"/></span>
													</a>																					
												<% } else { %>
													<a href='<%="B2BCustomerState.action?custId="+user.getId()+"&action=e"%>' class="button-medium small-rounded-corners">
														<span><kk:msg  key="common.enable"/></span>
													</a>																					
												<% } %>
											</div>
										</div>
									<% } %>
								<% } else { %>
									<kk:msg  key="b2buser.body.no.users"/>
								<% } %>
							</div>
							<div class="form-buttons-wide">
								<a href="MyAccount.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>


