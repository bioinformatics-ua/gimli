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

/**
 * Represent GDep Token.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class GDepToken {

    /**
     * The text of the token.
     */
    private String text;
    /**
     * The lemma of the token.
     */
    private String lemma;
    /**
     * The part-of-speech tag of the token.
     */
    private String pos;
    /**
     * The chunk tag of the token.
     */
    private String chunk;
    /**
     * The index of the token related with the dependency.
     */
    private int depToken;
    /**
     * The dependency tag.
     */
    private String depTag;

    /**
     * Constructor.
     * @param text Text of the token.
     * @param lemma Lemma of the token.
     * @param pos Part-of-speech tag of the token.
     * @param chunk Chunk tag of the token.
     * @param depToken Index of the dependency token.
     * @param depTag Dependency tag.
     */
    public GDepToken(String text, String lemma, String pos, String chunk, int depToken, String depTag) {
        this.text = text;
        this.lemma = lemma;
        this.pos = pos;
        this.chunk = chunk;
        this.depToken = depToken;
        this.depTag = depTag;
    }

    /**
     * Get the chunk tag of the token.
     * @return The chunk tag.
     */
    public String getChunk() {
        return chunk;
    }

    /**
     * Set the chunk tag of the token.
     * @param chunk The chunk tag.
     */
    public void setChunk(final String chunk) {
        this.chunk = chunk;
    }

    /**
     * Get the dependency tag.
     * @return The dependency tag.
     */
    public String getDepTag() {
        return depTag;
    }

    /**
     * Set the dependency tag.
     * @param depTag The dependency tag.
     */
    public void setDepTag(final String depTag) {
        this.depTag = depTag;
    }

    /**
     * Get the index of the token of the dependency.
     * @return The index of the token.
     */
    public int getDepToken() {
        return depToken;
    }

    /**
     * Set the index of the token of the dependency.
     * @param depToken The index of the token.
     */
    public void setDepToken(final int depToken) {
        this.depToken = depToken;
    }

    /**
     * Get the lemma of the token.
     * @return The lemma.
     */
    public String getLemma() {
        return lemma;
    }

    /**
     * Set the lemma of the token.
     * @param lemma The lemma.
     */
    public void setLemma(final String lemma) {
        this.lemma = lemma;
    }

    /**
     * Get the part-of-speech (POS) tag of the token.
     * @return The POS tag.
     */
    public String getPOS() {
        return pos;
    }

    /**
     * Set the part-of-speech (POS) tag of the token.
     * @param pos The POS tag.
     */
    public void setPOS(final String pos) {
        this.pos = pos;
    }

    /**
     * Get the text of the token.
     * @return The text.
     */
    public String getText() {
        return text;
    }

    /**
     * Set text of the token.
     * @param text The text.
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Provide text representation of the GDep token.
     * @return The text representation.
     */
    public String toString() {
        return text + "\t" + lemma + "\t" + pos + "\t" + chunk + "\t" + depToken + "\t" + depTag;
    }
}
