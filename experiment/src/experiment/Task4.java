//
// $Id$

package experiment;

import com.threerings.media.MediaPanel;

/**
 * Extends task 3 by allowing the user to click and drag on existing sprites to move them around
 * the display.
 */
public class Task4 extends Task
{
    // Step 1: Copy the code for your circle sprite from Task 3. You will be reusing it in this
    // exercise.

    // Override the hitTest() method in your circle sprite and implement it such that it returns
    // true if the x and y coordinates are inside your circle, false if not.

    protected void setup (final MediaPanel panel)
    {
        // Step 2: Add a MouseListener to the MediaPanel that listens for mouse pressed and mouse
        // released events. Add a MouseMotionListener to the MediaPanel that listens for mouse
        // dragged events.

        // When the mouse is pressed, use panel.getSpriteManager().getHighestHitSprite() to
        // determine whether a sprite was clicked on by the mouse. If no sprite was clicked, create
        // a new circle sprite and place it at the location. If a sprite was clicked, cause
        // subsequent "mouse dragged" events to update the location of that sprite with the
        // location of the mouse. When you received a mouse released event, stop moving the pressed
        // sprite.

        // You can choose whether a newly created sprite starts in the "pressed" state (and is thus
        // immediately draggable if the user clicks on an empty location triggering the creation of
        // a new sprite and then drags the mouse) or not.
    }
}
