/*
 Copyright (c) 2026, Take Some

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.sky.textures;

import com.jme3.math.FastMath;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.math.MyMath;
import jme3utilities.sky.Constants;

/**
 * Reader for the ASCII Yale Bright Star Catalog.
 *
 * @author Take Some
 */
final class BrightStarCatalogReader {

    /**
     * Mutable counters gathered while reading a catalog.
     */
    final private static class CatalogStats {
        /** Number of duplicate entries. */
        int duplicateEntries;
        /** Number of missed entries. */
        int missedEntries;
        /** Next expected entry id. */
        int nextEntry = 1;
        /** Number of catalog entries read. */
        int readEntries;
        /** Number of skipped entries. */
        int skippedEntries;
    }

    /**
     * Exception for unexpected invalid data in a catalog entry.
     */
    final private static class InvalidEntryException extends Exception {
        /** serialization identifier. */
        final static long serialVersionUID = 1L;

        /**
         * Instantiate the exception.
         *
         * @param message descriptive text
         */
        InvalidEntryException(String message) {
            super(message);
        }
    }

    /**
     * Exception for an invalid apparent magnitude in a catalog entry.
     */
    final private static class InvalidMagnitudeException extends Exception {
        /** serialization identifier. */
        final static long serialVersionUID = 1L;
    }

    /** Expected id of the last entry in the catalog. */
    final private static int lastEntryExpected = 9_110;
    /** Number of degrees from equator to pole. */
    final private static int maxDeclination = 90;
    /** Maximum apparent magnitude of all stars in the catalog. */
    final private static float maxMagnitude = 7.96f;
    /** Number of minutes in an hour or degree. */
    final private static int maxMinutes = 60;
    /** Number of seconds in a minute. */
    final private static int maxSeconds = 60;
    /** Minimum apparent magnitude of all stars in the catalog. */
    final private static float minMagnitude = -1.47f;
    /** Earth's rate of rotation, in radians per sidereal hour. */
    final private static float radiansPerHour
            = FastMath.TWO_PI / Constants.hoursPerDay;
    /** Message logger for this class. */
    final private static Logger logger
            = Logger.getLogger(BrightStarCatalogReader.class.getName());

    /**
     * Hidden constructor.
     */
    private BrightStarCatalogReader() {
        // do nothing
    }

