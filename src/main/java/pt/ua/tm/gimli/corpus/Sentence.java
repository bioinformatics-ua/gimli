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
package pt.ua.tm.gimli.corpus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.config.Constants.Parsing;
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.config.Constants.LabelFormat;
import pt.ua.tm.gimli.config.Resources;
import pt.ua.tm.gimli.dictionary.DictionaryMatcher;
import pt.ua.tm.gimli.dictionary.DictionaryMatchers;
import pt.ua.tm.gimli.exception.GimliException;
import pt.ua.tm.gimli.external.gdep.GDepCorpus;
import pt.ua.tm.gimli.external.gdep.GDepSentence;
import pt.ua.tm.gimli.external.gdep.GDepToken;
import pt.ua.tm.gimli.external.wrapper.Parser;

/**
 * Used to represent sentence.
 *
 * @author David Campos (<a
 * href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Sentence {

    /**
     * The unique identifier of the sentence.
     */
    private String id;
    /**
     * The corpus of the sentence.
     */
    private Corpus corpus;
    /**
     * The tokens of the sentence.
     */
    private ArrayList<Token> tokens;
    /**
     * The annotations associated with this sentence.
     */
    private ArrayList<Annotation> annotations;

    /**
     * Constructor.
     *
     * @param c The corpus of the sentence.
     */
    public Sentence(Corpus c) {
        this.corpus = c;
        this.tokens = new ArrayList<Token>();
        this.annotations = new ArrayList<Annotation>();
    }

    public void parse(Parser parser, String sentenceText) throws GimliException {
        // Pre-process
        sentenceText = sentenceText.replaceAll("/", " / ");
        sentenceText = sentenceText.replaceAll("-", " - ");
        sentenceText = sentenceText.replaceAll("[.]", " . ");
        sentenceText = sentenceText.replaceAll("//s+", " ");
        List<Object> output = parser.parse(sentenceText);

        // Get GDep Output
        GDepSentence gs = new GDepSentence(new GDepCorpus());
        for (Object obj : output) {
            String[] parts = obj.toString().split("\t");

            String text = parts[1];
            text = text.replaceAll("''", "\"");
            text = text.replaceAll("``", "\"");

            String lemma = parts[2];
            String chunk = parts[3];
            String pos = parts[4];
            int depToken = Integer.valueOf(parts[6]) - 1;
            String depTag = parts[7];


            GDepToken gt = new GDepToken(text, lemma, pos, chunk, depToken, depTag);
            gs.addToken(gt);
        }

        //Add token and respective features from GDep output
        int start = 0;
        for (int k = 0; k < gs.size(); k++) {
            GDepToken gt = gs.getToken(k);
            Token t = new Token(this, start, k, gs);
            start = t.getEnd() + 1;
            addToken(t);
        }

        // Dictionary matching as features
        DictionaryMatchers.getInstance().get("prge").match(this);        
        DictionaryMatchers.getInstance().get("verbs").match(this);
        DictionaryMatchers.getInstance().get("aminoacid").match(this);
        DictionaryMatchers.getInstance().get("nucleicacid").match(this);
        DictionaryMatchers.getInstance().get("nucleobase").match(this);
        DictionaryMatchers.getInstance().get("nucleoside").match(this);
        DictionaryMatchers.getInstance().get("nucleotide").match(this);
    }

    /**
     * Get specific annotation of the sentence.
     *
     * @param i Index of the annotation.
     * @return The annotation.
     */
    public Annotation getAnnotation(int i) {
        if (i > annotations.size() - 1) {
            return null;
        }
        return annotations.get(i);
    }

    /**
     * Get the corpus of the sentence.
     *
     * @return The corpus.
     */
    public Corpus getCorpus() {
        return corpus;
    }

    /**
     * Get the identifier of the sentence.
     *
     * @return The ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set sentence identifier.
     *
     * @param id The new identifier of the sentence.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Add a token to the sentence.ß
     *
     * @param t The token.
     */
    public void addToken(Token t) {
        tokens.add(t);
    }

    /**
     * Get specific token of the sentence.
     *
     * @param i The index of the token.
     * @return The token.
     */
    public Token getToken(final int i) {
        return tokens.get(i);
    }

    /**
     * Set specific token of the sentence.
     *
     * @param i The index of the token.
     * @param t The token.
     */
    public void setToken(final int i, final Token t) {
        tokens.set(i, t);
    }

    /**
     * Get the size of the sentence, a.k.a the number of tokens.
     *
     * @return The number of tokens.
     */
    public int size() {
        return tokens.size();
    }

    /**
     * Remove all annotations of the sentence. Remove on the list of
     * annotations, and change all the labels of tokens to
     * <code>O</code>.
     */
    public void cleanAnnotations() {
        annotations = new ArrayList<Annotation>();
        for (int i = 0; i < size(); i++) {
            getToken(i).setLabel(LabelTag.O);
        }
    }

    /**
     * Change the parsing order of the sentence.
     */
    public void reverse() {
        Annotation newAnnotation, a;
        Collections.reverse(tokens);

        int sentenceSize = size() - 1;
        int annotationSize;
        int newStart;
        int newEnd;

        for (int i = 0; i < annotations.size(); i++) {
            a = annotations.get(i);
            annotationSize = a.getEndIndex() - a.getStartIndex();
            newStart = sentenceSize - a.getEndIndex();
            newEnd = sentenceSize - a.getEndIndex() + annotationSize;

            newAnnotation = new Annotation(this, newStart, newEnd, a.getScore());
            annotations.set(i, newAnnotation);
        }
    }

    /**
     * Add annotations to the sentence considering that the tokens are already
     * tagged.
     *
     * @param score The confidence value to generate the annotations.
     */
    public void addAnnotationsFromTags(double score) {
        if (corpus.getParsing().equals(Parsing.BW)) {
            addAnnotationsFromTagsBackward(score);
        } else {
            addAnnotationsFromTagsForward(score);
        }
    }

    /**
     * Add annotations to the sentence considering that the tokens are already
     * tagged, and the corpus is in Forward direction.
     *
     * @param score The confidence value to generate the annotations.
     */
    private void addAnnotationsFromTagsForward(final double score) {
        LabelTag label;
        boolean isAnnotation;
        int start = 0, end = 0;
        for (int i = 0; i < size(); i++) {
            label = getToken(i).getLabel();
            isAnnotation = false;

            if (corpus.getFormat().equals(LabelFormat.IO)) {
                if (label.equals(LabelTag.I)) {
                    isAnnotation = true;
                }
            } else {
                if (label.equals(LabelTag.B)) {
                    isAnnotation = true;
                }
            }

            if (isAnnotation) {
                start = end = i;

                for (int k = start + 1; k < size(); k++) {
                    label = getToken(k).getLabel();
                    if (label.equals(LabelTag.B) || label.equals(LabelTag.O)) {
                        break;
                    }
                    end++;
                }

                annotations.add(new Annotation(this, start, end, score));
                i = end;
            }
        }
    }

    /**
     * Add annotations to the sentence considering that the tokens are already
     * tagged, and the corpus is in Backward direction.
     *
     * @param score The confidence value to generate the annotations.
     */
    private void addAnnotationsFromTagsBackward(final double score) {
        LabelTag label;
        boolean isAnnotation;
        int start = 0, end = 0;
        for (int i = size() - 1; i >= 0; i--) {
            label = getToken(i).getLabel();
            isAnnotation = false;

            if (corpus.getFormat().equals(LabelFormat.IO)) {
                if (label.equals(LabelTag.I)) {
                    isAnnotation = true;
                }
            } else {
                if (label.equals(LabelTag.B)) {
                    isAnnotation = true;
                }
            }

            if (isAnnotation) {
                start = end = i;

                for (int k = end - 1; k >= 0; k--) {
                    label = getToken(k).getLabel();
                    if (label.equals(LabelTag.B) || label.equals(LabelTag.O)) {
                        break;
                    }
                    start--;
                }

                annotations.add(new Annotation(this, start, end, score));
                i = start;
            }
        }
    }

    /**
     * From the whole set of annotations, get the first annotation that is equal
     * to the one provided.
     *
     * @param a The annotation.
     * @return The annotation that is equal to the one provided by argument, *
     * or <code>null</code> otherwise.
     */
    public Annotation containsExactAnnotation(final Annotation a) {
        for (Annotation an : annotations) {
            if (an.equals(a)) {
                return an;
            }
        }
        return null;
    }

    /**
     * From the whole set of annotations, get the first annotation that contains
     * the one provided.
     *
     * @param a The annotation.
     * @return The annotation that contains to the one provided by argument, *
     * or <code>null</code> otherwise.
     */
    public Annotation containsApproximateAnnotation(final Annotation a) {
        for (Annotation an : annotations) {
            if (an.contains(a)) {
                return an;
            }
        }
        return null;
    }

    /**
     * Add annotation to sentence and update the labels of the tokens following
     * the desired format.
     *
     * @param a Annotation to add to the sentence.
     */
    public void addAnnotation(final Annotation a) {
        int start = a.getStartIndex();
        int end = a.getEndIndex();
        int length = end - start + 1;
        LabelFormat format = corpus.getFormat();

        for (int i = start; i <= end; i++) {
            if (i == start) { // First Token
                if (format.equals(LabelFormat.IO)) {
                    tokens.get(i).setLabel(LabelTag.I);
                } else if (format.equals(LabelFormat.BIO)) {
                    tokens.get(i).setLabel(LabelTag.B);
                } else if (format.equals(LabelFormat.BMEWO)) {
                    if (length > 1) {
                        tokens.get(i).setLabel(LabelTag.B);
                    } else {
                        tokens.get(i).setLabel(LabelTag.W);
                    }
                }
            } else if (i == end) { // Last Token
                if (format.equals(LabelFormat.BMEWO)) {
                    tokens.get(i).setLabel(LabelTag.E);
                } else {
                    tokens.get(i).setLabel(LabelTag.I);
                }
            } else { // Tokens in the Middle
                if (format.equals(LabelFormat.BMEWO)) {
                    tokens.get(i).setLabel(LabelTag.M);
                } else {
                    tokens.get(i).setLabel(LabelTag.I);
                }
            }
        }

        annotations.add(a);
    }

    /**
     * Remove specific annotation.
     *
     * @param i Index of the annotation to be removed.
     */
    public void removeAnnotation(final int i) {
        removeAnnotation(getAnnotation(i));
    }

    /**
     * Remove annotation from sentence.
     *
     * @param a Annotation to be removed.
     */
    public void removeAnnotation(final Annotation a) {
        int start = a.getStartIndex();
        int end = a.getEndIndex();

        for (int i = start; i <= end; i++) {
            tokens.get(i).setLabel(LabelTag.O);
        }
        annotations.remove(a);
    }

    /**
     * Get Sentence in a format to save the corpus in a file.
     *
     * @return String presenting the sentence.
     */
    public final String toExportFormat() {
        StringBuilder sb = new StringBuilder();

        Token t;
        for (int i = 0; i < tokens.size(); i++) {
            t = getToken(i);
            sb.append(t.getText());
            sb.append("\t");
            sb.append(t.featuresToString());
            sb.append("\t");
            sb.append(t.getLabel().toString());
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * ****************************************************************
     */
    /**
     * ****************************************************************
     */
    /*REFAZER CONVERSÃO PARA XML*/
    /**
     * ****************************************************************
     */
    /**
     * ****************************************************************
     */
    /**
     * Convert a sentence to the IeXML format.
     *
     * @return {@link StringBuilder} that partialMatch the sentence in the
     * specified format
     */
    /*public final StringBuilder convertToIeXMLFormat() {
     StringBuilder sb = new StringBuilder();
    
     String labelB = "";
     int counter = 0;
     for (Token t : tokens) {
     String label = t.getLabel().toString();
     String text = t.getText();
    
     //text = StringEscapeUtils.escapeHtml(text);
     //text = text.replaceAll("'", "&apos;");
     //text = text.replaceAll("&#39;", "&apos;");
    
     if (label.equals(Constants.getB())) {
     if (labelB.equals(Constants.getB()) || labelB.equals(Constants.getI())) {
     sb.append("</e> ");
     }
     sb.append(" <e id=\":::PRGE\">");
     sb.append(text);
     } else if (label.equals(Constants.getI())) {
     sb.append(" ");
     sb.append(text);
     } else if (label.equals(Constants.getO())) {
     if (labelB.equals(Constants.getO())) {
     sb.append(" ");
     sb.append(text);
     } else if (labelB.equals(Constants.getB()) || labelB.equals(Constants.getI())) {
     sb.append("</e> ");
     sb.append(text);
     } else {
     sb.append(text);
     }
     }
    
     // Add close tag on the end of the sentence
     if (counter == ( tokens.size() - 1 )) {
     if (label.equals(Constants.getB()) || label.equals(Constants.getI())) {
     sb.append("</e> ");
     }
     }
    
     labelB = label;
     counter++;
     }
    
     if (sb.charAt(0) == ' ') {
     sb.deleteCharAt(0);
     }
    
     return sb;
     }*/
    /**
     * Get the number of annotations associated with this sentence.
     *
     * @return The number of annotations.
     */
    public int getNumberAnnotations() {
        return annotations.size();
    }

    /**
     * Provide text representation of the sentence.
     *
     * @return Text of the sentence.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            sb.append(t.getText());
            sb.append(" ");
        }
        String ret = sb.toString();
        ret = ret.trim();

        return ret;
    }

    /**
     * Compare two sentences.
     *
     * @param obj The sentence to be compared with.
     * @return <code>True</code> if the two sentences are equal, and
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
        final Sentence other = (Sentence) obj;
        if (this.corpus != other.corpus && ( this.corpus == null || !this.corpus.equals(other.corpus) )) {
            return false;
        }
        if (this.tokens != other.tokens && ( this.tokens == null || !this.tokens.equals(other.tokens) )) {
            return false;
        }
        if (this.annotations != other.annotations && ( this.annotations == null || !this.annotations.equals(other.annotations) )) {
            return false;
        }
        return true;
    }

    /**
     * Override the hashCode method to consider all the internal variables.
     *
     * @return Unique number for each sentence.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + ( this.corpus != null ? this.corpus.hashCode() : 0 );
        hash = 79 * hash + ( this.tokens != null ? this.tokens.hashCode() : 0 );
        hash = 79 * hash + ( this.annotations != null ? this.annotations.hashCode() : 0 );
        return hash;
    }
}
