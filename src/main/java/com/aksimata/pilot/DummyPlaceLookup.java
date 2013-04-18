package com.aksimata.pilot;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soluvas.commons.tenant.TenantRef;
import org.soluvas.data.EntityLookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * A sample, request-scoped, lookup
 * @author ceefour
 */
@Repository("placeLookup") @Scope("request")
public class DummyPlaceLookup implements EntityLookup<String, String> {
	
	private static final Logger log = LoggerFactory
			.getLogger(DummyPlaceLookup.class);
	private final TenantRef tenant;
	
	@Inject
	public DummyPlaceLookup(TenantRef tenant) {
		super();
		this.tenant = tenant;
		log.info("My tenant is {}", tenant);
	}
	
	@PostConstruct
	public void init() {
		log.debug("Initializing lookup for {}", tenant);
	}
	
	@PreDestroy
	public void destroy() {
		log.debug("Shutdown lookup for {}", tenant);
	}
	
	@Override @Nullable
	public <S extends String> S findOne(String id) {
		return (S) (tenant.getTenantId() + "/" + tenant.getTenantEnv() + "/" + id);
	}

}
