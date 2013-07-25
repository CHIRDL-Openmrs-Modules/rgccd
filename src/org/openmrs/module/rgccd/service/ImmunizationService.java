package org.openmrs.module.rgccd.service;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.openmrs.module.rgccd.ImmunizationQueryOutput;
import org.openmrs.module.rgccd.Medication;
import org.springframework.transaction.annotation.Transactional;

/**
 * CCD related services
 * 
 * @author Tammy Dugan
 */
@Transactional
public interface ImmunizationService
{
	public String getImmunization(String inputString,String mrn) throws AxisFault,RemoteException;
	
	public ImmunizationQueryOutput createImmunizationList(String immunizationListString, String mrn);
}