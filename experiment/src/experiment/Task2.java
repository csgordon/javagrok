//
// $Id$

package experiment;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.threerings.media.MediaPanel;
import com.threerings.media.util.LinePath;
import com.threerings.util.DirectionUtil;
import com.threerings.media.sprite.OrientableImageSprite;

/**
 * Displays a bug on the screen that crawls where the user clicks the mouse.
 */
public class Task2 extends Task
{
    protected void setup (final MediaPanel panel)
    {
        final OrientableImageSprite sprite = new OrientableImageSprite(getMirage("ladybug.png"));
        panel.addSprite(sprite);

        // start at the center of the display
        sprite.setLocation(panel.getWidth()/2, panel.getHeight()/2);

        // listens for mouse clicks and moves the sprite to the clicked location
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked (MouseEvent ev) {
                sprite.setOrientation(DirectionUtil.getDirection(
                                          sprite.getX(), sprite.getY(), ev.getX(), ev.getY()));
                sprite.move(new LinePath(new Point(ev.getX(), ev.getY()), 1000));
            }
        });
    }
}
