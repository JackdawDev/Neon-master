package dev.jackdaw1101.neon.utils;

import dev.jackdaw1101.neon.Neon;
import dev.jackdaw1101.neon.API.utilities.CC;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionHandler {

    private final Neon plugin;
    private final String serverVersion;
    private final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;
    private final String serverSoftware;
    private final boolean isPaper;
    private final boolean isSpigot;
    private final boolean isBukkit;
    private final boolean isFolia;

    private final Set<Version> supportedVersions = new HashSet<>();
    private final Set<Version> unstableVersions = new HashSet<>();
    private final Set<Version> unsupportedVersions = new HashSet<>();

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("(?:git-)?(?:Paper|Spigot|Bukkit)?[\\s-]?(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-R\\d+\\.\\d+)?.*");

    public VersionHandler(Neon plugin) {
        this.plugin = plugin;

        this.serverSoftware = detectServerSoftware();
        this.isPaper = serverSoftware.equalsIgnoreCase("Paper");
        this.isSpigot = serverSoftware.equalsIgnoreCase("Spigot") || serverSoftware.equalsIgnoreCase("Paper");
        this.isBukkit = serverSoftware.equalsIgnoreCase("Bukkit") || isSpigot;
        this.isFolia = isFolia();

        String rawVersion = Bukkit.getBukkitVersion().split("-")[0];
        this.serverVersion = rawVersion;

        int[] versionParts = parseVersion(rawVersion);
        this.majorVersion = versionParts[0];
        this.minorVersion = versionParts[1];
        this.patchVersion = versionParts[2];

        initializeVersions();
    }

    private void initializeVersions() {


        supportedVersions.add(new Version(1, 8));
        supportedVersions.add(new Version(1, 8, 3));
        supportedVersions.add(new Version(1, 8, 4));
        supportedVersions.add(new Version(1, 8, 5));
        supportedVersions.add(new Version(1, 8, 6));
        supportedVersions.add(new Version(1, 8, 7));
        supportedVersions.add(new Version(1, 8, 8));
        supportedVersions.add(new Version(1, 8, 9));

        supportedVersions.add(new Version(1, 9));
        supportedVersions.add(new Version(1, 9, 2));
        supportedVersions.add(new Version(1, 9, 4));
        supportedVersions.add(new Version(1, 9, 5));

        supportedVersions.add(new Version(1, 10));
        supportedVersions.add(new Version(1, 10, 2));

        supportedVersions.add(new Version(1, 11));
        supportedVersions.add(new Version(1, 11, 2));

        supportedVersions.add(new Version(1, 12));
        supportedVersions.add(new Version(1, 12, 1));
        supportedVersions.add(new Version(1, 12, 2));

        supportedVersions.add(new Version(1, 13));
        supportedVersions.add(new Version(1, 13, 1));
        supportedVersions.add(new Version(1, 13, 2));

        unstableVersions.add(new Version(1, 14));
        unstableVersions.add(new Version(1, 14, 1));
        unstableVersions.add(new Version(1, 14, 2));
        unstableVersions.add(new Version(1, 14, 3));
        unstableVersions.add(new Version(1, 14, 4));

        unstableVersions.add(new Version(1, 15));
        unstableVersions.add(new Version(1, 15, 1));
        unstableVersions.add(new Version(1, 15, 2));

        supportedVersions.add(new Version(1, 16, 1));
        supportedVersions.add(new Version(1, 16, 2));
        supportedVersions.add(new Version(1, 16, 3));
        supportedVersions.add(new Version(1, 16, 4));
        supportedVersions.add(new Version(1, 16, 5));

        supportedVersions.add(new Version(1, 17));
        supportedVersions.add(new Version(1, 17, 1));

        supportedVersions.add(new Version(1, 18));
        supportedVersions.add(new Version(1, 18, 1));
        supportedVersions.add(new Version(1, 18, 2));

        supportedVersions.add(new Version(1, 19));
        supportedVersions.add(new Version(1, 19, 1));
        supportedVersions.add(new Version(1, 19, 2));
        supportedVersions.add(new Version(1, 19, 3));
        supportedVersions.add(new Version(1, 19, 4));

        supportedVersions.add(new Version(1, 20));
        supportedVersions.add(new Version(1, 20, 1));
        supportedVersions.add(new Version(1, 20, 2));
        supportedVersions.add(new Version(1, 20, 3));
        supportedVersions.add(new Version(1, 20, 4));
        supportedVersions.add(new Version(1, 20, 5));
        supportedVersions.add(new Version(1, 20, 6));

        unstableVersions.add(new Version(1, 21));
        unstableVersions.add(new Version(1, 21, 1));
        unstableVersions.add(new Version(1, 21, 2));
        unstableVersions.add(new Version(1, 21, 3));
        unstableVersions.add(new Version(1, 21, 4));


        unstableVersions.add(new Version(1, 21, 5));
        unstableVersions.add(new Version(1, 21, 6));
        unstableVersions.add(new Version(1, 21, 7));
        unstableVersions.add(new Version(1, 21, 8));
        unstableVersions.add(new Version(1, 21, 9));
        unstableVersions.add(new Version(1, 21, 10));
        unstableVersions.add(new Version(1, 21, 11));


    }

    private int[] parseVersion(String version) {
        int major = 1;
        int minor = 0;
        int patch = 0;

        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            try {
                major = Integer.parseInt(matcher.group(1));
                minor = Integer.parseInt(matcher.group(2));
                if (matcher.group(3) != null) {
                    patch = Integer.parseInt(matcher.group(3));
                }
            } catch (NumberFormatException e) {

            }
        } else {

            String[] parts = version.split("\\.");
            if (parts.length >= 1) major = Integer.parseInt(parts[0]);
            if (parts.length >= 2) minor = Integer.parseInt(parts[1]);
            if (parts.length >= 3) patch = Integer.parseInt(parts[2]);
        }

        return new int[]{major, minor, patch};
    }

    private String detectServerSoftware() {
        try {

            Class.forName("com.destroystokyo.paper.PaperConfig");
            return "Paper";
        } catch (ClassNotFoundException e) {
            try {

                Class.forName("org.spigotmc.SpigotConfig");
                return "Spigot";
            } catch (ClassNotFoundException ex) {

                return "Bukkit";
            }
        }
    }


    /**
     * Check if the current server version is supported
     */
    public boolean isVersionSupported() {
        Version currentVersion = new Version(majorVersion, minorVersion, patchVersion);
        return supportedVersions.contains(currentVersion);
    }

    /**
     * Check if the current server version is unstable
     */
    public boolean isVersionUnstable() {
        Version currentVersion = new Version(majorVersion, minorVersion, patchVersion);
        return unstableVersions.contains(currentVersion);
    }

    /**
     * Check if the current server version doesn't exist or is completely unsupported
     */
    public boolean isVersionNonExistent() {
        Version currentVersion = new Version(majorVersion, minorVersion, patchVersion);
        return !supportedVersions.contains(currentVersion) && !unstableVersions.contains(currentVersion);
    }

    /**
     * Get the status of the current version
     */
    public VersionStatus getVersionStatus() {
        if (isVersionSupported()) return VersionStatus.SUPPORTED;
        if (isVersionUnstable()) return VersionStatus.UNSTABLE;
        return VersionStatus.UNSUPPORTED;
    }

    /**
     * Run version check and handle accordingly
     * @return true if plugin should continue loading, false if should disable
     */
    public boolean performVersionCheck() {
        VersionStatus status = getVersionStatus();
        boolean softwareSupported = true;

        String header = "&8&m+----------------------------------------------+";

        DebugUtil.debug(CC.translate(header));

        switch (status) {
            case SUPPORTED:
                DebugUtil.debug(CC.translate("&a&l✓ FULLY SUPPORTED VERSION ✓"));
                DebugUtil.debug(CC.translate("&7Version: &a" + getFullVersionString()));
                DebugUtil.debug(CC.translate("&7Software: &a" + serverSoftware));
                if (isFolia) {
                    DebugUtil.debug(CC.translate("&7Folia: &aEnabled"));
                }
                DebugUtil.debug(CC.translate(header));
                return true;

            case UNSTABLE:
                DebugUtil.debug(CC.translate("&e&l⚠ UNSTABLE VERSION ⚠"));
                DebugUtil.debug(CC.translate("&7Version: &e" + getFullVersionString()));
                DebugUtil.debug(CC.translate("&7Software: &a" + serverSoftware));
                DebugUtil.debug(CC.translate("&eThis version may work but is not fully tested."));
                DebugUtil.debug(CC.translate("&eReport any issues to the Jackdaw1101."));
                if (isFolia) {
                    DebugUtil.debug(CC.translate("&7Folia: &aEnabled"));
                }
                DebugUtil.debug(CC.translate(header));
                return true;

            case UNSUPPORTED:
                DebugUtil.debug(CC.translate("&c&l✘ UNSUPPORTED VERSION ✘"));
                DebugUtil.debug(CC.translate("&7Version: &c" + getFullVersionString()));
                DebugUtil.debug(CC.translate("&7Software: &a" + serverSoftware));
                DebugUtil.debug(CC.translate("&cThis version does not exist or is not supported!"));
                DebugUtil.debug(CC.translate("&cIf you think this is a problem report it to Jackdaw1101!."));

                DebugUtil.debug(CC.translate("&7Supported versions:"));
                displayVersions(supportedVersions, "&a");

                DebugUtil.debug(CC.translate("&7Unstable versions (may work):"));
                displayVersions(unstableVersions, "&e");

                DebugUtil.debug(CC.translate(header));

                return false;
        }

        return true;
    }

    private void displayVersions(Set<Version> versions, String color) {
        List<String> versionStrings = new ArrayList<>();
        for (Version v : versions) {
            versionStrings.add(color + v.toString());
        }

        for (int i = 0; i < versionStrings.size(); i += 6) {
            int end = Math.min(i + 6, versionStrings.size());
            String line = String.join("&7, ", versionStrings.subList(i, end));
            DebugUtil.debug(CC.translate("  " + line));
        }
    }

    /**
     * Get full version string including patch
     */
    public String getFullVersionString() {
        if (patchVersion > 0) {
            return majorVersion + "." + minorVersion + "." + patchVersion;
        } else {
            return majorVersion + "." + minorVersion;
        }
    }

    /**
     * Get major version (e.g., 1)
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Get minor version (e.g., 21)
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * Get patch version (e.g., 1)
     */
    public int getPatchVersion() {
        return patchVersion;
    }

    /**
     * Get server software name
     */
    public String getServerSoftware() {
        return serverSoftware;
    }

    /**
     * Check if running on Paper
     */
    public boolean isPaper() {
        return isPaper;
    }

    /**
     * Check if running on Spigot
     */
    public boolean isSpigot() {
        return isSpigot;
    }

    /**
     * Check if running on Bukkit
     */
    public boolean isBukkit() {
        return isBukkit;
    }

    /**
     * Check if running on Folia
     */
    public boolean isFolia() {
        return isFolia;
    }

    /**
     * Check if the server version is at least the specified version
     */
    public boolean isAtLeast(int major, int minor) {
        if (majorVersion > major) return true;
        if (majorVersion < major) return false;
        return minorVersion >= minor;
    }

    /**
     * Check if the server version is at least the specified version with patch
     */
    public boolean isAtLeast(int major, int minor, int patch) {
        if (majorVersion > major) return true;
        if (majorVersion < major) return false;
        if (minorVersion > minor) return true;
        if (minorVersion < minor) return false;
        return patchVersion >= patch;
    }

    /**
     * Check if the server version is between two versions (inclusive)
     */
    public boolean isBetween(int major1, int minor1, int major2, int minor2) {
        if (majorVersion < major1) return false;
        if (majorVersion > major2) return false;
        if (majorVersion == major1 && minorVersion < minor1) return false;
        if (majorVersion == major2 && minorVersion > minor2) return false;
        return true;
    }

    /**
     * Version enum for status
     */
    public enum VersionStatus {
        SUPPORTED,
        UNSTABLE,
        UNSUPPORTED
    }

    /**
     * Inner class representing a version
     */
    private static class Version {
        private final int major;
        private final int minor;
        private final int patch;

        public Version(int major, int minor) {
            this(major, minor, 0);
        }

        public Version(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Version version = (Version) obj;
            return major == version.major &&
                    minor == version.minor &&
                    patch == version.patch;
        }

        @Override
        public int hashCode() {
            return Objects.hash(major, minor, patch);
        }

        @Override
        public String toString() {
            if (patch > 0) {
                return major + "." + minor + "." + patch;
            } else {
                return major + "." + minor;
            }
        }
    }
}
