/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The Federal Office of Administration (Bundesverwaltungsamt, BVA)
 * licenses this file to you under the Apache License, Version 2.0 (the
 * License). You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package de.bund.bva.pliscommon.sicherheit.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.bund.bva.pliscommon.aufrufkontext.AufrufKontext;
import de.bund.bva.pliscommon.sicherheit.Berechtigungsmanager;
import de.bund.bva.pliscommon.sicherheit.common.exception.AuthentifizierungTechnicalException;

/**
 * Tests für Caching
 *
 */
public class SicherheitCachingTest extends AbstractSicherheitTest {

    @Test
    public void testeAuthentifziereMitKontext() throws Exception {
        AufrufKontext aufrufKontext = this.aufrufKontextFactory.erzeugeAufrufKontext();
        aufrufKontext.setDurchfuehrenderBenutzerKennung("nutzer");
        aufrufKontext.setDurchfuehrenderSachbearbeiterName("name");
        aufrufKontext.setDurchfuehrendeBehoerde("behoerde");
        aufrufKontext.setDurchfuehrenderBenutzerPasswort("passwort");
        aufrufKontext.setRollenErmittelt(false);
        aufrufKontext.setRolle(new String[] { "Rolle_XYZ" });
        Berechtigungsmanager berechtigungsmanager =
            this.sicherheit.getBerechtigungsManagerUndAuthentifiziere(aufrufKontext);
        assertNotNull(berechtigungsmanager);
        assertEquals(1, this.testAccessManager.getCountAuthentifziere());

        berechtigungsmanager = this.sicherheit.getBerechtigungsManagerUndAuthentifiziere(aufrufKontext);
        assertNotNull(berechtigungsmanager);
        assertEquals(1, this.testAccessManager.getCountAuthentifziere());

        aufrufKontext.setKorrelationsId("NEU");
        berechtigungsmanager = this.sicherheit.getBerechtigungsManagerUndAuthentifiziere(aufrufKontext);
        assertNotNull(berechtigungsmanager);
        assertEquals(1, this.testAccessManager.getCountAuthentifziere());
        assertEquals("NEU", this.aufrufKontextVerwalter.getAufrufKontext().getKorrelationsId());

        aufrufKontext.setDurchfuehrenderBenutzerPasswort("passwort2");
        berechtigungsmanager = this.sicherheit.getBerechtigungsManagerUndAuthentifiziere(aufrufKontext);
        assertNotNull(berechtigungsmanager);
        assertEquals(2, this.testAccessManager.getCountAuthentifziere());
        assertEquals("NEU", this.aufrufKontextVerwalter.getAufrufKontext().getKorrelationsId());
    }

    @Test
    public void testeAuthentifziereMitKontextTTL() throws Exception {
        AufrufKontext aufrufKontext = this.aufrufKontextFactory.erzeugeAufrufKontext();
        aufrufKontext.setDurchfuehrenderBenutzerKennung("nutzer");
        aufrufKontext.setDurchfuehrenderSachbearbeiterName("name");
        aufrufKontext.setDurchfuehrendeBehoerde("behoerde");
        aufrufKontext.setDurchfuehrenderBenutzerPasswort("passwort");
        aufrufKontext.setRollenErmittelt(false);
        aufrufKontext.setRolle(new String[] { "Rolle_XYZ" });

        Berechtigungsmanager berechtigungsmanager =
            this.sicherheit.getBerechtigungsManagerUndAuthentifiziere(aufrufKontext);
        assertNotNull(berechtigungsmanager);
        assertEquals("Authentifiziert ohne Aufruf des AccessManagers", 1,
            this.testAccessManager.getCountAuthentifziere());

        // Warte bis der Cache nach 1 Sek abläuft
        Thread.sleep(1100);

        berechtigungsmanager = this.sicherheit.getBerechtigungsManagerUndAuthentifiziere(aufrufKontext);
        assertNotNull(berechtigungsmanager);
        assertEquals(
            "Aufruf des Accessmanagers wurde nicht registriert, obwohl der Cache bereits abgelaufen war", 2,
            this.testAccessManager.getCountAuthentifziere());

        // Warte eie halbe Sekunge - der Wert ist noch gecached
        Thread.sleep(500);

        berechtigungsmanager = this.sicherheit.getBerechtigungsManagerUndAuthentifiziere(aufrufKontext);
        assertNotNull(berechtigungsmanager);
        assertEquals("Aufruf des CacheManagers registriert, obwohl Daten noch gecached sein müssten.", 2,
            this.testAccessManager.getCountAuthentifziere());

        // Warte nochmal 600ms -> der Wert ist nicht mehr gecached
        Thread.sleep(600);

        berechtigungsmanager = this.sicherheit.getBerechtigungsManagerUndAuthentifiziere(aufrufKontext);
        assertNotNull(berechtigungsmanager);
        assertEquals(
            "Kein Aufruf registiert, obwohl Cache abgelaufen war. Vermutlich wurde ttl durch das lesen vor 600ms zurückgesetzt",
            3, this.testAccessManager.getCountAuthentifziere());

    }

    @Test
    public void testeAuthentifiziereMitParams() throws AuthentifizierungTechnicalException {
        Berechtigungsmanager berechtigungsManager =
            this.sicherheit.getBerechtigungsManagerUndAuthentifiziere("kennung", "passwort", "CERT",
                "CERT-DN", "1234567890");
        assertNotNull(berechtigungsManager);
        assertNotNull(berechtigungsManager.getRollen());
        assertTrue(berechtigungsManager.getRollen().size() > 0);
        assertEquals(1, this.testAccessManager.getCountAuthentifziere());

        berechtigungsManager =
            this.sicherheit.getBerechtigungsManagerUndAuthentifiziere("kennung", "passwort", "CERT",
                "CERT-DN", "1234567890");
        assertNotNull(berechtigungsManager);
        assertNotNull(berechtigungsManager.getRollen());
        assertTrue(berechtigungsManager.getRollen().size() > 0);

        assertEquals(1, this.testAccessManager.getCountAuthentifziere());

        this.sicherheit.getBerechtigungsManagerUndAuthentifiziere("kennung", "passwort", "CERT", "CERT-DN",
            "9999999999");
        assertEquals(1, this.testAccessManager.getCountAuthentifziere());

        this.sicherheit.getBerechtigungsManagerUndAuthentifiziere("kennung2", "passwort", "CERT", "CERT-DN",
            "1234567890");
        assertEquals(2, this.testAccessManager.getCountAuthentifziere());

    }

    @Test
    public void testeAuthentifiziereNutzer() {
        Berechtigungsmanager berechtigungsManager =
            this.sicherheit.getBerechtigungsManagerUndAuthentifiziereNutzer("kennung", "passwort", "900600",
                null, "987654321");
        assertNotNull(berechtigungsManager);
        assertNotNull(berechtigungsManager.getRollen());
        assertTrue(berechtigungsManager.getRollen().size() > 0);
        assertEquals(1, this.testAccessManager.getCountAuthentifziere());

        berechtigungsManager =
            this.sicherheit.getBerechtigungsManagerUndAuthentifiziereNutzer("kennung", "geheim12", "900600",
                null, "987654321");
        assertNotNull(berechtigungsManager);
        assertNotNull(berechtigungsManager.getRollen());
        assertTrue(berechtigungsManager.getRollen().size() > 0);
        assertEquals(2, this.testAccessManager.getCountAuthentifziere());
    }

}
