/*
 *
 *
 *
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.*;


public class Explorer {

  private int pollRun = 0;
  private RobotData robotData;
  private int explorerMode;

  private int nonwallExits (IRobot robot) {
     int walls = 0;
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++) {
       if(robot.look(i) == IRobot.WALL)
       walls++;
     }
     int nowalls = 4 - walls;
     return nowalls;
   }

  private int passageExits (IRobot robot) {
     int pass = 0;
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++) {
       if(robot.look(i) == IRobot.PASSAGE)
       pass++;
     }
     return pass;
   }

  private int beenbeforeExits (IRobot robot) {
     int been = 0;
     for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++) {
       if(robot.look(i) == IRobot.BEENBEFORE)
       been++;
     }
     return been;
   }



   // Ar teisingai?
  private int DeadEnd(IRobot robot) {
     int dir = IRobot.AHEAD;
     //for (int i = IRobot.AHEAD; i <= IRobot.LEFT; i++) {
       //if(robot.look(i) != IRobot.WALL)
       //int dir = i;
     //}
     while (robot.look(dir) == IRobot.WALL) {
       dir++;
     }
     return dir;
   }

  private int Corridor(IRobot robot) {
     if (robot.look(IRobot.AHEAD) != IRobot.WALL ) {
        return IRobot.AHEAD;
     }
     else if (robot.look(IRobot.LEFT) != IRobot.WALL ) {
        return IRobot.LEFT;
     }
     else return IRobot.RIGHT;
   }

  private int JuncOrCross(IRobot robot) {
     int dir = IRobot.AHEAD;
     int p = passageExits(robot);
     int randno;
     if(p>0)
        do {
         randno = (int) Math.round(Math.random()*3);
         switch(randno) {
           case 0:
       	      dir = IRobot.LEFT;
              break;
       	   case 1:
       	      dir = IRobot.RIGHT;
              break;
       	   case 2:
       	      dir = IRobot.BEHIND;
              break;
       	   default:
       	      dir = IRobot.AHEAD;
           }
        } while (robot.look(dir) != IRobot.PASSAGE);

     else do {
          randno = (int) Math.round(Math.random()*3);
          switch(randno) {
            case 0:
        	      dir = IRobot.LEFT;
                break;
        	  case 1:
        	      dir = IRobot.RIGHT;
                break;
        	  case 2:
        	      dir = IRobot.BEHIND;
                break;
        	  default:
        	      dir = IRobot.AHEAD;
          }
        } while (robot.look(dir) != IRobot.BEENBEFORE);
     return dir;
   }

  /*private int Crossroad(IRobot robot) {
     int dir = IRobot.AHEAD;
     int p = passageExits(robot);
     int randno;
     if(p>0)
        do {
         randno = (int) Math.round(Math.random()*3);
         switch(randno) {
           case 0:
       	      dir = IRobot.LEFT;
              break;
       	   case 1:
       	      dir = IRobot.RIGHT;
              break;
       	   case 2:
       	      dir = IRobot.BEHIND;
              break;
       	   default:
       	      dir = IRobot.AHEAD;
           }
        } while (robot.look(dir) != IRobot.PASSAGE);

     else do {
          randno = (int) Math.round(Math.random()*3);
          switch(randno) {
            case 0:
        	      dir = IRobot.LEFT;
                break;
        	  case 1:
        	      dir = IRobot.RIGHT;
                break;
        	  case 2:
        	      dir = IRobot.BEHIND;
                break;
        	  default:
        	      dir = IRobot.AHEAD;
          }
        } while (robot.look(dir) != IRobot.BEENBEFORE);
     return dir;
   } */

  public int exploreControl (IRobot robot) {
    int exits = nonwallExits(robot);
    int direction = IRobot.AHEAD;

    switch(exits) {
      case 1:
        if (pollRun == 0)
        direction = DeadEnd(robot);
        else direction = backtrackControl(robot);
        break;
      case 2:
        direction = Corridor(robot);
        break;
      case 3:
      default:
        if (beenbeforeExits(robot) == 1) {
          robotData.recordJunction(robot);
          robotData.printJunction();
          direction = JuncOrCross(robot);
        }
        else if (passageExits(robot) == 0) {
                direction = backtrackControl(robot);
            }
        else direction = JuncOrCross(robot);
      }
    return direction;
  }

  public int backtrackControl(IRobot robot) {
    int exits = nonwallExits(robot);
    int direction = IRobot.AHEAD;

    switch(exits) {
      case 1:
        direction = DeadEnd(robot);
        break;
      case 2:
        direction = Corridor(robot);
        break;
      case 3:
      default:
        if (passageExits(robot) == 0 ) {
          int firstenter = robotData.searchJunction(robot.getLocation().x, robot.getLocation().y);
          int opposite;

          if(firstenter - 1000 < 2 )
            opposite = firstenter + 2;
          else
            opposite = firstenter - 2;

          robot.setHeading(opposite);
          direction  = IRobot.AHEAD;
        }
       else  { direction = exploreControl(robot);
       explorerMode = 0; }

    }
    return direction;
  }

  public void controlRobot(IRobot robot){
      int go;
      // On the first move of the first run of a new maze
      if ((robot.getRuns() == 0) && (pollRun == 0)) {
         robotData = new RobotData(); //reset the data store
         explorerMode = 1;
       }

      if (explorerMode == 1)
        go = exploreControl(robot);
      else go = backtrackControl(robot);

      robot.face(go);

      pollRun++; // Increment pollRun so that the data is not
                 // reset each time the robot moves
   }

   public void reset() {
     robotData.resetJunctionCounter();
     robotData.resetArrayList();
     explorerMode = 1;
   }
 }

