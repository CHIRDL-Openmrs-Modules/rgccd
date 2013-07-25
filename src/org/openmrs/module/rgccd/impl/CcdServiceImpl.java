package org.openmrs.module.rgccd.impl;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutil.util.XMLUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.rgccd.Medication;
import org.openmrs.module.rgccd.MedicationListComparator;
import org.openmrs.module.rgccd.db.CcdDAO;
import org.openmrs.module.rgccd.service.CcdService;
import org.regenstrief.www.services.HL7CcdServiceStub;
import org.regenstrief.www.services.MRNCcdServiceStub;
import org.regenstrief.www.services.MRNCcdServiceStub.EntityIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * CCD related service implementations
 * 
 * @author Vibha Anand
 */
public class CcdServiceImpl implements CcdService {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private CcdDAO dao;
	
	private final String USER_TYPE = "SPIN Users";
	
	private final String PROVIDER_TYPE = "WSH_ALL_PP_MASTER";
	
	private final String MRN_TYPE = "Wishard Memorial Hospital Medical Record Numbers";
	
	/**
	 * Empty constructor
	 */
	public CcdServiceImpl() {
		
	}
	
	/**
	 * @return CcdDAO
	 */
	public CcdDAO getCcdDAO() {
		return this.dao;
	}
	
	/**
	 * Sets the DAO for this service. The dao allows interaction with the database.
	 * 
	 * @param dao
	 */
	public void setCcdDAO(CcdDAO dao) {
		this.dao = dao;
	}
	
	public String getCcdByMRN(String mrn, String providerId, Integer locationId) throws AxisFault,RemoteException{
		String ccd = null;
		String username = null;
		
		AdministrationService as = Context.getAdministrationService();
		
		// Set username and password
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		LocationAttributeValue locAttrValue = chirdlUtilBackportsService.getLocationAttributeValue(locationId,
		    "medicationListQueryUser");
		if (locAttrValue != null) {
			username = locAttrValue.getValue();
		}
		
		if (username == null) {
			this.log.error("Could not find medicationListQueryUser for location: " + locationId);
			return null;
		}
		String passWord = as.getGlobalProperty("rgccd.password");
		
		// Invoke service
		if (providerId == null) {
			log.info("Could not query medication list for mrn: " + mrn + " because provider ID is null");
			return null;
		}
		
		providerId = Util.removeLeadingZeros(providerId); //remove trailing zeros because regenstrief ids don't have them
		
		String mrnCcdServiceUrl = as.getGlobalProperty("rgccd.mrnServiceUrl");
		if (mrnCcdServiceUrl == null) {
			log.error("No value found for global property rgccd.mrnServiceUrl");
			return null;
		}
		
		//call the ccd service
//		MRNCcdServiceStub stub = new MRNCcdServiceStub("https://phoenix.regenstrief.org:8443/NHIN/services/MRNRequestForCCD");
		MRNCcdServiceStub stub = new MRNCcdServiceStub(mrnCcdServiceUrl);
		
		MRNCcdServiceStub.GetCcd getCcd = new MRNCcdServiceStub.GetCcd();
		MRNCcdServiceStub.GetCcdE req = new MRNCcdServiceStub.GetCcdE();
		
		//set the parameters for the service
		EntityIdentifier identifier = new EntityIdentifier();
		identifier.setId(username);
		identifier.setSystem(USER_TYPE);
		getCcd.setUser(identifier);
		
		getCcd.setPassword(passWord);
		
		identifier = new EntityIdentifier();
		identifier.setId(providerId);
		identifier.setSystem(PROVIDER_TYPE);
		getCcd.setDoctor(identifier);
		
		identifier = new EntityIdentifier();
		identifier.setId(mrn);
		identifier.setSystem(MRN_TYPE);
		getCcd.setPatient(identifier);
		
		req.setGetCcd(getCcd);
		
		//call the service
		MRNCcdServiceStub.GetCcdResponseE res = stub.getCcd(req);
		
		ccd = res.getGetCcdResponse().getCcd();
		
		//write the ccd to a file
		InputStream input = new ByteArrayInputStream(ccd.getBytes());
		String ccdDirectory = IOUtil.formatDirectoryName(as.getGlobalProperty("rgccd.ccdDirectory"));
		
		try {
	        OutputStream output = new FileOutputStream(ccdDirectory + "ccd_" + Util.archiveStamp() + "_" + mrn + ".xml");
	        IOUtil.bufferedReadWrite(input, output);
        }
        catch (Exception e) {
        	log.error("",e);
        	return null;
        }
		return ccd;
	}
	
