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
package pt.ua.tm.gimli.corpus;

/**
 * Used to represent annotation.
 * {@link Sentence}.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Annotation {

    /**
     * The annotation is part of an sentence.
     */
    private Sentence sentence;
    /**
     * Index of the first token that is inside the annotation.
     */
    private int startIndex;
    /**
     * Index of the last token that is inside the annotation.
     */
    private int endIndex;
    /**
     * Confidence value to generate the annotation.
     */
    private double score;

    /**
     * Constructor.
     * @param s The sentence to which the annotation is associated.
     * @param startIndex The index of the first token of the
     * annotation in the sentence.
     * @param endIndex The index of the last token of the annotation in the
     * sentence.
     * @param score The confidence value to generate the annotation.
     */
    public Annotation(final Sentence s, final int startIndex,
            final int endIndex, final double score) {
        this.sentence = s;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.score = score;
    }

    /**
     * Provides access to the confidence value.
     * @return The confidence value to generate the annotation.
     */
    public double getScore() {
        return score;
    }

    /**
     * Get text of the annotation.
     * @return Annotation text
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i <= endIndex; i++) {
            sb.append(sentence.getToken(i).getText());
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Provides access to the index of the last token of the annotation.
     * @return The index of the token.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Provides access to the sentence.
     * @return The sentence.
     */
    public Sentence getSentence() {
        return sentence;
    }

    /**
     * Provides access to the index of the first token of the annotation.
     * @return The index of the token.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Provides a textual representation of the annotation, based on the token
     * indexes.
     * @return A {@link String} representing the annotation.
     */
    @Override
    public String toString() {
        return "(" + startIndex + "," + endIndex + ")";
    }

    /**
     * Used to compare 2 annotations.
     * @param obj The annotation to be compared with.
     * @return <code>True</code> if the two annotations are equal, and
     * <code>False</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Annotation other = (Annotation) obj;
        if (this.sentence != other.sentence && ( this.sentence == null
                || !this.sentence.equals(other.sentence) )) {
            return false;
        }
        if (this.startIndex != other.startIndex) {
            return false;
        }
        if (this.endIndex != other.endIndex) {
            return false;
        }
        return true;
    }

    /**
     * Override the hashCode method to consider all the internal variables.
     * @return Unique number for each annotation.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + ( this.sentence != null ? this.sentence.hashCode() : 0 );
        hash = 79 * hash + this.startIndex;
        hash = 79 * hash + this.endIndex;
        return hash;
    }

    /**
     * Verify if one annotation contains the other. In other words, verify the
     * nested matching alignment.
     * @param a The annotation that should be contained in the current one.
     * @return <code>True</code> if the argument annotation is contained in the
     * current one, and
     * <code>False</code> otherwise.
     */
    public boolean contains(Annotation a) {
        if (this.sentence != a.sentence && ( this.sentence == null|| !this.sentence.equals(a.sentence) )) {
            return false;
        }

        if (this.startIndex <= a.getStartIndex() && this.endIndex >= a.getEndIndex()) {
            return true;
        }
        if (a.getStartIndex() <= this.startIndex && a.getEndIndex() >= this.endIndex) {
            return true;
        }
        return false;
    }
}
