package search

import java.io.File
import java.util.*
import java.util.function.Predicate
import java.util.function.Predicate.not


fun main(args: Array<String>) {

    val lines = File(args[1]).readLines()
    val people = createPeopleFromLines(lines)
    val indexedData = indexingPeopleData(people)
    val scanner = Scanner(System.`in`)

    do {
        printMenu()
        val option = scanner.nextLine()
        when (option) {
            "1" -> {
                val searchResult = findPerson(people, indexedData, scanner)
                if (searchResult.isNotEmpty()) {
                    printAll("${searchResult.size} persons found:", searchResult)
                } else println("No matching people found.")
            }
            "2" -> printAll("=== List of people ===", people)
            "0" -> println()
            else -> println("Incorrect option! Try again.")
        }
    } while (option != "0")


}

fun createPeopleFromLines(lines: List<String>): List<Person> {
    val persons = mutableListOf<Person>()
    for (line in lines) {
        val splitData = line.split(" ")
        if (splitData.size < 3) {
            persons.add(Person(splitData.first(), splitData.last()))
        } else {
            persons.add(Person(splitData.first(), splitData[1], splitData.last()))
        }
    }
    return persons
}

fun indexingPeopleData(people: List<Person>): Map<String, List<Int>> {
    val peopleMap = mutableMapOf<String, MutableList<Int>>()
    for ((index, person) in people.withIndex()) {
        assignIndexToParam(peopleMap, index, listOf(person.firstName, person.lastName, person.email))
    }
    return peopleMap
}

private fun assignIndexToParam(peopleMap: MutableMap<String, MutableList<Int>>, index: Int, parameters: List<String>) {
    for (param in parameters) {
        val key = param.toLowerCase()
        if (!peopleMap.containsKey(key)) {
            peopleMap[key] = mutableListOf(index)
        } else {
            peopleMap[key]?.add(index)
        }
    }
}

fun printAll(header: String, people: List<Person>) {
    println(header)
    for (person in people) {
        println(person)
    }
}

fun findPerson(people: List<Person>, indexedData: Map<String, List<Int>>, scanner: Scanner): List<Person> {
    println("Select a matching strategy: ALL, ANY, NONE")
    val strategy = Strategy.valueOf(scanner.nextLine())

    println("Enter a name or email to search all suitable people.")
    val dataToSearch = scanner.nextLine().split(" ").map { it.toLowerCase() }

    val searchResultIds = mutableSetOf<Int>()
    for (data in dataToSearch) {
        val indexIds = getIndexIds(data, indexedData)
        if (searchResultIds.isEmpty() && indexIds != null) {
            searchResultIds.addAll(indexIds)
        } else if (indexIds != null) {
            when (strategy) {
                Strategy.ALL -> searchResultIds.retainAll(indexIds)
                Strategy.ANY -> searchResultIds.addAll(indexIds)
                Strategy.NONE -> searchResultIds.addAll(indexIds)
            }
        } else if (strategy == Strategy.ALL) {
            return emptyList()
        }
    }

    return when (strategy) {
        Strategy.ALL -> collectPeople(people, searchResultIds::contains)
        Strategy.ANY -> collectPeople(people, searchResultIds::contains)
        Strategy.NONE -> collectPeople(people) { !searchResultIds.contains(it) }
    }
}

private fun collectPeople(people: List<Person>, predicate: Predicate<Int>): MutableList<Person> {
    val peopleListToReturn = mutableListOf<Person>()
    for (i in people.indices) {
        if (predicate.test(i)) {
            peopleListToReturn.add(people[i])
        }
    }
    return peopleListToReturn
}

private fun getIndexIds(data: String, indexedData: Map<String, List<Int>>): List<Int>? {
    return if (indexedData.containsKey(data)) {
        indexedData[data]
    } else null
}

fun printMenu() {
    println("=== Menu ===\n1. Search information.\n2. Print all data.\n0. Exit.")
}

class Person(val firstName: String, val lastName: String, val email: String = "") {
    override fun toString(): String {
        return "$firstName $lastName $email".trim()
    }
}

enum class Strategy {
    ALL, ANY, NONE
}
