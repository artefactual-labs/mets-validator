package com.artefactual.mets.validator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

public class METSValidatorTest {

	@Test
	public void testMETS1() {
		METSValidator.main(new String[] {"src/test/resources/METS1.xml"});
	}
	
	@Test
	public void testMETS2() {
		METSValidator.main(new String[] {"src/test/resources/METS2.xml"});
	}
	
	@Test
	public void testMETSFailures() throws JDOMException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream catchStream = new PrintStream(out);
        PrintStream stdout = System.out;
		System.setOut(catchStream);
		METSValidator.main(new String[] {"src/test/resources/METS-failures.xml"});
		catchStream.flush();
		System.setOut(stdout);
		//System.out.println(out.toString());
		Document svrl = new SAXBuilder().build(IOUtils.toInputStream(out.toString()));
		int failedAsserts = svrl.getRootElement().getContent(new ElementFilter("failed-assert", Schematron.SVRL_NS)).size();
		org.junit.Assert.assertEquals("Number of schematron failures is predictable for test XML", 6, failedAsserts);
	}

}
