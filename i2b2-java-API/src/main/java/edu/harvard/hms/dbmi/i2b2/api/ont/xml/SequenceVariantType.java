/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.i2b2.api.ont.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sequenceVariantType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sequenceVariantType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AaChange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="DnaChange" type="{http://www.i2b2.org/xsd/cell/ont/1.1/}dnaChangeType"/>
 *         &lt;element name="HGVSName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SystematicName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SystematicNameProtein" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sequenceVariantType", propOrder = {
    "aaChange",
    "dnaChange",
    "hgvsName",
    "systematicName",
    "systematicNameProtein"
})
public class SequenceVariantType {

    @XmlElement(name = "AaChange", required = true)
    protected String aaChange;
    @XmlElement(name = "DnaChange", required = true)
    protected DnaChangeType dnaChange;
    @XmlElement(name = "HGVSName", required = true)
    protected String hgvsName;
    @XmlElement(name = "SystematicName", required = true)
    protected String systematicName;
    @XmlElement(name = "SystematicNameProtein", required = true)
    protected String systematicNameProtein;

    /**
     * Gets the value of the aaChange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAaChange() {
        return aaChange;
    }

    /**
     * Sets the value of the aaChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAaChange(String value) {
        this.aaChange = value;
    }

    /**
     * Gets the value of the dnaChange property.
     * 
     * @return
     *     possible object is
     *     {@link DnaChangeType }
     *     
     */
    public DnaChangeType getDnaChange() {
        return dnaChange;
    }

    /**
     * Sets the value of the dnaChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link DnaChangeType }
     *     
     */
    public void setDnaChange(DnaChangeType value) {
        this.dnaChange = value;
    }

    /**
     * Gets the value of the hgvsName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHGVSName() {
        return hgvsName;
    }

    /**
     * Sets the value of the hgvsName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHGVSName(String value) {
        this.hgvsName = value;
    }

    /**
     * Gets the value of the systematicName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystematicName() {
        return systematicName;
    }

    /**
     * Sets the value of the systematicName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystematicName(String value) {
        this.systematicName = value;
    }

    /**
     * Gets the value of the systematicNameProtein property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystematicNameProtein() {
        return systematicNameProtein;
    }

    /**
     * Sets the value of the systematicNameProtein property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystematicNameProtein(String value) {
        this.systematicNameProtein = value;
    }

}
