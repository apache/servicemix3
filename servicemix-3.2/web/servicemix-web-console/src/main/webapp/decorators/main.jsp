<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%--
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
   
    http://www.apache.org/licenses/LICENSE-2.0
   
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
--%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <title><decorator:title default="ServiceMix Console" /></title>
    <style type="text/css" media="screen">
        @import url(<c:url value="/styles/style.css"/>);
        @import url(<c:url value="/styles/sorttable.css"/>);
    </style>
    <c:if test="${!disableJavaScript}">
        <script type='text/javascript' src='<c:url value="/js/common.js"/>'></script>
        <script type='text/javascript' src='<c:url value="/js/css.js"/>'></script>
        <script type='text/javascript' src='<c:url value="/js/standardista-table-sorting.js"/>'></script>
    </c:if>

    <decorator:head />
</head>

<body>

<div id="wrapper-menu-top">
    <div id="menu-top">
    </div>
    <!--menu-top-->
</div>
<!--wrapper-menu-top-->

<div id="wrapper-header">
    <div id="header">
        <div id="wrapper-header2">
            <div id="wrapper-header3">
              <div id="wrapper-header4">
                <div><div>ServiceMix Console</div></div>
              </div>
            </div>
        </div>
    </div>
</div>

<div id="wrapper-content">
    <div id="wrapper-menu-page">
        <div id="menu-page">
          <ul>
              <li><a href="<c:url value='/index.jsp'/>" title="Home<"><span>Home</span></a></li>
              <li><a href="<c:url value='/components.jsp'/>" title="Components"><span>Components</span></a></li>
              <li><a href="<c:url value='/shared-libraries.jsp'/>" title="Shared Libraries"><span>Shared Libraries</span></a></li>
              <li><a href="<c:url value='/service-assemblies.jsp'/>" title="Service Assemblies"><span>Service Assemblies</span></a></li>
              <li><a href="<c:url value='/service-units.jsp'/>" title="Service Units"><span>Service Units</span></a></li>
              <li><a href="<c:url value='/endpoints.jsp'/>" title="Endpoints"><span>Endpoints</span></a></li>
              <li><a href="<c:url value='/audit.jsp'/>" title="Audit"><span>Audit</span></a></li>
              <li><a href="<c:url value='/view.jsp'/>" title="View"><span>View</span></a></li>
          </ul>
          <br/>
          <br/>
          <br/>
          <br/>
          <br/>
          <small>&copy; 2007 The Apache Software Foundation</small>
        </div>
    </div>

    <div id="content">
        <decorator:body/>

    </div>
</div>

<div id="wrapper-footer">
    <div id="footer">
    </div>

</div>

</body>
</html>

