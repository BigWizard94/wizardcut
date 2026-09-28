package info.bigwizardmedia.wizardcut.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivityObserverTest {

    @Test
    fun missingCapabilitiesAreOffline() {
        assertFalse(ConnectivityObserver.hasValidatedInternet(null))
    }

    @Test
    fun internetWithoutValidationIsOffline() {
        assertFalse(
            ConnectivityObserver.hasValidatedInternet(
                hasInternetCapability = true,
                hasValidatedCapability = false,
            )
        )
    }

    @Test
    fun validatedInternetIsOnline() {
        assertTrue(
            ConnectivityObserver.hasValidatedInternet(
                hasInternetCapability = true,
                hasValidatedCapability = true,
            )
        )
    }
}
