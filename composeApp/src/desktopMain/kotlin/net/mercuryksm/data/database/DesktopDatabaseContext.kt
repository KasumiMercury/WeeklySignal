package net.mercuryksm.data.database

actual fun getDatabaseContext(): Any {
    return Unit // Desktop doesn't need context
}