	public String getCcdByHL7(String mrn, Integer locationId, String hl7)throws AxisFault,RemoteException {
		String ccd = null;
		String username = null;
		
		AdministrationService as = Context.getAdministrationService();
		
		// Set username and password
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		LocationAttributeValue locAttrValue = chirdlUtilBackportsService.getLocationAttributeValue(locationId,
		    "medicationListQueryUser");
		if (locAttrValue != null) {
			username = locAttrValue.getValue();
		}
		
		if (username == null) {
			this.log.error("Could not find medicationListQueryUser for location: " + locationId);
			return null;
		}
		String passWord = as.getGlobalProperty("rgccd.password");
		
		String hl7CcdServiceUrl = as.getGlobalProperty("rgccd.hl7ServiceUrl");
		if (hl7CcdServiceUrl == null) {
			log.error("No value found for global property rgccd.hl7ServiceUrl");
			return null;
		}
		
		//call the ccd service
//		HL7CcdServiceStub stub = new HL7CcdServiceStub("https://phoenix.regenstrief.org:8443/NHIN/services/getCcd");
		HL7CcdServiceStub stub = new HL7CcdServiceStub(hl7CcdServiceUrl);
		
		HL7CcdServiceStub.GetCcd getCcd = new HL7CcdServiceStub.GetCcd();
		HL7CcdServiceStub.GetCcdE req = new HL7CcdServiceStub.GetCcdE();
		
		//set the parameters for the service
		getCcd.setHl7(hl7);
		getCcd.setPassword(passWord);
		getCcd.setUserNameSystem(USER_TYPE);
		getCcd.setUserName(username);
		
		req.setGetCcd(getCcd);
		
		//call the service
		HL7CcdServiceStub.GetCcdResponseE res = stub.getCcd(req);
		
		ccd = res.getGetCcdResponse().getCcd();
		
		//write the ccd to a file
		InputStream input = new ByteArrayInputStream(ccd.getBytes());
		String ccdDirectory = IOUtil.formatDirectoryName(as.getGlobalProperty("rgccd.ccdDirectory"));
		
		try {
	        OutputStream output = new FileOutputStream(ccdDirectory + "ccd_" + Util.archiveStamp() + "_" + mrn + ".xml");
	        IOUtil.bufferedReadWrite(input, output);
        }
        catch (Exception e) {
           log.error("",e);
	       return null;
        }
		return ccd;
	}
	
