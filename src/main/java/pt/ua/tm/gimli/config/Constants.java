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
package pt.ua.tm.gimli.config;

/**
 * Store global constants and type enumerators.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class Constants {

    /**
     * Parsing direction of the text.
     */
    public enum Parsing {

        /**
         * Parse the text from left to right, forward direction.
         */
        FW,
        /**
         * Parse the text from right to left, backward direction.
         */
        BW
    }

    /**
     * Available annotation encoding formats, which use a combination of
     * {@link LabelTag}.
     * <p>
     * This encoding formats are used to tag the tokens as being part or not
     * of an entity name.
     * <p>
     * Note that <code>IO</code> and <code>BIO</code> were carefully tested.
     * However, only minor tests were performed with <code>BMEWO</code>.
     * Consequently, we do not guarantee the functionality of this format.
     */
    public enum LabelFormat {

        /**
         * The most basic encoding format that only uses Inside and Outside
         * tags.
         */
        IO,
        /**
         * The de facto standard solution, which solves the problem of followed
         * entity names.
         */
        BIO,
        /**
         * Encoding format that also marks the tokens in the middle of the
         * entity name.
         */
        BMEWO;
    }

    /**
     * Symbols used to tag the tokens.
     */
    public enum LabelTag {

        /**
         * The token is in the <b>Beginning</b> of the annotation.
         */
        B,
        /**
         * The token is <b>Inside</b> the annotation.
         */
        I,
        /**
         * The token is <b>Outside</b> the annotation.
         */
        O,
        /**
         * The token is in the <b>Middle</b> of the annotation.
         */
        M,
        /**
         * The token is in the <b>End</b> of the annotation.
         */
        E,
        /**
         * The single token is a complete annotation.
         */
        W;
    }

    /**
     * Biomedical entity names supported by Gimli.
     */
    public enum EntityType {

        /**
         * Used for Gene/Protein entity names.
         */
        protein,
        /**
         * User for DNA entity names.
         */
        DNA,
        /**
         * Used for RNA entity names.
         */
        RNA,
        /**
         * Used for Cell Type entity names.
         */
        cell_type,
        /**
         * Used for Cell Line entity names.
         */
        cell_line;
    }
    /**
     * Dictionary types supported by Gimli.
     */
    public enum DictionaryType {
        /**
         * Gene/protein names.
         */
        PRGE,
        /**
         * Biomedical concepts.
         */
        CONCEPT,
        /**
         * Trigger verbs.
         */
        VERB;
    }
    
    /**
     * Prefix folder that contains the required tools.
     */
    private static final String TOOLSPREFIXDIR = "resources/tools/";
    /**
     * Command line to parse texts using GDep performing tokenisation.
     */
    public static final String[] GDEPCOMMAND = {TOOLSPREFIXDIR
        + "gdep/gdep_gimli"};
    /**
     * Command line to parse texts using GDep without performing tokenisation.
     */
    public static final String[] GDEPCOMMANDNT = {TOOLSPREFIXDIR
        + "gdep/gdep_gimli", "-nt"};
    /**
     * Command line to parse texts using Enju, performing tokenisation and using
     * models trained on biomedical documents (GENIA).
     */
    /*public static final String[] ENJUPARSERCOMMAND = {
        TOOLSPREFIXDIR + "enju-2.4.2/enju",
        "-genia"
    };*/
    /**
     * Command line to parse texts using Enju, without performing tokenisation
     * and using models trained on biomedical documents (GENIA).
     */
    /*public static final String[] ENJUPARSERCOMMANDNT = {
        TOOLSPREFIXDIR + "enju-2.4.2/enju",
        "-genia",
        "-nt"
    };*/
    /**
     * Command line to parse texts using GeniaTagger, performing tokenisation
     * and using models trained on biomedical documents (GENIA).
     */
    /*public static final String[] GENIATAGGERCOMMAND = {TOOLSPREFIXDIR
        + "geniatagger/genia"};*/
    /**
     * Command line to parse texts using GeniaTagger, without performing
     * tokenisation and using models trained on biomedical documents (GENIA).
     */
    /*public static final String[] GENIATAGGERCOMMANDNT = {TOOLSPREFIXDIR
        + "geniatagger/genia", "-nt"};*/
}
