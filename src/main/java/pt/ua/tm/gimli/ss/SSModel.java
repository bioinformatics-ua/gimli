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
package pt.ua.tm.gimli.ss;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByThreadedLabelLikelihood;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.SumLatticeDefault;
import cc.mallet.fst.Transducer;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureVectorSequence;
import cc.mallet.pipe.tsf.OffsetConjunctions;
import cc.mallet.pipe.tsf.RegexMatches;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.config.Constants.EntityType;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.config.ModelConfig;
import pt.ua.tm.gimli.corpus.Annotation;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.features.Input2TokenSequence;
import pt.ua.tm.gimli.features.WordLength;
import pt.ua.tm.gimli.features.WordShape;
import pt.ua.tm.gimli.model.CRFBase;

/**
 * Use to train a CRF model for sentence splitting.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.1
 * @since 1.1
 */
public class SSModel extends CRFBase {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(SSModel.class);

    /**
     * Constructor.
     * @param config Configuration of the model.
     * @param parsing Parsing direction.
     */
    public SSModel(final ModelConfig config, final Parsing parsing) {
        super(config, parsing);
    }

    /**
     * Constructor.
     * @param config Configuration of the model.
     * @param parsing Parsing directions.
     * @param fileModel Name of the file to load the model.
     * @throws GimliException Problem loading the model from file.
     */
    public SSModel(final ModelConfig config, final Parsing parsing, final String fileModel) throws GimliException {
        super(config, parsing, fileModel);
    }

    /**
     * Setup features pipe.
     * @return The features pipe.
     */
    private Pipe setupPipe() {
        ModelConfig c = getConfig();
        ArrayList<Pipe> pipe = new ArrayList<Pipe>();

        pipe.add(new Input2TokenSequence(c));

        // EOS Symbols
        if (c.isSymbols()) {
            pipe.add(new RegexMatches("CloseSquare", Pattern.compile(".*[\\]].*")));
            pipe.add(new RegexMatches("Colon", Pattern.compile(".*[:].*")));
            pipe.add(new RegexMatches("CloseParen", Pattern.compile(".*[)].*")));
            pipe.add(new RegexMatches("Dot", Pattern.compile(".*[\\.].*")));
            pipe.add(new RegexMatches("Question", Pattern.compile(".*[\\?].*")));
            pipe.add(new RegexMatches("Exclamation", Pattern.compile(".*[\\!].*")));
        }
        // Token length
        if (c.isCounting()) {
            pipe.add(new WordLength());
        }

        // Orthographic features
        if (c.isCapitalization()) {
            pipe.add(new RegexMatches("InitCap", Pattern.compile(Constants.CAPS + ".*")));
            pipe.add(new RegexMatches("AllCaps", Pattern.compile(Constants.CAPS + "+")));
            pipe.add(new RegexMatches("Lowercase", Pattern.compile(Constants.LOW + "+")));
        }
        // Word shape features (morphological)
        if (c.isMorphology()) {
            pipe.add(new WordShape());
        }

        // Conjunctions
        if (c.isConjunctions()) {
            pipe.add(new OffsetConjunctions(new int[][]{{-2}, {-1}, {0}, {1}, {2}}));
        }

        //pipe.add(new PrintTokenSequenceFeatures());
        pipe.add(new TokenSequence2FeatureVectorSequence(true, true));

        return new SerialPipes(pipe);
    }

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

    @Override
    public void test(final Corpus corpus) throws GimliException {
        InstanceList testingData = corpus.toModelFormat(this.getCRF().getInputPipe());

        Corpus gold = corpus;
        Corpus silver = corpus.clone();
        silver.cleanAnnotations();

        int counter = 0;
        LabelTag p;

        NoopTransducerTrainer crfTrainer = new NoopTransducerTrainer(getCRF());
        for (Instance i : testingData) {
            Sequence input = (Sequence) i.getData();
            Transducer tran = crfTrainer.getTransducer();
            Sequence pred = tran.transduce(input);

            // Get score
            double logScore = new SumLatticeDefault(getCRF(), input, pred).getTotalWeight();
            double logZ = new SumLatticeDefault(getCRF(), input).getTotalWeight();
            double prob = Math.exp(logScore - logZ);

            for (int j = 0; j < pred.size(); j++) {
                p = LabelTag.valueOf(pred.get(j).toString());
                silver.getSentence(counter).getToken(j).setLabel(p);
            }
            silver.getSentence(counter).addAnnotationsFromTags(prob);
            counter++;
        }

        // Get results
        int tp, tn, fp, fn;
        tp = tn = fp = fn = 0;

        Token tgold, tsilver;
        for (int i = 0; i < gold.size(); i++) {
            for (int j = 0; j < gold.getSentence(i).size(); j++) {

                tgold = gold.getSentence(i).getToken(j);
                tsilver = silver.getSentence(i).getToken(j);

                if (isValidEOSToken(tgold.getText())) {
                    if (tgold.getLabel().equals(LabelTag.I) && tsilver.getLabel().equals(LabelTag.I)) {
                        tp++;
                    }
                    if (tgold.getLabel().equals(LabelTag.O) && tsilver.getLabel().equals(LabelTag.O)) {
                        tn++;
                    }
                    if (tgold.getLabel().equals(LabelTag.I) && tsilver.getLabel().equals(LabelTag.O)) {
                        fn++;
                    }
                    if (tgold.getLabel().equals(LabelTag.O) && tsilver.getLabel().equals(LabelTag.I)) {
                        fp++;
                    }
                }
            }
        }

        int correct = tp + tn;
        int wrong = fp + fn;
        int total = tp + tn + fp + fn;

        double accuracy = (double) ( (double) correct / (double) total );
        double precision = (double) tp / (double) ( tp + fp );
        double recall = (double) tp / (double) ( tp + fn );
        double tpr = (double) tp / (double) ( tp + fn );
        double fpr = (double) fp / (double) ( fp + tn );
        double tnr = (double) tn / (double) ( tn + fp );
        double fdr = (double) fp / (double) ( fp + tp );
        double f1 = 2 * ( ( precision * recall ) / ( precision + recall ) );

        logger.info("TOTAL EOS TOKENS: {}", total);
        logger.info("TOTAL ABSTRACTS: {}", gold.size());
        logger.info("");

        logger.info("TOTAL CORRECT: {}", correct);
        logger.info("TOTAL WRONG: {}", wrong);
        logger.info("");

        logger.info("TP: {}", tp);
        logger.info("FP: {}", fp);
        logger.info("TN: {}", tn);
        logger.info("FN: {}", fn);
        logger.info("");

        logger.info("True Positive Rate (SENSITIVITY): {}", tpr * 100.0);
        logger.info("True Negative Rate (SPECIFICITY): {}", tnr * 100.0);
        logger.info("False Positive Rate: {}", fpr * 100.0);
        logger.info("False Discovery Rate: {}", fdr * 100.0);
        logger.info("");

        logger.info("Accuracy: {}", accuracy * 100.0);
        logger.info("");

        logger.info("Precision: {}", precision * 100.0);
        logger.info("Recall: {}", recall * 100.0);
        logger.info("F1: {}", f1 * 100.0);

    }

