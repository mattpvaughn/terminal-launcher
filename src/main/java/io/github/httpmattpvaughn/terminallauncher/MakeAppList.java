package io.github.httpmattpvaughn.terminallauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by Matt on 3/5/2016.
 */
class MakeAppList extends AsyncTask<String, Void, List<ResolveInfo>> {
    private PackageManager pm;
    private List<ResolveInfo> appList;

    MakeAppList(PackageManager pm, List<ResolveInfo> appList) {
        super();
        this.pm = pm;
        this.appList = appList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List<ResolveInfo> appsList) {
        this.appList.addAll(appsList);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected List<ResolveInfo> doInBackground(String... params) {
        return Utils.makeAppList(pm);
    }
}
