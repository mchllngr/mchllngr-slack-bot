package repository.user

import datastore.DataStore
import db.User
import model.user.UserId
import java.time.LocalDate

interface UserRepository {

    fun select(id: UserId): User?

    fun insert(id: UserId)

    fun updateBirthdate(
        id: UserId,
        birthdate: LocalDate
    )

    fun updateIncludeBirthdateYear(
        id: UserId,
        includeBirthdateYear: Boolean
    )

    fun updateEnableBirthdateReminders(
        id: UserId,
        enableBirthdateReminders: Boolean
    )

    fun delete(id: UserId)

    companion object {

        fun create(dataStore: DataStore): UserRepository = UserRepositoryImpl(dataStore)
    }
}

class UserRepositoryImpl(dataStore: DataStore) : UserRepository {

    private val queries = dataStore.userQueries

    override fun select(id: UserId) = queries.select(id).executeAsOneOrNull()

    override fun insert(id: UserId) {
        queries.insert(id)
    }

    override fun updateBirthdate(
        id: UserId,
        birthdate: LocalDate
    ) {
        // TODO insert new user if not exists
        queries.updateBirthdate(birthdate, id)
    }

    override fun updateIncludeBirthdateYear(
        id: UserId,
        includeBirthdateYear: Boolean
    ) {
        // TODO insert new user if not exists
        queries.updateIncludeBirthdateYear(includeBirthdateYear, id)
    }

    override fun updateEnableBirthdateReminders(
        id: UserId,
        enableBirthdateReminders: Boolean
    ) {
        // TODO insert new user if not exists
        queries.updateEnableBirthdateReminders(enableBirthdateReminders, id)
    }

    override fun delete(id: UserId) {
        queries.delete(id)
    }
}
