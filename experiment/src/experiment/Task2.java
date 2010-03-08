//
// $Id$

package experiment;

import com.threerings.media.MediaPanel;

/**
 * Displays a bug on the screen that moves where the user clicks the mouse.
 */
public class Task2 extends Task
{
    protected void setup (final MediaPanel panel)
    {
        // Step 1: Create an OrientableImageSprite using the getMirage("ladybug.png") image and add
        // it to the display. Center the ladybug image in the middle of the window using the
        // setLocation() method. Note that the "hot spot" of an OrientableImageSprite is in the
        // center of the sprite rather than the upper-left like it is for a plain-old ImageSprite.
        // The hot spot is what is moved to the location you specify with setLocation().

        // Step 2: Add a listener to MediaPanel that listens for mouse clicks. Move the ladybug
        // sprite to the point at which the user clicks, using a LinePath. Use
        // DirectionUtil.getDirection() to compute an orientation constant and pass that to
        // setOrientation() on the ladybug sprite to cause the ladybug to orient in the direction
        // of motion.
    }
}
