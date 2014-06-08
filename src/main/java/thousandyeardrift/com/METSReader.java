package thousandyeardrift.com;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

/**
 * Command-line tool that reads METS files and validates them against Schematron 2.0 rules.
 *
 */
public class METSReader 
{
	private static final Options options = new Options();
	
	static {
		Option help = new Option( "help", "print this message" );
		Option verbose = new Option( "verbose", "be extra verbose" );
		Option outFile = OptionBuilder.withArgName("file")
				.hasArg().withDescription("file to save output").withLongOpt("file").create("f");
		Option format = OptionBuilder.withDescription("output json instead of xml").withLongOpt("json").create("j");
		Option schematron = OptionBuilder.hasArg().withArgName("file").withDescription("specify a schematron file")
				.withLongOpt("schematron").create("s");
		options.addOption(help);
		options.addOption(verbose);
		options.addOption(outFile);
		options.addOption(format);
		
	}
	
    public static void main( String[] args )
    {
    	CommandLineParser parser = new PosixParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            
            // set up Schematron
            
            
            @SuppressWarnings("unchecked")
			List<String> files = (List<String>)line.getArgList();
            for(String fname : files) {
            	try(InputStream in = FileUtils.openInputStream(new File(fname))) {
            		
            	} catch (IOException e) {
            		// TODO to stderr
					e.printStackTrace();
				}
            	
            }
        }
        catch( ParseException exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }

    }
}
