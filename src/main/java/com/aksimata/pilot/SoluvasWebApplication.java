package com.aksimata.pilot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.Page;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author ceefour
 *
 */
@Component("webApp")
public class SoluvasWebApplication extends WebApplication {
	
	private static final Logger log = LoggerFactory
			.getLogger(SoluvasWebApplication.class);
	private EventBus eventBus;
	private ScheduledExecutorService scheduleExecutor;
	
	@Override
	protected void init() {
		super.init();
		getComponentInstantiationListeners().add(
				new SpringComponentInjector(this));
		eventBus = new EventBus(this);
		scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduleExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				eventBus.post(new DateTime());
			}
		}, 1, 3, TimeUnit.SECONDS);
		scheduleExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				eventBus.post(new Integer(1));
			}
		}, 2, 4, TimeUnit.SECONDS);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return HomePageku.class;
	}

}
