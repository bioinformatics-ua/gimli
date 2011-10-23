/*
 *  Gimli - High-performance and multi-corpus recognition of biomedical
 *  entity names
 *
 *  Copyright (C) 2011 David Campos, Universidade de Aveiro, Instituto de
 *  Engenharia Electrónica e Telemática de Aveiro
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version. Thus, this program could not be
 *  used for commercial purposes.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package pt.ua.tm.gimli.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
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
import pt.ua.tm.gimli.config.Constants.DictionaryType;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.config.Constants.EntityType;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.dictionary.DictionaryMatcher;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.config.Resources;
import pt.ua.tm.gimli.external.gdep.GDepCorpus;
import pt.ua.tm.gimli.external.gdep.GDepParser;
import pt.ua.tm.gimli.external.gdep.GDepSentence;
import pt.ua.tm.gimli.external.gdep.GDepToken;

/**
 * Read and load corpus in the JNLPBA format.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class JNLPBAReader implements ICorpusReader {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(JNLPBAReader.class);
    /**
     * File with the corpus in the JNLPBA format.
     */
    private String fileCorpus;
    /**
     * File with the GDep parsing of the corpus.
     */
    private String fileGDep;
    /**
     * Filter the entity type.
     */
    private EntityType type;

    /**
     * Constructor.
     * @param fileCorpus The file that contains the corpus in the JNLPBA format.
     * @param fileGDep File with GDep Parsing. If the file does not exists,
     * Gimli will parse the corpus for you and save the result in this file.
     */
    public JNLPBAReader(final String fileCorpus, final String fileGDep) {
        this.fileCorpus = fileCorpus;
        this.fileGDep = fileGDep;
        this.type = null;
    }

    /**
     * Constructor.
     * @param fileCorpus The file that contains the corpus in the JNLPBA format.
     * @param fileGDep File with GDep Parsing. If the file does not exists,
     * Gimli will parse the corpus for you and save the result in this file.
     * @param type The entity type that will be used in this corpus.
     */
    public JNLPBAReader(final String fileCorpus, final String fileGDep, final EntityType type) {
        this.fileCorpus = fileCorpus;
        this.fileGDep = fileGDep;
        this.type = type;
    }

    /**
     * Load JNLPBA corpus following the desired format.
     * @param format Format to annotate the corpus.
     * @return Corpus containing the sentences, tokens and tags.
     * @throws GimliException Problem reading the files.
     */
    @Override
    public Corpus read(LabelFormat format) throws GimliException {
        // Load GDep data
        GDepCorpus gdepOutput = new GDepCorpus();

        // Check if GDep exists
        boolean fileGDepExists = false;
        if (fileGDep != null) {
            File file = new File(fileGDep);
            fileGDepExists = file.exists();
        }

        if (!fileGDepExists) {
            logger.info("Running GDep parser...");
            gdepOutput = getGDepCorpus();
            logger.info("Saving GDep parsing result into file...");
            gdepOutput.writeToFile(fileGDep);
        } else {
            logger.info("Loading GDep parsing from file...");
            gdepOutput.loadFromFile(fileGDep);
        }

        // Get Corpus
        logger.info("Loading tokens, annotations and IDs...");
        Corpus c = loadCorpus(format, gdepOutput);


        // Add Dictionary matching results to corpus

        logger.info("Performing dictionary matching...");
        // PRGE
        DictionaryMatcher matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("prge"), DictionaryType.PRGE, true);
        matcher.match(c);
        // Verb
        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("verbs"), DictionaryType.VERB, false);
        matcher.match(c);
        // Concepts
        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("aminoacid"), DictionaryType.CONCEPT, false);
        matcher.match(c);
        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("nucleicacid"), DictionaryType.CONCEPT, false);
        matcher.match(c);
        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("nucleobase"), DictionaryType.CONCEPT, false);
        matcher.match(c);
        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("nucleoside"), DictionaryType.CONCEPT, false);
        matcher.match(c);
        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("nucleotide"), DictionaryType.CONCEPT, false);
        matcher.match(c);

        return c;
    }

    /**
     * Load corpus from GDep parsing result.
     * @param format The encoding format to be used in the corpus.
     * @param gdepOutput GDep parsing result.
     * @return The corpus with all data and annotations.
     * @throws GimliException Problem reading the corpus.
     */
    private Corpus loadCorpus(LabelFormat format, GDepCorpus gdepOutput) throws GimliException {
        Corpus c = new Corpus(format, EntityType.protein);

        Sentence s;
        GDepSentence gs;
        GDepToken gt;
        Token t;
        int start = 0;


        // Parser GDep Output
        for (int i = 0; i < gdepOutput.size(); i++) {
            gs = gdepOutput.getSentence(i);
            s = new Sentence(c);

            start = 0;
            for (int k = 0; k < gs.size(); k++) {
                gt = gs.getToken(k);
                t = new Token(s, start, k, gs);
                start = t.getEnd() + 1;
                s.addToken(t);
            }
            c.addSentence(s);
        }

        // Add tags, the correspondent annotations, and Sentence IDs
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(fileCorpus));
            BufferedReader br = new BufferedReader(isr);
            String line;
            String[] parts;
            String label, entity;
            int sentenceCounter = 0, tokenCounter = 0;
            String medline = "";

            while (( line = br.readLine() ) != null) {
                if (line.equals("") || line.equals("\n")) {
                    c.getSentence(sentenceCounter).setId(medline);
                    tokenCounter = 0;
                    c.getSentence(sentenceCounter).addAnnotationsFromTags(1.0);
                    sentenceCounter++;
                } else {
                    if (line.contains("###MEDLINE")) {
                        medline = line;
                        line = br.readLine();
                        line = br.readLine();
                    }

                    parts = line.split("\t");

                    // Check if corpus contains annotations
                    boolean corpusHasAnnotations = false;
                    if (parts.length > 1 && type != null) {
                        corpusHasAnnotations = true;
                    }

                    // Add annotations
                    if (corpusHasAnnotations) {
                        label = parts[1].substring(0, 1);
                        if (label.equals(LabelTag.O.toString())) {
                            c.getSentence(sentenceCounter).getToken(tokenCounter).setLabel(LabelTag.valueOf(label));
                        } else {
                            entity = parts[1].substring(2);
                            if (type.equals(EntityType.valueOf(entity))) {
                                c.getSentence(sentenceCounter).getToken(tokenCounter).setLabel(LabelTag.valueOf(label));
                            }
                        }
                    }

                    tokenCounter++;
                }
            }
            br.close();
            isr.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was a problem reading the corpus.", ex);
        }

        return c;
    }

    /**
     * Get GDep corpus by using the GDep parser.
     * @return The GDep parsing result.
     * @throws GimliException Problem reading the corpus.
     */
    public GDepCorpus getGDepCorpus() throws GimliException {
        GDepCorpus output = new GDepCorpus();

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(fileCorpus));
            BufferedReader br = new BufferedReader(isr);
            String line;

            GDepParser gdep = new GDepParser(false);
            gdep.launch();

            List<Object> list;
            GDepSentence s;
            StringBuilder sb = new StringBuilder();
            String[] parts;
            String token, lemma, pos, chunk, depTag;
            int depToken;
            GDepToken t;

            while (( line = br.readLine() ) != null) {
                if (line.equals("") || line.equals("\n")) {
                    list = gdep.parse(sb.toString().trim());

                    s = new GDepSentence(output);
                    for (int i = 0; i < list.size(); i++) {
                        parts = list.get(i).toString().split("\t");

                        token = parts[1];
                        token = token.replaceAll("''", "\"");
                        token = token.replaceAll("``", "\"");

                        lemma = parts[2];
                        chunk = parts[3];
                        pos = parts[4];
                        depToken = Integer.valueOf(parts[6]) - 1;
                        depTag = parts[7];

                        t = new GDepToken(token, lemma, pos, chunk, depToken, depTag);
                        s.addToken(t);
                    }

                    output.addSentence(s);

                    sb = new StringBuilder();
                } else {
                    if (line.contains("###MEDLINE")) {
                        line = br.readLine();
                        line = br.readLine();
                    }

                    parts = line.split("\t");
                    sb.append(parts[0]);
                    sb.append(" ");
                }
            }
            gdep.terminate();
            br.close();
            isr.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was a problem reading the corpus.", ex);
        }

        return output;
    }

    /**
     * Print help message of the program.
     * @param options Command line arguments.
     * @param msg Message to be displayed.
     */
    private static void printHelp(Options options, String msg) {
        logger.error(msg);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("./gimli.sh convert JNLPBA", options);
    }

    /**
     * Main program to read JNLPBA corpora and store the result in a file.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        // Start stopwatch
        StopWatch watch = new StopWatch();
        watch.start();

        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information.");
        options.addOption("c", "corpus", true, "File with the corpus.");
        options.addOption("g", "gdep", true, "File to load/save the GDep output.");

        Option o = new Option("e", "entity", true, "Target entity, should be protein, dna, rna, cell_line or cell_type.");
        o.setRequired(false);
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

        String str;

        // Get corpus
        String corpus = null;
        if (commandLine.hasOption('c')) {
            corpus = commandLine.getOptionValue('c');
        } else {
            printHelp(options, "Please specify the corpus file.");
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
        }


        // Get GDep output
        String gdep = null;
        if (commandLine.hasOption('g')) {
            gdep = commandLine.getOptionValue('g');
        } else {
            printHelp(options, "Please specify the file to load/save the GDep output.");
            return;
        }

        // Get output
        String output = null;
        if (commandLine.hasOption('o')) {
            output = commandLine.getOptionValue('o');
        } else {
            printHelp(options, "Please specify the output file.");
            return;
        }

        // Create corpus reader
        JNLPBAReader reader;
        if (entity != null) {
            reader = new JNLPBAReader(corpus, gdep, entity);
        } else {
            reader = new JNLPBAReader(corpus, gdep);
        }

        // Load corpus and write it to a file
        Corpus c = null;
        try {
            c = reader.read(LabelFormat.BIO);
            c.writeToFile(output);
        }
        catch (GimliException ex) {
            logger.error("There was a problem loading the corpus.", ex);
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
