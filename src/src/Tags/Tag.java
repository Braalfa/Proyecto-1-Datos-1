package Tags;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import SuperList.*;
import java.util.ArrayList;
import java.awt.Point;
import java.util.Random;
import java.awt.Color;
import Component.Component;
import SuperList.*;
public abstract class Tag extends Label {
    protected Point nodCoords;
    protected Point coords;
    protected AnchorPane parent;
    protected ArrayList<Line> lines;
    protected Component component;
    private double relClickX;
    private double relClickY;
    protected ArrayList<Tag> nextTag;
    protected SuperTree superTree;
    private Color linesColor;

    abstract protected void addNextTag(Tag nextTag);

    public Tag(AnchorPane parent, Point nodCoords, Component component){
        this.coords=new Point((int)nodCoords.getX(),(int)nodCoords.getY());
        this.nodCoords=nodCoords;
        this.parent=parent;
        this.component=component;
        this.lines = new ArrayList<>();
        this.setMovementListener();
        this.nextTag=new ArrayList<>();
        this.superTree= SuperTree.getInstance();

        Random random=new Random();
        this.linesColor=new Color(random.nextFloat(),random.nextFloat(),random.nextFloat());
    }

    protected void display(String text){
        this.setText(text);
        this.setFont(new Font("Facade",10));
        this.setStyle("-fx-background-color: #EFEFEF");
        parent.getChildren().add(this);
        parent.applyCss();
        parent.layout();
    }

