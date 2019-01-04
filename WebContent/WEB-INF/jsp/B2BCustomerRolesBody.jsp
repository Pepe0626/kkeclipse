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

<s:set scope="request" var="canChangeAddressBool" value="canChangeAddressBool"/> 
<%boolean canChangeAddressBool = (Boolean)(request.getAttribute("canChangeAddressBool")); %>
<s:set scope="request" var="ordersNeedApprovalBool" value="ordersNeedApprovalBool"/> 
<%boolean ordersNeedApprovalBool = (Boolean)(request.getAttribute("ordersNeedApprovalBool")); %>
<s:set scope="request" var="canViewSiblingOrdersBool" value="canViewSiblingOrdersBool"/> 
<%boolean canViewSiblingOrdersBool = (Boolean)(request.getAttribute("canViewSiblingOrdersBool")); %>
<s:set scope="request" var="orderLimitStr" value="orderLimitStr"/> 
<%String orderLimitStr = (String)(request.getAttribute("orderLimitStr")); %>
<s:set scope="request" var="aggOrderLimitStr" value="aggOrderLimitStr"/> 
<%String aggOrderLimitStr = (String)(request.getAttribute("aggOrderLimitStr")); %>
<s:set scope="request" var="b2bCustName" value="b2bCustName"/> 
<%String b2bCustName = (String)(request.getAttribute("b2bCustName")); %>

 				<h1 id="page-title"><kk:msg  key="b2b.roles.body.title" arg0="<%=b2bCustName%>"/></h1>			
	    		<div class="content-area rounded-corners">
		    		<div>
		    			<s:if test="hasActionErrors()">
						   <div class="messageStackError">  
						        <s:iterator value="actionErrors">  
						            <s:property escape="false"/>
						        </s:iterator>  
			    			</div>  
						</s:if>	
			    		<form action="B2BCustomerRolesSubmit.action" id="form1" method="post">
			    			<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
			    			<input type="hidden" value="<s:property value="b2bCustId" />" id="b2bCustId" name="b2bCustId" />
							<div class="form-section">
								<h3><kk:msg  key="b2b.roles.body.roles"/></h3>
								<div class="form-section-fields">
									<div class="form-section-divider"></div>
									<div class="form-input">
										<label><kk:msg  key="b2b.roles.body.can.change.address"/></label>
										<select name="canChangeAddressBool">
											<option value="true" <%=(canChangeAddressBool)?"selected=\"selected\"":"" %>><kk:msg  key="common.yes"/></option>
											<option value="false" <%=(!canChangeAddressBool)?"selected=\"selected\"":"" %>><kk:msg  key="common.no"/></option>
										</select>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="b2b.roles.body.orders.need.approval"/></label>
										<select name="ordersNeedApprovalBool">
											<option value="true" <%=(ordersNeedApprovalBool)?"selected=\"selected\"":"" %>><kk:msg  key="common.yes"/></option>
											<option value="false" <%=(!ordersNeedApprovalBool)?"selected=\"selected\"":"" %>><kk:msg  key="common.no"/></option>
										</select>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="b2b.roles.body.can.view.sibling.orders"/></label>
										<select name="canViewSiblingOrdersBool">
											<option value="true" <%=(canViewSiblingOrdersBool)?"selected=\"selected\"":"" %>><kk:msg  key="common.yes"/></option>
											<option value="false" <%=(!canViewSiblingOrdersBool)?"selected=\"selected\"":"" %>><kk:msg  key="common.no"/></option>
										</select>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="b2b.roles.body.order.limit"/></label>
										<input type="text" value="<s:property value="orderLimitStr" />" id="orderLimitStr" name="orderLimitStr" number="true" maxlength="10"/>
										<span class="validation-msg"></span>
									</div>
									<div class="form-input">
										<label><kk:msg  key="b2b.roles.body.agg.order.limit"/></label>
										<input type="text" value="<s:property value="aggOrderLimitStr" />" id="aggOrderLimitStr" name="aggOrderLimitStr" number="true" maxlength="10"/>
										<span class="validation-msg"></span>
									</div>
								</div>
							</div>
							<div class="form-buttons">
								<a onclick="javascript:formValidate('form1', 'continue-button');" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.continue"/></span></a>
								<a href="B2BManageCustomers.action" id="back-button" class="button small-rounded-corners"><span><kk:msg  key="common.back"/></span></a>
							</div>
						</form>
			    	</div>
	    		</div>

