package app.backend.controller

import app.backend.JWT_SECRET
import app.backend.dtos.AuthUserDTO
import app.backend.dtos.LoginDTO
import app.backend.dtos.RegisterDTO
import app.backend.errors.LoginException
import app.backend.errors.RegistrationException
import app.backend.models.DbUser
import app.backend.services.UserService
import app.backend.util.cleanEmail
import app.backend.util.cleanName
import app.backend.util.cleanPassword
import app.backend.util.isEmailValid
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Date
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/auth")
class AuthController(private val userService: UserService) {

  @PostMapping("/register")
  fun register(@RequestBody body: RegisterDTO): ResponseEntity<DbUser> {
    val dbUser = DbUser(
        firstname = cleanName(body.firstname),
        lastname = cleanName(body.lastname),
        email = cleanName(body.lastname),
    )
    user.password = cleanPassword(body.password)

    if (!isEmailValid(dbUser.email)) {
      throw RegistrationException("Email is invalid!")
    }

    if (userService.findByEmail(dbUser.email) != null) {
      throw RegistrationException("Email in use!")
    }

    return ResponseEntity.ok(userService.save(dbUser))
  }

  @PostMapping("/login")
  fun login(request: HttpServletRequest, @RequestBody body: LoginDTO): ResponseEntity<AuthUserDTO> {
    val email = cleanEmail(body.email)
    val password = cleanPassword(body.password)

    val user = (userService.findByEmail(email)
        ?: throw LoginException("Email not found!"))

    if (!user.comparePassword(password)) {
      throw LoginException("Invalid password!")
    }

    val issuer = user.id.toString()

    val jwt = Jwts.builder()
        .setIssuer(issuer)
        .setExpiration(Date(System.currentTimeMillis() + 60 * 60 * 24 * 1000))
        .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
        .compact()

    return ResponseEntity.ok(AuthUserDTO(
        token = jwt,
        id = user.id,
        firstname = user.firstname,
        lastname = user.lastname,
        email = user.email
    ))
  }

  @PostMapping("/logout")
  fun logout(request: HttpServletRequest): ResponseEntity<String> {
    request.session.removeAttribute("token")
    return ResponseEntity.ok("Successfully logged out.")
  }
}