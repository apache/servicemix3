<?xml version="1.0"?>
<!-- START SNIPPET: example -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jbi="xalan://org.apache.servicemix.components.xslt.XalanExtension"
                extension-element-prefixes="jbi"
                xmlns:foo="http://servicemix.org/cheese/" version="1.0">

  <xsl:template match="/">

    <!-- lets pass a new message body -->
    <jbi:invoke service="foo:service1">
      <cheese id="{/foo/@id}">Edam</cheese>
    </jbi:invoke>

    <!--  lets split the message  -->
    <jbi:invoke service="foo:service2">
      <xsl:copy-of select="/foo/beer"/>
    </jbi:invoke>

    <!-- 1-many split -->
    <xsl:for-each select="/foo/lineitem">
      <jbi:invoke service="foo:service3">
        <xsl:copy-of select="."/>
      </jbi:invoke>
    </xsl:for-each>

    <!--  pass the  entire message to the final endpoint  -->
    <jbi:invoke service="foo:receiver">
      <jbi:copyProperties/>
      <jbi:setOutProperty name="bar" select="/sample/@id"/>
      <xsl:copy-of select="/"/>
    </jbi:invoke>

  </xsl:template>
</xsl:stylesheet>
<!-- END SNIPPET: example -->
