//
// $Id$

package experiment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.threerings.media.MediaPanel;
import com.threerings.media.sprite.Sprite;

/**
 * Adds a custom rendered sprite to the display every time the user clicks the mouse. The sprite
 * should render itself as a 50x50 filled circle.
 */
public class Task3 extends Task
{
    protected void setup (final MediaPanel panel)
    {
        // listens for mouse clicks and moves the sprite to the clicked location
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked (MouseEvent ev) {
                CircleSprite sprite = new CircleSprite();
                sprite.setLocation(ev.getX(), ev.getY());
                panel.addSprite(sprite);
            }
        });
    }

    protected static class CircleSprite extends Sprite
    {
        public CircleSprite () {
            super(50, 50);
            setOriginOffset(25, 25);
        }

        public void paint (Graphics2D gfx) {
            gfx.setColor(Color.red);
            gfx.fillOval(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
        }
    }
}
