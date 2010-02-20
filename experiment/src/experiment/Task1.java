//
// $Id$

package experiment;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.samskivert.swing.Label;

import com.threerings.media.MediaPanel;
import com.threerings.media.animation.FloatingTextAnimation;
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
    protected void setup (final MediaPanel panel)
    {
        final ImageSprite sprite = new ImageSprite(getMirage("cartman.png"));
        panel.addSprite(sprite);

        // moves the sprite back and forth across the display
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

        // listens for mouse clicks and checks for intersection with the sprite
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked (MouseEvent ev) {
                String text = sprite.hitTest(ev.getX(), ev.getY()) ? "Ouch!" : "Ha ha!";
                panel.addAnimation(
                    new FloatingTextAnimation(createLabel(text), ev.getX(), ev.getY()));
            }
        });
    }
}
