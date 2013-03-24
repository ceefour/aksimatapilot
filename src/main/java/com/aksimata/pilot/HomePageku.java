package com.aksimata.pilot;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.Subscribe;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.WebResponse.CacheScope;
import org.apache.wicket.util.time.Duration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePageku extends WebPage {

	private static final Logger log = LoggerFactory.getLogger(HomePageku.class);
	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static final List<ScheduledFuture<?>> futures = new CopyOnWriteArrayList<>();

	@Override
	protected void setHeaders(WebResponse response) {
		if (getApplication().getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT) {
			// super will disable caching entirely, which is sad
			// live.js can work with weak caching
			//super.setHeaders(response);
		} else {
			// 5 minutes is reasonable lag if web is updated during 5 mins,
			// after which the browser will request If-None-Match and
			// we can return 304 Not Modified if we haven't changed
			response.enableCaching(Duration.minutes(5), CacheScope.PUBLIC);
		}
	}
	
	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		// at this point you'll have getRequestCycle().getResponse() is a org.apache.wicket.protocol.http.BufferedWebResponse
		// which you can call toString() to get the body
		final BufferedWebResponse response = (BufferedWebResponse) getRequestCycle().getResponse();
		final CharSequence text = response.getText();
		try {
			final byte[] textBytes = IOUtils.toByteArray(new CharSequenceReader(text));
			//		log.trace("Content is {}", text);
			final String md5 = DigestUtils.md5Hex(textBytes);
			final String strongETag = "\"" + md5 + "\"";

			final WebRequest request = (WebRequest) getRequestCycle().getRequest();
			final String ifNoneMatch = request.getHeader("If-None-Match");
			if (ifNoneMatch != null && (ifNoneMatch.equals(strongETag) || ifNoneMatch.equals(md5))) {
				// matching, no need to send response
				log.debug("ETag for {} matches {}", getPageClass().getName(), ifNoneMatch);
				response.sendError(304, "ETag for " + getPageClass().getName() + " matches " + ifNoneMatch);
			} else {
				response.setContentLength(textBytes.length);
				response.setHeader("ETag", strongETag);
			}
		} catch (IOException e) {
			log.error("Cannot generate MD5 ETag for " + text, e);
		}
	}
	
	final IModel<DateTime> pushLucuModel = new Model<>();
	private Label pushLucu;
	private Label pushHeap;
	
	@Subscribe
	public void onDateTime(AjaxRequestTarget target, DateTime event) {
		log.info("Hey the date is now {}", event);
		pushLucuModel.setObject(event);
		target.add(pushLucu);
	}

	@Subscribe
	public void onInteger(AjaxRequestTarget target, Integer event) {
		log.info("Requested heap refresh {}", event);
		target.add(pushHeap);
	}
	
	public HomePageku() {
		final Label lucu = new Label("lucu", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				int quasecs = new DateTime().getSecondOfDay() / 15;
				return new DateTime().toString("yyyy-MM-dd HH:mm") + " " + quasecs + " quasecs";
			}
		});
		add(lucu);
		pushLucu = new Label("pushLucu", pushLucuModel);
		pushLucu.setOutputMarkupId(true);
		add(pushLucu);
		pushHeap = new Label("pushHeap", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return String.format("Total: %d Free: %d",
						Runtime.getRuntime().totalMemory(),
						Runtime.getRuntime().freeMemory());
			}
		});
		pushHeap.setOutputMarkupId(true);
		add(pushHeap);
	}

}
