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
<%@page import="java.util.Random"%>
<%@page import="com.konakart.app.Content"%>
<%@page import="com.konakart.app.ContentDescription"%>
<%@page import="com.konakart.appif.ContentIf"%>
<%@page import="java.lang.InterruptedException"%>
<%  com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<%  boolean hideRow1 =  kkEng.getPropertyAsBoolean("main.page.hide.banner.row1", false);%>
<%  boolean hideRow2 =  kkEng.getPropertyAsBoolean("main.page.hide.banner.row2", false);%>
<%  boolean contentEnabled = kkEng.getContentMgr().isEnabled();%>
<%  String contentDir = kkEng.getContentImagesDir();%>
<% int limite = 0; %>


	<%if (!hideRow1) { %>
	<%  ContentIf banner = null;%>

	<%if (contentEnabled) { %>
		<% ContentIf[] topBanners = kkEng.getContentMgr().getContentForType(3, 1);%>
		<% System.out.println("Banner: "+topBanners); %>
		 <%-- <input type="hidden" id="bannerShow" value="<%=banner%>"/>
		 <input type="hidden" id="contador" value="<%=topBanners.length-1%>"/> --%>
		<% 
		for(int f = 0; f< topBanners.length;f++){
			try {
				String idV="bannerArray"+f;
				//System.out.println("# "+idV);
				//System.out.println(topBanners[f].getClickUrl());
				String url=topBanners[f].getClickUrl();
				//System.out.println(topBanners[f].getDescription().getName1());
				banner = topBanners[f];
				System.out.println("Banner datos: "+banner);
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				Thread.sleep(5000);
			} catch (Exception ex){
				System.out.println("**");
			}
			
			
			%>
<%--  			<input type="hidden" id="bannerArray" value="<%=topBanners%>"/>  --%>
<%-- 		<input type="hidden" id=<%=idV%> value="<%=topBanners[f]%>"/> --%>
		
			<% 	
		}	   
		%>
		<%if (topBanners.length >= 1) { %>
			<% int idx = new Random().nextInt(topBanners.length);%>
			<% banner = topBanners[idx];
			System.out.println("-----"+banner);%>
		<% } %>
		var b = "<%=banner%>";
		var tb = "<%=topBanners%>";
		b= tb[1];
		console.log(b);
	<% } %> 
	<%if (banner == null) { %>
		<% banner = new Content();%>
		<% banner.setDescription(new ContentDescription());%>
		<% banner.getDescription().setName1("banner-baterias.png");%>
		<% banner.getDescription().setName2("banner-baterias.png");%>
		<% banner.getDescription().setName3("banner-baterias.png");%>
		<% banner.getDescription().setTitle("Descuento Acumuladores");%>
		<% banner.setClickUrl("SelectProd.action?prodId=66");%>
	<% } %> 

	<div id="slideshow"  class="rounded-corners" >
	<a href="<%=banner.getClickUrl()%>">
		<picture id="slide-1" class="slide rounded-corners">
			<!--[if IE 9]><video style="display: none;"><![endif]-->
			<source  srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner.getDescription().getName1()%>" media="(min-width: 750px)">
			<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner.getDescription().getName2()%>" media="(min-width: 440px)">
			<source srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner.getDescription().getName3()%>" >
			<!--[if IE 9]></video><![endif]-->
			<img srcset="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner.getDescription().getName1()%>" alt="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner.getDescription().getTitle()%>">
		</picture>
	</a>
	</div>
<%} %>


 


