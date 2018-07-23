package hr.vkeglevic.doomsdayterminal.ui.views;

import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

/**
 *
 * @author vanja
 */
public class TcpServerSettingsPanel extends Panel {

    private TextBox portTextBox;

    public TcpServerSettingsPanel() {
        super(getLayout());
        init();
    }

    private static LinearLayout getLayout() {
        LinearLayout linearLayout = new LinearLayout(Direction.HORIZONTAL);
        linearLayout.setSpacing(0);
        return linearLayout;
    }

    private void init() {
        portTextBox = new TextBox();

        addComponent(portTextBox.withBorder(Borders.singleLine("Port")));
    }

    public TextBox getPortTextBox() {
        return portTextBox;
    }

}
