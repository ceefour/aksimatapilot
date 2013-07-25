package com.aksimata.pilot;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.Subscribe;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.ProgressBar;


/**
 * @author adri
 *
 */
@SuppressWarnings("serial")
public class ProgressPage extends BasePage {

	private static final Logger log = LoggerFactory
			.getLogger(ProgressPage.class);
	private final Model<Integer> progressValueModel = new Model<>(35);
	private final ProgressBar progressBar;
	
    /**
     * Construct.
     *
     * @param parameters the current page parameters.
     */
    public ProgressPage(PageParameters parameters) {
        super(parameters);
        
		progressBar = new ProgressBar("progressBar", progressValueModel).active(true).striped(true);
		progressBar.setOutputMarkupId(true);
        add(progressBar);

//        add(new Global("global"));
//        add(new Grid("grid"));
//        add(new FluidGrid("fluidGridSystem"));
//        add(new Layouts("layouts"));
//        add(new Responsive("responsive"));
    }

    @Override
    protected boolean hasNavigation() {
        return true;
    }
    
    @Subscribe
    public void updateProgressBar(AjaxRequestTarget target, Integer value) {
    	log.debug("Updating progressbar from {}", value);
    	progressValueModel.setObject(value);
    	target.add(progressBar);
    }

}
