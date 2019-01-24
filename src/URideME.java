import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/*
 * This is currently a mock to experiment with UI elements
 * on the Nokia 3310 3G. The APIs to make it work are simple
 * but we'll need to use 3rd party JSON librries, and doing
 * OAuth on to get a user access token may be some work.
 */
public class URideME extends MIDlet
    implements CommandListener, ItemStateListener {

  private Display display = null;
  private FontCanvas fontCanvas = null;
  private final int padding = 4;
  private boolean painting = false;
  private static Image pinImage = null;
  private static Image mapImage = null;
  private static Image knockoutImage= null;
  private static Image backgroundImage = null;
  private static Image logoImage = null;
  private static Image brandingImage = null;
  private static Image addressImage = null;
  private static Image big1minImage = null;
  private static Image big2minImage = null;
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

  public URideME() {
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
    private String strText = "";
    private String strAction = "";
    private Vector vect = new Vector();
    private URideME parent = null;
    private int width;
    private int height;
    protected Timer timer;
    protected TimerTask updateTask;
    static final int FAST_DELAY = 21;
    private int circle_min = 180;
    private int circle_pos = 190;
    private int circle_max = 200;
    private int circle_direction = 1;
    private int progress_w = padding;
    private int spinner = 360;

    public FontCanvas(URideME parent) {
      this.parent = parent;
      this.setFullScreenMode(true);
      width = getWidth();
      height = getHeight();
      try {
        pinImage = Image.createImage ("/pin.png");
        mapImage = Image.createImage ("/map.png");
        knockoutImage = Image.createImage ("/knockout.png");
        logoImage = Image.createImage ("/icon72x72.png");
        brandingImage = Image.createImage ("/branding.png");
        addressImage = Image.createImage ("/address.png");
        big1minImage = Image.createImage ("/big1min.png");
        big2minImage = Image.createImage ("/big2min.png");
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
            pulseCircle();
          } else if (state == 2) {
            showProgress();
          } else if (state == 3) {
            spinDown();
          }
          repaint(width - 100, 0, 100, 20);
        }
      };
      long interval = FAST_DELAY;
      timer.schedule(updateTask, interval, interval);
    }

    protected void stopFrameTimer() {
      timer.cancel();
    }

    public synchronized void pulseCircle() {
      circle_pos += (1 * circle_direction);
      if (circle_pos <= circle_min) {
        circle_direction = 1;
      } else if (circle_pos >= circle_max) {
        circle_direction = -1;
      }
      repaint((width - circle_max) / 2, (height - circle_max) / 2, circle_max, circle_max);
    }

    public synchronized void spinDown() {
      spinner = (spinner < 9) ? 360 : spinner - 9;
      repaint(width / 2 - (74 / 2), 100 - (74 / 2), 74, 74);
    }

    public synchronized void showProgress() {
      if (progress_w < width - (padding * 2)) {
        progress_w++;
        repaint(padding, 32, 2, progress_w);
      } else {
        progress_w = padding;
      }
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
      state = (state > 2) ? 0 : state + 1;
      this.repaint();
    }

    public void paint(Graphics g) {
      calendar = Calendar.getInstance();
      int hour = calendar.get(Calendar.HOUR); if (hour < 1) { hour += 12; }
      int minute = calendar.get(Calendar.MINUTE);
      //int second = calendar.get(Calendar.SECOND);
      String strTime = "" + hour + (minute < 10 ? ":0" : ":") + minute;
      // Load some page defaults
      if (state == -1) {
        backgroundImage = null;
        strText = "";
        strTime = "";
      } else if (state == 0) {
        backgroundImage = addressImage;
        strText = "Destination";
      } else if (state == 1) {
        backgroundImage = null;
        strText = "UberX";
      } else if (state == 2) {
        backgroundImage = mapImage;
        strText = "Requesting";
      } else {
        backgroundImage = enrouteImage;
        strText = "En Route";
      }
      // Set a background image or just fill black
      if (null == backgroundImage) {
        g.setColor(0x000000);
        g.fillRect(0, 0, width, height);
      } else {
        g.drawImage(backgroundImage, width / 2, height / 2, Graphics.HCENTER | Graphics.VCENTER);
      }

      Font fontSm = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
      Font fontLg = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
      // Place page specific elements
      if (state == -1) {
        g.drawImage(logoImage, width / 2, height / 2 - 50, Graphics.VCENTER | Graphics.HCENTER);
        g.drawImage(brandingImage, width / 2, height / 2 + 50, Graphics.VCENTER | Graphics.HCENTER);
        g.setColor(0xFFFFFF);
        letterFont = openSansLight;
        letters(g, "Ride Uber on J2ME", 28, height / 2 + 80);
      } else if (state == 1) {
        int cx = width / 2 - (circle_pos / 2);
        int cy = height / 2 - (circle_pos / 2);
        int w = 4;
        g.setColor(0x24c4e2);
        g.fillArc(cx + 0, cy + 0, circle_pos - (2 * 0), circle_pos - (2 * 0), 0, 365);
        g.setColor(0x000000);
        g.fillArc(cx + w, cy + w, circle_pos - (2 * w), circle_pos - (2 * w), 0, 365);
        g.setColor(0xFFFFFF);
        letterFont = openSansBold;
        letters(g, "REQUEST", width / 2 - 44, height - 23);
        g.setColor(0x99FF99);
        g.drawRoundRect(width / 2 - 52, height - 28, 104, 27, 9, 9);
        g.drawImage(big2minImage, width / 2, height / 2, Graphics.VCENTER | Graphics.HCENTER);
      } else if (state == 2) {
        g.drawImage(knockoutImage, 0, 0, Graphics.LEFT | Graphics.TOP);
        g.setColor(0xFFFFFF);
        letterFont = openSansBold;
        letters(g, "CANCEL", width / 2 - 38, height - 23);
        g.setColor(0xFF3333);
        g.drawRoundRect(width / 2 - 52, height - 28, 104, 27, 9, 9);
        g.setColor(0x99FF99);
        g.setStrokeStyle(Graphics.DOTTED);
        g.drawRect(padding, 32, progress_w, 3);
        g.drawRect(padding, 33, progress_w - 2, 1);
        g.drawImage(pinImage, width / 2, height / 2, Graphics.HCENTER | Graphics.VCENTER);
      } else if (state == 3) {
        g.setColor(0x1F1F1F);
        g.fillArc(width / 2 - (74 / 2), 100 - (74 / 2), 74, 74, 0, 365);
        g.setColor(0x24c4e2);
        g.fillArc(width / 2 - (64 / 2), 100 - (64 / 2), 63, 63, spinner - 15, 335);
        g.setColor(0xFFFFFF);
        g.fillArc(width / 2 - (66 / 2), 100 - (66 / 2), 66, 66, spinner - 30, 6);
        g.setColor(0x1F1F1F);
        g.fillArc(width / 2 - (62 / 2), 100 - (62 / 2), 62, 62, 0, 365);
        g.drawImage(big1minImage, width / 2, 100, Graphics.VCENTER | Graphics.HCENTER);
      }
      // No menus on Splash Screen or Requesting Page (which shoud be a modal dialog)
      if (state != -1 && state != 2) {
        g.setColor(0xFFFFFF);
        g.setFont(fontSm);
        g.drawString("Menu", padding, height - fontSm.getHeight(), Graphics.LEFT | Graphics.TOP);
        g.drawString("Back", width - padding - fontSm.stringWidth("Back"),
                                height - fontSm.getHeight(), Graphics.LEFT | Graphics.TOP);
      }
      g.setColor(0xFFFFFF);
      letterFont = openSansBold;
      letters(g, strText, 4, 4);
      letters(g, strTime, width - (strTime.length() * 12) + 2, 4);
      painting = false;
    }

  }

}
