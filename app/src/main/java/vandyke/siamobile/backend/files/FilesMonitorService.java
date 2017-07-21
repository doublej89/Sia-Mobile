/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.api.Renter;
import vandyke.siamobile.api.SiaRequest;
import vandyke.siamobile.backend.BaseMonitorService;

import java.util.ArrayList;

public class FilesMonitorService extends BaseMonitorService {

    private static FilesMonitorService instance;
    private ArrayList<FilesUpdateListener> listeners;

    private SiaDir rootDir;

    public void refresh() {
        System.out.println("filesmonitorservice refresh");
        refreshFiles();
    }

    public void refreshFiles() {
        rootDir = new SiaDir("root");
        Renter.files(new SiaRequest.VolleyCallback() {
            public void onSuccess(JSONObject response) {
                System.out.println(response);
                try {
                    JSONArray files = response.getJSONArray("files");
                    for (int i = 0; i < files.length(); i++) {
                        JSONObject fileJson = files.getJSONObject(i);
                        String siapath = fileJson.getString("siapath");
                        rootDir.addSiaFile(new SiaFile(fileJson), siapath.split("/"), 0);
                    }
                    rootDir.printAll(System.out, 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendFilesUpdate();
            }

            public void onError(SiaRequest.Error error) {

            }
        });
    }

    public void onCreate() {
        rootDir = new SiaDir("rootDir");
        listeners = new ArrayList<>();
        instance = this;
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public interface FilesUpdateListener {
        void onFilesUpdate(FilesMonitorService service);
    }

    public void registerListener(FilesUpdateListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(FilesUpdateListener listener) {
        listeners.remove(listener);
    }

    public void sendFilesUpdate() {
        for (FilesUpdateListener listener : listeners)
            listener.onFilesUpdate(this);
    }

    public SiaDir getRootDir() {
        return rootDir;
    }

    public static void staticRefresh() {
        if (instance != null)
            instance.refresh();
    }

    public static void staticPostRunnable() {
        if (instance != null)
            instance.postRefreshRunnable();
    }
}
