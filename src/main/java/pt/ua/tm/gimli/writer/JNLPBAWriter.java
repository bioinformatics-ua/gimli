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
package pt.ua.tm.gimli.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import pt.ua.tm.gimli.corpus.Annotation;
import pt.ua.tm.gimli.corpus.AnnotationComparator;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.config.Constants.EntityType;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.model.CRFModel;
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.gimli.processing.Abbreviation;
import pt.ua.tm.gimli.processing.Parentheses;

/**
 * Annotate and write the result into a file following the JNLPBA format.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class JNLPBAWriter implements ICorpusWriter {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(JNLPBAWriter.class);
    /**
     * Help message.
     */
    private static final String MODEL_HELP = "Please follow the format: [file],[entity],[parsing],[features]\n"
            + "file: File with model;\n"
            + "entity: protein, DNA, RNA, cell_line or cell_type;\n"
            + "parsing: fw (forward) or bw (backward);\n"
            + "features: File with features configuration.";

    /**
     * Write annotation result in the JNLPBA format using only one entity type.
     * @param corpus The corpus.
     * @param file The file to store the result.
     * @throws GimliException Problem writing into the file.
     */
    @Override
    public void write(final Corpus corpus, final String file) throws GimliException {
        Sentence s;
        Token t;
        String medline = "", lastmedline = "";
        try {
            FileOutputStream out = new FileOutputStream(file);
            for (int i = 0; i < corpus.size(); i++) {
                s = corpus.getSentence(i);
                medline = s.getId();

                if (!medline.equals(lastmedline)) {
                    out.write(medline.getBytes());
                    out.write("\n".getBytes());
                    out.write("\n".getBytes());
                }

                for (int j = 0; j < s.size(); j++) {
                    t = s.getToken(j);
                    out.write(t.getText().getBytes());
                    out.write("\t".getBytes());
                    if (!t.getLabel().equals(LabelTag.O)) {
                        out.write(( t.getLabel().toString() + "-" + corpus.getEntity().toString() ).getBytes());
                    } else {
                        out.write(t.getLabel().toString().getBytes());
                    }
                    out.write("\n".getBytes());
                }
                out.write("\n".getBytes());

                lastmedline = medline;
            }
            out.close();
        }
        catch (IOException ex) {
            throw new GimliException("It was not possible to write the annotated corpus in the file.", ex);
        }
    }

    /**
     * Write annotation result in the JNLPBA format, combining annotations of
     * various entity types.
     * @param corpora The annotated corpora.
     * @param file The file to store the result.
     * @throws GimliException Problem writing in the output file.
     */
    public void write(final Corpus[] corpora, final String file) throws GimliException {
        if (corpora.length < 2) {
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            ArrayList<Annotation> annotations;
            Sentence s;
            String medline = "", lastmedline = "";
            for (int i = 0; i < corpora[0].size(); i++) {
                s = corpora[0].getSentence(i);
                medline = s.getId();

                // Collect all annotations for this sentence
                annotations = new ArrayList<Annotation>();
                for (int j = 0; j < corpora.length; j++) {
                    s = corpora[j].getSentence(i);
                    for (int k = 0; k < s.getNumberAnnotations(); k++) {
                        annotations.add(s.getAnnotation(k));
                    }
                }

                // Sort annotations
                Collections.sort(annotations, new AnnotationComparator());

                // Choose the best annotation for the same tokens
                ArrayList<Annotation> intersections;
                ArrayList<Annotation> unique = new ArrayList<Annotation>();
                for (int j = 0; j < annotations.size(); j++) {
                    Annotation a = annotations.get(j);

                    intersections = new ArrayList<Annotation>();
                    intersections.add(a);
                    int lastMatch = j;
                    for (int k = j + 1; k < annotations.size(); k++) {
                        Annotation an = annotations.get(k);
                        if (a.getStartIndex() <= an.getEndIndex() && a.getEndIndex() >= an.getStartIndex()) {
                            intersections.add(an);
                            lastMatch = k;
                        }
                    }

                    Annotation best = intersections.get(0);
                    for (int k = 1; k < intersections.size(); k++) {
                        a = intersections.get(k);
                        if (a.getScore() > best.getScore()) {
                            best = a;
                        }
                    }
                    unique.add(best);

                    j = lastMatch;
                }

                // Sort annotations
                Collections.sort(unique, new AnnotationComparator());

                // Write result to output
                if (!medline.equals(lastmedline)) {
                    out.write(medline.getBytes());
                    out.write("\n".getBytes());
                    out.write("\n".getBytes());
                }

                s = corpora[0].getSentence(i);

                String[] labels = new String[s.size()];
                for (int j = 0; j < labels.length; j++) {
                    labels[j] = LabelTag.O.toString();
                }

                for (int j = 0; j < unique.size(); j++) {
                    Annotation a = unique.get(j);
                    String entity = a.getSentence().getCorpus().getEntity().toString();
                    for (int k = a.getStartIndex(); k <= a.getEndIndex(); k++) {
                        if (k == a.getStartIndex()) {
                            labels[k] = LabelTag.B.toString() + "-" + entity;
                        } else {
                            labels[k] = LabelTag.I.toString() + "-" + entity;
                        }
                    }
                }

                for (int j = 0; j < s.size(); j++) {
                    out.write(s.getToken(j).getText().getBytes());
                    out.write("\t".getBytes());
                    out.write(labels[j].getBytes());
                    out.write("\n".getBytes());
                }
                out.write("\n".getBytes());
                lastmedline = medline;
            }
            out.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was a problem writing in the output file.", ex);
        }
    }

    /**
     * Print help message of the program.
     * @param options Command line arguments.
     * @param msg Message to be displayed.
     */
    private static void printHelp(Options options, String msg) {
        logger.error(msg);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("./gimli.sh annotate", options);
    }

    /**
     * Main program annotate a corpus using one or several models, and save the
     * result in the JNLPBA format.
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
        EntityType[] entities = new EntityType[input.length];
        String[] features = new String[input.length];
        Parsing[] parsing = new Parsing[input.length];

        String[] strs;
        for (int i = 0; i < input.length; i++) {
            strs = input[i].split(",");

            if (strs.length != 4) {
                printHelp(options, "Wrong input format for models.");
                return;
            }

            models[i] = strs[0].trim();
            entities[i] = EntityType.valueOf(strs[1].trim());
            parsing[i] = Parsing.valueOf(strs[2].trim().toUpperCase());
            features[i] = strs[3].trim();
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
                || ( features.length != entities.length )
                || ( features.length != parsing.length )) {
            logger.error("The number of feature files, parsing, entities and models are different from each other.");
            return;
        }

        // Give feedback
        logger.info("Corpus: {}", corpus);
        logger.info("Models:");
        for (int i = 0; i < models.length; i++) {
            logger.info("\t{}: {}, {}, {}, {}", new Object[]{i + 1, models[i], entities[i], features[i], parsing[i]});
        }
        logger.info("Output: {}", output);


        // Load model configurations
        ModelConfig[] mc = new ModelConfig[features.length];
        for (int i = 0; i < features.length; i++) {
            mc[i] = new ModelConfig(features[i]);
        }

        // Get unique entities
        ArrayList<EntityType> uniqueEntities = new ArrayList<EntityType>();
        for (int i = 0; i < entities.length; i++) {
            if (!uniqueEntities.contains(entities[i])) {
                uniqueEntities.add(entities[i]);
            }
        }

        // Load Corpus
        Corpus[] c = new Corpus[uniqueEntities.size()];
        try {
            for (int i = 0; i < uniqueEntities.size(); i++) {
                c[i] = new Corpus(LabelFormat.BIO, uniqueEntities.get(i), corpus);
            }
        }
        catch (GimliException ex) {
            logger.error("There was a problem loading the corpus.", ex);
            return;
        }

        // Combine models of the same entity
        for (int j = 0; j < uniqueEntities.size(); j++) {
            ArrayList<CRFModel> crfmodels = new ArrayList<CRFModel>();

            try {
                for (int i = 0; i < entities.length; i++) {
                    if (entities[i].equals(uniqueEntities.get(j))) {
                        crfmodels.add(new CRFModel(mc[i], parsing[i], models[i]));
                    }
                }
            }
            catch (GimliException ex) {
                logger.error("There was a problem loading a model.", ex);
                return;
            }


            Annotator a = new Annotator(c[j]);
            if (crfmodels.size() > 1) {

                logger.info("Annotating the corpus by combining {} models for {}...", crfmodels.size(), uniqueEntities.get(j));
                a.annotate(crfmodels.toArray(new CRFModel[0]));
            } else {
                logger.info("Annotating the corpus using 1 model for {}...", uniqueEntities.get(j));
                a.annotate(crfmodels.get(0));
            }
        }

        // Post-processing
        for (int i = 0; i < c.length; i++) {
            Parentheses.processRemoving(c[i]);
            Abbreviation.process(c[i]);
        }

        // Write annotations to file
        logger.info("Writing the corpus in the JNLPBA format...");
        JNLPBAWriter writer = new JNLPBAWriter();

        try {
            if (c.length > 1) {
                //Annotator.writeJNLPBACombined(c, output);
                writer.write(c, output);
            } else {
                writer.write(c[0], output);
                //Annotator.writeJNLPBA(c[0], output);
            }
        } catch (GimliException ex) {
            logger.error("There was a problem writing the corpus to file.", ex);
            return;
        }
        
        // Time feedback
        watch.stop();
        logger.info("Done!");
        logger.info("Total time: {}", watch.toString());

        double time = (double) watch.getTime();
        double size = (double) c[0].size();

        double perSentence = time / size;
        double perAbstract = time / ( size / 10.0 );
        double perHundred = time / ( size / 100.0 );

        logger.info("Per sentence: {} seconds", String.format("%2.4f", perSentence / 1000.0));
        logger.info("Per abstract: {} seconds", String.format("%2.4f", perAbstract / 1000.0));
        logger.info("10 abstracts: {} seconds", String.format("%2.4f", perHundred / 1000.0));
    }
}
