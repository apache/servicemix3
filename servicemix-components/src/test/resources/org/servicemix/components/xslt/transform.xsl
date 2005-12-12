<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
  version='1.0'>

  <xsl:output method="xml" indent="yes" encoding="ISO-8859-1"/>

  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="attribute::*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="sample">
    <transformed>
      <cheese>
        <xsl:apply-templates select="*|@*"/>
      </cheese>
    </transformed>
  </xsl:template>

</xsl:stylesheet>
