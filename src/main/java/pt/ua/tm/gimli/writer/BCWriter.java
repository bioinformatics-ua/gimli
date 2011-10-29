/*
 * Gimli - High-performance and multi-corpus recognition of biomedical
 * entity names
 *
 * Copyright (C) 2011 David Campos, Universidade de Aveiro, Instituto de
 * Engenharia Electrónica e Telemática de Aveiro
 *
 * Gimli is licensed under the Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 Unported License. To view a copy of
 * this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/.
 *
 * Gimli is a free software, you are free to copy, distribute, change and
 * transmit it. However, you may not use Gimli for commercial purposes.
 *
 * Gimli is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 */
package pt.ua.tm.gimli.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.annotator.Annotator;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.config.Constants.EntityType;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.model.CRFModel;
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.gimli.corpus.Annotation;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.processing.Abbreviation;
import pt.ua.tm.gimli.processing.Parentheses;

/**
 * Annotate and write the result into a file following the BioCreative format.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class BCWriter implements ICorpusWriter {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(BCWriter.class);
    /**
     * Help message.
     */
    private static final String MODEL_HELP = "Please follow the format: [file],[parsing],[features]\n"
            + "file: File with model;\n"
            + "parsing: fw (forward) or bw (backward);\n"
            + "features: File with features configuration.";

    /**
     * Print help message of the program.
     * @param options Command line arguments.
     * @param msg Message to be displayed.
     */
    private static void printHelp(Options options, String msg) {
        logger.error(msg);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("./gimli.sh annotate BC2", options);
    }

    /**
     * Convert the {@link Sentence} to the BioCreative format.
     * @param s The sentence to be converted.
     * @return The string that represents the sentence in the BioCreative
     * format.
     */
    private String sentenceToBCFormat(final Sentence s) {
        StringBuilder sb = new StringBuilder();

        Annotation a;
        for (int i = 0; i < s.getNumberAnnotations(); i++) {
            a = s.getAnnotation(i);

            String text = a.getText();
            int startChar = s.getToken(a.getStartIndex()).getStart();
            int endChar = s.getToken(a.getEndIndex()).getEnd();

            // Add annotation
            sb.append(s.getId());
            sb.append("|");
            sb.append(startChar);
            sb.append(" ");
            sb.append(endChar);
            sb.append("|");
            sb.append(text);
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Write annotation result in the BioCreative format.
     * @param corpus The corpus.
     * @param file The file to store the result.
     * @throws GimliException Problem writing into the file.
     */
    @Override
    public void write(final Corpus corpus, final String file) throws GimliException {
        try {
            FileOutputStream out = new FileOutputStream(file);
            for (int i = 0; i < corpus.size(); i++) {
                out.write(sentenceToBCFormat(corpus.getSentence(i)).getBytes());
            }
            out.close();
        }
        catch (IOException ex) {
            throw new GimliException("It was not possible to write the annotated corpus in the file.", ex);
        }
    }

    /**
     * Main program annotate a corpus using one or several models, and save the
     * result in the BioCreative format.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        // Start stopwatch
        StopWatch watch = new StopWatch();
        watch.start();

        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");

        options.addOption("c", "corpus", true, "File with the corpus in the CoNNL format.");

        Option o = new Option("m", "model", true, MODEL_HELP);
        o.setArgs(Integer.MAX_VALUE);

        options.addOption(o);

        options.addOption("o", "output", true, "File to save the annotated corpus.");
        CommandLine commandLine = null;
        try {
            // Parse the program arguments
            commandLine = parser.parse(options, args);
        }
        catch (ParseException ex) {
            logger.error("There was a problem processing the input arguments.", ex);
            return;
        }

        // Show help text
        if (commandLine.hasOption('h')) {
            printHelp(options, "");
            return;
        }

        // Get corpus
        String corpus = null;
        if (commandLine.hasOption('c')) {
            corpus = commandLine.getOptionValue('c');
        } else {
            printHelp(options, "Please specify the corpus file.");
            return;
        }

        // Get models
        String[] input = null;
        if (commandLine.hasOption('m')) {
            input = commandLine.getOptionValues('m');
        } else {
            printHelp(options, "Please specify the models.");
            return;
        }

        // Parse models characteristics
        String[] models = new String[input.length];
        String[] features = new String[input.length];
        Parsing[] parsing = new Parsing[input.length];

        String[] strs;
        for (int i = 0; i < input.length; i++) {
            strs = input[i].split(",");

            if (strs.length != 3) {
                printHelp(options, "Wrong input format for models.");
                return;
            }

            models[i] = strs[0].trim();
            parsing[i] = Parsing.valueOf(strs[1].trim().toUpperCase());
            features[i] = strs[2].trim();
        }

        // Get output
        String output = null;
        if (commandLine.hasOption('o')) {
            output = commandLine.getOptionValue('o');
        } else {
            printHelp(options, "Please specify the output file.");
            return;
        }

        // Check length consistency
        if (( features.length != models.length )
                || ( features.length != parsing.length )) {
            logger.error("The number of feature files, parsing, entities and models are different from each other.");
            return;
        }

        // Give user feedback
        logger.info("Corpus: {}", corpus);
        logger.info("Models:");
        for (int i = 0; i < models.length; i++) {
            logger.info("\t{}: {}, {}, {}", new Object[]{i + 1, models[i], features[i], parsing[i]});
        }
        logger.info("Output: {}", output);


        // Load model configurations
        ModelConfig[] mc = new ModelConfig[features.length];
        for (int i = 0; i < features.length; i++) {
            mc[i] = new ModelConfig(features[i]);
        }

        // Load Corpus
        Corpus c = null;
        try {
            c = new Corpus(LabelFormat.BIO, EntityType.protein, corpus);
        }
        catch (GimliException ex) {
            logger.error("There was a problem loading the corpus", ex);
            return;
        }

        // Load models
        CRFModel[] crfmodels = new CRFModel[models.length];
        try {
            for (int i = 0; i < models.length; i++) {
                crfmodels[i] = new CRFModel(mc[i], parsing[i], models[i]);
            }
        }
        catch (GimliException ex) {
            logger.error("There was a problem loading the model(s)", ex);
            return;
        }


        Annotator a = new Annotator(c);
        if (crfmodels.length > 1) {
            // Annotate combining the models
            logger.info("Annotating the corpus by combining {} models... ", crfmodels.length);
            a.annotate(crfmodels);

        } else {
            // Annotate using only one model
            logger.info("Annotating the corpus using 1 model...");
            a.annotate(crfmodels[0]);
        }

        // Post-process annotations
        Parentheses.processRemoving(c);
        Abbreviation.process(c);

        // Write to file in the BC format
        BCWriter writer = new BCWriter();
        try {
            logger.info("Wrtiting annotated corpus into file: {}", output);
            writer.write(c, output);
        }
        catch (GimliException ex) {
            logger.error("There was a problem writing the corpus to file", ex);
        }

        // Time feedback
        watch.stop();
        logger.info("Done!");
        logger.info("Total time: {}", watch.toString());

        double time = (double) watch.getTime();
        double size = (double) c.size();

        double perSentence = time / size;
        double perAbstract = time / ( size / 10.0 );
        double perHundred = time / ( size / 100.0 );

        logger.info("Per sentence: {} seconds", String.format("%2.4f", perSentence / 1000.0));
        logger.info("Per abstract: {} seconds", String.format("%2.4f", perAbstract / 1000.0));
        logger.info("10 abstracts: {} seconds", String.format("%2.4f", perHundred / 1000.0));
    }
}
