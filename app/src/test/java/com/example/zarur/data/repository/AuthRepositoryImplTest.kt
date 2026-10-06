package com.example.zarur.data.repository

import android.content.Context
import com.example.zarur.data.local.AppPreferences
import com.example.zarur.domain.model.Resource
import com.example.zarur.presentation.service.WelcomeNotificationScheduler
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private val context: Context = mockk(relaxed = true)
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val appPreferences: AppPreferences = mockk(relaxed = true)
    private val collectionReference: CollectionReference = mockk(relaxed = true)
    private val documentReference: DocumentReference = mockk(relaxed = true)

    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        mockkStatic("com.google.firebase.auth.GoogleAuthProvider")
        mockkObject(WelcomeNotificationScheduler)

        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(any()) } returns documentReference
        every { WelcomeNotificationScheduler.scheduleWelcomeNotification(any(), any()) } returns Unit

        repository = AuthRepositoryImpl(context, firebaseAuth, firestore, appPreferences)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSignOut() {
        repository.signOut()
        verify { firebaseAuth.signOut() }
    }

    @Test
    fun testGetCurrentUserId() {
        val user: FirebaseUser = mockk()
        every { user.uid } returns "u123"
        every { firebaseAuth.currentUser } returns user

        assertEquals("u123", repository.getCurrentUserId())
    }

    @Test
    fun testGetCurrentUser_nullUser() = runTest {
        every { firebaseAuth.currentUser } returns null

        val emissions = repository.getCurrentUser().toList()
        assertEquals(1, emissions.size)
        assertTrue(emissions[0] is Resource.Success)
        assertNull((emissions[0] as Resource.Success).data)
    }

    @Test
    fun testGetCurrentUser_success() = runTest {
        val user: FirebaseUser = mockk()
        every { user.uid } returns "u123"
        every { firebaseAuth.currentUser } returns user

        val docSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val task: Task<DocumentSnapshot> = mockk()
        coEvery { task.await() } returns docSnapshot
        every { documentReference.get() } returns task
        every { docSnapshot.exists() } returns true
        every { docSnapshot.getString("fullName") } returns "Test User"
        every { docSnapshot.getString("email") } returns "test@example.com"

        val emissions = repository.getCurrentUser().toList()
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val resultUser = (emissions[1] as Resource.Success).data
        assertNotNull(resultUser)
        assertEquals("u123", resultUser?.id)
        assertEquals("Test User", resultUser?.fullName)
        assertEquals("test@example.com", resultUser?.email)
    }

    @Test
    fun testSignIn_success() = runTest {
        val authResult: AuthResult = mockk(relaxed = true)
        val firebaseUser: FirebaseUser = mockk(relaxed = true)
        every { firebaseUser.uid } returns "u123"
        every { authResult.user } returns firebaseUser

        val authTask: Task<AuthResult> = mockk()
        coEvery { authTask.await() } returns authResult
        every { firebaseAuth.signInWithEmailAndPassword("test@example.com", "password123") } returns authTask

        val docSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val docTask: Task<DocumentSnapshot> = mockk()
        coEvery { docTask.await() } returns docSnapshot
        every { documentReference.get() } returns docTask
        every { docSnapshot.exists() } returns true
        every { docSnapshot.getString("fullName") } returns "Test User"
        every { docSnapshot.getString("email") } returns "test@example.com"

        val emissions = repository.signIn("test@example.com", "password123").toList()
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val user = (emissions[1] as Resource.Success).data
        assertEquals("u123", user.id)
        assertEquals("test@example.com", user.email)
    }

    @Test
    fun testSignIn_error() = runTest {
        val authTask: Task<AuthResult> = mockk()
        coEvery { authTask.await() } throws Exception("Invalid credentials")
        every { firebaseAuth.signInWithEmailAndPassword("test@example.com", "wrong") } returns authTask

        val emissions = repository.signIn("test@example.com", "wrong").toList()
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
        assertEquals("Email yoki parol noto'g'ri kiritildi.", (emissions[1] as Resource.Error).message)
    }

    @Test
    fun testSignUp_success() = runTest {
        val authResult: AuthResult = mockk(relaxed = true)
        val firebaseUser: FirebaseUser = mockk(relaxed = true)
        every { firebaseUser.uid } returns "u123"
        every { authResult.user } returns firebaseUser

        val authTask: Task<AuthResult> = mockk()
        coEvery { authTask.await() } returns authResult
        every { firebaseAuth.createUserWithEmailAndPassword("test@example.com", "password123") } returns authTask

        val setVoidTask: Task<Void> = mockk(relaxed = true)
        coEvery { setVoidTask.await() } returns mockk<Void>()
        every { documentReference.set(any()) } returns setVoidTask

        val emissions = repository.signUp("test@example.com", "password123", "New User").toList()
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val user = (emissions[1] as Resource.Success).data
        assertEquals("u123", user.id)
        assertEquals("New User", user.fullName)
        assertEquals("test@example.com", user.email)
    }

    @Test
    fun testSignUp_error() = runTest {
        val authTask: Task<AuthResult> = mockk()
        coEvery { authTask.await() } throws Exception("User already exists")
        every { firebaseAuth.createUserWithEmailAndPassword("test@example.com", "password123") } returns authTask

        val emissions = repository.signUp("test@example.com", "password123", "New User").toList()
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
        assertEquals("User already exists", (emissions[1] as Resource.Error).message)
    }

    @Test
    fun testSignInWithGoogle_success() = runTest {
        val credential: AuthCredential = mockk()
        every { GoogleAuthProvider.getCredential("id_token", null) } returns credential

        val authResult: AuthResult = mockk(relaxed = true)
        val firebaseUser: FirebaseUser = mockk(relaxed = true)
        every { firebaseUser.uid } returns "google_u123"
        every { firebaseUser.displayName } returns "Google User"
        every { firebaseUser.email } returns "google@example.com"
        every { authResult.user } returns firebaseUser

        val authTask: Task<AuthResult> = mockk()
        coEvery { authTask.await() } returns authResult
        every { firebaseAuth.signInWithCredential(credential) } returns authTask

        val docSnapshot: DocumentSnapshot = mockk(relaxed = true)
        val docTask: Task<DocumentSnapshot> = mockk()
        coEvery { docTask.await() } returns docSnapshot
        every { documentReference.get() } returns docTask
        every { docSnapshot.exists() } returns false

        val setVoidTask: Task<Void> = mockk(relaxed = true)
        coEvery { setVoidTask.await() } returns mockk<Void>()
        every { documentReference.set(any()) } returns setVoidTask

        val emissions = repository.signInWithGoogle("id_token").toList()
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)

        val user = (emissions[1] as Resource.Success).data
        assertEquals("google_u123", user.id)
        assertEquals("google@example.com", user.email)
    }
}

