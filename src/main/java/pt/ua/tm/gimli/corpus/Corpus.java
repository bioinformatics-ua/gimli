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
package pt.ua.tm.gimli.corpus;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.config.Constants.EntityType;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.exception.GimliException;

/**
 * Used to represent corpus.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Corpus {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Corpus.class);
    /**
     * The set of {@link Sentence} objects of the corpus.
     */
    private ArrayList<Sentence> sentences;
    /**
     * The encoding format used in this corpus.
     */
    private LabelFormat format;
    /**
     * The target entity name of this corpus.
     */
    private EntityType entity;
    /**
     * The parsing direction of the corpus.
     */
    private Parsing parsing;

    /**
     * Constructor.
     * @param format The encoding format.
     * @param entity The target entity type.
     */
    public Corpus(final LabelFormat format, final EntityType entity) {
        this.format = format;
        this.entity = entity;
        sentences = new ArrayList<Sentence>();
        this.parsing = Parsing.FW;
    }

    /**
     * Constructor that loads the corpus from a file in the CoNNL format, which
     * should be obtained using the Reader classes that implement the
     * ICorpusReader interface.
     * @param format The encoding format.
     * @param entity The target entity type.
     * @param file The file to load the corpus.
     * @throws GimliException Problem reading the corpus content from the file.
     */
    public Corpus(final LabelFormat format, final EntityType entity, final String file)
            throws GimliException {
        this(format, entity);

        // Load corpus from file
        loadFromFile(file);
    }

    /**
     * Get the sentences of the corpus.
     * @return The {@link ArrayList} of {@link Sentence}.
     */
    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    /**
     * Get the target entity name of the corpus.
     * @return The target entity name.
     */
    public EntityType getEntity() {
        return entity;
    }

    /**
     * Add sentence to corpus.
     * @param s sentence to add
     */
    public void addSentence(final Sentence s) {
        sentences.add(s);
    }

    /**
     * Change the sentence on a specific index of the set of sentences.
     * @param i The index.
     * @param s The sentence to be placed.
     */
    public void setSentence(final int i, final Sentence s) {
        sentences.set(i, s);
    }

    /**
     * Get sentence from corpus.
     * @param i Get the sentence from the specific index.
     * @return The sentence in the position <code>i</code>.
     */
    public Sentence getSentence(final int i) {
        return sentences.get(i);
    }

    /**
     * Get the number of sentences of the corpus.
     * @return The number of sentences.
     */
    public int size() {
        return sentences.size();
    }

    /**
     * Get the encoding format used in the corpus.
     * @return The encoding format.
     */
    public LabelFormat getFormat() {
        return format;
    }

    /**
     * Convert sentences from CoNNL file to Mallet instances.
     * @param p Pipe that specifies the features to be extracted
     * @return List of instances to train/test CRF
     */
    public InstanceList toModelFormat(Pipe p) {

        InstanceList instances = new InstanceList(p);
        String text;
        Sentence s;

        for (Integer i = 0; i < size(); i++) {
            s = getSentence(i);
            text = s.toExportFormat();
            instances.addThruPipe(new Instance(text, null, i, null));
        }

        return instances;
    }

    /**
     * Remove all the annotations of the corpus.
     */
    public void cleanAnnotations() {
        for (int i = 0; i < size(); i++) {
            getSentence(i).cleanAnnotations();
        }
    }

    /**
     * Change the order of the corpus. If the current is forward, changes
     * to backward, and vice-versa.
     */
    public void reverse() {
        for (int i = 0; i < size(); i++) {
            getSentence(i).reverse();
        }

        // Set parsing
        if (parsing.equals(Parsing.FW)) {
            parsing = Parsing.BW;
        } else {
            parsing = Parsing.FW;
        }
    }

    /**
     * Get the current parsing direction of the corpus.
     * @return The {@link Parsing} direction.
     */
    public Parsing getParsing() {
        return parsing;
    }

    /**
     * Considering the {@link LabelFormat} used by the corpus, provides the
     * pattern that should not be allowed during the training. For instance,
     * considering the BIO format, the appearance of one token with the label I
     * will be always preceeded with one with the label B. Thus, the pattern
     * O>I is not allowed.
     * @return The forbidden patterns.
     */
    public Pattern getForbiddenPattern() {
        String forbiddenPattern = null;
        if (format.equals(LabelFormat.BIO)) {
            if (parsing.equals(Parsing.FW)) {
                forbiddenPattern = LabelTag.O + "," + LabelTag.I;
            } else {
                forbiddenPattern = LabelTag.I + "," + LabelTag.O;
            }
            return Pattern.compile(forbiddenPattern);
        } else if (format.equals(LabelFormat.IO)) {
            return null;
        } else if (format.equals(LabelFormat.BMEWO)) {
            if (parsing.equals(Parsing.FW)) {
                forbiddenPattern = LabelTag.O + "," + LabelTag.M;
            } else {
                forbiddenPattern = LabelTag.M + "," + LabelTag.O;
            }
            return Pattern.compile(forbiddenPattern);
        }
        return null;
    }

    /**
     * Provides the tags that are used for entity names annotations.
     * Considering the BIO format, only the tags B and I are used to identify
     * the entity names.
     * @return The allowed tags.
     */
    public String[] getAllowedTags() {
        String[] allowedTags = null;
        if (format.equals(LabelFormat.BIO)) {
            allowedTags = new String[]{LabelTag.B.toString(), LabelTag.I.toString()};
        } else if (format.equals(LabelFormat.IO)) {
            allowedTags = new String[]{LabelTag.I.toString()};
        } else if (format.equals(LabelFormat.BMEWO)) {
            allowedTags = new String[]{LabelTag.B.toString(), LabelTag.M.toString(), LabelTag.E.toString(), LabelTag.W.toString()};
        }
        return allowedTags;
    }

    /**
     * Save corpus in a file.
     * @param file File to save the corpus with the tokens, features and labels.
     * @throws GimliException Problem writing the data in the output file.
     */
    public void writeToFile(final String file) throws GimliException {
        logger.info("Writing corpus in CoNNL format to file: {}", file);
        GZIPOutputStream out;
        try {
            out = new GZIPOutputStream(new FileOutputStream(file));
        }
        catch (IOException ex) {
            throw new GimliException("It was not possible to create the compressed output file.", ex);
        }

        Sentence s;
        try {
            for (int i = 0; i < size(); i++) {
                s = getSentence(i);
                out.write(s.getId().getBytes());
                out.write("\n".getBytes());

                out.write(s.toExportFormat().getBytes());
            }
            out.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was an error writing data to the compressed file.", ex);
        }
    }

    /**
     * Load a corpus from a file in the CoNNL format, which should be obtained
     * using the Reader classes that implement the {@link ICorpusReader}
     * interface.
     * @param file The file that contains the corpus.
     * @throws GimliException Problem reading the file that contain the corpus.
     */
    private void loadFromFile(final String file) throws GimliException {

        logger.info("Loading corpus from file: {}", file);

        try {
            InputStreamReader isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
            BufferedReader br = new BufferedReader(isr);
            String line;

            Sentence s = new Sentence(this);
            Token t;
            int start = 0, counter = 0;
            while (( line = br.readLine() ) != null) {
                if (line.equals("") || line.equals("\n")) {

                    s.addAnnotationsFromTags(1.0);
                    addSentence(s);

                    s = new Sentence(this);
                    start = 0;
                    counter = 0;
                } else {
                    if (start == 0) {
                        s.setId(line);
                        line = br.readLine();
                    }

                    t = new Token(s, start, counter, line);
                    s.addToken(t);
                    start += t.getText().length();

                    counter++;
                }
            }
            br.close();
            isr.close();
        }
        catch (IOException ex) {
            throw new GimliException("It was not possible to read the corpus.", ex);
        }

    }

    /**
     * Get the total number of annotations of the corpus.
     * @return The total number of annotations.
     */
    public int getNumberAnnotations() {
        int count = 0;
        for (Sentence s : sentences) {
            count += s.getNumberAnnotations();
        }
        return count;
    }
}
