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


   private static final class NameBinding {
      final int route;
      final int segment;
      final int child;
      final StringListEntry entry;

      NameBinding(int route, int segment, int child, StringListEntry entry) {
         this.route = route;
         this.segment = segment;
         this.child = child;
         this.entry = entry;
      }

      void apply() {
         String value = (String)this.entry.getValue();
         if (this.child < 0) {
            FavelaSplits.setSegmentName(this.route, this.segment, value);
         } else {
            FavelaSplits.setChildName(this.route, this.segment, this.child, value);
         }

      }
   }



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

         List<AbstractConfigListEntry> secBossBarTop = new ArrayList();
         secBossBarTop.add(entryBuilder.startBooleanToggle(Component.literal("Bar Over The Art"), Config.bossBarOnTop).setDefaultValue(false).setTooltip(new Component[]{Component.literal("The server draws its frame over the health bar, which"), Component.literal("can bury it. This paints the bar again afterwards, so"), Component.literal("it sits on top and stays readable.")}).setSaveConsumer((newValue) -> Config.bossBarOnTop = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Boss Bar"), secBossBarTop).setExpanded(false).build());

         List<AbstractConfigListEntry> secVuln = new ArrayList();
         secVuln.add(entryBuilder.startBooleanToggle(Component.literal("Boss Vulnerability"), Config.vulnHud).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Reads the colour of the health bar floating over the"), Component.literal("boss and says on screen when it is taking reduced"), Component.literal("damage or none at all. Nothing shows while it is"), Component.literal("hittable.")}).setSaveConsumer((newValue) -> Config.vulnHud = newValue).build());
         secVuln.add(entryBuilder.startStrField(Component.literal("Resistant Text"), Config.vulnResistantText).setDefaultValue("RESISTANT").setSaveConsumer((newValue) -> Config.vulnResistantText = newValue).build());
         secVuln.add(entryBuilder.startColorField(Component.literal("Resistant Colour"), Config.vulnResistantColor).setDefaultValue(5627135).setSaveConsumer((newValue) -> Config.vulnResistantColor = newValue).build());
         secVuln.add(entryBuilder.startStrField(Component.literal("Invulnerable Text"), Config.vulnInvulnerableText).setDefaultValue("INVULNERABLE").setSaveConsumer((newValue) -> Config.vulnInvulnerableText = newValue).build());
         secVuln.add(entryBuilder.startColorField(Component.literal("Invulnerable Colour"), Config.vulnInvulnerableColor).setDefaultValue(11184810).setSaveConsumer((newValue) -> Config.vulnInvulnerableColor = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Boss Vulnerability"), secVuln).setExpanded(false).build());

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

         List<NameBinding> nameBindings = new ArrayList();
         List<AbstractConfigListEntry> secNames = new ArrayList();

         for(int r = 0; r < FavelaSplits.routeCount(); ++r) {
            List<AbstractConfigListEntry> dungeon = new ArrayList();

            for(int s = 0; s < FavelaSplits.segmentCount(r); ++s) {
               int children = FavelaSplits.childCount(r, s);
               StringListEntry segmentEntry = entryBuilder.startStrField(Component.literal(children > 0 ? "Boss" : "Split " + (s + 1)), FavelaSplits.segmentName(r, s)).setDefaultValue(FavelaSplits.segmentName(r, s)).build();
               nameBindings.add(new NameBinding(r, s, -1, segmentEntry));
               if (children > 0) {
                  List<AbstractConfigListEntry> phases = new ArrayList();
                  phases.add(segmentEntry);

                  for(int c = 0; c < children; ++c) {
                     StringListEntry phase = entryBuilder.startStrField(Component.literal("Phase " + (c + 1)), FavelaSplits.childName(r, s, c)).setDefaultValue(FavelaSplits.childName(r, s, c)).build();
                     nameBindings.add(new NameBinding(r, s, c, phase));
                     phases.add(phase);
                  }

                  dungeon.add(entryBuilder.startSubCategory(Component.literal(FavelaSplits.segmentName(r, s)), phases).setExpanded(false).build());
               } else {
                  dungeon.add(segmentEntry);
               }
            }

            secNames.add(entryBuilder.startSubCategory(Component.literal(FavelaSplits.routeName(r)), dungeon).setExpanded(false).build());
         }

         livesplits.addEntry(entryBuilder.startSubCategory(Component.literal("Split Names"), secNames).setExpanded(false).build());



         builder.setSavingRunnable(() -> {
            for(NameBinding binding : nameBindings) {
               binding.apply();
            }

            FavelaSplits.applyNames();

            Config.save();
         });
         return builder.build();
      };
   }
}
