package voicechat.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileController {

    @FXML
    Label labTitleLogin;
    @FXML
    TextField textLogin;
    @FXML
    TextField textName;
    @FXML
    Button butEdit;
    @FXML
    Button butSave;
    @FXML
    Button butCancel;

    String previousName;

    @FXML
    public void actionSave(){
        // TODO: отправить на сервер
        textName.setEditable(false);
        butEdit.setVisible(true);
        butSave.setVisible(false);
        butCancel.setVisible(false);
    }

    @FXML
    public void actionEdit(){
        previousName = textName.getText();
        textName.setEditable(true);
        butEdit.setVisible(false);
        butSave.setVisible(true);
        butCancel.setVisible(true);
    }

    @FXML
    public void actionCancel(){
        textName.setEditable(false);
        textName.setText(previousName);
        butEdit.setVisible(true);
        butSave.setVisible(false);
        butCancel.setVisible(false);
    }
}
