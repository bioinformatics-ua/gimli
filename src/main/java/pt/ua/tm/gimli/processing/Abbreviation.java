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

import pt.ua.tm.gimli.external.biotext.ExtractAbbreviations;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import pt.ua.tm.gimli.corpus.Annotation;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.corpus.Sentence;

/**
 * Perform abbreviation resolution post-processing.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Abbreviation {

    /**
     * Process the corpus by getting the abbreviations, and extending the
     * existent annotations.
     * @param c The corpus to be processed.
     */
    public static void process(Corpus c) {
        Sentence s;
        String text;
        HashMap<String, String> pairs;
        Set<String> acronyms;
        Iterator<String> it;


        ExtractAbbreviations extractor = new ExtractAbbreviations();
        for (int i = 0; i < c.size(); i++) {
            s = c.getSentence(i);
            text = s.toString();

            pairs = extractor.extractAbbrPairs(text);

            acronyms = pairs.keySet();
            it = acronyms.iterator();
            Annotation shortAnnotation, longAnnotation, tmp;
            String shortText, longText;
            while (it.hasNext()) {

                shortText = it.next();
                longText = pairs.get(shortText);

                shortAnnotation = getAnnotationFromText(s, shortText);
                longAnnotation = getAnnotationFromText(s, longText);

                if (s.containsExactAnnotation(shortAnnotation) != null
                        || s.containsApproximateAnnotation(shortAnnotation) != null
                        || s.containsExactAnnotation(longAnnotation) != null
                        || s.containsApproximateAnnotation(longAnnotation) != null) {

                    // Add short if it does not exist
                    if (s.containsExactAnnotation(shortAnnotation) == null
                            && s.containsApproximateAnnotation(shortAnnotation) == null) {
                        s.addAnnotation(shortAnnotation);
                    }

                    // Long form does not exist, add
                    if (s.containsExactAnnotation(longAnnotation) == null
                            && s.containsApproximateAnnotation(longAnnotation) == null) {
                        s.addAnnotation(longAnnotation);
                    } // Partial match of long form annotations
                    else if (s.containsExactAnnotation(longAnnotation) == null
                            && ( tmp = s.containsApproximateAnnotation(longAnnotation) ) != null) {
                        s.removeAnnotation(tmp);
                        s.addAnnotation(longAnnotation);
                    }
                }
            }
        }
    }

    /**
     * Get the annotation from text.
     * @param s The sentence that contains the annotation.
     * @param text The text of the annotation.
     * @return The annotation that reflect the input text.
     */
    private static Annotation getAnnotationFromText(final Sentence s, final String text) {
        String[] tokens = text.split(" ");

        int start;
        int end;
        int count;
        for (int i = 0; i < s.size(); i++) {
            if (s.getToken(i).getText().equals(tokens[0])) {
                end = start = i;
                count = 1;
                for (int j = start + 1; j < s.size() && count < tokens.length && s.getToken(j).getText().equals(tokens[count]); j++) {
                    end++;
                    count++;
                }
                return new Annotation(s, start, end, 0.0);
            }
        }
        return null;
    }
}
