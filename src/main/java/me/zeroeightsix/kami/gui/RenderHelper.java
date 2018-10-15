package me.zeroeightsix.kami.gui;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by 086 on 5/07/2017.
 */
public class RenderHelper {

    public static void drawArc(double cx, double cy, double r, double start_angle, double end_angle, int num_segments)
    {
        glBegin(GL_TRIANGLES);

        for (int i = (int) (num_segments/(360/start_angle))+1; i <= num_segments/(360/end_angle); i++) {
            double previousangle = 2*Math.PI*(i-1)/num_segments;
            double angle = 2*Math.PI*i/num_segments;
            glVertex2d(cx, cy);
            glVertex2d(cx+Math.cos(angle)*r, cy+Math.sin(angle)*r);
            glVertex2d(cx+Math.cos(previousangle)*r, cy+Math.sin(previousangle)*r);
        }

        glEnd();
    }

    public static void drawArcOutline(double cx, double cy, double r, double start_angle, double end_angle, int num_segments) {
        glBegin(GL_LINE_LOOP);

        for (int i = (int) (num_segments/(360/start_angle))+1; i <= num_segments/(360/end_angle); i++) {
            double angle = 2*Math.PI*i/num_segments;
            glVertex2d(cx+Math.cos(angle)*r, cy+Math.sin(angle)*r);
        }

        glEnd();
    }

    public static void drawCircleOutline(double x, double y, double radius){
        drawCircleOutline(x, y, radius, 0, 360, 40);
    }

    public static void drawCircleOutline(double x, double y, double radius, int start, int end, int segments){
        drawArcOutline(x, y, radius, start, end, segments);
    }

    public static void drawCircle(double x, double y, double radius){
        drawCircle(x, y, radius, 0, 360, 64);
    }

    public static void drawCircle(double x, double y, double radius, int start, int end, int segments){
        drawArc(x, y, radius, start, end, segments);
    }

    public static void drawOutlinedRoundedRectangle(int x, int y, int width, int height, double radius, double dR, double dG, double dB, double dA, double outlineWidth) {
        drawRoundedRectangle(x, y, width, height, radius);
        glColor4d(dR, dG, dB, dA);
        drawRoundedRectangle(x+outlineWidth, y+outlineWidth, width - outlineWidth*2, height - outlineWidth*2, radius);
    }

    public static void drawRectangle(double x, double y, double width, double height) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBegin(GL_LINE_LOOP);
        {
            glVertex2d(width, 0);
            glVertex2d(0, 0);
            glVertex2d(0, height);
            glVertex2d(width, height);
        }
        glEnd();
    }

    public static void drawFilledRectangle(double x, double y, double width, double height) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBegin(GL_QUADS);
        {
            glVertex2d(x+width, y);
            glVertex2d(x, y);
            glVertex2d(x, y+height);
            glVertex2d(x+width, y+height);
        }
        glEnd();
    }

    public static void drawRoundedRectangle(double x, double y, double width, double height, double radius){
        glEnable(GL_BLEND);

//        drawArc(50,50,30,0,90,64);
        drawArc( (x + width - radius),  (y + height - radius), radius, 0,90,16); // bottom right
        drawArc( (x+radius),  (y + height - radius), radius, 90,180,16); // bottom left
        drawArc(x+radius, y+radius, radius, 180,270,16); // top left
        drawArc( (x + width - radius),  (y+radius), radius, 270,360,16); // top right

        glBegin(GL_TRIANGLES);
        {
            glVertex2d(x + width - radius, y);
            glVertex2d(x + radius, y);
            glVertex2d(x + width - radius, y+radius);

            glVertex2d(x + width - radius, y+radius);
            glVertex2d(x + radius, y);
            glVertex2d(x + radius, y+radius);


            glVertex2d(x+width, y+radius);
            glVertex2d(x, y+radius);
            glVertex2d(x, y+height-radius);

            glVertex2d(x+width, y+radius);
            glVertex2d(x, y+height-radius);
            glVertex2d(x+width, y+height-radius);


            glVertex2d(x + width - radius, y+height-radius);
            glVertex2d(x + radius, y+height-radius);
            glVertex2d(x + width - radius, y+height);

            glVertex2d(x + width - radius, y+height);
            glVertex2d(x + radius, y+height-radius);
            glVertex2d(x + radius, y+height);
        }
        glEnd();

    }

}
