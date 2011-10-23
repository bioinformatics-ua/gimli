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
package pt.ua.tm.gimli.dictionary;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import pt.ua.tm.gimli.config.Constants.DictionaryType;
import pt.ua.tm.gimli.corpus.Corpus;
import pt.ua.tm.gimli.config.Constants.LabelTag;
import pt.ua.tm.gimli.corpus.Sentence;
import pt.ua.tm.gimli.exception.GimliException;

/**
 * Perform dictionary matching and provide the result as features of the tokens.
 *<p>
 * FOR NOW IT IS WORKING OK, BUT I NEED TO CHANGE THE IMPLEMENTATION, IN ORDER
 * TO REMOVE THE DEPENDENCY OF LINGPIPE.
 * NEEDS TO BE MORE EFFICIENT WITH LARGE DICTIONARIES.
 * I WILL CHANGE THAT AFTER WORKING WITH NORMALISATION.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class DictionaryMatcher {
    
    /**
     * Chunker used to remove stopwords.
     */
    private ExactDictionaryChunker stopwordChunker;
    /**
     * Chunker used to identify entity names of the dictionary.
     */
    private ExactDictionaryChunker dictionaryChunker;
    /**
     * The type of the dictionary.
     */
    private DictionaryType type;

    /**
     * Load the dictionary chunker.
     * @return The exact dictionary chunker with the dictionary loaded
     * @throws GimliException Problem reading the dictionary list
     */
    private ExactDictionaryChunker loadStopwordChunker(InputStream list) throws GimliException {
        // Load Dictionary
        MapDictionary<String> dictionary = new MapDictionary<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(list));

            String line;

            while (( line = br.readLine() ) != null) {
                dictionary.addEntry(new DictionaryEntry<String>(line, "STOPWORD", 1.0));
            }
            br.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was an error reading the dictionary file.", ex);
        }

        // Set chunker
        ExactDictionaryChunker chunker = new ExactDictionaryChunker(dictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                false, false);

        return chunker;
    }

    /**
     * Load the dictionary chunker
     * @return The exact dictionary chunker with the dictionary loaded
     * @throws GimliException Problem reading the dictionary file
     */
    private ExactDictionaryChunker loadDictionaryChunker(InputStream list, boolean withVariations) throws GimliException {
        MapDictionary<String> dictionary = new MapDictionary<String>();
        ArrayList<String> vars;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(list));
            String line;

            while (( line = br.readLine() ) != null) {
                if (withVariations) {
                    vars = DictionaryVariations.getVariants(line);
                } else {
                    vars = new ArrayList<String>();
                }

                vars.add(line);
                for (String v : vars) {
                    if (v.length() < 3) {
                        continue;
                    }
                    if (!isStopword(v)) {
                        dictionary.addEntry(new DictionaryEntry<String>(v, "NAME", 1.0));
                    }
                }

            }
            br.close();
        }
        catch (IOException ex) {
            throw new GimliException("There was an error reading the dictionary file.", ex);
        }

        ExactDictionaryChunker chunker = new ExactDictionaryChunker(dictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                false, false);
        return chunker;
    }

    /**
     * Checks if a text is a stopword or not.
     * @param text Text to be analyzed.
     * @return true if the input text is a stopword, false otherwise
     */
    private boolean isStopword(String text) {
        Chunking chunking = stopwordChunker.chunk(text);
        if (chunking.chunkSet().size() == 1) {

            Chunk c = (Chunk) chunking.chunkSet().toArray()[0];
            String phrase = text.substring(c.start(), c.end());

            if (phrase.equals(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructor.
     * @param stopwords {@link InputStream} to the stopwords dictionary.
     * @param dictionary {@link InputStream} to the dictionary.
     * @param withVariations <code>True</code> if is to generate orthographic
     * variations, and <code>False</code> otherwise.
     */
    public DictionaryMatcher(final InputStream stopwords, final InputStream dictionary, final DictionaryType type, final boolean withVariations) {
        try {
            stopwordChunker = loadStopwordChunker(stopwords);
            dictionaryChunker = loadDictionaryChunker(dictionary, withVariations);
            this.type = type;
        }
        catch (GimliException ex) {
            throw new RuntimeException("There was an error loading a dictionary.", ex);
        }
    }

    /**
     * Match the corpus with the dictionary.
     * @param corpus The corpus to be matched.
     */
    public void match(Corpus corpus) {
        Sentence s;
        LabelTag[] labels;
        for (int i = 0; i < corpus.size(); i++) {
            s = corpus.getSentence(i);
            labels = matchSentence(s);

            for (int j = 0; j < labels.length; j++) {
                if (labels[j].equals(LabelTag.I)) {
                    s.getToken(j).addFeature("LEXICON=" + type);
                }
            }
            corpus.setSentence(i, s);
        }
    }

    /**
     * Match sentence using the dictionary chunker.
     * @param sentence The sentence to be matched.
     * @return The resulting labels of the tokens.
     */
    private LabelTag[] matchSentence(Sentence sentence) {
        // Initialize labels
        LabelTag[] labels = new LabelTag[sentence.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = LabelTag.O;
        }

        // Get Sentence
        String phrase = sentence.toString();

        // Chunking
        Chunking chunking = dictionaryChunker.chunk(phrase);
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();

            // Get whitespaces before
            String sub = phrase.substring(0, start);
            char[] chars = sub.toCharArray();
            int whiteSpacesBefore = 0;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == ' ') {
                    whiteSpacesBefore++;
                }
            }

            // Get whitespace entity
            sub = phrase.substring(start, end);
            chars = sub.toCharArray();
            int whiteSpacesEntity = 0;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == ' ') {
                    whiteSpacesEntity++;
                }
            }

            // Final start and end without counting whitespaces
            int s = start - whiteSpacesBefore;
            int e = end - whiteSpacesBefore - whiteSpacesEntity - 1;

            // Set labels
            int c = 0;
            for (int i = 0; i < sentence.size(); i++) {
                if (( c >= s ) && ( c <= e )) {
                    labels[i] = LabelTag.I;
                }

                c += sentence.getToken(i).getText().length();
            }
        }

        return labels;
    }
}
