package org.therapi.reha.patient.verlauf;

import java.util.Optional;

import javax.swing.ImageIcon;

import systemEinstellungen.SystemConfig;

class Icons {

    private static final ImageIcon EMPTYICON = new ImageIcon(new byte[0]) {
        @Override
        public int getIconHeight() {
            return 32;
        }

        public int getIconWidth() {
            return 32;
        };
    };

    static ImageIcon forName(String name) {
        return Optional.ofNullable(SystemConfig.hmSysIcons.get(name))
                       .orElse(EMPTYICON);

    }

}
