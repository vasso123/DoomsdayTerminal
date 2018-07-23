package hr.vkeglevic.doomsdayterminal.ui.views;

import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.CheckBox;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

/**
 *
 * @author vanja
 */
public class DataPanel extends Panel {

    private TextBox dataTB;
    private CheckBox showDataAsHexCB;
    private Button copyToClpbrdBTN;
    private Button clearDataBTN;

    public DataPanel() {
        super(getLayout());
        init();
    }

    private static LayoutManager getLayout() {
        GridLayout gl = new GridLayout(1);
        return gl;
    }

    private void init() {
        initDataTextBox();
        initControls();
    }

    private void initControls() {
        Panel controlsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(0));

        showDataAsHexCB = new CheckBox();
        controlsPanel.addComponent(showDataAsHexCB.withBorder(Borders.singleLine("HEX")));

        copyToClpbrdBTN = new Button("Copy");
        controlsPanel.addComponent(copyToClpbrdBTN.withBorder(Borders.singleLineBevel()));

        clearDataBTN = new Button("Clear");
        controlsPanel.addComponent(clearDataBTN.withBorder(Borders.singleLineBevel()));

        addComponent(controlsPanel);
    }

    private void initDataTextBox() {
        dataTB = new TextBox().setReadOnly(true);
        addComponent(
                dataTB,
                GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.FILL, true, true)
        );
    }

    public TextBox getDataTB() {
        return dataTB;
    }

    public CheckBox getShowDataAsHexCB() {
        return showDataAsHexCB;
    }

    public Button getCopyToClpbrdBTN() {
        return copyToClpbrdBTN;
    }

    public Button getClearDataBTN() {
        return clearDataBTN;
    }

}
