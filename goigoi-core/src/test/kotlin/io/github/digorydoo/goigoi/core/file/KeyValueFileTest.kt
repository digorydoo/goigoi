package io.github.digorydoo.goigoi.core.file

import ch.digorydoo.kutils.logging.Log
import java.io.File
import java.nio.file.Files
import kotlin.test.*

internal class KeyValueFileTest {
    private lateinit var testDir: File
    private lateinit var stringsFilePath: String
    private lateinit var backupFilePath: String

    @BeforeTest
    fun beforeEach() {
        testDir = Files.createTempDirectory("key_value_file_test").toFile()
        stringsFilePath = File(testDir, "stats.dat").absolutePath
        backupFilePath = File(testDir, "stats.dat.bak").absolutePath
        Log.enabled = false
    }

    @AfterTest
    fun afterEach() {
        testDir.deleteRecursively()
        Log.enabled = true
    }

    @Test
    fun `should write and read key value pairs`() {
        val kvFile = KeyValueFile(stringsFilePath, backupFilePath)
        kvFile.open()

        kvFile.set("key1", "value1")
        kvFile.set("key2", "value2")

        assertEquals("value1", kvFile.get("key1"))
        assertEquals("value2", kvFile.get("key2"))

        kvFile.close()

        // Reopen and check persistence
        val kvFile2 = KeyValueFile(stringsFilePath, backupFilePath)
        kvFile2.open()

        assertEquals("value1", kvFile2.get("key1"))
        assertEquals("value2", kvFile2.get("key2"))

        kvFile2.close()
    }

    @Test
    fun `should handle remove and overwrite`() {
        val kvFile = KeyValueFile(stringsFilePath, backupFilePath)
        kvFile.open()

        kvFile.set("key1", "value1")
        assertEquals("value1", kvFile.get("key1"))

        kvFile.set("key1", "value1_updated")
        assertEquals("value1_updated", kvFile.get("key1"))

        kvFile.remove("key1")
        assertNull(kvFile.get("key1"))

        kvFile.close()
    }

    @Test
    fun `should create backup on open when strings file is non empty`() {
        val kvFile = KeyValueFile(stringsFilePath, backupFilePath)
        kvFile.open()
        kvFile.set("key1", "value1")
        kvFile.close()

        val backupFile = File(backupFilePath)

        if (backupFile.exists()) {
            backupFile.delete()
        }

        // Re-open should write backup because mapKeysToPos is non-empty
        val kvFile2 = KeyValueFile(stringsFilePath, backupFilePath)
        kvFile2.open()
        kvFile2.close()

        assertTrue(backupFile.exists(), "Backup file should be created on open when data exists")
        assertTrue(backupFile.length() > 0, "Backup file should not be empty")
    }

    @Test
    fun `should recover from backup if main file is corrupted`() {
        // Step 1: Create initial valid file and trigger backup
        val kvFile1 = KeyValueFile(stringsFilePath, backupFilePath)
        kvFile1.open()
        kvFile1.set("key1", "value1")
        kvFile1.close()

        // Step 2: Open again to create backup file
        val kvFile2 = KeyValueFile(stringsFilePath, backupFilePath)
        kvFile2.open()
        kvFile2.close()

        val backupFile = File(backupFilePath)
        assertTrue(backupFile.exists(), "Backup file should exist")

        // Step 3: Corrupt the main strings file
        val mainFile = File(stringsFilePath)
        mainFile.writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04))

        // Step 4: Open KeyValueFile, it should fail to parse main file and recover from backup
        val kvFile3 = KeyValueFile(stringsFilePath, backupFilePath)
        kvFile3.open()

        assertEquals("value1", kvFile3.get("key1"), "Should recover key1 value from backup")
        kvFile3.close()
    }
}
