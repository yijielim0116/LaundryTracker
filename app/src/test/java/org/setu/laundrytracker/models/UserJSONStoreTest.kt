package org.setu.laundrytracker.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UserJSONStoreTest {

    // fresh temp folder for every test, deleted after
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var file: File

    @Before
    fun setUp() {
        file = File(tempFolder.root, "users.json")
    }

    // a new store reading the same file = opening the app again
    private fun reopen(): UserJSONStore {
        return UserJSONStore(file)
    }

    // makes a user the same way the sign up screen does
    private fun newUser(email: String, password: String): UserModel {
        val salt = Passwords.newSalt()
        return UserModel(email = email, passwordHash = Passwords.hash(password, salt), salt = salt)
    }

    @Test
    fun startsEmptyWhenThereIsNoFile() {
        assertEquals(0, reopen().findAll().size)
    }

    @Test
    fun usersAreStillThereAfterReopening() {
        val store = reopen()
        store.create(newUser("amy@example.com", "secret1"))
        store.create(newUser("bob@example.com", "secret2"))

        val all = reopen().findAll()
        assertEquals(2, all.size)
        assertEquals(1L, all[0].id)
        assertEquals(2L, all[1].id)
    }

    @Test
    fun findByEmailIgnoresCapitalsAndSpaces() {
        val store = reopen()
        store.create(newUser("amy@example.com", "secret1"))

        assertNotNull(store.findByEmail("AMY@Example.com"))
        assertNotNull(store.findByEmail("  amy@example.com "))
        assertNull(store.findByEmail("nobody@example.com"))
    }

    @Test
    fun passwordIsNotSavedAsPlainText() {
        reopen().create(newUser("amy@example.com", "secret1"))

        assertFalse(file.readText().contains("secret1"))
    }

    @Test
    fun rightPasswordMatchesAndWrongOneDoesNot() {
        reopen().create(newUser("amy@example.com", "secret1"))
        val user = reopen().findByEmail("amy@example.com")!!

        assertTrue(Passwords.matches("secret1", user))
        assertFalse(Passwords.matches("secret2", user))
        assertFalse(Passwords.matches("", user))
    }

    @Test
    fun samePasswordGetsADifferentHashForEachUser() {
        val amy = newUser("amy@example.com", "secret1")
        val bob = newUser("bob@example.com", "secret1")

        // different salts, so the hashes are different too
        assertNotEquals(amy.salt, bob.salt)
        assertNotEquals(amy.passwordHash, bob.passwordHash)
    }

    @Test
    fun brokenFileStartsEmptyInsteadOfCrashing() {
        file.writeText("this is not json")

        assertEquals(0, reopen().findAll().size)
    }
}
