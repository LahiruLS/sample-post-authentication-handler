<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.TenantDataManager" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.util.Base64" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="java.net.URLDecoder" %>


<%
    request.getSession().invalidate();
    String queryString = request.getQueryString();
    Map<String, String> idpAuthenticatorMapping = null;
    if (request.getAttribute(Constants.IDP_AUTHENTICATOR_MAP) != null) {
        idpAuthenticatorMapping = (Map<String, String>) request.getAttribute(Constants.IDP_AUTHENTICATOR_MAP);
    }
    
    String errorMessage = "Authentication Failed! Please Retry";
    String authenticationFailed = "false";
    String[] listOfOrgs = null;
    
    if (Boolean.parseBoolean(request.getParameter(Constants.AUTH_FAILURE))) {
        authenticationFailed = "true";
        
        if (request.getParameter(Constants.AUTH_FAILURE_MSG) != null) {
            errorMessage = request.getParameter(Constants.AUTH_FAILURE_MSG);
            
            if (errorMessage.equalsIgnoreCase("authentication.fail.message")) {
                errorMessage = "Authentication Failed! Please Retry";
            }
        }
    }
    if (request.getParameterMap().containsKey("orgList")
            && StringUtils.isNotEmpty(request.getParameter("orgList"))) {
        String organizations = URLDecoder.decode(request.getParameter("orgList").toString(), "UTF-8");
        byte[] tmp =  Base64.getUrlDecoder().decode(organizations);
        organizations = new String(tmp);
        listOfOrgs = organizations.split(",");
    }
%>

<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WSO2 Identity Server</title>
    
    <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
    <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
    
    <!-- <script src="js/scripts.js"></script> -->
    <script src="assets/js/jquery-1.7.1.min.js"></script>
    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <script src="js/respond.min.js"></script>
    <![endif]-->
</head>

<body>

<!-- header -->
<header class="header header-default">
    <div class="container-fluid"><br></div>
    <div class="container-fluid">
        <div class="pull-left brand float-remove-xs text-center-xs">
            <a href="#">
                <img src="images/logo-inverse.svg" alt="wso2" title="wso2" class="logo">
                
                <h1><em>Identity Server</em></h1>
            </a>
        </div>
    </div>
</header>

<!-- page content -->
<div class="container-fluid body-wrapper">
    
    <div class="row">
        <div class="col-md-12">
            
            <!-- content -->
            <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-4 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title blue-bg padding-double white boarder-bottom-blue margin-none">
                        Select Organization to login for the <%=request.getParameter("sp")%> Application &nbsp;
                        &nbsp;</h2>
                </div>
                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <div class="padding-double login-form">
                        <div id="errorDiv"></div>
                        <%
                            if ("true".equals(authenticationFailed)) {
                        %>
                        <div class="alert alert-danger" id="failed-msg">
                            <%=errorMessage%>
                        </div>
                        <% }
                        
                        %>
                        <form id="pin_form" name="pin_form" action="/commonauth"  method="POST">
                            <div id="loginTable1" class="identity-box">
                                <div class="row">
                                    <div class="span6">
                                        <!-- Token Pin -->
                                        <div class="control-group">
                                        
                                        </div>
                                        <input type="hidden" name="sessionDataKey"
                                               value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>
                                        <div class='col-md-12 form-group'>
                                            Allowed Organizations:
                                            <select id="organization">
                                                <option value="select-an-organization">
                                                    Please select an organization
                                                </option>
                                                <%
                                                    if (ArrayUtils.isNotEmpty(listOfOrgs)) {
                                                        for (String selection : listOfOrgs) {
                                                %>
                                                <option value='<%=Encode.forHtmlAttribute(selection)%>'>
                                                    <%=Encode.forHtmlAttribute(selection)%>
                                                </option>
                                                <%
                                                        }
                                                    }
                                                %>    
                                            </select>
                                            <br/><br/>
                                            <div class='col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group'>
                                                <button class='form-control btn btn-primary submit-button' type='submit' onclick="$('#loading').show();">Submit</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                        <script>
                            var form = document.forms[0],
                                r = document.getElementById("organization");

                            form.addEventListener('submit', function(e) {
                                if (r.options[r.selectedIndex].value == null || r.options[r.selectedIndex].value ==
                                    "select-an-organization"){
                                    alert("Please select an organization to login!");
                                    e.preventDefault();
                                }
                            });
                        </script>
                    </div>
                </div>
                <!-- /content -->
            
            </div>
        </div>
        <!-- /content/body -->
    
    </div>
</div>

<!-- footer -->
<footer class="footer">
    <div class="container-fluid">
        <p>WSO2 Identity Server | &copy;
            <script>document.write(new Date().getFullYear());</script>
            <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights Reserved.
        </p>
    </div>
</footer>
<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
</body>
</html>
