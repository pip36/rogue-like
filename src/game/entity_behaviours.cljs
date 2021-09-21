(ns game.entity-behaviours
  (:require
   [game.state :as state]
   [game.util :refer [adjacent-squares add-coordinates normalize]]))

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
      (state/get-entity-at x2 y2) [x y]
      (state/tile-is? :BLANK x2 y2) [x2 y2]
      :else [x y])))


(defmethod movement :FOLLOW
  [monster]
  (let [player (state/get-player)
        x (:x monster)
        y (:y monster)
        x-diff (- (:x player) x)
        y-diff (- (:y player) y)
        [x2 y2] (if (> (Math/abs x-diff) (Math/abs y-diff))
                  (add-coordinates [(normalize x-diff) 0] [x y])
                  (add-coordinates [0 (normalize y-diff)] [x y]))]
    (cond
      (state/player-at? x2 y2) [x y]
      (state/get-entity-at x2 y2) [x y]
      (state/tile-is? :BLANK x2 y2) [x2 y2]
      :else [x y])))

