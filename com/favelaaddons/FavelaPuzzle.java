package com.favelaaddons;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class FavelaPuzzle {
   public static List<BlockPos> activeSolution = new ArrayList();
   private static long startTime;

   public static void escanearEResolver(Minecraft client) {
      if (!Build.FULL) {
         return;
      }

      activeSolution.clear();
      if (client.level != null && client.player != null) {
         char[][] grid = new char[10][10];

         for(int r = 0; r < 10; ++r) {
            for(int c = 0; c < 10; ++c) {
               grid[r][c] = '.';
            }
         }

         BlockPos topLeft = new BlockPos(-346, 43, 179);
         AABB roomBox = new AABB((double)(topLeft.getX() - 1), (double)topLeft.getY(), (double)(topLeft.getZ() - 1), (double)(topLeft.getX() + 11), (double)(topLeft.getY() + 4), (double)(topLeft.getZ() + 11));
         List<Entity> entities = client.level.getEntities(client.player, roomBox);
         boolean foundWall = false;

         for(Entity e : entities) {
            if (e instanceof Display.ItemDisplay) {
               Display.ItemDisplay itemDisplay = (Display.ItemDisplay)e;
               BlockPos ePos = e.blockPosition();
               int c = ePos.getX() - topLeft.getX();
               int r = ePos.getZ() - topLeft.getZ();
               if (c >= 0 && c < 10 && r >= 0 && r < 10) {
                  String data = itemDisplay.getItemStack().getComponents().toString();
                  if (data.contains("number0")) {
                     grid[r][c] = '0';
                  } else if (data.contains("number1")) {
                     grid[r][c] = '1';
                  } else if (data.contains("number2")) {
                     grid[r][c] = '2';
                  } else if (data.contains("number3")) {
                     grid[r][c] = '3';
                  } else if (data.contains("number4")) {
                     grid[r][c] = '4';
                  } else if (data.contains("colors=[0]") && grid[r][c] == '.') {
                     grid[r][c] = 'X';
                  }
               }
            }
         }

         for(int r = 0; r < 10; ++r) {
            for(int c = 0; c < 10; ++c) {
               if (grid[r][c] != '.') {
                  foundWall = true;
               }
            }
         }

         if (foundWall) {
            startTime = System.currentTimeMillis();
            boolean[][] bulbs = new boolean[10][10];
            boolean solved = solve(grid, bulbs, 0, 0);
            long endTime = System.currentTimeMillis();
            if (solved) {
               for(int r = 0; r < 10; ++r) {
                  for(int c = 0; c < 10; ++c) {
                     if (bulbs[r][c]) {
                        activeSolution.add(topLeft.offset(c, 0, r));
                     }
                  }
               }
            }

         }
      }
   }

   private static boolean solve(char[][] grid, boolean[][] bulbs, int r, int c) {
      if (System.currentTimeMillis() - startTime > 5000L) {
         return false;
      } else {
         if (c == 10) {
            ++r;
            c = 0;
         }

         if (isUnsolvable(grid, bulbs, r, c)) {
            return false;
         } else if (r == 10) {
            return isSolved(grid, bulbs);
         } else if (grid[r][c] != '.') {
            return solve(grid, bulbs, r, c + 1);
         } else {
            if (canPlace(grid, bulbs, r, c)) {
               bulbs[r][c] = true;
               if (solve(grid, bulbs, r, c + 1)) {
                  return true;
               }

               bulbs[r][c] = false;
            }

            return solve(grid, bulbs, r, c + 1);
         }
      }
   }

   private static boolean isUnsolvable(char[][] grid, boolean[][] bulbs, int currentR, int currentC) {
      for(int r = 0; r < 10; ++r) {
         for(int c = 0; c < 10; ++c) {
            if (grid[r][c] >= '0' && grid[r][c] <= '4') {
               int req = grid[r][c] - 48;
               int adj = 0;
               int possible = 0;
               if (r > 0 && grid[r - 1][c] == '.') {
                  if (bulbs[r - 1][c]) {
                     ++adj;
                     ++possible;
                  } else if (!isDecided(r - 1, c, currentR, currentC)) {
                     ++possible;
                  }
               }

               if (r < 9 && grid[r + 1][c] == '.') {
                  if (bulbs[r + 1][c]) {
                     ++adj;
                     ++possible;
                  } else if (!isDecided(r + 1, c, currentR, currentC)) {
                     ++possible;
                  }
               }

               if (c > 0 && grid[r][c - 1] == '.') {
                  if (bulbs[r][c - 1]) {
                     ++adj;
                     ++possible;
                  } else if (!isDecided(r, c - 1, currentR, currentC)) {
                     ++possible;
                  }
               }

               if (c < 9 && grid[r][c + 1] == '.') {
                  if (bulbs[r][c + 1]) {
                     ++adj;
                     ++possible;
                  } else if (!isDecided(r, c + 1, currentR, currentC)) {
                     ++possible;
                  }
               }

               if (adj > req) {
                  return true;
               }

               if (possible < req) {
                  return true;
               }
            }
         }
      }

      for(int r = 0; r < 10; ++r) {
         for(int c = 0; c < 10; ++c) {
            if (grid[r][c] == '.' && isDecided(r, c, currentR, currentC) && !isCellLit(grid, bulbs, r, c)) {
               boolean canBeLit = false;

               for(int i = r + 1; i < 10 && grid[i][c] == '.'; ++i) {
                  if (!isDecided(i, c, currentR, currentC)) {
                     canBeLit = true;
                     break;
                  }
               }

               if (!canBeLit) {
                  for(int i = c + 1; i < 10 && grid[r][i] == '.'; ++i) {
                     if (!isDecided(r, i, currentR, currentC)) {
                        canBeLit = true;
                        break;
                     }
                  }
               }

               if (!canBeLit) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static boolean isDecided(int r, int c, int currentR, int currentC) {
      return r < currentR || r == currentR && c < currentC;
   }

   private static boolean canPlace(char[][] grid, boolean[][] bulbs, int r, int c) {
      for(int i = r - 1; i >= 0 && grid[i][c] == '.'; --i) {
         if (bulbs[i][c]) {
            return false;
         }
      }

      for(int i = r + 1; i < 10 && grid[i][c] == '.'; ++i) {
         if (bulbs[i][c]) {
            return false;
         }
      }

      for(int i = c - 1; i >= 0 && grid[r][i] == '.'; --i) {
         if (bulbs[r][i]) {
            return false;
         }
      }

      for(int i = c + 1; i < 10 && grid[r][i] == '.'; ++i) {
         if (bulbs[r][i]) {
            return false;
         }
      }

      return true;
   }

   private static boolean isCellLit(char[][] grid, boolean[][] bulbs, int r, int c) {
      if (bulbs[r][c]) {
         return true;
      } else {
         for(int i = r - 1; i >= 0 && grid[i][c] == '.'; --i) {
            if (bulbs[i][c]) {
               return true;
            }
         }

         for(int i = r + 1; i < 10 && grid[i][c] == '.'; ++i) {
            if (bulbs[i][c]) {
               return true;
            }
         }

         for(int i = c - 1; i >= 0 && grid[r][i] == '.'; --i) {
            if (bulbs[r][i]) {
               return true;
            }
         }

         for(int i = c + 1; i < 10 && grid[r][i] == '.'; ++i) {
            if (bulbs[r][i]) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean isSolved(char[][] grid, boolean[][] bulbs) {
      for(int r = 0; r < 10; ++r) {
         for(int c = 0; c < 10; ++c) {
            if (grid[r][c] >= '0' && grid[r][c] <= '4') {
               int req = grid[r][c] - 48;
               int adj = 0;
               if (r > 0 && bulbs[r - 1][c]) {
                  ++adj;
               }

               if (r < 9 && bulbs[r + 1][c]) {
                  ++adj;
               }

               if (c > 0 && bulbs[r][c - 1]) {
                  ++adj;
               }

               if (c < 9 && bulbs[r][c + 1]) {
                  ++adj;
               }

               if (adj != req) {
                  return false;
               }
            }
         }
      }

      for(int r = 0; r < 10; ++r) {
         for(int c = 0; c < 10; ++c) {
            if (grid[r][c] == '.' && !isCellLit(grid, bulbs, r, c)) {
               return false;
            }
         }
      }

      return true;
   }
}
