<?xml version="1.0" encoding="UTF-8"?>

<!-- Created by Clement on 090524 -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mb="http://musicbrainz.org/ns/mmd-1.0#">
    <xsl:template match="/">
        <RESULT>
            <xsl:for-each select="*[local-name()='o']/*[local-name()='results']/*[local-name()='e']">
        
                <xsl:text>&#10;</xsl:text>
                <RECORD>
                    <xsl:text>&#10; &#32;</xsl:text>  
                    <ITEM ANGIE-VAR='?artistName'>
                        <xsl:value-of select="name"/>
                    </ITEM>
                    <xsl:text>&#10; &#32;</xsl:text>  
                    <ITEM ANGIE-VAR='?artistId'>
                        <xsl:value-of select="id"/>
                    </ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> 
                    <ITEM ANGIE-VAR='?beginDate' >
                        
                    </ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> 
                    <ITEM ANGIE-VAR='?endDate'>
                        
                    </ITEM>                   
                </RECORD>
               
            </xsl:for-each>  
        </RESULT>
    </xsl:template>
</xsl:stylesheet>
