package sample;

import com.sun.xml.internal.bind.v2.runtime.ClassBeanInfoImpl;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class SudokuNumpad
        extends Group {

    public class ClosingEvent
            extends Event {
        //public final EventType<ClosingEvent> CLOSING_EVENT_EVENT_TYPE = new EventType(ANY, "ON_CLOSING");
        private int number;

        public ClosingEvent(EventType<? extends Event> eventType, int value) {
            super(eventType);
            number = value;
        }

        public int getNumber() {
            return number;
        }
    }

    //private Group group;
    private Rectangle background;
    private int number;
    final int buttonSize = 40;
    private boolean isVisible;
    public Event onCloseEvent;
    private EventType onCloseEventType = new EventType(ClosingEvent.ANY);
    public EventHandler<ClosingEvent> onCloseEventHandler;

    public SudokuNumpad(Group owner) {
        super();
        initialize();
        owner.getChildren().add(this);
        this.onCloseEventHandler = null;
        this.onCloseEvent = new ClosingEvent(ClosingEvent.ANY, -1);
        this.hide();
    }

    public boolean isShown() {
        return this.isVisible;
    }

    public void setOnClosing(EventHandler<ClosingEvent> value) {
        this.addEventHandler(onCloseEventType, value);
        onCloseEventHandler = value;

    }

    private void initialize() {
        this.background = new Rectangle(20 + 3 * buttonSize, 20 + 4 * buttonSize);
        this.background.setFill(Color.WHITESMOKE);
        this.background.setStroke(Color.GRAY);
        this.background.setStrokeWidth(2.0);
        this.getChildren().add(this.background);
        number = -1;

        Button button;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                Integer id = 1 + j * 3 + i;
                button = new Button();
                button.setId(id.toString());
                button.setText(id.toString());
                button.setLayoutX(10 + i * buttonSize);
                button.setLayoutY(10 + j * buttonSize);
                button.setPrefWidth(buttonSize);
                button.setPrefHeight(buttonSize);
                button.setOnAction(this::handle);
                this.getChildren().add(button);
            }
        button = new Button();
        button.setId("0");
        button.setText("Clear");
        button.setLayoutX(10);
        button.setLayoutY(10 + buttonSize * 3);
        button.setPrefWidth(buttonSize * 2);
        button.setPrefHeight(buttonSize);
        button.setOnAction(this::handle);
        this.getChildren().add(button);
        button = new Button();
        button.setId("-1");
        button.setText("X");
        button.setLayoutX(10 + buttonSize * 2);
        button.setLayoutY(10 + buttonSize * 3);
        button.setPrefWidth(buttonSize);
        button.setPrefHeight(buttonSize);
        button.setOnAction(this::handle);
        button.setCancelButton(true);
        button.setDefaultButton(true);
        this.getChildren().add(button);
    }

    public void setShowPosition(double x, double y) {
        this.setLayoutX(x - this.background.getWidth() / 2);
        this.setLayoutY(y - 10 - buttonSize * 1.5);
    }

    public void hide() {
        this.setVisible(false);
        this.toBack();
        this.setMouseTransparent(true);
        this.isVisible = false;
        if (this.onCloseEventHandler != null) {
            if (number > -1)
                this.fireEvent(new ClosingEvent(onCloseEventType, number));
        }
    }

    public void show(double x, double y) {
        number = -1;
        setShowPosition(x, y);
        this.setVisible(true);
        this.toFront();
        this.setMouseTransparent(false);
        this.isVisible = true;
    }

    public int getNumber() {
        return number;
    }

    private void handle(ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof Button) {
            number = Integer.valueOf(((Button) actionEvent.getSource()).getId());
            this.hide();
        }
    }

}
