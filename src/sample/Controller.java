package sample;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.util.Callback;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * The JavaFX Controller Class.
 * Controls the behaviour of the JavaFX application
 * @author Andrew White
 * @version 1.0
 */

public class Controller {

    @FXML
    private ImageView pictureViewer;
    @FXML
    private Label pictureTitle;
    @FXML
    private DatePicker imageDateChooser;
    @FXML
    private Label pictureExplanation;
    @FXML
    private Label programTitle;
    @FXML
    private ImageView smallLogo;
    @FXML
    private Button moreInfo;
    @FXML
    private ProgressBar imageLoadProgress;
    @FXML
    private Label imageLoadLabel;
    @FXML
    private BorderPane mainGUI;

    private String defaultImagePath = "one.jpg";
    private File defaultImageFile;
    private String apodImagePath;
    private String DefaultTitle = "";
    private String DefaultExplanation = "Please select the date of the astronomy of the day image you want to view";
    private APOD apod = null;
    private URL LocalCreateURL;
    private String LocalExplanation = "";
    private String LocalTitle = "";
    private LocalDate previousDateSelected;


    /**
     * Initialises the state of the JavaFX application
     * @throws Exception
     */
    public void initialize() throws Exception{

        //Set the progress bar and label to invisible as default
        //These nodes are stacked behind the ImageView node
        imageLoadLabel.setVisible(false);
        imageLoadProgress.setVisible(false);

        //set the initial title and explanation to blank.
        editText(DefaultTitle, DefaultExplanation);

        //display the start image a picture of the Nasa Logo
        updateDisplay(defaultImagePath);

        //set the updateDisplay to fire when a date on the date picker is selected
        imageDateChooser.setOnAction(actionEvent -> getNewImage(imageDateChooser.getValue()));

        //set the program title to have the nasa font
        javafx.scene.text.Font nasaFont = Font.loadFont(getClass().getResourceAsStream("Nasa.ttf"), 80);
        programTitle.setFont(nasaFont);

        //display small logo next to the program title
        File logoFile = new File("one.jpg");
        Image logo = new Image(logoFile.toURI().toString());
        smallLogo.setPreserveRatio(true);
        smallLogo.setFitWidth(100);
        smallLogo.setFitHeight(100);
        smallLogo.setImage(logo);

        //set the "More" button, to display the full explanation of the displayed image in a dialogue Box, when clicked
        moreInfo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //create a dialogue box and configure
                Alert extraInfo = new Alert(Alert.AlertType.INFORMATION, LocalExplanation);
                extraInfo.setTitle("Astronomy picture of the day");
                extraInfo.setHeaderText("Full Explanation");

                //create an ImageView with a shall nasa logo displayed
                ImageView dialogueImage = new ImageView(new Image(new File(defaultImagePath).toURI().toString()));
                dialogueImage.setPreserveRatio(true);
                dialogueImage.setFitWidth(70);
                dialogueImage.setFitHeight(70);

                //set the dialogue box to contain the nasa logo
                extraInfo.setGraphic(dialogueImage);

                //set the styling of the messageBox
                //by getting the root node of the dialogue pane
                //and applying the size and style to it
                DialogPane extraInfoDialogPane = extraInfo.getDialogPane();
                extraInfoDialogPane.setStyle("-fx-font-size: 18; -fx-background: white;");
                extraInfoDialogPane.setPrefWidth(700);

                //get main window location
                Point2D mainWindowLoc = mainWindowCenterPos();

                //set Position of the dialogue box
                double startX = mainWindowLoc.getX() - 350;
                double startY = mainWindowLoc.getY() - 250;
                extraInfo.setX(startX);
                extraInfo.setY(startY);

                //display the dialog box and block thread until ok is clicked
                extraInfo.showAndWait();

            }
        });


        //Create a callback function the will disable future dates in the datepicker
        Callback<DatePicker, DateCell> f1 = new Callback<DatePicker, DateCell>() {
        @Override
        public DateCell call(DatePicker datePicker) {
            return new DateCell(){
                @Override
                public void updateItem(LocalDate localDate, boolean b) {
                    super.updateItem(localDate, b);

                    if (localDate.isAfter(LocalDate.now())){
                        setDisable(true);
                    }

                    if (localDate.isBefore(LocalDate.of(1995, 6,20))){
                        setDisable(true);
                    }

                }
            };
        }
        };
        //attach the callback function that formats DateCells to the Cell Factory of the datepicker
        imageDateChooser.setDayCellFactory(f1);
    }

    /**
     * Method to update the picture displayed in the program with the APOD of the date just selected.
     * Catches a VideoException, is the choosen date has a video and not a image
     * Run when every the datepicker is updated (see initialise method).
     * @param date The date of the new picture to be displayed
     */
    private void getNewImage(LocalDate date){
        try {
            callApodAPI(date);

            //display new image
            updateDisplay(LocalCreateURL);

            //saves the current selected date
            previousDateSelected = date;
        } catch (VideoException e){
            //exception is thrown if there is no image file available on the selected date
            System.out.println("Havent update program as the selected date doesnt contain an image, it contains a video");
            //if the previous date is not null - so if the date with video is not selected as the first selection
            if (!Objects.isNull(previousDateSelected)){
                //get the current EventHandler for the datepicker
                EventHandler<ActionEvent> currentAction = imageDateChooser.getOnAction();
                //set the on Action event to nothing so that when the datepicker value is updated, with setValue,
                //no event is triggered and therefore no image is retrieved
                imageDateChooser.setOnAction(null);
                //as the current selected date is a video, set the datepicker back to the previous selected date,
                //which is currently displayed on the GUI
                imageDateChooser.setValue(previousDateSelected);
                //re-enable the original event to fire when a date is picked
                imageDateChooser.setOnAction(currentAction);
            } else {
                //if the video link is selected as the first selection the display on the datepicker must remain blank
                imageDateChooser.getEditor().setText("");
            }
            //As the video link was selected notiify the user that this was the case.
            displayVideoSelectionMessage();
        }
    }

    /**
     * Displays the image that is at the URL that is passed to it.
     * Uses a background task to download the image, so not to freeze the GUI
     * @param path URL of the image to be displayed.
     */
    private void updateDisplay(URL path){

        //clear the display while new image loads
        //the display will be blank expect the progress bar
        moreInfo.setVisible(false);
        pictureExplanation.setVisible(false);
        pictureTitle.setVisible(false);

        //display the progress nodes
        imageLoadLabel.setVisible(true);
        imageLoadProgress.setVisible(true);

        //set up the ImageViewer to display the image nicely
        pictureViewer.setPreserveRatio(true);
        pictureViewer.setFitWidth(Main.WINDOW_WIDTH - 50);
        pictureViewer.setFitHeight(Main.WINDOW_HEIGHT - 300);
        pictureViewer.setSmooth(true);

        //Creates a background task to download the image located at the provided path
        Task task = new ImageUpdateTask(path);
        new Thread(task).start();

        //updates ImageView with the downloaded image when it is downloaded.
        pictureViewer.imageProperty().bind(task.valueProperty());

        //update the display when the download is complete
        //displaying the new title, explanation and the more button
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                //makes the more button, explanation label and title visible
                //and set the values of the title and the Explanation
                moreInfo.setVisible(true);
                pictureExplanation.setVisible(true);
                pictureTitle.setVisible(true);
                editText(LocalTitle, LocalExplanation);

                //clear the Progress Nodes
                imageLoadLabel.setVisible(false);
                imageLoadProgress.setVisible(false);

                //When the program first loads pictureExplanation will be blank
                //When the program first loads you dont want to display the more button
                if (LocalExplanation.equals("")){
                    moreInfo.setVisible(false);
                }

            }
        });
    }

    /**
     * Overload of other displayImage method. Used to set up the initial image, nasa logo, that is saved locally
     * Converts the images string path to a URL and calls the other displayImage method to set the image to the
     * ImageViewer
     * @param path String of the address of the image
     */
    private void updateDisplay(String path){
        File f1 = new File(path);
        try{
            updateDisplay(f1.toURI().toURL());
        } catch (MalformedURLException e){
            System.out.println("Cant create a URL from the Local file path");
        }


    }


    /**
     * Set the text for the APOD title and explanation
     * @param t The text of the APOD title
     * @param e The text of the APOD explanation
     */
    private void editText(String t, String e){
        //edit the viewer
        pictureTitle.setText(t);
        pictureExplanation.setText(e);
    }

    /**
     * Creates a APOD object with the returned date from the date picker
     * Runs APOD methods to get the APOD data using a HTTPConnection Request and parse the response
     * The parsed data is then saved locally
     * @param choosenDate the date of the APOD to get
     * @throws VideoException when a date with a video rather than a image is choosen
     */

    private void callApodAPI(LocalDate choosenDate) throws VideoException{
        //create an APOD
        apod = new APOD(choosenDate);
        apod.createURL();
        apod.makeAPIcall();
        apod.parseResponse();

        //save current apod call to local class variables
        LocalCreateURL = apod.getApodURL();
        LocalExplanation = apod.getApodExplanation();
        LocalTitle = apod.getApodTitle();

    }

    /**
     * Method that creates and displays a information dialogue if a date is picked that contains an video
     * rather than an image.
     */

    private void displayVideoSelectionMessage(){
        //get main window location
        Point2D mainWindowLoc = mainWindowCenterPos();

        String message = "The date selected contains a video and can not be displayed";

        //create and configure the dialogue
        Alert videoSelected = new Alert(Alert.AlertType.INFORMATION, message);
        videoSelected.setTitle("Video selected");
        videoSelected.setHeaderText("");

        //Set the position of the dialogue Box
        //cant get the height and width of window if its not displayed.
        double startX = mainWindowLoc.getX() - 200;
        double startY = mainWindowLoc.getY() - 100;
        videoSelected.setX(startX);
        videoSelected.setY(startY);

        //format the dialogue box using the root node
        DialogPane rootNode = videoSelected.getDialogPane();
        rootNode.setStyle("-fx-font-size: 18; -fx-background: white;");

        //display and block the UI thread until the OK is clicked.
        videoSelected.showAndWait();
    }

    private Point2D mainWindowCenterPos(){

        Scene thisScene = mainGUI.getScene();
        //System.out.println("Pos X: " + thisScene.getWindow().getX() + "Pos Y: " + thisScene.getWindow().getY());
        double PosX = thisScene.getWindow().getX();
        double PosY = thisScene.getWindow().getY();

        double HalfWidth = thisScene.getWindow().getWidth() / 2;
        double HalfHeight = thisScene.getWindow().getHeight() / 2;

        return new Point2D(PosX + HalfWidth, PosY + HalfHeight);
    }

}

