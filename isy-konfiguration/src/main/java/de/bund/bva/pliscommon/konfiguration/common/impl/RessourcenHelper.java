package de.bund.bva.pliscommon.konfiguration.common.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import de.bund.bva.pliscommon.konfiguration.common.exception.KonfigurationDateiException;
import de.bund.bva.pliscommon.konfiguration.common.konstanten.NachrichtenSchluessel;

/**
 * Diese Klasse erleichtert den Umgang mit Ordnern, die Property-Dateien enthalten.
 */
class RessourcenHelper {

    /**
     * Sucht alle Properties-Dateien in einem bestimmten Ordner und liefert den relativen Pfad zu den Dateien
     * zurück. Die Property-Dateien müssen mit '.properties' enden, alle anderen Dateien werden ignoriert.
     * @param ordnerPfad
     *            Pfad zu dem Ordner, in dem nach Properties-Dateien gesucht werden sollen.
     * @return Das Set mit den Pfaden zu allen Properties-Dateien.
     */
    public static Set<String> ladePropertiesAusOrdner(String ordnerPfad) {
        Set<String> allePropertiesPfade = new HashSet<String>();
        URI ordnerUri = getAbsoluterPfad(ordnerPfad);
        File ordner = new File(ordnerUri);
        File[] alleProperties = ordner.listFiles();
        for (File property : alleProperties) {
            String relativerPfad = ordnerPfad.concat(property.getName());
            if (relativerPfad.endsWith(".properties")) {
                allePropertiesPfade.add(relativerPfad);
            }
        }

        return allePropertiesPfade;
    }

    /**
     * Erzeugt den absoluten Pfad einer Datei die im Classpath abgelegt ist. Der Pfad wird per
     * {@link Class#getResource(String)} geladen.
     * @param relativerPfad
     *            Pfad zu einer Datei die im Classpath liegt.
     * @throws KonfigurationDateiException
     *             Wenn die Datei nicht geladen werden kann.
     * @return Absoluter Pfad, der aus dem relativen Pfad erzeugt wurde.
     */
    public static URI getAbsoluterPfad(String relativerPfad) {
        URL url = RessourcenHelper.class.getResource(relativerPfad);
        if (url == null) {
            throw new KonfigurationDateiException(NachrichtenSchluessel.ERR_DATEI_NICHT_GEFUNDEN,
                relativerPfad);
        }

        URI absoluterPfad;
        try {
            absoluterPfad = url.toURI();
        } catch (URISyntaxException ex) {
            throw new KonfigurationDateiException(NachrichtenSchluessel.ERR_DATEI_LESEN, ex, relativerPfad);
        }
        return absoluterPfad;
    }

    /**
     * Prüft, ob es sich bei dem angegebenen Pfad zu einer Ressource um einen Ordner oder eine Datei handelt.
     * @param relativerPfad
     *            Relativer Pfad zu einer Ressource.
     * @return true wenn die Ressource ein Ordner ist, false wenn es eine Datei ist.
     */
    public static boolean istOrdner(String relativerPfad) {
        URI absoluterPfad = getAbsoluterPfad(relativerPfad);
        return Files.isDirectory(Paths.get(absoluterPfad));
    }
}
