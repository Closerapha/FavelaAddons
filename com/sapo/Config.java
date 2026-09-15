package com.sapo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public class Config {
   public static boolean active = true;
   public static int minCroaks = 8;
   public static int maxCroaks = 14;
   public static boolean devMode = false;
   public static String triggerText = "";
   public static String alertText = "CAUTION!";
   public static int alertX = 100;
   public static int alertY = 50;
   public static float alertScale = 2.0F;
   public static int alertTime = 100;
   public static int alertColor = 16733525;
   public static boolean aliveOrDeadMode = false;
   public static int aliveOrDeadX = 50;
   public static int aliveOrDeadY = 70;
   public static float aliveOrDeadScale = 2.0F;
   public static String soundTriggers = "";
   public static float soundVolume = 1.0F;
   public static boolean dpsHudEnabled = true;
   public static int dpsHudX = 10;
   public static int dpsHudY = 10;
   public static float dpsHudScale = 1.5F;
   public static int dpsHudColor = 16777215;
   public static boolean hideDamageNumbers = false;
   public static boolean primedLimiter = true;
   public static int primedIntervalMs = 4100;
   public static boolean debuggerEnabled = true;
   public static int debuggerKey = 72;
   public static int debuggerRadius = 20;
   public static boolean debuggerCrosshair = false;
   public static boolean debuggerComponents = false;
   public static int portalRange = 64;
   public static String portalModels = "telos:mob/portal";
   public static boolean boneEsp = true;
   public static boolean carrotStickEsp = true;
   public static boolean stickEsp = true;
   public static boolean armorStandEsp = true;
   public static boolean interactionEsp = true;
   public static boolean autoWalls = true;
   public static boolean autoWallsTracer = true;
   public static boolean splits = false;
   public static boolean splitsShowPhases = true;
   public static int splitsMaxRows = 8;
   public static int splitsWidth = 300;
   public static int splitsX = 10;
   public static int splitsY = 130;
   public static float splitsScale = 1.0F;
   public static boolean bossHp = true;
   public static String bossHpLabel = "Boss ";
   public static int bossHpDecimals = 1;
   public static boolean bossHpColorByHealth = true;
   public static int bossHpColor = 16777215;
   public static int bossHpX = 10;
   public static int bossHpY = 100;
   public static float bossHpScale = 1.5F;
   public static boolean calls = false;
   public static int ambushAt = 65;
   public static String ambushText = "AMBUSH";
   public static int deathmarkAt = 40;
   public static String deathmarkText = "DEATHMARK";
   public static int callColor = 16755200;
   public static int callStayTicks = 40;
   public static boolean callSound = false;
   public static int callX = 10;
   public static int callY = 80;
   public static float callScale = 2.0F;
   public static boolean traitDetector = true;
   public static boolean traitHud = true;
   public static int traitHudX = 10;
   public static int traitHudY = 60;
   public static float traitHudScale = 1.2F;
   public static boolean trapCounter = true;
   public static boolean trapBlockAtMax = true;
   public static String trapCounterModels = "trap_n6b";
   public static String[] parsedTrapCounterModels = new String[0];
   public static int trapCounterMax = 15;
   public static int trapCounterRange = 50;
   public static int trapCounterX = 10;
   public static int trapCounterY = 40;
   public static float trapCounterScale = 1.5F;
   public static String trapCounterLabel = "Trap:";
   public static boolean trapCounterHideEmpty = false;
   public static boolean armorSwapEnabled = true;
   public static List<ArmorSetup> armorSetups = defaultSetups();
   public static boolean armorSwapReturn = true;
   public static String autoWallsModels = "ophanim_projectile6";
   public static String autoWallsBossModels = "modelengine:ophanim";
   public static String[] parsedAutoWallsBossModels = new String[0];
   public static int autoWallsBossRange = 50;
   public static int autoWallsBossClearance = 8;
   public static boolean autoWallsIgnoreCentre = true;
   public static String[] parsedAutoWallsModels = new String[0];
   public static String[] parsedPortalModels = new String[0];
   public static boolean autoWallsTurn = false;
   public static int autoWallsTurnSpeed = 20;
   public static int espRange = 48;
   public static String[] parsedSoundTriggers = new String[0];
   public static String[] parsedTextTriggers = new String[0];
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final File CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sapo");
   private static final File FILE;


   public static class ArmorSetup {
      @SerializedName("nome")
      public String name;
      @SerializedName("ativo")
      public boolean enabled;
      @SerializedName("tecla")
      public int key;
      @SerializedName("slot1")
      public int slot1;
      @SerializedName("slot2")
      public int slot2;
      @SerializedName("slot3")
      public int slot3;
      @SerializedName("slot4")
      public int slot4;
      @SerializedName("cooldown")
      public int cooldownTicks;

      public ArmorSetup(String name, boolean enabled, int key, int slot1, int slot2, int slot3, int slot4, int cooldownTicks) {
         this.name = name;
         this.enabled = enabled;
         this.key = key;
         this.slot1 = slot1;
         this.slot2 = slot2;
         this.slot3 = slot3;
         this.slot4 = slot4;
         this.cooldownTicks = cooldownTicks;
      }

      public int[] slots() {
         return new int[]{this.slot1, this.slot2, this.slot3, this.slot4};
      }
   }


   public static void resizeSetups(int count) {
      int target = Math.max(1, Math.min(20, count));

      while(armorSetups.size() > target) {
         armorSetups.remove(armorSetups.size() - 1);
      }

      while(armorSetups.size() < target) {
         armorSetups.add(new ArmorSetup("Setup " + (armorSetups.size() + 1), false, 0, 0, 0, 0, 0, 2));
      }

   }

   public static List<ArmorSetup> defaultSetups() {
      List<ArmorSetup> setups = new ArrayList();
      setups.add(new ArmorSetup("Setup 1", true, 82, 6, 7, 8, 9, 2));
      return setups;
   }

   private static List<ArmorSetup> normalizeSetups(List<ArmorSetup> loaded) {
      List<ArmorSetup> setups = new ArrayList();

      for(int i = 0; i < loaded.size(); ++i) {
         ArmorSetup setup = (ArmorSetup)loaded.get(i);
         if (setup != null) {
            if (setup.name == null || setup.name.isEmpty()) {
               setup.name = "Setup " + (i + 1);
            }

            if (setup.cooldownTicks <= 0) {
               setup.cooldownTicks = 2;
            }

            setups.add(setup);
         }
      }

      return setups;
   }

   private static List<ArmorSetup> migrateSetups(SapoData data) {
      List<ArmorSetup> setups = defaultSetups();
      ArmorSetup first = (ArmorSetup)setups.get(0);
      first.key = data.armorSwapKey != 0 ? data.armorSwapKey : 82;
      first.slot1 = data.armorSwapSlot1 != null ? data.armorSwapSlot1 : 6;
      first.slot2 = data.armorSwapSlot2 != null ? data.armorSwapSlot2 : 7;
      first.slot3 = data.armorSwapSlot3 != null ? data.armorSwapSlot3 : 8;
      first.slot4 = data.armorSwapSlot4 != null ? data.armorSwapSlot4 : 9;
      first.cooldownTicks = data.armorSwapDelay != 0 ? data.armorSwapDelay : 2;
      return setups;
   }
   public static void updateParsedTriggers() {
      if (soundTriggers != null && !soundTriggers.isEmpty()) {
         parsedSoundTriggers = soundTriggers.split(",");

         for(int i = 0; i < parsedSoundTriggers.length; ++i) {
            parsedSoundTriggers[i] = parsedSoundTriggers[i].trim().toLowerCase();
         }
      } else {
         parsedSoundTriggers = new String[0];
      }

      if (trapCounterModels != null && !trapCounterModels.isEmpty()) {
         parsedTrapCounterModels = trapCounterModels.split(",");

         for(int i = 0; i < parsedTrapCounterModels.length; ++i) {
            parsedTrapCounterModels[i] = parsedTrapCounterModels[i].trim();
         }
      } else {
         parsedTrapCounterModels = new String[0];
      }

      if (autoWallsBossModels != null && !autoWallsBossModels.isEmpty()) {
         parsedAutoWallsBossModels = autoWallsBossModels.split(",");

         for(int i = 0; i < parsedAutoWallsBossModels.length; ++i) {
            parsedAutoWallsBossModels[i] = parsedAutoWallsBossModels[i].trim();
         }
      } else {
         parsedAutoWallsBossModels = new String[0];
      }

      if (autoWallsModels != null && !autoWallsModels.isEmpty()) {
         parsedAutoWallsModels = autoWallsModels.split(",");

         for(int i = 0; i < parsedAutoWallsModels.length; ++i) {
            parsedAutoWallsModels[i] = parsedAutoWallsModels[i].trim();
         }
      } else {
         parsedAutoWallsModels = new String[0];
      }

      if (portalModels != null && !portalModels.isEmpty()) {
         parsedPortalModels = portalModels.split(",");

         for(int i = 0; i < parsedPortalModels.length; ++i) {
            parsedPortalModels[i] = parsedPortalModels[i].trim();
         }
      } else {
         parsedPortalModels = new String[0];
      }

      if (triggerText != null && !triggerText.isEmpty()) {
         parsedTextTriggers = triggerText.split(",");

         for(int i = 0; i < parsedTextTriggers.length; ++i) {
            parsedTextTriggers[i] = parsedTextTriggers[i].trim();
         }
      } else {
         parsedTextTriggers = new String[0];
      }

   }

   public static void load() {
      if (FILE.exists()) {
         try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8);

            try {
               SapoData data = (SapoData)GSON.fromJson(reader, SapoData.class);
               active = data.active;
               minCroaks = data.minCroaks;
               maxCroaks = data.maxCroaks;
               devMode = data.devMode;
               if (data.triggerText != null) {
                  triggerText = data.triggerText;
               }

               if (data.alertText != null) {
                  alertText = data.alertText;
               }

               alertX = data.alertX != 0 ? data.alertX : 100;
               alertY = data.alertY != 0 ? data.alertY : 50;
               alertScale = data.alertScale != 0.0F ? data.alertScale : 2.0F;
               alertTime = data.alertTime != 0 ? data.alertTime : 100;
               alertColor = data.alertColor != 0 ? data.alertColor : 16733525;
               aliveOrDeadMode = data.aliveOrDeadMode;
               aliveOrDeadX = data.aliveOrDeadX != 0 ? data.aliveOrDeadX : 50;
               aliveOrDeadY = data.aliveOrDeadY != 0 ? data.aliveOrDeadY : 70;
               aliveOrDeadScale = data.aliveOrDeadScale != 0.0F ? data.aliveOrDeadScale : 2.0F;
               if (data.soundTriggers != null) {
                  soundTriggers = data.soundTriggers;
               }

               soundVolume = data.soundVolume != 0.0F ? data.soundVolume : 1.0F;
               dpsHudEnabled = data.dpsHudEnabled;
               dpsHudX = data.dpsHudX != 0 ? data.dpsHudX : 10;
               dpsHudY = data.dpsHudY != 0 ? data.dpsHudY : 10;
               dpsHudScale = data.dpsHudScale != 0.0F ? data.dpsHudScale : 1.5F;
               dpsHudColor = data.dpsHudColor != 0 ? data.dpsHudColor : 16777215;
               hideDamageNumbers = data.hideDamageNumbers;
               primedLimiter = data.primedLimiter != null ? data.primedLimiter : true;
               primedIntervalMs = data.primedIntervalMs != 0 ? data.primedIntervalMs : 4100;
               debuggerEnabled = data.debuggerEnabled != null ? data.debuggerEnabled : true;
               debuggerKey = data.debuggerKey != 0 ? data.debuggerKey : 72;
               debuggerRadius = data.debuggerRadius != 0 ? data.debuggerRadius : 20;
               debuggerCrosshair = data.debuggerCrosshair;
               debuggerComponents = data.debuggerComponents;
               portalRange = data.portalRange != null ? data.portalRange : 64;
               portalModels = data.portalModels != null ? data.portalModels : "telos:mob/portal";
               boneEsp = data.boneEsp != null ? data.boneEsp : true;
               carrotStickEsp = data.carrotStickEsp != null ? data.carrotStickEsp : true;
               stickEsp = data.stickEsp != null ? data.stickEsp : true;
               armorStandEsp = data.armorStandEsp != null ? data.armorStandEsp : true;
               interactionEsp = data.interactionEsp != null ? data.interactionEsp : true;
               if (data.autoWallsTracer != null) {
                  autoWalls = data.autoWalls != null ? data.autoWalls : true;
                  autoWallsTracer = data.autoWallsTracer;
               } else {
                  autoWalls = true;
                  autoWallsTracer = data.autoWalls == null || data.autoWalls;
               }

               splits = data.splits != null ? data.splits : false;
               splitsShowPhases = data.splitsShowPhases != null ? data.splitsShowPhases : true;
               splitsMaxRows = data.splitsMaxRows != 0 ? data.splitsMaxRows : 8;
               splitsWidth = data.splitsWidth != 0 ? data.splitsWidth : 300;
               splitsX = data.splitsX != 0 ? data.splitsX : 10;
               splitsY = data.splitsY != 0 ? data.splitsY : 130;
               splitsScale = data.splitsScale != 0.0F ? data.splitsScale : 1.0F;
               bossHp = data.bossHp != null ? data.bossHp : true;
               if (data.bossHpLabel != null) {
                  bossHpLabel = data.bossHpLabel;
               }

               bossHpDecimals = data.bossHpDecimals != null ? data.bossHpDecimals : 1;
               bossHpColorByHealth = data.bossHpColorByHealth != null ? data.bossHpColorByHealth : true;
               bossHpColor = data.bossHpColor != 0 ? data.bossHpColor : 16777215;
               bossHpX = data.bossHpX != 0 ? data.bossHpX : 10;
               bossHpY = data.bossHpY != 0 ? data.bossHpY : 100;
               bossHpScale = data.bossHpScale != 0.0F ? data.bossHpScale : 1.5F;
               calls = data.calls != null ? data.calls : false;
               ambushAt = data.ambushAt != 0 ? data.ambushAt : 65;
               if (data.ambushText != null) {
                  ambushText = data.ambushText;
               }

               deathmarkAt = data.deathmarkAt != 0 ? data.deathmarkAt : 40;
               if (data.deathmarkText != null) {
                  deathmarkText = data.deathmarkText;
               }

               callColor = data.callColor != 0 ? data.callColor : 16755200;
               callStayTicks = data.callStayTicks != 0 ? data.callStayTicks : 40;
               callSound = data.callSound != null ? data.callSound : false;
               callX = data.callX != 0 ? data.callX : 10;
               callY = data.callY != 0 ? data.callY : 80;
               callScale = data.callScale != 0.0F ? data.callScale : 2.0F;
               traitDetector = data.traitDetector != null ? data.traitDetector : true;
               traitHud = data.traitHud != null ? data.traitHud : true;
               traitHudX = data.traitHudX != 0 ? data.traitHudX : 10;
               traitHudY = data.traitHudY != 0 ? data.traitHudY : 60;
               traitHudScale = data.traitHudScale != 0.0F ? data.traitHudScale : 1.2F;
               trapCounter = data.trapCounter != null ? data.trapCounter : true;
               trapBlockAtMax = data.trapBlockAtMax != null ? data.trapBlockAtMax : true;
               if (data.trapCounterModels != null) {
                  trapCounterModels = data.trapCounterModels;
               }

               trapCounterMax = data.trapCounterMax != 0 ? data.trapCounterMax : 15;
               trapCounterRange = data.trapCounterRange != 0 ? data.trapCounterRange : 50;
               trapCounterX = data.trapCounterX != 0 ? data.trapCounterX : 10;
               trapCounterY = data.trapCounterY != 0 ? data.trapCounterY : 40;
               trapCounterScale = data.trapCounterScale != 0.0F ? data.trapCounterScale : 1.5F;
               if (data.trapCounterLabel != null) {
                  trapCounterLabel = data.trapCounterLabel;
               }

               trapCounterHideEmpty = data.trapCounterHideEmpty != null ? data.trapCounterHideEmpty : false;
               armorSwapEnabled = data.armorSwapEnabled != null ? data.armorSwapEnabled : true;
               armorSetups = data.armorSetups != null && !data.armorSetups.isEmpty() ? normalizeSetups(data.armorSetups) : migrateSetups(data);
               armorSwapReturn = data.armorSwapReturn != null ? data.armorSwapReturn : true;
               if (data.autoWallsModels != null) {
                  autoWallsModels = data.autoWallsModels.trim().equalsIgnoreCase("ophanim_projectile") ? "ophanim_projectile6" : data.autoWallsModels;
               }

               if (data.autoWallsBossModels != null) {
                  autoWallsBossModels = data.autoWallsBossModels;
               }

               autoWallsBossRange = data.autoWallsBossRange != 0 ? data.autoWallsBossRange : 50;
               autoWallsBossClearance = data.autoWallsBossClearance != 0 ? data.autoWallsBossClearance : 8;
               autoWallsIgnoreCentre = data.autoWallsIgnoreCentre != null ? data.autoWallsIgnoreCentre : true;

               autoWallsTurn = data.autoWallsTurn != null ? data.autoWallsTurn : false;
               autoWallsTurnSpeed = data.autoWallsTurnSpeed != 0 ? data.autoWallsTurnSpeed : 20;

               espRange = data.espRange != 0 ? data.espRange : 48;
            } catch (Throwable var4) {
               try {
                  reader.close();
               } catch (Throwable var3) {
                  var4.addSuppressed(var3);
               }

               throw var4;
            }

            reader.close();
         } catch (IOException var5) {
            System.out.println("Error loading Sapo configurations.");
         }
      } else {
         save();
      }

      updateParsedTriggers();
      extractDefaultSound();
   }

   public static void extractDefaultSound() {
      if (!CONFIG_DIR.exists()) {
         CONFIG_DIR.mkdirs();
      }

      File soundFile = new File(CONFIG_DIR, "sapo_alerta.wav");
      if (!soundFile.exists()) {
         try {
            InputStream in = Config.class.getResourceAsStream("/sapo_alerta.wav");

            try {
               if (in != null) {
                  Files.copy(in, soundFile.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                  System.out.println("[FavelaClient] Default sapo_alerta.wav file extracted successfully.");
               } else {
                  System.out.println("[FavelaClient] Warning: sapo_alerta.wav not found inside mod resources.");
               }
            } catch (Throwable var5) {
               if (in != null) {
                  try {
                     in.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (in != null) {
               in.close();
            }
         } catch (IOException e) {
            System.out.println("[FavelaClient] Error extracting default sapo_alerta.wav: " + e.getMessage());
         }
      }

   }

   public static void save() {
      updateParsedTriggers();
      if (!CONFIG_DIR.exists()) {
         CONFIG_DIR.mkdirs();
      }

      try {
         OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8);

         try {
            SapoData data = new SapoData(active, minCroaks, maxCroaks, devMode, triggerText, alertText, alertX, alertY, alertScale, alertTime, alertColor, aliveOrDeadMode, aliveOrDeadX, aliveOrDeadY, aliveOrDeadScale, soundTriggers, soundVolume, dpsHudEnabled, dpsHudX, dpsHudY, dpsHudScale, dpsHudColor, hideDamageNumbers, primedLimiter, primedIntervalMs, debuggerEnabled, debuggerKey, debuggerRadius, debuggerCrosshair, debuggerComponents, boneEsp, carrotStickEsp, stickEsp, armorStandEsp, interactionEsp, autoWalls, autoWallsTracer, autoWallsModels, autoWallsBossModels, autoWallsBossRange, autoWallsBossClearance, autoWallsIgnoreCentre, autoWallsTurn, autoWallsTurnSpeed, armorSwapEnabled, armorSetups, armorSwapReturn, splits, splitsShowPhases, splitsMaxRows, splitsWidth, splitsX, splitsY, splitsScale, bossHp, bossHpLabel, bossHpDecimals, bossHpColorByHealth, bossHpColor, bossHpX, bossHpY, bossHpScale, calls, ambushAt, ambushText, deathmarkAt, deathmarkText, callColor, callStayTicks, callSound, callX, callY, callScale, traitDetector, traitHud, traitHudX, traitHudY, traitHudScale, trapCounter, trapBlockAtMax, trapCounterModels, trapCounterMax, trapCounterRange, trapCounterX, trapCounterY, trapCounterScale, trapCounterLabel, trapCounterHideEmpty, espRange, portalRange, portalModels);
            GSON.toJson(data, writer);
         } catch (Throwable var4) {
            try {
               writer.close();
            } catch (Throwable var3) {
               var4.addSuppressed(var3);
            }

            throw var4;
         }

         writer.close();
      } catch (IOException var5) {
         System.out.println("Error saving Sapo configurations.");
      }

   }

   static {
      FILE = new File(CONFIG_DIR, "sapo_config.json");
   }

   private static class SapoData {
      @SerializedName("ativo")
      boolean active;
      @SerializedName("minCroac")
      int minCroaks;
      @SerializedName("maxCroac")
      int maxCroaks;
      @SerializedName("modoDev")
      boolean devMode;
      @SerializedName("textoGatilho")
      String triggerText;
      @SerializedName("textoAlerta")
      String alertText;
      @SerializedName("alertaX")
      int alertX;
      @SerializedName("alertaY")
      int alertY;
      @SerializedName("alertaEscala")
      float alertScale;
      @SerializedName("alertaTempo")
      int alertTime;
      @SerializedName("alertaCor")
      int alertColor;
      @SerializedName("modoVivoOuMorto")
      boolean aliveOrDeadMode;
      @SerializedName("vivoMortoX")
      int aliveOrDeadX;
      @SerializedName("vivoMortoY")
      int aliveOrDeadY;
      @SerializedName("vivoMortoEscala")
      float aliveOrDeadScale;
      @SerializedName("somGatilhos")
      String soundTriggers;
      @SerializedName("somVolume")
      float soundVolume;
      @SerializedName("dpsHudAtivo")
      boolean dpsHudEnabled;
      @SerializedName("dpsHudX")
      int dpsHudX;
      @SerializedName("dpsHudY")
      int dpsHudY;
      @SerializedName("dpsHudEscala")
      float dpsHudScale;
      @SerializedName("dpsHudCor")
      int dpsHudColor;
      @SerializedName("ocultarDanoTexto")
      boolean hideDamageNumbers;
      @SerializedName("limitadorPrimed")
      Boolean primedLimiter;
      @SerializedName("primedIntervalo")
      int primedIntervalMs;
      @SerializedName("debugAtivo")
      Boolean debuggerEnabled;
      @SerializedName("debugTecla")
      int debuggerKey;
      @SerializedName("debugComponentes")
      boolean debuggerComponents;
      @SerializedName("debugMira")
      boolean debuggerCrosshair;
      @SerializedName("espOssoAtivo")
      Boolean boneEsp;
      @SerializedName("espCenouraAtivo")
      Boolean carrotStickEsp;
      @SerializedName("espGravetoAtivo")
      Boolean stickEsp;
      @SerializedName("espArmorStandAtivo")
      Boolean armorStandEsp;
      @SerializedName("espInteracaoAtivo")
      Boolean interactionEsp;
      @SerializedName("autoWallsGiroAtivo")
      Boolean autoWallsTurn;
      @SerializedName("autoWallsGiroVelocidade")
      int autoWallsTurnSpeed;
      @SerializedName("autoWallsBossModelos")
      String autoWallsBossModels;
      @SerializedName("autoWallsBossDistanciaMin")
      int autoWallsBossClearance;
      @SerializedName("autoWallsIgnorarCentro")
      Boolean autoWallsIgnoreCentre;
      @SerializedName("autoWallsBossAlcance")
      int autoWallsBossRange;
      @SerializedName("autoWallsTracer")
      Boolean autoWallsTracer;
      @SerializedName("autoWallsModelos")
      String autoWallsModels;
      @SerializedName("autoWalls")
      Boolean autoWalls;
      @SerializedName("splitsAtivo")
      Boolean splits;
      @SerializedName("splitsFases")
      Boolean splitsShowPhases;
      @SerializedName("splitsMaxLinhas")
      int splitsMaxRows;
      @SerializedName("splitsLargura")
      int splitsWidth;
      @SerializedName("splitsX")
      int splitsX;
      @SerializedName("splitsY")
      int splitsY;
      @SerializedName("splitsEscala")
      float splitsScale;
      @SerializedName("bossHpAtivo")
      Boolean bossHp;
      @SerializedName("bossHpRotulo")
      String bossHpLabel;
      @SerializedName("bossHpCasas")
      Integer bossHpDecimals;
      @SerializedName("bossHpCorPorVida")
      Boolean bossHpColorByHealth;
      @SerializedName("bossHpCor")
      int bossHpColor;
      @SerializedName("bossHpX")
      int bossHpX;
      @SerializedName("bossHpY")
      int bossHpY;
      @SerializedName("bossHpEscala")
      float bossHpScale;
      @SerializedName("chamadas")
      Boolean calls;
      @SerializedName("ambushEm")
      int ambushAt;
      @SerializedName("ambushTexto")
      String ambushText;
      @SerializedName("deathmarkEm")
      int deathmarkAt;
      @SerializedName("deathmarkTexto")
      String deathmarkText;
      @SerializedName("chamadaCor")
      int callColor;
      @SerializedName("chamadaTicks")
      int callStayTicks;
      @SerializedName("chamadaSom")
      Boolean callSound;
      @SerializedName("chamadaX")
      int callX;
      @SerializedName("chamadaY")
      int callY;
      @SerializedName("chamadaEscala")
      float callScale;
      @SerializedName("traitDetector")
      Boolean traitDetector;
      @SerializedName("traitHud")
      Boolean traitHud;
      @SerializedName("traitHudX")
      int traitHudX;
      @SerializedName("traitHudY")
      int traitHudY;
      @SerializedName("traitHudEscala")
      float traitHudScale;
      @SerializedName("trapContador")
      Boolean trapCounter;
      @SerializedName("trapBloquearNoMaximo")
      Boolean trapBlockAtMax;
      @SerializedName("trapModelos")
      String trapCounterModels;
      @SerializedName("trapMaximo")
      int trapCounterMax;
      @SerializedName("trapAlcance")
      int trapCounterRange;
      @SerializedName("trapX")
      int trapCounterX;
      @SerializedName("trapY")
      int trapCounterY;
      @SerializedName("trapEscala")
      float trapCounterScale;
      @SerializedName("trapRotulo")
      String trapCounterLabel;
      @SerializedName("trapOcultarVazio")
      Boolean trapCounterHideEmpty;
      @SerializedName("trocaArmaduraAtivo")
      Boolean armorSwapEnabled;
      @SerializedName("trocaArmaduraSetups")
      List<ArmorSetup> armorSetups;
      @SerializedName("trocaArmaduraTecla")
      int armorSwapKey;
      @SerializedName("trocaArmaduraSlot1")
      Integer armorSwapSlot1;
      @SerializedName("trocaArmaduraSlot2")
      Integer armorSwapSlot2;
      @SerializedName("trocaArmaduraSlot3")
      Integer armorSwapSlot3;
      @SerializedName("trocaArmaduraSlot4")
      Integer armorSwapSlot4;
      @SerializedName("trocaArmaduraDelay")
      int armorSwapDelay;
      @SerializedName("trocaArmaduraVoltar")
      Boolean armorSwapReturn;
      @SerializedName("espAlcance")
      int espRange;
      @SerializedName("debugRaio")
      int debuggerRadius;
      @SerializedName("portalAlcance")
      Integer portalRange;
      @SerializedName("portalModelos")
      String portalModels;

      SapoData(boolean active, int minCroaks, int maxCroaks, boolean devMode, String triggerText, String alertText, int alertX, int alertY, float alertScale, int alertTime, int alertColor, boolean aliveOrDeadMode, int aliveOrDeadX, int aliveOrDeadY, float aliveOrDeadScale, String soundTriggers, float soundVolume, boolean dpsHudEnabled, int dpsHudX, int dpsHudY, float dpsHudScale, int dpsHudColor, boolean hideDamageNumbers, boolean primedLimiter, int primedIntervalMs, boolean debuggerEnabled, int debuggerKey, int debuggerRadius, boolean debuggerCrosshair, boolean debuggerComponents, boolean boneEsp, boolean carrotStickEsp, boolean stickEsp, boolean armorStandEsp, boolean interactionEsp, boolean autoWalls, boolean autoWallsTracer, String autoWallsModels, String autoWallsBossModels, int autoWallsBossRange, int autoWallsBossClearance, boolean autoWallsIgnoreCentre, boolean autoWallsTurn, int autoWallsTurnSpeed, boolean armorSwapEnabled, List<ArmorSetup> armorSetups, boolean armorSwapReturn, boolean splits, boolean splitsShowPhases, int splitsMaxRows, int splitsWidth, int splitsX, int splitsY, float splitsScale, boolean bossHp, String bossHpLabel, int bossHpDecimals, boolean bossHpColorByHealth, int bossHpColor, int bossHpX, int bossHpY, float bossHpScale, boolean calls, int ambushAt, String ambushText, int deathmarkAt, String deathmarkText, int callColor, int callStayTicks, boolean callSound, int callX, int callY, float callScale, boolean traitDetector, boolean traitHud, int traitHudX, int traitHudY, float traitHudScale, boolean trapCounter, boolean trapBlockAtMax, String trapCounterModels, int trapCounterMax, int trapCounterRange, int trapCounterX, int trapCounterY, float trapCounterScale, String trapCounterLabel, boolean trapCounterHideEmpty, int espRange, int portalRange, String portalModels) {
         this.active = active;
         this.minCroaks = minCroaks;
         this.maxCroaks = maxCroaks;
         this.devMode = devMode;
         this.triggerText = triggerText;
         this.alertText = alertText;
         this.alertX = alertX;
         this.alertY = alertY;
         this.alertScale = alertScale;
         this.alertTime = alertTime;
         this.alertColor = alertColor;
         this.aliveOrDeadMode = aliveOrDeadMode;
         this.aliveOrDeadX = aliveOrDeadX;
         this.aliveOrDeadY = aliveOrDeadY;
         this.aliveOrDeadScale = aliveOrDeadScale;
         this.soundTriggers = soundTriggers;
         this.soundVolume = soundVolume;
         this.dpsHudEnabled = dpsHudEnabled;
         this.dpsHudX = dpsHudX;
         this.dpsHudY = dpsHudY;
         this.dpsHudScale = dpsHudScale;
         this.dpsHudColor = dpsHudColor;
         this.hideDamageNumbers = hideDamageNumbers;
         this.primedLimiter = primedLimiter;
         this.primedIntervalMs = primedIntervalMs;
         this.debuggerEnabled = debuggerEnabled;
         this.debuggerKey = debuggerKey;
         this.debuggerRadius = debuggerRadius;
         this.debuggerCrosshair = debuggerCrosshair;
         this.debuggerComponents = debuggerComponents;
         this.portalRange = portalRange;
         this.portalModels = portalModels;
         this.boneEsp = boneEsp;
         this.carrotStickEsp = carrotStickEsp;
         this.stickEsp = stickEsp;
         this.armorStandEsp = armorStandEsp;
         this.interactionEsp = interactionEsp;
         this.autoWalls = autoWalls;
         this.autoWallsTracer = autoWallsTracer;
         this.autoWallsModels = autoWallsModels;
         this.autoWallsBossModels = autoWallsBossModels;
         this.autoWallsBossRange = autoWallsBossRange;
         this.autoWallsBossClearance = autoWallsBossClearance;
         this.autoWallsIgnoreCentre = autoWallsIgnoreCentre;
         this.autoWallsTurn = autoWallsTurn;
         this.autoWallsTurnSpeed = autoWallsTurnSpeed;
         this.armorSwapEnabled = armorSwapEnabled;
         this.armorSetups = armorSetups;
         this.armorSwapReturn = armorSwapReturn;
         this.splits = splits;
         this.splitsShowPhases = splitsShowPhases;
         this.splitsMaxRows = splitsMaxRows;
         this.splitsWidth = splitsWidth;
         this.splitsX = splitsX;
         this.splitsY = splitsY;
         this.splitsScale = splitsScale;
         this.bossHp = bossHp;
         this.bossHpLabel = bossHpLabel;
         this.bossHpDecimals = bossHpDecimals;
         this.bossHpColorByHealth = bossHpColorByHealth;
         this.bossHpColor = bossHpColor;
         this.bossHpX = bossHpX;
         this.bossHpY = bossHpY;
         this.bossHpScale = bossHpScale;
         this.calls = calls;
         this.ambushAt = ambushAt;
         this.ambushText = ambushText;
         this.deathmarkAt = deathmarkAt;
         this.deathmarkText = deathmarkText;
         this.callColor = callColor;
         this.callStayTicks = callStayTicks;
         this.callSound = callSound;
         this.callX = callX;
         this.callY = callY;
         this.callScale = callScale;
         this.traitDetector = traitDetector;
         this.traitHud = traitHud;
         this.traitHudX = traitHudX;
         this.traitHudY = traitHudY;
         this.traitHudScale = traitHudScale;
         this.trapCounter = trapCounter;
         this.trapBlockAtMax = trapBlockAtMax;
         this.trapCounterModels = trapCounterModels;
         this.trapCounterMax = trapCounterMax;
         this.trapCounterRange = trapCounterRange;
         this.trapCounterX = trapCounterX;
         this.trapCounterY = trapCounterY;
         this.trapCounterScale = trapCounterScale;
         this.trapCounterLabel = trapCounterLabel;
         this.trapCounterHideEmpty = trapCounterHideEmpty;
         this.espRange = espRange;
      }
   }
}