    public static void main(String[] args) {
        try {
            Corpus c1 = convertCorpusToSentenceSplitting("/Users/david/Downloads/PennBioIE/train.gz");
            Corpus c2 = convertCorpusToSentenceSplitting("resources/corpus/gold/jnlpba/train/corpus_protein.gz");
            Corpus c3 = convertCorpusToSentenceSplitting("/Users/david/Downloads/PennBioIE/test.gz");
            Corpus c4 = convertCorpusToSentenceSplitting("resources/corpus/gold/jnlpba/test/corpus.gz");

            Corpus c = Corpus.merge(new Corpus[]{c1, c2, c3, c4});
            //Corpus c = Corpus.merge(new Corpus[]{c1, c2});

            //c.writeToFile("/Users/david/Downloads/PennBioIE/train_ss.gz");

            ModelConfig mc = new ModelConfig("config/ss.config");

            SSModel model = new SSModel(mc, Parsing.FW);
            model.train(c);
            model.writeToFile("resources/models/gimli/sentence_splitting_fw_o2.gz");
            //SSModel model = new SSModel(mc, Parsing.FW, "resources/models/gimli/sentence_splitting_fw_o2.gz");

            c1 = convertCorpusToSentenceSplitting("/Users/david/Downloads/PennBioIE/test.gz");
            logger.error("PENNBIOIE");
            model.test(c1);

            c2 = convertCorpusToSentenceSplitting("resources/corpus/gold/jnlpba/test/corpus.gz");
            logger.error("JNLPBA");
            model.test(c2);

            c = Corpus.merge(new Corpus[]{c1, c2});
            logger.error("MERGED");
            model.test(c);
        }
        catch (GimliException ex) {
            logger.error("Problem reading corpus.", ex);
        }
    }

    /**
     * Get a standard corpus and convert it to Sentence Splitting.
     * @param file The file that contains the corpus in the Gimli CoNNL format.
     * @return The corpus formated and tagged for sentence splitting.
     * @throws GimliException Problem reading the corpus from file.
     */
    private static Corpus convertCorpusToSentenceSplitting(final String file) throws GimliException {
        Token token;
        Corpus c = new Corpus(LabelFormat.BIO, EntityType.protein, file);
        c.cleanAnnotations();

        Corpus newCorpus = new Corpus(LabelFormat.IO, EntityType.protein);

        String id = "", lastid = "";
        Sentence s = null;
        int idx;
        String t;
        for (int i = 0; i < c.size(); i++) {
            id = c.getSentence(i).getId();

            if (id.contains("_")) {
                id = id.substring(0, id.indexOf("_"));
            }

            if (!id.equals(lastid)) {
                if (!lastid.equals("")) {
                    newCorpus.addSentence(s);
                }
                s = new Sentence(newCorpus);
                s.setId(id);
            }

            for (int j = 0; j < c.getSentence(i).size(); j++) {
                token = c.getSentence(i).getToken(j).clone(s);
                token.removeFeatures();
                s.addToken(token);
            }

            idx = s.size() - 1;
            t = s.getToken(idx).getText();
            if (isValidEOSToken(t)) {
                s.addAnnotation(new Annotation(s, idx, idx, 1.0));
            }
            lastid = id;
        }
        newCorpus.addSentence(s);
        return newCorpus;
    }

    /**
     * Check if the token is a valid end of sentence token.
     * @param text The text of the token.
     * @return true if it is a valid EOS token, false otherwise.
     */
    private static boolean isValidEOSToken(final String text) {
        Pattern p = Pattern.compile("[.:?!\\)\\]]");
        Matcher m = p.matcher(text);
        return m.matches();
    }
}
