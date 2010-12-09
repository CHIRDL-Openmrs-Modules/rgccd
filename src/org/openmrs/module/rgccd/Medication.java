/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rgccd;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonName;


/**
 *
 */
public class Medication {
	private Log log = LogFactory.getLog(this.getClass());

	private String ndcName = null;
	private String regenstriefName = null;
	private String name = null;
	private PersonName prescriberName = null;
	private String prescriberNumber = null;
	private Date dispenseDate = null;
	private String quantity = null;
	private String units = null;
	private String rxNormName = null;
	private String originalText = null;
	private String sig = null;
	
	public Medication(){
		
	}
	
	
	public Medication(Medication medication){
		this.ndcName = medication.getNdcName();
		this.regenstriefName = medication.getRegenstriefName();
		this.name = medication.getName();
		this.prescriberName = new PersonName(medication.getPrescriberGivenName(), null, medication.getPrescriberFamilyName());
		this.prescriberNumber = medication.getPrescriberNumber();
		this.dispenseDate = medication.getDispenseDate();
		this.quantity = medication.getQuantity();
		this.units = medication.getUnits();
		this.rxNormName = medication.getRxNormName();
		this.originalText = medication.getOriginalText();
		this.sig = medication.getSig();
	}
	
    /**
     * @return the ndcName
     */
    public String getNdcName() {
    	return this.ndcName;
    }
	
    /**
     * @param ndcName the ndcName to set
     */
    public void setNdcName(String ndcName) {
    	this.ndcName = ndcName;
    }
	
    /**
     * @return the regenstriefName
     */
    public String getRegenstriefName() {
    	return this.regenstriefName;
    }
	
    /**
     * @param regenstriefName the regenstriefName to set
     */
    public void setRegenstriefName(String regenstriefName) {
    	this.regenstriefName = regenstriefName;
    }
	
    /**
     * @return the name
     */
    public String getName() {
    	return this.name;
    }
	
    /**
     * @param name the name to set
     */
    public void setName(String name) {
    	this.name = name;
    }
	
    /**
     * @return the prescriberNumber
     */
    public String getPrescriberNumber() {
    	return this.prescriberNumber;
    }
	
    /**
     * @param prescriberNumber the prescriberNumber to set
     */
    public void setPrescriberNumber(String prescriberNumber) {
    	this.prescriberNumber = prescriberNumber;
    }
	
    /**
     * @return the dispenseDate
     */
    public Date getDispenseDate() {
    	return this.dispenseDate;
    }
	
    /**
     * @param dispenseDate the dispenseDate to set
     */
    public void setDispenseDate(String dispenseDate) {
    	
    	try {
	        this.dispenseDate = getLocalDate(dispenseDate);
        }
        catch (ParseException e) {
	        log.error("Error generated", e);
        }
    }
    
    //code adapted from http://download.oracle.com/javase/6/docs/technotes/guides/jmx/examples/Lookup/ldap/Client.java
	public static Date getLocalDate(String dateString) throws java.text.ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.S");
		Date localDate = formatter.parse(dateString);
		if (dateString.endsWith("Z")) {
			Date date = new Date();
			if (formatter.getCalendar().getTimeZone().inDaylightTime(date))
				localDate = new Date(localDate.getTime() + formatter.getCalendar().getTimeZone().getRawOffset()
				        + formatter.getCalendar().getTimeZone().getDSTSavings());
			else
				localDate = new Date(localDate.getTime() + formatter.getCalendar().getTimeZone().getRawOffset());
		}
		return localDate;
	}

	
    /**
     * @return the quantity
     */
    public String getQuantity() {
    	return this.quantity;
    }
	
    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(String quantity) {
    	this.quantity = quantity;
    }
	
    /**
     * @return the units
     */
    public String getUnits() {
    	return this.units;
    }
	
    /**
     * @param units the units to set
     */
    public void setUnits(String units) {
    	this.units = units;
    }

	
    /**
     * @return the rxNormName
     */
    public String getRxNormName() {
    	return this.rxNormName;
    }

	
    /**
     * @param rxNormName the rxNormName to set
     */
    public void setRxNormName(String rxNormName) {
    	this.rxNormName = rxNormName;
    }

	
    /**
     * @return the originalText
     */
    public String getOriginalText() {
    	return this.originalText;
    }

	
    /**
     * @param originalText the originalText to set
     */
    public void setOriginalText(String originalText) {
    	this.originalText = originalText;
    }

	
    /**
     * @return the givenName
     */
    public String getPrescriberGivenName() {
    	if(this.prescriberName != null){
    		return this.prescriberName.getGivenName();
    	}
    	return null;
    }

	
    /**
     * @param givenName the givenName to set
     */
    public void setPrescriberGivenName(String givenName) {
    	if(this.prescriberName == null){
    		this.prescriberName = new PersonName();
    	}
    	this.prescriberName.setGivenName(givenName);
    }

	
    /**
     * @return the familyName
     */
    public String getPrescriberFamilyName() {
    	if(this.prescriberName != null){
    		return this.prescriberName.getFamilyName();
    	}
    	return null;
    }

	
    /**
     * @param familyName the familyName to set
     */
    public void setPrescriberFamilyName(String familyName) {
    	if(this.prescriberName == null){
    		this.prescriberName = new PersonName();
    	}
    	this.prescriberName.setFamilyName(familyName);
    }

	
    /**
     * @return the sig
     */
    public String getSig() {
    	return this.sig;
    }

	
    /**
     * @param sig the sig to set
     */
    public void setSig(String sig) {
    	this.sig = sig;
    }
   
}
