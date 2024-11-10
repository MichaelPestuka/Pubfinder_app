package com.example.itu

class User(
    var id: String = "0",
    var username: String = "Nobody",
    var password_hash: String = "0",
    var blacklisted_pubs: Array<String> = arrayOf<String>(),
    var timetable: Array<String> = arrayOf<String>()
)
{
    override fun toString(): String
    {
        return "name: " + username + " blocked times: " + timetable.joinToString(", ")
    }
}