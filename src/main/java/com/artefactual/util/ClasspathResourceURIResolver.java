package com.artefactual.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClasspathResourceURIResolver implements URIResolver {
	private static final Logger LOG = LoggerFactory.getLogger(ClasspathResourceURIResolver.class);
	
	@SuppressWarnings("rawtypes")
	private Class[] searchPaths;

	public ClasspathResourceURIResolver(@SuppressWarnings("rawtypes") Class... searchPaths) {
		this.searchPaths = searchPaths;
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		LOG.debug("called with {} {}", href, base);
		for(@SuppressWarnings("rawtypes") Class c : searchPaths) {
			try(InputStream in = c.getResourceAsStream(href)) {
				return new StreamSource(in);
			} catch (IOException ignored) {
			}
		}
		return new StreamSource(ClassLoader.getSystemResourceAsStream(href));
	}

}
