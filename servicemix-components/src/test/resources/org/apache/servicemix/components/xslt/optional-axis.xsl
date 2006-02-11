<?xml version="1.0"?>
<!-- START SNIPPET: route -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:jbi="xalan://org.apache.servicemix.components.xslt.XalanExtension"
    extension-element-prefixes="jbi"

    xmlns:foo="http://servicemix.org/cheese/"

    xmlns:prod="http://foo.org/prod" xmlns:ms="http://foo.org/ms" xmlns:xsi="http://foo.org/msi"

    version="1.0">

    <!--xsl:strip-space elements="*"/-->
    <xsl:template match="/">

        <jbi:invoke service="foo:service1">
            <jbi:copyProperties/>
            <jbi:setOutProperty name="foo" select="prod:resolveItemXrefResponse/prod:response/ms:Values/ms:Item[position() = 1]"/>
            <xsl:copy-of select="/"/>
        </jbi:invoke>

        <jbi:invoke service="foo:service2">
            <jbi:copyProperties/>
            <jbi:setOutProperty name="foo" select="prod:resolveItemXrefResponse/prod:response/ms:Values/ms:Item[position() = 2]"/>
            <xsl:copy-of select="/"/>
        </jbi:invoke>

        <jbi:invoke service="foo:service3">
            <jbi:copyProperties/>
            <jbi:setOutProperty name="foo" select="prod:resolveItemXrefResponse/prod:response/ms:Values/ms:Item[position() = 3]"/>
            <xsl:copy-of select="/"/>
        </jbi:invoke>

        <jbi:invoke service="foo:service4">
            <jbi:copyProperties/>
            <jbi:setOutProperty name="foo" select="prod:resolveItemXrefResponse/prod:response/ms:Values/ms:Item[position() = 4]"/>
            <xsl:copy-of select="/"/>
        </jbi:invoke>

        <jbi:invoke service="foo:service5">
            <jbi:copyProperties/>
            <jbi:setOutProperty name="foo" select="count(prod:resolveItemXrefResponse/prod:response/ms:Keys/ms:Item[text()='MSTR']/preceding-sibling::*) + 1"/>
            <xsl:copy-of select="/"/>
        </jbi:invoke>

        <jbi:invoke service="foo:service6">
            <jbi:copyProperties/>
            <jbi:setOutProperty name="foo" select="prod:resolveItemXrefResponse/prod:response/ms:Values/ms:Item[position() = count(prod:resolveItemXrefResponse/prod:response/ms:Keys/ms:Item[text()='MSTR']/preceding-sibling::*) + 1]"/>
            <xsl:copy-of select="/"/>
        </jbi:invoke>

        <jbi:invoke service="foo:receiver">
            <jbi:copyProperties/>
            <jbi:setOutProperty name="bar" select="/"/>
            <xsl:copy-of select="/"/>
        </jbi:invoke>

    </xsl:template>
    <!-- /prod:resolveItemXrefResponse/prod:response/ms:Values/ms:Item[position()=count(/prod:resolveItemXrefResponse/prod:response/ms:Keys/ms:Item[text()='MSTR']/preceding-sibling::*) + 1])-->
</xsl:stylesheet>
<!-- END SNIPPET: route -->