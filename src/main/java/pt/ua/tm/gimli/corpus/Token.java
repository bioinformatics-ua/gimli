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
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.external.gdep.GDepSentence;
import pt.ua.tm.gimli.external.gdep.GDepToken;

/**
 * Used to represent token.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Token {

    /**
     * The sentence to which this token makes part of.
     */
    private Sentence sentence;
    /**
     * The index of the first character of the token.
     * This counting discards white spaces.
     */
    private int start;
    /**
     * The index of the last character of the token.
     * This counting discards white spaces.
     */
    private int end;
    /**
     * The t of the token.
     */
    private String text;
    /**
     * The index of the token in the sentence.
     */
    private int index;
    /**
     * The set of features of the token.
     */
    private ArrayList<String> features;
    /**
     * The annotation label of the token.
     */
    private LabelTag label;

    /**
     * Default token initialization.
     * Used to simplify the constructors.
     * @param s The sentence.
     * @param start The first char index.
     * @param end The last char index.
     * @param text The text of the token.
     * @param index The index in the sentence.
     */
    private void init(final Sentence s, final int start, final int end, final String text, final int index) {
        this.sentence = s;
        this.start = start;
        this.end = end;
        this.text = text;
        this.index = index;
        this.label = LabelTag.O;
        this.features = new ArrayList<String>();
    }
    
    /**
     * Constructor.
     * @param s The sentence.
     * @param start The first char index.
     * @param end The last char index.
     * @param text The text of the token.
     * @param index The index in the sentence.
     */
    public Token(final Sentence s, final int start, final int end, final String text, final int index) {
        init(s, start, end, text, index);
    }

    /**
     * Constructor using the GDep Parsing result to build a new Token.
     * @param s The sentence to which the Token is associated with.
     * @param start The first char index.
     * @param index The index in the sentence.
     * @param gs The GDep parsing result of sentence, since we can add
     * features of the whole sentence to the token.
     */
    public Token(final Sentence s, final int start, final int index, final GDepSentence gs) {
        GDepToken gt = gs.getToken(index);

        // Init token
        int e = start + gt.getText().length() - 1;
        init(s, start, e, gt.getText(), index);

        // Add features
        features.add("LEMMA=" + gt.getLemma());
        features.add("POS=" + gt.getPOS());
        features.add("CHUNK=" + gt.getChunk());

        // Add Dependency features
        if (gt.getDepTag().equals("OBJ")) {
            features.add("OBJ=" + gs.getToken(gt.getDepToken()).getLemma());
        } else if (gt.getDepTag().equals("SUB")) {
            features.add("SUB=" + gs.getToken(gt.getDepToken()).getLemma());
        } else if (gt.getDepTag().equals("NMOD")) {
            features.add("NMOD_OF=" + gs.getToken(gt.getDepToken()).getLemma());
        }

        String f;
        GDepToken gt2;
        for (int i = 0; i < gs.size(); i++) {
            gt2 = gs.getToken(i);
            if (( gt2.getDepToken() == index ) && gt2.getDepTag().equals("NMOD")) {
                f = "NMOD_BY=" + gt2.getLemma();
                if (!features.contains(f)) {
                    features.add(f);
                }
            }
        }
    }

    /**
     * Constructor using the GENIA Parsing result to build a new Token.
     * @param s The sentence to which the Token is associated with.
     * @param start The first char index.
     * @param index The index in the sentence.
     * @param geniaResult The GENIA parsing result.
     */
    /*public Token(final Sentence s, final int start, final int index,
    final String geniaResult) {
    String[] parts = geniaResult.split("\\s+");
    String t = parts[0];
    
    t = t.replaceAll("''", "\"");
    t = t.replaceAll("``", "\"");
    
    String lemma = parts[1];
    String pos = parts[2];
    String chunk = parts[3];
    
    // Create token
    int e = start + t.length() - 1;
    
    this.sentence = s;
    this.start = start;
    this.end = e;
    this.text = t;
    this.index = index;
    this.label = LabelTag.O;
    this.features = new ArrayList<String>();
    
    // Add features
    features.add("LEMMA=" + lemma);
    features.add("POS=" + pos);
    features.add("CHUNK=" + chunk);
    }*/
    
    /**
     * Constructor used to load a token of a corpus stored in a file.
     * @param s The sentence to which the Token is associated with.
     * @param start The first char index.
     * @param index The index in the sentence.
     * @param exportFormat The token in the export format.
     */
    public Token(final Sentence s, final int start, final int index, final String exportFormat) {
        String[] parts = exportFormat.split("\\s+");
        String text = parts[0];
        String lemma = parts[1];
        String pos = parts[2];
        String chunk = parts[3];
        String label = parts[parts.length - 1];
        int e = start + text.length() - 1;
        
        // Init token
        init(s, start, e, text, index);
        this.label = LabelTag.valueOf(label);

        // Add features
        features.add(lemma);
        features.add(pos);
        features.add(chunk);

        for (int i = 4; i < parts.length - 1; i++) {
            features.add(parts[i]);
        }
    }

    /**
     * Get the last char index.
     * @return The char index.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Get the index of the token in the sentence.
     * @return The token index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the annotation tag of the token.
     * @return The tag.
     */
    public LabelTag getLabel() {
        return label;
    }

    /**
     * Get the sentence to which to token makes part of.
     * @return The sentence.
     */
    public Sentence getSentence() {
        return sentence;
    }

    /**
     * Get the first char index.
     * @return The char index.
     */
    public int getStart() {
        return start;
    }

    /**
     * Get the t of the token.
     * @return The t.
     */
    public String getText() {
        return text;
    }

    /**
     * Set the annotation label of the token.
     * @param label The new label of the token.
     */
    public void setLabel(final LabelTag label) {
        this.label = label;
    }

    /**
     * Add a new feature to the token.
     * @param f The new feature to be added.
     */
    public void addFeature(final String f) {
        if (features.contains(f)) {
            return;
        }
        features.add(f);
    }

    /**
     * Get a feature from a specific index.
     * @param i The index in the {@link ArrayList}.
     * @return The feature in the position <code>i</code>.
     */
    public String getFeature(final int i) {
        return features.get(i);
    }

    /**
     * Set a feature in a specific index.
     * @param i The index in the {@link ArrayList}.
     * @param f The new feature to that index.
     */
    public void setFeature(final int i, final String f) {
        features.set(i, f);
    }

    /**
     * Get the number of features of the token.
     * @return The number of features.
     */
    public int sizeFeatures() {
        return features.size();
    }

    /**
     * Get a text representation of the features of the token.
     * @return Text with all the features.
     */
    public String featuresToString() {
        StringBuilder sb = new StringBuilder();
        for (String f : features) {
            sb.append(f);
            sb.append("\t");
        }
        return sb.toString().trim();
    }
}
