package lila.round

import akka.actor._
import chess.Color

import lila.game.{ Game, GameRepo, Pov }

private[round] final class CheatDetector {

  def apply(game: Game): Fu[Option[Color]] = interesting(game) ?? {
    GameRepo findMirror game map {
      _ ?? { mirror =>
        mirror.players map (p => p -> p.userId) collectFirst {
          case (player, Some(userId)) => game.players find (_.userId == player.userId) map { cheater =>
            lila.log("cheat").info(s"${cheater.color} ($userId) @ ${game.id} uses ${mirror.id}")
            cheater.color
          }
        } flatten
      }
    }
  }

  private def gameUrl(gameId: String) = s"https://lichess.org/${gameId}"

  private def interesting(game: Game) =
    game.rated && game.turns == 15 && game.speed != chess.Speed.Bullet
}
