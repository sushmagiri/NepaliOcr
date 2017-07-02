import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class GUIController {
    public Node root;
    Image image;
    File file;
    public Text imageOverlayText;
    public ImageView myImageView;
    public ScrollPane imageContainer;
    public TextArea text;
    public Text statusText;
    public HBox fileControls;
    public HBox container;

    private final double zoomAmount =1.2;
    private final String TESSERACT_PATH = "tesseract-ocr";

    public void saveText() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .addAll(new ExtensionFilter("Text File", "*.txt"));
        File file = fileChooser.showSaveDialog((root.getScene().getWindow()));
        if (file != null)
            try {
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.print(text.getText());
                writer.close();
            } catch (IOException e) {
                // do something
            }
    }

    public void zoomOut() {
        myImageView.setFitWidth(myImageView.getFitWidth()/zoomAmount);
        myImageView.setFitHeight(myImageView.getFitHeight()/zoomAmount);
    }

    public void zoomIn() {
        myImageView.setFitWidth(myImageView.getFitWidth()*zoomAmount);
        myImageView.setFitHeight(myImageView.getFitHeight()*zoomAmount);
    }

    public void fitToContainer(){
        myImageView.setFitHeight(imageContainer.getHeight());
        myImageView.setFitWidth(imageContainer.getWidth()-2); //-2 for borders
    }

    public void originalSize(){
        myImageView.setFitHeight(myImageView.getImage().getHeight());
        myImageView.setFitWidth(myImageView.getImage().getWidth());
    }

    @FXML
    private void locateFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .addAll(new ExtensionFilter("Image files", "*.BMP", "*.PNG", "*.JPG"));

         file = fileChooser.showOpenDialog((root.getScene().getWindow()));
        if (file != null) {
            imageOverlayText.setVisible(false);
            text.setText(null);

            fileControls.getChildren().forEach(node -> node.setDisable(false));

            Image image = new Image(file.toURI().toString(), true);

            myImageView.setImage(image);
            statusText.setText("Image Loaded");
            Filter filter=new Filter();
            File ans= filter.display(file);
            myImageView.setImage(new Image(ans.toURI().toString(), true));
            //for zooming
            image.widthProperty().addListener((observable, oldValue, newValue) ->
                    myImageView.setFitWidth((Double) newValue));
            image.heightProperty().addListener((observable, oldValue, newValue) ->
                    myImageView.setFitHeight((Double) newValue));


            statusText.setText("Filtering Completed");
//            doOcr(file);
        }
    }

//    private void doOcr(File imageFile) {
//        Task<String> ocrTask = new Task<String>() {
//            @Override
//            protected String call() throws Exception {
//                Tesseract tesseract = new Tesseract();
//                tesseract.setDatapath(TESSERACT_PATH);
//                tesseract.setLanguage("nep");
//                try {
//                    return tesseract.doOCR(imageFile);
//                } catch (TesseractException e) {
//                    e.printStackTrace();
//                    statusText.setText(e.getCause().getMessage());
//                }
//                return null;
//            }
//        };
//        ocrTask.setOnSucceeded(event -> {
//            String value = ocrTask.getValue();
//            if (value == null)
//                return;
//            text.setText(ocrTask.getValue());
//            statusText.setText("Completed");
//        });
//        new Thread(ocrTask).start();
//    }

    public void handleButtonOpen(ActionEvent actionEvent) {
       FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        System.out.println(file);
        fileChooser.getExtensionFilters()
                .addAll(new ExtensionFilter("Image files", "*.BMP", "*.PNG", "*.JPG"));

        myImageView.setImage(new Image(file.toURI().toString(), true));


    }

    public void handleButtonExit(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void handleButtonFilter(ActionEvent actionEvent) throws IOException {
        Filter filter=new Filter();
        File ans= filter.display(file);
        myImageView.setImage(new Image(ans.toURI().toString(), true));
        System.out.println("hel");
    }

    public void btnOcr(ActionEvent actionEvent) {
        Task<String> ocrTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath(TESSERACT_PATH);
                tesseract.setLanguage("nep");
                try {
                    return tesseract.doOCR(file);
                } catch (TesseractException e) {
                    e.printStackTrace();
                    statusText.setText(e.getCause().getMessage());
                }
                return null;
            }
        };
        ocrTask.setOnSucceeded(event -> {
            String value = ocrTask.getValue();
            if (value == null)
                return;
            text.setText(ocrTask.getValue());
            statusText.setText("OCR Completed");
        });
        new Thread(ocrTask).start();
    }
}
