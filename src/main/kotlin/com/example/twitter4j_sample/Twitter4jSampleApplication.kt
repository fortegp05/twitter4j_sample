package com.example.twitter4j_sample

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.User
import twitter4j.auth.RequestToken
import javax.annotation.PostConstruct
import javax.servlet.http.HttpSession

@SpringBootApplication
@Controller
class Twitter4jSampleApplication {

	lateinit var twitter: Twitter

	@PostConstruct
	fun ctor() {
		twitter = TwitterFactory.getSingleton()
	}

	@Autowired
	var session: HttpSession? = null

	@GetMapping("index")
	fun index(model: Model): String {
		return "index"
	}

	@GetMapping("login")
	fun login(): String {
		val requestToken = twitter.getOAuthRequestToken("http://localhost:8080/callback")
		session?.setAttribute("requestToken", requestToken)
		return "redirect:${requestToken.authenticationURL}"
	}

	@GetMapping("callback")
	fun callback2(
			@RequestParam(value = "oauth_token") oauthToken: String,
			@RequestParam(value = "oauth_verifier") oauthVerifier: String
	): String {
		val requestToken: RequestToken = session?.getAttribute("requestToken") as RequestToken
		twitter.getOAuthAccessToken(requestToken, oauthVerifier)

		session?.removeAttribute("requestToken")

		val user: User = twitter.verifyCredentials()
		session?.setAttribute("user", user)

		return "redirect:/user"
	}

	@GetMapping("user")
	fun user(model: Model): String {
		val user = session?.getAttribute("user")
		if (!(user is User)) return "index"

		model.addAttribute("screenName", user.screenName)

		return "user"
	}

	@GetMapping("logout")
	fun logout(model: Model): String {
		session?.invalidate()
		return "redirect:index"
	}
}

fun main(args: Array<String>) {
	runApplication<Twitter4jSampleApplication>(*args)
}
