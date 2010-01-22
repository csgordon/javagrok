//
// $Id$

package org.javagrok.doclet;

import java.util.Map;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * Handles formatting of \@grok Javadoc tags. \@grok tags are inserted into the source based on
 * annotation information during a pre-processing phase.
 */
public class GrokTaglet implements Taglet
{
    /**
     * Registers this taglet. Called magically by Javadoc.
     */
    public static void register (Map<String, Taglet> map)
    {
        map.put(NAME, new GrokTaglet());
    }

    // from interface Taglet
    public boolean inField ()
    {
        return true;
    }

    // from interface Taglet
    public boolean inConstructor ()
    {
        return true;
    }

    // from interface Taglet
    public boolean inMethod ()
    {
        return true;
    }

    // from interface Taglet
    public boolean inOverview ()
    {
        return false;
    }

    // from interface Taglet
    public boolean inPackage ()
    {
        return false;
    }

    // from interface Taglet
    public boolean inType ()
    {
        return true;
    }

    // from interface Taglet
    public boolean isInlineTag ()
    {
        return false;
    }

    // from interface Taglet
    public String getName ()
    {
        return NAME;
    }

    // from interface Taglet
    public String toString (Tag tag)
    {
        return "<DT><B>Grok:</B></DT><DD>" + tag.text() + "</DD>\n";
    }

    // from interface Taglet
    public String toString (Tag[] tags)
    {
        if (tags.length == 0) {
            return null;
        }
        StringBuffer buf = new StringBuffer("<DT><B>Grok:</B></DT>");
        for (Tag tag : tags) {
            buf.append("<DD>" + tag.text() + "</DD>");
        }
        return buf.toString();
    }

    protected static final String NAME = "org.javagrok.info";
}
