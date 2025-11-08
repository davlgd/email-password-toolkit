package validation

import org.passay.*
import scala.jdk.CollectionConverters.*

object PasswordGenerator:

  private val generator = new PasswordGenerator()
  private val MinLength = 24
  private val MaxLength = 128
  private val DefaultLength = 32

  private val simpleSpecialChars = new CharacterData:
    def getErrorCode: String = "INSUFFICIENT_SPECIAL"
    def getCharacters: String = "!@#$%^&*()-_=+[]{}|;:,.<>?"

  private val qualityValidator = new PasswordValidator(
    new WhitespaceRule(),
    new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 3, false),
    new IllegalSequenceRule(EnglishSequenceData.Numerical, 3, false),
    new RepeatCharacterRegexRule(3)
  )

  def generate(length: Int = DefaultLength): Either[String, String] =
    if length < MinLength then
      Left(s"Password length must be at least $MinLength characters")
    else if length > MaxLength then
      Left(s"Password length must not exceed $MaxLength characters")
    else
      val minCharPerType = Math.min(4, length / 4)

      val rules = List(
        new CharacterRule(EnglishCharacterData.UpperCase, minCharPerType),
        new CharacterRule(EnglishCharacterData.LowerCase, minCharPerType),
        new CharacterRule(EnglishCharacterData.Digit, minCharPerType),
        new CharacterRule(simpleSpecialChars, minCharPerType)
      )

      try {
        var password = generator.generatePassword(length, rules.asJava)
        var attempts = 0
        val maxAttempts = 100

        while !isValidPassword(password) && attempts < maxAttempts do
          password = generator.generatePassword(length, rules.asJava)
          attempts += 1

        if isValidPassword(password) then Right(password)
        else Left("Failed to generate a valid password after multiple attempts")
      } catch {
        case e: Exception => Left(s"Password generation failed: ${e.getMessage}")
      }

  private def isValidPassword(password: String): Boolean =
    val result = qualityValidator.validate(new PasswordData(password))
    result.isValid