<%if (!hideRow2) { %>
	<% ContentIf banner1 = null;%>
	<% ContentIf banner2 = null;%>
	<% ContentIf banner3 = null;%>
	<% ContentIf banner4 = null;%>

	<%if (contentEnabled) { %>
		<% ContentIf[] subBanners = kkEng.getContentMgr().getContentForType(6, 2);%>
		<%if (subBanners.length >= 5) { %>
			<% banner1 = subBanners[0];%>
			<% banner2 = subBanners[1];%>
			
			<% banner3 = null;%>
			<%if ((int) (Math.random() * 100) > 50) { %>
				<% banner3 = subBanners[2];%>
			<% } else { %>
				<% banner3 = subBanners[3];%>
			<% } %> 
			
			<% banner4 = subBanners[4];%>
		<% } %> 
	<% } %> 

	<%if (banner1 == null) { %>
		<% banner1 = new Content();%>
		<% banner1.setDescription(new ContentDescription());%>
		<% banner1.getDescription().setName1("home_electronics-sale.jpg");%>
		<% banner1.setClickUrl("ShowSpecials.action");%>
	<% } %> 
	
	<%if (banner2 == null) { %>
		<% banner2 = new Content();%>
		<% banner2.setDescription(new ContentDescription());%>
		<% banner2.getDescription().setName1("home_electronics-sale-2.jpg");%>
		<% banner2.setClickUrl("SelectCat.action?catId=23");%>
	<% } %> 
	
	<%if (banner3 == null) { %>
		<% banner3 = new Content();%>
		<% banner3.setDescription(new ContentDescription());%>
		<% banner3.getDescription().setName1("home_gifts-for-the-home.jpg");%>
		<% banner3.setClickUrl("SelectCat.action?catId=24");%>
	<% } %> 
	
	<%if (banner4 == null) { %>
		<% banner4 = new Content();%>
		<% banner4.setDescription(new ContentDescription());%>
		<% banner4.getDescription().setName1("home_iphone-5.jpg");%>
		<% banner4.setClickUrl("SelectProd.action?prodId=35");%>
	<% } %> 
	
	<div id="banners">
	<%if (banner1 != null) { %>
		<a href="<%=banner1.getClickUrl()%>"><img id="banner-1" class="banner-small rounded-corners" 
			src="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner1.getDescription().getName1()%>"/></a>
	<% } %> 
	<%if (banner2 != null) { %>
		<a href="<%=banner2.getClickUrl()%>"><img id="banner-2" class="banner-small rounded-corners" 
			src="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner2.getDescription().getName1()%>"/></a>
	<% } %> 
	<%if (banner3 != null) { %>
		<a href="<%=banner3.getClickUrl()%>"><img id="banner-3" class="banner-small rounded-corners" 
			src="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner3.getDescription().getName1()%>"/></a>
	<% } %> 
	<%if (banner4 != null) { %>
		<a href="<%=banner4.getClickUrl()%>"><img id="banner-4" class="banner-small rounded-corners last-child" 
			src="<%=kkEng.getImageBase()%>/<%=contentDir%>/<%=banner4.getDescription().getName1()%>"/></a>
	<% } %> 
	</div>
<% } %> 
<%
    String myVar="blabla";
%>

<%-- <script>
 	function changeImage(){
		
 		var contador = document.getElementById("contador").value;
		var bannerShow = document.getElementById("bannerShow").value;
		for (i=0; i<=contador; i++){
		//console.log("******"+i);
		var arrayBanners = document.getElementById("bannerArray"+i).value;
		//console.log("*******************"+arrayBanners);
		//console.log("%%%%%%%%%%%%%%%%%%%"+bannerShow);
		bannerShow = arrayBanners;
		console.log("{{{{}}}}"+bannerShow);
		 <%=banner%>
		} 
/* 		var  cont = 0;
 		if(cont <= contador){
 			//console.log(cont);
			//console.log("if");
			//bannerShow =arrayBanners[cont];
 			//console.log("*****"+bannerShow);
 			console.log("---"+cont+"---");
			return cont++;
 		} 
 		if (cont > arrayBanners.length){
 			console.log("+++"+cont+"+++");
 			return cont = 0;
 		} */
 		console.log("¨*******************************");
		setTimeout(changeImage,2000);	
	}
 	changeImage();
</script>  --%>