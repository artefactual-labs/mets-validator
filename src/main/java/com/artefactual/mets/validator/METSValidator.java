package com.artefactual.mets.validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artefactual.util.AppVersion;

/**
 * Command-line tool that reads METS files and validates them against Schematron
 * 2.0 rules.
 * 
 */
@SuppressWarnings("static-access")
public class METSValidator {
	private static final Logger LOG = LoggerFactory
			.getLogger(METSValidator.class);
	
	// some POSIX/FreeBSD standard error codes
	private static final int EXIT_USAGE = 64;
	private static final int EXIT_NOINPUT = 66;
	private static final int EXIT_CANTCREAT = 73;

	private static final String ARCHIVEMATICA_SCHEMATRON = "/archivematica_mets_schematron.xml";

	private static final Options options = new Options();
	private static final String CMD_LINE_SYNTAX = "java -jar mets-validator-1.0-jar-with-dependencies.jar [options] file1.xml file2.xml";
	private static Option help = OptionBuilder.withLongOpt("help")
			.withDescription("print this message").create('h');
	private static Option verbose = OptionBuilder.withLongOpt("verbose")
			.withDescription("be extra verbose").create('v');
	private static Option outFile = OptionBuilder.withArgName("file").hasArg()
			.withDescription("append output to file").withLongOpt("out")
			.create("o");
	//private static Option format = OptionBuilder
	//		.withDescription("output json instead of xml").withLongOpt("json")
	//		.create("j");
	private static Option schematron = OptionBuilder.hasArg()
			.withArgName("file").withDescription("specify schematron file")
			.withLongOpt("schematron").create("s");

	static {
		options.addOption(help);
		options.addOption(verbose);
		options.addOption(outFile);
		//options.addOption(format);
		options.addOption(schematron);
	}

	public static void main(String[] args) {
		CommandLineParser parser = new PosixParser();
		CommandLine line = null;
		String[] headfoot = getHeaderFooter();
		try {
			line = parser.parse(options, args);
			if (line.getArgList().isEmpty()) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(CMD_LINE_SYNTAX, headfoot[0], options, headfoot[1]);
				System.exit(EXIT_USAGE);
			}
		} catch (ParseException exp) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(CMD_LINE_SYNTAX, headfoot[0], options, headfoot[1]);
			System.exit(EXIT_USAGE);
		}

		// set up Schematron
		Source schematronSource = null;
		try {
			if (line.hasOption(schematron.getOpt())) {
				schematronSource = new StreamSource(
						FileUtils.openInputStream(new File(line
								.getOptionValue('s'))));
			} else {
				schematronSource = new StreamSource(
						Schematron.class
								.getResourceAsStream(ARCHIVEMATICA_SCHEMATRON));
			}
		} catch (IOException e) {
			LOG.error("Cannot load schematron", e);
			System.exit(EXIT_NOINPUT);
		}
		Schematron schematron = new Schematron(schematronSource);

		// set up out file
		OutputStream outStream = null;
		if (line.hasOption(outFile.getOpt())) {
			try {
				outStream = FileUtils.openOutputStream(new File(line
						.getOptionValue(outFile.getOpt())));
			} catch (IOException e) {
				LOG.error("Cannot open or create output file", e);
				System.exit(EXIT_CANTCREAT);
			}
		} else {
			outStream = System.out;
		}

		@SuppressWarnings("unchecked")
		List<String> files = (List<String>) line.getArgList();
		int failedValidations = 0;
		for (String fname : files) {
			try (InputStream in = FileUtils.openInputStream(new File(fname))) {
				Document results = schematron.validate(new StreamSource(in));
				if(schematron.hasFailedAsserts(results)) {
					failedValidations++;
				}
				XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
				out.output(results, outStream);
			} catch (IOException e) {
				LOG.error("Cannot read input file", e);
				System.exit(EXIT_NOINPUT);
			}
		}
		System.exit(-1*failedValidations);
	}

	public static String[] getHeaderFooter() {
		String[] result = new String[2];
		Package aPackage = METSValidator.class.getPackage();
		String title = aPackage.getImplementationTitle();
		result[1] = aPackage.getImplementationVendor();
		String version = AppVersion.get();
		result[0] = MessageFormat.format("{0} v{1}", title, version);
		return result;
	}
}
