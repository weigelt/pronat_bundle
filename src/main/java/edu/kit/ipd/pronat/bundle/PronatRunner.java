package edu.kit.ipd.pronat.bundle;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.JFrame;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.data.AbstractPostPipelineData;
import edu.kit.ipd.parse.luna.data.AbstractPrePipelineData;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
import edu.kit.ipd.pronat.postpipelinedatamodel.PostPipelineData;
import edu.kit.ipd.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.ipd.pronat.prepipedatamodel.tools.StringToHypothesis;
import edu.kit.ipd.pronat.voice_recorder.VoiceRecorder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import edu.kit.ipd.parse.luna.Luna;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

public class PronatRunner {

	private static final String CMD_OPTION_CREATE_CONFIG_FILES = "c";
	private static final String CMD_OPTION_INTERACTIVE_MODE = "i";
	private static final String CMD_OPTION_TEST_MODE = "t";
	private static final String CMD_OPTION_SAVE_TO_FILE = "s";
	private static final String CMD_OPTION_FILE_MODE = "f";

	private static Luna luna = Luna.getInstance();

	static Properties lunaProps;

	private PronatRunner() {
	}

	/**
	 * Main method - reads the command line arguments and runs LUNA
	 *
	 * @param args
	 *            Command line arguments (to be parsed with commons-cli)
	 */
	public static void main(String[] args) {
		lunaProps = ConfigManager.getConfiguration(Luna.class);
		CommandLine cmd = null;
		//command line parsing
		try {
			cmd = doCommandLineParsing(args);
		} catch (final ParseException exception) {
			System.err.println("Wrong command line arguments given: " + exception.getMessage());
			System.exit(1);
		}

		if (cmd.hasOption(CMD_OPTION_CREATE_CONFIG_FILES)) {
			// TODO:
			// do whatever to do
		}

		PrePipelineData prePipelineData = new PrePipelineData();
		PostPipelineData postPipelineData = new PostPipelineData();

		if (cmd.hasOption(CMD_OPTION_INTERACTIVE_MODE)) {

			final VoiceRecorder vc = new VoiceRecorder();
			runVC(vc);
			//init luna
			try {
				initLuna(prePipelineData, postPipelineData);
			} catch (LunaInitException e) {
				System.err.println("Exception during initialization of LUNA: " + e.getMessage());
				System.exit(1);
			}
			//			PrePipelineData prePipelineData = new PrePipelineData();
			prePipelineData.setInputFilePath(vc.getSavedFilePath());
			luna.setPrePipelineData(prePipelineData);
			//			luna.getPrePipelineData().setInputFilePath(vc.getSavedFilePath());
			// run luna
			try {
				runLuna();
			} catch (final LunaRunException e) {
				System.err.println("Exception during run of LUNA: " + e.getMessage());
				System.exit(1);
			}
		}

		if (cmd.hasOption(CMD_OPTION_TEST_MODE)) {
			//init luna
			// Test
			//			PrePipelineData prePipelineData = new PrePipelineData();
			//			luna.setPrePipelineData(prePipelineData);
			// Test
			try {
				initLuna(prePipelineData, postPipelineData);
			} catch (LunaInitException e) {
				System.err.println("Exception during initialization of LUNA: " + e.getMessage());
				System.exit(1);
			}
			//			PrePipelineData prePipelineData = new PrePipelineData();
			//			prePipelineData.setInputFilePath(Paths.get(cmd.getOptionValue(CMD_OPTION_TEST_MODE)));
			//			luna.setPrePipelineData(prePipelineData);
			try {
				((PrePipelineData) luna.getPrePipelineData().asPrePipelineData())
						.setInputFilePath(Paths.get(cmd.getOptionValue(CMD_OPTION_TEST_MODE)));
			} catch (PipelineDataCastException e) {
				e.printStackTrace();
			}
			//			luna.getPrePipelineData().setInputFilePath(Paths.get(cmd.getOptionValue(CMD_OPTION_TEST_MODE)));
			// run luna
			try {
				runLuna();
			} catch (final LunaRunException e) {
				System.err.println("Exception during run of LUNA: " + e.getMessage());
				System.exit(1);
			}
		}

		if (cmd.hasOption(CMD_OPTION_FILE_MODE)) {
			//init luna
			String lunaPrePipe = lunaProps.getProperty("PRE_PIPE");
			if (lunaPrePipe.contains("multiasr")) {
				lunaProps.setProperty("PRE_PIPE", lunaPrePipe.replace("multiasr", ""));
			}
			try {
				initLuna(prePipelineData, postPipelineData);
			} catch (LunaInitException e) {
				System.err.println("Exception during initialization of LUNA: " + e.getMessage());
				System.exit(1);
			}
			File textFile = new File(cmd.getOptionValue(CMD_OPTION_FILE_MODE));
			String string = "";
			try {
				string = FileUtils.readFileToString(textFile);
			} catch (IOException e1) {
				System.err.println("IO Exception read of text file: " + e1.getMessage());
				System.exit(1);
			}
			//			PrePipelineData prePipelineData = new PrePipelineData();
			//TODO: add option to use stanford tokenizer or not and use proper method
			prePipelineData.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(string, true));
			luna.setPrePipelineData(prePipelineData);
			//			luna.getPrePipelineData().setMainHypothesis(StringToHypothesis.stringToMainHypothesis(string));
			// run luna
			try {
				runLuna();
			} catch (final LunaRunException e) {
				System.err.println("Exception during run of LUNA: " + e.getMessage());
				System.exit(1);
			}
		}

		if (cmd.hasOption(CMD_OPTION_SAVE_TO_FILE)) {
			//TODO: implement me!
			//			AGGGraphCreator agggc = new AGGGraphCreator((ParseGraph) luna.getMainGraph());
			//			agggc.saveTo(cmd.getOptionValue(CMD_OPTION_SAVE_TO_FILE), Paths.get(".").toAbsolutePath().normalize().toString());
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
		final Option fileOption;

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

		fileOption = new Option(CMD_OPTION_FILE_MODE, "file-mode", true, "Runs LUNA on the specified text file (no ASR is needed)");
		fileOption.setRequired(false);
		fileOption.setType(Path.class);

		saveOption = new Option(CMD_OPTION_SAVE_TO_FILE, "save-to-file", true, "Saves the resulting graph into the specified agg file");
		saveOption.setRequired(false);
		saveOption.setType(Path.class);

		options.addOption(configOption);
		options.addOption(interactiveOption);
		options.addOption(testOption);
		options.addOption(saveOption);
		options.addOption(fileOption);

		// create the parser
		final CommandLineParser parser = new DefaultParser();

		line = parser.parse(options, args);

		return line;
	}

	private static void runLuna() throws LunaRunException {
		luna.run();
	}

	private static void initLuna(AbstractPrePipelineData abstractPrePipelineData, AbstractPostPipelineData abstractPostPipelineData)
			throws LunaInitException {
		luna.init(abstractPrePipelineData, abstractPostPipelineData);
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
