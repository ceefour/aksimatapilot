package com.aksimata.pilot;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.soluvas.data.EntityLookup;

/**
 * @author ceefour
 *
 */
@SuppressWarnings("serial")
public class MultitenantPlacePage extends BasePage {

	@SpringBean(name="placeLookup")
	private EntityLookup<String, String> placeLookup;
	
	public MultitenantPlacePage(PageParameters parameters) {
		super(parameters);
		add(new Label("tenantId", parameters.get("tenantId")));
		add(new Label("tenantEnv", parameters.get("tenantEnv")));
		add(new Label("lookupVal", placeLookup.findOne("hendy")));
	}

}
