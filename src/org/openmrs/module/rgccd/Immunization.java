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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 */
public class Immunization {
	private Log log = LogFactory.getLog(this.getClass());

	private String vaccine = null;
	private Integer dose = null;
	private Integer patientId = null;
	
	public Immunization(){
		
	}
	
	public Immunization(Immunization immunization){
		this.vaccine=immunization.getVaccine();
		this.dose=immunization.getDose();
		this.patientId=immunization.getPatientId();
	}

	
    /**
     * @return the vaccine
     */
    public String getVaccine() {
    	return this.vaccine;
    }

	
    /**
     * @param vaccine the vaccine to set
     */
    public void setVaccine(String vaccine) {
    	this.vaccine = vaccine;
    }

	
    /**
     * @return the dose
     */
    public Integer getDose() {
    	return this.dose;
    }

	
    /**
     * @param dose the dose to set
     */
    public void setDose(Integer dose) {
    	this.dose = dose;
    }

	
    /**
     * @return the patientId
     */
    public Integer getPatientId() {
    	return this.patientId;
    }

	
    /**
     * @param patientId the patientId to set
     */
    public void setPatientId(Integer patientId) {
    	this.patientId = patientId;
    }

   
}