    private void setMovementListener(){
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Tag source = (Tag) event.getSource();
                if(event.getButton()==MouseButton.PRIMARY) {
                    ScrollPane scrollpane = (ScrollPane) component.getScene().lookup("#scrollPane");
                    relClickX = -source.getLayoutX() + event.getSceneX()+scrollpane.getHvalue()*(parent.getWidth()-scrollpane.getViewportBounds().getWidth());
                    relClickY = -source.getLayoutY() + event.getSceneY()+scrollpane.getVvalue()*(parent.getHeight()-scrollpane.getViewportBounds().getHeight());
                    event.consume();
                    Line line = new Line();
                    if (lines.isEmpty()) {
                        double xpos = source.getNodCords().getX() + source.getComponent().getX();
                        double ypos = source.getNodCords().getY() + source.getComponent().getY();
                        line.setStartX(xpos);
                        line.setStartY(ypos);
                        line.setEndX(xpos);
                        line.setEndY(ypos);
                    } else {
                        Line lastLine = lines.get(lines.size() - 1);
                        line.setStartX(lastLine.getEndX());
                        line.setEndX(lastLine.getEndX());
                        line.setStartY(lastLine.getEndY());
                        line.setEndY(lastLine.getEndY());
                    }
                    source.addLine(line);
                    parent.getChildren().add(line);

                }else if (event.getButton()==MouseButton.SECONDARY){
                    source.goBackHome();
                    source.clearLines();
                }
            }
        });
        this.setOnMouseDragged( new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                Tag source= (Tag)event.getSource();
                ScrollPane scrollpane = (ScrollPane) component.getScene().lookup("#scrollPane");
                source.setLayoutX(event.getSceneX()+scrollpane.getHvalue()*(parent.getWidth()-scrollpane.getViewportBounds().getWidth())-relClickX);
                source.setLayoutY(event.getSceneY()+scrollpane.getVvalue()*(parent.getHeight()-scrollpane.getViewportBounds().getHeight())-relClickY);
                if(lines.size()>=1) {
                    Line thisLine = lines.get(lines.size() - 1);
                    if (!source.overlapsComponents(thisLine)) {
                        if (Math.abs(source.getLayoutX() - thisLine.getStartX()) > Math.abs(source.getLayoutY() - thisLine.getStartY())) {
                            thisLine.setEndX(source.getLayoutX());
                            thisLine.setEndY(thisLine.getStartY());
                        } else {
                            thisLine.setEndY(source.getLayoutY());
                            thisLine.setEndX(thisLine.getStartX());
                        }
                        if (!source.overlapsComponents(thisLine)) {
                            thisLine.setVisible(true);
                        } else {
                            thisLine.setEndY(thisLine.getStartY());
                            thisLine.setEndX(thisLine.getStartX());
                        }
                    } else {
                        thisLine.setEndY(thisLine.getStartY());
                        thisLine.setEndX(thisLine.getStartX());
                        thisLine.setVisible(false);

                    }
                }
                event.consume();

            }
        });
        this.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    Tag source = (Tag) event.getSource();
                    Tag overlap;
                    Line thisLine = lines.get(lines.size() - 1);
                    overlap = source.getOverlap();
                    if (overlap == null) {
                        if (thisLine.getStartX() == thisLine.getEndX() && thisLine.getStartY() == thisLine.getEndY()) {
                            if (lines.size() > 1) {
                                lines.remove(thisLine);
                                parent.getChildren().remove(thisLine);
                                thisLine = lines.get(lines.size() - 1);
                                source.uptadeDisplacedTag(thisLine);
                            } else {
                                source.setLayoutX(coords.getX() + component.getX());
                                source.setLayoutY(coords.getY() + component.getY());
                            }
                        } else {
                            source.uptadeDisplacedTag(thisLine);
                        }
                    } else {
                        if ((((overlap instanceof InputTag && !overlap.hasNextTag() && !superTree.willFeedback(overlap.getComponent(),source.getComponent()))
                                || (overlap instanceof OutputTag && !superTree.willFeedback(source.getComponent(),overlap.getComponent())))
                                && overlap.getComponent() != source.getComponent()) ){
                            ConnectorSingleton connector = ConnectorSingleton.getInstance(source, overlap);
                            boolean result = connector.autoConnect();
                            if (!result) {
                                source.uptadeDisplacedTag(thisLine);
                            } else {
                                source.addNextTag(overlap);
                                overlap.addNextTag(source);
                                source.setVisible(false);
                                overlap.setVisible(false);
                                if(source instanceof InputTag){
                                    overlap.setLinesColor(source.getLinesColor());
                                    source.getLines().addAll(overlap.getLines());
                                    overlap.resetLines();
                                }else{
                                    source.setLinesColor(overlap.getLinesColor());
                                    overlap.getLines().addAll(source.getLines());
                                    source.resetLines();
                                }
                                overlap.goBackHome();
                                source.goBackHome();
                            }
                        } else {
                            if (lines.size() > 1) {
                                lines.remove(thisLine);
                                parent.getChildren().remove(thisLine);
                                thisLine = lines.get(lines.size() - 1);
                                source.uptadeDisplacedTag(thisLine);
                            } else {
                                source.setLayoutX(coords.getX() + component.getX());
                                source.setLayoutY(coords.getY() + component.getY());
                            }
                        }
                    }


                }
            }
        });
    }

    public void uptadeDisplacedTag(Line thisLine){
        if (thisLine.getStartX() > thisLine.getEndX()) {
            this.setLayoutX(thisLine.getEndX() - this.getWidth());
            this.setLayoutY(thisLine.getEndY() - this.getHeight() / 2);
        } else if (thisLine.getStartX() < thisLine.getEndX()) {
            this.setLayoutX(thisLine.getEndX());
            this.setLayoutY(thisLine.getEndY() - this.getHeight() / 2);
        } else if (thisLine.getStartY() > thisLine.getEndY()) {
            this.setLayoutX(thisLine.getEndX() - this.getWidth() / 2);
            this.setLayoutY(thisLine.getEndY() - this.getHeight());
        } else {
            this.setLayoutX(thisLine.getEndX() - this.getWidth() / 2);
            this.setLayoutY(thisLine.getEndY());
        }

    }

    public void goBackHome() {
        this.setLayoutX(coords.getX() + component.getX());
        this.setLayoutY(coords.getY() + component.getY());
    }

    public void disconect(){
        this.clearAllNeighboors();
        this.clearLines();
        this.setLinesColor(this.getLinesColor());
        this.setLayoutX(coords.getX() + component.getX());
        this.setLayoutY(coords.getY() + component.getY());
    }


    private boolean overlaps(Line line, Tag start, Tag end){
        boolean result=false;
        Bounds startBn = start.getComponent().getLayoutBounds();
        Bounds endBn = end.getComponent().getLayoutBounds();

        if( line.intersects(startBn.getMinX()+1,startBn.getMinY(),startBn.getMaxX()-startBn.getMinX()-2, startBn.getMaxY()-startBn.getMinY())  ||
            line.intersects(endBn.getMinX()+1,endBn.getMinY(),endBn.getMaxX()-endBn.getMinX()-2, endBn.getMaxY()-endBn.getMinY())){
                result=true;

        }
        return result;

    }

    public Tag getOverlap(){
        ObservableList<Node> nodos=parent.getChildren();
        Tag overlap=null;
        for(Node nodo:nodos){
            if(nodo instanceof Tag && !nodo.getClass().equals(this.getClass()) && (nodo.isVisible() || nodo instanceof OutputTag)){
                if(nodo.getBoundsInParent().intersects(this.getBoundsInParent())){
                    overlap=(Tag)nodo;
                    break;
                }
            }
        }
        return overlap;
    }

    private Point getNodCords(){
        return this.nodCoords;
    }

    public Component getComponent(){
        return this.component;
    }

    public void removeLastLine(){
        Line line= lines.get(lines.size()-1);
        parent.getChildren().remove(line);
        lines.remove(line);
    }

    public Point getLastLineStartPoint(){
        if(this.lines.isEmpty() ){
            return new Point((int)(nodCoords.getX()+component.getX()),(int)(nodCoords.getY()+component.getY()));
        }else if ( lines.size()==1){
            return new Point((int)(nodCoords.getX()+component.getX()),(int)(nodCoords.getY()+component.getY()));
        }else{
            Line lastLine= lines.get(lines.size()-2);
            return new Point((int)lastLine.getEndX(),(int)lastLine.getEndY());
        }
    }
    public Point getLastLineEndPoint(){
        if(this.lines.isEmpty() || this.hasNextTag()){
            return new Point((int)(nodCoords.getX()+component.getX()),(int)(nodCoords.getY()+component.getY()));
        }else{
            Line lastLine= lines.get(lines.size()-1);
            return new Point((int)lastLine.getEndX(),(int)lastLine.getEndY());
        }
    }

    public boolean hasNextTag(){
        if (!this.nextTag.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public ArrayList<Tag> getNextTag() {
        return nextTag;
    }

    public void clearAllNeighboors(){
        if(this.hasNextTag()){
            for (Tag tag : nextTag) {
                tag.goBackHome();
                if(tag.getNextTag().size()==1){
                    tag.clearLines();
                    tag.setVisible(true);

                }
                tag.getNextTag().remove(this);

            }
            this.nextTag=new ArrayList<>();
        }
        this.setVisible(true);
    }

    public void clearLines(){
        for(Line line:this.lines){
            parent.getChildren().remove(line);
        }
        lines=new ArrayList<>();
    }

    private boolean overlapsComponents(Line line){
        ObservableList<Node> nodes=parent.getChildren();
        Bounds bounds;
        boolean result= false;
        for(Node node: nodes){
            bounds=component.getBoundsInParent();
            if ((node instanceof Component) && ((node!= this.getComponent()
                    ||line.intersects(bounds.getMinX()+1,bounds.getMinY(),bounds.getMaxX()-bounds.getMinX()-2, bounds.getMaxY()-bounds.getMinY())))){
                bounds=node.getBoundsInParent();
                if(line.intersects(bounds)){
                    System.out.print(node);
                    result=true;
                    break;
                 }
            }
        }
        return result;
    }

    public void destroy(){
        this.disconect();
        parent.getChildren().remove(this);
    }

    public void setLinesColor(Color color){
        int r=color.getRed();
        int g=color.getGreen();
        int b=color.getBlue();
        for(Line line: this.lines){
            this.setLineColor(line,r,g,b);
        }
    }

    private void setLineColor(Line line, int r, int g, int b){
        line.setStyle("-fx-stroke:  rgb(" + r + "," + g + ", " + b + ");");
    }

    public void addLine(Line line){
        line.setVisible(true);
        this.getLines().add(line);
        this.setLineColor(line);
    }
    private void setLineColor(Line line){
        int r=this.getLinesColor().getRed();
        int g=this.getLinesColor().getGreen();
        int b=this.getLinesColor().getBlue();
        line.setStyle("-fx-stroke:  rgb(" + r + "," + g + ", " + b + ");");
    }

    public Color getLinesColor(){
        return this.linesColor;
    }

    public void resetLines() {
        this.lines = new ArrayList<>();
    }

    public ArrayList<Line> cloneLines(Point diplacement, AnchorPane parent){
        Line copyLine;
        ArrayList<Line> linesCopy=new ArrayList<>();
        for(Line line:lines){
            copyLine=new Line(line.getStartX()+diplacement.getX(),line.getStartY()+diplacement.getY(),line.getEndX()+diplacement.getX(),line.getEndY()+diplacement.getY());
            linesCopy.add(copyLine);
            parent.getChildren().add(copyLine);
        }
        return linesCopy;
    }

    public Point getCoords() {
        return coords;
    }

    public void setCoords(Point coords) {
        this.coords = coords;
    }

    public void setNextTag(ArrayList<Tag> nextTag) {
        this.nextTag = nextTag;
    }

    public void setLines(ArrayList<Line> lines){
        this.lines=lines;
    }

    public void addToPane(AnchorPane pane){
        pane.getChildren().add(this);
        for(Line line:lines){
            pane.getChildren().add(this);
        }
    }
}