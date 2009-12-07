<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www-sop.inria.fr/oasis/ProActive/schemas" xmlns:p="http://www-sop.inria.fr/oasis/ProActive/schemas"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:java="http://xml.apache.org/xalan/java"
    exclude-result-prefixes="java"
    version="1.0">
    
    <xsl:param name="nameList"/>
    <xsl:param name="valueList"/>

    <xsl:template match="/">
        <xsl:value-of select="java:org.objectweb.proactive.extensions.gcmdeployment.environment.ReplaceVariables.init($nameList, $valueList)"/>
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="node()" priority="1">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*|text()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text()" priority="2">
        <!--<xsl:value-of select="my:replaceAll(self::node(),1,$valueList)"/>-->
        <xsl:value-of select="java:org.objectweb.proactive.extensions.gcmdeployment.environment.ReplaceVariables.replaceAll(string(self::node()))"/>
    </xsl:template>

    <xsl:template match="@*">
        <!--<xsl:attribute name="{name(.)}" select="my:replaceAll(self::node(),1,$valueList)"/>-->
        <xsl:attribute name="{name(.)}">
            <xsl:value-of select="java:org.objectweb.proactive.extensions.gcmdeployment.environment.ReplaceVariables.replaceAll(string(self::node()))"/>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>
