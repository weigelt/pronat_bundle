package edu.kit.ipd.parse.bundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.kit.ipd.parse.luna.Luna;
import edu.kit.ipd.parse.luna.LunaRunException;

public class ParseRunner {

	private static final String CMD_OPTION_CREATE_CONFIG_FILES = "c";

	private static Luna luna = Luna.getInstance();

	private ParseRunner() {
	}

	/**
	 * Main method - reads the command line arguments and runs LUNA
	 *
	 * @param args
	 *            Command line arguments (to be parsed with commons-cli)
	 */
	public static void main(String[] args) {
		CommandLine cmd = null;
		//command line parsing
		try {
			cmd = doCommandLineParsing(args);
		} catch (ParseException exception) {
			System.err.println("Wrong command line arguments given: " + exception.getMessage());
			System.exit(1);
		}

		if (cmd.hasOption(CMD_OPTION_CREATE_CONFIG_FILES)) {
			// do whatever to do
		}

		//init luna
		initLuna();

		// run luna
		try {
			runLuna();
		} catch (LunaRunException e) {
			System.err.println("Exeption during run of LUNA: " + e.getMessage());
			System.exit(1);
		}
		System.exit(0);
	}

	/**
	 * Parse and check command line arguments
	 *
	 * @param args
	 *            Command line arguments given by the user
	 * @return CommandLine object that encapsulates all options
	 * @throws ParseException
	 *             Thrown iff wrong command line parameters or arguments given.
	 */
	static CommandLine doCommandLineParsing(String[] args) throws ParseException {
		CommandLine line = null;
		Options options = new Options();
		Option o;

		o = new Option(CMD_OPTION_CREATE_CONFIG_FILES, "create-config-files", false, "Creates config files in /user/.parse");
		o.setRequired(false);
		o.setType(Integer.class);
		options.addOption(o);

		// create the parser
		CommandLineParser parser = new DefaultParser();

		line = parser.parse(options, args);

		return line;
	}

	private static void runLuna() throws LunaRunException {
		luna.run();
	}

	private static void initLuna() {
		// TODO Auto-generated method stub

	}

}
