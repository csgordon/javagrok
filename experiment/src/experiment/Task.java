//
// $Id$

package experiment;

import com.threerings.media.image.ImageManager;

import com.threerings.media.MediaPanel;

/**
 * The base class for the experiment tasks.
 */
public abstract class Task
{
    public abstract void init (ImageManager imgr, MediaPanel panel);
}
