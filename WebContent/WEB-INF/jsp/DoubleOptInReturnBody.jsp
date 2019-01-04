<%--
//
// (c) 2018 DS Data Systems UK Ltd, All rights reserved.
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

	<s:set scope="request" var="closePanelAction" value="closePanelAction"/> 
	<%String closePanelAction = (String)(request.getAttribute("closePanelAction")); %>
	<s:set scope="request" var="emailAddr" value="emailAddr"/> 
	<%String emailAddr = (String)(request.getAttribute("emailAddr")); %>
	<s:set scope="request" var="message" value="message"/> 
	<%String message = (String)(request.getAttribute("message")); %>	
	<h1 id="page-title"><kk:msg  key="header.double.opt.in"/></h1>			
	<div class="content-area rounded-corners">
		<div id="double-opt-in-ret">
			<%=message%>
			<div class="form-buttons-wide">
				<a href="<%=closePanelAction%>?emailAddr=<%=emailAddr%>" id="continue-button" class="button small-rounded-corners"><span><kk:msg  key="common.close"/></span></a>
			</div>
		</div>
	</div>


