import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;

public class ControladorFXML {

    @FXML
    private ImageView and;
    @FXML
    private ImageView nand;
    @FXML
    private ImageView or;
    @FXML
    private ImageView nor;
    @FXML
    private ImageView not;
    @FXML
    private ImageView xor;
    @FXML
    private ImageView xnor;
    @FXML
    private VBox componentsVbox;
    @FXML
    private URL location;
    @FXML
    private AnchorPane circuitPane;

    private ImageView componentSelected;

    public ControladorFXML(){

    }

    @FXML
    public void initialize(){
        ObservableList<Node> components=componentsVbox.getChildren();

        for(Node component: components){
            setUpDrag((ImageView) component);

        }
        setUpDrop();
    }

    public void handleClickComponents(ImageView source) {
        source.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if(source.getOpacity()==1.0){
                    source.setOpacity(0.6) ;
                    componentSelected= source;
                }else{
                    source.setOpacity(1) ;
                    componentSelected= null;
                }
                event.consume();
            }
        });
    }
    public void setUpDrag(ImageView source){
        source.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                /* drag was detected, start a drag-and-drop gesture*/
                /* allow any transfer mode */
                Dragboard db = source.startDragAndDrop(TransferMode.COPY);

                /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(source.getId());
                db.setContent(content);

                event.consume();
            }
        });

        source.setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* the drag and drop gesture ended */
                /* if the data was successfully moved, clear it */
                if (event.getTransferMode() == TransferMode.MOVE) {
                    System.out.print("Success Received");
                }
                event.consume();
            }
        });
    }
    public void setUpDrop() {
        circuitPane.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* data is dragged over the target */
                /* accept it only if it is not dragged from the same node
                 * and if it has a string data */
                if (event.getGestureSource() != circuitPane &&
                        event.getDragboard().hasString()) {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY);
                    System.out.print("Drop is possible");
                }

                event.consume();
            }
        });

        circuitPane.setOnDragEntered(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* the drag-and-drop gesture entered the target */
                /* show to the user that it is an actual gesture target */
                if (event.getGestureSource() != circuitPane &&
                        event.getDragboard().hasString()) {
                    System.out.print("Entered Drag Zone");
                }

                event.consume();
            }
        });

        circuitPane.setOnDragExited(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* mouse moved away, remove the graphical cues */
                System.out.print("Left Drag Zone");

                event.consume();
            }
        });

        circuitPane.setOnDragDropped(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* data dropped */
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    System.out.print("Dropped Succesful");
                }
                /* let the source know whether the string was successfully
                 * transferred and used */
                event.setDropCompleted(success);

                event.consume();
            }
        });

    }

}