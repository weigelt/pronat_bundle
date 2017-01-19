package edu.kit.ipd.parse.bundle;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.kit.ipd.parse.luna.Luna;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.voice_recorder.VoiceRecorder;

public class ParseRunner {

	private static final String CMD_OPTION_CREATE_CONFIG_FILES = "c";
	private static final String CMD_OPTION_INTERACTIVE_MODE = "i";
	private static final String CMD_OPTION_TEST_MODE = "t";
	private static final String CMD_OPTION_SAVE_TO_FILE = "s";

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
		} catch (final ParseException exception) {
			System.err.println("Wrong command line arguments given: " + exception.getMessage());
			System.exit(1);
		}

		if (cmd.hasOption(CMD_OPTION_CREATE_CONFIG_FILES)) {
			// do whatever to do
		}

		if (cmd.hasOption(CMD_OPTION_INTERACTIVE_MODE)) {

			final VoiceRecorder vc = new VoiceRecorder();
			runVC(vc);
			//init luna
			initLuna();
			luna.getPrePipelineData().setInputFilePath(vc.getSavedFilePath());
			// run luna
			try {
				runLuna();
			} catch (final LunaRunException e) {
				System.err.println("Exeption during run of LUNA: " + e.getMessage());
				System.exit(1);
			}
		}

		if (cmd.hasOption(CMD_OPTION_TEST_MODE)) {
			//init luna
			initLuna();
			luna.getPrePipelineData().setInputFilePath(Paths.get(cmd.getOptionValue(CMD_OPTION_TEST_MODE)));
			// run luna
			try {
				runLuna();
			} catch (final LunaRunException e) {
				System.err.println("Exeption during run of LUNA: " + e.getMessage());
				System.exit(1);
			}
		}

		if(cmd.hasOption(CMD_OPTION_SAVE_TO_FILE)){
			//			luna.getMainGraph().
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
		final Options options = new Options();
		final Option configOption;
		final Option interactiveOption;
		final Option testOption;
		final Option saveOption;

		configOption = new Option(CMD_OPTION_CREATE_CONFIG_FILES, "create-config-files", false, "Creates config files in /user/.parse");
		configOption.setRequired(false);
		configOption.setType(Integer.class);

		interactiveOption = new Option(CMD_OPTION_INTERACTIVE_MODE, "interactive-mode", false,
				"Starts the audio recorder GUI and runs LUNA afterwards");
		interactiveOption.setRequired(false);
		interactiveOption.setType(Integer.class);

		testOption = new Option(CMD_OPTION_TEST_MODE, "test-mode", true, "Runs LUNA on the specified audio file");
		testOption.setRequired(false);
		testOption.setType(Path.class);

		saveOption = new Option(CMD_OPTION_SAVE_TO_FILE, "save-to-file", true, "Saves the resulting graph into the specified agg file");
		saveOption.setRequired(false);
		saveOption.setType(Path.class);

		options.addOption(configOption);
		options.addOption(interactiveOption);
		options.addOption(testOption);

		// create the parser
		final CommandLineParser parser = new DefaultParser();

		line = parser.parse(options, args);

		return line;
	}

	private static void runLuna() throws LunaRunException {
		luna.run();
	}

	private static void initLuna() {
		luna.init();
	}

	private static void runVC(VoiceRecorder vc) {
		vc.open();
		final JFrame f = new JFrame("Capture/Playback");
		//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add("Center", vc);
		f.pack();
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final int w = 360;
		final int h = 170;
		f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
		f.setSize(w, h);
		f.setVisible(true);
		int i = 0;
		while (!vc.hasRecorded()) {
			System.out.println(i++);
		}
	}

}
