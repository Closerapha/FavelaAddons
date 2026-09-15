package com.favelaaddons;

import java.util.Objects;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class AlertHudEditorScreen extends Screen {
   private boolean isDraggingAlert = false;
   private boolean isDraggingAliveOrDead = false;
   private boolean isDraggingDps = false;
   private boolean isDraggingTrap = false;
   private boolean isDraggingPrimed = false;
   private boolean isDraggingCall = false;
   private boolean isDraggingBossHp = false;
   private boolean isDraggingSplits = false;
   private double dragOffsetX;
   private double dragOffsetY;

   public AlertHudEditorScreen(Component title) {
      super(title);
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      super.extractRenderState(graphics, mouseX, mouseY, delta);
      graphics.fill(0, 0, this.width, this.height, -2013265920);
      graphics.text(this.font, "Drag the texts to move them. Use + and - to change the size of the text under the mouse.", 10, 10, -1, true);
      graphics.text(this.font, "Press ESC to save and exit. Texts will automatically snap to the center when dragged near it.", 10, 25, -5592406, true);
      if (this.isDraggingAlert || this.isDraggingAliveOrDead || this.isDraggingDps || this.isDraggingTrap || this.isDraggingPrimed || this.isDraggingCall || this.isDraggingBossHp || this.isDraggingSplits) {
         graphics.fill(this.width / 2, 0, this.width / 2 + 1, this.height, 1157627903);
      }

      int alertWidth = this.font.width(Config.alertText);
      Objects.requireNonNull(this.font);
      int alertHeight = 9;
      graphics.pose().pushMatrix();
      graphics.pose().translate((float)Config.alertX, (float)Config.alertY);
      graphics.pose().scale(Config.alertScale, Config.alertScale);
      graphics.text(this.font, Config.alertText, 0, 0, Config.alertColor | -16777216, true);
      if (this.isMouseOver((double)mouseX, (double)mouseY, Config.alertX, Config.alertY, alertWidth, alertHeight, Config.alertScale)) {
         graphics.fill(-2, -2, alertWidth + 2, alertHeight + 2, 1157627903);
      }

      graphics.pose().popMatrix();
      if (Config.aliveOrDeadMode) {
         String textVm = "CROUCH!";
         int vmWidth = this.font.width(textVm);
         Objects.requireNonNull(this.font);
         int vmHeight = 9;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.aliveOrDeadX, (float)Config.aliveOrDeadY);
         graphics.pose().scale(Config.aliveOrDeadScale, Config.aliveOrDeadScale);
         graphics.text(this.font, textVm, 0, 0, -11141291, true);
         if (this.isMouseOver((double)mouseX, (double)mouseY, Config.aliveOrDeadX, Config.aliveOrDeadY, vmWidth, vmHeight, Config.aliveOrDeadScale)) {
            graphics.fill(-2, -2, vmWidth + 2, vmHeight + 2, 1157627903);
         }

         graphics.pose().popMatrix();
      }

      if (Config.dpsHudEnabled) {
         String dpsText = "DPS: 125.0";
         int dpsWidth = this.font.width(dpsText);
         Objects.requireNonNull(this.font);
         int dpsHeight = 9;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.dpsHudX, (float)Config.dpsHudY);
         graphics.pose().scale(Config.dpsHudScale, Config.dpsHudScale);
         graphics.text(this.font, dpsText, 0, 0, -11141291, true);
         if (this.isMouseOver((double)mouseX, (double)mouseY, Config.dpsHudX, Config.dpsHudY, dpsWidth, dpsHeight, Config.dpsHudScale)) {
            graphics.fill(-2, -2, dpsWidth + 2, dpsHeight + 2, 1157627903);
         }

         graphics.pose().popMatrix();
      }
      if (Config.primedTimer) {
         String primedText = this.primedPreview();
         int primedWidth = this.font.width(primedText);
         Objects.requireNonNull(this.font);
         int primedHeight = 9;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.primedTimerX, (float)Config.primedTimerY);
         graphics.pose().scale(Config.primedTimerScale, Config.primedTimerScale);
         graphics.text(this.font, primedText, 0, 0, -1551496, true);
         if (this.isMouseOver((double)mouseX, (double)mouseY, Config.primedTimerX, Config.primedTimerY, primedWidth, primedHeight, Config.primedTimerScale)) {
            graphics.fill(-2, -2, primedWidth + 2, primedHeight + 2, 1157627903);
         }

         graphics.pose().popMatrix();
      }


      if (Config.trapCounter) {
         String trapText = this.trapPreview();
         int trapWidth = this.font.width(trapText);
         Objects.requireNonNull(this.font);
         int trapHeight = 9;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.trapCounterX, (float)Config.trapCounterY);
         graphics.pose().scale(Config.trapCounterScale, Config.trapCounterScale);
         graphics.text(this.font, trapText, 0, 0, -11141291, true);
         if (this.isMouseOver((double)mouseX, (double)mouseY, Config.trapCounterX, Config.trapCounterY, trapWidth, trapHeight, Config.trapCounterScale)) {
            graphics.fill(-2, -2, trapWidth + 2, trapHeight + 2, 1157627903);
         }

         graphics.pose().popMatrix();
      }


      if (Config.calls) {
         String callText = this.callPreview();
         int callWidth = this.font.width(callText);
         Objects.requireNonNull(this.font);
         int callHeight = 9;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.callX, (float)Config.callY);
         graphics.pose().scale(Config.callScale, Config.callScale);
         graphics.text(this.font, callText, 0, 0, Config.callColor | -16777216, true);
         if (this.isMouseOver((double)mouseX, (double)mouseY, Config.callX, Config.callY, callWidth, callHeight, Config.callScale)) {
            graphics.fill(-2, -2, callWidth + 2, callHeight + 2, 1157627903);
         }

         graphics.pose().popMatrix();
      }

      if (Config.bossHp) {
         String bossText = FavelaBossHp.format(0.853F);
         int bossWidth = this.font.width(bossText);
         Objects.requireNonNull(this.font);
         int bossHeight = 9;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.bossHpX, (float)Config.bossHpY);
         graphics.pose().scale(Config.bossHpScale, Config.bossHpScale);
         graphics.text(this.font, bossText, 0, 0, FavelaBossHp.colorFor(0.853F) | -16777216, true);
         if (this.isMouseOver((double)mouseX, (double)mouseY, Config.bossHpX, Config.bossHpY, bossWidth, bossHeight, Config.bossHpScale)) {
            graphics.fill(-2, -2, bossWidth + 2, bossHeight + 2, 1157627903);
         }

         graphics.pose().popMatrix();
      }

      if (Config.splits) {
         String splitName = "Ophanim";
         String splitTime = "1:39.52";
         int splitWidth = Math.max(Config.splitsWidth, this.font.width(splitName) + this.font.width(splitTime) + 10);
         Objects.requireNonNull(this.font);
         int splitHeight = 20;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.splitsX, (float)Config.splitsY);
         graphics.pose().scale(Config.splitsScale, Config.splitsScale);
         graphics.text(this.font, splitName, 0, 0, -171, true);
         graphics.text(this.font, splitTime, Config.splitsWidth - this.font.width(splitTime), 0, -171, true);
         graphics.text(this.font, "  WALLS", 0, 10, -5592406, true);
         graphics.text(this.font, "12.44", Config.splitsWidth - this.font.width("12.44"), 10, -5592406, true);
         if (this.isMouseOver((double)mouseX, (double)mouseY, Config.splitsX, Config.splitsY, splitWidth, splitHeight, Config.splitsScale)) {
            graphics.fill(-2, -2, splitWidth + 2, splitHeight + 2, 1157627903);
         }

         graphics.pose().popMatrix();
      }

   }

   private String callPreview() {
      String text = Config.ambushText;
      return text != null && !text.trim().isEmpty() ? text.trim() : "AMBUSH";
   }

   private String primedPreview() {
      return Config.primedTimerLabel + "4.1";
   }

   private String trapPreview() {
      return Config.trapCounterLabel + " 7";
   }

   private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height, float scale) {
      double scaledWidth = (double)((float)width * scale);
      double scaledHeight = (double)((float)height * scale);
      return mouseX >= (double)x && mouseX <= (double)x + scaledWidth && mouseY >= (double)y && mouseY <= (double)y + scaledHeight;
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
      double mouseX = event.x();
      double mouseY = event.y();
      int button = event.button();
      if (button == 0) {
         if (Config.aliveOrDeadMode) {
            int var10003 = Config.aliveOrDeadX;
            int var10004 = Config.aliveOrDeadY;
            int var10005 = this.font.width("CROUCH!");
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, var10003, var10004, var10005, 9, Config.aliveOrDeadScale)) {
               this.isDraggingAliveOrDead = true;
               this.dragOffsetX = mouseX - (double)Config.aliveOrDeadX;
               this.dragOffsetY = mouseY - (double)Config.aliveOrDeadY;
               return true;
            }
         }

         if (Config.splits) {
            int splitW = Math.max(Config.splitsWidth, 80);
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, Config.splitsX, Config.splitsY, splitW, 20, Config.splitsScale)) {
               this.isDraggingSplits = true;
               this.dragOffsetX = mouseX - (double)Config.splitsX;
               this.dragOffsetY = mouseY - (double)Config.splitsY;
               return true;
            }
         }

         if (Config.bossHp) {
            int bossX = Config.bossHpX;
            int bossY = Config.bossHpY;
            int bossW = this.font.width(FavelaBossHp.format(0.853F));
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, bossX, bossY, bossW, 9, Config.bossHpScale)) {
               this.isDraggingBossHp = true;
               this.dragOffsetX = mouseX - (double)Config.bossHpX;
               this.dragOffsetY = mouseY - (double)Config.bossHpY;
               return true;
            }
         }

         if (Config.calls) {
            int callX = Config.callX;
            int callY = Config.callY;
            int callW = this.font.width(this.callPreview());
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, callX, callY, callW, 9, Config.callScale)) {
               this.isDraggingCall = true;
               this.dragOffsetX = mouseX - (double)Config.callX;
               this.dragOffsetY = mouseY - (double)Config.callY;
               return true;
            }
         }

         if (Config.primedTimer) {
            int primedX = Config.primedTimerX;
            int primedY = Config.primedTimerY;
            int primedW = this.font.width(this.primedPreview());
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, primedX, primedY, primedW, 9, Config.primedTimerScale)) {
               this.isDraggingPrimed = true;
               this.dragOffsetX = mouseX - (double)Config.primedTimerX;
               this.dragOffsetY = mouseY - (double)Config.primedTimerY;
               return true;
            }
         }


         if (Config.trapCounter) {
            int trapX = Config.trapCounterX;
            int trapY = Config.trapCounterY;
            int trapW = this.font.width(this.trapPreview());
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, trapX, trapY, trapW, 9, Config.trapCounterScale)) {
               this.isDraggingTrap = true;
               this.dragOffsetX = mouseX - (double)Config.trapCounterX;
               this.dragOffsetY = mouseY - (double)Config.trapCounterY;
               return true;
            }
         }

         if (Config.dpsHudEnabled) {
            int var8 = Config.dpsHudX;
            int var10 = Config.dpsHudY;
            int var12 = this.font.width("DPS: 125.0");
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, var8, var10, var12, 9, Config.dpsHudScale)) {
               this.isDraggingDps = true;
               this.dragOffsetX = mouseX - (double)Config.dpsHudX;
               this.dragOffsetY = mouseY - (double)Config.dpsHudY;
               return true;
            }
         }

         int var9 = Config.alertX;
         int var11 = Config.alertY;
         int var13 = this.font.width(Config.alertText);
         Objects.requireNonNull(this.font);
         if (this.isMouseOver(mouseX, mouseY, var9, var11, var13, 9, Config.alertScale)) {
            this.isDraggingAlert = true;
            this.dragOffsetX = mouseX - (double)Config.alertX;
            this.dragOffsetY = mouseY - (double)Config.alertY;
            return true;
         }
      }

      return super.mouseClicked(event, bl);
   }

   private int snapX(int proposedX, int textWidth, float scale) {
      int center = this.width / 2;
      int scaledWidth = (int)((float)textWidth * scale);
      return Math.abs(proposedX + scaledWidth / 2 - center) < 15 ? center - scaledWidth / 2 : proposedX;
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      double mouseX = event.x();
      double mouseY = event.y();
      if (this.isDraggingAliveOrDead) {
         int proposedX = (int)(mouseX - this.dragOffsetX);
         Config.aliveOrDeadX = this.snapX(proposedX, this.font.width("CROUCH!"), Config.aliveOrDeadScale);
         Config.aliveOrDeadY = (int)(mouseY - this.dragOffsetY);
         return true;
      } else if (this.isDraggingDps) {
         int proposedX = (int)(mouseX - this.dragOffsetX);
         Config.dpsHudX = this.snapX(proposedX, this.font.width("DPS: 125.0"), Config.dpsHudScale);
         Config.dpsHudY = (int)(mouseY - this.dragOffsetY);
         return true;
      } else if (this.isDraggingPrimed) {
         int proposedX = (int)(mouseX - this.dragOffsetX);
         Config.primedTimerX = this.snapX(proposedX, this.font.width(this.primedPreview()), Config.primedTimerScale);
         Config.primedTimerY = (int)(mouseY - this.dragOffsetY);
         return true;
      } else if (this.isDraggingTrap) {
         int proposedX = (int)(mouseX - this.dragOffsetX);
         Config.trapCounterX = this.snapX(proposedX, this.font.width(this.trapPreview()), Config.trapCounterScale);
         Config.trapCounterY = (int)(mouseY - this.dragOffsetY);
         return true;
      } else if (this.isDraggingCall) {
         int proposedX = (int)(mouseX - this.dragOffsetX);
         Config.callX = this.snapX(proposedX, this.font.width(this.callPreview()), Config.callScale);
         Config.callY = (int)(mouseY - this.dragOffsetY);
         return true;
      } else if (this.isDraggingBossHp) {
         int proposedX = (int)(mouseX - this.dragOffsetX);
         Config.bossHpX = this.snapX(proposedX, this.font.width(FavelaBossHp.format(0.853F)), Config.bossHpScale);
         Config.bossHpY = (int)(mouseY - this.dragOffsetY);
         return true;
      } else if (this.isDraggingSplits) {
         Config.splitsX = (int)(mouseX - this.dragOffsetX);
         Config.splitsY = (int)(mouseY - this.dragOffsetY);
         return true;
      } else if (this.isDraggingAlert) {
         int proposedX = (int)(mouseX - this.dragOffsetX);
         Config.alertX = this.snapX(proposedX, this.font.width(Config.alertText), Config.alertScale);
         Config.alertY = (int)(mouseY - this.dragOffsetY);
         return true;
      } else {
         return super.mouseDragged(event, dragX, dragY);
      }
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      if (!this.isDraggingAlert && !this.isDraggingAliveOrDead && !this.isDraggingDps && !this.isDraggingTrap && !this.isDraggingCall && !this.isDraggingBossHp && !this.isDraggingSplits) {
         return super.mouseReleased(event);
      } else {
         this.isDraggingAlert = false;
         this.isDraggingAliveOrDead = false;
         this.isDraggingDps = false;
         this.isDraggingTrap = false;
         this.isDraggingPrimed = false;
         this.isDraggingCall = false;
         this.isDraggingBossHp = false;
         this.isDraggingSplits = false;
         Config.save();
         return true;
      }
   }

   public boolean keyPressed(KeyEvent event) {
      int keyCode;
      double mouseX;
      double mouseY;
      boolean var10000;
      label85: {
         keyCode = event.key();
         mouseX = this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
         mouseY = this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
         if (Config.aliveOrDeadMode) {
            int var10003 = Config.aliveOrDeadX;
            int var10004 = Config.aliveOrDeadY;
            int var10005 = this.font.width("CROUCH!");
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, var10003, var10004, var10005, 9, Config.aliveOrDeadScale)) {
               var10000 = true;
               break label85;
            }
         }

         var10000 = false;
      }

      boolean overVM;
      label80: {
         overVM = var10000;
         if (Config.dpsHudEnabled) {
            int var11 = Config.dpsHudX;
            int var13 = Config.dpsHudY;
            int var15 = this.font.width("DPS: 125.0");
            Objects.requireNonNull(this.font);
            if (this.isMouseOver(mouseX, mouseY, var11, var13, var15, 9, Config.dpsHudScale)) {
               var10000 = true;
               break label80;
            }
         }

         var10000 = false;
      }

      boolean overDps = var10000;
      boolean overTrap = false;
      if (Config.trapCounter) {
         int trapW = this.font.width(this.trapPreview());
         Objects.requireNonNull(this.font);
         overTrap = this.isMouseOver(mouseX, mouseY, Config.trapCounterX, Config.trapCounterY, trapW, 9, Config.trapCounterScale);
      }

      int var12 = Config.alertX;
      int var14 = Config.alertY;
      int var16 = this.font.width(Config.alertText);
      Objects.requireNonNull(this.font);
      boolean overAlert = this.isMouseOver(mouseX, mouseY, var12, var14, var16, 9, Config.alertScale);
      if (keyCode != 61 && keyCode != 334) {
         if (keyCode != 45 && keyCode != 333) {
            return super.keyPressed(event);
         } else {
            if (overVM) {
               Config.aliveOrDeadScale -= 0.1F;
               if (Config.aliveOrDeadScale < 0.5F) {
                  Config.aliveOrDeadScale = 0.5F;
               }
            } else if (overDps) {
               Config.dpsHudScale -= 0.1F;
               if (Config.dpsHudScale < 0.5F) {
                  Config.dpsHudScale = 0.5F;
               }
            } else if (overTrap) {
               Config.trapCounterScale -= 0.1F;
               if (Config.trapCounterScale < 0.5F) {
                  Config.trapCounterScale = 0.5F;
               }
            } else if (overAlert || !overVM && !overDps && !overTrap && !overAlert) {
               Config.alertScale -= 0.1F;
               if (Config.alertScale < 0.5F) {
                  Config.alertScale = 0.5F;
               }
            }

            Config.save();
            return true;
         }
      } else {
         if (overVM) {
            Config.aliveOrDeadScale += 0.1F;
            if (Config.aliveOrDeadScale > 5.0F) {
               Config.aliveOrDeadScale = 5.0F;
            }
         } else if (overDps) {
            Config.dpsHudScale += 0.1F;
            if (Config.dpsHudScale > 5.0F) {
               Config.dpsHudScale = 5.0F;
            }
         } else if (overTrap) {
            Config.trapCounterScale += 0.1F;
            if (Config.trapCounterScale > 5.0F) {
               Config.trapCounterScale = 5.0F;
            }
         } else if (overAlert || !overVM && !overDps && !overTrap && !overAlert) {
            Config.alertScale += 0.1F;
            if (Config.alertScale > 5.0F) {
               Config.alertScale = 5.0F;
            }
         }

         Config.save();
         return true;
      }
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }
}
