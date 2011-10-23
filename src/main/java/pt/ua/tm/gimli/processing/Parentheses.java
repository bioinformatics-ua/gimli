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
package pt.ua.tm.gimli.processing;

import pt.ua.tm.gimli.corpus.Annotation;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.corpus.Token;

/**
 * Perform parentheses post-processing.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Parentheses {

    /**
     * Process corpus by removing annotations with inconsistent number of
     * annotations.
     * @param c The corpus to be processed.
     */
    public static void processRemoving(Corpus c) {
        Sentence s;
        Annotation a;
        for (int i = 0; i < c.size(); i++) {
            s = c.getSentence(i);
            for (int j = 0; j < s.getNumberAnnotations(); j++) {
                a = s.getAnnotation(j);
                if (!isParanthesesBalanced(a)) {
                    s.removeAnnotation(a);
                }
            }
        }
    }

    /**
     * Process corpus by correcting annotations.
     * @param c The corpus to be processed.
     */
    public static void processCorrecting(Corpus c) {
        Sentence s;
        Annotation a;
        for (int i = 0; i < c.size(); i++) {
            s = c.getSentence(i);
            for (int j = 0; j < s.getNumberAnnotations(); j++) {
                a = s.getAnnotation(j);

                if (!isParanthesesBalanced(a) && !extendLeft(a) && !extendRight(a) && !shrinkLeft(a) && !shrinkRight(a)) {
                    s.removeAnnotation(a);
                }
            }
        }
    }

    /**
     * Extend the annotation to the left side by adding tokens until the
     * previous parentheses.
     * @param a The annotation to be extended.
     * @return <code>True</code> if the annotation was extended, and
     * <code>False</code> otherwise.
     */
    private static boolean extendLeft(Annotation a) {
        Token t;
        Annotation na;
        for (int k = a.getStartIndex() - 1; k >= 0; k--) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t) || hasClose(t)) {
                na = new Annotation(a.getSentence(), k, a.getEndIndex(), a.getScore());
                if (isParanthesesBalanced(na)) {
                    a.getSentence().removeAnnotation(a);
                    a.getSentence().addAnnotation(na);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extend the annotation to the right side by adding tokens until the
     * next parentheses.
     * @param a The annotation to be extended.
     * @return <code>True</code> if the annotation was extended, and
     * <code>False</code> otherwise.
     */
    private static boolean extendRight(Annotation a) {
        Token t;
        Annotation na;
        for (int k = a.getEndIndex() + 1; k < a.getSentence().size(); k++) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t) || hasClose(t)) {
                na = new Annotation(a.getSentence(), a.getStartIndex(), k, a.getScore());
                if (isParanthesesBalanced(na)) {
                    a.getSentence().removeAnnotation(a);
                    a.getSentence().addAnnotation(na);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Shrink the annotation in the left side by removing tokens until the
     * next parentheses.
     * @param a The annotation to be shrunk.
     * @return <code>True</code> if the annotation was shrunk, and
     * <code>False</code> otherwise.
     */
    private static boolean shrinkLeft(Annotation a) {
        Token t;
        Annotation na;
        for (int k = a.getStartIndex(); k < a.getEndIndex(); k++) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t) || hasClose(t)) {
                na = new Annotation(a.getSentence(), k + 1, a.getEndIndex(), a.getScore());
                if (isParanthesesBalanced(na)) {
                    a.getSentence().removeAnnotation(a);
                    a.getSentence().addAnnotation(na);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Shrink the annotation in the right side by removing tokens until the
     * previous parentheses.
     * @param a The annotation to be shrunk.
     * @return <code>True</code> if the annotation was shrunk, and
     * <code>False</code> otherwise.
     */
    private static boolean shrinkRight(Annotation a) {
        Token t;
        Annotation na;
        for (int k = a.getEndIndex(); k > a.getStartIndex(); k--) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t) || hasClose(t)) {
                na = new Annotation(a.getSentence(), a.getStartIndex(), k - 1, a.getScore());
                if (isParanthesesBalanced(na)) {
                    a.getSentence().removeAnnotation(a);
                    a.getSentence().addAnnotation(na);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the number of parentheses of an annotation is odd.
     * @param a The annotation to be analyzed.
     * @return <code>True</code> if the annotation contains an odd number of
     * parentheses, and <code>False</code> otherwise.
     */
    private static boolean isParanthesesBalanced(Annotation a) {
        int countOpen = 0, countClose = 0;
        Token t;
        for (int k = a.getStartIndex(); k <= a.getEndIndex(); k++) {
            t = a.getSentence().getToken(k);
            if (hasOpen(t)) {
                countOpen++;
            }
            if (hasClose(t)) {
                countClose++;
            }
        }
        if (countOpen != countClose) {
            return false;
        }
        return true;
    }

    /**
     * Check if a token contains an open parentheses.
     * @param t The token to be analyzed.
     * @return <code>True</code> if the annotation contains an open parentheses,
     * and <code>False</code> otherwise.
     */
    private static boolean hasOpen(Token t) {
        String text = t.getText();
        if (text.contains("(")) {
            return true;
        }
        if (text.contains("[")) {
            return true;
        }
        if (text.contains("{")) {
            return true;
        }
        return false;
    }

    /**
     * Check if a token contains an close parentheses.
     * @param t The token to be analyzed.
     * @return <code>True</code> if the annotation contains an close parentheses,
     * and <code>False</code> otherwise.
     */
    private static boolean hasClose(Token t) {
        String text = t.getText();
        if (text.contains(")")) {
            return true;
        }
        if (text.contains("]")) {
            return true;
        }
        if (text.contains("}")) {
            return true;
        }
        return false;
    }
}
