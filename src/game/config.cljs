(ns game.config)

(def CANVAS-ID "gameCanvas")
(def CANVAS-WIDTH 500)
(def CANVAS-HEIGHT 500)
(def TILE-SIZE 16)

;; monster types 
(def jelly
  {:type :JELLY
   :color "darkred"
   :movement :RANDOM})

(def statue
  {:type :STATUE
   :color "grey"
   :movement :STATIC})