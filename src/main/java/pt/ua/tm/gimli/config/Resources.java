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
package pt.ua.tm.gimli.config;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.util.Streams;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Properties;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ua.tm.gimli.external.snowball.EnglishStemmer;

/**
 * Access external resources, such as dictionaries, tokeniser and stemming
 * models.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Resources {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Resources.class);
    /**
     * Tokenizer
     */
    private static TokenizerME tokenizer = null;
    /**
     * Part-of-speech tagger.
     */
    private static HmmDecoder pos = null;
    /**
     * Stemmer.
     */
    private static EnglishStemmer stemmer = null;
    /**
     * File with resources configuration.
     */
    private static String fileName = "resources.properties";
    /**
     * Resources properties.
     */
    private static Properties properties = null;

    /**
     * Load Resources Properties.
     */
    private static void loadProperties() {
        if (properties == null) {
            try {
                properties = new Properties();
                properties.load(ClassLoader.getSystemResourceAsStream(fileName));
            }
            catch (IOException ex) {
                throw new RuntimeException("Can't load resources properties file");
            }
        }
    }

    /**
     * Load tokenizer.
     * @return loaded tokenizer with the model.
     */
    public static TokenizerME getTokenizer() {
        loadProperties();

        if (tokenizer == null) {
            String tokenizerModel = properties.getProperty("tokenizer");
            logger.info("Loading tokenizer: {}", tokenizerModel);
            InputStream modelIn = ClassLoader.getSystemResourceAsStream(tokenizerModel);

            TokenizerModel model = null;
            try {
                model = new TokenizerModel(modelIn);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            tokenizer = new TokenizerME(model);
        }

        return tokenizer;
    }

    /**
     * Load stemmer.
     * @return loaded stemmer.
     */
    public static EnglishStemmer getStemmer() {
        loadProperties();

        if (stemmer == null) {
            stemmer = new EnglishStemmer();
        }
        return stemmer;
    }

    /**
     * Load POS tagger.
     * @return loaded tagger using the model.
     */
    public static HmmDecoder getPos() {
        loadProperties();

        if (pos == null) {
            String posModel = properties.getProperty("pos");
            logger.info("Loading pos tagger: {}", posModel);
            try {
                ObjectInputStream objIn = new ObjectInputStream(ClassLoader.getSystemResourceAsStream(posModel));
                HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
                Streams.closeInputStream(objIn);
                pos = new HmmDecoder(hmm);

            }
            catch (FileNotFoundException ex) {
                throw new RuntimeException("POS model file not found.", ex);
            }
            catch (IOException ex) {
                throw new RuntimeException("There was a problem reading the POS model.", ex);
            }
            catch (ClassNotFoundException ex) {
                throw new RuntimeException("Illegal POS model format.", ex);
            }
        }
        return pos;
    }

    /**
     * Load lexicon stream.
     * @param name Name of the lexicon.
     * @return InputStream to the lexicon file.
     */
    public static InputStream getLexicon(String name) {
        loadProperties();

        logger.info("Loading lexicon: {}", name);
        String file = properties.getProperty(name);
        if (file == null) {
            throw new RuntimeException("Lexicon " + name + " does not exist.");
        }

        InputStream is = ClassLoader.getSystemResourceAsStream(file);
        if (is == null) {
            throw new RuntimeException("Lexicon " + name + " does not exist.");
        }

        return is;
    }

    /**
     * Show used resources.
     */
    public static void print() {
        loadProperties();
        logger.info("RESOURCES:");
        logger.info("Tokenizer: {}", properties.getProperty("tokenizer"));
        logger.info("POS: {}", properties.getProperty("pos"));
        logger.info("PRGE: {}", properties.getProperty("prge"));
        logger.info("Aminoacid: {}", properties.getProperty("aminoacid"));
        logger.info("Nucleicacid: {}", properties.getProperty("nucleicacid"));
        logger.info("Nucleotide: {}", properties.getProperty("nucleotide"));
        logger.info("Nucleoside: {}", properties.getProperty("nucleoside"));
        logger.info("Nucleobase: {}", properties.getProperty("nucleobase"));
        logger.info("Verbs: {}", properties.getProperty("verbs"));
        logger.info("Greek: {}", properties.getProperty("greek"));
        logger.info("Stopwords: {}", properties.getProperty("stopwords"));
    }
}
