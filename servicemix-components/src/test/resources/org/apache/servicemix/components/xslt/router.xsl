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

  <xsl:template match="/*">
    <xsl:choose>

      <!-- lets forward the inbound message to a service -->
      <xsl:when test="@id = '4'">
        <jbi:forward service="foo:trace"/>
      </xsl:when>

      <!-- lets generate the output XML to use as input, copy the input properties and define some new propertes -->
      <xsl:when test="@id = '12'">
        <jbi:invoke service="foo:script">
          <jbi:copyProperties/>
          <jbi:setOutProperty name="foo" select="@sent"/>
          <cheese code="{@id}">
            <description>This is some content generated from the routing XSL</description>
          </cheese>
        </jbi:invoke>
      </xsl:when>

      <xsl:when test="@id != '2'">
        <jbi:forward service="foo:receiver"/>
      </xsl:when>

      <xsl:otherwise>
        <jbi:forward service="foo:trace"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
<!-- END SNIPPET: route -->
