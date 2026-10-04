package keybindlayouts;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class LayoutScreen extends Screen {
    private TextFieldWidget nameField;
    private String status = null;

    public LayoutScreen() {
        super(Text.literal("Keybind Layouts"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 40;

        if (status != null) {
            ButtonWidget label = ButtonWidget.builder(Text.literal(status), b -> {})
                    .dimensions(cx - 150, 10, 300, 20).build();
            label.active = false;
            addDrawableChild(label);
        }

        // existing layouts: click name to apply, X to delete
        for (String name : new ArrayList<>(LayoutManager.all().keySet())) {
            addDrawableChild(ButtonWidget.builder(Text.literal(name), b -> {
                boolean restart = LayoutManager.apply(name);
                if (restart) {
                    status = "Multi Key Bindings: restart the game to apply";
                    clearAndInit();
                } else {
                    close();
                }
            }).dimensions(cx - 100, y, 170, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> {
                LayoutManager.delete(name);
                clearAndInit();
            }).dimensions(cx + 75, y, 25, 20).build());

            y += 24;
        }

        // save current keybinds as a new layout
        int bottom = this.height - 55;
        nameField = new TextFieldWidget(textRenderer, cx - 100, bottom, 200, 20, Text.literal("Name"));
        nameField.setPlaceholder(Text.literal("Layout name..."));
        addDrawableChild(nameField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save current keybinds"), b -> {
            String n = nameField.getText().trim();
            if (!n.isEmpty()) {
                LayoutManager.saveCurrent(n);
                clearAndInit();
            }
        }).dimensions(cx - 100, bottom + 24, 200, 20).build());
    }
}
