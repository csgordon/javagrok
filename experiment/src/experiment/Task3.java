//
// $Id$

package experiment;

import com.threerings.media.MediaPanel;

/**
 * Adds a custom rendered sprite to the display every time the user clicks the mouse.
 */
public class Task3 extends Task
{
    // Step 1: create an inner class that extends Sprite and overrides its paint() method to render
    // a filled circle to represent the sprite (use Graphics2D.setColor() and
    // Graphics2D.fillOval()).

    // When you create your sprite, you will need to specify its width and height. You can either
    // hardcode those values, or accept a width and height in your sprite's constructor.

    // In your paint method, you can read the _bounds field of the Sprite to determine where and at
    // what size to render your circle.

    // Step 2: Use the setOriginOffset() method in your sprite's constructor to offset the origin
    // of your sprite to the center of the circle. This means that when you use setLocation() to
    // position your sprite, the value you supply will be interpreted as the center of the rendered
    // sprite rather than as its upper left coordinate (the default).

    protected void setup (final MediaPanel panel)
    {
        // Step 3: add a listener to the MediaPanel to listen for mouse clicks. Every time the
        // mouse is clicked, create a new instance of your circle sprite, add it to the panel and
        // position it where the user clicked the mouse.
    }
}
