package controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.stage.Stage;
import javafx.scene.Node;


public class PopupController {

    //FXML

    @FXML
    private Label typeAttributLabel;

    @FXML
    private TextField valueField;

    @FXML
    private Label captionLabel;

    @FXML
    private JFXButton showPredictionButton;

    @FXML
    private Label predictionLabel;

    @FXML
    private JFXButton differentKnnButton;

    @FXML
    private JFXButton sameKnnButton;


    //ATTRIBUTI

    private String msg;
    private String tmp;
    private String prediction;
    private Boolean waitManager = true;
    private Boolean waitClient = true;
    private boolean sameKnn;

    //GET E SET

    public void setWaitManager(Boolean waitManager) {
        this.waitManager = waitManager;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTypeAttributeLabel() {
        return typeAttributLabel.getText();
    }

    public void setTypeAttributeLabel(String tipoAttributo) {
        this.typeAttributLabel.setText(tipoAttributo);
    }

    
    public String getTmp() {
        return tmp;
    }

    public Boolean getWaitClient() {
        return waitClient;
    }

    public void setWaitClient(Boolean waitClient) {
        this.waitClient = waitClient;
    }

    
    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }    

    public void hideButton() {
        showPredictionButton.setOpacity(0);
    }
    
    public boolean isSameKnn() {
        return sameKnn;
    }

    //METODI
    

    public void changeMsg() throws InterruptedException {

        while (waitManager) {
            Thread.currentThread().sleep(300);
        }
        typeAttributLabel.setText(msg);
    }

    
    @FXML
    void nextValue(KeyEvent event) throws InterruptedException {

        if (event.getCode() == KeyCode.ENTER) {

            tmp = valueField.getText().trim();
            valueField.clear();
            System.out.println(tmp);

            //se tmp non è vuota la mando al Client
            if (tmp.compareTo("") != 0) {

                //Attendo che Client prenda tmp e restiuisca il messaggio
                waitManager = true;
                waitClient = false;
                while (waitManager) {
                    Thread.currentThread().sleep(100);
                }
                changeMsg();
            } 
            //se tmp è vuota non faccio nulla
        }
    }

    @FXML
    void close(ActionEvent event) {

        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
        return;
    }

    @FXML
    void changeText(ActionEvent event) throws InterruptedException {

        captionLabel.setOpacity(1);
        System.out.println(prediction);
        predictionLabel.setText(prediction);
        predictionLabel.setOpacity(1);
        showPredictionButton.setOpacity(0);
        sameKnnButton.setLayoutX(14);
        sameKnnButton.setLayoutY(206);
        differentKnnButton.setLayoutX(198);
        differentKnnButton.setLayoutY(206);
    }

    
    public void showButton() {

        showPredictionButton.setOpacity(1);
        showPredictionButton.setLayoutX(77);
        showPredictionButton.setLayoutY(120);
        valueField.setOpacity(0);
        typeAttributLabel.setOpacity(0);
        setWaitClient(true);
    }

    @FXML
    void useSameKnn(ActionEvent event) throws InterruptedException{
        
        //Setto la variabile che comunica al Client di ripetere la predizione
        sameKnn = true;

        //Aspetto che il Client comunichi al Manager il msg giusto
        waitManager = true;
        waitClient = false;
        while(waitManager) {
            Thread.currentThread().sleep(100);
        }

        //Reset delle label per prendere la predizione
        sameKnnButton.setLayoutX(435);
        sameKnnButton.setLayoutY(171);
        differentKnnButton.setLayoutX(435);
        differentKnnButton.setLayoutY(250);
        showPredictionButton.setOpacity(0);
        showPredictionButton.setLayoutY(94);
        showPredictionButton.setLayoutX(288);
        valueField.setOpacity(1);
        typeAttributLabel.setText(msg);
        typeAttributLabel.setOpacity(1);
        captionLabel.setOpacity(0);
        predictionLabel.setOpacity(0);

    }

    @FXML
    void useOtherKnn(ActionEvent event) {
        this.close(event);
        sameKnn = false;
        setWaitClient(false);
    }

}
