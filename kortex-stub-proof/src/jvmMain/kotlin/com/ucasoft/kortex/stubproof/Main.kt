package com.ucasoft.kortex.stubproof

import com.ucasoft.kortex.di.clientModule
import com.ucasoft.kortex.di.configModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

/**
 * Phase 0 stub proof (AGENTS.md §7). A throwaway program, not the Runtime.
 */
fun main() {
    startKoin {
        modules(configModule, clientModule)
    }

    runBlocking {
        StubProof().run()
    }
}
