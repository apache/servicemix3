<?xml version="1.0"?>
<!--

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

-->
<!-- START SNIPPET: route -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jbi="xalan://org.apache.servicemix.components.xslt.XalanExtension"
                extension-element-prefixes="jbi"

                xmlns:foo="http://servicemix.org/cheese/"
                version="1.0">

  <xsl:strip-space elements="*"/>

  <xsl:template match="/">

    <!-- lets invoke a number of services one after the other -->
    <jbi:invoke service="foo:service1">
      <jbi:copyProperties/>
      <jbi:setOutProperty name="foo" select="/sample"/>
      <xsl:copy-of select="/"/>
    </jbi:invoke>

    <jbi:invoke service="foo:service2">
      <jbi:copyProperties/>
      <jbi:setOutProperty name="bar" select="/sample/@id"/>
      <xsl:copy-of select="/"/>
    </jbi:invoke>

    <jbi:invoke service="foo:service3">
      <jbi:copyProperties/>
      <jbi:setOutProperty name="foo" select="string(/sample)"/>
      <jbi:setOutProperty name="bar" select="string(/sample/@id)"/>
      <xsl:copy-of select="/"/>
    </jbi:invoke>

    <jbi:invoke service="foo:receiver">
      <jbi:copyProperties/>
      <jbi:setOutProperty name="bar" select="/sample/@id"/>
      <xsl:copy-of select="/"/>
    </jbi:invoke>

  </xsl:template>

</xsl:stylesheet>
<!-- END SNIPPET: route -->
