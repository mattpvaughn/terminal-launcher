package io.github.httpmattpvaughn.terminallauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * From Inkplayer Music Player by Matt
 */

public class Utils {
    static List<ResolveInfo> makeAppList(PackageManager packageManager) {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        return packageManager.queryIntentActivities(i, 0);
    }
}
