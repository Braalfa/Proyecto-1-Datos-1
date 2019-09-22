package Component;

import javafx.scene.image.Image;

import java.awt.*;

public class OrGate extends Component {

    public OrGate() {

        super(2,  new Point(51,17), new Point(0,10), new Point(0,23), new Image("imgs/or.png"), ComponentType.OR);
    }

    @Override
    public boolean calculate() throws NullPointerException {
        output= input1 || input2;
        return output;
    }
}