package com.aksimata.pilot;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ceefour
 *
 */
@SuppressWarnings("serial")
public class MultitenantPage extends BasePage {

	public MultitenantPage(PageParameters parameters) {
		super(parameters);
		add(new Label("tenantId", parameters.get("tenantId")));
		add(new Label("tenantEnv", parameters.get("tenantEnv")));
	}

}
