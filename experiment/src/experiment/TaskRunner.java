//
// $Id$

package experiment;

import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.samskivert.util.Logger;
import com.samskivert.util.TerseLogFormatter;

import com.threerings.media.FrameManager;
import com.threerings.media.ManagedJFrame;
import com.threerings.media.MediaPanel;
import com.threerings.media.image.ImageManager;
import com.threerings.resource.ResourceManager;

/**
 * Sets up the environment and runs a particular task.
 */
public class TaskRunner
{
    public static void main (String[] args)
    {
        int taskNo = 0;
        try {
            taskNo = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // any failure is ignored and we display usage
        }
        if (taskNo == 0) {
            System.err.println("Usage: TaskRunner taskno");
            System.exit(255);
        }

        String tcname = "experiment.Task" + taskNo;
        final Task task;
        try {
            task = (Task)Class.forName(tcname).newInstance();
        } catch (Exception e) {
            System.err.println("Failed to create '" + tcname + "': " + e);
            System.exit(255);
            return; // unreached, but appeases javac's static analysis
        }

        // set up terse logging
        Logger.getLogger("com.samskivert"); // triggers logger initialization
        TerseLogFormatter.configureDefaultHandler();

        // set up our myriad interlocking bits of scaffolding
        final ManagedJFrame frame = new ManagedJFrame("Task " + taskNo);
        frame.setDefaultCloseOperation(ManagedJFrame.EXIT_ON_CLOSE);
        final FrameManager fmgr = FrameManager.newInstance(frame);
        final ResourceManager rmgr = new ResourceManager("rsrc");
        final ImageManager imgr = new ImageManager(rmgr, frame);

        // now we ship ourselves off to the AWT thread to start everything up
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                // set up our media panel, 
                MediaPanel panel = new MediaPanel(fmgr);
                frame.getContentPane().add(panel, BorderLayout.CENTER);

                // initialize the task, which will add sprites and start them up
                task.init(imgr, panel);

                // show our panel and start the frame manager running
                frame.setSize(500, 500);
                frame.setVisible(true);
                fmgr.start();
            }
        });
    }
}
