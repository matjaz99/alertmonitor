package si.matjazcerkvenik.alertmonitor.model;

import java.util.Random;

public class TagColors {

    private static String[] colors = {"IndianRed", "Salmon", "LightSalmon", "Pink",
            "HotPink", "PaleVioletRed", "Coral", "Tomato", "Gold", "Khaki", "DarkKhaki",
            "Plum", "Orchid", "MediumOrchid", "MediumPurple", "GreenYellow",
            "Chartreuse", "PaleGreen", "SpringGreen", "MediumSeaGreen", "YellowGreen",
            "OliveDrab", "Olive", "MediumAquamarine", "DarkCyan", "Cyan", "Aquamarine",
            "DarkTurquoise", "PaleTurquoise", "LightBlue", "SkyBlue",
            "CornflowerBlue", "Cornsilk", "BlanchedAlmond", "NavajoWhite", "BurlyWood",
            "RosyBrown", "SandyBrown", "Goldenrod", "Peru", "Chocolate", "AntiqueWhite",
            "#7AB1D3", "#F1948A", "#BB8FCE", "#7FB3D5", "#EDBB99", "#AEB6BF", "#F0B27A",
            "#85C1E9", "#76D7C4", "#73C6B6", "#82E0AA", "#F7DC6F", "#F8C471", "#BA4431",
            "#83F1D7", "#AAC6F9", "#FC9B7A", "#B393CD", "#A09FE2", "#E2E086", "#FCECC6",
            "#F9B386", "#F97583", "#B283D7", "#E0BFAA", "#DAE086", "#E286DA", "#FC99D5",
            "#C68F7F", "#F9DCF1", "#9585A3", "#BFB38F", "#AEFCE0", "#D7B2F9", "#94E9C0",
            "#8FFC9F", "#9495E0", "#95C0D3", "#CDCDD7", "#D7AEB1", "#F2B694", "#E9B388",
            "#C68F7F", "#B1C6D7", "#B1F1DA", "#DC7FF1", "#ECE07F", "#909020", "#F1F1A1"};

    private static String[] niances = {
            "7A", "F1", "7F", "A0", "DA",
            "85", "A3", "F2", "D7", "E2",
            "CD", "AE", "95", "99", "C0",
            "B1", "94", "8F", "B3", "93",
            "D7", "C6", "E0", "DC", "B2",
            "9B", "B6", "92", "86", "A7",
            "D3", "8A", "CE", "D5", "FC",
            "E9", "EC", "B6", "AA", "83",
            "F9", "BF", "88", "9F", "B1", "C6"
};

    public static String getColor(String tagName) {

        int r = new Random().nextInt(colors.length);
//        int rRed = new Random().nextInt(niances.length);
//        int rGreen = new Random().nextInt(niances.length);
//        int rBlue = new Random().nextInt(niances.length);

        String result = colors[r];
//        String result = "#" + niances[rRed] + niances[rGreen] + niances[rBlue];

        if (tagName == null) {
            return result;
        }
        if (tagName.equalsIgnoreCase(Severity.CRITICAL)) {
            return "#FF0000";
        }
        if (tagName.equalsIgnoreCase(Severity.MAJOR)) {
            return "#FF7100";
        }
        if (tagName.equalsIgnoreCase(Severity.MINOR)) {
            return "#FFC900";
        }
        if (tagName.equalsIgnoreCase(Severity.WARNING)) {
            return "#FFE800";
        }
        if (tagName.equalsIgnoreCase(Severity.INFORMATIONAL)) {
            return "#00C8FF";
        }
        if (tagName.equalsIgnoreCase(Severity.INDETERMINATE)) {
            return "#8F5CFF";
        }

        return result;

    }

}
