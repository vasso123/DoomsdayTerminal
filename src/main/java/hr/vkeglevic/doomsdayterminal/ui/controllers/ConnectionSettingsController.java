package hr.vkeglevic.doomsdayterminal.ui.controllers;

import hr.vkeglevic.doomsdayterminal.infrastructure.TcpServerConnection;
import hr.vkeglevic.doomsdayterminal.infrastructure.TcpClientConnection;
import hr.vkeglevic.doomsdayterminal.model.Connection;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;
import hr.vkeglevic.doomsdayterminal.infrastructure.SerialConnection;
import hr.vkeglevic.doomsdayterminal.ui.views.SerialSettingsPanel;
import hr.vkeglevic.doomsdayterminal.ui.views.TabbedPanel;
import hr.vkeglevic.doomsdayterminal.ui.views.TcpClientSettingsPanel;
import hr.vkeglevic.doomsdayterminal.ui.views.TcpServerSettingsPanel;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author vanja
 */
public class ConnectionSettingsController {

    private TabbedPanel view;

    public ConnectionSettingsController() {

    }

    public Component getView() {
        return view.withBorder(Borders.singleLine("Connection settings"));
    }

    public void init() {
        List<Pair<String, Panel>> tabs = new ArrayList<>();
        tabs.add(new ImmutablePair<>("TCP Client", new TcpClientSettingsPanel()));
        tabs.add(new ImmutablePair<>("TCP Server", new TcpServerSettingsPanel()));
        tabs.add(new ImmutablePair<>("Serial", new SerialSettingsPanel()));
        view = new TabbedPanel(tabs);
    }

    /**
     * @return new, non opened connection, type depending on the currently
     * selected settings
     */
    public Connection getConnection() {
        Panel currentlySelectedTab = view.getCurrentlySelectedTab();
        if (currentlySelectedTab instanceof TcpClientSettingsPanel) {
            return getTcpClientConnection(currentlySelectedTab);
        } else if (currentlySelectedTab instanceof TcpServerSettingsPanel) {
            return getTcpServerConnection(currentlySelectedTab);
        } else if (currentlySelectedTab instanceof SerialSettingsPanel) {
            return getSerialConnection(currentlySelectedTab);
        } else {
            throw new NotImplementedException("Unknown connection type requested!");
        }
    }

    private Connection getTcpServerConnection(Panel currentlySelectedTab) {
        TcpServerSettingsPanel panel = (TcpServerSettingsPanel) currentlySelectedTab;
        String port = panel.getPortTextBox().getText();
        checkValidTcpPort(port);
        return new TcpServerConnection(Integer.parseInt(panel.getPortTextBox().getText()));
    }

    private Connection getTcpClientConnection(Panel currentlySelectedTab) {
        TcpClientSettingsPanel panel = (TcpClientSettingsPanel) currentlySelectedTab;
        String ip = panel.getIpAddressTextBox().getText();
        String port = panel.getPortTextBox().getText();
        checkValidIpAddress(ip);
        checkValidTcpPort(port);
        return new TcpClientConnection(
                ip,
                Integer.parseInt(panel.getPortTextBox().getText())
        );
    }

    private Connection getSerialConnection(Panel currentlySelectedTab) {
        SerialSettingsPanel panel = (SerialSettingsPanel) currentlySelectedTab;
        String port = panel.getPortTB().getText();
        checkValidSerialPort(port);
        return new SerialConnection(
                panel.getPortTB().getText(), 
                panel.getBaudCB().getSelectedItem(), 
                panel.getDataBitsCB().getSelectedItem(), 
                panel.getStopBitsCB().getSelectedItem().getValue(), 
                panel.getParityCB().getSelectedItem().getValue(),
                panel.getFlowControlCB().getSelectedItem().getValue()
        );
    }

    private void checkValidTcpPort(String port) throws RuntimeException {
        if (!StringUtils.isNumeric(port)) {
            throw new RuntimeException("Please enter a valid TCP port number!");
        }
    }

    private void checkValidIpAddress(String ip) throws RuntimeException {
        if (StringUtils.isBlank(ip)) {
            throw new RuntimeException("Please enter IP address or hostname!");
        }
    }

    private void checkValidSerialPort(String port) {
        if(StringUtils.isBlank(port)) {
            throw new RuntimeException("Please enter serial port name!");
        }
    }

}
