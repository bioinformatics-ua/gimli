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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.ss.SentenceSplitter;

/**
 * Utility class for sentence splitter.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.1
 * @since 1.1
 */
public class Splitter {

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
        formatter.printHelp("./gimli_ss.sh", options);
    }

    public static void main(String[] args) {
        // Start stopwatch
        StopWatch watch = new StopWatch();
        watch.start();

        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");
        options.addOption("i", "task", true, "Folder with input files to be split.");
        options.addOption("o", "task", true, "Folder to store the output files.");
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

        // Get input folder name
        String inputFolderName = null;
        if (commandLine.hasOption('i')) {
            inputFolderName = commandLine.getOptionValue('i');
        } else {
            printHelp(options, "Please specify the input folder name.");
            return;
        }
        File inputFolder = new File(inputFolderName);
        if (!inputFolder.isDirectory()) {
            logger.error("The input path is not a folder.");
            return;
        }
        if (!inputFolder.exists()) {
            logger.error("The input folder does not exists.");
            return;
        }
        if (!inputFolder.canRead()) {
            logger.error("There are no read permissions on input folder.");
            return;
        }

        // Get output folder name
        String outputFolderName = null;
        if (commandLine.hasOption('o')) {
            outputFolderName = commandLine.getOptionValue('o');
        } else {
            printHelp(options, "Please specify the output folder name.");
            return;
        }
        File outputFolder = new File(outputFolderName);
        if (!outputFolder.isDirectory()) {
            logger.error("The output path is not a folder.");
            return;
        }
        if (!outputFolder.exists()) {
            logger.error("The input folder does not exists.");
            return;
        }
        if (!outputFolder.canWrite()) {
            logger.error("There are no write permissions on output folder.");
            return;
        }

        BufferedReader br;
        File[] files = inputFolder.listFiles();
        String line;
        StringBuilder sb;
        SentenceSplitter ss = null;
        ArrayList<String> sentences;
        FileOutputStream fos;
        File file;
        
        try {
            ss = new SentenceSplitter("resources/models/gimli/sentence_splitting_fw_o2.gz");
        }
        catch (GimliException ex) {
            logger.error("There was a problem loading the Sentence Splitter model.");
            return;
        }

        for (File f : files) {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                sb = new StringBuilder();
                while (( line = br.readLine() ) != null) {
                    if (!line.equals("\n")) {
                        sb.append(line);
                        sb.append("\n");
                    }
                }
                br.close();
                sentences = ss.split(sb.toString());
            } catch (Exception ex) {
                logger.error("The was a problem reading the file.", ex);
                return;
            }
            
            try {
                file = new File(outputFolder, f.getName());
                fos = new FileOutputStream(file);
                for (String s:sentences){
                    fos.write(s.getBytes());
                    fos.write("\n".getBytes());
                }
                fos.close();
            } catch (Exception ex) {
                logger.error("The was a problem writing the file.", ex);
                return;
            }
        }

        watch.stop();
        logger.info("Split {} files in {}", files.length, watch);
    }
}
