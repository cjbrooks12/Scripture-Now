package com.caseybrooks.androidbibletools.enumeration;

import java.util.ArrayList;
import java.util.EnumSet;

/** Enumerates several English translations of the Bible according to their
 *   name and the number of chapters in that book. Using the static method
 *   parseBook will attempt to find the appropriate enumeration given a
 *   string of the version's full name or its code.
 */
public enum Version {
    //TODO: add support for all versions available on BibleStudyTools
    ASV("American Standard Version", "asv"),
    ESV("English Standard Version", "esv"),
    GWT("God's Word Translation", "gw"),
    HCSB("Holman Christian Standard Bible", "csb"),
    KJV("King James Version", "kjv"),
    MSG("The Message", "msg"),
    NASB("New American Standard Bible", "nas"),
    NIV("New International Version", "niv"),
    NIRV("New International Reader's Version", "nirv"),
    NKJV("New King James Version", "nkjv"),
    NLT("New Living Translation", "nlt"),
    NRSV("New Revised Standard Version", "nrs"),
	RSV("Revised Standard Version", "rsv"),
    YLT("Young's Literal Translation", "ylt");

    private final String name;
    private final String code;

    private Version(String name, String code) {
        this.name = name;
        this.code = code;
    }

    /** Attempt to parse a String to find the appropriate version.
     *
     * @param name String to be parsed
     * @return Version the version enum if found
     */
    public static Version parseVersion(String name) {
        for (Version version : EnumSet.allOf(Version.class)) {
            if(version.getName().toLowerCase().contains(name.toLowerCase())) return version;
            else if(version.getCode().contains(name.toLowerCase())) return version;
        }

        return null;
    }

    /** Returns a list of the name of all the available versions. Useful for
     *  displaying to the user which versions are available, or populating a
     *  spinner or dropdown menu
     *
     * @return String[] list of names of all available versions
     */
    public static String[] getAllNames() {
        ArrayList<String> versions = new ArrayList<String>();

        for (Version version : EnumSet.allOf(Version.class)) {
            versions.add(version.getName());
        }

        String[] toReturn = new String[versions.size()];
        versions.toArray(toReturn);

        return toReturn;
    }

    public static String[] getAllCodes() {
        ArrayList<String> versions = new ArrayList<String>();

        for (Version version : EnumSet.allOf(Version.class)) {
            versions.add(version.getCode());
        }

        String[] toReturn = new String[versions.size()];
        versions.toArray(toReturn);

        return toReturn;
    }

    /** Returns the name of the version, intended to be displayed to the user
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /** Returns a code for the name which is used internally to search online
     *
     * @return String
     */
    public String getCode() {
        return code;
    }
}
