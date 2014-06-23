package com.artefactual.mets.validator;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artefactual.util.ClasspathResourceURIResolver;

/**
 * This class validates XML documents against ISO Schematron files. It can be
 * configured as a Spring bean or used separately. The bean must be initialized
 * through the loadSchemas() method after the map of named schemas is set.
 * 
 * @author count0
 * 
 */
public class Schematron {
	private static final Logger log = LoggerFactory.getLogger(Schematron.class);
	public static final String SVRL_NS_URI = "http://purl.oclc.org/dsdl/svrl";
	public static final Namespace SVRL_NS = Namespace.getNamespace("svrl", SVRL_NS_URI);
	private static final String ISO_SVRL_PATH = "/iso_svrl.xsl";
	private Templates schematronTemplates = null;
	private Source schematron = null;
	
	public Schematron(Source schematron) {
		this.schematron = schematron;
		loadSchematron();
	}

	/**
	 * Use this to initialize the configured schemas. Generate stylesheet
	 * implementations of ISO Schematron files and preload them into Transformer
	 * Templates for quick use.
	 */
	private void loadSchematron() {
		// Load up a transformer and the ISO Schematron to XSL templates.
		
		//TransformerFactoryImpl factory = new TransformerFactoryImpl();
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setURIResolver(new ClasspathResourceURIResolver());
		Templates isoSVRLTemplates = null;
		try (InputStream svrlRes = Schematron.class
				.getResourceAsStream(ISO_SVRL_PATH)) {
			if(svrlRes == null) {
				throw new Error(ISO_SVRL_PATH + " cannot obtain stream");
			}
			Source svrlrc = new StreamSource(svrlRes);
			isoSVRLTemplates = factory.newTemplates(svrlrc);
		} catch (IOException e) {
			throw new Error("Error setting up transformer factory", e);
		} catch (TransformerConfigurationException e) {
			throw new Error("Error setting up transformer", e);
		}

		// Get a transformer
		Transformer t = null;
		try {
			t = isoSVRLTemplates.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new Error("There was a problem configuring the transformer.",
					e);
		}


		JDOMResult res = new JDOMResult();
		try {
			t.transform(schematron, res);
		} catch (TransformerException e) {
			throw new Error(
					"Schematron issue: There were problems transforming Schematron to XSL.",
					e);
		}

		// compile templates for schematron
		try {
			schematronTemplates = factory.newTemplates(new JDOMSource(res.getDocument()));
			log.debug("Schematron template compiled: {}", this.schematron);
		} catch (TransformerConfigurationException e) {
			throw new Error("There was a problem configuring the transformer.",
					e);
		}

	}

	/**
	 * This is the lowest level validation call, which returns an XML validation
	 * report in schematron output format. (see http://purl.oclc.org/dsdl/svrl)
	 * 
	 * @param source
	 *            XML Source to validate
	 * @return schematron output document
	 */
	public Document validate(Source source) {

		// get a transformer
		Transformer t = null;
		try {
			t = schematronTemplates.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new Error("There was a problem configuring the transformer.",
					e);
		}

		// call the transform
		JDOMResult svrlRes = new JDOMResult();
		try {
			t.transform(source, svrlRes);
		} catch (TransformerException e) {
			throw new Error(
					"There was a problem running Schematron validation XSL.", e);
		}
		return svrlRes.getDocument();
	}
	
	public boolean hasFailedAsserts(Document n) {
		return !n.getRootElement().getContent(new ElementFilter("failed-assert", SVRL_NS)).isEmpty();
	}

}
