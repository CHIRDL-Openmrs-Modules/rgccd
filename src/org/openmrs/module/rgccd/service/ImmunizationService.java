package org.openmrs.module.rgccd.service;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.openmrs.module.rgccd.ImmunizationQueryOutput;

/**
 * CCD related services
 * 
 * @author Tammy Dugan
 */
public interface ImmunizationService
{
	public String getImmunization(String inputString,String mrn) throws AxisFault,RemoteException;
	
	public ImmunizationQueryOutput createImmunizationList(String immunizationListString, String mrn);
}