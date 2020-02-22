package sample;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import java.net.URL;

/**
 * A Class to run a background task to download the image from a URL link
 * @author Andrew White
 * @version 1
 */

public class ImageUpdateTask extends Task <Image>{

    private Image im1;
    private URL link;

    public ImageUpdateTask(URL aURL) {
        link = aURL;
    }

    @Override
    protected Image call() throws Exception {
        im1 = new Image(link.toString());
        return im1;
    }
}
