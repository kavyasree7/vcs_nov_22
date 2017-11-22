/*Copyright (c) 2016-2017 wavemaker.com All Rights Reserved.
 This software is the confidential and proprietary information of wavemaker.com You shall not disclose such Confidential Information and shall use it only in accordance
 with the terms of the source code license agreement you entered into with wavemaker.com*/
package com.vcs_nov_22.salesdb.service;

/*This is a Studio Managed File. DO NOT EDIT THIS FILE. Your changes may be reverted by Studio.*/

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.wavemaker.runtime.data.dao.WMGenericDao;
import com.wavemaker.runtime.data.exception.EntityNotFoundException;
import com.wavemaker.runtime.data.export.ExportType;
import com.wavemaker.runtime.data.expression.QueryFilter;
import com.wavemaker.runtime.data.model.AggregationInfo;
import com.wavemaker.runtime.file.model.Downloadable;

import com.vcs_nov_22.salesdb.Customers;
import com.vcs_nov_22.salesdb.Leads;


/**
 * ServiceImpl object for domain model class Customers.
 *
 * @see Customers
 */
@Service("salesdb.CustomersService")
@Validated
public class CustomersServiceImpl implements CustomersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomersServiceImpl.class);

    @Lazy
    @Autowired
	@Qualifier("salesdb.LeadsService")
	private LeadsService leadsService;

    @Autowired
    @Qualifier("salesdb.CustomersDao")
    private WMGenericDao<Customers, Integer> wmGenericDao;

    public void setWMGenericDao(WMGenericDao<Customers, Integer> wmGenericDao) {
        this.wmGenericDao = wmGenericDao;
    }

    @Transactional(value = "salesdbTransactionManager")
    @Override
	public Customers create(Customers customers) {
        LOGGER.debug("Creating a new Customers with information: {}", customers);
        Customers customersCreated = this.wmGenericDao.create(customers);
        if(customersCreated.getLeadses() != null) {
            for(Leads leadse : customersCreated.getLeadses()) {
                leadse.setCustomers(customersCreated);
                LOGGER.debug("Creating a new child Leads with information: {}", leadse);
                leadsService.create(leadse);
            }
        }
        return customersCreated;
    }

	@Transactional(readOnly = true, value = "salesdbTransactionManager")
	@Override
	public Customers getById(Integer customersId) throws EntityNotFoundException {
        LOGGER.debug("Finding Customers by id: {}", customersId);
        Customers customers = this.wmGenericDao.findById(customersId);
        if (customers == null){
            LOGGER.debug("No Customers found with id: {}", customersId);
            throw new EntityNotFoundException(String.valueOf(customersId));
        }
        return customers;
    }

    @Transactional(readOnly = true, value = "salesdbTransactionManager")
	@Override
	public Customers findById(Integer customersId) {
        LOGGER.debug("Finding Customers by id: {}", customersId);
        return this.wmGenericDao.findById(customersId);
    }


	@Transactional(rollbackFor = EntityNotFoundException.class, value = "salesdbTransactionManager")
	@Override
	public Customers update(Customers customers) throws EntityNotFoundException {
        LOGGER.debug("Updating Customers with information: {}", customers);
        this.wmGenericDao.update(customers);

        Integer customersId = customers.getId();

        return this.wmGenericDao.findById(customersId);
    }

    @Transactional(value = "salesdbTransactionManager")
	@Override
	public Customers delete(Integer customersId) throws EntityNotFoundException {
        LOGGER.debug("Deleting Customers with id: {}", customersId);
        Customers deleted = this.wmGenericDao.findById(customersId);
        if (deleted == null) {
            LOGGER.debug("No Customers found with id: {}", customersId);
            throw new EntityNotFoundException(String.valueOf(customersId));
        }
        this.wmGenericDao.delete(deleted);
        return deleted;
    }

	@Transactional(readOnly = true, value = "salesdbTransactionManager")
	@Override
	public Page<Customers> findAll(QueryFilter[] queryFilters, Pageable pageable) {
        LOGGER.debug("Finding all Customers");
        return this.wmGenericDao.search(queryFilters, pageable);
    }

    @Transactional(readOnly = true, value = "salesdbTransactionManager")
    @Override
    public Page<Customers> findAll(String query, Pageable pageable) {
        LOGGER.debug("Finding all Customers");
        return this.wmGenericDao.searchByQuery(query, pageable);
    }

    @Transactional(readOnly = true, value = "salesdbTransactionManager")
    @Override
    public Downloadable export(ExportType exportType, String query, Pageable pageable) {
        LOGGER.debug("exporting data in the service salesdb for table Customers to {} format", exportType);
        return this.wmGenericDao.export(exportType, query, pageable);
    }

	@Transactional(readOnly = true, value = "salesdbTransactionManager")
	@Override
	public long count(String query) {
        return this.wmGenericDao.count(query);
    }

    @Transactional(readOnly = true, value = "salesdbTransactionManager")
	@Override
    public Page<Map<String, Object>> getAggregatedValues(AggregationInfo aggregationInfo, Pageable pageable) {
        return this.wmGenericDao.getAggregatedValues(aggregationInfo, pageable);
    }

    @Transactional(readOnly = true, value = "salesdbTransactionManager")
    @Override
    public Page<Leads> findAssociatedLeadses(Integer id, Pageable pageable) {
        LOGGER.debug("Fetching all associated leadses");

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("customers.id = '" + id + "'");

        return leadsService.findAll(queryBuilder.toString(), pageable);
    }

    /**
	 * This setter method should only be used by unit tests
	 *
	 * @param service LeadsService instance
	 */
	protected void setLeadsService(LeadsService service) {
        this.leadsService = service;
    }

}

