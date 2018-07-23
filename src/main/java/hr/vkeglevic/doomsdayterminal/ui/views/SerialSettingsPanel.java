package hr.vkeglevic.doomsdayterminal.ui.views;

import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

/**
 *
 * @author vanja
 */
public class SerialSettingsPanel extends Panel {

    private TextBox portTB;
    private TextBox baudTB;
    private TextBox dataSizeTB;
    private TextBox parityTB;
    private TextBox handshakeTB;

    public SerialSettingsPanel() {
        super(getLayout());
        init();
    }

    private static LayoutManager getLayout() {
        GridLayout gridLayout = new GridLayout(2);
        return gridLayout;
    }

    private void init() {
        portTB = new TextBox();
        baudTB = new TextBox();
        dataSizeTB = new TextBox();
        parityTB = new TextBox();
        handshakeTB = new TextBox();

        addComponent(portTB.withBorder(Borders.singleLine("Port")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(baudTB.withBorder(Borders.singleLine("Baud")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(dataSizeTB.withBorder(Borders.singleLine("Data size")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(parityTB.withBorder(Borders.singleLine("Parity")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(handshakeTB.withBorder(Borders.singleLine("Handshake")), GridLayout.createHorizontallyFilledLayoutData(1));
    }

    public TextBox getPortTB() {
        return portTB;
    }

    public TextBox getBaudTB() {
        return baudTB;
    }

    public TextBox getDataSizeTB() {
        return dataSizeTB;
    }

    public TextBox getParityTB() {
        return parityTB;
    }

    public TextBox getHandshakeTB() {
        return handshakeTB;
    }

}
