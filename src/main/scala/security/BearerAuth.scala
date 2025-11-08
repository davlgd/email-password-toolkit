package security

object BearerAuth:
  private val ValidToken = "valid-token"

  def extractToken(authHeaderOpt: Option[String]): Either[Unit, Unit] =
    authHeaderOpt
      .filter(_.startsWith("Bearer "))
      .map(_.substring(7))
      .filter(_ == ValidToken)
      .map(_ => ())
      .toRight(())
