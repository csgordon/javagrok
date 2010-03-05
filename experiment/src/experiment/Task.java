//
// $Id$

package experiment;

import java.awt.Color;
import java.awt.Font;

import com.samskivert.swing.Label;

import com.threerings.media.MediaPanel;
import com.threerings.media.image.ImageManager;
import com.threerings.media.image.Mirage;

/**
 * The base class for the experiment tasks.
 */
public abstract class Task
{
    public void init (ImageManager imgr, MediaPanel panel) {
        _imgr = imgr;
        _panel = panel;
        setup(_panel);
    }

    protected abstract void setup (MediaPanel panel);

    protected Mirage getMirage (String imagePath)
    {
        return _imgr.getMirage(imagePath);
    }

    protected Label createLabel (String text)
    {
        Label label = new Label(text, Color.BLACK, LABEL_FONT);
        label.layout(_panel);
        return label;
    }

    protected ImageManager _imgr;
    protected MediaPanel _panel;

    protected static final Font LABEL_FONT = new Font("Dialog", Font.BOLD, 16);
}
