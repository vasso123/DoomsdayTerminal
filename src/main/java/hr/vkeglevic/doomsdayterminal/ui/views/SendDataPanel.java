package hr.vkeglevic.doomsdayterminal.ui.views;

import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.BorderLayout;
import static com.googlecode.lanterna.gui2.BorderLayout.Location.*;
import static com.googlecode.lanterna.gui2.Borders.*;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

/**
 *
 * @author vanja
 */
public class SendDataPanel extends Panel {

    private CheckBox sendHexCB;
    private TextBox dataToSendTB;
    private Button sendBTN;
    private Button pasteBTN;

    public SendDataPanel() {
        super(getLayout());
        init();
    }

    private static LayoutManager getLayout() {
        return new BorderLayout();
    }

    private void init() {
        initSendHexCheckbox();
        initDataToSendTextBox();
        initButtons();
    }

    private void initButtons() {
        sendBTN = new Button("Send");
        pasteBTN = new Button("Paste");
        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(0));
        buttonPanel
                .addComponent(pasteBTN.withBorder(singleLineBevel()))
                .addComponent(sendBTN.withBorder(singleLineBevel()));
        addComponent(buttonPanel, RIGHT);
    }

    private void initDataToSendTextBox() {
        Border tbDataBorder = new TextBox().withBorder(singleLine("Send"));
        addComponent(tbDataBorder, CENTER);
        dataToSendTB = (TextBox) tbDataBorder.getComponent();
    }

    private void initSendHexCheckbox() {
        String sendHexCbLabel = "HEX";
        Border sendHexWithBorder = new CheckBox().withBorder(singleLine(sendHexCbLabel));
        addComponent(sendHexWithBorder, LEFT);
        sendHexCB = (CheckBox) sendHexWithBorder.getComponent();
    }

    public CheckBox getSendHexCB() {
        return sendHexCB;
    }

    public TextBox getDataToSendTB() {
        return dataToSendTB;
    }

    public Button getSendBTN() {
        return sendBTN;
    }

    public void setSendButtonListener(Button.Listener l) {
        sendBTN.removeListener(l);
        sendBTN.addListener(l);
    }

    public void setPasteButtonListener(Button.Listener l) {
        pasteBTN.removeListener(l);
        pasteBTN.addListener(l);
    }

    public String getDataToSend() {
        return dataToSendTB.getText();
    }

    public Button getPasteBTN() {
        return pasteBTN;
    }
    
    

}
