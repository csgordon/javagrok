//
// $Id$

package experiment;

import com.threerings.media.MediaPanel;
import com.threerings.media.image.ImageManager;
import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.ImageSprite;
import com.threerings.media.sprite.PathAdapter;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;

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

        final int y = (panel.getHeight()-sprite.getHeight())/2;
        final int lx = 0, rx = panel.getWidth()-sprite.getWidth();
        sprite.addSpriteObserver(new PathAdapter() {
            public void pathCompleted (Sprite sprite, Path path, long when) {
                if (sprite.getX() == lx) {
                    sprite.move(new LinePath(lx, y, rx, y, 1000));
                } else {
                    sprite.move(new LinePath(rx, y, lx, y, 1000));
                }
            }
        });
        sprite.move(new LinePath(lx, y, rx, y, 1000));
    }
}
