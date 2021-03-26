package hauptFenster;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.util.HashMap;

import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;

import CommonTools.Colors;

public final class CompoundPainterMap {
    public static final HashMap<String, CompoundPainter<Object>> map = new HashMap<String, CompoundPainter<Object>>();

    static {

        CompoundPainter<Object> cp = null;
        MattePainter mp = null;
        LinearGradientPaint p = null;
        /*****************/
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(960, 100);
        float[] dist = { 0.0f, 0.75f };
        Color[] colors = { Color.WHITE, Colors.PiOrange.alpha(0.25f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("PatNeuanlage", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 100);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, new Color(231, 120, 23) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("SuchePanel", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 15);// vorher 45
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Colors.PiOrange.alpha(0.5f), Color.WHITE };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("ButtonPanel", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 40);
        dist = new float[] { 0.0f, 1.00f };
        colors = new Color[] { Colors.PiOrange.alpha(0.5f), Color.WHITE };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("StammDatenPanel", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 100);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Colors.PiOrange.alpha(0.70f), Color.WHITE };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("AnredePanel", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 150);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.PiOrange.alpha(0.5f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("HauptPanel", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 150);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.PiOrange.alpha(0.5f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("FliessText", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 150);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.PiOrange.alpha(0.5f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("getTabs", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 450);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Colors.PiOrange.alpha(0.25f), Color.WHITE };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("getTabs2", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(350, 290);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Yellow.alpha(0.05f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("RezeptGebuehren", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(400, 550);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Gray.alpha(0.15f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("EBerichtPanel", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(600, 350);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Yellow.alpha(0.25f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("ArztBericht", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(600, 750);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Yellow.alpha(0.05f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("RezNeuanlage", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(300, 100);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Gray.alpha(0.05f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("ScannerUtil", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 400);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.TaskPaneBlau.alpha(0.45f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("ArztAuswahl", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 400);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Green.alpha(0.45f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("KassenAuswahl", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(900, 100);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.PiOrange.alpha(0.25f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("KVKRohDaten", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(600, 550);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.TaskPaneBlau.alpha(0.45f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("ArztPanel", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(400, 100);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Blue.alpha(0.15f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("ArztNeuanlage", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(400, 100);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Green.alpha(0.25f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("KasseNeuanlage", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(600, 550);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Green.alpha(0.5f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("KassenPanel", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(200, 120);
        dist = new float[] { 0.0f, 0.5f };
        colors = new Color[] { Colors.TaskPaneBlau.alpha(1.0f), Color.WHITE };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("SuchenSeite", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(300, 270);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Gray.alpha(0.15f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("GutachtenWahl", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(900, 100);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Color.WHITE, Colors.Yellow.alpha(0.05f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("VorBerichte", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(0, 600);
        dist = new float[] { 0.0f, 0.75f };
        colors = new Color[] { Colors.Yellow.alpha(0.15f), Color.WHITE };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("TextBlock", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(200, 120);
        dist = new float[] { 0.0f, 0.5f };
        colors = new Color[] { Colors.TaskPaneBlau.alpha(1.0f), Color.WHITE };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("TagWahlNeu", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(390, 180);
        dist = new float[] { 0.0f, 0.5f };
        colors = new Color[] { Colors.TaskPaneBlau.alpha(1.0f), Color.WHITE };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("Zeitfenster", cp);
        /*****************/
        start = new Point2D.Float(0, 0);
        end = new Point2D.Float(400, 500);
        dist = new float[] { 0.0f, 0.5f };
        colors = new Color[] { Color.WHITE, Colors.Gray.alpha(0.15f) };
        p = new LinearGradientPaint(start, end, dist, colors);
        mp = new MattePainter(p);
        cp = new CompoundPainter<Object>(mp);
        map.put("SystemInit", cp);

        /*****************/
    }

    public CompoundPainter<Object> forName(String name) {
        return map.get(name);

    }
}
