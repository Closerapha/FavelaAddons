package com.sapo;

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

public class SapoModMenu implements ModMenuApi {


   private static final class SetupBinding {
      final Config.ArmorSetup setup;
      final BooleanListEntry enabled;
      final StringListEntry name;
      final KeyCodeEntry key;
      final IntegerListEntry slot1;
      final IntegerListEntry slot2;
      final IntegerListEntry slot3;
      final IntegerListEntry slot4;
      final IntegerListEntry cooldown;

      SetupBinding(Config.ArmorSetup setup, BooleanListEntry enabled, StringListEntry name, KeyCodeEntry key, IntegerListEntry slot1, IntegerListEntry slot2, IntegerListEntry slot3, IntegerListEntry slot4, IntegerListEntry cooldown) {
         this.setup = setup;
         this.enabled = enabled;
         this.name = name;
         this.key = key;
         this.slot1 = slot1;
         this.slot2 = slot2;
         this.slot3 = slot3;
         this.slot4 = slot4;
         this.cooldown = cooldown;
      }

      void apply() {
         this.setup.enabled = this.enabled.getValue();
         String typedName = this.name.getValue();
         if (typedName != null && !typedName.trim().isEmpty()) {
            this.setup.name = typedName.trim();
         }

         this.setup.key = this.key.getValue().getKeyCode().getValue();
         this.setup.slot1 = this.slot1.getValue();
         this.setup.slot2 = this.slot2.getValue();
         this.setup.slot3 = this.slot3.getValue();
         this.setup.slot4 = this.slot4.getValue();
         this.setup.cooldownTicks = Math.max(1, this.cooldown.getValue());
      }
   }


   private static boolean openArmorSwapTab = false;

   private static void applyAndReopen(Screen parent, List<SetupBinding> bindings, Runnable change) {
      for(SetupBinding binding : bindings) {
         binding.apply();
      }

      change.run();
      Config.save();
      openArmorSwapTab = true;
      Minecraft client = Minecraft.getInstance();
      client.setScreen((new SapoModMenu()).getModConfigScreenFactory().create(parent));
   }

   private static String describeSetup(Config.ArmorSetup setup) {
      String label = setup.name != null && !setup.name.trim().isEmpty() ? setup.name.trim() : "New Setup";
      String keyName = setup.key > 0 ? Type.KEYSYM.getOrCreate(setup.key).getDisplayName().getString() : "no key";
      StringBuilder slots = new StringBuilder();

      for(int slot : setup.slots()) {
         if (slot >= 1 && slot <= 9) {
            if (slots.length() > 0) {
               slots.append(",");
            }

            slots.append(slot);
         }
      }

      String slotList = slots.length() > 0 ? slots.toString() : "no slots";
      return label + "  [" + keyName + "]  " + slotList + (setup.enabled ? "" : "  (off)");
   }

