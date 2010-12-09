package org.openmrs.module.rgccd.service;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.openmrs.module.rgccd.Medication;
import org.springframework.transaction.annotation.Transactional;

/**
 * CCD related services
 * 
 * @author Vibha Anand
 */
@Transactional
public interface CcdService
{
	public String getCcdByMRN(String mrn, String providerId,Integer locationId) throws AxisFault,RemoteException;
	
	public String getCcdByHL7(String mrn, Integer locationId, String hl7) throws AxisFault,RemoteException;
	
	public List<Medication> createMedicationList(String ccd,String mrn);

	public void transformCCDToHTML(String ccd,String outputFile);

}