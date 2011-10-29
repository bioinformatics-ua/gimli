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
