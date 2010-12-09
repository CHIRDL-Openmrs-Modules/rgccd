package org.openmrs.module.rgccd.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

/**
 * Adds links to the administration page of the openmrs webapp
 * 
 * @author Tammy Dugan
 *
 */
public class AdminList extends AdministrationSectionExt {

	@Override
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	@Override
	public String getTitle() {
		return "rgccd.title";
	}
	
	@Override
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		
		return map;
	}
	
}
