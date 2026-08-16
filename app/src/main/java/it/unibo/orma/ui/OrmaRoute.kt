package it.unibo.orma.ui

import kotlinx.serialization.Serializable

/**
 * Navigazione type-safe: ogni destinazione è un oggetto serializzabile, quindi una
 * rotta inesistente è un errore di compilazione e non di runtime.
 */
sealed interface OrmaRoute {
    @Serializable data object Home : OrmaRoute
    @Serializable data object Record : OrmaRoute
    @Serializable data object Stats : OrmaRoute
    @Serializable data object Profile : OrmaRoute
    @Serializable data object Settings : OrmaRoute

    /** Rotta dinamica: porta con sé l'id dell'escursione da mostrare. */
    @Serializable data class HikeDetail(val hikeId: Long) : OrmaRoute
}
