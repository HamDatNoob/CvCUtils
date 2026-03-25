package org.hamdatnoob.cvcutils.util;

public class TextColor {
    public static final int tabColor = 0x7f000000;
    public static final int white = 0xffffffff;
    public static final int black = 0xff000000;
    public static final int darkBlue = 0xff0000aa;
    public static final int darkGreen = 0xff00aa00;
    public static final int darkAqua = 0xff00aaaa;
    public static final int darkRed = 0xffaa0000;
    public static final int darkPurple = 0xffaa00aa;
    public static final int gold = 0xffffaa00;
    public static final int gray = 0xffaaaaaa;
    public static final int darkGray = 0xff555555;
    public static final int blue = 0xff5555ff;
    public static final int green = 0xff55ff55;
    public static final int aqua = 0xff55ffff;
    public static final int red = 0xffff5555;
    public static final int pink = 0xffff55ff;
    public static final int yellow = 0xffffff55;

    public static String convertInt(int code){
        switch(code){
            case black: return "§0";
            case darkBlue: return "§1";
            case darkGreen: return "§2";
            case darkAqua: return "§3";
            case darkRed: return "§4";
            case darkPurple: return "§5";
            case gold: return "§6";
            case gray: return "§7";
            case darkGray: return "§8";
            case blue: return "§9";
            case green: return "§a";
            case aqua: return "§b";
            case red: return "§c";
            case pink: return "§d";
            case yellow: return "§e";
            case white: return "§f";
        }
        return null;
    }

//    TODO: ill make this if necessary
//    public static int convertCode(String code){
//
//    }
}
