package com.aksimata.pilot;

import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * @author adri
 *
 */
@SuppressWarnings("serial")
public class ProgressPage extends BasePage {
	
    /**
     * Construct.
     *
     * @param parameters the current page parameters.
     */
    public ProgressPage(PageParameters parameters) {
        super(parameters);

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

}
