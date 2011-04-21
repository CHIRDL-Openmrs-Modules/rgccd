package org.openmrs.module.rgccd.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.module.rgccd.db.ImmunizationDAO;

/**
 * Hibernate implementations of Immunization database methods.
 * 
 * @author Tammy Dugan
 * 
 */
public class HibernateImmunizationDAO implements ImmunizationDAO
{
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	/**
	 * Empty constructor
	 */
	public HibernateImmunizationDAO()
	{
	}

	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

}
