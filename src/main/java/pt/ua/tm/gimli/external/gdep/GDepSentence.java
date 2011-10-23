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
