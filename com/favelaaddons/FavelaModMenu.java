package com.favelaaddons;

import com.mojang.blaze3d.platform.InputConstants.Type;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FavelaModMenu implements ModMenuApi {




   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return (parent) -> {
         ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.literal("FavelaAddons"));
         ConfigEntryBuilder entryBuilder = builder.entryBuilder();
         ConfigCategory qol = builder.getOrCreateCategory(Component.literal("QOL"));

         List<AbstractConfigListEntry> secAlerts = new ArrayList();
         secAlerts.add(entryBuilder.startStrField(Component.literal("Trigger Text(s)"), Config.triggerText).setDefaultValue("").setTooltip(new Component[]{Component.literal("Messages that trigger the on-screen alert."), Component.literal("Separate several with commas.")}).setSaveConsumer((newValue) -> Config.triggerText = newValue).build());
         secAlerts.add(entryBuilder.startStrField(Component.literal("On-Screen Alert Message"), Config.alertText).setDefaultValue("CAUTION!").setSaveConsumer((newValue) -> Config.alertText = newValue).build());
         secAlerts.add(entryBuilder.startColorField(Component.literal("Alert Color"), Config.alertColor).setDefaultValue(16733525).setSaveConsumer((newValue) -> Config.alertColor = newValue).build());
         secAlerts.add(entryBuilder.startIntField(Component.literal("Alert Time (Seconds)"), Config.alertTime / 20).setDefaultValue(5).setMin(1).setMax(60).setSaveConsumer((newValue) -> Config.alertTime = newValue * 20).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Alerts"), secAlerts).setExpanded(false).build());

         List<AbstractConfigListEntry> secDps = new ArrayList();
         secDps.add(entryBuilder.startBooleanToggle(Component.literal("Show DPS HUD"), Config.dpsHudEnabled).setDefaultValue(true).setSaveConsumer((newValue) -> Config.dpsHudEnabled = newValue).build());
         secDps.add(entryBuilder.startBooleanToggle(Component.literal("Hide Damage Numbers"), Config.hideDamageNumbers).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Hides floating numbers to improve FPS")}).setSaveConsumer((newValue) -> Config.hideDamageNumbers = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("DPS"), secDps).setExpanded(false).build());

         List<AbstractConfigListEntry> secSounds = new ArrayList();
         secSounds.add(entryBuilder.startStrField(Component.literal("Sound Triggers"), Config.soundTriggers).setDefaultValue("").setTooltip(new Component[]{Component.literal("Messages that trigger the alert sound."), Component.literal("Separate several with commas.")}).setSaveConsumer((newValue) -> Config.soundTriggers = newValue).build());
         secSounds.add(entryBuilder.startFloatField(Component.literal("Sound Volume"), Config.soundVolume).setDefaultValue(1.0F).setMin(0.0F).setMax(2.0F).setTooltip(new Component[]{Component.literal("Custom sound volume (0.0 to 2.0)")}).setSaveConsumer((newValue) -> Config.soundVolume = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Sounds"), secSounds).setExpanded(false).build());

         List<AbstractConfigListEntry> secTraps = new ArrayList();
         secTraps.add(entryBuilder.startBooleanToggle(Component.literal("Trap Counter"), Config.trapCounter).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Counts how many traps of your ability are on the ground.")}).setSaveConsumer((newValue) -> Config.trapCounter = newValue).build());
         secTraps.add(entryBuilder.startBooleanToggle(Component.literal("Hide When Empty"), Config.trapCounterHideEmpty).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Hides the counter while no trap is on the ground.")}).setSaveConsumer((newValue) -> Config.trapCounterHideEmpty = newValue).build());

         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Traps"), secTraps).setExpanded(false).build());

         List<AbstractConfigListEntry> secCalls = new ArrayList();
         secCalls.add(entryBuilder.startBooleanToggle(Component.literal("Ambush and Deathmark"), Config.calls).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Calls the party plays for itself while a boss bar is up:"), Component.literal("it watches the bar and shouts at the percentages below."), Component.literal("Drag the text with /fa hud.")}).setSaveConsumer((newValue) -> Config.calls = newValue).build());
         secCalls.add(entryBuilder.startIntField(Component.literal("Call Ambush At (%)"), Config.ambushAt).setDefaultValue(60).setMin(1).setMax(100).setTooltip(new Component[]{Component.literal("Ambush only boosts damage above 60% of the boss health,"), Component.literal("so the call fires as the bar drops past this number.")}).setSaveConsumer((newValue) -> Config.ambushAt = newValue).build());
         secCalls.add(entryBuilder.startStrField(Component.literal("Ambush Text"), Config.ambushText).setDefaultValue("AMBUSH").setSaveConsumer((newValue) -> Config.ambushText = newValue).build());
         secCalls.add(entryBuilder.startIntField(Component.literal("Call Deathmark At (%)"), Config.deathmarkAt).setDefaultValue(40).setMin(1).setMax(100).setTooltip(new Component[]{Component.literal("Deathmark only boosts damage below 40% of the boss health,"), Component.literal("so the call fires as the bar drops past this number.")}).setSaveConsumer((newValue) -> Config.deathmarkAt = newValue).build());
         secCalls.add(entryBuilder.startStrField(Component.literal("Deathmark Text"), Config.deathmarkText).setDefaultValue("DEATHMARK").setSaveConsumer((newValue) -> Config.deathmarkText = newValue).build());
         secCalls.add(entryBuilder.startColorField(Component.literal("Ambush Colour"), Config.ambushColor).setDefaultValue(16755200).setSaveConsumer((newValue) -> Config.ambushColor = newValue).build());
         secCalls.add(entryBuilder.startColorField(Component.literal("Deathmark Colour"), Config.deathmarkColor).setDefaultValue(16733525).setSaveConsumer((newValue) -> Config.deathmarkColor = newValue).build());
         secCalls.add(entryBuilder.startIntField(Component.literal("Call Stay Time (ticks)"), Config.callStayTicks).setDefaultValue(40).setMin(5).setMax(200).setTooltip(new Component[]{Component.literal("20 ticks = 1 second.")}).setSaveConsumer((newValue) -> Config.callStayTicks = newValue).build());
         secCalls.add(entryBuilder.startBooleanToggle(Component.literal("Play Sound On A Call"), Config.callSound).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Uses the same favela_alerta.wav as the Sounds section.")}).setSaveConsumer((newValue) -> Config.callSound = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Ambush and Deathmark"), secCalls).setExpanded(false).build());

         List<AbstractConfigListEntry> secPrimed = new ArrayList();
         secPrimed.add(entryBuilder.startBooleanToggle(Component.literal("Primed Timer"), Config.primedTimer).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Counts down to the moment the Primed trait comes"), Component.literal("back, while you hold a weapon that carries it."), Component.literal("Every hit restarts the count, since Primed asks for"), Component.literal("four seconds without attacking.")}).setSaveConsumer((newValue) -> Config.primedTimer = newValue).build());
         secPrimed.add(entryBuilder.startIntField(Component.literal("Recharge (ms)"), Config.primedTimerMs).setDefaultValue(4100).setMin(100).setMax(30000).setTooltip(new Component[]{Component.literal("How long the countdown runs. Primed recharges"), Component.literal("after 4s, so 4100 leaves a margin.")}).setSaveConsumer((newValue) -> Config.primedTimerMs = newValue).build());
         secPrimed.add(entryBuilder.startStrField(Component.literal("Label"), Config.primedTimerLabel).setDefaultValue("Primed ").setTooltip(new Component[]{Component.literal("Text drawn before the number. Leave it empty"), Component.literal("to show the number on its own.")}).setSaveConsumer((newValue) -> Config.primedTimerLabel = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Primed Timer"), secPrimed).setExpanded(false).build());

         List<AbstractConfigListEntry> secBossHp = new ArrayList();
         secBossHp.add(entryBuilder.startBooleanToggle(Component.literal("Boss HP Percent"), Config.bossHp).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Reads the boss bar on screen and shows its health"), Component.literal("as a number. Drag it with /fa hud.")}).setSaveConsumer((newValue) -> Config.bossHp = newValue).build());
         secBossHp.add(entryBuilder.startStrField(Component.literal("Label"), Config.bossHpLabel).setDefaultValue("Boss ").setTooltip(new Component[]{Component.literal("Text before the number. Leave empty for just the percent.")}).setSaveConsumer((newValue) -> Config.bossHpLabel = newValue).build());
         secBossHp.add(entryBuilder.startIntField(Component.literal("Decimals"), Config.bossHpDecimals).setDefaultValue(1).setMin(0).setMax(3).setTooltip(new Component[]{Component.literal("0 shows 85%, 1 shows 85.3%.")}).setSaveConsumer((newValue) -> Config.bossHpDecimals = newValue).build());
         secBossHp.add(entryBuilder.startBooleanToggle(Component.literal("Colour By Health"), Config.bossHpColorByHealth).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Green above 60%, yellow above 25%, red below."), Component.literal("Turn off to use the fixed colour below.")}).setSaveConsumer((newValue) -> Config.bossHpColorByHealth = newValue).build());
         secBossHp.add(entryBuilder.startColorField(Component.literal("Fixed Colour"), Config.bossHpColor).setDefaultValue(16777215).setSaveConsumer((newValue) -> Config.bossHpColor = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Boss HP"), secBossHp).setExpanded(false).build());





         ConfigCategory livesplits = builder.getOrCreateCategory(Component.literal("Livesplits"));
         livesplits.addEntry(entryBuilder.startBooleanToggle(Component.literal("Boss Splits"), Config.splits).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Times each boss and its phases like a speedrun."), Component.literal("A killed boss collapses into one line with its total."), Component.literal("Drag the panel with /fa hud.")}).setSaveConsumer((newValue) -> Config.splits = newValue).build());
         livesplits.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show Phases"), Config.splitsShowPhases).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Lists the phases of the boss you are fighting.")}).setSaveConsumer((newValue) -> Config.splitsShowPhases = newValue).build());
         livesplits.addEntry(entryBuilder.startIntField(Component.literal("Panel Width"), Config.splitsWidth).setDefaultValue(300).setMin(60).setMax(600).setTooltip(new Component[]{Component.literal("Where the times line up on the right.")}).setSaveConsumer((newValue) -> Config.splitsWidth = newValue).build());



         builder.setSavingRunnable(() -> {
            Config.save();
         });
         return builder.build();
      };
   }
}
