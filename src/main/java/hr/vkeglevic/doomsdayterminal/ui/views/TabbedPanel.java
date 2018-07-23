package hr.vkeglevic.doomsdayterminal.ui.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import static com.googlecode.lanterna.gui2.Borders.*;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import static com.googlecode.lanterna.gui2.LinearLayout.*;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.*;
import com.googlecode.lanterna.gui2.Panel;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author vanja
 */
public class TabbedPanel extends Panel {

    private final List<Pair<String, Panel>> tabs;
    private final List<Button> tabChoosers = new ArrayList<>();
    private final Button.Listener buttonListener = this::handleTabChange;
    private final Theme tabChoosedTheme = LanternaThemes.getRegisteredTheme("defrost");
    private final Theme tabNotChoosedTheme = LanternaThemes.getDefaultTheme();
    private final Panel tabChooserPanel;

    public TabbedPanel(List<Pair<String, Panel>> tabs) {
        super(new LinearLayout(Direction.VERTICAL).setSpacing(0));
        this.tabs = tabs;
        tabChooserPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(0));
        init();
    }

    private void init() {
        final int initiallyShowedTab = 0;
        tabs.forEach((tab) -> {
            Button tabChooser = new Button(tab.getKey());
            tabChooser.addListener(buttonListener);
            tabChoosers.add(tabChooser);
        });
        Button initialTabChooser = tabChoosers.get(initiallyShowedTab);
        highlightCurrentTabChooser(initialTabChooser);
        addComponent(tabChooserPanel);
        // showing only the first tab on init
        addComponent(tabs.get(initiallyShowedTab).getValue(), createLayoutData(Fill));
    }

    private void handleTabChange(Button tabChooser) {
        highlightCurrentTabChooser(tabChooser);
        // for now, matching tabs with buttons will be through tab labels
        Panel tabToShow = tabs.stream()
                .filter((tab) -> tab.getKey().equals(tabChooser.getLabel()))
                .findFirst()
                .get()
                .getValue();
        List<Component> children = new ArrayList<>(getChildren());
        removeComponent(children.get(1));
        addComponent(tabToShow, createLayoutData(Fill));
    }

    @Override
    public TerminalSize calculatePreferredSize() {
        // preferred size will be the size of the biggest tab + the tab buttons
        TerminalSize calc = super.calculatePreferredSize();
        for (Pair<String, Panel> tab : tabs) {
            calc = calc.max(tab.getValue().getPreferredSize());
        }
        final int buttonHeight = 3;
        return new TerminalSize(calc.getColumns(), calc.getRows() + buttonHeight);
    }

    public Panel getCurrentlySelectedTab() {
        List<Component> children = new ArrayList<>(getChildren());
        return (Panel) children.get(1);
    }

    private void highlightCurrentTabChooser(Button selectedTabChooser) {
        tabChooserPanel.removeAllComponents();
        tabChoosers.forEach((tabChooser) -> {
            tabChooserPanel.addComponent(
                    tabChooser.withBorder(
                            tabChooser == selectedTabChooser ? singleLine() : singleLineBevel()
                    )
            );
        });
    }

}
