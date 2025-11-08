package validation

import org.passay.*
import scala.jdk.CollectionConverters.*

case class PasswordStrength(entropy: Double, strength: String, valid: Boolean, errors: List[String])

object PasswordStrengthAnalyzer:

  private val validator = new PasswordValidator(
    new LengthRule(24, Int.MaxValue),
    new CharacterRule(EnglishCharacterData.UpperCase, 1),
    new CharacterRule(EnglishCharacterData.LowerCase, 1),
    new CharacterRule(EnglishCharacterData.Digit, 1),
    new CharacterRule(EnglishCharacterData.Special, 1),
    new WhitespaceRule(),
    new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 3, false),
    new IllegalSequenceRule(EnglishSequenceData.Numerical, 3, false),
    new RepeatCharacterRegexRule(3)
  )

  def analyze(password: String): PasswordStrength =
    val passwordData = new PasswordData(password)
    val result = validator.validate(passwordData)
    val entropy = validator.estimateEntropy(passwordData)
    val strength = classifyStrength(entropy)
    val valid = result.isValid
    val errors = if valid then List.empty else result.getDetails.asScala.map(_.getErrorCode).toList

    PasswordStrength(entropy, strength, valid, errors)

  private def classifyStrength(entropy: Double): String =
    if entropy < 20 then "weak"
    else if entropy < 32 then "medium"
    else "strong"
