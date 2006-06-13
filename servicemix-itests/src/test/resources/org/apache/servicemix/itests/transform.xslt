<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:jbi="xalan://org.servicemix.components.xslt.XalanExtension"
    extension-element-prefixes="jbi"
    xmlns:my="http://servicemix.org/demo/" version="1.0">
    <xsl:template match="/">
        <ser:simpleMethod xmlns:ser="http://server.simpleexample/">
            <arg0>20</arg0>
            <arg1>2</arg1>
        </ser:simpleMethod>
    </xsl:template>
</xsl:stylesheet>