package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.print.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.StringConverter;


public class Main extends Application
        implements EventHandler<MouseEvent> {

    protected int WIDTH = 800;
    protected int HEIGHT = 800;
    private AnimationTimer animationTimer;

    private double x, y;
    private SudokuBoard MainBoard;
    private SudokuGraphics GameGraphics;
    private SudokuEngine GameEngine;
    private ContextMenu contextMenu;
    private Rectangle background;
    private CheckBox chbShowPossibleNumbers;
    private CheckBox chbShowAnimation;
    private CheckBox chbShowInvalidFields;
    private Button butGenerate;
    private Button butSolve;
    private Button butValidateSolution;
    private Button butReset;
    private Button butPrint;
    private ComboBox<Pair<Integer, String>> cbxDifficulty;
    private Label lblMain;
    private Label lblDifficulty;
    private int selectedGridID;
    private int controlsCount;
    private Group root;
    private PrinterJob printerJob;

    private ObservableList<Pair<Integer, String>> listDifficulties;
    //To implement
    //private VBox menuContainer;


    public static void main(String[] args)
            throws Exception {
        try {
            launch(args);
        } catch (Exception e) {
            Exception ex = new Exception();
            ex.addSuppressed(e);
            throw ex;
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new Group();
        Initialize(primaryStage, root);

        root.setFocusTraversable(true);
        root.requestFocus();
        //root.setOnMouseMoved(this);
        root.setOnMouseClicked(this);
    }

    void Initialize(Stage stage, Group root) {
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle("SUDOKU");
        stage.setScene(scene);
        stage.show();

        //init main variables
        initMenus();
        controlsCount = 0;
        initScene(root);
        initGame(root);
        initHandlers();

    }

    private void initScene(Group root) {
        double xControlOffset;
        double maxControlWidth = 0.0;
        listDifficulties = FXCollections.observableArrayList();
        listDifficulties.addAll(
                new Pair<>(50, "Easy (50 clues)"),
                new Pair<>(40, "Medium (40 clues)"),
                new Pair<>(30, "Hard (30 clues)"),
                new Pair<>(20, "Extreme (20 clues)"),
                new Pair<>(0, "User defined (min of 17 clues)")
        );


        background = new Rectangle(WIDTH, HEIGHT);
        background.setFill(Color.WHITE);
        background.setLayoutX(0);
        background.setLayoutY(0);
        root.getChildren().add(background);

        xControlOffset = 10;

        lblDifficulty = new Label();
        lblDifficulty.setLayoutX(xControlOffset);
        lblDifficulty.setLayoutY(10);
        lblDifficulty.setText("Difficulty");
        root.getChildren().add(lblDifficulty);

        cbxDifficulty = new ComboBox<>();
        cbxDifficulty.getItems().addAll(listDifficulties);

        cbxDifficulty.setConverter(new StringConverter<Pair<Integer, String>>() {
            @Override
            public String toString(Pair<Integer, String> object) {
                return object.getValue();
            }

            @Override
            public Pair<Integer, String> fromString(String string) {
                return null;
            }
        });

        cbxDifficulty.getSelectionModel().select(0);
        cbxDifficulty.setLayoutX(xControlOffset);
        cbxDifficulty.setLayoutY(30);
        root.getChildren().add(cbxDifficulty);
        // cbxDifficulty.layout();

        butGenerate = new Button("Generate new Sudoku");
        butGenerate.setLayoutX(xControlOffset);
        butGenerate.setLayoutY(60);
        butGenerate.setPrefWidth(150);
        //butGenerate.
        root.getChildren().add(butGenerate);

        butReset = new Button("Reset");
        butReset.setLayoutX(170);
        butReset.setLayoutY(60);
        root.getChildren().add(butReset);

        root.applyCss();
        root.layout();
        maxControlWidth = Math.max(Math.max(cbxDifficulty.getWidth(), butGenerate.getWidth()), butReset.getWidth());
        xControlOffset += nextInt(maxControlWidth + 50, 5);

        chbShowPossibleNumbers = new CheckBox("Show possible numbers");
        chbShowPossibleNumbers.setLayoutX(xControlOffset);
        chbShowPossibleNumbers.setLayoutY(25);
        root.getChildren().add(chbShowPossibleNumbers);

        chbShowInvalidFields = new CheckBox("Show invalid fields");
        chbShowInvalidFields.setLayoutX(xControlOffset);
        chbShowInvalidFields.setLayoutY(45);
        root.getChildren().add(chbShowInvalidFields);

        chbShowAnimation = new CheckBox("Show solving animation");
        chbShowAnimation.setLayoutX(xControlOffset);
        chbShowAnimation.setLayoutY(65);
        root.getChildren().add(chbShowAnimation);

        root.applyCss();
        root.layout();
        maxControlWidth = Math.max(Math.max(chbShowPossibleNumbers.getWidth(), chbShowInvalidFields.getWidth()), chbShowAnimation.getWidth());
        xControlOffset += nextInt(maxControlWidth + 50, 5);

        butSolve = new Button("Solve Sudoku");
        butSolve.setLayoutX(xControlOffset);
        butSolve.setLayoutY(25);
        root.getChildren().add(butSolve);

        butValidateSolution = new Button("Validate solution");
        butValidateSolution.setLayoutX(xControlOffset);
        butValidateSolution.setLayoutY(60);
        root.getChildren().add(butValidateSolution);

        root.applyCss();
        root.layout();
        maxControlWidth = Math.max(butSolve.getWidth(), butValidateSolution.getWidth());
        xControlOffset += nextInt(maxControlWidth + 50, 5);

        butPrint = new Button("Print");
        butPrint.setLayoutX(xControlOffset);
        butPrint.setLayoutY(25);
        butPrint.setPrefHeight(60);
        butPrint.setPrefWidth(60);
        root.getChildren().add(butPrint);

        controlsCount = root.getChildren().size();
    }

    private void initMenus() {
        contextMenu = new ContextMenu();
        //MenuItem menuItem;
        for (int i = 0; i < 10; i++) {
            MenuItem menuItem = new MenuItem(i == 0 ? "Clear" : String.valueOf(i));
            menuItem.setUserData(i);
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        int selectedNumber = (int) ((MenuItem) event.getTarget()).getUserData();
                        //selectedGridID=(int)((SudokuGraphics.GridField)event.getSource()).getUserData();
                        GameEngine.Update(selectedGridID / 9, selectedGridID % 9, selectedNumber);
                    } catch (Exception ex) {
                        Alert messageBox = new Alert(Alert.AlertType.ERROR);
                        messageBox.setTitle("ERROR");
                        messageBox.setContentText(ex.getMessage());
                        messageBox.show();
                    }
                }
            });
            contextMenu.getItems().add(i, menuItem);
        }
    }


    private void initGame(Group root) {
        MainBoard = SudokuBoard.TestCase();
        GameGraphics = new SudokuGraphics();
        GameGraphics.setLayoutX(WIDTH / 12);
        GameGraphics.setLayoutY(100);

        GameEngine = new SudokuEngine(MainBoard, GameGraphics);
        GameEngine.InitializeGraphics(WIDTH / 11);
        GameEngine.Update();//to show only needed data
        root.getChildren().add(GameEngine.getGraphics());
    }

    private void initHandlers() {
        GameEngine.setOnMouseEventForFields(this);

        chbShowPossibleNumbers.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GameEngine.setShowPossibleNumbers(chbShowPossibleNumbers.isSelected());
                GameEngine.Update();
            }
        });
        chbShowInvalidFields.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GameEngine.setShowInvalidFields(chbShowInvalidFields.isSelected());
                //GameEngine.setDebug(true);
                GameEngine.Update();
            }
        });

        //TODO:Improve on this method, without throwing exceptions
        butValidateSolution.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert messageBox = new Alert(Alert.AlertType.INFORMATION);
                messageBox.setTitle("Solution validation");
                try {
                    if (GameEngine.ValidateSolution()) {
                        messageBox.setContentText("Congratulations. Solve correct");
                    } else {
                        messageBox.setContentText("Sorry. You made at least one mistake");
                    }
                    messageBox.show();

                } catch (Exception ex) {
                    messageBox.setContentText(ex.getMessage());
                    messageBox.show();
                }
            }
        });

        butSolve.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Task<Void> taskSolveInBackground = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            GameEngine.Solve(chbShowAnimation.isSelected());

                        } catch (Exception ex) {
                            Exception ee = new Exception();
                            ee.addSuppressed(ex);
                            throw ee;
                        }
                        return null;
                    }
                };
                taskSolveInBackground.setOnSucceeded((WorkerStateEvent) ->
                        {
                            GameEngine.Update();
                            EnableControls();
                            butValidateSolution.fire();
                        }
                );
                DisableControls();
                new Thread(taskSolveInBackground).start();
            }

        });

        butReset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GameEngine.Reset();
            }
        });

        butPrint.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                root.applyCss();
                root.layout();

                printerJob = PrinterJob.createPrinterJob();
                if (printerJob != null && printerJob.showPrintDialog(GameGraphics.getScene().getWindow())) {
                    Printer printer = printerJob.getPrinter();
                    System.out.println(printer.toString());
                    PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
                    System.out.println(pageLayout.toString());
                    //pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
                    //System.out.println(pageLayout.toString());
                    //pageLayout.get
                    double minScale = Math.min(
                            pageLayout.getPrintableWidth() / GameGraphics.getBoundsInParent().getWidth(),
                            pageLayout.getPrintableHeight() / GameGraphics.getBoundsInParent().getHeight()
                    );

                    Scale scale = new Scale(minScale, minScale);
                    Translate translate = new Translate(
                            -GameGraphics.getLayoutX(),
                            -GameGraphics.getLayoutY());

                    GameGraphics.getTransforms().add(translate);
                    GameGraphics.getTransforms().add(scale);

                    //GameGraphics.applyCss();
                    //GameGraphics.layout();
                    if (printerJob.printPage(GameGraphics))
                        printerJob.endJob();

                    GameGraphics.getTransforms().remove(scale);
                    GameGraphics.getTransforms().remove(translate);

                }
            }
        });

        butGenerate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (cbxDifficulty.getValue().getKey() == 0) {
                    if (butGenerate.getText().equals("Generate new Sudoku")) {
                        butGenerate.setText("Finish");
                        GameEngine.setUserDefinedPuzzleMode(true);
                        butGenerate.setDisable(true);
                    } else {
                        butGenerate.setText("Generate new Sudoku");
                        GameEngine.setUserDefinedPuzzleMode(false);
                    }
                }
                //} else {
                GameEngine.Generate(cbxDifficulty.getValue().getKey(), System.nanoTime());
                GameEngine.Update();

            }
        });
    }

    private void MouseEventHandler(MouseEvent mouseEvent, Object sender) {
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            contextMenu.hide();
        }

        if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED && mouseEvent.getButton() == MouseButton.SECONDARY && sender instanceof SudokuGraphics.GridField) {
            selectedGridID = Integer.valueOf(((SudokuGraphics.GridField) mouseEvent.getSource()).getId());
            contextMenu.show(background, mouseEvent.getScreenX(), mouseEvent.getSceneY());
        }
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED && mouseEvent.getButton() == MouseButton.PRIMARY && sender instanceof SudokuGraphics.GridField) {
            selectedGridID = Integer.valueOf(((SudokuGraphics.GridField) mouseEvent.getSource()).getId());
            GameEngine.markField(selectedGridID / 9, selectedGridID % 9);
            GameEngine.Update();
            butGenerate.setDisable(GameEngine.getSelectedFieldsCount() < 17);
        }
        mouseEvent.consume();
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        MouseEventHandler(mouseEvent, mouseEvent.getSource());
    }

    private void DisableControls() {
        for (int i = 1; i < controlsCount; i++)
            root.getChildren().get(i).setDisable(true);

    }

    private void EnableControls() {
        for (int i = 1; i < controlsCount; i++)
            root.getChildren().get(i).setDisable(false);
    }

    private int nextInt(double number, int divider) {
        return (((int) (number + divider)) / divider) * divider;
    }
}
