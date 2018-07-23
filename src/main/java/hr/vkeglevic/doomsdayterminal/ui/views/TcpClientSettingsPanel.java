package hr.vkeglevic.doomsdayterminal.ui.views;

import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Direction;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.*;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

/**
 *
 * @author vanja
 */
public class TcpClientSettingsPanel extends Panel {

    private TextBox ipAddressTextBox;
    private TextBox portTextBox;

    public TcpClientSettingsPanel() {
        super(getLayout());
        init();
    }

    private static LinearLayout getLayout() {
        LinearLayout linearLayout = new LinearLayout(Direction.VERTICAL);
        linearLayout.setSpacing(0);
        return linearLayout;
    }

    private void init() {
        ipAddressTextBox = new TextBox();
        portTextBox = new TextBox();

        addComponent(ipAddressTextBox.withBorder(Borders.singleLine("IP")), LinearLayout.createLayoutData(Fill));
        addComponent(portTextBox.withBorder(Borders.singleLine("Port")), LinearLayout.createLayoutData(Fill));
    }

    public TextBox getIpAddressTextBox() {
        return ipAddressTextBox;
    }

    public TextBox getPortTextBox() {
        return portTextBox;
    }

}