   private static List<AbstractConfigListEntry> buildSetupEntries(ConfigEntryBuilder entryBuilder, Config.ArmorSetup setup, List<SetupBinding> bindings) {
      BooleanListEntry enabled = entryBuilder.startBooleanToggle(Component.literal("Enabled"), setup.enabled).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Turns this setup on without touching the others.")}).build();
      StringListEntry name = entryBuilder.startStrField(Component.literal("Name"), setup.name).setDefaultValue("New Setup").setTooltip(new Component[]{Component.literal("Shown in the list and in the chat message.")}).build();
      KeyCodeEntry key = entryBuilder.startKeyCodeField(Component.literal("Keybind"), Type.KEYSYM.getOrCreate(setup.key)).setDefaultValue(Type.KEYSYM.getOrCreate(setup.key)).setTooltip(new Component[]{Component.literal("Key that runs this setup. Give each setup its own key.")}).build();
      IntegerListEntry slot1 = entryBuilder.startIntField(Component.literal("Slot 1"), setup.slot1).setDefaultValue(0).setMin(0).setMax(9).setTooltip(new Component[]{Component.literal("Hotbar slot 1-9. Use 0 to skip this one.")}).build();
      IntegerListEntry slot2 = entryBuilder.startIntField(Component.literal("Slot 2"), setup.slot2).setDefaultValue(0).setMin(0).setMax(9).setTooltip(new Component[]{Component.literal("Hotbar slot 1-9. Use 0 to skip this one.")}).build();
      IntegerListEntry slot3 = entryBuilder.startIntField(Component.literal("Slot 3"), setup.slot3).setDefaultValue(0).setMin(0).setMax(9).setTooltip(new Component[]{Component.literal("Hotbar slot 1-9. Use 0 to skip this one.")}).build();
      IntegerListEntry slot4 = entryBuilder.startIntField(Component.literal("Slot 4"), setup.slot4).setDefaultValue(0).setMin(0).setMax(9).setTooltip(new Component[]{Component.literal("Hotbar slot 1-9. Use 0 to skip this one.")}).build();
      IntegerListEntry cooldown = entryBuilder.startIntField(Component.literal("Cooldown (ticks)"), setup.cooldownTicks).setDefaultValue(2).setMin(1).setMax(40).setTooltip(new Component[]{Component.literal("Ticks between each step of this swap."), Component.literal("1 tick = 50ms. Raise it if a piece fails to equip.")}).build();
      bindings.add(new SetupBinding(setup, enabled, name, key, slot1, slot2, slot3, slot4, cooldown));
      List<AbstractConfigListEntry> entries = new ArrayList();
      entries.add(enabled);
      entries.add(name);
      entries.add(key);
      entries.add(slot1);
      entries.add(slot2);
      entries.add(slot3);
      entries.add(slot4);
      entries.add(cooldown);
      return entries;
   }

   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return (parent) -> {
         ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.literal("Sapo Configuration"));
         ConfigEntryBuilder entryBuilder = builder.entryBuilder();
         ConfigCategory qol = builder.getOrCreateCategory(Component.literal("QOL"));
         List<AbstractConfigListEntry> secSpacebar = new ArrayList();
         secSpacebar.add(entryBuilder.startBooleanToggle(Component.literal("Jump Active"), Config.active).setDefaultValue(true).setSaveConsumer((newValue) -> Config.active = newValue).build());
         secSpacebar.add(entryBuilder.startIntField(Component.literal("Min (CPS)"), Config.minCroaks).setDefaultValue(8).setMin(1).setMax(30).setSaveConsumer((newValue) -> Config.minCroaks = newValue).build());
         secSpacebar.add(entryBuilder.startIntField(Component.literal("Max (CPS)"), Config.maxCroaks).setDefaultValue(14).setMin(1).setMax(30).setSaveConsumer((newValue) -> Config.maxCroaks = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Spacebar Spammer"), secSpacebar).setExpanded(false).build());

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
         secTraps.add(entryBuilder.startBooleanToggle(Component.literal("Block Use At Max"), Config.trapBlockAtMax).setDefaultValue(true).setTooltip(new Component[]{Component.literal("At max traps a new one destroys the oldest, so right"), Component.literal("click is blocked while you hold the trap ability."), Component.literal("Hold shift to use it anyway.")}).setSaveConsumer((newValue) -> Config.trapBlockAtMax = newValue).build());
         secTraps.add(entryBuilder.startBooleanToggle(Component.literal("Hide When Empty"), Config.trapCounterHideEmpty).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Hides the counter while no trap is on the ground.")}).setSaveConsumer((newValue) -> Config.trapCounterHideEmpty = newValue).build());

         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Traps"), secTraps).setExpanded(false).build());

         List<AbstractConfigListEntry> secCalls = new ArrayList();
         secCalls.add(entryBuilder.startBooleanToggle(Component.literal("Ambush and Deathmark"), Config.calls).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Calls the party plays for itself while a boss bar is up:"), Component.literal("it watches the bar and shouts at the percentages below."), Component.literal("Drag the text with /fa editarHUD.")}).setSaveConsumer((newValue) -> Config.calls = newValue).build());
         secCalls.add(entryBuilder.startIntField(Component.literal("Call Ambush At (%)"), Config.ambushAt).setDefaultValue(65).setMin(1).setMax(100).setSaveConsumer((newValue) -> Config.ambushAt = newValue).build());
         secCalls.add(entryBuilder.startStrField(Component.literal("Ambush Text"), Config.ambushText).setDefaultValue("AMBUSH").setSaveConsumer((newValue) -> Config.ambushText = newValue).build());
         secCalls.add(entryBuilder.startIntField(Component.literal("Call Deathmark At (%)"), Config.deathmarkAt).setDefaultValue(40).setMin(1).setMax(100).setSaveConsumer((newValue) -> Config.deathmarkAt = newValue).build());
         secCalls.add(entryBuilder.startStrField(Component.literal("Deathmark Text"), Config.deathmarkText).setDefaultValue("DEATHMARK").setSaveConsumer((newValue) -> Config.deathmarkText = newValue).build());
         secCalls.add(entryBuilder.startColorField(Component.literal("Call Colour"), Config.callColor).setDefaultValue(16755200).setSaveConsumer((newValue) -> Config.callColor = newValue).build());
         secCalls.add(entryBuilder.startIntField(Component.literal("Call Stay Time (ticks)"), Config.callStayTicks).setDefaultValue(40).setMin(5).setMax(200).setTooltip(new Component[]{Component.literal("20 ticks = 1 second.")}).setSaveConsumer((newValue) -> Config.callStayTicks = newValue).build());
         secCalls.add(entryBuilder.startBooleanToggle(Component.literal("Play Sound On A Call"), Config.callSound).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Uses the same sapo_alerta.wav as the Sounds section.")}).setSaveConsumer((newValue) -> Config.callSound = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Ambush and Deathmark"), secCalls).setExpanded(false).build());

         List<AbstractConfigListEntry> secBossHp = new ArrayList();
         secBossHp.add(entryBuilder.startBooleanToggle(Component.literal("Boss HP Percent"), Config.bossHp).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Reads the boss bar on screen and shows its health"), Component.literal("as a number. Drag it with /fa editarHUD.")}).setSaveConsumer((newValue) -> Config.bossHp = newValue).build());
         secBossHp.add(entryBuilder.startStrField(Component.literal("Label"), Config.bossHpLabel).setDefaultValue("Boss ").setTooltip(new Component[]{Component.literal("Text before the number. Leave empty for just the percent.")}).setSaveConsumer((newValue) -> Config.bossHpLabel = newValue).build());
         secBossHp.add(entryBuilder.startIntField(Component.literal("Decimals"), Config.bossHpDecimals).setDefaultValue(1).setMin(0).setMax(3).setTooltip(new Component[]{Component.literal("0 shows 85%, 1 shows 85.3%.")}).setSaveConsumer((newValue) -> Config.bossHpDecimals = newValue).build());
         secBossHp.add(entryBuilder.startBooleanToggle(Component.literal("Colour By Health"), Config.bossHpColorByHealth).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Green above 60%, yellow above 25%, red below."), Component.literal("Turn off to use the fixed colour below.")}).setSaveConsumer((newValue) -> Config.bossHpColorByHealth = newValue).build());
         secBossHp.add(entryBuilder.startColorField(Component.literal("Fixed Colour"), Config.bossHpColor).setDefaultValue(16777215).setSaveConsumer((newValue) -> Config.bossHpColor = newValue).build());
         qol.addEntry(entryBuilder.startSubCategory(Component.literal("Boss HP"), secBossHp).setExpanded(false).build());





         List<SetupBinding> setupBindings = new ArrayList();
         if (Build.FULL) {
         ConfigCategory assists = builder.getOrCreateCategory(Component.literal("Assists"));
         List<AbstractConfigListEntry> secCherub = new ArrayList();
         secCherub.add(entryBuilder.startBooleanToggle(Component.literal("Cherub Solver"), Config.aliveOrDeadMode).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Watches the Cherub beam sounds and calls the dodge"), Component.literal("on screen: CROUCH! for beam 1, JUMP! for beam 2."), Component.literal("Drag the text with /fa editarHUD.")}).setSaveConsumer((newValue) -> Config.aliveOrDeadMode = newValue).build());
         assists.addEntry(entryBuilder.startSubCategory(Component.literal("Cherub Solver"), secCherub).setExpanded(false).build());

         List<AbstractConfigListEntry> secAutoClicker = new ArrayList();
         secAutoClicker.add(entryBuilder.startBooleanToggle(Component.literal("Primed Limiter"), Config.primedLimiter).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Spaces out Melinoe auto clicker hits while you hold"), Component.literal("any weapon whose lore carries the Primed trait,"), Component.literal("so the trait has time to come back. Manual clicks are free.")}).setSaveConsumer((newValue) -> Config.primedLimiter = newValue).build());
         secAutoClicker.add(entryBuilder.startIntField(Component.literal("Primed Interval (ms)"), Config.primedIntervalMs).setDefaultValue(4100).setMin(50).setMax(30000).setTooltip(new Component[]{Component.literal("Minimum time between two auto clicker hits."), Component.literal("Primed recharges after 4s, so 4100 leaves a margin.")}).setSaveConsumer((newValue) -> Config.primedIntervalMs = newValue).build());
         assists.addEntry(entryBuilder.startSubCategory(Component.literal("Auto Clicker"), secAutoClicker).setExpanded(false).build());

         List<AbstractConfigListEntry> secAutoWalls = new ArrayList();
         secAutoWalls.add(entryBuilder.startBooleanToggle(Component.literal("Enable Auto Walls"), Config.autoWalls).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Master switch: finds the wall and the gap in it."), Component.literal("The tracer and the camera turn both need this on.")}).setSaveConsumer((newValue) -> Config.autoWalls = newValue).build());
         secAutoWalls.add(entryBuilder.startBooleanToggle(Component.literal("Gap Tracer"), Config.autoWallsTracer).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Draws the line from your crosshair to the gap."), Component.literal("Turning it off keeps the detection running,"), Component.literal("so the camera turn still works.")}).setSaveConsumer((newValue) -> Config.autoWallsTracer = newValue).build());
         secAutoWalls.add(entryBuilder.startBooleanToggle(Component.literal("Camera Turn"), Config.autoWallsTurn).setDefaultValue(false).setTooltip(new Component[]{Component.literal("While on, keeps your view aimed at the gap by itself,"), Component.literal("turning horizontally only, so you just walk forward.")}).setSaveConsumer((newValue) -> Config.autoWallsTurn = newValue).build());
         secAutoWalls.add(entryBuilder.startIntField(Component.literal("Turn Speed (degrees/tick)"), Config.autoWallsTurnSpeed).setDefaultValue(20).setMin(1).setMax(180).setTooltip(new Component[]{Component.literal("How far the view may rotate per tick (20 ticks/second)."), Component.literal("180 snaps almost instantly, 5 sweeps slowly.")}).setSaveConsumer((newValue) -> Config.autoWallsTurnSpeed = newValue).build());
         secAutoWalls.add(entryBuilder.startIntField(Component.literal("Min Gap Distance From Boss"), Config.autoWallsBossClearance).setDefaultValue(8).setMin(0).setMax(64).setTooltip(new Component[]{Component.literal("Gaps closer than this to the boss are ignored,"), Component.literal("so it never aims you into the middle of the arena."), Component.literal("0 accepts any gap.")}).setSaveConsumer((newValue) -> Config.autoWallsBossClearance = newValue).build());
         secAutoWalls.add(entryBuilder.startBooleanToggle(Component.literal("Ignore Rings From The Boss"), Config.autoWallsIgnoreCentre).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Topha fires walls from the arena edge inwards and"), Component.literal("also from the boss outwards. With this on, the camera"), Component.literal("only turns for the ones closing in on you.")}).setSaveConsumer((newValue) -> Config.autoWallsIgnoreCentre = newValue).build());
         assists.addEntry(entryBuilder.startSubCategory(Component.literal("Auto Walls"), secAutoWalls).setExpanded(false).build());

         assists.addEntry(entryBuilder.startTextDescription(Component.literal("§lArmor Swap")).build());
         assists.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Armor Swap"), Config.armorSwapEnabled).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Master switch for every setup below.")}).setSaveConsumer((newValue) -> Config.armorSwapEnabled = newValue).build());
         assists.addEntry(entryBuilder.startBooleanToggle(Component.literal("Return To Previous Slot"), Config.armorSwapReturn).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Selects the slot you were holding once the swap ends.")}).setSaveConsumer((newValue) -> Config.armorSwapReturn = newValue).build());

         assists.addEntry(new SapoButtonEntry(Component.literal("Setups"), Component.literal("+ Add Setup"), () -> {
            applyAndReopen(parent, setupBindings, () -> Config.armorSetups.add(new Config.ArmorSetup("Setup " + (Config.armorSetups.size() + 1), false, 0, 0, 0, 0, 0, 2)));
         }));

         for(Config.ArmorSetup setup : Config.armorSetups) {
            List<AbstractConfigListEntry> setupEntries = buildSetupEntries(entryBuilder, setup, setupBindings);
            setupEntries.add(new SapoButtonEntry(Component.literal("Remove"), Component.literal("Remove This Setup"), () -> {
               applyAndReopen(parent, setupBindings, () -> Config.armorSetups.remove(setup));
            }));
            assists.addEntry(entryBuilder.startSubCategory(Component.literal(describeSetup(setup)), setupEntries).setExpanded(false).build());
         }
            if (openArmorSwapTab) {
               openArmorSwapTab = false;
               builder.setFallbackCategory(assists);
            }
         }
         ConfigCategory livesplits = builder.getOrCreateCategory(Component.literal("Livesplits"));
         livesplits.addEntry(entryBuilder.startBooleanToggle(Component.literal("Boss Splits"), Config.splits).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Times each boss and its phases like a speedrun."), Component.literal("A killed boss collapses into one line with its total."), Component.literal("Drag the panel with /fa editarHUD.")}).setSaveConsumer((newValue) -> Config.splits = newValue).build());
         livesplits.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show Phases"), Config.splitsShowPhases).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Lists the phases of the boss you are fighting.")}).setSaveConsumer((newValue) -> Config.splitsShowPhases = newValue).build());
         livesplits.addEntry(entryBuilder.startIntField(Component.literal("Panel Width"), Config.splitsWidth).setDefaultValue(300).setMin(60).setMax(600).setTooltip(new Component[]{Component.literal("Where the times line up on the right.")}).setSaveConsumer((newValue) -> Config.splitsWidth = newValue).build());

         if (Build.FULL) {
         ConfigCategory dev = builder.getOrCreateCategory(Component.literal("DEV"));
         List<AbstractConfigListEntry> secTraits = new ArrayList();
         secTraits.add(entryBuilder.startBooleanToggle(Component.literal("Trait Detector"), Config.traitDetector).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Reads the lore of the weapon in your hand and"), Component.literal("reports every stat and trait it carries."), Component.literal("Use /fa trait for the full dump.")}).setSaveConsumer((newValue) -> Config.traitDetector = newValue).build());
         secTraits.add(entryBuilder.startBooleanToggle(Component.literal("Show On HUD"), Config.traitHud).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Shows the matched traits on screen while holding"), Component.literal("the weapon. Drag it with /fa editarHUD.")}).setSaveConsumer((newValue) -> Config.traitHud = newValue).build());
         dev.addEntry(entryBuilder.startSubCategory(Component.literal("Traits"), secTraits).setExpanded(false).build());

         List<AbstractConfigListEntry> secDebugger = new ArrayList();
         secDebugger.add(entryBuilder.startBooleanToggle(Component.literal("Enable Debugger"), Config.debuggerEnabled).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Scans every entity around you and dumps them"), Component.literal("to the console and to your clipboard.")}).setSaveConsumer((newValue) -> Config.debuggerEnabled = newValue).build());
         secDebugger.add(entryBuilder.startKeyCodeField(Component.literal("Scan Keybind"), Type.KEYSYM.getOrCreate(Config.debuggerKey)).setDefaultValue(Type.KEYSYM.getOrCreate(72)).setTooltip(new Component[]{Component.literal("Press this key in-game to scan nearby entities.")}).setKeySaveConsumer((newValue) -> Config.debuggerKey = newValue.getValue()).build());
         secDebugger.add(entryBuilder.startBooleanToggle(Component.literal("Crosshair Only"), Config.debuggerCrosshair).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Scans only what the crosshair is pointing at,"), Component.literal("following your line of sight up to the scan radius,"), Component.literal("instead of everything around you.")}).setSaveConsumer((newValue) -> Config.debuggerCrosshair = newValue).build());
         secDebugger.add(entryBuilder.startBooleanToggle(Component.literal("Dump Item Components"), Config.debuggerComponents).setDefaultValue(false).setTooltip(new Component[]{Component.literal("Writes the full component map of every item display"), Component.literal("into the dump file. Very verbose, only for digging.")}).setSaveConsumer((newValue) -> Config.debuggerComponents = newValue).build());
         secDebugger.add(entryBuilder.startIntField(Component.literal("Scan Radius (blocks)"), Config.debuggerRadius).setDefaultValue(20).setMin(1).setMax(128).setTooltip(new Component[]{Component.literal("Radius used when scanning for nearby entities."), Component.literal("Default: 20 blocks.")}).setSaveConsumer((newValue) -> Config.debuggerRadius = newValue).build());
         dev.addEntry(entryBuilder.startSubCategory(Component.literal("Debugger"), secDebugger).setExpanded(false).build());

         List<AbstractConfigListEntry> secEsp = new ArrayList();
         secEsp.add(entryBuilder.startBooleanToggle(Component.literal("Bone ESP"), Config.boneEsp).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Paints every item display holding a Bone"), Component.literal("solid red, visible through blocks.")}).setSaveConsumer((newValue) -> Config.boneEsp = newValue).build());
         secEsp.add(entryBuilder.startBooleanToggle(Component.literal("Carrot on a Stick ESP"), Config.carrotStickEsp).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Paints item displays holding a Carrot on a Stick orange.")}).setSaveConsumer((newValue) -> Config.carrotStickEsp = newValue).build());
         secEsp.add(entryBuilder.startBooleanToggle(Component.literal("Stick ESP"), Config.stickEsp).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Paints item displays holding a plain Stick cyan.")}).setSaveConsumer((newValue) -> Config.stickEsp = newValue).build());
         secEsp.add(entryBuilder.startBooleanToggle(Component.literal("Armor Stand ESP"), Config.armorStandEsp).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Paints armor stands magenta, including the invisible"), Component.literal("ones carrying the item displays.")}).setSaveConsumer((newValue) -> Config.armorStandEsp = newValue).build());
         secEsp.add(entryBuilder.startBooleanToggle(Component.literal("Interaction ESP"), Config.interactionEsp).setDefaultValue(true).setTooltip(new Component[]{Component.literal("Paints interaction entities yellow - the invisible"), Component.literal("clickable hitboxes servers use for NPCs and buttons.")}).setSaveConsumer((newValue) -> Config.interactionEsp = newValue).build());
         secEsp.add(entryBuilder.startIntField(Component.literal("ESP Range (blocks)"), Config.espRange).setDefaultValue(48).setMin(1).setMax(256).setSaveConsumer((newValue) -> Config.espRange = newValue).build());
         dev.addEntry(entryBuilder.startSubCategory(Component.literal("ESP"), secEsp).setExpanded(false).build());
         }


         builder.setSavingRunnable(() -> {
            for(SetupBinding binding : setupBindings) {
               binding.apply();
            }

            Config.save();
         });
         return builder.build();
      };
   }
}
