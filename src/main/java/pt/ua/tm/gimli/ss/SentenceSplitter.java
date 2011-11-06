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

import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.Transducer;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import java.util.ArrayList;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.exception.GimliException;

/**
 * Provides a Sentence Splitter implementation. It was trained using two
 * corpora: JNLPBA and PennBioIE Oncology. A total of 3561 abstracts was used
 *   for training procedures.
 *   <p>
 *       In order to evaluate the performance of this solution, we merged the
 *       corpora and split it in two parts (both parts contain equivalent
 *       proportions of each corpus.):
 *       <ul>
 *           <li>Training: 2867 abstracts</li>
 *           <li>Testing: 694 abstracts</li>
 *       </ul>
 *
 *       After training the CRF Model using the training corpus, Gimli SS
 *       achieved the following results in the test corpus:
 *       <ul>
 *           <li><strong>Accuracy: 99.89%</strong></li>
 *           <li>Precision: 99.91%</li>
 *           <li>Recall: 99.91%</li>
 *           <li><strong>F1: 99.91%</strong></li>
 *       </ul>
 *       We also made a study of the splitting speed, considering that each
 *       abstract contains an average of 7 sentences:
 *       <ul>
 *           <li><strong>5.3 milliseconds</strong> per abstract.</li>
 *       </ul>
 *   </p>
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class SentenceSplitter {

    private SSModel model;
    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(SentenceSplitter.class);

    /**
     * Constructor.
     * @param fileModel The model to be used on this Sentence Splitter.
     * @throws GimliException Problem reading the model from file.
     */
    public SentenceSplitter(final String fileModel) throws GimliException {
        this.model = new SSModel(null, Parsing.FW, fileModel);
    }

    /**
     * Perform text sentence splitting.
     * @param text The text to be split.
     * @return The list of sentences.
     */
    public ArrayList<String> split(String text) {

        String[] lines = text.split("\n");
        String[][] tokens = tokenize(lines);

        InstanceList testingData = textToModelFormat(tokens, model.getCRF().getInputPipe());
        NoopTransducerTrainer crfTrainer = new NoopTransducerTrainer(model.getCRF());

        int charCounter, prevStart, nonWhiteSpaces;
        LabelTag l;
        Instance inst;
        ArrayList<String> sentences = new ArrayList<String>();
        for (int i = 0; i < testingData.size(); i++) {
            inst = testingData.get(i);
            charCounter = 0;
            prevStart = 0;
            nonWhiteSpaces = 0;

            Sequence input = (Sequence) inst.getData();
            Transducer tran = crfTrainer.getTransducer();
            Sequence pred = tran.transduce(input);

            for (int j = 0; j < pred.size(); j++) {
                l = LabelTag.valueOf(pred.get(j).toString());
                charCounter += tokens[i][j].length();

                if (l.equals(LabelTag.I)) {
                    int k;
                    for (k = prevStart; k < lines[i].length() && nonWhiteSpaces < charCounter; k++) {
                        if (lines[i].charAt(k) != ' ') {
                            nonWhiteSpaces++;
                        }
                    }

                    sentences.add(lines[i].substring(prevStart, k));
                    prevStart = k + 1;
                }
            }

        }

        return sentences;
    }

    /**
     * Convert the text to the format to be provided to the CRF.
     * @param tokens The set of tokens.
     * @param p The pipe to extract features.
     * @return The list of instances to be provided to the CRF.
     */
    private InstanceList textToModelFormat(String[][] tokens, Pipe p) {
        InstanceList instances = new InstanceList(p);
        StringBuilder sb;

        for (int i = 0; i < tokens.length; i++) {
            //Convert tokens to input model format
            sb = new StringBuilder();

            for (int j = 0; j < tokens[i].length; j++) {
                sb.append(tokens[i][j]);
                sb.append("\t");
                sb.append(LabelTag.O);
                sb.append("\n");
            }

            // Add instance
            instances.addThruPipe(new Instance(sb.toString(), null, 0, null));
        }
        return instances;
    }

    /**
     * Perform tokenization.
     * @param lines The lines to be tokenized.
     * @return The respective tokens.
     */
    private String[][] tokenize(String[] lines) {
        String[][] res = new String[lines.length][];

        String[] tokens;
        String line;
        for (int i = 0; i < lines.length; i++) {
            line = lines[i];

            line = line.replaceAll("!", " ! ");
            line = line.replaceAll("[?]", " ? ");
            line = line.replaceAll("[:]", " : ");
            line = line.replaceAll("[\\)]", " \\) ");
            line = line.replaceAll("[\\]]", " \\] ");
            line = line.replaceAll("/", " / ");
            line = line.replaceAll("-", " - ");
            line = line.replaceAll("[.]", " . ");
            line = line.replaceAll("[ ]+", " ");

            tokens = line.split(" ");
            res[i] = tokens;
        }
        return res;
    }
}
