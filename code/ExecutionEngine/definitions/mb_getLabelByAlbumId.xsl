<?xml version="1.0" encoding="UTF-8"?>

<!-- Created by Clement on 090524 -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mb="http://musicbrainz.org/ns/mmd-2.0#">
    <xsl:template match="/">
        <RESULT>
            <xsl:for-each select="*[local-name()='metadata']/*[local-name()='label-list']/*[local-name()='label']">
        
                <xsl:text>&#10;</xsl:text>
                <RECORD>
                    <xsl:text>&#10; &#32;</xsl:text>  
                    <ITEM ANGIE-VAR='?albumId' >
                        <xsl:text>NOT DEFINED</xsl:text>
                    </ITEM>                    
                    <xsl:text>&#10; &#32;</xsl:text> 
                    <ITEM ANGIE-VAR='?labelId'>
                        <xsl:value-of select="@id"/>
                    </ITEM>
                    <xsl:text>&#10; &#32;</xsl:text> 
                    <ITEM ANGIE-VAR='?labelName'>
                        <xsl:value-of select="mb:name"/>
                    </ITEM>
                    <xsl:text>&#10; &#32;</xsl:text>  
                    <ITEM ANGIE-VAR='?beginDate' >
                        <xsl:value-of select="mb:life-span/mb:begin"/>
                    </ITEM>                   
                    <xsl:text>&#10;</xsl:text>  
                </RECORD>
               
            </xsl:for-each>  
        </RESULT>
    </xsl:template>
</xsl:stylesheet>
