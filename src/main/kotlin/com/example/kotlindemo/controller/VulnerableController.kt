package com.example.kotlindemo.controller

import org.springframework.web.bind.annotation.*
import java.io.File
import java.sql.DriverManager
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/vuln")
class VulnerableController {

    // SQL Injection - user input directly in SQL query
    @GetMapping("/search")
    fun searchArticles(@RequestParam q: String): String {
        val conn = DriverManager.getConnection("jdbc:h2:mem:testdb")
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM articles WHERE title LIKE '%" + q + "%'")
        val results = StringBuilder()
        while (rs.next()) {
            results.append(rs.getString("title")).append("\n")
        }
        return results.toString()
    }

    // Command Injection - user input in Runtime.exec
    @GetMapping("/ping")
    fun pingHost(@RequestParam host: String): String {
        val process = Runtime.getRuntime().exec("ping -c 1 " + host)
        return process.inputStream.bufferedReader().readText()
    }

    // Path Traversal - user input in file path
    @GetMapping("/file")
    fun readFile(@RequestParam path: String): String {
        val file = File("/data/uploads/" + path)
        return file.readText()
    }

    // XSS - reflected user input without encoding
    @GetMapping("/greet")
    fun greetUser(@RequestParam name: String, response: HttpServletResponse): String {
        response.contentType = "text/html"
        return "<html><body><h1>Hello, " + name + "!</h1></body></html>"
    }

    // SSRF - user-controlled URL
    @GetMapping("/fetch")
    fun fetchUrl(@RequestParam url: String): String {
        val connection = java.net.URL(url).openConnection()
        return connection.getInputStream().bufferedReader().readText()
    }

    // Hardcoded credentials
    @PostMapping("/login")
    fun login(@RequestParam user: String, @RequestParam pass: String): String {
        val dbPassword = "admin123!"
        val dbUser = "root"
        val conn = DriverManager.getConnection("jdbc:mysql://db:3306/app", dbUser, dbPassword)
        val stmt = conn.prepareStatement("SELECT * FROM users WHERE username='" + user + "' AND password='" + pass + "'")
        val rs = stmt.executeQuery()
        return if (rs.next()) "Login OK" else "Failed"
    }

    // Log Injection
    @PostMapping("/log")
    fun logAction(@RequestParam action: String, request: HttpServletRequest): String {
        val logger = java.util.logging.Logger.getLogger("VulnApp")
        logger.info("User action: " + action + " from IP: " + request.remoteAddr)
        return "Logged"
    }

    // Open Redirect
    @GetMapping("/redirect")
    fun redirectUser(@RequestParam target: String, response: HttpServletResponse) {
        response.sendRedirect(target)
    }
}
