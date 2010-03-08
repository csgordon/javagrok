//
// $Id$

package experiment;

import com.threerings.media.MediaPanel;

/**
 * Displays a simple sprite on the screen, moving back and forth. Intercepts clicks on that sprite
 */
public class Task1 extends Task
{
    protected void setup (final MediaPanel panel)
    {
        // Hello and welcome to the first task. I hope you are all set and have
        // received cookies and a drink to be ready to complete the tasks. :)
    	// You should also have a questionnaire and experiment overview document.
    	
    	// Remember: This experiment does not assess you, it assess our tool.
    	// Something else: You do not have to complete all the tasks. It is totally okay to 
    	// stop after an hour.
    	
        // Step 1: Create an ImageSprite with the image "cartman.png" and add it to the MediaPanel
        // supplied as an argument.

        // You will want to call getMirage("cartman.png") (which is defined in Task, the parent
        // class of this task skeleton) to obtain the image that you will supply to the
        // ImageSprite.

        // Step 2: Move the ImageSprite on a Path back and forth across the window. You will want
        // to use the LinePath class to move the sprite. The LinePath will only move the Sprite
        // from one point to another, so you will have to add a PathObserver to the Sprite to
        // receive notification when the Sprite reaches the end of its path. At that point, set the
        // sprite on a new path going back in the opposite direction, so that you achieve an
        // oscilating motion back and forth across the window.
    	
    	// Hint: The top left corner of the panel has the coordinates (0,0). The top right
    	// corner has the coordinates (0, panel's width) and the bottom right corner has
    	// the coordinates (panel's height, panel's width).
    	
    	// Hint 2: The actual movement has to be invoked on the sprite. Not the panel.
    	

        // Step 3: Move the ImageSprite on a Path back and forth across the window. You will want
        // to use the LinePath class to move the sprite. The path should be added to the sprite
        // object. The LinePath will only move the Sprite from one point to another, so you will
        // have to add a PathObserver to the Sprite to receive notification when the Sprite reaches
        // the end of its path. At that point, set the sprite on a new path going back in the
        // opposite direction, so that you achieve an oscillating motion back and forth across the
        // window.

        // Step 4: Add a MouseListener to the MediaPanel to be notified of mouse clicks. When the
        // user clicks the mouse, use the hitTest() method on the Sprite to determine whether or
        // not the user clicked on the sprite. In either case, you will create a
        // FloatingTextAnimation to emit feedback to the user. The FloatingTextAnimation takes a
        // Label in its constructor, use the createLabel() method defined in Task (our superclass)
        // to create that label. If the click hits the sprite, create a label that says "Ouch!", if
        // it misses the sprite, create a label that says "Ha ha!"

        // Congratulations. You have just created a "punch Cartman" game. :)
    }
}
