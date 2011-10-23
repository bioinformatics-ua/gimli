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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate orthographic variants of an entity name.
 * <p>
 * THIS WILL BE ALSO CHANGED WHEN I MAKE THE NEW IMPLEMENTATION OF THE
 * DICTIONARY MATCHER.
 *
 * @author David Campos
 * (<a href="mailto:david.campos@ua.pt">david.campos@ua.pt</a>)
 * @version 1.0
 * @since 1.0
 */
public class DictionaryVariations {
    
    private static final Pattern pat1 = Pattern.compile("([a-z]|[A-Z])([0-9])");
    private static final Pattern pat2 = Pattern.compile("([0-9])([a-z]|[A-Z])");
    //private static final Pattern pat3 = Pattern.compile("(alpha|beta|gamma|Alpha|Beta|Gamma|ALPHA|BETA|GAMMA)");
    //private static final Pattern pat4 = Pattern.compile("\\s\\((.*?)\\)\\s");
    
    /**
     * Generate all the variants of one name.
     * @param name The base name.
     * @return The {@link ArrayList} of variants of the name.
     */
    public static ArrayList<String> getVariants(final String name) {
        String var;
        
        ArrayList<String> ret = new ArrayList<String>();

        //Replace spaces by hyphens
        var = name.replaceAll(" ", "-");
        if (!var.equals(name)) {
            ret.add(var);
        }

        //Replace hyphens by spaces
        var = name.replaceAll("-", " ");
        if (!var.equals(name)) {
            ret.add(var);
        }

        //Remove spaces
        var = name.replaceAll(" ", "");
        if (!var.equals(name)) {
            ret.add(var);
        }

        //Remove Hyphens
        var = name.replaceAll("-", "");
        if (!var.equals(name)) {
            ret.add(var);
        }

        Matcher m;
        // Insert hyphen on letter-digit sequence
        m = pat1.matcher(name);
        var = m.replaceAll("$1-$2");
        m = pat2.matcher(var);
        var = m.replaceAll("$1-$2");
        if (!var.equals(name)) {
            ret.add(var);
        }

        // Replace roman by arabic numbers
        var = name.replaceAll("(?i)X", "10");
        var = name.replaceAll("(?i)IX", "9");
        var = var.replaceAll("(?i)VIII", "8");
        var = var.replaceAll("(?i)VII", "7");
        var = var.replaceAll("(?i)VI", "6");
        var = var.replaceAll("(?i)V", "5");
        var = var.replaceAll("(?i)IV", "4");
        var = var.replaceAll("(?i)III", "3");
        var = var.replaceAll("(?i)II", "2");
        var = var.replaceAll("(?i)I", "1");
        if (!var.equals(name)) {
            ret.add(var);
        }
        
        var = name.replaceAll("(?i)X", "10");
        var = name.replaceAll("(?i)1X", "9");
        var = var.replaceAll("(?i)V111", "8");
        var = var.replaceAll("(?i)V11", "7");
        var = var.replaceAll("(?i)V1", "6");
        var = var.replaceAll("(?i)V", "5");
        var = var.replaceAll("(?i)1V", "4");
        var = var.replaceAll("(?i)111", "3");
        var = var.replaceAll("(?i)11", "2");
        var = var.replaceAll("(?i)1", "1");
        if (!var.equals(name)) {
            ret.add(var);
        }

        /*// Replace arabic by roman numbers
        var = name.replaceAll("9", "ix");
        var = var.replaceAll("8", "viii");
        var = var.replaceAll("7", "vii");
        var = var.replaceAll("6", "vi");
        var = var.replaceAll("5", "v");
        var = var.replaceAll("4", "iv");
        var = var.replaceAll("3", "iii");
        var = var.replaceAll("2", "ii");
        var = var.replaceAll("1", "i");
        if (!var.equals(name)) {
            ret.add(var);
        }
        
        var = name.replaceAll("9", "1x");
        var = var.replaceAll("8", "v111");
        var = var.replaceAll("7", "v11");
        var = var.replaceAll("6", "v1");
        var = var.replaceAll("5", "v");
        var = var.replaceAll("4", "1v");
        var = var.replaceAll("3", "111");
        var = var.replaceAll("2", "11");
        var = var.replaceAll("1", "1");
        if (!var.equals(name)) {
            ret.add(var);
        }
        
        
        // Insert hyphen before greek letter
        m = pat3.matcher(name);
        var = m.replaceAll("-$1");
        if (!var.equals(name)) {
            ret.add(var);
        }

        // Remove Parenthesis
        var = name.replaceAll("\\(.*?\\)", "");
        var = var.replaceAll("[\\s]+", " ");
        var = var.trim();
        if (!var.equals(name)) {
            ret.add(var);
        }

        // Add content of parenthesis as entity
        m = pat4.matcher(name);
        
        while (m.find()) {
            var = m.group();
            Matcher m2 = pat4.matcher(var);
            var = m2.replaceAll("$1");
            
            if (!var.equals(name) && var.contains(" ")) {
                ret.add(var);
            }            
        }*/
        
        // Replace greek letter by numbers
        /*var = name.replaceAll("(?i)(alpha)", "1");
        var = var.replaceAll("(?i)(beta)", "2");
        var = var.replaceAll("(?i)(gamma)", "3");
        if (!var.equals(name)) {
            ret.add(var);
        }*/        
        
        // Replace numbers by greek letters
        var = name.replaceAll("1","alpha");
        var = var.replaceAll("2","beta");
        var = var.replaceAll("3","gamma");
        var = var.replaceAll("4","delta");
        var = var.replaceAll("5","epsilon");
        var = var.replaceAll("6","zeta");
        var = var.replaceAll("7","eta");
        var = var.replaceAll("8","theta");
        if (!var.equals(name)) {
            ret.add(var);
        }
        
        // Add prefix h to symbols
        // Add suffix p to symbols
        if (name.equals(name.toUpperCase()))
        {
            var = "h" + name;
            ret.add(var);
            var = var + "p";
            ret.add(var);
        }
        
        
        //Invert order
        /*String[] parts = name.split(" ");
        var = "";
        for(int i=parts.length-1; i >=0; i--){
            var += parts[i];
            var += " "; 
        }
        var = var.trim();
        if (!var.equals(name)) {
            ret.add(var);
        }*/
        
        //System.out.println(name);         
        //System.out.println(ret);
        //System.out.println("");
        
        return ret;
    }
}
