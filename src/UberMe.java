import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class UberMe extends MIDlet
    implements CommandListener, ItemStateListener {

  private Display display = null;
  private FontCanvas fontCanvas = null;
  private boolean painting = false;
  private static Image currentImage = null;
  private static Image splashImage = null;
  private static Image addressImage = null;
  private static Image requestImage = null;
  private static Image enrouteImage = null;
  private static Image openSansBold = null;
  private static Image openSansLight = null;
  private static Image letterFont = null;
  public Calendar calendar;

  private final int openSansMetrics[] = {
       8,  6,  9, 14, 13, 18, 16,  5,  7,  7, 11, 12,  6,  6,  6,  9,
      12, 12, 12, 12, 12, 12, 12, 12, 12, 12,  5,  5, 13, 12, 12, 10,
      18, 14, 14, 13, 15, 11, 11, 14, 15,  6,  6, 14, 12, 18, 15, 16,
      13, 16, 14, 11, 12, 14, 13, 19, 13, 12, 12,  7,  9,  7, 11, 10,
       9, 11, 13, 10, 12, 12,  9, 12, 12,  5,  5, 12,  5, 19, 12, 12,
      13, 12, 10, 10,  9, 13, 12, 17, 12, 12, 10,  9,  8,  9, 12,  8 };

  public UberMe() {
    display = Display.getDisplay(this);
    fontCanvas = new FontCanvas(this);
  }

  public void startApp() throws MIDletStateChangeException {
    display.setCurrent(fontCanvas);
  }

  public void pauseApp() {}

  protected void destroyApp(boolean unconditional)
      throws MIDletStateChangeException {}

  public void commandAction(Command c, Displayable d) {}

  public void itemStateChanged(Item item) {}

  class FontCanvas extends Canvas {

    private int state = -1;
    private int circle_w = 180;
    private int circle_min = 175;
    private int circle_max = 185;
    private int circle_direction = 1;
    private String strText = "";
    private String strAction = "";
    private Vector vect = new Vector();
    private UberMe parent = null;
    private int width;
    private int height;
    protected Timer timer;
    protected TimerTask updateTask;
    static final int FRAME_DELAY = 60;

    public FontCanvas(UberMe parent) {
      this.parent = parent;
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
      try {
        splashImage = Image.createImage ("/splash.png");
        addressImage = Image.createImage ("/address.png");
        requestImage = Image.createImage ("/request.png");
        enrouteImage = Image.createImage ("/enroute.png");
        openSansBold = Image.createImage ("/sans-bold-20.png");
        openSansLight = Image.createImage ("/sans-light-20.png");
      } catch (Exception ex) {
      }
    }

    public boolean animate() {
      System.out.println("animate is called");
      return (state == 1);
    }

    protected void showNotify() {
      startFrameTimer();
    }

    protected void hideNotify() {
      stopFrameTimer();
    }

    protected void startFrameTimer() {
      timer = new Timer();
      updateTask = new TimerTask() {
        public void run() {
          if (state == 1) {
            moveCircle();
          }
          // TODO: update the the clock and other timers
        }
      };
      long interval = FRAME_DELAY;
      timer.schedule(updateTask, interval, interval);
    }

    protected void stopFrameTimer() {
      timer.cancel();
    }

    // Called on expiry of timer.
    public synchronized void moveCircle() {
      circle_w += (1 * circle_direction);
      if (circle_w <= circle_min) {
        circle_direction = 1;
      } else if (circle_w >= circle_max) {
        circle_direction = -1;
      }
      repaint(15, 90, 200, 200);
    }

    public void letters(Graphics g, String phrase, int fx, int fy) {
      for (int i = 0; i < phrase.length(); i++) {
        int cw = 20;
        int ch = 22;
        int ascii = ((int) phrase.charAt(i));
        if (ascii >= 32 && ascii <= 126) {
          int cx = ((ascii - 32) / 8) * cw;
          int cy = ((ascii - 32) % 8) * ch;
          cw = openSansMetrics[ascii - 32];
          g.setClip(fx, fy, cw, ch);
          g.fillRect(fx, fy, cw, ch);
          g.drawImage(letterFont, fx - cx, fy - cy, Graphics.LEFT | Graphics.TOP);
          fx += cw;
          g.setClip(0 ,0, width, height);
        }
      }
    }

    public void keyPressed(int keyCode){
      vect.addElement(getKeyName(keyCode));
      state = (state > 1) ? 0 : state + 1;
      this.repaint();
    }

    public void paint(Graphics g) {
      calendar = Calendar.getInstance();
      int hour = calendar.get(Calendar.HOUR); if (hour < 1) { hour += 12; }
      int minute = calendar.get(Calendar.MINUTE);
      String strTime = "" + hour + (minute < 10 ? ":0" : ":") + minute;
      Font fontSm = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
      Font fontLg = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);

      if (state == -1) {
        currentImage = splashImage;
        strText = "";
        strTime = "";
      } else if (state == 0) {
        currentImage = addressImage;
        strText = "Uber Me";
      } else if (state == 1) {
        currentImage = requestImage;
        strText = "Request";
      } else {
        currentImage = enrouteImage;
        strText = "En Route";
      }
      g.drawImage(currentImage, 0, 0, Graphics.LEFT | Graphics.TOP);
      g.setColor(0xFFFFFF);
      letterFont = openSansBold;
      letters(g, strText, 4, 4);
      letters(g, strTime, width - (strTime.length() * 12) + 2, 4);
      g.setFont(fontSm);
      if (state == 1) {
        g.setColor(0x24c4e2);
        // put this in a for or while loop
        int cx = width / 2 - (circle_w / 2);
        int cy = 190 - (circle_w / 2); // Maybe use centerpoint
        g.drawArc(cx + 0, cy + 0, circle_w - 0, circle_w, 0, 365);
        g.drawArc(cx + 1, cy + 1, circle_w - 2, circle_w - 2, 0, 365);
        g.drawArc(cx + 2, cy + 2, circle_w - 4, circle_w - 4, 0, 365);
        // done drawing circle
        g.setColor(0xFFFFFF);
        letters(g, "REQUEST", width / 2 - 44, height - 22);
        g.setColor(0x99ff99);
        g.drawRoundRect(width / 2 - 52, height - 27, 104, 26, 9, 9);
      }
      if (state > -1) {
        g.setColor(0xFFFFFF);
        g.drawString("Menu", 4, height - fontSm.getHeight(), Graphics.LEFT | Graphics.TOP);
        g.drawString("Back", width - 4 - fontSm.stringWidth("Back"),
                                height - fontSm.getHeight(), Graphics.LEFT | Graphics.TOP);
      }
      painting = false;
    }

  }

}