class RobotData {
      private static int junctionCounter;
      ArrayList junctionRecorder = new ArrayList();
      //private static int maxJunctions = 10000;
      //Junctions[] junctionRecorder = new Junctions [maxJunctions];
      //Point[] JunctionRecorder = new Point [maxJunctions];

      /*public void Recorder (int x, int y, int a) {
        junctionRecorder[junctionCounter] = new Junctions (x, y, a);
        junctionCounter++;
      }*/

      public void recordJunction (IRobot robot) {
        junctionRecorder.add(robot.getLocation().x);
        junctionRecorder.add(robot.getLocation().y);
        junctionRecorder.add(robot.getHeading());
        junctionCounter++;
      }

      public void printJunction () {
        int xx = (int) junctionRecorder.get(3*(junctionCounter-1));
        int yy = (int) junctionRecorder.get(3*(junctionCounter-1)+1);
        int aa = (int) junctionRecorder.get(3*(junctionCounter-1)+2);
        String direction;
        int dir = aa-1000;
        switch (dir) {
          case 0:
          direction = "NORTH";
          break;
          case 1:
          direction = "EAST";
          break;
          case 2:
          direction = "SOUTH";
          break;
          default:
          direction = "WEST";
        }
        System.out.println("Junction " + junctionCounter + "(x=" + xx + " ,y=" + yy + ") heading " + direction);
      }

      public int searchJunction(int x, int y) {
        int enter = 0;
        for (int i = 1; i<= junctionCounter ; i++) {
          int xx = (int) junctionRecorder.get(3*(i-1));
          int yy = (int) junctionRecorder.get(3*(i-1)+1);
          int aa = (int) junctionRecorder.get(3*(i-1)+2);
          if(x == xx && y == yy)
            enter = aa;
        }
        return enter;
      }

      public void resetJunctionCounter() {
          junctionCounter = 0;
      }

      public void resetArrayList() {
          junctionRecorder.removeAll(junctionRecorder);
      }

      /*class Junctions {
        int x;
        int y;
        int a;
        public Junctions (int x, int y, int a) {
          this.x = x;
          this.y = y;
          this.a = a;
        }
      }*/
    }