    /**
     * Read the star catalog and collect valid stars.
     *
     * @param catalogFilePath filesystem path to the catalog file (not null)
     * @return new collection of valid stars
     */
    static Collection<Star> read(String catalogFilePath) {
        Collection<Star> result = new TreeSet<>();
        File catalogFile = new File(catalogFilePath);
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(catalogFile);
            bufferedReader = new BufferedReader(fileReader);
            read(bufferedReader, catalogFilePath, result);
        } catch (FileNotFoundException exception) {
            logger.log(Level.SEVERE, "unable to open {0}",
                    MyString.quote(catalogFilePath));
            throw new RuntimeException(exception);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "unable to read {0}",
                    MyString.quote(catalogFilePath));
        } catch (InvalidEntryException exception) {
            logger.log(Level.SEVERE, "", exception);
        } finally {
            close(fileReader, bufferedReader, catalogFilePath);
        }

        return result;
    }

    /**
     * Close catalog readers.
     *
     * @param fileReader file reader, or null
     * @param bufferedReader buffered reader, or null
     * @param catalogFilePath catalog path for logging (not null)
     */
    private static void close(FileReader fileReader,
            BufferedReader bufferedReader, String catalogFilePath) {
        try {
            if (fileReader != null) {
                fileReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException exception) {
            logger.log(Level.WARNING, "unable to close {0}",
                    MyString.quote(catalogFilePath));
        }
    }

    /**
     * Extract a star's declination from a catalog entry.
     *
     * @param line text line read from the catalog (not null)
     * @return angle north of the celestial equator, in degrees
     */
    private static float declination(String line)
            throws InvalidEntryException {
        assert line != null;

        String dd = line.substring(83, 86);
        String mm = line.substring(86, 88);
        String ss = line.substring(88, 90);
        logger.log(Level.FINE, "{0}d {1}m {2}s", new Object[]{dd, mm, ss});

        int degrees = Integer.parseInt(dd);
        if (degrees < -maxDeclination || degrees > maxDeclination) {
            throw new InvalidEntryException(
                    "dec degrees should be between -90 and 90, inclusive");
        }
        int minutes = Integer.parseInt(mm);
        if (minutes < 0 || minutes >= maxMinutes) {
            throw new InvalidEntryException(
                    "dec minutes should be between 0 and 59, inclusive");
        }
        float seconds = Float.parseFloat(ss);
        if (seconds < 0f || seconds >= maxSeconds) {
            throw new InvalidEntryException(
                    "dec seconds should be between 0 and 59, inclusive");
        }

        float result;
        if (degrees > 0) {
            result = degrees + minutes / 60f + seconds / 3600f;
        } else {
            result = degrees - minutes / 60f - seconds / 3600f;
        }

        assert result >= -maxDeclination : result;
        assert result <= maxDeclination : result;
        logger.log(Level.FINE, "result = {0}", result);
        return result;
    }

    /**
     * Read catalog lines and build up the collection of stars.
     *
     * @param bufferedReader reader to use (not null)
     * @param catalogFilePath catalog path for logging (not null)
     * @param result collection to populate (not null)
     */
    private static void read(BufferedReader bufferedReader,
            String catalogFilePath, Collection<Star> result)
            throws IOException, InvalidEntryException {
        assert bufferedReader != null;
        assert result != null;

        CatalogStats stats = new CatalogStats();
        for (;;) {
            String textLine = bufferedReader.readLine();
            if (textLine == null) {
                break;
            }
            logger.log(Level.FINE, "{0}", textLine);
            if (textLine.length() < 5) {
                continue;
            }
            String actualPrefix = textLine.substring(0, 4);
            if (!actualPrefix.matches("[ ]*[0-9]+")) {
                continue;
            }
            ++stats.readEntries;

            int actualEntry = Integer.parseInt(actualPrefix.trim());
            if (actualEntry > stats.nextEntry) {
                logger.log(Level.FINE, "missed entries #{0} through #{1}",
                        new Object[]{stats.nextEntry, actualEntry - 1});
                stats.nextEntry = actualEntry;
                stats.missedEntries += actualEntry - stats.nextEntry;

            } else if (actualEntry < stats.nextEntry) {
                logger.log(Level.WARNING,
                        "skipped entry due to duplicate id #{0}",
                        actualEntry);
                ++stats.skippedEntries;
                continue;
            }

            assert actualEntry == stats.nextEntry : stats.nextEntry;
            Star star = null;
            try {
                star = readStar(textLine, stats.nextEntry);

            } catch (InvalidMagnitudeException exception) {
                logger.log(Level.FINE,
                        "skipped entry #{0} due to invalid magnitude",
                        stats.nextEntry);
                ++stats.skippedEntries;
            }
            if (star != null) {
                if (result.contains(star)) {
                    logger.log(Level.FINE, "entry #{0} is a duplicate",
                            stats.nextEntry);
                    ++stats.duplicateEntries;
                } else {
                    boolean success = result.add(star);
                    assert success : stats.nextEntry;
                }
            }
            ++stats.nextEntry;
        }

        logStats(catalogFilePath, stats, result.size());
    }

    /**
     * Construct a new star based on a catalog entry.
     *
     * @param textLine line of text read from the catalog (not null)
     * @param entryId entry id (&ge;1)
     * @return new star
     */
    private static Star readStar(String textLine, int entryId)
            throws InvalidEntryException, InvalidMagnitudeException {
        assert textLine != null;
        assert entryId >= 1 : entryId;

        if (textLine.length() < 107) {
            throw new InvalidEntryException("catalog entry is too short");
        }
        String magnitudeText = textLine.substring(102, 107);
        logger.log(Level.FINE, "mag={0}", magnitudeText);

        if (magnitudeText.equals("     ")) {
            throw new InvalidMagnitudeException();
        }
        float apparentMagnitude;
        try {
            apparentMagnitude = Float.parseFloat(magnitudeText);
        } catch (NumberFormatException exception) {
            logger.log(Level.WARNING, "entry #{0} has invalid magnitude {1}",
                    new Object[]{entryId, MyString.quote(magnitudeText)});
            throw new InvalidMagnitudeException();
        }
        if (apparentMagnitude < minMagnitude
                || apparentMagnitude > maxMagnitude) {
            logger.log(Level.WARNING, "entry #{0} has invalid magnitude {1}",
                    new Object[]{entryId, MyString.quote(magnitudeText)});
            throw new InvalidMagnitudeException();
        }

        float declinationDegrees = declination(textLine);
        float declination = MyMath.toRadians(declinationDegrees);
        float rightAscension = rightAscensionHours(textLine) * radiansPerHour;

        Star result = new Star(rightAscension, declination, apparentMagnitude);

        return result;
    }

    /**
     * Log catalog read statistics.
     *
     * @param catalogFilePath catalog path (not null)
     * @param stats catalog counters (not null)
     * @param collectedStars number of collected stars
     */
    private static void logStats(String catalogFilePath, CatalogStats stats,
            int collectedStars) {
        int lastEntryRead = stats.nextEntry - 1;
        if (lastEntryRead != lastEntryExpected) {
            logger.log(Level.WARNING,
                    "expected last entry to be #{0} but it was actually #{1}",
                    new Object[]{lastEntryExpected, lastEntryRead});
        }
        if (stats.missedEntries > 0) {
            logger.log(Level.WARNING, "missed {0} entries",
                    stats.missedEntries);
        }
        logger.log(Level.INFO, "read {0} catalog entries from {1}",
                new Object[]{stats.readEntries, catalogFilePath});
        if (stats.duplicateEntries > 0) {
            logger.log(Level.WARNING, "{0} duplicate entries",
                    stats.duplicateEntries);
        }
        if (stats.skippedEntries > 0) {
            logger.log(Level.WARNING, "{0} entries skipped",
                    stats.skippedEntries);
        }
        logger.log(Level.INFO, "collected {0} stars", collectedStars);
    }

    /**
     * Extract a star's right ascension from a catalog entry.
     *
     * @param line text line read from the catalog (not null)
     * @return angle east of the March equinox, in hours
     */
    private static float rightAscensionHours(String line)
            throws InvalidEntryException {
        assert line != null;

        String hh = line.substring(75, 77);
        String mm = line.substring(77, 79);
        String ss = line.substring(79, 83);
        logger.log(Level.FINE, "{0}:{1}:{2}", new Object[]{hh, mm, ss});

        int hours = Integer.parseInt(hh);
        if (hours < 0 || hours >= Constants.hoursPerDay) {
            throw new InvalidEntryException(
                    "RA hours should be between 0 and 23, inclusive");
        }
        int minutes = Integer.parseInt(mm);
        if (minutes < 0 || minutes >= maxMinutes) {
            throw new InvalidEntryException(
                    "RA minutes should be between 0 and 59, inclusive");
        }
        float seconds = Float.parseFloat(ss);
        if (seconds < 0f || seconds >= maxSeconds) {
            throw new InvalidEntryException(
                    "RA seconds should be between 0 and 59, inclusive");
        }

        float result = hours + minutes / 60f + seconds / 3600f;

        assert result >= 0f : result;
        assert result < Constants.hoursPerDay : result;
        logger.log(Level.FINE, "result = {0}", result);
        return result;
    }
}
