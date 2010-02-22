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
 * Extends task 3 by allowing the user to click and drag on existing sprites to move them around
 * the display.
 */
public class Task4 extends Task
{
    protected Sprite _pressed;

    protected void setup (final MediaPanel panel)
    {
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed (MouseEvent ev) {
                _pressed = panel.getSpriteManager().getHighestHitSprite(ev.getX(), ev.getY());
                // if they didn't click on an existing sprite, create a new one
                if (_pressed == null) {
                    _pressed = new CircleSprite();
                    _pressed.setLocation(ev.getX(), ev.getY());
                    panel.addSprite(_pressed);
                }
            }

            public void mouseReleased (MouseEvent e) {
                _pressed = null;
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged (MouseEvent ev) {
                if (_pressed != null) {
                    _pressed.setLocation(ev.getX(), ev.getY());
                }
            }
        });
    }

    protected static class CircleSprite extends Sprite
    {
        public CircleSprite () {
            super(2*RADIUS, 2*RADIUS);
            setOriginOffset(RADIUS, RADIUS);
        }

        public void paint (Graphics2D gfx) {
            gfx.setColor(Color.red);
            gfx.fillOval(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
        }

        public boolean hitTest (int x, int y) {
            int cx = _bounds.x + _oxoff;
            int cy = _bounds.y + _oyoff;
            float dx = x - cx, dy = y - cy;
            return dx*dx + dy*dy <= RADIUS*RADIUS;
        }

        protected static final int RADIUS = 25;
    }
}
