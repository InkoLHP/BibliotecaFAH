package com.example.bibliounifornew.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android

object SupabaseConfig {
    val client = createSupabaseClient(
        supabaseUrl = "https://iwzbzolshfjvnmwjlxkc.supabase.co",
        supabaseKey = "sb_publishable_oYHhG72n4fFSCWmsTgIiBA_OG2XHjNJ"
    ) {
        httpEngine = Android.create()
        install(Postgrest)
    }
}