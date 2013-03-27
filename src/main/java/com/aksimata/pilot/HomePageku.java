package com.aksimata.pilot;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.Nullable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.WebResponse.CacheScope;
import org.apache.wicket.util.time.Duration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.Select2Choice;
import com.vaynberg.wicket.select2.Select2MultiChoice;
import com.vaynberg.wicket.select2.TextChoiceProvider;

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
	
//	@Override
//	protected void onAfterRender() {
//		super.onAfterRender();
//		// at this point you'll have getRequestCycle().getResponse() is a org.apache.wicket.protocol.http.BufferedWebResponse
//		// which you can call toString() to get the body
//		final BufferedWebResponse response = (BufferedWebResponse) getRequestCycle().getResponse();
//		final String originalText = response.getText().toString();
//		try {
//			// workaround for Wicket Ajax behaviors always changing
//			final String text = originalText.replaceAll("\\?[0-9-]+\\.IBehaviorListener", "?IBehaviorListener");
//			
//			final byte[] textBytes = IOUtils.toByteArray(new StringReader(text));
//			//		log.trace("Content is {}", text);
//			final String md5 = DigestUtils.md5Hex(textBytes);
//			final String strongETag = "\"" + md5 + "\"";
//
//			final WebRequest request = (WebRequest) getRequestCycle().getRequest();
//			final String ifNoneMatch = request.getHeader("If-None-Match");
//			if (ifNoneMatch != null && (ifNoneMatch.equals(strongETag) || ifNoneMatch.equals(md5))) {
//				// matching, no need to send response
//				log.debug("ETag for {} matches {}", getPageClass().getName(), ifNoneMatch);
//				response.sendError(304, "ETag for " + getPageClass().getName() + " matches " + ifNoneMatch);
//			} else {
//				response.setContentLength(textBytes.length);
//				response.setHeader("ETag", strongETag);
//			}
//		} catch (IOException e) {
//			log.error("Cannot generate MD5 ETag for " + originalText, e);
//		}
//	}
	
	final IModel<DateTime> pushLucuModel = new Model<>();
	private Label pushLucu;
	private Label pushHeap;
	
//	@Subscribe
//	public void onDateTime(AjaxRequestTarget target, DateTime event) {
//		log.info("Hey the date is now {}", event);
//		pushLucuModel.setObject(event);
//		target.add(pushLucu);
//	}
//
//	@Subscribe
//	public void onInteger(AjaxRequestTarget target, Integer event) {
//		log.info("Requested heap refresh {}", event);
//		target.add(pushHeap);
//	}
	
	final IModel<Locale> localeModel = new Model<>(null);
	final IModel<Collection<Locale>> localesModel = (IModel) new ListModel<>(new ArrayList<Locale>());
	
	public HomePageku() {
		final Label lucu = new Label("lucu", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				int quasecs = new DateTime().getSecondOfDay() / 15;
//				return new DateTime().toString("yyyy-MM-dd HH:mm") + " " + quasecs + " quasecs";
				return new DateTime().toString("yyyy-MM-dd 'jam' HH");
			}
		});
		add(lucu);
		pushLucu = new Label("pushLucu", pushLucuModel);
		pushLucu.setOutputMarkupId(true);
		add(pushLucu);
		pushHeap = new Label("pushHeap", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return String.format("Total: %d",
						Runtime.getRuntime().totalMemory());
//				return String.format("Total: %d Free: %d",
//						Runtime.getRuntime().totalMemory(),
//						Runtime.getRuntime().freeMemory());
			}
		});
		pushHeap.setOutputMarkupId(true);
		add(pushHeap);
		
		final List<Locale> locales = ImmutableList.copyOf(Locale.getAvailableLocales());
		
		final Select2Choice<Locale> localeSelect = new Select2Choice<Locale>("localeSelect", localeModel, new TextChoiceProvider<Locale>() {
			@Override
			public void query(final String term, int page, Response<Locale> response) {
				final Collection<Locale> matching = Collections2.filter(locales, new Predicate<Locale>() {
					@Override
					public boolean apply(@Nullable Locale input) {
						return StringUtils.containsIgnoreCase(input.getDisplayName(), term);
					}
				});
				response.addAll(matching);
			}

			@Override
			public Collection<Locale> toChoices(Collection<String> ids) {
				return Collections2.transform(ids, //Locale::forLanguageTag);
						new Function<String, Locale>() {
					@Override @Nullable
					public Locale apply(@Nullable String input) {
						return Locale.forLanguageTag(input);
					}
				});
			}

			@Override
			protected String getDisplayText(Locale choice) {
				return choice.getDisplayName();
			}

			@Override
			protected Object getId(Locale choice) {
				return choice.toLanguageTag();
			}
		});
		localeSelect.getSettings().setMinimumInputLength(1);
		localeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				log.info("Locale single select is {}", localeModel.getObject());
			}
		});
		
		add(localeSelect);

		final Select2MultiChoice<Locale> localesSelect = new Select2MultiChoice<Locale>("localesSelect", localesModel, new TextChoiceProvider<Locale>() {
			@Override
			public void query(final String term, int page, Response<Locale> response) {
				final Collection<Locale> matching = Collections2.filter(locales, new Predicate<Locale>() {
					@Override
					public boolean apply(@Nullable Locale input) {
						return StringUtils.containsIgnoreCase(input.getDisplayName(), term);
					}
				});
				response.addAll(matching);
			}

			@Override
			public Collection<Locale> toChoices(Collection<String> ids) {
				return Collections2.transform(ids, //Locale::forLanguageTag);
						new Function<String, Locale>() {
					@Override @Nullable
					public Locale apply(@Nullable String input) {
						return Locale.forLanguageTag(input);
					}
				});
			}

			@Override
			protected String getDisplayText(Locale choice) {
				return choice.getDisplayName();
			}

			@Override
			protected Object getId(Locale choice) {
				return choice.toLanguageTag();
			}
		});
		localesSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				log.info("Locales multi select is {}", localesModel.getObject());
			}
		});
		localesSelect.getSettings().setMinimumInputLength(1);
		add(localesSelect);
	}

}
