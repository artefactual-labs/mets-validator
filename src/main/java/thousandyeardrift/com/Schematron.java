package thousandyeardrift.com;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

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
	private static final String ISO_SVRL_PATH = "thousandyeardrift/com/mets/reader/iso_svrl.xsl";
	private Templates schematronTemplates = null;
	private String schematronFile = null;
	
	public Schematron(String schematronFile) {
		this.schematronFile = schematronFile;
		loadSchematron();
	}

	/**
	 * Use this to initialize the configured schemas. Generate stylesheet
	 * implementations of ISO Schematron files and preload them into Transformer
	 * Templates for quick use.
	 */
	private void loadSchematron() {
		// Load up a transformer and the ISO Schematron to XSL templates.
		TransformerFactory factory = TransformerFactory.newInstance();
		Templates isoSVRLTemplates = null;
		try (InputStream svrlRes = Schematron.class
				.getResourceAsStream(ISO_SVRL_PATH)) {
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

		Source schematron = null;
		try {
			if(schematronFile != null) {
				schematron = new StreamSource(FileUtils.openInputStream(new File(schematronFile)));
			} else {
				schematron = new StreamSource(Schematron.class
						.getResourceAsStream(ISO_SVRL_PATH));
			}
		} catch (IOException e) {
			throw new Error("Cannot load schematron", e);
		}
		DOMResult res = new DOMResult();
		try {
			t.transform(schematron, res);
		} catch (TransformerException e) {
			throw new Error(
					"Schematron issue: There were problems transforming Schematron to XSL.",
					e);
		}

		// compile templates object for each profile
		try {
			schematronTemplates = factory.newTemplates(new DOMSource(res
					.getNode()));
			log.info("Schematron template compiled: {}", this.schematronFile);
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
	public Node validate(Source source) {

		// get a transformer
		Transformer t = null;
		try {
			t = schematronTemplates.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new Error("There was a problem configuring the transformer.",
					e);
		}

		// call the transform
		DOMResult svrlRes = new DOMResult();
		try {
			t.transform(source, svrlRes);
		} catch (TransformerException e) {
			throw new Error(
					"There was a problem running Schematron validation XSL.", e);
		}
		return svrlRes.getNode();
	}

}
