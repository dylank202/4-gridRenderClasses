package csc133;

public class spot {
    // monitor width and height, in pixels. monitorW isn't used anywhere else yet but
    // i might as well put it here with monitorH so we have both in one place
    public static final int monitorW = 1920, monitorH = 1080;
    // padding / offset from the edges of the screen (assuming correct monitor wid & ht)
    public static int winPosX = 100, winPosY = 75;

    public static int winWidth = monitorW - winPosX * 2;    // window width
    public static int winHeight = monitorH - winPosY * 2;   // window height
}
