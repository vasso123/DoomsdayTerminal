package hr.vkeglevic.doomsdayterminal.ui.views;

import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.ComboBox;
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
    private ComboBox<Integer> baudCB;
    private ComboBox<Integer> dataBitsCB;
    private ComboBox<ComboBoxValue<Integer>> stopBitsCB;
    private ComboBox<ComboBoxValue<Integer>> parityCB;
    private ComboBox<ComboBoxValue<Integer>> flowControlCB;

    public static class ComboBoxValue<V> {

        private final String valueDesc;
        private final V value;

        public ComboBoxValue(String valueDesc, V value) {
            this.valueDesc = valueDesc;
            this.value = value;
        }

        public String getValueDesc() {
            return valueDesc;
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return valueDesc;
        }
        
        

    }

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
        baudCB = new ComboBox<>(110, 300, 600, 1200, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000);
        dataBitsCB = new ComboBox<>(5, 6, 7, 8);
        stopBitsCB = new ComboBox<>(
                new ComboBoxValue<>("1", 1),
                new ComboBoxValue<>("2", 2),
                new ComboBoxValue<>("1.5", 3)
        );
        parityCB = new ComboBox<>(
                new ComboBoxValue<>("NONE", 0),
                new ComboBoxValue<>("ODD", 1),
                new ComboBoxValue<>("EVEN", 2),
                new ComboBoxValue<>("MARK", 3),
                new ComboBoxValue<>("SPACE", 4)
        );
        flowControlCB = new ComboBox<>(
                new ComboBoxValue<>("NONE", 0),
                new ComboBoxValue<>("RTSCTS_IN", 1),
                new ComboBoxValue<>("RTSCTS_OUT", 2),
                new ComboBoxValue<>("XONXOFF_IN", 4),
                new ComboBoxValue<>("XONXOFF_OUT", 8)
        );
        baudCB.setReadOnly(true);
        dataBitsCB.setReadOnly(true);
        stopBitsCB.setReadOnly(true);
        parityCB.setReadOnly(true);
        flowControlCB.setReadOnly(true);

        baudCB.setSelectedIndex(5);
        dataBitsCB.setSelectedIndex(3);

        addComponent(portTB.withBorder(Borders.singleLine("Port")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(baudCB.withBorder(Borders.singleLine("Baud")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(dataBitsCB.withBorder(Borders.singleLine("Data bits")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(stopBitsCB.withBorder(Borders.singleLine("Stop bits")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(parityCB.withBorder(Borders.singleLine("Parity")), GridLayout.createHorizontallyFilledLayoutData(1));
        addComponent(flowControlCB.withBorder(Borders.singleLine("Flow control")), GridLayout.createHorizontallyFilledLayoutData(1));
    }

    public TextBox getPortTB() {
        return portTB;
    }

    public ComboBox<Integer> getBaudCB() {
        return baudCB;
    }

    public ComboBox<Integer> getDataBitsCB() {
        return dataBitsCB;
    }

    public ComboBox<ComboBoxValue<Integer>> getParityCB() {
        return parityCB;
    }

    public ComboBox<ComboBoxValue<Integer>> getFlowControlCB() {
        return flowControlCB;
    }

    public ComboBox<ComboBoxValue<Integer>> getStopBitsCB() {
        return stopBitsCB;
    }

}
