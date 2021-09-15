(ns game.monsters.types
  (:require
   [game.monsters.movement :as movement]))

(def jelly
  {:type :JELLY
   :color "darkred"
   :movement movement/random})

(def statue
  {:type :STATUE
   :color "grey"
   :movement movement/static})