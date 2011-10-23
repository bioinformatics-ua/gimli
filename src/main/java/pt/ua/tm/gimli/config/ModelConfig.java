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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.exception.GimliException;

/**
 * Store the characteristics of the CRF Model, including
 * features usage and model order.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class ModelConfig implements Serializable {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(ModelConfig.class);
    private boolean token;
    private boolean stem;
    private boolean lemma;
    private boolean pos;
    private boolean chunk;
    private boolean nlp;
    private boolean capitalization;
    private boolean counting;
    private boolean symbols;
    private boolean ngrams;
    private boolean suffix;
    private boolean prefix;
    private boolean greek;
    private boolean roman;
    private boolean morphology;
    private boolean prge;
    private boolean concepts;
    private boolean verbs;
    private boolean window;
    private boolean conjunctions;
    private int order;

    /**
     * Constructor that will load the features from a properties file.
     * @param file The file that contains the model properties.
     */
    public ModelConfig(final String file) {
        loadFromFile(file);
    }
    
    
    public ModelConfig(boolean token, boolean stem, boolean lemma, boolean pos,
            boolean chunk, boolean nlp, boolean capitalization, boolean counting,
            boolean symbols, boolean ngrams, boolean suffix, boolean prefix,
            boolean greek, boolean roman, boolean morphology, boolean prge,
            boolean concepts, boolean verbs, boolean window, boolean conjunctions,
            int order) {
        this.token = token;
        this.stem = stem;
        this.lemma = lemma;
        this.pos = pos;
        this.chunk = chunk;
        this.nlp = nlp;
        this.capitalization = capitalization;
        this.counting = counting;
        this.symbols = symbols;
        this.ngrams = ngrams;
        this.suffix = suffix;
        this.prefix = prefix;
        this.greek = greek;
        this.roman = roman;
        this.morphology = morphology;
        this.prge = prge;
        this.concepts = concepts;
        this.verbs = verbs;
        this.window = window;
        this.conjunctions = conjunctions;
        this.order = order;
    }

    /**
     * Load configurations from file.
     * @param file File to load the model configurations.
     */
    private void loadFromFile(final String file) {
        try {
            Properties properties = new Properties();
            FileInputStream fis = new FileInputStream(file);
            properties.load(fis);

            try {
                token = String2Boolean(properties.getProperty("token"));
                stem = String2Boolean(properties.getProperty("stem"));
                lemma = String2Boolean(properties.getProperty("lemma"));
                pos = String2Boolean(properties.getProperty("pos"));
                chunk = String2Boolean(properties.getProperty("chunk"));
                nlp = String2Boolean(properties.getProperty("nlp"));
                capitalization = String2Boolean(properties.getProperty("capitalization"));
                counting = String2Boolean(properties.getProperty("counting"));
                symbols = String2Boolean(properties.getProperty("symbols"));
                ngrams = String2Boolean(properties.getProperty("ngrams"));
                suffix = String2Boolean(properties.getProperty("suffix"));
                prefix = String2Boolean(properties.getProperty("prefix"));
                greek = String2Boolean(properties.getProperty("greek"));
                roman = String2Boolean(properties.getProperty("roman"));
                morphology = String2Boolean(properties.getProperty("morphology"));
                prge = String2Boolean(properties.getProperty("prge"));
                concepts = String2Boolean(properties.getProperty("concepts"));
                verbs = String2Boolean(properties.getProperty("verbs"));
                window = String2Boolean(properties.getProperty("window"));
                conjunctions = String2Boolean(properties.getProperty("conjunctions"));
                order = Integer.parseInt(properties.getProperty("order"));
            }
            catch (GimliException ex) {
                throw new RuntimeException("There was a problem loading the features.", ex);
            }
            fis.close();
        }
        catch (IOException ex) {
            throw new RuntimeException("There was a problem loading the features.", ex);
        }
    }

    public void print() {
        logger.info("Token: {}", token);
        logger.info("Stem: {}", stem);
        logger.info("Lemma: {}", lemma);
        logger.info("POS: {}", pos);
        logger.info("Chunk: {}", chunk);
        logger.info("NLP: {}", nlp);
        logger.info("Capitalization: {}", capitalization);
        logger.info("Counting: {}", counting);
        logger.info("Symbols: {}", symbols);
        logger.info("NGrams: {}", ngrams);
        logger.info("Suffix: {}", suffix);
        logger.info("Prefix: {}", prefix);
        logger.info("Greek: {}", greek);
        logger.info("Roman: {}", roman);
        logger.info("Word Shape: {}", morphology);
        logger.info("PRGE: {}", prge);
        logger.info("Biomedical Concepts: {}", concepts);
        logger.info("Verbs: {}", verbs);
        logger.info("Window: {}", window);
        logger.info("Conjunctions: {}", conjunctions);
        logger.info("Order: {}", order);
    }

    private boolean String2Boolean(String s) throws GimliException {
        if (s.equals("1")) {
            return true;
        } else if (s.equals("0")) {
            return false;
        }
        throw new GimliException("String value must be 0 or 1 to be converted to boolean.");
    }

    public boolean isCapitalization() {
        return capitalization;
    }

    public void setCapitalization(boolean capitalization) {
        this.capitalization = capitalization;
    }

    public boolean isChunk() {
        return chunk;
    }

    public void setChunk(boolean chunk) {
        this.chunk = chunk;
    }

    public boolean isConcepts() {
        return concepts;
    }

    public void setConcepts(boolean concepts) {
        this.concepts = concepts;
    }

    public boolean isConjunctions() {
        return conjunctions;
    }

    public void setConjunctions(boolean conjunctions) {
        this.conjunctions = conjunctions;
    }

    public boolean isCounting() {
        return counting;
    }

    public void setCounting(boolean counting) {
        this.counting = counting;
    }

    public boolean isGreek() {
        return greek;
    }

    public void setGreek(boolean greek) {
        this.greek = greek;
    }

    public boolean isLemma() {
        return lemma;
    }

    public void setLemma(boolean lemma) {
        this.lemma = lemma;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        ModelConfig.logger = logger;
    }

    public boolean isMorphology() {
        return morphology;
    }

    public void setMorphology(boolean morphology) {
        this.morphology = morphology;
    }

    public boolean isNgrams() {
        return ngrams;
    }

    public void setNgrams(boolean ngrams) {
        this.ngrams = ngrams;
    }

    public boolean isNLP() {
        return nlp;
    }

    public void setNLP(boolean nlp) {
        this.nlp = nlp;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isPos() {
        return pos;
    }

    public void setPos(boolean pos) {
        this.pos = pos;
    }

    public boolean isPrefix() {
        return prefix;
    }

    public void setPrefix(boolean prefix) {
        this.prefix = prefix;
    }

    public boolean isPrge() {
        return prge;
    }

    public void setPrge(boolean prge) {
        this.prge = prge;
    }

    public boolean isRoman() {
        return roman;
    }

    public void setRoman(boolean roman) {
        this.roman = roman;
    }

    public boolean isStem() {
        return stem;
    }

    public void setStem(boolean stem) {
        this.stem = stem;
    }

    public boolean isSuffix() {
        return suffix;
    }

    public void setSuffix(boolean suffix) {
        this.suffix = suffix;
    }

    public boolean isSymbols() {
        return symbols;
    }

    public void setSymbols(boolean symbols) {
        this.symbols = symbols;
    }

    public boolean isToken() {
        return token;
    }

    public void setToken(boolean token) {
        this.token = token;
    }

    public boolean isVerbs() {
        return verbs;
    }

    public void setVerbs(boolean verbs) {
        this.verbs = verbs;
    }

    public boolean isWindow() {
        return window;
    }

    public void setWindow(boolean window) {
        this.window = window;
    }
}
