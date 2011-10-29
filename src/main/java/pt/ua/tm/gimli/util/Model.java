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
package pt.ua.tm.gimli.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.config.Constants.EntityType;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.model.CRFModel;
import pt.ua.tm.gimli.config.ModelConfig;

/**
 * Utility class to train and test models.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Model {

    /**
     * The task to be performed.
     */
    private enum Task {
        /**
         * Train a model.
         */
        TRAIN,
        /**
         * Test the model.
         */
        TEST
    }
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Model.class);

    /**
     * Print help message of the program.
     * @param options Command line arguments.
     * @param msg Message to be displayed.
     */
    private static void printHelp(final Options options, final String msg) {
        logger.error(msg);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("./gimli.sh model", options);
    }

    /**
     * Main program to train and test models.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        // Start stopwatch
        StopWatch watch = new StopWatch();
        watch.start();
        
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");
        options.addOption("v", "verbose", false, "Verbose mode.");
        options.addOption("t", "task", true, "Task to be performed, train or test the model.");
        options.addOption("p", "parsing", true, "Parsing direction, fw (forward) or backward (bw)");
        options.addOption("f", "features", true, "File with features configuration.");
        options.addOption("e", "entity", true, "Target entity, should be protein, dna, rna, cell_line or cell_type.");
        options.addOption("c", "corpus", true, "File with the corpus in the CoNNL format.");
        options.addOption("m", "model", true, "File to save/load the CRF model.");
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

        // Verbose mode
        boolean verbose = false;
        if (commandLine.hasOption('v')) {
            verbose = true;
        }

        String str = "";

        // Get task
        Task task = null;
        if (commandLine.hasOption('t')) {
            str = commandLine.getOptionValue('t');
            try {
                task = Task.valueOf(str.toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                printHelp(options, "Task must be train or test.");
                return;
            }
        } else {
            printHelp(options, "Please specify the task to be performed.");
            return;
        }

        // Get parsing
        Parsing parsing = null;
        if (commandLine.hasOption('p')) {
            str = commandLine.getOptionValue('p');
            try {
                parsing = Parsing.valueOf(str.toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                printHelp(options, "Parsing must be fw (forward) or bw (backward).");
                return;
            }
        } else {
            printHelp(options, "Please specify the parsing direction.");
            return;
        }

        // Get features
        String features = null;
        if (commandLine.hasOption('f')) {
            features = commandLine.getOptionValue('f');
        } else {
            printHelp(options, "Please specify the model configuration file.");
            return;
        }

        // Get entity
        EntityType entity = null;
        if (commandLine.hasOption('e')) {
            str = commandLine.getOptionValue('e');
            try {
                entity = EntityType.valueOf(str);
            }
            catch (IllegalArgumentException ex) {
                printHelp(options, "Entity must be protein, dna, rna, cell_line or cell_type.");
                return;
            }
        } else {
            printHelp(options, "Please specify the task to be performed.");
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

        // Get model
        String model = null;
        if (commandLine.hasOption('m')) {
            model = commandLine.getOptionValue('m');
        } else {
            printHelp(options, "Please specify the model file.");
            return;
        }

        // Provide feedback
        logger.info("Parsing: {}", parsing);
        logger.info("Entity: {}", entity);
        logger.info("Corpus: {}", corpus);
        logger.info("Model: {}", model);


        // Set defaults
        LabelFormat format = LabelFormat.BIO;

        // Load model configuration
        ModelConfig mc = new ModelConfig(features);
        if (verbose) {
            mc.print();
        }

        Corpus c = null;
        try {
            c = new Corpus(format, entity, corpus);
        }
        catch (GimliException ex) {
            logger.error("Problem loading the corpus from file.", ex);
            return;
        }

        // Deal with model and corpus direction
        if (!parsing.equals(c.getParsing())) {
            c.reverse();
        }

        // Train
        if (task.equals(Task.TRAIN)) {
            CRFModel m = new CRFModel(mc, parsing);
            try {
                m.train(c);
                m.writeToFile(model);
            }
            catch (GimliException ex) {
                logger.error("Problem training and saving the model.", ex);
                return;
            }
            // Test
        } else {
            try {
                CRFModel m = new CRFModel(mc, parsing, model);
                m.test(c);
            }
            catch (GimliException ex) {
                logger.error("Problem loading the model from file.", ex);
                return;
            }
        }
        
        // Time feedback
        watch.stop();
        logger.info("Done!");
        logger.info("Total time: {}", watch.toString());
    }
}
