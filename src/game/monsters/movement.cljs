(ns game.monsters.movement
  "Set of movement functions for monsters: (monster) => [x y]"
  (:require
   [game.tile-map :as tile]))

(defn random
  "Random movement to any surrounding square."
  [monster]
  (let [x (+ (:x monster) (dec (rand-int 3)))
        y (+ (:y monster) (dec (rand-int 3)))]
    (if (tile/is? :BLANK x y)
      [x y]
      [(:x monster) (:y monster)])))

(defn static
  "Can't move! Just returns the same position."
  [monster]
  [(:x monster) (:y monster)])