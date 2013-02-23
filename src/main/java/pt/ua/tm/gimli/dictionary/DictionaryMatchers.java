/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.tm.gimli.dictionary;

import java.util.HashMap;
import java.util.Map;
import pt.ua.tm.gimli.config.Constants;
import pt.ua.tm.gimli.config.Constants.DictionaryType;
import pt.ua.tm.gimli.config.Resources;

/**
 *
 * @author david
 */
public class DictionaryMatchers {

    private Map<String, DictionaryMatcher> matchers;
    private static DictionaryMatchers instance = null;

    private DictionaryMatchers(){
        this.matchers = new HashMap<String, DictionaryMatcher>();
        init();
    }
    
    public static DictionaryMatchers getInstance() {
        if (instance == null) {
            instance = new DictionaryMatchers();
        }
        return instance;
    }

    public DictionaryMatcher get(String name) {
        if (!matchers.containsKey(name)){
            return null;
        }
        return matchers.get(name);
    }

    private void init() {
        // PRGE
        DictionaryMatcher matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("prge"), Constants.DictionaryType.PRGE, true);
        matchers.put("prge", matcher);

        // Verb
        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("verbs"), Constants.DictionaryType.VERB, false);
        matchers.put("verbs", matcher);

        // Concepts
        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("aminoacid"), Constants.DictionaryType.CONCEPT, false);
        matchers.put("aminoacid", matcher);

        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("nucleicacid"), Constants.DictionaryType.CONCEPT, false);
        matchers.put("nucleicacid", matcher);

        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("nucleobase"), Constants.DictionaryType.CONCEPT, false);
        matchers.put("nucleobase", matcher);

        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("nucleoside"), Constants.DictionaryType.CONCEPT, false);
        matchers.put("nucleoside", matcher);

        matcher = new DictionaryMatcher(Resources.getLexicon("stopwords"), Resources.getLexicon("nucleotide"), Constants.DictionaryType.CONCEPT, false);
        matchers.put("nucleotide", matcher);
    }
}
