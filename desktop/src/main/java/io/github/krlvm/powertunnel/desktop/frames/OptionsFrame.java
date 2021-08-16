/*
 * This file is part of PowerTunnel.
 *
 * PowerTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.krlvm.powertunnel.desktop.frames;

import io.github.krlvm.powertunnel.configuration.ConfigurationStore;
import io.github.krlvm.powertunnel.desktop.BuildConstants;
import io.github.krlvm.powertunnel.desktop.application.DesktopApp;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.updater.UpdateNotifier;
import io.github.krlvm.powertunnel.desktop.utilities.SystemUtility;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.preferences.Preference;
import io.github.krlvm.powertunnel.preferences.PreferenceGroup;
import io.github.krlvm.powertunnel.preferences.PreferenceType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class OptionsFrame extends PreferencesFrame {

    private static List<PreferenceGroup> getPreferences() {
        final List<PreferenceGroup> groups = new ArrayList<>();
        List<Preference> preferences;

        {
            preferences = new ArrayList<>();

            if (SystemUtility.IS_WINDOWS) {
                preferences.add(new Preference(
                        "auto_proxy_setup",
                        "Auto proxy setup",
                        "Automatically setting up system proxy server configuration..\n\nWindows: Internet Explorer can be started for a few seconds to apply changes.",
                        "true",
                        PreferenceType.SWITCH,
                        null, null, null
                ));
            }

            preferences.add(new Preference(
                    "upstream_proxy_enabled",
                    "Connect via upstream proxy server",
                    "Use a proxy server to connect to the Internet",
                    "false",
                    PreferenceType.SWITCH,
                    null, null, null
            ));
            preferences.add(new Preference(
                    "upstream_proxy_host",
                    "Upstream proxy host",
                    null,
                    "",
                    PreferenceType.STRING,
                    "upstream_proxy_enabled", "true", null
            ));
            preferences.add(new Preference(
                    "upstream_proxy_port",
                    "Upstream proxy port",
                    null,
                    "8080",
                    PreferenceType.NUMBER,
                    "upstream_proxy_enabled", "true", null
            ));

            preferences.add(new Preference(
                    "upstream_proxy_auth_enabled",
                    "Upstream proxy authorization",
                    "Authenticate on upstream proxy server",
                    "false",
                    PreferenceType.SWITCH,
                    "upstream_proxy_enabled", "true", null
            ));
            preferences.add(new Preference(
                    "upstream_proxy_auth_username",
                    "Upstream proxy username",
                    null,
                    "",
                    PreferenceType.STRING,
                    "upstream_proxy_auth_enabled", "true", null
            ));
            preferences.add(new Preference(
                    "upstream_proxy_auth_password",
                    "Upstream proxy password",
                    null,
                    "",
                    PreferenceType.STRING,
                    "upstream_proxy_auth_enabled", "true", null
            ));

            groups.add(new PreferenceGroup("Proxy connection", null, preferences));
        }

        {
            preferences = new ArrayList<>();

            preferences.add(new Preference(
                    "transparent_mode",
                    "Run proxy in transparent mode (recommended)",
                    "When proxy server is in transparent mode, it declares that it does not modify requests and responses and does not set 'Via' header",
                    "true",
                    PreferenceType.SWITCH,
                    null, null, null
            ));
            preferences.add(new Preference(
                    "allow_requests_to_origin_server",
                    "Allow requests to origin server (recommended)",
                    "Experimental option, fixed many connectivity issues.",
                    "true",
                    PreferenceType.SWITCH,
                    null, null, null
            ));

            groups.add(new PreferenceGroup("Proxy settings", null, preferences));
        }

        return groups;
    }

    public OptionsFrame(ConfigurationStore configuration) {
        super("Options", "desktop-app-options",
                DesktopApp.CONFIGURATION_FILE, configuration,
                getPreferences()
        );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resetAppOptionsFrame();
            }
        });
    }

    @Override
    protected void frameInitialized() {
        final JPanel notePanel = new JPanel();
        notePanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        notePanel.add(new JLabel("<html><b>NOTE:</b> Some features have been moved to plugins,<br>so you need to open their settings for configuring</html>"));
        insertComponent(notePanel);

        if(UpdateNotifier.ENABLED) {
            final JPanel updatePanel = new JPanel(new BorderLayout());
            final JLabel updateLabel = new JLabel();
            final JButton updateButton = new JButton("Check for updates");

            updateButton.addActionListener(e -> {
                updateButton.setEnabled(false);
                setUpdateInfo(updateLabel, true);
                new Thread(() -> {
                    if(!UpdateNotifier.checkAndNotify(BuildConstants.NAME, BuildConstants.REPO, true)) {
                        UIUtility.showErrorDialog(OptionsFrame.this, "Failed to check for updates");
                    }
                    setUpdateInfo(updateLabel, false);
                    updateButton.setEnabled(true);
                }, "App Update Checker").start();
            });

            updatePanel.add(updateLabel, BorderLayout.WEST);
            updatePanel.add(updateButton, BorderLayout.EAST);
            updatePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            insertComponent(updatePanel);

            setUpdateInfo(updateLabel, false);
        }

        super.frameInitialized();
    }

    private void setUpdateInfo(JLabel label, boolean checking) {
        label.setText("<html><b>" +
                (checking ? "Checking for updates..." : (UpdateNotifier.NEW_VERSION == null ?
                        "No updates are available" : "An update is available: " + UpdateNotifier.NEW_VERSION
                )) +
                "</b></html>"
        );
    }

    @Override
    protected void onFailedToInitialize() {
        super.onFailedToInitialize();
        resetAppOptionsFrame();
    }

    private void resetAppOptionsFrame() {
        GraphicalApp.getInstance().optionsFrame = null;
    }
}
