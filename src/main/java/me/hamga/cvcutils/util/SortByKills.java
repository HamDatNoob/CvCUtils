package me.hamga.cvcutils.util;

import java.util.Comparator;

public class SortByKills implements Comparator<CvCPlayer> {
    @Override
    public int compare(CvCPlayer a, CvCPlayer b) {
        return b.getKills() - a.getKills();
    }
}