	public List<Medication> createMedicationList(String ccd, String mrn) {
		List<Medication> drugs = new ArrayList<Medication>();
		
		if (ccd == null) {
			log.info("CCD is null for mrn: " + mrn + " so medication list could not be created");
			return drugs;
		}
		
		try {
			
			// Also try to get the DOM built from the original file
			InputStream transformInput = new ByteArrayInputStream(ccd.getBytes());
			
			Document doc = XMLUtil.parseXMLFromInputStream(transformInput);
			
			NodeList contentTags = doc.getElementsByTagName("content");
			HashMap<String,String> sigMap = new HashMap<String,String>();
			
			for(int i = 0; i <contentTags.getLength();i++){
				Node currChild = contentTags.item(i);
				Element childElement = (Element) currChild;
				String idAttribute = childElement.getAttribute("ID");
				if(idAttribute != null&&idAttribute.length()>0){
					sigMap.put(idAttribute, currChild.getTextContent());
				}
			}
			
			NodeList subAdmin = doc.getElementsByTagName("substanceAdministration");
			
			for (int i = 0; i < subAdmin.getLength(); i++) {
				Medication drug = new Medication();
				drugs.add(drug);
				Node currChild = subAdmin.item(i);
				
				NodeList children1 = currChild.getChildNodes();
				
				for (int j = 0; j < children1.getLength(); j++) {
					currChild = children1.item(j);
					
					if (currChild.getNodeType() == Node.ELEMENT_NODE) {
						Element childElement = (Element) currChild;
						//Set the medication names
						if (childElement.getNodeName().equals("consumable")) {
							
							NodeList children2 = currChild.getChildNodes();
							
							for (int k = 0; k < children2.getLength(); k++) {
								currChild = children2.item(k);
								if (currChild.getNodeType() == Node.ELEMENT_NODE) {
									childElement = (Element) currChild;
									if (childElement.getNodeName().equals("manufacturedProduct")) {
										
										NodeList children3 = currChild.getChildNodes();
										
										for (int l = 0; l < children3.getLength(); l++) {
											currChild = children3.item(l);
											if (currChild.getNodeType() == Node.ELEMENT_NODE) {
												childElement = (Element) currChild;
												if (childElement.getNodeName().equals("manufacturedMaterial")) {
													
													NodeList children4 = currChild.getChildNodes();
													
													for (int m = 0; m < children4.getLength(); m++) {
														currChild = children4.item(m);
														if (currChild.getNodeType() == Node.ELEMENT_NODE) {
															childElement = (Element) currChild;
															if (childElement.getNodeName().equals("code")) {
																
																if (childElement.getAttribute("codeSystemName")
																        .equals("NDC")) {
																	drug
																	        .setNdcName(childElement
																	                .getAttribute("displayName"));
																}
																
																if (childElement.getAttribute("codeSystemName").equals(
																    "Local Concept")) {
																	drug.setRegenstriefName(childElement
																	        .getAttribute("displayName"));
																}
																
																if (childElement.getAttribute("codeSystemName").equals(
																    "RxNorm Concept Unique Identifier")) {
																	drug.setRxNormName(childElement
																	        .getAttribute("displayName"));
																}
																
																NodeList children5 = currChild.getChildNodes();
																
																for (int n = 0; n < children5.getLength(); n++) {
																	currChild = children5.item(n);
																	if (currChild.getNodeType() == Node.ELEMENT_NODE) {
																		childElement = (Element) currChild;
																		if (childElement.getNodeName().equals("translation")) {
																			
																			if (childElement.getAttribute("codeSystemName")
																			        .equals("NDC")) {
																				drug.setNdcName(childElement
																				        .getAttribute("displayName"));
																			}
																			
																			if (childElement.getAttribute("codeSystemName")
																			        .equals("Local Concept")) {
																				drug.setRegenstriefName(childElement
																				        .getAttribute("displayName"));
																			}
																			
																			if (childElement.getAttribute("codeSystemName")
																			        .equals(
																			            "RxNorm Concept Unique Identifier")) {
																				drug.setRxNormName(childElement
																				        .getAttribute("displayName"));
																			}
																		}
																		if (childElement.getNodeName()
																		        .equals("originalText")) {
																			drug.setOriginalText(childElement
																			        .getTextContent());
																			
																		}
																	}
																}
															}
															if (childElement.getNodeName().equals("name")) {
																drug.setName(childElement.getTextContent());
															}
														}
													}
												}
											}
										}
									}
								}
								
							}
						}
						if (childElement.getNodeName().equals("entryRelationship")) {
							
							if (childElement.getAttribute("typeCode").equals("REFR")) {
								NodeList children2 = currChild.getChildNodes();
								
								for (int k = 0; k < children2.getLength(); k++) {
									currChild = children2.item(k);
									if (currChild.getNodeType() == Node.ELEMENT_NODE) {
										childElement = (Element) currChild;
										if (childElement.getNodeName().equals("supply")) {
											NodeList children3 = currChild.getChildNodes();
											
											for (int l = 0; l < children3.getLength(); l++) {
												currChild = children3.item(l);
												if (currChild.getNodeType() == Node.ELEMENT_NODE) {
													childElement = (Element) currChild;
													//set the quantity and units
													if (childElement.getNodeName().equals("quantity")) {
														
														drug.setUnits(childElement.getAttribute("unit"));
													}
													if (childElement.getNodeName().equals("author")) {
														NodeList children4 = currChild.getChildNodes();
														
														for (int m = 0; m < children4.getLength(); m++) {
															currChild = children4.item(m);
															if (currChild.getNodeType() == Node.ELEMENT_NODE) {
																childElement = (Element) currChild;
																if (childElement.getNodeName().equals("assignedAuthor")) {
																	NodeList children5 = currChild.getChildNodes();
																	
																	for (int n = 0; n < children5.getLength(); n++) {
																		currChild = children5.item(n);
																		if (currChild.getNodeType() == Node.ELEMENT_NODE) {
																			childElement = (Element) currChild;
																			//set the prescriber number
																			if (childElement.getNodeName().equals("id")) {
																				drug.setPrescriberNumber(childElement
																				        .getAttribute("extension"));
																			}
																			if (childElement.getNodeName().equals(
																			    "assignedPerson")) {
																				NodeList children6 = currChild
																				        .getChildNodes();
																				
																				for (int o = 0; o < children6.getLength(); o++) {
																					currChild = children6.item(o);
																					if (currChild.getNodeType() == Node.ELEMENT_NODE) {
																						childElement = (Element) currChild;
																						if (childElement.getNodeName()
																						        .equals("name")) {
																							NodeList children7 = currChild
																							        .getChildNodes();
																							
																							for (int p = 0; p < children7
																							        .getLength(); p++) {
																								currChild = children7
																								        .item(p);
																								if (currChild.getNodeType() == Node.ELEMENT_NODE) {
																									childElement = (Element) currChild;
																									//set the prescriber name
																									if (childElement
																									        .getNodeName()
																									        .equals("given")) {
																										drug
																										        .setPrescriberGivenName(childElement
																										                .getTextContent());
																									}
																									if (childElement
																									        .getNodeName()
																									        .equals("family")) {
																										drug
																										        .setPrescriberFamilyName(childElement
																										                .getTextContent());
																									}
																								}
																							}
																						}
																					}
																				}
																			}
																		}
																	}
																}
															}
															
														}
														
													}
												}
											}
										}
									}
								}
							}
							//set the dispense date
							if (childElement.getAttribute("typeCode").equals("COMP")) {
								NodeList children2 = currChild.getChildNodes();
								
								for (int k = 0; k < children2.getLength(); k++) {
									currChild = children2.item(k);
									if (currChild.getNodeType() == Node.ELEMENT_NODE) {
										childElement = (Element) currChild;
										if (childElement.getNodeName().equals("supply")) {
											NodeList children3 = currChild.getChildNodes();
											
											for (int l = 0; l < children3.getLength(); l++) {
												currChild = children3.item(l);
												if (currChild.getNodeType() == Node.ELEMENT_NODE) {
													childElement = (Element) currChild;
													if (childElement.getNodeName().equals("effectiveTime")) {
														drug.setDispenseDate(childElement.getAttribute("value"));
													} else if (childElement.getNodeName().equals("quantity")) {
														drug.setQuantity(childElement.getAttribute("value"));
														drug.setUnits(childElement.getAttribute("unit"));
													}
												}
											}
										}
									}
								}
							}
							//set the sig
							if (childElement.getAttribute("typeCode").equals("SUBJ")) {
								NodeList children2 = currChild.getChildNodes();
								
								for (int k = 0; k < children2.getLength(); k++) {
									currChild = children2.item(k);
									if (currChild.getNodeType() == Node.ELEMENT_NODE) {
										childElement = (Element) currChild;
										if (childElement.getNodeName().equals("act")) {
											NodeList children3 = currChild.getChildNodes();
											
											for (int l = 0; l < children3.getLength(); l++) {
												currChild = children3.item(l);
												if (currChild.getNodeType() == Node.ELEMENT_NODE) {
													childElement = (Element) currChild;
													if (childElement.getNodeName().equals("text")) {
														NodeList children4 = currChild.getChildNodes();
														
														for (int m = 0; m < children4.getLength(); m++) {
															currChild = children4.item(m);
															if (currChild.getNodeType() == Node.ELEMENT_NODE) {
																childElement = (Element) currChild;
																if (childElement.getNodeName().equals("reference")) {
																	String sigLink = childElement.getAttribute("value");
																	sigLink = sigLink.substring(1); //remove # from beginning
																	drug.setSig(sigMap.get(sigLink));
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			//set the supply for each medication
			NodeList sections = doc.getElementsByTagName("section");
			Node targetSection = null;
			//find the Medications table section
			for (int i = 0; i < sections.getLength(); i++) {
				
				Node currChild = sections.item(i);
				
				NodeList children1 = currChild.getChildNodes();
				
				for (int j = 0; j < children1.getLength(); j++) {
					currChild = children1.item(j);
					
					if (currChild.getNodeType() == Node.ELEMENT_NODE) {
						Element childElement = (Element) currChild;
						
						if (childElement.getNodeName().equals("title")&&
								childElement.getTextContent().equals("Medications")) {
							targetSection=sections.item(i);
							break;
						}
					}
				}
			}
			if (targetSection != null) {
				//find the text tag in the target section
				NodeList children1 = targetSection.getChildNodes();
				for (int j = 0; j < children1.getLength(); j++) {
					Node currChild = children1.item(j);
					
					if (currChild.getNodeType() == Node.ELEMENT_NODE) {
						Element childElement = (Element) currChild;
						
						if (childElement.getNodeName().equals("text")) {
							targetSection = currChild;
							break;
						}
					}
					
				}
				//find the table tag in the target section
				children1 = targetSection.getChildNodes();
				for (int j = 0; j < children1.getLength(); j++) {
					Node currChild = children1.item(j);
					
					if (currChild.getNodeType() == Node.ELEMENT_NODE) {
						Element childElement = (Element) currChild;
						
						if (childElement.getNodeName().equals("table")) {
							targetSection = currChild;
							break;
						}
					}
					
				}
				//find the table tag in the target section
				children1 = targetSection.getChildNodes();
				for (int j = 0; j < children1.getLength(); j++) {
					Node currChild = children1.item(j);
					
					if (currChild.getNodeType() == Node.ELEMENT_NODE) {
						Element childElement = (Element) currChild;
						
						if (childElement.getNodeName().equals("tbody")) {
							targetSection = currChild;
							break;
						}
					}
					
				}
				Node tbodySection = targetSection;
				//look through the tbody rows to find the row for the correct medication
				children1 = tbodySection.getChildNodes();
				for (int j = 0; j < children1.getLength(); j++) {
					Node currChild = children1.item(j);
					
					if (currChild.getNodeType() == Node.ELEMENT_NODE) {
						Element childElement = (Element) currChild;
						
						if (childElement.getNodeName().equals("tr")) {
							NodeList children2 = currChild.getChildNodes();
							String drugName = null;
							String strength = null;
							String quantity = null;
							String dispenseDate = null;
							for (int k = 0; k < children2.getLength(); k++) {
								currChild = children2.item(k);
								if (currChild.getNodeType() == Node.ELEMENT_NODE) {
									childElement = (Element) currChild;
									
									if (childElement.getNodeName().equals("td")) {
										NodeList children3 = currChild.getChildNodes();
										
										for (int l = 0; l < children3.getLength(); l++) {
											currChild = children3.item(l);
											
											if (currChild.getNodeType() == Node.ELEMENT_NODE) {
												childElement = (Element) currChild;
												
												if (childElement.getNodeName().equals("content")
												        && childElement.getAttribute("ID").startsWith("Medications-drug")) {
													
													drugName = childElement.getTextContent();
												}
												if (childElement.getNodeName().equals("content")
												        && childElement.getAttribute("ID").startsWith("Medications-dispense-date")) {
													
													dispenseDate = childElement.getTextContent();
												}
												if (childElement.getNodeName().equals("content")
												        && childElement.getAttribute("ID")
												                .startsWith("Medications-strength")) {
													strength = childElement.getTextContent();
												}
												if (childElement.getNodeName().equals("content")
												        && childElement.getAttribute("ID")
												                .startsWith("Medications-quantity")) {
													quantity = childElement.getTextContent();
												}
											}
										}

									}
								}
							}
							String pattern = "dd-MMM-yyyy";

							SimpleDateFormat dateForm = new SimpleDateFormat(pattern);
							
							for (Medication currDrug : drugs) {
								if (currDrug.getRegenstriefName()!=null&&
										currDrug.getRegenstriefName().equalsIgnoreCase(drugName)&&
										dateForm.format(currDrug.getDispenseDate()).equals(dispenseDate)) {
									if(currDrug.getStrength() == null || currDrug.getQuantity() == null){
										currDrug.setStrength(strength);
										currDrug.setQuantity(quantity);
										break;
									}
								}
								if (currDrug.getNdcName()!=null&&
										currDrug.getNdcName().equalsIgnoreCase(drugName)&&
										dateForm.format(currDrug.getDispenseDate()).equals(dispenseDate)) {
									if(currDrug.getStrength() == null || currDrug.getQuantity() == null){
										currDrug.setStrength(strength);
										currDrug.setQuantity(quantity);
										break;
									}
								}
							}
						}
					}
					
				}
			}
		}
		catch (Exception e) {
			log.error("Error parsing CCD for Medications", e);
		}
		
		if (drugs.size() == 0) {
			log.info("No medications found for mrn: " + mrn);
		} else {
			printMeds(drugs,mrn);
		}
		
		return drugs;
	}
	
	public void printMeds(List<Medication> drugs, String mrn) {
		List<Medication> medicationList = new ArrayList<Medication>();
		
		for(Medication drug:drugs){
			medicationList.add(drug);
		}
		
		//sort by dispense date in descending order
		Collections.sort(medicationList, new MedicationListComparator());
		
		StringBuffer buf = new StringBuffer();
		buf.append("medications found for mrn: " + mrn + "\n\n");
		for (Medication drug : drugs) {
			//rx norm name, then original text name, then ndc name
			String name = drug.getRxNormName();
			
			if (name == null||name.length()==0) {
				name = drug.getOriginalText();
			}
			
			if (name == null||name.length()==0) {
				name = drug.getNdcName();
			}
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			
			// ignore null values
			if (name != null) {
				buf.append("Drug name: " + name + "\n");
				String dispenseDateString = "";
				
				if(drug.getDispenseDate()!= null){
					dispenseDateString = formatter.format(drug.getDispenseDate());
				}
				buf.append("Dispense date: " + dispenseDateString + "\n");
				buf.append("Sig: "+drug.getSig()+"\n");
				buf.append("Quantity: "+drug.getQuantity());
				String units = drug.getUnits();
				if (units != null && units.trim().length() > 0) {
					buf.append(" " + units);
				}
				
				buf.append("\n");
				buf.append("Strength: "+drug.getStrength()+"\n\n");
			}
		}
		log.info(buf.toString());
		
	}
	
	public void transformCCDToHTML(String ccd, String outputFile) {
		try {
			AdministrationService adminService = Context.getService(AdministrationService.class);
			ByteArrayInputStream input = new ByteArrayInputStream(ccd.getBytes());
			FileOutputStream output = new FileOutputStream(outputFile);
			String xsltFile = adminService.getGlobalProperty("rgccd.convertCCDToHTMLFile");
			FileInputStream xsltInputStream = new FileInputStream(xsltFile);
			
			if (xsltFile == null) {
				log.error("You must set rgccd.convertCCDToHTMLFile to run transformCCDToHTML");
				return;
			}
			XMLUtil.transformXML(input, output, xsltInputStream, null);
		}
		catch (Exception e) {
			log.error("",e);
		}
	}
}
