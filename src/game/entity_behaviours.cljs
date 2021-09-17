(ns game.entity-behaviours
  (:require
   [game.state :as state]
   [game.util :refer [adjacent-squares]]))

(defmulti movement :movement)

(defmethod movement :STATIC
  [monster]
  [(:x monster) (:y monster)])

(defmethod movement :RANDOM
  [monster]
  (let [x (:x monster)
        y (:y monster)
        choices (adjacent-squares x y)
        [x2 y2] (get choices (rand-int 4))]
    (cond
      (state/player-at? x2 y2) [x y]
      (state/tile-is? :BLANK x2 y2) [x2 y2]
      :else [x y])))

