/**
 * 
 */
package org.openmrs.module.rgccd;

import java.util.Comparator;


/**
 * @author tmdugan
 *
 */
public class MedicationListComparator implements Comparator
{
	//sort row in descending order by encounter datetime
	public int compare(Object obj1, Object obj2)
	{
		Medication med1 = (Medication) obj1;
		Medication med2 = (Medication) obj2;
		
		
		//sort null dispense dates last
		if(med1.getDispenseDate()==null&&med2.getDispenseDate()!=null){
			return 1;
		}
		
		if(med2.getDispenseDate()==null&&med1.getDispenseDate()!=null){
			return -1;
		}
		
		if(med2.getDispenseDate()==null&&med1.getDispenseDate()==null){
			return 0;
		}
		
		return med2.getDispenseDate().
			compareTo(med1.getDispenseDate());
	}

}
