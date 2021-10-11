package service.test

import datastore.DataStore
import db.Test

interface TestService {

    fun getTestEntries(): List<Test>

    fun insertTest(test: Test)

    fun deleteTests()

    companion object {

        fun create(dataStore: DataStore): TestService = TestServiceImpl(dataStore)
    }
}

class TestServiceImpl(dataStore: DataStore) : TestService {

    private val queries = dataStore.testQueries

    override fun getTestEntries() = queries.selectAll().executeAsList()

    override fun insertTest(test: Test) {
        queries.insert(test.name, test.number)
    }

    override fun deleteTests() {
        queries.delete()
    }
}
