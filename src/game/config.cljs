(ns game.config)

(def input-keys {38 :UP
                 40 :DOWN
                 37 :LEFT
                 39 :RIGHT
                 79 :O})

(def CANVAS-ID "gameCanvas")
(def CANVAS-WIDTH 500)
(def CANVAS-HEIGHT 500)
(def TILE-SIZE 16)

;; entity types
(def player
  {:id :player
   :color "red"
   :health 100
   :attack 5
   :direction :UP})

(def jelly
  {:type :JELLY
   :color "green"
   :health 10
   :attack 5
   :movement :RANDOM})

(def statue
  {:type :STATUE
   :color "grey"
   :health 10
   :attack 5
   :movement :STATIC})