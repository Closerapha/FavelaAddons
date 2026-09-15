package com.sapo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class SapoButtonEntry extends TooltipListEntry<Object> {
   private static final int BUTTON_WIDTH = 150;
   private final Button button;
   private final List<AbstractWidget> widgets;

   public SapoButtonEntry(Component fieldName, Component label, Runnable action) {
      super(fieldName, (java.util.function.Supplier)null);
      this.button = Button.builder(label, (pressed) -> action.run()).bounds(0, 0, BUTTON_WIDTH, 20).build();
      this.widgets = Collections.singletonList(this.button);
   }

   public Object getValue() {
      return null;
   }

   public Optional<Object> getDefaultValue() {
      return Optional.empty();
   }

   public void save() {
   }

   public boolean isEdited() {
      return false;
   }

   public List<? extends GuiEventListener> children() {
      return this.widgets;
   }

   public List<? extends NarratableEntry> narratables() {
      return this.widgets;
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
      super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
      this.button.setX(x);
      this.button.setY(y);
      this.button.setWidth(BUTTON_WIDTH);
      this.button.active = this.isEditable();
      this.button.extractRenderState(graphics, mouseX, mouseY, delta);
   }
}
