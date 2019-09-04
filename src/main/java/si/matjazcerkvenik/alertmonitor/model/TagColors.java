package si.matjazcerkvenik.alertmonitor.model;

import java.util.Random;

public class TagColors {

    private static String[] colors = {"violet", "#7AB1D3", "#F1948A", "#BB8FCE", "#7FB3D5",
            "#85C1E9", "#76D7C4", "#73C6B6", "#82E0AA", "#F7DC6F", "#F8C471", "#F0B27A",
            "#EDBB99", "#AEB6BF",
            "#83F1D7", "#AAC6F9", "#FC9B7A", "#B393CD", "#A09FE2", "#E2E086", "#FCECC6",
            "#F9B386", "#F97583", "#B283D7", "#E0BFAA", "#DAE086", "#E286DA", "#FC99D5",
            "#C68F7F"};

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
        int rRed = new Random().nextInt(niances.length);
        int rGreen = new Random().nextInt(niances.length);
        int rBlue = new Random().nextInt(niances.length);

        //String result = colors[r];
        String result = "#" + niances[rRed] + niances[rGreen] + niances[rBlue];

        if (tagName == null) {
            return result;
        }
        if (tagName.equals("critical")) {
            return "#FF0000";
        }
        if (tagName.equals("major")) {
            return "#FF7300";
        }
        if (tagName.equals("minor")) {
            return "#FFC700";
        }
        if (tagName.equals("warning")) {
            return "#FFE800";
        }

        return result;

    }

}
