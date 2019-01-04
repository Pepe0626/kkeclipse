<%--
//
// (c) 2012 DS Data Systems UK Ltd, All rights reserved.
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
<%if (kkEng != null) {%>

	<s:set scope="request" var="loginToken" value="loginToken"/> 
	<%String loginToken = (String)(request.getAttribute("loginToken")); %>
	<s:set scope="request" var="loginType" value="loginType"/> 
	<%String loginType = (String)(request.getAttribute("loginType")); %>
			
	<html>
		<head>
		<style>
			.pp-loading {
				background-image:url('images/loader_large.gif');
				background-repeat: no-repeat;
			    height: 50px;
				width: 50px;
				margin: 50px auto;
			}
			.pp-wait {
			    height: 50px;
				width: 150px;
				margin: 50px auto;
			}
		</style>
		<script type="text/javascript">	
		if (top.window.opener != null) {
			top.window.opener.location = top.location;
			top.close();
		}
		</script>
		</head>
			<body onload="if(top.window.opener==null) document.forms[0].submit();">
				<div class="pp-wait"><kk:msg  key="login.body.pp.wait"/></div>
				<div class="pp-loading"></div>
				<form action="LoginSubmit.action" id='extLoginForm' method="post">
					<input type="hidden" value="<%=kkEng.getXsrfToken()%>" name="xsrf_token"/>
					<input id="loginToken" name="loginToken" type="hidden" value="<%=loginToken%>"/>
					<input id="loginType" name="loginType" type="hidden" value="<%=loginType%>"/>
				</form>
			</body>
	</html>
<%}%>













