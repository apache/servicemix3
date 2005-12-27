<?xml version="1.0"?>
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
