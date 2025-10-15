/**
 * @author Michael Peštuka (xpestu01)
 */
package com.example.itu

/**
 * Class holding one user data
 */
class User(
    var id: String = "1",
    var username: String = "Nobody",
    var password_hash: String = "0",
    var blacklisted_pubs: Array<String> = arrayOf<String>(),
    var timetable: Array<String> = arrayOf<String>()
)
