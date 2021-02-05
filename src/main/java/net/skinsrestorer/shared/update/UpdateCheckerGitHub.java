/*
 * #%L
 * SkinsRestorer
 * %%
 * Copyright (C) 2021 SkinsRestorer
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package net.skinsrestorer.shared.update;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.skinsrestorer.shared.storage.Config;
import net.skinsrestorer.shared.utils.SRLogger;
import org.inventivetalent.update.spiget.UpdateCallback;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class UpdateCheckerGitHub extends UpdateChecker {
    private static final String resourceId = "SkinsRestorerX";
    private static final String RELEASES_URL_Latest = "https://api.github.com/repos/SkinsRestorer/%s/releases/latest";
    private static final String RELEASES_URL = "https://api.github.com/repos/SkinsRestorer/%s/releases";
    private final SRLogger log;
    private final String userAgent;
    private String currentVersion;
    private GitHubReleaseInfo releaseInfo;


    public UpdateCheckerGitHub(int resourceId, String currentVersion, SRLogger log, String userAgent) {
        super(resourceId, currentVersion, log, userAgent);
        this.log = log;
        this.userAgent = userAgent;
        this.currentVersion = currentVersion;
    }

    @Override
    public void checkForUpdate(final UpdateCallback callback) {

        if (Config.UPDATER_DELAY_ENABLED) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(RELEASES_URL, resourceId)).openConnection();
                connection.setRequestProperty("User-Agent", this.userAgent);
                int responsecode = connection.getResponseCode();

                if (responsecode != 200) {
                    log.logAlways(Level.WARNING, "Failed to get release info from api.github.com.");
                    return;
                }

                // Get current version number
                String currentVersionNumber = currentVersion.substring(0, currentVersion.indexOf("-"));


                // TEST
                System.out.println("currentVersionNumber = " + currentVersionNumber);
 n
            } catch (Exception ignored) {
                log.logAlways(Level.WARNING, "Failed to get release info from api.github.com.");
            }
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(RELEASES_URL_Latest, resourceId)).openConnection();
            connection.setRequestProperty("User-Agent", this.userAgent);
            int responsecode = connection.getResponseCode();

            if (responsecode != 200) {
                log.logAlways(Level.WARNING, "Failed to get release info from api.github.com.");
                return;
            }

            JsonObject apiResponse = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
            this.releaseInfo = new Gson().fromJson(apiResponse, GitHubReleaseInfo.class);

            releaseInfo.assets.forEach(gitHubAssetInfo -> {
                releaseInfo.latestDownloadURL = gitHubAssetInfo.browser_download_url;

                if (this.isVersionNewer(this.currentVersion, releaseInfo.tag_name)) {
                    callback.updateAvailable(releaseInfo.tag_name, gitHubAssetInfo.browser_download_url, true);
                } else {
                    callback.upToDate();
                }
            });

        } catch (Exception ignored) {
            log.logAlways(Level.WARNING, "Failed to get release info from api.github.com.");
        }

    }

    @Override
    public GitHubReleaseInfo getLatestResourceInfo() {
        return this.releaseInfo;
    }
}
