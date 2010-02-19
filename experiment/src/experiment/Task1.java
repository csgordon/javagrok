//
// $Id$

package experiment;

import com.threerings.media.MediaPanel;
import com.threerings.media.util.LinePath;
import com.threerings.media.sprite.ImageSprite;
import com.threerings.media.image.ImageManager;
import com.threerings.media.image.Mirage;

/**
 * Displays a simple sprite on the screen, moving back and forth.
 */
public class Task1 extends Task
{
    public void init (ImageManager imgr, MediaPanel panel)
    {
        Mirage cartman = imgr.getMirage("cartman.png");
        ImageSprite sprite = new ImageSprite(cartman);
        panel.getSpriteManager().addSprite(sprite);

        int y = (panel.getHeight()-sprite.getHeight())/2;
        sprite.move(new LinePath(0, y, panel.getWidth()-sprite.getWidth(), y, 1000));
    }
}
