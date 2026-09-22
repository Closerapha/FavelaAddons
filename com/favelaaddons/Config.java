package com.favelaaddons;

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
   public static boolean primedTimer = false;
   public static int primedTimerMs = 4100;
   public static String primedTimerLabel = "Primed ";
   public static int primedTimerX = 10;
   public static int primedTimerY = 120;
   public static float primedTimerScale = 1.5F;
   public static int portalRange = 64;
   public static String portalModels = "telos:mob/portal";
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
   public static int ambushAt = 60;
   public static String ambushText = "AMBUSH";
   public static int deathmarkAt = 40;
   public static String deathmarkText = "DEATHMARK";
   public static int ambushColor = 16755200;
   public static int deathmarkColor = 16733525;
   public static int callStayTicks = 40;
   public static boolean callSound = false;
   public static int callX = 10;
   public static int callY = 80;
   public static float callScale = 2.0F;
   public static boolean trapCounter = true;
   public static String trapCounterModels = "trap_n6b";
   public static String[] parsedTrapCounterModels = new String[0];
   public static int trapCounterMax = 15;
   public static int trapCounterRange = 50;
   public static int trapCounterX = 10;
   public static int trapCounterY = 40;
   public static float trapCounterScale = 1.5F;
   public static String trapCounterLabel = "Trap:";
   public static boolean trapCounterHideEmpty = false;
   public static boolean monolith = true;
   public static boolean monolithRing = true;
   public static boolean monolithHud = true;
   public static String monolithModels = "arcanist_orb";
   public static String[] parsedMonolithModels = new String[0];
   public static int monolithRadius = 6;
   public static int monolithColor = 10131967;
   public static int monolithX = 10;
   public static int monolithY = 120;
   public static float monolithScale = 1.2F;
   public static boolean monolithTempered = false;
   public static boolean vulnHud = false;
   public static String vulnResistantText = "RESISTANT";
   public static String vulnInvulnerableText = "INVULNERABLE";
   public static int vulnResistantColor = 5627135;
   public static int vulnInvulnerableColor = 11184810;
   public static int vulnX = 10;
   public static int vulnY = 150;
   public static float vulnScale = 2.0F;
   public static String[] parsedPortalModels = new String[0];
   public static String[] parsedSoundTriggers = new String[0];
   public static String[] parsedTextTriggers = new String[0];
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final File CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons");
   private static final File FILE;



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

      if (portalModels != null && !portalModels.isEmpty()) {
         parsedPortalModels = portalModels.split(",");

         for(int i = 0; i < parsedPortalModels.length; ++i) {
            parsedPortalModels[i] = parsedPortalModels[i].trim();
         }
      } else {
         parsedPortalModels = new String[0];
      }

   }

   public static void load() {
      if (FILE.exists()) {
         try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8);

            try {
               FavelaData data = (FavelaData)GSON.fromJson(reader, FavelaData.class);
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
               primedTimer = data.primedTimer != null ? data.primedTimer : false;
               primedTimerMs = data.primedTimerMs != 0 ? data.primedTimerMs : 4100;
               primedTimerLabel = data.primedTimerLabel != null ? data.primedTimerLabel : "Primed ";
               primedTimerX = data.primedTimerX != 0 ? data.primedTimerX : 10;
               primedTimerY = data.primedTimerY != 0 ? data.primedTimerY : 120;
               primedTimerScale = data.primedTimerScale != 0.0F ? data.primedTimerScale : 1.5F;
               portalRange = data.portalRange != null ? data.portalRange : 64;
               portalModels = data.portalModels != null ? data.portalModels : "telos:mob/portal";
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
               ambushAt = data.ambushAt != 0 ? data.ambushAt : 60;
               if (data.ambushText != null) {
                  ambushText = data.ambushText;
               }

               deathmarkAt = data.deathmarkAt != 0 ? data.deathmarkAt : 40;
               if (data.deathmarkText != null) {
                  deathmarkText = data.deathmarkText;
               }

               ambushColor = data.ambushColor != 0 ? data.ambushColor : 16755200;
               deathmarkColor = data.deathmarkColor != 0 ? data.deathmarkColor : 16733525;
               callStayTicks = data.callStayTicks != 0 ? data.callStayTicks : 40;
               callSound = data.callSound != null ? data.callSound : false;
               callX = data.callX != 0 ? data.callX : 10;
               callY = data.callY != 0 ? data.callY : 80;
               callScale = data.callScale != 0.0F ? data.callScale : 2.0F;
               trapCounter = data.trapCounter != null ? data.trapCounter : true;
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
               monolith = data.monolith != null ? data.monolith : true;
               monolithRing = data.monolithRing != null ? data.monolithRing : true;
               monolithHud = data.monolithHud != null ? data.monolithHud : true;
               if (data.monolithModels != null) {
                  monolithModels = data.monolithModels;
               }

               monolithRadius = data.monolithRadius != 0 ? data.monolithRadius : 6;
               monolithColor = data.monolithColor != 0 ? data.monolithColor : 10131967;
               monolithX = data.monolithX != 0 ? data.monolithX : 10;
               monolithY = data.monolithY != 0 ? data.monolithY : 120;
               monolithScale = data.monolithScale != 0.0F ? data.monolithScale : 1.2F;
               monolithTempered = data.monolithTempered != null ? data.monolithTempered : false;
               vulnHud = data.vulnHud != null ? data.vulnHud : false;
               vulnResistantText = data.vulnResistantText != null ? data.vulnResistantText : "RESISTANT";
               vulnInvulnerableText = data.vulnInvulnerableText != null ? data.vulnInvulnerableText : "INVULNERABLE";
               vulnResistantColor = data.vulnResistantColor != 0 ? data.vulnResistantColor : 5627135;
               vulnInvulnerableColor = data.vulnInvulnerableColor != 0 ? data.vulnInvulnerableColor : 11184810;
               vulnX = data.vulnX != 0 ? data.vulnX : 10;
               vulnY = data.vulnY != 0 ? data.vulnY : 150;
               vulnScale = data.vulnScale != 0.0F ? data.vulnScale : 2.0F;


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
            System.out.println("Error loading FavelaAddons configurations.");
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

      File soundFile = new File(CONFIG_DIR, "favela_alerta.wav");
      if (!soundFile.exists()) {
         try {
            InputStream in = Config.class.getResourceAsStream("/favela_alerta.wav");

            try {
               if (in != null) {
                  Files.copy(in, soundFile.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
               } else {
                  System.out.println("[FavelaAddons] Warning: favela_alerta.wav not found inside mod resources.");
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
            System.out.println("[FavelaAddons] Error extracting default favela_alerta.wav: " + e.getMessage());
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
            FavelaData data = new FavelaData(triggerText, alertText, alertX, alertY, alertScale, alertTime, alertColor, aliveOrDeadMode, aliveOrDeadX, aliveOrDeadY, aliveOrDeadScale, soundTriggers, soundVolume, dpsHudEnabled, dpsHudX, dpsHudY, dpsHudScale, dpsHudColor, hideDamageNumbers, primedTimer, primedTimerMs, primedTimerLabel, primedTimerX, primedTimerY, primedTimerScale, vulnHud, vulnResistantText, vulnInvulnerableText, vulnResistantColor, vulnInvulnerableColor, vulnX, vulnY, vulnScale, splits, splitsShowPhases, splitsMaxRows, splitsWidth, splitsX, splitsY, splitsScale, bossHp, bossHpLabel, bossHpDecimals, bossHpColorByHealth, bossHpColor, bossHpX, bossHpY, bossHpScale, calls, ambushAt, ambushText, deathmarkAt, deathmarkText, ambushColor, deathmarkColor, callStayTicks, callSound, callX, callY, callScale, trapCounter, trapCounterModels, trapCounterMax, trapCounterRange, trapCounterX, trapCounterY, trapCounterScale, trapCounterLabel, trapCounterHideEmpty, monolith, monolithRing, monolithHud, monolithModels, monolithRadius, monolithColor, monolithX, monolithY, monolithScale, monolithTempered, portalRange, portalModels);
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
         System.out.println("Error saving FavelaAddons configurations.");
      }

   }

   static {
      FILE = new File(CONFIG_DIR, "favelaaddons.json");
   }

   private static class FavelaData {
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
      @SerializedName("primedTimer")
      Boolean primedTimer;
      @SerializedName("primedTimerMs")
      int primedTimerMs;
      @SerializedName("primedTimerRotulo")
      String primedTimerLabel;
      @SerializedName("primedTimerX")
      int primedTimerX;
      @SerializedName("primedTimerY")
      int primedTimerY;
      @SerializedName("primedTimerEscala")
      float primedTimerScale;
      @SerializedName("vulnHud")
      Boolean vulnHud;
      @SerializedName("vulnResistenteTexto")
      String vulnResistantText;
      @SerializedName("vulnInvulneravelTexto")
      String vulnInvulnerableText;
      @SerializedName("vulnResistenteCor")
      int vulnResistantColor;
      @SerializedName("vulnInvulneravelCor")
      int vulnInvulnerableColor;
      @SerializedName("vulnX")
      int vulnX;
      @SerializedName("vulnY")
      int vulnY;
      @SerializedName("vulnEscala")
      float vulnScale;
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
      @SerializedName("ambushCor")
      int ambushColor;
      @SerializedName("deathmarkCor")
      int deathmarkColor;
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
      @SerializedName("trapContador")
      Boolean trapCounter;
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
      @SerializedName("monolito")
      Boolean monolith;
      @SerializedName("monolitoAnel")
      Boolean monolithRing;
      @SerializedName("monolitoHud")
      Boolean monolithHud;
      @SerializedName("monolitoModelos")
      String monolithModels;
      @SerializedName("monolitoRaio")
      int monolithRadius;
      @SerializedName("monolitoCor")
      int monolithColor;
      @SerializedName("monolitoX")
      int monolithX;
      @SerializedName("monolitoY")
      int monolithY;
      @SerializedName("monolitoEscala")
      float monolithScale;
      @SerializedName("monolitoTemperado")
      Boolean monolithTempered;
      @SerializedName("portalAlcance")
      Integer portalRange;
      @SerializedName("portalModelos")
      String portalModels;

      FavelaData(String triggerText, String alertText, int alertX, int alertY, float alertScale, int alertTime, int alertColor, boolean aliveOrDeadMode, int aliveOrDeadX, int aliveOrDeadY, float aliveOrDeadScale, String soundTriggers, float soundVolume, boolean dpsHudEnabled, int dpsHudX, int dpsHudY, float dpsHudScale, int dpsHudColor, boolean hideDamageNumbers, boolean primedTimer, int primedTimerMs, String primedTimerLabel, int primedTimerX, int primedTimerY, float primedTimerScale, boolean vulnHud, String vulnResistantText, String vulnInvulnerableText, int vulnResistantColor, int vulnInvulnerableColor, int vulnX, int vulnY, float vulnScale, boolean splits, boolean splitsShowPhases, int splitsMaxRows, int splitsWidth, int splitsX, int splitsY, float splitsScale, boolean bossHp, String bossHpLabel, int bossHpDecimals, boolean bossHpColorByHealth, int bossHpColor, int bossHpX, int bossHpY, float bossHpScale, boolean calls, int ambushAt, String ambushText, int deathmarkAt, String deathmarkText, int ambushColor, int deathmarkColor, int callStayTicks, boolean callSound, int callX, int callY, float callScale, boolean trapCounter, String trapCounterModels, int trapCounterMax, int trapCounterRange, int trapCounterX, int trapCounterY, float trapCounterScale, String trapCounterLabel, boolean trapCounterHideEmpty, boolean monolith, boolean monolithRing, boolean monolithHud, String monolithModels, int monolithRadius, int monolithColor, int monolithX, int monolithY, float monolithScale, boolean monolithTempered, int portalRange, String portalModels) {
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
         this.primedTimer = primedTimer;
         this.primedTimerMs = primedTimerMs;
         this.primedTimerLabel = primedTimerLabel;
         this.primedTimerX = primedTimerX;
         this.primedTimerY = primedTimerY;
         this.primedTimerScale = primedTimerScale;
         this.portalRange = portalRange;
         this.portalModels = portalModels;
         this.vulnHud = vulnHud;
         this.vulnResistantText = vulnResistantText;
         this.vulnInvulnerableText = vulnInvulnerableText;
         this.vulnResistantColor = vulnResistantColor;
         this.vulnInvulnerableColor = vulnInvulnerableColor;
         this.vulnX = vulnX;
         this.vulnY = vulnY;
         this.vulnScale = vulnScale;
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
         this.ambushColor = ambushColor;
         this.deathmarkColor = deathmarkColor;
         this.callStayTicks = callStayTicks;
         this.callSound = callSound;
         this.callX = callX;
         this.callY = callY;
         this.callScale = callScale;
         this.trapCounter = trapCounter;
         this.trapCounterModels = trapCounterModels;
         this.trapCounterMax = trapCounterMax;
         this.trapCounterRange = trapCounterRange;
         this.trapCounterX = trapCounterX;
         this.trapCounterY = trapCounterY;
         this.trapCounterScale = trapCounterScale;
         this.trapCounterLabel = trapCounterLabel;
         this.trapCounterHideEmpty = trapCounterHideEmpty;
         this.monolith = monolith;
         this.monolithRing = monolithRing;
         this.monolithHud = monolithHud;
         this.monolithModels = monolithModels;
         this.monolithRadius = monolithRadius;
         this.monolithColor = monolithColor;
         this.monolithX = monolithX;
         this.monolithY = monolithY;
         this.monolithScale = monolithScale;
         this.monolithTempered = monolithTempered;
      }
   }
}
