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
package pt.ua.tm.gimli.model;

import pt.ua.tm.gimli.config.ModelConfig;
import cc.mallet.fst.CRF;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.types.InstanceList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.exception.GimliException;

/**
 * Basic CRF characteristics and features.
 * CRF Model implementations should extend {@link CRFBase}.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public abstract class CRFBase implements ICRFBase {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(CRFBase.class);
    /**
     * Configurarion of the model, including features and order.
     */
    private ModelConfig config;
    /**
     * The CRF model.
     */
    private CRF crf;
    /**
     * Parsing direction of the model.
     */
    private Parsing parsing;

    /**
     * Constructor.
     * @param config Model configuration.
     * @param parsing Parsing direction.
     */
    public CRFBase(final ModelConfig config, final Parsing parsing) {
        this.config = config;
        this.parsing = parsing;
    }

    /**
     * Constructor that loads the model from a file.
     * @param config Model configuration.
     * @param parsing Parsing direction.
     * @param file File that contains the model.
     * @throws GimliException Problem loading the model from file.
     */
    public CRFBase(final ModelConfig config, final Parsing parsing, final String file) throws GimliException {
        this.config = config;
        this.parsing = parsing;
        loadFromFile(file);
    }

    /**
     * Get configuration of the model.
     * @return The model configuration.
     */
    public ModelConfig getConfig() {
        return config;
    }

    /**
     * Get the {@link CRF} model.
     * @return The CRF model.
     */
    public CRF getCRF() {
        return crf;
    }

    /**
     * Get the parsing direction of the model.
     * @return Parsing direction.
     */
    public Parsing getParsing() {
        return parsing;
    }

    /**
     * Set the configuration of the model.
     * @param config The new configuration.
     */
    public void setConfig(final ModelConfig config) {
        this.config = config;
    }

    /**
     * Set the CRF of the model.
     * @param crf The new CRF.
     */
    public void setCRF(final CRF crf) {
        this.crf = crf;
    }

    /**
     * Set the parsing direction of the model.
     * @param parsing The new parsing direction.
     */
    public void setParsing(final Parsing parsing) {
        this.parsing = parsing;
    }

    /**
     * 
     * @param corpus
     * @throws GimliException 
     */
    @Override
    public abstract void train(Corpus corpus) throws GimliException;

    /**
     * Implementation of the test capability, in order to provide feedback
     * about the performance of the model.
     * @param corpus The corpus where the model should be tested.
     * @throws GimliException Problem testing the corpus.
     */
    @Override
    public void test(final Corpus corpus) throws GimliException {

        // Load test Data
        InstanceList testingData = corpus.toModelFormat(crf.getInputPipe());

        // Define Evaluator
        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{testingData},
                new String[]{"test"}, corpus.getAllowedTags(), corpus.getAllowedTags()) {
        };

        // Evaluate
        NoopTransducerTrainer crfTrainer = new NoopTransducerTrainer(crf);
        evaluator.evaluateInstanceList(crfTrainer, testingData, "test");
    }

    /**
     * Load model from file.
     * @param file The file that contains the model.
     * @throws GimliException Problem reading the input file.
     */
    private void loadFromFile(final String file) throws GimliException {
        logger.info("Loading model from file: {}", file);
        ObjectInputStream ois = null;
        CRF crf = null;
        try {
            ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
            crf = (CRF) ois.readObject();
            ois.close();
        } catch (ClassNotFoundException ex) {
            throw new GimliException("Provided model is not in CRF format.", ex);
        } catch (IOException ex) {
            throw new GimliException("There was a problem loading the CRF model from file.", ex);
        }
        this.crf = crf;
    }

    /**
     * Write the model into a file.
     * @param file The file to store the model.
     * @throws GimliException Problem writing the output file.
     */
    public void writeToFile(final String file) throws GimliException {
        logger.info("Writing model to file: {}", file);
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
            oos.writeObject(crf);
            oos.close();
        } catch (IOException ex) {
            throw new GimliException("There was a problem writing the model to file.", ex);
        }
    }
}
