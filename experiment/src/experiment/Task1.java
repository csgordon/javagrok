//
// $Id$

package experiment;

import com.threerings.media.MediaPanel;
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
        Mirage cartman = imgr.getMirage("cartman.jpg");
        ImageSprite sprite = new ImageSprite(cartman);
        sprite.setLocation(50, 50);
        panel.getSpriteManager().addSprite(sprite);
    }
}
