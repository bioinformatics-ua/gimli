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
package pt.ua.tm.gimli.model;

import pt.ua.tm.gimli.config.ModelConfig;
import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByThreadedLabelLikelihood;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.FeaturesInWindow;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.pipe.tsf.TokenTextCharNGrams;
import cc.mallet.pipe.tsf.TokenTextCharPrefix;
import cc.mallet.pipe.tsf.TokenTextCharSuffix;
import cc.mallet.types.InstanceList;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.features.Input2TokenSequence;
import pt.ua.tm.gimli.features.MixCase;
import pt.ua.tm.gimli.features.WordShape;
import pt.ua.tm.gimli.features.NumberOfCap;
import pt.ua.tm.gimli.features.NumberOfDigit;
import pt.ua.tm.gimli.features.Stemmer;
import pt.ua.tm.gimli.features.WordLength;
import pt.ua.tm.gimli.config.Constants.Parsing;

/**
 * The CRF model used by Gimli, providing features to train and test the models.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class CRFModel extends CRFBase {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(CRFModel.class);
    
    /**
     * Constructor.
     * @param config Model configuration.
     * @param parsing Parsing direction.
     */
    public CRFModel(final ModelConfig config, final Parsing parsing) {
        super(config, parsing);
    }

    /**
     * Constructor that loads the model from an input file.
     * @param config Model configuration.
     * @param parsing Parsing direction.
     * @param file File that contains the model.
     * @throws GimliException Problem reading the model from file.
     */
    public CRFModel(final ModelConfig config, final Parsing parsing, final String file) throws GimliException {
        super(config, parsing, file);
    }

    /**
     * Setup the features to be used by the model.
     * @return The {@link Pipe} that contains the description of the features
     * to be extracted.
     * @throws GimliException Problem specifying the features.
     */
    private Pipe setupPipe() throws GimliException {
        ModelConfig config = getConfig();
        ArrayList<Pipe> pipe = new ArrayList<Pipe>();

        try {
            pipe.add(new Input2TokenSequence(getConfig()));

            if (config.isStem()) {
                pipe.add(new Stemmer("STEM="));
            }

            if (config.isCapitalization()) {
                pipe.add(new RegexMatches("InitCap", Pattern.compile(Constants.CAPS + ".*")));
                pipe.add(new RegexMatches("EndCap", Pattern.compile(".*" + Constants.CAPS)));
                pipe.add(new RegexMatches("AllCaps", Pattern.compile(Constants.CAPS + "+")));
                pipe.add(new RegexMatches("Lowercase", Pattern.compile(Constants.LOW + "+")));
                pipe.add(new MixCase());
                pipe.add(new RegexMatches("DigitsLettersAndSymbol", Pattern.compile("[0-9a-zA-z]+[-%/\\[\\]:;()'\"*=+][0-9a-zA-z]+")));
            }

            if (config.isCounting()) {
                pipe.add(new NumberOfCap());
                pipe.add(new NumberOfDigit());
                pipe.add(new WordLength());
            }

            if (config.isSymbols()) {
                pipe.add(new RegexMatches("Hyphen", Pattern.compile(".*[-].*")));
                pipe.add(new RegexMatches("BackSlash", Pattern.compile(".*[/].*")));
                pipe.add(new RegexMatches("OpenSquare", Pattern.compile(".*[\\[].*")));
                pipe.add(new RegexMatches("CloseSquare", Pattern.compile(".*[\\]].*")));
                pipe.add(new RegexMatches("Colon", Pattern.compile(".*[:].*")));
                pipe.add(new RegexMatches("SemiColon", Pattern.compile(".*[;].*")));
                pipe.add(new RegexMatches("Percent", Pattern.compile(".*[%].*")));
                pipe.add(new RegexMatches("OpenParen", Pattern.compile(".*[(].*")));
                pipe.add(new RegexMatches("CloseParen", Pattern.compile(".*[)].*")));
                pipe.add(new RegexMatches("Comma", Pattern.compile(".*[,].*")));
                pipe.add(new RegexMatches("Dot", Pattern.compile(".*[\\.].*")));
                pipe.add(new RegexMatches("Apostrophe", Pattern.compile(".*['].*")));
                pipe.add(new RegexMatches("QuotationMark", Pattern.compile(".*[\"].*")));
                pipe.add(new RegexMatches("Star", Pattern.compile(".*[*].*")));
                pipe.add(new RegexMatches("Equal", Pattern.compile(".*[=].*")));
                pipe.add(new RegexMatches("Plus", Pattern.compile(".*[+].*")));
            }

            if (config.isNgrams()) {
                pipe.add(new TokenTextCharNGrams("CHARNGRAM=", new int[]{2, 3, 4}));
            }

            if (config.isSuffix()) {
                pipe.add(new TokenTextCharSuffix("2SUFFIX=", 2));
                pipe.add(new TokenTextCharSuffix("3SUFFIX=", 3));
                pipe.add(new TokenTextCharSuffix("4SUFFIX=", 4));
            }

            if (config.isPrefix()) {
                pipe.add(new TokenTextCharPrefix("2PREFIX=", 2));
                pipe.add(new TokenTextCharPrefix("3PREFIX=", 3));
                pipe.add(new TokenTextCharPrefix("4PREFIX=", 4));
            }

            if (config.isMorphology()) {
                pipe.add(new WordShape());
            }

            if (config.isGreek()) {
                pipe.add(new RegexMatches("GREEK", Pattern.compile(Constants.GREEK, Pattern.CASE_INSENSITIVE)));
            }

            if (config.isRoman()) {
                pipe.add(new RegexMatches("ROMAN", Pattern.compile("((?=[MDCLXVI])((M{0,3})((C[DM])|(D?C{0,3}))?((X[LC])|(L?XX{0,2})|L)?((I[VX])|(V?(II{0,2}))|V)?))")));
            }

            if (config.isConjunctions()) {
                pipe.add(new OffsetConjunctions(true, Pattern.compile("LEMMA=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
                pipe.add(new OffsetConjunctions(true, Pattern.compile("POS=.*"), new int[][]{{-1, 0}, {-2, -1}, {0, 1}, {-1, 1}, {-3, -1}}));
            }

            if (config.isWindow()) {
                pipe.add(new FeaturesInWindow("WINDOW_LEMMA=", -3, 3, Pattern.compile("LEMMA=.*"), true));
                pipe.add(new FeaturesInWindow("WINDOW_WORD=", -3, 3, Pattern.compile("WORD=.*"), true));
                pipe.add(new FeaturesInWindow("WINDOW_LEXICON=", -3, 3, Pattern.compile("LEXICON=.*"), true));
                pipe.add(new FeaturesInWindow("WINDOW_SPECIAL=", -3, 3, Pattern.compile("SPECIAL=.*"), true));
                pipe.add(new FeaturesInWindow("WINDOW_FEATURES=", -1, 1));
            }

            //pipe.add(new PrintTokenSequenceFeatures());

            pipe.add(new TokenSequence2FeatureVectorSequence(true, true));

        }
        catch (Exception ex) {
            throw new GimliException("There was a problem initializing the features.", ex);
        }
        return new SerialPipes(pipe);
    }

    /**
     * Train the CRF model.
     * @throws GimliException Problem training the model.
     */
    @Override
    public void train(final Corpus corpus) throws GimliException {
        ModelConfig config = getConfig();

        //Set pipe
        Pipe p = setupPipe();

        // Load Data
        logger.info("Extracting features and converting data into training format...");
        InstanceList trainingData = corpus.toModelFormat(p);

        // Define CRF
        int order = config.getOrder() + 1;
        int[] orders = new int[order];
        for (int i = 0; i < order; i++) {
            orders[i] = i;
        }

        CRF crf = new CRF(trainingData.getPipe(), (Pipe) null);
        String startStateName = crf.addOrderNStates(
                trainingData,
                orders,
                null, // "defaults" parameter; see mallet javadoc
                "O",
                corpus.getForbiddenPattern(),
                null,
                true); // true for a fully connected CRF

        for (int i = 0; i < crf.numStates(); i++) {
            crf.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
        }
        crf.getState(startStateName).setInitialWeight(0.0);
        crf.setWeightsDimensionAsIn(trainingData, true);

        // Train with Threads
        int numThreads = 32;
        CRFTrainerByThreadedLabelLikelihood crfTrainer = new CRFTrainerByThreadedLabelLikelihood(crf, numThreads);
        crfTrainer.train(trainingData);
        crfTrainer.shutdown();

        TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
                new InstanceList[]{trainingData},
                new String[]{"train"}, corpus.getAllowedTags(), corpus.getAllowedTags()) {
        };
        evaluator.evaluate(crfTrainer);

        setCRF(crf);
    }
}
