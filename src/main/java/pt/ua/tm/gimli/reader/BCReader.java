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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants.DictionaryType;
import pt.ua.tm.gimli.corpus.Annotation;
import pt.ua.tm.gimli.corpus.Corpus;
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
 * Read and load corpus in the BioCreative II Gene Mention task format.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class BCReader implements ICorpusReader {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(BCReader.class);
    /**
     * File that contains the sentences.
     */
    private String fileCorpus;
    /**
     * File that contains the annotations.
     */
    private String fileAnnotations;
    /**
     * File with the GDep parsing of the corpus.
     */
    private String fileGDep;

    /**
     * Constructor.
     * @param fileCorpus File with sentences.
     * @param fileAnnotations File with annotations.
     * @param fileGDep File with GDep Parsing. If the file does not exists,
     * Gimli will parse the corpus for you and save the result in this file.
     */
    public BCReader(final String fileCorpus, final String fileAnnotations, final String fileGDep) {
        this.fileCorpus = fileCorpus;
        this.fileAnnotations = fileAnnotations;
        this.fileGDep = fileGDep;
    }

    /**
     * Load BC corpus following the desired format.
     * @param format Format to annotate the corpus
     * @return Corpus containing the sentences, tokens and tags.
     * @throws GimliException Problem reading the files.
     */
    @Override
    public final Corpus read(final LabelFormat format) throws GimliException {

        // Load GDep data
        boolean gdepExists = new File(fileGDep).exists();
        GDepCorpus gdep = new GDepCorpus();

        if (fileGDep == null || !gdepExists) {
            logger.info("Running GDep parser");
            gdep = getGDepCorpus();
            logger.info("Saving GDep parsing result into file...");
            gdep.writeToFile(fileGDep);
        } else {
            logger.info("Loading GDep parsing from file...");
            gdep.loadFromFile(fileGDep);
        }

        // Get Annotations
        logger.info("Loading annotations...");
        MultiValueMap annotations = null;
        if (fileAnnotations != null) {
            annotations = getAnnotations();
        }

        // Get Corpus
        logger.info("Loading tokens and IDs...");
        Corpus c = loadCorpus(format, gdep, annotations);

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
     * @param annotations Annotations of the corpus.
     * @return The corpus with all data and annotations.
     * @throws GimliException Problem reading the corpus.
     */
    private Corpus loadCorpus(final LabelFormat format, final GDepCorpus gdepOutput, final MultiValueMap annotations) throws GimliException {
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

        // Get sentences IDs and add annotations
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(fileCorpus));
            BufferedReader br = new BufferedReader(isr);
            String line, id;
            int sentenceCounter = 0;
            while (( line = br.readLine() ) != null) {
                // Get only sentence, removing the ID
                id = line.substring(0, line.indexOf(" "));
                s = c.getSentence(sentenceCounter);
                s.setId(id);

                if (annotations != null) {
                    Collection<BCAnnotation> a = annotations.getCollection(id);
                    setAnnotations(s, a);
                }

                c.setSentence(sentenceCounter, s);
                sentenceCounter++;
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
            String line, sentence;

            GDepParser parser = new GDepParser(true);
            parser.launch();

            List<Object> list;
            GDepSentence s;
            String[] parts;
            String token, lemma, pos, chunk, depTag;
            int depToken;
            GDepToken t;

            while (( line = br.readLine() ) != null) {
                sentence = line.substring(line.indexOf(" ") + 1);
                sentence = sentence.replaceAll("/", " / ");
                sentence = sentence.replaceAll("-", " - ");
                sentence = sentence.replaceAll("[.]", " . ");
                sentence = sentence.replaceAll("//s+", " ");

                list = parser.parse(sentence);
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
            }
            parser.terminate();
            br.close();
            isr.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was a problem reading the corpus.", ex);
        }

        return output;
    }

    /**
     * Get annotations of this corpus reading the Annotations file.
     * @return MultiValueMap containing the annotations of each sentence
     * @throws GimliException Problem reading the annotations file
     */
    private MultiValueMap getAnnotations() throws GimliException {
        MultiValueMap annotations = new MultiValueMap();

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(fileAnnotations));
            BufferedReader br = new BufferedReader(isr);
            String line;

            int startChar, endChar;
            String pos, id;
            String[] parts;

            while (( line = br.readLine() ) != null) {
                parts = line.split("[|]");
                id = parts[0];

                pos = parts[1];
                parts = pos.split("\\s+");

                startChar = Integer.parseInt(parts[0]);
                endChar = Integer.parseInt(parts[1]);

                BCAnnotation a = new BCAnnotation(startChar, endChar);
                BCAnnotation other;

                // For annotations with the same start, use the largest one
                Collection<BCAnnotation> col;
                if (( col = annotations.getCollection(id) ) != null) {
                    Iterator<BCAnnotation> it = col.iterator();
                    while (it.hasNext()) {
                        other = it.next();
                        if (other.getStartChar() == a.getStartChar() && a.getEndChar() > other.getEndChar()) {
                            annotations.remove(id, other);
                            break;
                        }
                    }
                }

                annotations.put(id, a);
            }
        }
        catch (IOException ex) {
            throw new GimliException("There was a problem reading the annotations file.", ex);
        }

        return annotations;
    }

    /**
     * Set the annotations of an sentence.
     * @param s Sentence to add annotations.
     * @param annotations The annotations to be added.
     */
    private void setAnnotations(Sentence s, Collection<BCAnnotation> annotations) {
        if (annotations == null) {
            return;
        }

        List<BCAnnotation> list = new ArrayList(annotations);
        Collections.sort(list, new BCAnnotationComparator());

        /*List<BCAnnotation> tmp = new ArrayList(annotations);
        Collections.sort(tmp, new BCAnnotationComparator());*/

        int currentAnnotation = 0;
        Annotation a;
        BCAnnotation bc;
        int startIndex, endIndex;
        int charCount = 0;
        for (int i = 0; i < s.size(); i++) {

            if (currentAnnotation >= list.size()) {
                break;
            }

            bc = list.get(currentAnnotation);

            if (bc != null && bc.getStartChar() == charCount) {

                endIndex = startIndex = i;

                for (int j = i; j < s.size() && charCount <= bc.getEndChar(); j++) {
                    endIndex = j;
                    charCount += s.getToken(j).getText().length();
                }

                a = new Annotation(s, startIndex, endIndex, 1.0);
                s.addAnnotation(a);

                currentAnnotation++;
                i = endIndex;
                /*tmp.remove(bc);*/
            } else {
                if (charCount > bc.getStartChar()) {
                    currentAnnotation++;
                }

                charCount += s.getToken(i).getText().length();
            }
        }

        /*if (tmp.size() > 0) {
        System.out.println(s + " | " + tmp.size());
        }*/
    }

    /**
     * Print help message of the program.
     * @param options Command line arguments.
     * @param msg Message to be displayed.
     */
    private static void printHelp(final Options options, final String msg) {
        logger.error(msg);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("./gimli.sh convert BC2", options);
    }

    /**
     * Main program to read BioCreative corpora and store the result in a file.
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

        Option o = new Option("a", "annotations", true, "File with the annotations of the corpus.");
        o.setRequired(false);
        options.addOption(o);

        options.addOption("g", "gdep", true, "File to load/save the GDep output.");
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

        // Get annotations
        String annotations = null;
        if (commandLine.hasOption('a')) {
            annotations = commandLine.getOptionValue('a');
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
        BCReader reader = new BCReader(corpus, annotations, gdep);

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

    /**
     * Class to read BioCreative Annotations.
     */
    private class BCAnnotation {

        /**
         * Index of the first char of the annotation.
         */
        private int startChar;
        /**
         * Index of the last char of the annotation.
         */
        private int endChar;

        /**
         * Constructor.
         * @param startChar Index of the first char.
         * @param endChar Index of the last char.
         */
        public BCAnnotation(int startChar, int endChar) {
            this.startChar = startChar;
            this.endChar = endChar;
        }

        /**
         * Get the last char index.
         * @return The index.
         */
        public int getEndChar() {
            return endChar;
        }

        /**
         * Get the first char index.
         * @return The index.
         */
        public int getStartChar() {
            return startChar;
        }

        /**
         * Compare two BioCreative annotations.
         * @param obj The {@link BCAnnotation} to be compared with.
         * @return <code>True</code> if the two annotations are equal, and
         * <code>False</code> otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BCAnnotation other = (BCAnnotation) obj;
            if (this.startChar != other.startChar) {
                return false;
            }
            return true;
        }

        /**
         * Override the hashCode method to consider all the internal variables.
         * @return Unique number for each BioCreative annotation.
         */
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + this.startChar;
            return hash;
        }

        /**
         * Provide text representation of the annotation.
         * @return The text.
         */
        @Override
        public String toString() {
            return "(" + startChar + "," + endChar + ")";
        }
    }

    /**
     * Compare {@link BCAnnotation} objects.
     */
    private class BCAnnotationComparator implements Comparator<BCAnnotation> {

        /**
         * Compare BioCreative annotations.
         * @param t The first BioCreative annotation.
         * @param t1 The second BioCreative annotation.
         * @return <code>1</code> if the first annotation appears after or is
         * larger than the second one, and <code>-1</code> otherwise.
         * <code>0</code> if the annotations are equal.
         */
        @Override
        public int compare(BCAnnotation t, BCAnnotation t1) {
            if (t.getStartChar() > t1.getStartChar()) {
                return 1;
            }
            if (t.getStartChar() < t1.getStartChar()) {
                return -1;
            }

            if (t.getEndChar() > t1.getEndChar()) {
                return 1;
            }
            if (t.getEndChar() < t1.getEndChar()) {
                return -1;
            }
            return 0;
        }
    }
}
