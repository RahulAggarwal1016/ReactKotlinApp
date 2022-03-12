package app.backend.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity(name = "DbUser")
@Table(name = "db_users")
class DbUser(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int = 0,

    @Column(nullable = false)
    val firstname: String,

    @Column(nullable = false)
    val lastname: String,

    @Column(unique = true, nullable = false)
    val email: String,

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    val lists: MutableList<DbList>,

    @Column(nullable = false)
    @JsonIgnore
    val password : String
) {
  @Column(nullable = false)
  var password: String = ""
    @JsonIgnore // ignore password field when returning a user
    get() = field
    set(value) {
      val passwordEncoder = BCryptPasswordEncoder()
      field = passwordEncoder.encode(value)
    }

  fun comparePassword(password: String): Boolean = BCryptPasswordEncoder().matches(password, this.password)
}