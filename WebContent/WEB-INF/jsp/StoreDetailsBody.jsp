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

<% com.konakart.al.KKAppEng kkEng = (com.konakart.al.KKAppEng) session.getAttribute("konakartKey");%>
<% com.konakart.al.CustomerMgr customerMgr = kkEng.getCustomerMgr();%>
<% com.konakart.appif.VendorIf vendor = customerMgr.getCurrentVendor();%>	
<% com.konakart.appif.StoreIf store = customerMgr.getCurrentStore();%>	
<% com.konakart.al.ReviewMgr revMgr = kkEng.getReviewMgr();%>
<% String sortBy = revMgr.getDataDesc().getOrderBy();%>
<%com.konakart.appif.ReviewSearchIf search = revMgr.getRevSearch();%>
<%int rating = 0; %>
<%String ratingDecStr = "0"; %>
<%if (vendor != null){ %>
	<%rating = (vendor.getRating()==null)?0:vendor.getRating().setScale(0, java.math.BigDecimal.ROUND_HALF_UP).intValue();%>
	<%ratingDecStr = (vendor.getRating()==null)?"0":(vendor.getRating().setScale(1, java.math.BigDecimal.ROUND_HALF_UP)).toPlainString();%>
<% } %>


	<h1 id="page-title"><kk:msg  key="header.store"/></h1>			
	<div class="content-area rounded-corners">
		<div id="store-details">
		
			<div class="form-section">
				<div class="form-section-title">
					<h3><%=store.getStoreName()%></h3>							
				</div>
				<div class="addr-book-header">
					<%if (store.getStoreDesc() != null){ %>
						<div class="addr-book-explanation">
							<%=store.getStoreDesc()%>
						</div>
					<% } %>
					<%if (vendor != null && vendor.getDefaultAddr() != null){ %>
						<div class="addr-book-addr">
							<%=kkEng.removeCData(vendor.getDefaultAddr().getFormattedAddress())%>
						</div>
					<% } %>
				</div>
			</div>

	    	<div id="product-reviews">
    			<%if (vendor != null && vendor.getNumberReviews() > 0){ %>
		    		<div class="form-section-title">
						<h3><kk:msg  key="product.details.body.reviews"/> (<%=vendor.getNumberReviews()%>)</h3>							
					</div>
	   				<div id="average-customer-reviews">
	    				<div id="average-customer-reviews-meters">
	    					<% for (int i = 5; i > 0; i--){ %>
	    						<%int qty = revMgr.getRatingQuantity()[i-1]; %>
		    					<div class="average-customer-reviews-meter-container">
		    						<div class="average-customer-reviews-meter-label">
		    							<%if (qty > 0){ %> 
											<a href='<%="FilterVendorRev.action?rating="+i+"&t="+revMgr.getRevTimestamp()%>'>
												<span class="<%=(search!=null&&search.getRating()==i)?"kk-selected":"kk-not-selected"%>"></span><%=i%><kk:msg  key="show.reviews.body.stars"/>
											</a>
										<%}else{%>
											<span class="<%=(search!=null&&search.getRating()==i)?"kk-selected":"kk-not-selected"%>"></span><%=i%><kk:msg  key="show.reviews.body.stars"/>
										<%}%>
		    						</div>
		    						<div class="average-customer-reviews-meter">
		    							<span id="<%=i%>star" style="width:<%=revMgr.getRatingPercentage()[i-1]%>%"></span>
		    						</div>
		    						<div class="average-customer-reviews-number">(<%=qty%>)</div>				    					
		    					</div>
							<%}%>    				
	    				</div>
	    				<div id="average-customer-reviews-stars">
	    					<div id="average-customer-reviews-stars-title"><kk:msg  key="show.reviews.body.average"/>:</div>
	    					<div class="rating-big">
						        <%for (int i = 0; i < rating; i++){%>
						            <span class="star-big full"></span>
						         <%}%>
						        <%for (int i = rating; i < 5; i++){%>
						            <span class="star-big empty"></span>
						        <%}%>
						        &nbsp;(<%=ratingDecStr%>)
	    					</div>
	    					<div class="write-review">
	    						<a href='<%="WriteVendorReview.action?vid="+vendor.getId()+"&sid="+store.getStoreId()%>' class="write-review-button button small-rounded-corners"><kk:msg  key="common.write.review"/></a>	
	    					</div>
	    				</div>
	   				</div>
					<div class="product-reviews-navigation-mobile">
	   					<div class="product-reviews-navigation-top">
	    					<span class="number-of-items navigation-element"><%=revMgr.getCurrentOffset() + 1%>-<%=revMgr.getNumberOfReviews() + revMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=revMgr.getTotalNumberOfReviews()%></span>	
	    					<kk:paging pageList="<%=revMgr.getPageList()%>" currentPage="<%=revMgr.getCurrentPage()%>" showBack="<%=revMgr.getShowBack()%>" showNext="<%=revMgr.getShowNext()%>" action="NavigateVendorRev"  timestamp="<%=revMgr.getRevTimestamp()%>"></kk:paging>			    					
						</div>
						<div class="product-reviews-navigation-bottom">
							<kk:pageSize action="RevPageSize.action" name="numRevs" sizes="5,10,15,20" maxNum="<%=revMgr.getPageSize()%>" timestamp="<%=revMgr.getRevTimestamp()%>" type="small"/>			    				
		    				<kk:vendorRevOrderBy/>
						</div>
					</div>
					<div id="product-reviews-area">
						<%int numRevs =  (revMgr.getCurrentReviews().length > revMgr.getPageSize()) ? revMgr.getPageSize() : revMgr.getCurrentReviews().length;%>
						<% for (int i = 0; i < numRevs; i++){ %>
							<% com.konakart.appif.ReviewIf rev = revMgr.getCurrentReviews()[i];%>
							<div class="product-review">	
								<div class="rating">
			    					<%for (int j = 0; j < rev.getRating(); j++){%>
							            <span class="star full"></span>
							        <%}%>
							        <%for (int j = rev.getRating(); j < 5; j++){%>
							            <span class="star empty"></span>
							        <%}%>					    						
		    					</div>
		    					<div class="product-review-details">
		    						<kk:msg  key="show.reviews.body.by"/> <span class="product-review-details-author"><%=rev.getCustomerName()%></span>
									<span class="product-review-details-date"><%=new java.text.SimpleDateFormat("EEEE d MMMM yyyy").format(rev.getDateAdded().getTime())%></span>
								</div>
								<p><%=rev.getReviewText()%></p>
							</div>
						<%}%>				
					</div>
	   				<div class="product-reviews-navigation-mobile">
	   					<div class="product-reviews-navigation-top">
	    					<span class="number-of-items navigation-element"><%=revMgr.getCurrentOffset() + 1%>-<%=revMgr.getNumberOfReviews() + revMgr.getCurrentOffset()%> <kk:msg  key="common.of"/> <%=revMgr.getTotalNumberOfReviews()%></span>	
	    					<kk:paging pageList="<%=revMgr.getPageList()%>" currentPage="<%=revMgr.getCurrentPage()%>" showBack="<%=revMgr.getShowBack()%>" showNext="<%=revMgr.getShowNext()%>" action="NavigateVendorRev"  timestamp="<%=revMgr.getRevTimestamp()%>"></kk:paging>			    					
						</div>
						<div class="product-reviews-navigation-bottom">
							<kk:pageSize action="RevPageSize.action" name="numRevs" sizes="5,10,15,20" maxNum="<%=revMgr.getPageSize()%>" timestamp="<%=revMgr.getRevTimestamp()%>" type="small"/>			    				
		    				<kk:vendorRevOrderBy/>
	   					</div>		
					</div>
			<% } else if (vendor != null) { %>
				<p style="clear:both"><a href='<%="WriteVendorReview.action?vid="+vendor.getId()+"&sid="+store.getStoreId()%>' class="text-link"><kk:msg  key="show.reviews.body.be.first.store"/></a></p>
			<% } %>		   							
    	</div>	  
    </div>
</div>



