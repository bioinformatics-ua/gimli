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
package pt.ua.tm.gimli.external.gdep;

import java.util.ArrayList;

/**
 * Represents a GDep sentence.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class GDepSentence {

    /**
     * The corpus of this sentence.
     */
    private GDepCorpus corpus;
    /**
     * A sentence is a set of tokens.
     */
    private ArrayList<GDepToken> sentence;

    /**
     * Constructor.
     * @param corpus The corpus of the sentence.
     */
    public GDepSentence(final GDepCorpus corpus) {
        this.corpus = corpus;
        this.sentence = new ArrayList<GDepToken>();
    }

    /**
     * Add token to sentence.
     * @param token The token.
     */
    public void addToken(final GDepToken token) {
        sentence.add(token);
    }

    /**
     * Get specific token of the sentence.
     * @param i The index of the token.
     * @return The GDep token.
     */
    public GDepToken getToken(final int i) {
        return sentence.get(i);
    }

    /**
     * Get the size of the sentence, the number of tokens.
     * @return The number of tokens.
     */
    public int size() {
        return sentence.size();
    }

    /**
     * Get the GDep corpus of the GDep sentence.
     * @return The GDep corpus.
     */
    public GDepCorpus getCorpus() {
        return corpus;
    }
}
