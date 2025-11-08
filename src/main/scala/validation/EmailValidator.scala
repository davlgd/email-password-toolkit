package validation

object EmailValidator:
  private val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r

  def isValid(email: String): Boolean =
    emailRegex.matches(email)
