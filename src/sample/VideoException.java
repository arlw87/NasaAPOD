package sample;

/**
 * A exception class that is used to identify when a video is selected rather than a image
 * @author Andrew White
 * @version 1
 */

public class VideoException extends Exception{

    public VideoException() {}
    public VideoException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }
}
