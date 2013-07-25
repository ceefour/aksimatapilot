package com.aksimata.pilot;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.version.MessageDigestResourceVersion;
import org.apache.wicket.serialize.java.DeflatedJavaSerializer;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.aksimata.pilot.skin.FixBootstrapStylesCssResourceReference;
import com.google.javascript.jscomp.CompilationLevel;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.RenderJavaScriptToFooterHeaderResponseDecorator;
import de.agilecoders.wicket.core.markup.html.references.BootstrapPrettifyCssReference;
import de.agilecoders.wicket.core.markup.html.references.ModernizrJavaScriptReference;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.ThemeProvider;
import de.agilecoders.wicket.extensions.javascript.GoogleClosureJavaScriptCompressor;
import de.agilecoders.wicket.extensions.javascript.YuiCssCompressor;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.html5player.Html5PlayerCssReference;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.html5player.Html5PlayerJavaScriptReference;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.OpenWebIconsCssReference;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIJavaScriptReference;
import de.agilecoders.wicket.themes.markup.html.google.GoogleTheme;
import de.agilecoders.wicket.themes.markup.html.metro.MetroTheme;
import de.agilecoders.wicket.themes.markup.html.wicket.WicketTheme;
import de.agilecoders.wicket.themes.settings.BootswatchThemeProvider;

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
	private Properties properties;
	
	@Override
	protected void init() {
		super.init();
		getComponentInstantiationListeners().add(
				new SpringComponentInjector(this));
		eventBus = new EventBus(this);
		
        // deactivate ajax debug mode
        getDebugSettings().setAjaxDebugModeEnabled(false);

        configureBootstrap();
        configureResourceBundles();

        optimizeForWebPerformance();

        //StaticResourceRewriteMapper.withBaseUrl("http://wb.agile-coders.de").install(this);
		
		mountPage("/base", BasePage.class);
		mountPage("/progress", ProgressPage.class);
		mountPage("/t/${tenantId}/${tenantEnv}/multi", MultitenantPage.class);
		mountPage("/t/${tenantId}/${tenantEnv}/place", MultitenantPlacePage.class);

		// other stuff
		scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduleExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				eventBus.post(new DateTime());
			}
		}, 1, 3, TimeUnit.SECONDS);
		final Random random = new Random();
		scheduleExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				final int value = random.nextInt(101);
				log.debug("Sending integer {}", value);
				eventBus.post(value);
			}
		}, 2, 4, TimeUnit.SECONDS);
	}
	
	@Override
	protected void onDestroy() {
		scheduleExecutor.shutdown();
		super.onDestroy();
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return HomePageku.class;
	}

    /**
     * optimize wicket for a better web performance
     */
    private void optimizeForWebPerformance() {
        if (usesDeploymentConfig()) {
            getResourceSettings().setCachingStrategy(new FilenameWithVersionResourceCachingStrategy(
                    new MessageDigestResourceVersion()
            ));

            getResourceSettings().setJavaScriptCompressor(new GoogleClosureJavaScriptCompressor(CompilationLevel.SIMPLE_OPTIMIZATIONS));
            getResourceSettings().setCssCompressor(new YuiCssCompressor());

            getFrameworkSettings().setSerializer(new DeflatedJavaSerializer(getApplicationKey()));
        }

        setHeaderResponseDecorator(new RenderJavaScriptToFooterHeaderResponseDecorator());
    }

    /**
     * configure all resource bundles (css and js)
     */
    private void configureResourceBundles() {
        getResourceBundles().addJavaScriptBundle(SoluvasWebApplication.class, "core.js",
                                                 (JavaScriptResourceReference) getJavaScriptLibrarySettings().getJQueryReference(),
                                                 (JavaScriptResourceReference) getJavaScriptLibrarySettings().getWicketEventReference(),
                                                 (JavaScriptResourceReference) getJavaScriptLibrarySettings().getWicketAjaxReference(),
                                                 (JavaScriptResourceReference) ModernizrJavaScriptReference.INSTANCE
        );

//        getResourceBundles().addJavaScriptBundle(SoluvasWebApplication.class, "bootstrap.js",
//                                                 (JavaScriptResourceReference) Bootstrap.getSettings().getJsResourceReference(),
//                                                 (JavaScriptResourceReference) BootstrapPrettifyJavaScriptReference.INSTANCE,
//                                                 ApplicationJavaScript.INSTANCE
//        );

        getResourceBundles().addJavaScriptBundle(SoluvasWebApplication.class, "bootstrap-extensions.js",
                                                 JQueryUIJavaScriptReference.instance(),
                                                 Html5PlayerJavaScriptReference.instance()
        );

        getResourceBundles().addCssBundle(SoluvasWebApplication.class, "bootstrap-extensions.css",
                                          Html5PlayerCssReference.instance(),
                                          OpenWebIconsCssReference.instance()
        );

        getResourceBundles().addCssBundle(SoluvasWebApplication.class, "application.css",
                                          (CssResourceReference) BootstrapPrettifyCssReference.INSTANCE,
                                          FixBootstrapStylesCssResourceReference.INSTANCE
        );
    }

    /**
     * configures wicket-bootstrap and installs the settings.
     */
    private void configureBootstrap() {
        final ThemeProvider themeProvider = new BootswatchThemeProvider() {{
            add(new MetroTheme());
            add(new GoogleTheme());
            add(new WicketTheme());
            defaultTheme("wicket");
        }};

        final BootstrapSettings settings = new BootstrapSettings();
        settings.setJsResourceFilterName("footer-container")
                .setThemeProvider(themeProvider);
        Bootstrap.install(this, settings);

//        final IBootstrapLessCompilerSettings lessCompilerSettings = new BootstrapLessCompilerSettings();
//        lessCompilerSettings.setUseLessCompiler(usesDevelopmentConfig())
//                .setLessCompiler(new Less4JCompiler());
//        BootstrapLess.install(this, lessCompilerSettings);
    }

    /**
     * @return used configuration properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * loads all configuration properties from disk
     *
     * @return configuration properties
     */
    private Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            throw new WicketRuntimeException(e);
        }
        return properties;
    }
    
}
