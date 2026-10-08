package com.example.privacyshieldai

import com.example.privacyshieldai.data.CatalogParser
import com.example.privacyshieldai.data.Mechanism
import com.example.privacyshieldai.data.PlatformCatalog
import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Valida el catálogo incluido en la app. Falla si alguien rompe plataformas.json. */
class CatalogTest {

    // Los tests unitarios se ejecutan con el módulo app como directorio de trabajo.
    private val catalog: PlatformCatalog =
        CatalogParser.parse(File("src/main/assets/plataformas.json").readText(Charsets.UTF_8))

    @Test
    fun catalogHasTenPlatforms() {
        assertEquals(10, catalog.platforms.size)
    }

    @Test
    fun idsAreUnique() {
        val ids = catalog.platforms.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun everyPlatformHasStepsAndValidDate() {
        catalog.platforms.forEach { p ->
            assertTrue("${p.id}: sin pasos", p.steps.isNotEmpty())
            LocalDate.parse(p.updatedOn)
        }
    }

    @Test
    fun linksAreHttps() {
        catalog.platforms.forEach { p ->
            listOfNotNull(p.url, p.extraUrl, p.sourceUrl).forEach { url ->
                assertTrue("${p.id}: $url no es https", url.startsWith("https://"))
            }
        }
    }

    @Test
    fun mechanismHasItsAction() {
        catalog.platforms.forEach { p ->
            when (p.mechanism) {
                Mechanism.TOGGLE, Mechanism.FORM -> assertNotNull("${p.id}: falta url", p.url)
                Mechanism.REQUEST -> assertNotNull("${p.id}: falta email", p.email)
                Mechanism.NONE -> Unit
            }
        }
    }

    @Test
    fun catalogPackagesAreDeclaredInManifestQueries() {
        // Sin <package> en <queries>, Android 11+ oculta la app y se mostraría como no instalada.
        val manifest = File("src/main/AndroidManifest.xml").readText(Charsets.UTF_8)
        val declared = Regex("""<package\s+android:name="([^"]+)"""").findAll(manifest)
            .map { it.groupValues[1] }
            .toSet()
        val inCatalog = catalog.platforms.flatMap { it.packages }.toSet()
        assertEquals("Paquetes del catálogo y del manifest no coinciden", inCatalog, declared)
    }

    @Test
    fun everyPlatformHasAtLeastOnePackage() {
        catalog.platforms.forEach { p ->
            assertTrue("${p.id}: sin paquetes Android", p.packages.isNotEmpty())
        }
    }

    @Test
    fun roundTripKeepsData() {
        val again = CatalogParser.parse(CatalogParser.serialize(catalog))
        assertEquals(catalog, again)
    }

    @Test
    fun newerCatalogIsDetected() {
        val older = catalog.copy(dataVersion = "2026-01-01")
        assertTrue(CatalogParser.isNewer(catalog, older))
        assertFalse(CatalogParser.isNewer(older, catalog))
        assertFalse(CatalogParser.isNewer(catalog, catalog))
        assertTrue(CatalogParser.isNewer(catalog, null))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedSchemaIsRejected() {
        CatalogParser.parse("""{"schemaVersion": 99, "dataVersion": "2026-10-07", "platforms": []}""")
    }

    @Test
    fun unknownFieldsAreIgnored() {
        val parsed = CatalogParser.parse(
            """{"schemaVersion": 1, "dataVersion": "2026-10-07", "futureField": true, "platforms": []}"""
        )
        assertTrue(parsed.platforms.isEmpty())
    }
}
