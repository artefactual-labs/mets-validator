METS Validator
==============

To build the runnable JAR you do need java and maven:

    $ cd mets-validator
    $ mvn package

After packaging you can find the runnable JAR here:

    mets-validator/target/mets-validator-1.0-jar-with-dependencies.jar

You can move the jar wherever you like. To run the validator, do this:

    $ java -jar mets-validator-1.0-jar-with-dependencies.jar

That will give you usage/help output:

    usage: java -jar mets-validator-1.0-jar-with-dependencies.jar [options] file1.xml file2.xml
    METS Validator v1.0.ce622
     -h,--help                print this message
     -o,--out <file>          append output to file
     -s,--schematron <file>   specify schematron file
     -v,--verbose             be extra verbose
    Artefactual Systems, Inc.

The validator returns the following POSIX standard exit values:
* incorrect usage: 64
* bad input file: 66
* cannot create output file: 73

If validation completes normally then the exit value will reflect the number of files that failed to validate. So one failed validation will return -1. 6 invalid files will return -6, and so on. If all METS files are valid, then the exit value is 0.
