package com.example.tomcat.embed;

import org.apache.catalina.WebResourceSet;
import org.apache.catalina.webresources.StandardRoot;

public final class LoaderHidingResourceRoot extends StandardRoot {

	public LoaderHidingResourceRoot(TomcatEmbeddedContext context) {
		super(context);
	}

	@Override
	protected WebResourceSet createMainResourceSet() {
		return new LoaderHidingWebResourceSet(super.createMainResourceSet());
	}

}
