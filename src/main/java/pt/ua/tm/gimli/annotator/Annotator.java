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
package pt.ua.tm.gimli.annotator;

import cc.mallet.fst.CRF;
import cc.mallet.fst.NoopTransducerTrainer;
import cc.mallet.fst.SumLatticeDefault;
import cc.mallet.fst.Transducer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.model.CRFModel;

/**
 * Class used to annotate any {@link Corpus} using one or several
 * {@link CRFModel} trained by Gimli.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Annotator {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(Annotator.class);
    /**
     * The {@link Corpus} to be annotated by this {@link Annotator}.
     */
    private Corpus corpus;

    /**
     * Constructor.
     * @param corpus {@link Corpus} to be annotated.
     */
    public Annotator(final Corpus corpus) {
        this.corpus = corpus;
    }

    /**
     * Provides access to the used corpus.
     * @return The used {@link Corpus}.
     */
    public Corpus getCorpus() {
        return corpus;
    }

    /**
     * Annotate the corpus using one model.
     * @param model  The {@link CRFModel} that will be used to generate the
     * annotations.
     */
    public void annotate(final CRFModel model) {
        // Clean annotations
        corpus.cleanAnnotations();
        // Garantee that the corpus order follows the model
        boolean corpusReversed = false;
        if (!model.getParsing().equals(corpus.getParsing())) {
            corpus.reverse();
            corpusReversed = true;
        }

        LabelTag p;
        int counter = 0;

        InstanceList testingData = corpus.toModelFormat(model.getCRF().getInputPipe());

        NoopTransducerTrainer crfTrainer = new NoopTransducerTrainer(model.getCRF());
        for (Instance i : testingData) {
            Sequence input = (Sequence) i.getData();
            Transducer tran = crfTrainer.getTransducer();
            Sequence pred = tran.transduce(input);

            // Get score
            double logScore = new SumLatticeDefault(model.getCRF(), input, pred).getTotalWeight();
            double logZ = new SumLatticeDefault(model.getCRF(), input).getTotalWeight();
            double prob = Math.exp(logScore - logZ);

            for (int j = 0; j < pred.size(); j++) {
                p = LabelTag.valueOf(pred.get(j).toString());
                corpus.getSentence(counter).getToken(j).setLabel(p);
            }
            corpus.getSentence(counter).addAnnotationsFromTags(prob);
            counter++;
        }

        if (corpusReversed) {
            corpus.reverse();
        }
    }

    /**
     * Annotate the corpus combining several models of the same entity type.
     * <p>
     * In the combination algorithm, the model that provides an higher
     * confidence value, is the one that will provide the annotations for
     * the sentence.
     * @param models The array of {@link CRFModel} to be combined.
     */
    public void annotate(final CRFModel[] models) {
        if (models.length < 2) {
            logger.error("This method needs more than one model to be used");
            return;
        }

        // Garantee that the corpus is not annotated
        corpus.cleanAnnotations();

        // Put corpus in forward direction
        if (corpus.getParsing().equals(Parsing.BW)) {
            corpus.reverse();
        }

        // Collect CRFs and data
        CRF[] crfs = new CRF[models.length];
        InstanceList[] testingData = new InstanceList[models.length];
        for (int i = 0; i < models.length; i++) {
            crfs[i] = models[i].getCRF();

            if (models[i].getParsing().equals(Parsing.BW)) {
                corpus.reverse();
                testingData[i] = corpus.toModelFormat(crfs[i].getInputPipe());
                corpus.reverse();
            } else {
                testingData[i] = corpus.toModelFormat(crfs[i].getInputPipe());
            }
        }


        double maxProb;
        List<LabelTag> bestLabels, labels;
        for (int i = 0; i < testingData[0].size(); i++) {

            maxProb = 0.0;
            bestLabels = null;
            for (int j = 0; j < models.length; j++) {
                Sequence input = (Sequence) testingData[j].get(i).getData();
                NoopTransducerTrainer crfTrainer = new NoopTransducerTrainer(crfs[j]);
                Transducer tran = crfTrainer.getTransducer();
                Sequence pred = tran.transduce(input);

                // Reverse labels if necessary
                labels = new ArrayList<LabelTag>();
                for (int k = 0; k < pred.size(); k++) {
                    labels.add(LabelTag.valueOf(pred.get(k).toString()));
                }
                if (models[j].getParsing().equals(Parsing.BW)) {
                    Collections.reverse(labels);
                }

                // Get score
                double logScore = new SumLatticeDefault(crfs[j], input, pred).getTotalWeight();
                double logZ = new SumLatticeDefault(crfs[j], input).getTotalWeight();
                double prob = Math.exp(logScore - logZ);

                if (prob > maxProb) {
                    maxProb = prob;
                    bestLabels = labels;
                }
            }

            for (int j = 0; j < bestLabels.size(); j++) {
                corpus.getSentence(i).getToken(j).setLabel(bestLabels.get(j));
            }
            corpus.getSentence(i).addAnnotationsFromTags(maxProb);

        }
    }
}
