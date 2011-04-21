package org.openmrs.module.rgccd.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.chirdlutil.util.XMLUtil;
import org.openmrs.module.rgccd.Immunization;
import org.openmrs.module.rgccd.db.ImmunizationDAO;
import org.openmrs.module.rgccd.service.ImmunizationService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import _212._0._31._172.immuscheduler.ImmuSchedulerStub;

/**
 * CCD related service implementations
 * 
 * @author Vibha Anand
 */
public class ImmunizationServiceImpl implements ImmunizationService {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private ImmunizationDAO dao;

	/**
	 * Empty constructor
	 */
	public ImmunizationServiceImpl() {
		
	}
	
	/**
	 * @return ImmunizationDAO
	 */
	public ImmunizationDAO getImmunizationDAO() {
		return this.dao;
	}
	
	/**
	 * Sets the DAO for this service. The dao allows interaction with the database.
	 * 
	 * @param dao
	 */
	public void setImmunizationDAO(ImmunizationDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * Call the immunization forecasting service
	 * @see org.openmrs.module.rgccd.service.ImmunizationService#getImmunization(java.lang.String, java.lang.String)
	 */
	public String getImmunization(String inputString,String mrn) throws AxisFault,RemoteException{
		String immunization = null;
			
		//call the immunization service
		ImmuSchedulerStub stub = new ImmuSchedulerStub();
		ImmuSchedulerStub.GetScheduleString getScheduleString = new ImmuSchedulerStub.GetScheduleString();
		
		getScheduleString.setInputString(inputString);
				
		//call the service
		ImmuSchedulerStub.GetScheduleStringResponse res = stub.getScheduleString(getScheduleString);
		
		immunization = res.getGetScheduleStringResult();
		
		return immunization;
	}

	/**
	 * parse the immunization forecasting service response
	 * into an immunization list
	 * @see org.openmrs.module.rgccd.service.ImmunizationService#createImmunizationList(java.lang.String, java.lang.String)
	 */
	public List<Immunization> createImmunizationList(String immunizationListString, String mrn) {
		List<Immunization> immunizations = new ArrayList<Immunization>();
		
		if (immunizationListString == null) {
			log.info("immunization list is null for mrn: " + mrn + " so immunization list could not be created");
			return immunizations;
		}
		
		try {
			
			// Also try to get the DOM built from the original file
			InputStream transformInput = new ByteArrayInputStream(immunizationListString.getBytes());
			
			Document doc = XMLUtil.parseXMLFromInputStream(transformInput);
			
			NodeList tblOutputs = doc.getElementsByTagName("tbl_output");
			
			for (int i = 0; i < tblOutputs.getLength(); i++) {
				Immunization immunization = new Immunization();
				immunizations.add(immunization);
				Node currNode = tblOutputs.item(i);
								
				if (currNode.getNodeType() == Node.ELEMENT_NODE) {
						Element currElement = (Element) currNode;
						NodeList children = currElement.getChildNodes();
							
							for (int k = 0; k < children.getLength(); k++) {
								Node currChild = children.item(k);
								if (currChild.getNodeType() == Node.ELEMENT_NODE) {
									Element childElement = (Element) currChild;
									if (childElement.getNodeName().equals("Vaccine")) {
										immunization.setVaccine(childElement.getTextContent());
									}
									if (childElement.getNodeName().equals("Dose")) {
										try {
	                                        immunization.setDose(Integer.parseInt(childElement.getTextContent()));
                                        }
                                        catch (Exception e) {
                                        }
									}
									if (childElement.getNodeName().equals("PatientID")) {
										try {
	                                        immunization.setPatientId(Integer.parseInt(childElement.getTextContent()));
                                        }
                                        catch (Exception e) {
                                        }
									}
								}
							}
				}
			}													
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (immunizations.size() == 0) {
			log.info("No immunizations found for mrn: " + mrn);
		} 
		return immunizations;
	}
		
}
