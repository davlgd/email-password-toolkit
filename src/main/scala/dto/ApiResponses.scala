package dto

// Request models
case class EmailValidationRequest(email: String)
case class PasswordValidationRequest(password: String)

// Response models
case class EmailValidationResponse(valid: Boolean)

case class PasswordStrengthResponse(
  entropy: Double,
  strength: String,
  valid: Boolean,
  errors: List[String] = List.empty
)

case class PasswordGenerationResponse(
  password: String,
  entropy: Double,
  strength: String,
  valid: Boolean
)

case class ErrorResponse(message: String)
